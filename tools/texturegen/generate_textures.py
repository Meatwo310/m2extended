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
LEAD_HUE = 0.82
SILICON_HUE = 0.565


def shift_rgba(
    image: Image.Image,
    *,
    hue: float = LEAD_HUE,
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


def shift_silicon(image: Image.Image) -> Image.Image:
    return shift_rgba(image, hue=SILICON_HUE, sat_add=0.25, val_mul=1.6)


def draw_router_top(image: Image.Image, blocked: str | None = None) -> Image.Image:
    scale = 4
    big = image.resize((image.width * scale, image.height * scale), Image.Resampling.NEAREST)
    draw = ImageDraw.Draw(big, "RGBA")
    cx = image.width * scale / 2
    cy = image.height * scale / 2

    if blocked is not None:
        positions = {
            "right": (cx + 13 * scale, cy),
            "up": (cx, cy - 13 * scale),
            "down": (cx, cy + 13 * scale),
        }
        x, y = positions[blocked]

        for color, width in ((SHADOW, 3 * scale), (WHITE, 2 * scale)):
            draw_x(draw, x, y, 3.2 * scale, color, width)

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


def composite_router(name: str, blocked: str | None = None, *, hue: float = LEAD_HUE) -> None:
    image = Image.open(VANILLA_DUCTS / "duct-router.png")
    base = shift_silicon(image) if hue == SILICON_HUE else shift_rgba(image, hue=hue)
    top = tint_arrow(Image.open(VANILLA_DUCTS / "duct-router-top.png"))
    base.save(OUT / name)
    draw_router_top(top, blocked).save(OUT / name.replace(".png", "-top.png"))


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


def draw_x(
    draw: ImageDraw.ImageDraw,
    cx: float,
    cy: float,
    size: float,
    color: tuple[int, int, int, int],
    width: int,
) -> None:
    draw.line([(cx - size, cy - size), (cx + size, cy + size)], fill=color, width=width)
    draw.line([(cx - size, cy + size), (cx + size, cy - size)], fill=color, width=width)


def composite_junction(name: str = "directed-junction.png", *, hue: float = LEAD_HUE) -> None:
    source = Image.open(VANILLA_DISTRIBUTION / "junction.png")
    image = shift_silicon(source) if hue == SILICON_HUE else shift_rgba(source, hue=hue)
    image.save(OUT / name)
    top = Image.new("RGBA", image.size, (0, 0, 0, 0))
    scale = 4
    big = top.resize((top.width * scale, top.height * scale), Image.Resampling.NEAREST)
    draw = ImageDraw.Draw(big, "RGBA")
    cx = top.width * scale / 2
    cy = top.height * scale / 2

    for color, width in ((SHADOW, 3 * scale), (WHITE, 2 * scale)):
        draw_chevron(draw, cx + 11 * scale, cy, "right", 3.6 * scale, color, width)
        draw_chevron(draw, cx, cy + 11 * scale, "down", 3.6 * scale, color, width)

    big.resize(top.size, Image.Resampling.LANCZOS).save(OUT / name.replace(".png", "-top.png"))


def composite_instant_blocks() -> None:
    shift_silicon(Image.open(VANILLA_DISTRIBUTION / "router.png")).save(OUT / "silicon-router.png")
    shift_silicon(Image.open(VANILLA_DISTRIBUTION / "junction.png")).save(OUT / "silicon-junction.png")


def main() -> None:
    if not VANILLA_DUCTS.is_dir():
        raise SystemExit(f"Mindustry assets not found: {VANILLA_DUCTS}")

    OUT.mkdir(parents=True, exist_ok=True)
    composite_router("lead-directed-router.png")
    composite_router("lead-right-directed-router.png", "up")
    composite_router("lead-left-directed-router.png", "down")
    composite_router("lead-t-directed-router.png", "right")
    composite_junction("lead-directed-junction.png")
    composite_router("silicon-directed-router.png", hue=SILICON_HUE)
    composite_router("silicon-right-directed-router.png", "up", hue=SILICON_HUE)
    composite_router("silicon-left-directed-router.png", "down", hue=SILICON_HUE)
    composite_router("silicon-t-directed-router.png", "right", hue=SILICON_HUE)
    composite_junction("silicon-directed-junction.png", hue=SILICON_HUE)
    composite_instant_blocks()

    for name in (
        "lead-directed-router.png",
        "lead-right-directed-router.png",
        "lead-left-directed-router.png",
        "lead-t-directed-router.png",
        "lead-directed-junction.png",
        "lead-directed-router-top.png",
        "lead-right-directed-router-top.png",
        "lead-left-directed-router-top.png",
        "lead-t-directed-router-top.png",
        "lead-directed-junction-top.png",
        "silicon-router.png",
        "silicon-junction.png",
        "silicon-directed-router.png",
        "silicon-right-directed-router.png",
        "silicon-left-directed-router.png",
        "silicon-t-directed-router.png",
        "silicon-directed-junction.png",
        "silicon-directed-router-top.png",
        "silicon-right-directed-router-top.png",
        "silicon-left-directed-router-top.png",
        "silicon-t-directed-router-top.png",
        "silicon-directed-junction-top.png",
    ):
        print(OUT / name)


if __name__ == "__main__":
    main()
