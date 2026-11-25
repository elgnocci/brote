/* Escena de Brote: un balcón flotante estilo Tiny Diorama.
   API: const s = new BroteScene(canvas, {animate, transparent});
        s.setState({stage, mood, leaves, flowers, fireflies}); s.dropLeaf(); */
(function () {
  THREE.ColorManagement.legacyMode = false;

  const MOODS = {
    happy: { h: 1, c: 0, eye: 1 },
    tired: { h: 0.72, c: 0, eye: 0.35 },
    sick: { h: 0.12, c: 0, eye: 0.15 },
    recovering: { h: 0.55, c: 0, eye: 0.7 },
    celebrating: { h: 1, c: 1, eye: 1 },
  };
  const SKY = { happy: '#cfe6ef', tired: '#d6e0e0', sick: '#cbc6ba', recovering: '#d3e3e4', celebrating: '#ffe6b3' };
  const MAX_SLOTS = 10;

  const col = (a) => new THREE.Color(a);
  const mix = (a, b, t) => col(a).lerp(col(b), t);
  const matte = (c) => new THREE.MeshStandardMaterial({ color: c, roughness: 0.92, metalness: 0 });
  const solid = (m) => { m.castShadow = true; m.receiveShadow = true; return m; };
  const box = (w, h, d, m) => solid(new THREE.Mesh(new THREE.BoxGeometry(w, h, d), m));
  const cyl = (rt, rb, h, m, seg = 16) => solid(new THREE.Mesh(new THREE.CylinderGeometry(rt, rb, h, seg), m));
  const ball = (r, m, w = 20, hh = 14) => solid(new THREE.Mesh(new THREE.SphereGeometry(r, w, hh), m));
  const at = (o, x, y, z) => { o.position.set(x, y, z); return o; };

  /* Una hoja: elipsoide aplanado que sale del tallo hacia un lado. */
  function leafMesh(color) {
    const g = new THREE.Group();
    const l = ball(0.22, matte(color), 18, 12);
    l.scale.set(1, 0.28, 0.55);
    l.position.x = 0.2;
    g.add(l);
    return g;
  }
  function flowerMesh(petal, core) {
    const g = new THREE.Group();
    for (let i = 0; i < 5; i++) {
      const a = (i / 5) * Math.PI * 2;
      const p = ball(0.075, matte(petal), 12, 8);
      p.scale.set(1, 0.5, 1);
      p.position.set(0.16 + Math.cos(a) * 0.08, 0.02, Math.sin(a) * 0.08);
      g.add(p);
    }
    g.add(at(ball(0.05, matte(core), 12, 8), 0.16, 0.05, 0));
    return g;
  }

  class BroteScene {
    constructor(canvas, opts = {}) {
      this.canvas = canvas;
      this.animate = opts.animate !== false;
      this.transparent = !!opts.transparent;
      this.renderer = new THREE.WebGLRenderer({ canvas, antialias: true, alpha: this.transparent, preserveDrawingBuffer: !!opts.preserve });
      this.renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
      this.renderer.shadowMap.enabled = true;
      this.renderer.shadowMap.type = THREE.PCFSoftShadowMap;
      this.renderer.outputEncoding = THREE.sRGBEncoding;
      if (this.transparent) this.renderer.setClearColor(0x000000, 0);
      this.scene = new THREE.Scene();
      this.camera = new THREE.PerspectiveCamera(30, 1, 0.1, 100);
      this.scene.add(new THREE.HemisphereLight(0xffffff, 0x8a7a60, 0.75));
      const sun = new THREE.DirectionalLight(0xffffff, 0.95);
      sun.position.set(3, 7, 5);
      sun.castShadow = true;
      sun.shadow.mapSize.set(1024, 1024);
      Object.assign(sun.shadow.camera, { left: -4, right: 4, top: 4, bottom: -4 });
      this.scene.add(sun);
      this.world = new THREE.Group();
      this.scene.add(this.world);
      this.fx = new THREE.Group();
      this.scene.add(this.fx);
      this.falling = [];
      this.t = 0;
      this.state = { stage: 0, mood: 'happy', leaves: 6, flowers: 0, fireflies: 0 };
      this.resize();
      this.setState(this.state);
      if (this.animate) {
        let last = performance.now();
        const loop = (now) => {
          const dt = Math.min(0.05, (now - last) / 1000);
          last = now;
          this.tick(dt);
          requestAnimationFrame(loop);
        };
        requestAnimationFrame(loop);
      }
    }

    resize() {
      const w = this.canvas.clientWidth || this.canvas.width, h = this.canvas.clientHeight || this.canvas.height;
      this.renderer.setSize(w, h, false);
      const aspect = w / h;
      this.camera.aspect = aspect;
      /* Encuadre: que entre el balcón a lo ancho (≈5.2) y a lo alto (≈4.6). */
      const vf = THREE.MathUtils.degToRad(this.camera.fov);
      const hf = 2 * Math.atan(Math.tan(vf / 2) * aspect);
      const dist = Math.max(2.6 / Math.tan(hf / 2), 2.3 / Math.tan(vf / 2));
      const dir = new THREE.Vector3(0, 0.75, 1).normalize();
      this.camera.position.copy(dir.multiplyScalar(dist)).add(new THREE.Vector3(0, 1.0, 0));
      this.camera.lookAt(0, 1.0, 0);
      this.camera.updateProjectionMatrix();
      this.render();
    }

    setState(next) {
      this.state = Object.assign({}, this.state, next);
      const s = this.state;
      const m = MOODS[s.mood] || MOODS.happy;
      this.m = m;
      if (!this.transparent) this.scene.background = col(SKY[s.mood] || SKY.happy);
      this.world.clear();
      this.fx.clear();
      this.eyes = [];
      this.puffs = [];
      this.flies = [];
      this.bird = null;
      this.build(s, m);
      this.render();
    }

    build(s, m) {
      const W = this.world, h = m.h, sick = 1 - h;
      const add = (o) => { W.add(o); return o; };

      /* --- el balcón --- */
      const tile = matte(mix('#c9b79c', '#e3cfae', h));
      add(at(box(4.4, 0.22, 2.8, tile), 0, -0.11, 0));
      for (let i = -1; i <= 1; i++) add(at(box(0.015, 0.005, 2.8, matte('#b8a283')), i * 1.1, 0.002, 0));
      for (const z of [-0.7, 0.7]) add(at(box(4.4, 0.005, 0.015, matte('#b8a283')), 0, 0.002, z));
      add(at(box(4.4, 0.5, 2.8, matte('#b3aca1')), 0, -0.47, 0));
      const under = add(at(cyl(1.6, 0.5, 1.1, matte('#a39c91'), 6), 0, -1.2, 0));
      under.rotation.y = Math.PI / 6;

      const wallM = matte(mix('#d8cfc0', '#f3e6d1', h));
      add(at(box(4.4, 3.0, 0.25, wallM), 0, 1.5, -1.52));
      add(at(box(1.5, 2.1, 0.05, matte('#6d6a64')), 0.9, 1.15, -1.37));
      add(at(box(1.38, 1.98, 0.04, matte(mix('#8c9aa0', '#a9d4e6', h))), 0.9, 1.15, -1.35));
      add(at(box(0.04, 1.98, 0.06, matte('#6d6a64')), 0.9, 1.15, -1.33));

      const rail = matte('#4b4f55');
      add(at(box(4.4, 0.06, 0.08, rail), 0, 0.68, 1.36));
      /* Baranda de vidrio al frente, para que no tape a Brote. */
      const glass = new THREE.MeshStandardMaterial({ color: 0xbfe3ee, roughness: 0.1, transparent: true, opacity: 0.18, depthWrite: false });
      add(at(new THREE.Mesh(new THREE.BoxGeometry(4.3, 0.62, 0.02), glass), 0, 0.34, 1.36));
      for (const x of [-1.45, 0, 1.45]) add(at(cyl(0.02, 0.02, 0.68, rail, 6), x, 0.34, 1.36));
      for (const x of [-2.18, 2.18]) {
        add(at(box(0.06, 0.06, 2.8, rail), x, 0.95, 0));
        for (let z = -1.1; z <= 1.2; z += 0.3) add(at(cyl(0.018, 0.018, 0.95, rail, 6), x, 0.475, z));
      }

      /* --- etapas --- */
      const leafC = mix('#a9a56a', '#5fb54a', h);
      if (s.stage >= 1) {
        const vm = matte(leafC);
        for (let x = -2.1; x <= 2.1; x += 0.16) {
          const v = ball(0.07, vm, 10, 8);
          v.scale.set(1, 0.6, 1.2);
          add(at(v, x, 0.72 + Math.sin(x * 5) * 0.03, 1.36 + Math.cos(x * 7) * 0.05));
          if (s.stage >= 6 && Math.round(x * 10) % 3 === 0) add(at(ball(0.05, matte(mix('#9a8f86', '#ff8fb6', h)), 10, 8), x, 0.77, 1.42));
        }
        for (let y = 0.1; y < 0.7; y += 0.14) add(at(ball(0.065, vm, 10, 8), -2.12 + Math.sin(y * 9) * 0.04, y, 1.36));
      }
      if (s.stage >= 2) {
        const wood = matte('#a8724a');
        const chair = new THREE.Group();
        chair.add(at(box(0.5, 0.05, 0.5, wood), 0, 0.45, 0));
        chair.add(at(box(0.5, 0.5, 0.05, wood), 0, 0.72, -0.23));
        for (const [x, z] of [[-0.22, -0.22], [0.22, -0.22], [-0.22, 0.22], [0.22, 0.22]]) chair.add(at(cyl(0.025, 0.025, 0.45, wood, 6), x, 0.225, z));
        chair.position.set(-1.45, 0, -0.35);
        chair.rotation.y = 0.5;
        add(chair);
        const table = new THREE.Group();
        table.add(at(cyl(0.3, 0.3, 0.04, wood, 20), 0, 0.55, 0));
        table.add(at(cyl(0.03, 0.03, 0.55, wood, 8), 0, 0.275, 0));
        table.add(at(cyl(0.16, 0.16, 0.03, wood, 16), 0, 0.015, 0));
        table.add(at(cyl(0.05, 0.04, 0.1, matte('#ffffff'), 12), 0.08, 0.62, 0.02));
        table.position.set(-0.75, 0, -0.75);
        add(table);
      }
      if (s.stage >= 3) {
        add(at(cyl(0.28, 0.22, 0.55, matte('#5c6670'), 16), 1.75, 0.275, -0.95));
        const spike = matte(mix('#9c9a5c', '#3f8f3a', h));
        for (let i = 0; i < 9; i++) {
          const a = (i / 9) * Math.PI * 2;
          const c = solid(new THREE.Mesh(new THREE.ConeGeometry(0.07, 0.9 + (i % 3) * 0.2, 6), spike));
          c.position.set(1.75 + Math.cos(a) * 0.1, 0.95, -0.95 + Math.sin(a) * 0.1);
          c.rotation.z = Math.cos(a) * (0.3 + sick * 0.5);
          c.rotation.x = -Math.sin(a) * (0.3 + sick * 0.5);
          add(c);
        }
      }
      if (s.stage >= 4) {
        const lamp = new THREE.Group();
        lamp.add(at(box(0.22, 0.3, 0.22, matte('#3e3a36')), 0, 0, 0));
        const glow = new THREE.MeshStandardMaterial({ color: h > 0.5 ? 0xffe08a : 0x6d6a60, emissive: h > 0.5 ? 0xffc24a : 0x000000, emissiveIntensity: 0.8 });
        lamp.add(at(new THREE.Mesh(new THREE.BoxGeometry(0.16, 0.22, 0.16), glow), 0, 0, 0.04));
        lamp.position.set(-0.3, 2.05, -1.28);
        add(lamp);
      }
      if (s.stage >= 5) {
        const feeder = new THREE.Group();
        feeder.add(at(cyl(0.12, 0.08, 0.12, matte('#c9a26b'), 12), 0, 0, 0));
        feeder.add(at(cyl(0.005, 0.005, 0.35, matte('#555'), 4), 0, 0.22, 0));
        feeder.position.set(1.6, 0.48, 1.36);
        add(feeder);
        if (h > 0.5) {
          const bird = new THREE.Group();
          bird.add(at(ball(0.1, matte('#e2754b'), 14, 10), 0, 0, 0));
          bird.add(at(ball(0.07, matte('#e2754b'), 12, 8), 0.09, 0.08, 0));
          const beak = solid(new THREE.Mesh(new THREE.ConeGeometry(0.025, 0.07, 6), matte('#f2c14e')));
          beak.rotation.z = -Math.PI / 2;
          bird.add(at(beak, 0.18, 0.08, 0));
          bird.add(at(ball(0.015, matte('#1d1a17'), 8, 6), 0.12, 0.11, 0.05));
          bird.position.set(1.15, 0.81, 1.36);
          add(bird);
          this.bird = bird;
        }
      }
      if (s.stage >= 6) {
        ['#e8574b', '#f39a3c', '#f6d14b', '#6cc36a', '#4fa3e0', '#8a6fd1'].forEach((c, i) => {
          const arc = new THREE.Mesh(new THREE.TorusGeometry(1.75 - i * 0.08, 0.04, 8, 64, Math.PI), matte(mix('#9a9a9a', c, h)));
          arc.position.set(0, 2.25, -1.7);
          W.add(arc);
        });
      }

      /* --- Brote --- */
      const pet = new THREE.Group();
      pet.position.set(0.15, 0, 0.05);
      pet.scale.setScalar(1.35);
      add(pet);
      const potM = matte('#e07352');
      pet.add(at(cyl(0.46, 0.34, 0.6, potM, 24), 0, 0.3, 0));
      const rim = at(solid(new THREE.Mesh(new THREE.TorusGeometry(0.46, 0.06, 10, 32), potM)), 0, 0.6, 0);
      rim.rotation.x = Math.PI / 2;
      pet.add(rim);
      pet.add(at(cyl(0.43, 0.43, 0.02, matte('#6b4a32'), 24), 0, 0.58, 0));
      const eyeM = matte('#1d1a17');
      for (const sx of [-1, 1]) {
        const e = at(new THREE.Mesh(new THREE.SphereGeometry(0.055, 16, 12), eyeM), sx * 0.15, 0.36, 0.41);
        pet.add(e);
        this.eyes.push(e);
      }
      const mouth = this.mouth(s.mood, eyeM);
      if (mouth) pet.add(mouth);

      const slots = Math.min(MAX_SLOTS, Math.max(1, s.leaves + s.flowers));
      const stemH = 0.35 + slots * 0.09;
      const stemC = mix('#a9a56a', '#4f9f3f', h);
      const plant = new THREE.Group();
      plant.position.y = 0.6;
      plant.rotation.z = sick * 0.35;
      pet.add(plant);
      plant.add(at(cyl(0.035, 0.05, stemH, matte(stemC), 10), 0, stemH / 2, 0));
      this.slotPositions = [];
      const petal = mix('#a99a96', '#ff8fb6', h), core = mix('#a99f80', '#ffd23f', h);
      /* Las flores van abajo (ganadas, firmes) y las hojas arriba (el cupo de hoy). */
      const items = [];
      for (let i = 0; i < s.flowers; i++) items.push('flower');
      for (let i = 0; i < s.leaves; i++) items.push('leaf');
      items.forEach((kind, i) => {
        const side = i % 2 === 0 ? 1 : -1;
        const y = 0.22 + (i / Math.max(1, slots - 1)) * (stemH - 0.3);
        const piv = new THREE.Group();
        piv.position.y = y;
        piv.rotation.y = side > 0 ? 0 : Math.PI;
        piv.rotation.y += (i % 4 < 2 ? 0.5 : -0.5);
        const droop = kind === 'leaf' ? -0.15 - sick * 0.9 : -0.05;
        piv.rotation.z = droop;
        piv.add(kind === 'leaf' ? leafMesh(leafC) : flowerMesh(petal, core));
        plant.add(piv);
        this.slotPositions.push({ kind, piv });
      });
      if (m.c > 0 || (s.leaves === 0 && s.flowers > 0)) {
        const top = flowerMesh(petal, core);
        top.scale.setScalar(1.6);
        top.position.set(-0.25, stemH, 0);
        plant.add(top);
      } else {
        plant.add(at(ball(0.05, matte(stemC), 10, 8), 0, stemH, 0));
      }
      this.pet = pet;
      this.plant = plant;

      /* --- humo y luciérnagas --- */
      if (sick > 0.5) {
        const sm = new THREE.MeshStandardMaterial({ color: 0x6f6f6f, roughness: 1, transparent: true, opacity: 0.6 });
        for (let i = 0; i < 7; i++) {
          const p = new THREE.Mesh(new THREE.SphereGeometry(0.2 + (i % 3) * 0.07, 16, 12), sm);
          p.userData.o = i / 7;
          this.fx.add(p);
          this.puffs.push(p);
        }
      }
      const fm = new THREE.MeshBasicMaterial({ color: 0xffd23f });
      const hm = new THREE.MeshBasicMaterial({ color: 0xffc83a, transparent: true, opacity: 0.35, depthWrite: false });
      for (let i = 0; i < Math.min(s.fireflies, 30); i++) {
        const f = new THREE.Group();
        f.add(new THREE.Mesh(new THREE.SphereGeometry(0.04, 8, 6), fm));
        f.add(new THREE.Mesh(new THREE.SphereGeometry(0.14, 10, 8), hm));
        f.userData = { a: i * 2.4, r: 1.0 + (i % 5) * 0.25, y: 1.2 + (i % 4) * 0.35, sp: 0.25 + (i % 3) * 0.1 };
        this.fx.add(f);
        this.flies.push(f);
      }
      this.placeFx(0);
    }

    mouth(mood, mat) {
      let mesh;
      if (mood === 'celebrating') {
        mesh = new THREE.Mesh(new THREE.CircleGeometry(0.085, 20, Math.PI, Math.PI), mat);
        mesh.position.set(0, 0.25, 0.402);
      } else if (mood === 'tired') {
        mesh = new THREE.Mesh(new THREE.BoxGeometry(0.11, 0.026, 0.01), mat);
        mesh.position.set(0, 0.23, 0.4);
      } else {
        const smile = mood !== 'sick';
        const r = mood === 'recovering' ? 0.05 : 0.07;
        mesh = new THREE.Mesh(new THREE.TorusGeometry(r, 0.02, 8, 24, Math.PI), mat);
        if (smile) mesh.rotation.z = Math.PI;
        mesh.position.set(0, smile ? 0.24 + r * 0.6 : 0.18, 0.4);
      }
      mesh.rotation.x = -0.2;
      return mesh;
    }

    /** La hoja de más arriba se pone café y cae girando. Llamar antes de setState con una hoja menos. */
    dropLeaf() {
      const leaves = (this.slotPositions || []).filter((p) => p.kind === 'leaf');
      if (!leaves.length) return;
      const src = leaves[leaves.length - 1].piv;
      src.updateMatrixWorld(true);
      const wp = new THREE.Vector3(), wq = new THREE.Quaternion(), ws = new THREE.Vector3();
      src.matrixWorld.decompose(wp, wq, ws);
      const mat = matte('#5fb54a');
      mat.transparent = true;
      const leaf = leafMesh('#5fb54a');
      leaf.traverse((o) => { if (o.isMesh) o.material = mat; });
      leaf.position.copy(wp);
      leaf.quaternion.copy(wq);
      leaf.scale.copy(ws);
      leaf.userData = { t: 0, vx: (Math.random() - 0.5) * 0.4, mat };
      this.scene.add(leaf);
      this.falling.push(leaf);
    }

    placeFx(t) {
      this.puffs.forEach((p) => {
        const f = (t * 0.12 + p.userData.o) % 1;
        p.position.set(0.15 + Math.sin(f * 6 + p.userData.o * 9) * 0.6, 2.2 + f * 1.6, 0.35 + Math.cos(f * 5 + p.userData.o * 7) * 0.3);
        p.scale.setScalar(0.6 + f * 0.9);
      });
      this.flies.forEach((f) => {
        const u = f.userData;
        const a = u.a + t * u.sp;
        f.position.set(Math.cos(a) * u.r * 1.6, u.y + Math.sin(t * 1.3 + u.a) * 0.15, Math.sin(a) * u.r * 0.7);
        f.children[1].material.opacity = 0.25 + 0.25 * (1 + Math.sin(t * 3 + u.a)) / 2;
      });
    }

    tick(dt) {
      this.t += dt;
      const t = this.t;
      const blink = (t % 4) < 0.12 ? 0.1 : 1;
      for (const e of this.eyes) e.scale.y = Math.max(0.08, this.m.eye * blink);
      if (this.plant) this.plant.rotation.x = Math.sin(t * 1.4) * 0.03;
      if (this.bird) this.bird.position.y = 0.81 + Math.abs(Math.sin(t * 3)) * 0.04 * ((t % 5) < 1 ? 1 : 0);
      this.placeFx(t);
      const brown = col('#8a6a3a');
      this.falling = this.falling.filter((l) => {
        const u = l.userData;
        u.t += dt;
        u.mat.color.lerp(brown, Math.min(1, dt * 3));
        if (u.t > 0.35) {
          const ft = u.t - 0.35;
          l.position.y -= dt * (0.6 + ft * 0.8);
          l.position.x += Math.sin(ft * 5) * dt * 0.6 + u.vx * dt;
          l.rotation.z += dt * 2.5;
          l.rotation.x += dt * 1.2;
        }
        if (l.position.y < 0.03) {
          l.position.y = 0.03;
          u.mat.opacity = Math.max(0, u.mat.opacity - dt * 0.6);
        }
        if (u.mat.opacity <= 0) {
          this.scene.remove(l);
          return false;
        }
        return true;
      });
      this.render();
    }

    render() {
      this.renderer.render(this.scene, this.camera);
    }
  }

  window.BroteScene = BroteScene;
  window.BroteScene.MOODS = Object.keys(MOODS);
})();
