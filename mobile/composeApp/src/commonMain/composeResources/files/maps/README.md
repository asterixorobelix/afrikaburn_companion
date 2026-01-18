# Offline Map Assets

This directory contains assets for offline map rendering in the AfrikaBurn Companion app.

## Required Files

### PMTiles File (USER ACTION REQUIRED)

The app requires a PMTiles file for offline map tiles:

**File:** `tankwa-karoo.pmtiles`

**Location:** Place in this directory alongside style.json

**Region:** Tankwa Karoo, South Africa (approximately -32.35, 19.45)

**Recommended specifications:**
- Zoom levels: 10-16
- Target size: 20-50MB
- Format: OpenMapTiles schema

### How to Obtain

1. **Protomaps** (recommended):
   - Visit https://protomaps.com/
   - Extract region covering Tankwa Karoo
   - Download as PMTiles

2. **MapTiler Data**:
   - Visit https://data.maptiler.com/
   - Download OpenStreetMap tiles for South Africa
   - Extract Tankwa region

3. **Custom extraction**:
   - Use `pmtiles` CLI tool
   - Extract from larger regional dataset

### Optional: Fonts and Sprites

For label rendering, you may also need:

- `fonts/` directory with .pbf glyph files
- `sprites/` directory with sprite images

These are optional - the map will render without labels if fonts are unavailable.

## Files in this Directory

- `style.json` - Map style configuration for MapLibre
- `README.md` - This file
- `tankwa-karoo.pmtiles` - (USER PROVIDED) Offline map tiles
