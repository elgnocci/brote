/* Escena Tiny Diorama de Brote: 7 etapas de isla x 5 estados de ánimo. */
window.Island = (function () {
  THREE.ColorManagement.legacyMode = false;
  const MOODS = {
    happy: { h: 1, c: 0, eye: 1 },
    tired: { h: 0.72, c: 0, eye: 0.35 },
    sick: { h: 0.12, c: 0, eye: 0.15 },
    recovering: { h: 0.55, c: 0, eye: 0.7 },
    celebrating: { h: 1, c: 1, eye: 1 },
  };
  const STAGES = ['Isla pelada', 'Un árbol', 'Caminito', 'Segundo árbol', 'Casita', 'Laguna', 'Isla completa'];
  const mix = (a, b, t) => new THREE.Color(a).lerp(new THREE.Color(b), t);
  const matte = (col) => new THREE.MeshStandardMaterial({ color: col, roughness: 0.95, metalness: 0 });
  const solid = (m) => { m.castShadow = true; m.receiveShadow = true; return m; };

  function camera(aspect) {
    const cam = new THREE.PerspectiveCamera(30, aspect, 0.1, 100);
    cam.position.set(0, 4.2, 8.4);
    cam.lookAt(0, 0.55, 0);
    return cam;
  }

  function build(stage, moodName) {
    const m = MOODS[moodName];
    const h = m.h, sick = 1 - h, c = m.c;
    const scene = new THREE.Scene();
    scene.add(new THREE.HemisphereLight(0xffffff, 0x8a7a60, 0.75));
    const sun = new THREE.DirectionalLight(0xffffff, 0.95);
    sun.position.set(3, 6, 4);
    sun.castShadow = true;
    sun.shadow.mapSize.set(1024, 1024);
    Object.assign(sun.shadow.camera, { left: -4, right: 4, top: 4, bottom: -4 });
    scene.add(sun);

    const grass = mix('#bfae74', '#8ccf5a', h);
    const crown = mix('#a58a5a', '#62b34c', h);
    const leaf = mix('#a9a56a', '#6fc24e', h);
    const stemC = mix('#a9a56a', '#4f9f3f', h);
    const water = mix('#8f9a8c', '#5fb8e0', h);
    const dirt = matte('#9b6b47'), stone = matte('#c9c1b3'), trunkM = matte('#8a5a3b');
    const isl = new THREE.Group();
    scene.add(isl);
    const add = (mesh, x, y, z) => { mesh.position.set(x, y, z); isl.add(solid(mesh)); return mesh; };

    add(new THREE.Mesh(new THREE.CylinderGeometry(2.3, 1.6, 0.9, 10), dirt), 0, -0.45, 0);
    const under = add(new THREE.Mesh(new THREE.ConeGeometry(1.6, 1.2, 10), dirt), 0, -1.5, 0);
    under.rotation.x = Math.PI;
    add(new THREE.Mesh(new THREE.CylinderGeometry(2.34, 2.34, 0.16, 10), matte(grass)), 0, 0.04, 0);

    /* maceta con cara: el protagonista, un poco más grande que el resto */
    const pet = new THREE.Group();
    pet.scale.setScalar(1.4);
    isl.add(pet);
    const potM = matte('#e07352');
    const addPet = (mesh, x, y, z) => { mesh.position.set(x, y, z); pet.add(solid(mesh)); return mesh; };
    addPet(new THREE.Mesh(new THREE.CylinderGeometry(0.46, 0.34, 0.6, 24), potM), 0, 0.42, 0);
    const rim = addPet(new THREE.Mesh(new THREE.TorusGeometry(0.46, 0.06, 10, 32), potM), 0, 0.72, 0);
    rim.rotation.x = Math.PI / 2;
    const eyeM = new THREE.MeshStandardMaterial({ color: 0x1d1a17, roughness: 0.3 });
    for (const s of [-1, 1]) {
      const e = new THREE.Mesh(new THREE.SphereGeometry(0.055, 16, 12), eyeM);
      e.scale.y = Math.max(0.08, m.eye);
      e.position.set(s * 0.15, 0.48, 0.41);
      pet.add(e);
    }

    /* boca: cambia con el ánimo */
    const mouthM = new THREE.MeshStandardMaterial({ color: 0x1d1a17, roughness: 0.4 });
    const mouthAt = (mesh) => { mesh.position.set(0, 0.35, 0.4); mesh.rotation.x = -0.2; pet.add(mesh); };
    if (moodName === 'celebrating') {
      const open = new THREE.Mesh(new THREE.CircleGeometry(0.085, 20, Math.PI, Math.PI), mouthM);
      mouthAt(open);
      const tongue = new THREE.Mesh(new THREE.CircleGeometry(0.04, 16, Math.PI, Math.PI), matte('#ff8fa0'));
      tongue.position.set(0, 0.315, 0.402);
      tongue.rotation.x = -0.2;
      pet.add(tongue);
    } else if (moodName === 'tired') {
      mouthAt(new THREE.Mesh(new THREE.BoxGeometry(0.11, 0.026, 0.01), mouthM));
    } else {
      const smile = moodName !== 'sick';
      const r = moodName === 'recovering' ? 0.05 : 0.07;
      const arc = new THREE.Mesh(new THREE.TorusGeometry(r, 0.02, 8, 24, Math.PI), mouthM);
      if (smile) arc.rotation.z = Math.PI;
      mouthAt(arc);
      arc.rotation.x = -0.2;
      if (smile) arc.rotation.z = Math.PI;
      arc.position.y = smile ? 0.36 + r * 0.6 : 0.3;
    }

    /* brote */
    const plant = new THREE.Group();
    plant.position.y = 0.72;
    plant.rotation.z = sick * 0.6;
    plant.scale.y = 0.55 + 0.45 * h;
    pet.add(plant);
    const stem = solid(new THREE.Mesh(new THREE.CylinderGeometry(0.05, 0.07, 0.7, 12), matte(stemC)));
    stem.position.y = 0.35;
    plant.add(stem);
    for (const side of [-1, 1]) {
      const piv = new THREE.Group();
      piv.position.y = 0.62;
      piv.rotation.z = side * (h * 1.3 - 1);
      const l = solid(new THREE.Mesh(new THREE.SphereGeometry(0.3, 24, 16), matte(leaf)));
      l.scale.set(1, 0.3, 0.6);
      l.position.x = side * 0.28;
      piv.add(l);
      plant.add(piv);
    }
    if (c > 0) {
      const bloom = new THREE.Mesh(new THREE.SphereGeometry(0.13, 16, 12), matte('#ff8fb6'));
      bloom.position.y = 0.75;
      plant.add(bloom);
    }

    [[0.8, 0.3, 0.12], [-0.7, -1.3, 0.09], [1.9, -1.0, 0.1], [-1.8, 0.4, 0.08]].forEach(([x, z, r]) => {
      const s = add(new THREE.Mesh(new THREE.SphereGeometry(r, 12, 8), stone), x, 0.13, z);
      s.scale.y = 0.6;
    });

    const tree = (x, z, s) => {
      const t = new THREE.Group();
      t.position.set(x, 0.1, z);
      t.scale.setScalar(s);
      const tr = solid(new THREE.Mesh(new THREE.CylinderGeometry(0.06, 0.08, 0.7, 10), trunkM));
      tr.position.y = 0.35;
      t.add(tr);
      const cr = solid(new THREE.Mesh(new THREE.SphereGeometry(0.38, 24, 16), matte(crown)));
      cr.position.y = 0.85;
      cr.scale.setScalar(0.7 + 0.3 * h);
      t.add(cr);
      isl.add(t);
    };

    if (stage >= 1) tree(-1.7, 0.05, 1);
    if (stage >= 2) {
      for (let i = 0; i < 5; i++) {
        const k = i / 4;
        add(new THREE.Mesh(new THREE.CylinderGeometry(0.15, 0.16, 0.05, 10), stone), 0.5 + 0.85 * k, 0.13, 0.62 + 1.15 * k);
      }
    }
    if (stage >= 3) {
      tree(1.6, -0.3, 0.85);
      const tuft = matte(mix('#b0a060', '#5aa83e', h));
      [[0.9, -0.9], [-0.2, 1.5], [1.95, 0.7], [-1.95, -0.3], [-0.9, -0.6]].forEach(([x, z]) => {
        for (const d of [-0.06, 0, 0.06]) add(new THREE.Mesh(new THREE.ConeGeometry(0.04, 0.28 * (0.6 + 0.4 * h), 5), tuft), x + d, 0.26, z + d * 0.5);
      });
    }
    if (stage >= 4) {
      const house = new THREE.Group();
      house.position.set(-1.0, 0.12, -1.35);
      house.rotation.y = 0.55;
      const walls = solid(new THREE.Mesh(new THREE.BoxGeometry(0.62, 0.48, 0.5), matte(mix('#bdb2a0', '#f3e3c8', h))));
      walls.position.y = 0.24;
      house.add(walls);
      const roof = solid(new THREE.Mesh(new THREE.ConeGeometry(0.52, 0.38, 4), matte(mix('#9c6a58', '#d65f3e', h))));
      roof.position.y = 0.67;
      roof.rotation.y = Math.PI / 4;
      house.add(roof);
      const door = new THREE.Mesh(new THREE.BoxGeometry(0.14, 0.24, 0.02), matte('#6b4630'));
      door.position.set(0, 0.12, 0.26);
      house.add(door);
      const win = new THREE.Mesh(new THREE.BoxGeometry(0.12, 0.1, 0.02), matte(h > 0.5 ? '#ffe08a' : '#7d7a70'));
      win.position.set(0.19, 0.3, 0.26);
      house.add(win);
      isl.add(house);
    }
    if (stage >= 5) {
      const pond = add(new THREE.Mesh(new THREE.CylinderGeometry(0.62, 0.62, 0.04, 28), matte(water)), -1.15, 0.13, 0.95);
      pond.scale.z = 0.7;
      for (let i = 0; i < 9; i++) {
        const a = (i / 9) * Math.PI * 2;
        add(new THREE.Mesh(new THREE.SphereGeometry(0.07, 10, 8), stone), -1.15 + Math.cos(a) * 0.66, 0.14, 0.95 + Math.sin(a) * 0.47);
      }
    }
    if (stage >= 6 || c > 0) {
      const spots = [[0.9, 1.4], [-0.4, 1.7], [1.8, 0.3], [-1.7, -0.2], [0.4, -1.6], [-0.1, -0.9], [1.4, -1.2], [0.2, 2.0], [-1.95, 0.15], [1.1, 0.4]];
      spots.forEach(([x, z], i) => {
        const f = new THREE.Group();
        f.position.set(x, 0.12, z);
        const st = new THREE.Mesh(new THREE.CylinderGeometry(0.015, 0.015, 0.22, 6), matte(stemC));
        st.position.y = 0.11;
        f.add(st);
        const hd = new THREE.Mesh(new THREE.SphereGeometry(0.07, 12, 8), matte(i % 2 ? '#ff8fb6' : '#ffd23f'));
        hd.position.y = 0.24;
        hd.scale.setScalar(0.5 + 0.5 * h);
        f.add(hd);
        isl.add(f);
      });
    }
    if (stage >= 6) {
      ['#e8574b', '#f39a3c', '#f6d14b', '#6cc36a', '#4fa3e0', '#8a6fd1'].forEach((col, i) => {
        const arc = new THREE.Mesh(new THREE.TorusGeometry(2.15 - i * 0.1, 0.05, 8, 64, Math.PI), matte(mix('#9a9a9a', col, h)));
        arc.position.set(0, 0.15, -2.0);
        scene.add(arc);
      });
    }
    if (sick > 0.5) {
      const sm = new THREE.MeshStandardMaterial({ color: 0x6f6f6f, roughness: 1, transparent: true, opacity: 0.75 });
      [[0.1, 1.9, 0.1, 0.22], [-0.35, 2.3, -0.1, 0.28], [0.3, 2.65, 0, 0.32], [-0.6, 1.8, 0.3, 0.18], [0.75, 2.2, -0.2, 0.2]].forEach(([x, y, z, r]) => {
        const p = new THREE.Mesh(new THREE.SphereGeometry(r, 16, 12), sm);
        p.position.set(x, y, z);
        scene.add(p);
      });
    }
    isl.rotation.y = 0.35;
    return scene;
  }

  function makeRenderer(canvas, size) {
    const r = new THREE.WebGLRenderer({ canvas, antialias: true, alpha: true, preserveDrawingBuffer: true });
    r.setPixelRatio(1);
    r.setSize(size, size, false);
    r.setClearColor(0x000000, 0);
    r.shadowMap.enabled = true;
    r.shadowMap.type = THREE.PCFSoftShadowMap;
    r.outputEncoding = THREE.sRGBEncoding;
    return r;
  }

  function draw(renderer, stage, mood) {
    renderer.render(build(stage, mood), camera(1));
  }

  return { MOODS, STAGES, makeRenderer, draw };
})();
