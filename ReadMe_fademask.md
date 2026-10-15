# Fade mask

The row behind the search bar and add button uses the same frosted wallpaper as the board. A vertical mask fades that frost so widgets can show through the top of the row and disappear into solid frost lower down.

## Where it is wired

- `app/src/main/java/gd/app/hiboard/ui/FrostFadeDrawable.kt` draws the frost and the mask.
- `HiboardView` sets that drawable as the background of `boardHeader`.
- `app/src/main/res/layout/view_hiboard.xml` places `boardHeader` at the bottom of the screen, over the scrolling widgets.

The scroll view is padded by the header height, so cards can pass under the row and the last card can still be scrolled clear of it.

## Change the fade range

Edit the gradient in `FrostFadeDrawable.draw`:

```kotlin
intArrayOf(Color.TRANSPARENT, 0xEEFFFFFF.toInt(), Color.WHITE, Color.WHITE),
floatArrayOf(0f, 0.28f, 0.36f, 1f),
```

`floatArrayOf` is the position down the bar. `0` is the top edge. `1` is the bottom edge. Each number lines up with the color in the same slot.

| Stop | Current value | What it does |
| --- | --- | --- |
| 0.00 | `Color.TRANSPARENT` | Top of the bar is clear. Widgets show through. |
| 0.28 | `0xEEFFFFFF` | About 93% of the frost is visible. |
| 0.36 | `Color.WHITE` | Frost is solid from here down. |
| 1.00 | `Color.WHITE` | Bottom edge stays solid. |

Move `0.36` down (for example `0.6`) to keep the fade going farther. Move it up (for example `0.2`) to make the bar turn solid sooner.

The color’s first two hex digits are the opacity of the frost, not a white tint. `00` is clear, `FF` is solid. `EE` is only slightly clear.

## Change how tall the fade is

`paddingTop` on `boardHeader` in `view_hiboard.xml` adds empty space above the search bar. That space is part of the masked bar, so a larger `paddingTop` makes a taller fade. It is `16dp` now.

The mask always runs the full height of `boardHeader`, including the navigation-bar padding added in `HiboardView`.
