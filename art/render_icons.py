"""Renders the rule icons: art/rules/<name>.svg -> graphics/rulebook/rules/<name>.png (128x128, 8-bit RGBA).

Needs resvg-py and Pillow:  python -m pip install resvg-py pillow
Usage:  python art/render_icons.py            (every SVG)
        python art/render_icons.py fox seer   (only these)
"""
import io
import os
import sys

import resvg_py
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "art", "rules")
DST = os.path.join(ROOT, "graphics", "rulebook", "rules")
SIZE = 128


def render(name):
    png = resvg_py.svg_to_bytes(svg_path=os.path.join(SRC, name + ".svg"), width=SIZE, height=SIZE)
    img = Image.open(io.BytesIO(bytes(png))).convert("RGBA")
    if img.size != (SIZE, SIZE):
        raise ValueError(f"{name}: rendered at {img.size}")
    # A plain 8-bit RGBA PNG: no colour profile, gamma or dpi chunks.
    img.save(os.path.join(DST, name + ".png"), format="PNG", optimize=True)


def main():
    names = sys.argv[1:] or sorted(f[:-4] for f in os.listdir(SRC) if f.endswith(".svg"))
    for name in names:
        render(name)
    print(f"rendered {len(names)} icons into {DST}")


if __name__ == "__main__":
    main()
