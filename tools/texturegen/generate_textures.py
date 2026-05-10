from __future__ import annotations

from pathlib import Path
import colorsys

from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[2]
MINDUSTRY = ROOT.parent / "Mindustry"
VANILLA_DISTRIBUTION = MINDUSTRY / "core" / "assets-raw" / "sprites" / "blocks" / "distribution"
VANILLA_DUCTS = VANILLA_DISTRIBUTION / "ducts"
OUT = ROOT / "sprites" / "blocks"

SHADOW = (30, 33, 40, 220)
WHITE = (240, 246, 248, 255)


def shift_rgba(
    image: Image.Image,
    *,
    hue: float = 0.82,
    sat_mul: float = 0.72,
    sat_add: float = 0.18,
    val_mul: float = 1.08,
) -> Image.Image:
    result = image.convert("RGBA")
    pixels = result.load()

    for y in range(result.height):
        for x in range(result.width):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue

            _, s, v = colorsys.rgb_to_hsv(r / 255, g / 255, b / 255)
            s = min(1.0, s * sat_mul + sat_add)
            v = min(1.0, v * val_mul)
            rr, gg, bb = colorsys.hsv_to_rgb(hue, s, v)
            pixels[x, y] = (round(rr * 255), round(gg * 255), round(bb * 255), a)

    return result


def draw_marker(image: Image.Image, directions: tuple[str, ...]) -> Image.Image:
    scale = 4
    big = image.resize((image.width * scale, image.height * scale), Image.Resampling.NEAREST)
    draw = ImageDraw.Draw(big, "RGBA")
    cx = image.width * scale / 2
    cy = image.height * scale / 2

    for color, width in ((SHADOW, 3 * scale), (WHITE, 2 * scale)):
        if "up" in directions:
            draw_chevron(draw, cx, cy - 13 * scale, "up", 3.2 * scale, color, width)
        if "down" in directions:
            draw_chevron(draw, cx, cy + 13 * scale, "down", 3.2 * scale, color, width)

    return big.resize(image.size, Image.Resampling.LANCZOS)


def tint_arrow(image: Image.Image) -> Image.Image:
    result = image.convert("RGBA")
    pixels = result.load()

    for y in range(result.height):
        for x in range(result.width):
            r, g, b, a = pixels[x, y]
            if a == 0:
                continue
            v = min(255, round(max(r, g, b) * 1.04))
            pixels[x, y] = (v, min(255, v + 5), min(255, v + 9), a)

    return result


def composite_router(name: str, directions: tuple[str, ...]) -> None:
    base = shift_rgba(Image.open(VANILLA_DUCTS / "duct-router.png"))
    top = tint_arrow(Image.open(VANILLA_DUCTS / "duct-router-top.png"))
    image = draw_marker(Image.alpha_composite(base, top), directions)
    image.save(OUT / name)


def draw_chevron(
    draw: ImageDraw.ImageDraw,
    cx: float,
    cy: float,
    direction: str,
    size: float,
    color: tuple[int, int, int, int],
    width: int,
) -> None:
    if direction == "right":
        points = [(cx - size, cy - size), (cx, cy), (cx - size, cy + size)]
    elif direction == "up":
        points = [(cx - size, cy + size), (cx, cy), (cx + size, cy + size)]
    elif direction == "down":
        points = [(cx - size, cy - size), (cx, cy), (cx + size, cy - size)]
    else:
        raise ValueError(f"unknown chevron direction: {direction}")

    draw.line(points, fill=color, width=width, joint="curve")


def composite_junction() -> None:
    image = shift_rgba(Image.open(VANILLA_DISTRIBUTION / "junction.png"))
    scale = 4
    big = image.resize((image.width * scale, image.height * scale), Image.Resampling.NEAREST)
    draw = ImageDraw.Draw(big, "RGBA")
    cx = image.width * scale / 2
    cy = image.height * scale / 2

    for color, width in ((SHADOW, 3 * scale), (WHITE, 2 * scale)):
        draw_chevron(draw, cx + 11 * scale, cy, "right", 3.6 * scale, color, width)
        draw_chevron(draw, cx, cy + 11 * scale, "down", 3.6 * scale, color, width)

    big.resize(image.size, Image.Resampling.LANCZOS).save(OUT / "directed-junction.png")


def main() -> None:
    if not VANILLA_DUCTS.is_dir():
        raise SystemExit(f"Mindustry assets not found: {VANILLA_DUCTS}")

    OUT.mkdir(parents=True, exist_ok=True)
    composite_router("directed-router.png", ("up", "down"))
    composite_router("right-directed-router.png", ("down",))
    composite_router("left-directed-router.png", ("up",))
    composite_junction()

    for name in (
        "directed-router.png",
        "right-directed-router.png",
        "left-directed-router.png",
        "directed-junction.png",
    ):
        print(OUT / name)


if __name__ == "__main__":
    main()
