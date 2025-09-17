#!/usr/bin/env python3
import os
import sys
import argparse
from pathlib import Path
from PIL import Image, UnidentifiedImageError

def remap_alpha(root: Path, pattern: str, alpha_val: int):
    if not root.exists() or not root.is_dir():
        print(f"ERROR: “{root}” is not a valid directory.")
        sys.exit(1)

    print(f"Processing folder: {root.resolve()}")
    print(f"  → Looking for files containing “{pattern}” and remapping non-zero alpha → {alpha_val}\n")

    exts = {'.png', '.tga', '.tiff', '.webp', '.jpg', '.jpeg'}
    # lookup table: 0→0, 1–255→alpha_val
    lut = [0] + [alpha_val] * 255

    for dirpath, _, filenames in os.walk(root):
        for fname in filenames:
            # case-insensitive pattern match
            if pattern.lower() not in fname.lower():
                continue

            path = Path(dirpath) / fname
            if path.suffix.lower() not in exts:
                continue

            rel = path.relative_to(root)
            try:
                im = Image.open(path).convert('RGBA')
            except UnidentifiedImageError:
                print(f"{rel}: unsupported format, skipping.")
                continue
            except Exception as e:
                print(f"{rel}: ERROR opening – {e}")
                continue

            r, g, b, a = im.split()
            min_a, max_a = a.getextrema()
            if max_a == 0:
                print(f"{rel}: fully transparent → skipped")
                continue

            new_a = a.point(lut)
            if new_a.tobytes() == a.tobytes():
                print(f"{rel}: already uniform α={alpha_val}/transparent")
                continue

            out = Image.merge('RGBA', (r, g, b, new_a))
            try:
                out.save(path)
                print(f"{rel}: remapped non-zero α → {alpha_val}")
            except Exception as e:
                print(f"{rel}: ERROR saving – {e}")

if __name__ == "__main__":
    # ensure Pillow
    try:
        import PIL
    except ImportError:
        print("ERROR: Pillow not installed. Run `pip install pillow` and try again.")
        sys.exit(1)

    parser = argparse.ArgumentParser(
        description="Batch-remap non-zero alpha in images matching a filename pattern."
    )
    parser.add_argument(
        '--pattern', '-p', required=True,
        help="Substring to match in filenames (case-insensitive)."
    )
    parser.add_argument(
        '--alpha', '-a', type=int, required=True, metavar='VAL',
        help="Target alpha for non-transparent pixels (1–255)."
    )
    parser.add_argument(
        'folder', nargs='?',
        help="Folder to process (defaults to the script's directory)."
    )

    args = parser.parse_args()

    if not (1 <= args.alpha <= 255):
        print("ERROR: --alpha must be between 1 and 255.")
        sys.exit(1)

    target_dir = Path(args.folder) if args.folder else Path(__file__).parent
    remap_alpha(target_dir, args.pattern, args.alpha)
