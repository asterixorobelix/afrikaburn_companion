# Research Summary: v3.0 Offline Map

**Synthesis Date:** 2026-01-18
**Milestone:** v3.0 Offline Map

---

## Key Decisions

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Map SDK | MapLibre Compose v0.11.1+ | Official KMP support, no licensing costs, sufficient feature set |
| Tile Format | PMTiles (bundled) | Single-file distribution, efficient, works offline when bundled as asset |
| Location Library | Compass v1.6.7 | Comprehensive KMP location toolkit with built-in permissions |
| Local Storage | SQLDelight | Already in project infra, fits user camp pin persistence |
| Offline Strategy | Pre-bundled tiles (~50MB) | Guaranteed offline, no download required |

---

## Stack Summary

**Core Dependencies:**
```toml
maplibre-compose = "0.11.1"
compass-geolocation = "1.6.7"
sqldelight = "2.0.2"
```

**Key Files to Bundle:**
- `tankwa-karoo.pmtiles` (~20-50MB, zoom 10-16)
- `style.json` (local style definition)
- `fonts/` (glyphs for offline labels)
- `sprites/` (icons for markers)

---

## Table Stakes Features

**Must have for v3.0:**
1. Offline map tiles for Tankwa Karoo region
2. Camp markers with distinct icons
3. Artwork markers with distinct icons
4. Tap marker → show detail page
5. User GPS location (blue dot)
6. User can place own camp pin
7. Camp pin persists locally
8. Search camps/artworks by name
9. Standard gestures (pinch zoom, pan, double-tap)
10. Works fully offline

---

## Differentiators (Consider for v3.0 or defer)

- Compass mode for desert orientation
- "My Camp" quick-access button
- Backtrack feature (record path)
- Battery-efficient GPS (lazy polling)

---

## Anti-Features (NOT in v3.0)

| Feature | Reason |
|---------|--------|
| Live friend tracking | Requires network |
| Turn-by-turn navigation | Overkill, use external maps |
| 3D/AR views | Complexity without benefit |
| Continuous background GPS | Battery killer |
| User accounts | Unnecessary friction |
| Performance/event markers | Scope v3.0 to camps + artworks |

---

## Critical Pitfalls to Avoid

| Pitfall | Prevention |
|---------|------------|
| Offline tiles blank | Bundle ALL resources (tiles, fonts, sprites) locally |
| Battery drain | Use balanced accuracy, stop GPS when leaving screen |
| Memory leaks on iOS | Proper cleanup in `DisposableEffect.onDispose` |
| Android 12+ permissions | Request both COARSE and FINE location |
| Missing labels offline | Bundle font glyphs, use local paths in style.json |

---

## Build Order

**Phase 1 - Foundation:** Dependencies, SQLDelight schema, domain models, platform interfaces
**Phase 2 - Data Layer:** Repositories, platform services (Location, Offline)
**Phase 3 - Presentation:** MapViewModel, MapUiState, Koin module
**Phase 4 - UI Layer:** MapScreen, markers, controls, navigation integration
**Phase 5 - Polish:** Search, error handling, testing

---

## Pre-Event Verification Checklist

- [ ] App works in airplane mode
- [ ] All camp/artwork markers visible
- [ ] Labels and icons render offline
- [ ] GPS location shows correctly
- [ ] User camp pin persists after restart
- [ ] Battery drain acceptable (<5% in 30 min)

---

*Summary completed: 2026-01-18*
