"""Genera las 35 imágenes de la isla (7 etapas x 5 estados) para la app.

Uso, desde la raíz del repo:  python -I tools/island/render.py
Necesita Playwright de Python y Chrome (o Chromium) instalado.
"""
import os
import pathlib
import sys

from playwright.sync_api import sync_playwright

HERE = pathlib.Path(__file__).resolve().parent
OUT = HERE.parents[1] / "app" / "src" / "main" / "res" / "drawable-nodpi"
MOODS = ["happy", "tired", "sick", "recovering", "celebrating"]
STAGES = range(7)
CHROME = os.environ.get("CHROME", "/usr/bin/google-chrome")


def main() -> int:
    OUT.mkdir(parents=True, exist_ok=True)
    page_url = (HERE / "index.html").as_uri()
    with sync_playwright() as p:
        browser = p.chromium.launch(
            executable_path=CHROME if os.path.exists(CHROME) else None,
            args=["--use-gl=angle", "--use-angle=swiftshader", "--enable-unsafe-swiftshader",
                  "--allow-file-access-from-files"],
        )
        page = browser.new_page(viewport={"width": 512, "height": 512})
        for stage in STAGES:
            for mood in MOODS:
                page.goto(f"{page_url}?stage={stage}&mood={mood}")
                page.wait_for_selector("body[data-ready='1']", timeout=20000)
                target = OUT / f"island_{stage}_{mood}.png"
                page.locator("#gl").screenshot(path=str(target), omit_background=True)
                print(target.name)
        browser.close()
    count = len(list(OUT.glob("island_*_*.png")))
    print(f"{count} imágenes en {OUT}")
    return 0 if count == len(STAGES) * len(MOODS) else 1


if __name__ == "__main__":
    sys.exit(main())
