# Visual Design Direction — AfrikaBurn Companion

Derived from research into the Tankwa Karoo landscape, AfrikaBurn event photography, and the emotional positioning established in `BRAND_POSITIONING.md`.

---

## Visual Research Summary

### The Tankwa Karoo Landscape

The Tankwa Karoo — "land of thirst" in Khoisan — is one of the driest regions in South Africa. The landscape is:

- **Dominant colours:** Sandy beige, ochre, dusty brown. The earth is pale to mid-brown with rust-red undertones. After rare rains, ephemeral wildflowers introduce brief accents of yellow, purple, and red.
- **Textures:** Sparse, rocky plains with low-lying succulents. The surface is cracked, dusty, and austere. Everything has a fine layer of pale dust on it.
- **Light quality:** Unfiltered and intense during the day — bleached whites, high contrast. At dusk, the sky transitions through golden amber to deep indigo. At night, near-zero light pollution reveals one of Africa's darkest skies.
- **Temperature of light:** Warm. Even the shadows lean warm. Midday is harsh and bleached, but golden hour and firelight dominate the event experience.

### AfrikaBurn Visual Character

- **Day:** Dust, pale earth, massive wooden/metal sculptures casting sharp shadows. Colour comes from art, costumes, and camp structures against the monochrome desert.
- **Night:** Fire is the dominant light source. Amber, orange, and red glow against ink-black sky. Art installations are lit (mandatory for safety). LED and fire create warm pools of light in vast darkness.
- **Materials:** Wood (destined to burn), metal, fabric, rope. Raw, impermanent, handmade. Nothing polished or manufactured-looking.
- **Overall aesthetic:** Raw warmth. Not rustic-cute. Not tech-clean. Somewhere between a geology textbook and a campfire.

### Patterns Across Research

| Pattern | What It Means For The App |
|---|---|
| Ochre/brown earth dominates | Primary palette lives in the amber-to-brown range |
| Night usage is primary | Dark mode is the hero. Warm amber tones glow against dark surfaces. |
| Fire as light source | Accent colour is fire-amber, not electric blue or neon |
| Bleached midday light | Light mode uses warm off-whites, not pure white |
| Sparse vegetation (succulents) | Tertiary accent in muted sage green — for status, maps, nature elements |
| Handmade materials | Typography should feel humanist, not geometric or corporate |
| Dust on everything | Surfaces should feel warm and slightly textured, not clinical |
| Extreme contrast (midday sun vs night) | Both modes must handle extreme readability conditions |

---

## Colour Palette

### Problem With Current Palette

The current palette is **default Material Design 3 purple** (`#6750A4`). This is:
- Generic — indistinguishable from a thousand other MD3 apps
- Disconnected — purple has no relationship to the Tankwa, dust, fire, or earth
- Cool-toned — conflicts with the warm, desert-rooted positioning
- Forgettable — a participant would never associate this colour with AfrikaBurn

### Proposed Palette

Every colour below is traceable to the Tankwa Karoo landscape.

#### Light Mode

| Role | Hex | Name | Source |
|---|---|---|---|
| **Primary** | `#8B5E34` | Tankwa Earth | The ochre-brown of the playa dust |
| On Primary | `#FFFFFF` | White | |
| Primary Container | `#FFDDB5` | Dust Glow | Sunlight through dust particles |
| On Primary Container | `#2E1500` | Deep Earth | |
| **Secondary** | `#6F5B40` | Warm Brown | Dried river bed clay |
| On Secondary | `#FFFFFF` | White | |
| Secondary Container | `#FBDEBC` | Sand Light | Pale desert sand |
| On Secondary Container | `#271904` | Deep Brown | |
| **Tertiary** | `#51643F` | Karoo Sage | Succulent Karoo vegetation |
| On Tertiary | `#FFFFFF` | White | |
| Tertiary Container | `#D4EABB` | Sage Light | New growth after rain |
| On Tertiary Container | `#0F2004` | Deep Sage | |
| **Background** | `#FFF8F2` | Bone White | Sun-bleached bone on the playa |
| On Background | `#201A17` | Charcoal | |
| **Surface** | `#FFF8F2` | Bone White | |
| On Surface | `#201A17` | Charcoal | |
| Surface Variant | `#F3DFD0` | Dusty Surface | |
| On Surface Variant | `#52443B` | Warm Grey | |
| **Outline** | `#857469` | Dust Outline | |
| Outline Variant | `#D8C3B6` | Light Dust | |
| **Inverse Surface** | `#362F2B` | Night Ground | |
| Inverse On Surface | `#FBEEE6` | | |
| **Surface Containers** | | | |
| Surface Container Lowest | `#FFFFFF` | Pure White | |
| Surface Container Low | `#FEF1E9` | Warm White | |
| Surface Container | `#F8ECE3` | Warm Surface | |
| Surface Container High | `#F2E6DD` | Warm Surface High | |
| Surface Container Highest | `#ECE0D7` | Warm Surface Highest | |

#### Dark Mode (Primary Experience)

| Role | Hex | Name | Source |
|---|---|---|---|
| **Primary** | `#FFBA6E` | Fire Amber | Firelight on the playa at night |
| On Primary | `#4A2800` | Deep Amber | |
| Primary Container | `#6A4320` | Ember | Glowing coals |
| On Primary Container | `#FFDDB5` | Dust Glow | |
| **Secondary** | `#DFC2A2` | Warm Sand | Moonlit sand |
| On Secondary | `#3F2D17` | Deep Sand | |
| Secondary Container | `#57432B` | Night Sand | |
| On Secondary Container | `#FBDEBC` | Sand Light | |
| **Tertiary** | `#B8CEA0` | Soft Sage | Succulents in twilight |
| On Tertiary | `#243514` | Deep Sage | |
| Tertiary Container | `#3A4C29` | Night Sage | |
| On Tertiary Container | `#D4EABB` | Sage Light | |
| **Background** | `#1A1511` | Tankwa Night | The desert floor after dark |
| On Background | `#F0DFD2` | Warm Light | |
| **Surface** | `#1A1511` | Tankwa Night | |
| On Surface | `#F0DFD2` | Warm Light | |
| Surface Variant | `#52443B` | Warm Dark | |
| On Surface Variant | `#D8C3B6` | Dust Light | |
| **Outline** | `#A08D82` | Dust Outline | |
| Outline Variant | `#52443B` | Warm Dark | |
| **Inverse Surface** | `#F0DFD2` | Warm Light | |
| Inverse On Surface | `#362F2B` | Night Ground | |
| **Surface Containers** | | | |
| Surface Container Lowest | `#140F0B` | Deep Night | |
| Surface Container Low | `#201A17` | Night Low | |
| Surface Container | `#251F1B` | Night Medium | |
| Surface Container High | `#302925` | Night High | |
| Surface Container Highest | `#3B3430` | Night Highest | |

#### Before vs After Comparison

| Element | Before (Default MD3) | After (Tankwa) | Why |
|---|---|---|---|
| Primary | `#6750A4` (purple) | `#8B5E34` / `#FFBA6E` | Purple is arbitrary. Ochre is the literal ground under your feet. |
| Background Light | `#FFFBFE` (cool white) | `#FFF8F2` (warm bone) | Cool white feels clinical. Bone white feels like the desert. |
| Background Dark | `#1C1B1F` (cool dark) | `#1A1511` (warm charcoal) | Cool dark feels like a settings screen. Warm charcoal feels like the Tankwa at night. |
| Accent | `#625B71` (grey-purple) | `#51643F` (sage) | Grey-purple has no meaning. Sage references the actual Karoo vegetation. |
| Overall feeling | Generic Android app | Desert survival tool | Every colour tells you where you are. |

### Semantic Colours (Both Modes)

| Role | Light Mode | Dark Mode | When to Use |
|---|---|---|---|
| Success | `#2E7D32` | `#81C784` | Water found, camp saved, sync complete |
| Warning | `#E65100` | `#FFB74D` | Dust storm, low battery, schedule conflict |
| Error | `#C62828` | `#EF9A9A` | Emergency, failed action, critical alert |
| Info | `#1565C0` | `#64B5F6` | Neutral information, tips |

**Note:** Warning orange (`#E65100`) is close to the primary amber range. In warning contexts, always pair with a warning icon (triangle/exclamation) to avoid confusion with brand colour. This is the only potential conflict — and it's resolved by the icon+text pairing rule from the accessibility guidelines.

---

## Typography

### Recommended Display Font: Bitter

**Font:** [Bitter](https://fonts.google.com/specimen/Bitter) by Sol Matas (Huerta Tipografica)
**License:** Open Font License (free for all use)
**Availability:** Google Fonts, variable weight (100-900)
**Platform support:** Android, iOS, Web

**Why Bitter:**
- **Slab serif with warmth.** The wedge-shaped serifs feel grounded and substantial — like something stamped into wood or etched into stone. This connects to AfrikaBurn's handmade, raw-material aesthetic.
- **Designed for screens.** Created specifically for comfortable screen reading, with generous x-height and open counters. Legible at small sizes (camp names on map markers) and striking at large sizes (screen headers).
- **Not corporate.** Slab serifs carry a different connotation than geometric sans-serifs or traditional serifs. Bitter feels like a bulletin board at a desert outpost, not a tech company landing page.
- **Variable weight.** Full range from Thin to Black, so we can use one font file and adjust weight per style.
- **Cross-platform.** Works identically on Android and iOS via Google Fonts bundling.

**What Bitter is not:** It's not decorative, western-themed, or novelty. It's a readable, warm, grounded text face with just enough character to feel intentional.

### Revised Type Scale

| Style | Font | Size / Weight | Usage |
|---|---|---|---|
| Display Large | Bitter | 57sp / Regular (400) | Hero text, splash screen |
| Display Medium | Bitter | 45sp / Regular (400) | Large promotional text |
| Display Small | Bitter | 36sp / Regular (400) | Screen headers (rare) |
| Headline Large | Bitter | 32sp / Regular (400) | Primary section headers |
| Headline Medium | Bitter | 28sp / Regular (400) | Card titles, camp names |
| Headline Small | Bitter | 24sp / Regular (400) | Sub-section headers |
| Title Large | Bitter | 22sp / Medium (500) | Navigation, prominent labels |
| Title Medium | System | 16sp / Medium (500) | Card subtitles, secondary labels |
| Title Small | System | 14sp / Medium (500) | Tertiary labels |
| Body Large | System | 16sp / Regular (400) | Primary reading text |
| Body Medium | System | 14sp / Regular (400) | Secondary text |
| Body Small | System | 12sp / Regular (400) | Captions, timestamps |
| Label Large | System | 14sp / Medium (500) | Buttons, tabs |
| Label Medium | System | 12sp / Medium (500) | Small labels, tags |
| Label Small | System | 11sp / Medium (500) | Tiny metadata |

**Cut point:** Bitter for Display through Title Large. System font (Roboto/SF Pro) for Title Medium and below. This gives headlines character while keeping body text invisible and performant.

---

## Texture & Surface

### Warm Surfaces, Not Flat

The Tankwa Karoo is not a clean, flat environment. The app's surfaces should reflect this through **colour warmth**, not through literal texture overlays.

**Implementation approach:**
- **No noise/grain overlays.** These are expensive to render, hurt performance on low-end devices, and look gimmicky. The warmth comes from the colour palette itself.
- **Surface hierarchy through warm tones.** Light mode uses a progression from bone white (`#FFF8F2`) to dusty warm (`#ECE0D7`). Dark mode uses warm charcoal (`#1A1511`) to warm dark brown (`#3B3430`). This creates depth without shadows.
- **Warm shadows.** Where elevation shadows are used, tint them slightly warm (towards brown, not towards blue/grey). Material 3's default shadows are neutral — we override with warm tint.
- **Map styling.** The offline map tiles should use warm-tinted base colours rather than default Mapbox/OpenStreetMap blues and greys. Tan/ochre for land, warm grey for roads, sage for any vegetation indicators.

---

## Component Styling

### Cards

| Property | Value | Notes |
|---|---|---|
| Background | `surfaceContainerLow` | Subtle lift from background |
| Border | None | Clean look, separation via colour |
| Corner radius | 8dp (`cornerRadiusMedium`) | Rounded but not bubbly |
| Padding | 20dp horizontal, 24dp vertical | Generous tap targets |
| Elevation | 2dp (`elevationSmall`) in light mode, 0dp in dark mode | Dark mode uses colour for hierarchy |
| Press interaction | `pressableScale` (0.96f) | Existing micro-interaction pattern |

### Buttons

| Variant | Background | Text Colour | Usage |
|---|---|---|---|
| Primary (filled) | Primary (`#8B5E34` / `#FFBA6E`) | On Primary | Main CTAs |
| Secondary (tonal) | Secondary Container | On Secondary Container | Less prominent actions |
| Tertiary (text) | Transparent | Primary | Inline actions, links |
| Emergency | Error colour | On Error | Safety/emergency actions only |

### Status Badges

| Type | Container (Light) | Container (Dark) | Text | Icon |
|---|---|---|---|---|
| Open / Available | `#E8F5E9` | `#1B3A1B` | Success colour | Check circle |
| Warning / Alert | `#FFF3E0` | `#3E2700` | Warning colour | Warning triangle |
| Closed / Error | `#FFEBEE` | `#3B0A0A` | Error colour | Error circle |
| Info / Neutral | `#E3F2FD` | `#0A1929` | Info colour | Info circle |

### Navigation (Bottom Bar)

| Property | Light Mode | Dark Mode |
|---|---|---|
| Background | Surface Container (`#F8ECE3`) | Surface Container (`#251F1B`) |
| Selected icon | Primary (`#8B5E34`) | Primary (`#FFBA6E`) |
| Selected label | Primary | Primary |
| Unselected icon | On Surface Variant (`#52443B`) | On Surface Variant (`#D8C3B6`) |
| Unselected label | On Surface Variant | On Surface Variant |
| Indicator | Primary Container (`#FFDDB5`) | Primary Container (`#6A4320`) |

### Map Markers

| Type | Colour | Shape |
|---|---|---|
| Art installation | Primary (`#8B5E34` / `#FFBA6E`) | Circle with art icon |
| Theme camp | Secondary (`#6F5B40` / `#DFC2A2`) | Circle with camp icon |
| Your camp | Tertiary (`#51643F` / `#B8CEA0`) | Pin with home icon |
| Emergency/safety | Error colour | Circle with cross icon |
| Water/resource | Info colour | Circle with water icon |

---

## Implementation Priority

| Priority | Change | Visual Impact | Effort |
|---|---|---|---|
| **1 - Quick wins** | | | |
| 1a | Replace MD3 purple colour scheme with Tankwa palette in Theme.kt | Transformative | Low — one file |
| 1b | Update Background/Surface to warm tones in light + dark | Immediate warmth | Low — same file |
| 1c | Update splash screen colours in colors.xml (both day/night) | First impression | Low — two XML files |
| **2 - Medium effort** | | | |
| 2a | Add Bitter font, configure for Display-TitleLarge styles | Brand character | Medium — font asset + Theme.kt |
| 2b | Update map marker colours to match new palette | Map cohesion | Medium — marker styling |
| 2c | Warm-tint shadows/elevation in dark mode | Subtle polish | Medium — theme config |
| **3 - Larger refinements** | | | |
| 3a | Style offline map tiles with warm base colours | Full immersion | High — map tile config |
| 3b | Update app store screenshots with new palette | Public-facing | High — screenshot generation |
| 3c | Design and implement logo/wordmark | Brand identity | High — design + asset creation |

**First commit:** Items 1a + 1b + 1c can be done in a single Theme.kt + colors.xml commit. This one change will make the app feel completely different.

---

## Quality Checklist

- [x] Every colour traceable to the Tankwa Karoo landscape
- [x] Would a designer recognise this as intentionally chosen, not generated? Yes — the ochre/amber/sage palette is specific and defensible
- [x] No conflicts between brand colours and status colours (warning orange flagged and resolved with icon pairing)
- [x] Dark mode feels deliberate, not inverted — warm charcoal, fire amber primary, surface hierarchy
- [x] Font choice connected to mood and context, not arbitrary
- [x] Implementation path clear with effort levels

### "Not AI-Generated" Comparison

| Signal | Generic AI Output | This Direction |
|---|---|---|
| Colour source | "I chose warm tones because the app is about nature" | "Primary is #8B5E34 because that's the hex value of Tankwa dust in photos" |
| Font choice | "A modern sans-serif for clean readability" | "Bitter's slab serifs feel like something stamped into wood at a theme camp" |
| Dark mode | "Inverted light mode colours" | "Dark mode is primary because participants use the app at night around fires" |
| Colour naming | "Color1, Color2, Accent" | "Tankwa Earth, Fire Amber, Karoo Sage" — each name tells you where it's from |
| Anti-choices | None stated | "No fire/flame imagery, no cool-toned greys, no gradients, no pure black" |

---

## Appendix: Colour Values for Implementation

### Theme.kt Replacement Values

```kotlin
// Light Mode
val LightPrimary = Color(0xFF8B5E34)
val LightOnPrimary = Color(0xFFFFFFFF)
val LightPrimaryContainer = Color(0xFFFFDDB5)
val LightOnPrimaryContainer = Color(0xFF2E1500)
val LightSecondary = Color(0xFF6F5B40)
val LightOnSecondary = Color(0xFFFFFFFF)
val LightSecondaryContainer = Color(0xFFFBDEBC)
val LightOnSecondaryContainer = Color(0xFF271904)
val LightTertiary = Color(0xFF51643F)
val LightOnTertiary = Color(0xFFFFFFFF)
val LightTertiaryContainer = Color(0xFFD4EABB)
val LightOnTertiaryContainer = Color(0xFF0F2004)
val LightError = Color(0xFFBA1A1A)
val LightOnError = Color(0xFFFFFFFF)
val LightErrorContainer = Color(0xFFFFDAD6)
val LightOnErrorContainer = Color(0xFF410002)
val LightBackground = Color(0xFFFFF8F2)
val LightOnBackground = Color(0xFF201A17)
val LightSurface = Color(0xFFFFF8F2)
val LightOnSurface = Color(0xFF201A17)
val LightSurfaceVariant = Color(0xFFF3DFD0)
val LightOnSurfaceVariant = Color(0xFF52443B)
val LightOutline = Color(0xFF857469)
val LightOutlineVariant = Color(0xFFD8C3B6)

// Dark Mode
val DarkPrimary = Color(0xFFFFBA6E)
val DarkOnPrimary = Color(0xFF4A2800)
val DarkPrimaryContainer = Color(0xFF6A4320)
val DarkOnPrimaryContainer = Color(0xFFFFDDB5)
val DarkSecondary = Color(0xFFDFC2A2)
val DarkOnSecondary = Color(0xFF3F2D17)
val DarkSecondaryContainer = Color(0xFF57432B)
val DarkOnSecondaryContainer = Color(0xFFFBDEBC)
val DarkTertiary = Color(0xFFB8CEA0)
val DarkOnTertiary = Color(0xFF243514)
val DarkTertiaryContainer = Color(0xFF3A4C29)
val DarkOnTertiaryContainer = Color(0xFFD4EABB)
val DarkError = Color(0xFFFFB4AB)
val DarkOnError = Color(0xFF690005)
val DarkErrorContainer = Color(0xFF93000A)
val DarkOnErrorContainer = Color(0xFFFFDAD6)
val DarkBackground = Color(0xFF1A1511)
val DarkOnBackground = Color(0xFFF0DFD2)
val DarkSurface = Color(0xFF1A1511)
val DarkOnSurface = Color(0xFFF0DFD2)
val DarkSurfaceVariant = Color(0xFF52443B)
val DarkOnSurfaceVariant = Color(0xFFD8C3B6)
val DarkOutline = Color(0xFFA08D82)
val DarkOutlineVariant = Color(0xFF52443B)
```

### colors.xml Updates

```xml
<!-- values/colors.xml -->
<color name="splash_screen_background">#FFF8F2</color>
<color name="ic_launcher_background">#8B5E34</color>

<!-- values-night/colors.xml -->
<color name="splash_screen_background">#1A1511</color>
<color name="ic_launcher_background">#1A1511</color>
```
