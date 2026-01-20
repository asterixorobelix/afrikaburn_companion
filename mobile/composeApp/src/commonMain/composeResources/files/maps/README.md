# Offline Map Assets

This directory contains assets for offline map rendering in the AfrikaBurn Companion app.

## Current Status

**Without PMTiles:** Map shows dark background (#1a1a2e) with camp/artwork markers. Fully functional for navigation.

**With PMTiles:** Map shows terrain, roads, and place names in addition to markers.

## How to Add PMTiles (Optional)

### Step 1: Download from Protomaps (Easiest)

1. Visit **https://app.protomaps.com/downloads/osm**
2. Draw a bounding box around the Tankwa Karoo region:
   - Approximate bounds: `19.75, -32.60` to `20.05, -32.35`
   - Center: `-32.48, 19.90` (AfrikaBurn location)
3. Select zoom levels 10-16
4. Download as PMTiles format
5. Rename to `tankwa-karoo.pmtiles`
6. Place in this directory

### Step 2: Update style.json

Replace the simplified style.json with the full version:

```json
{
  "version": 8,
  "name": "AfrikaBurn Dark",
  "sources": {
    "openmaptiles": {
      "type": "vector",
      "url": "pmtiles://asset://files/maps/tankwa-karoo.pmtiles"
    }
  },
  "layers": [
    {
      "id": "background",
      "type": "background",
      "paint": { "background-color": "#1a1a2e" }
    },
    {
      "id": "landuse-desert",
      "type": "fill",
      "source": "openmaptiles",
      "source-layer": "landuse",
      "filter": ["all", ["==", "class", "sand"]],
      "paint": { "fill-color": "#252540" }
    },
    {
      "id": "road-minor",
      "type": "line",
      "source": "openmaptiles",
      "source-layer": "transportation",
      "filter": ["all", ["in", "class", "minor", "service", "path", "track"]],
      "paint": {
        "line-color": "#3a3a5c",
        "line-width": { "stops": [[12, 0.5], [16, 2]] }
      }
    },
    {
      "id": "road-primary",
      "type": "line",
      "source": "openmaptiles",
      "source-layer": "transportation",
      "filter": ["all", ["==", "class", "primary"]],
      "paint": {
        "line-color": "#5a5a7a",
        "line-width": { "stops": [[8, 1], [16, 6]] }
      }
    }
  ]
}
```

### Alternative: pmtiles CLI

```bash
# Install
npm install -g pmtiles

# Or with pip
pip install pmtiles

# Extract from larger dataset
pmtiles extract south-africa.pmtiles tankwa-karoo.pmtiles \
  --bbox=19.75,-32.60,20.05,-32.35 \
  --minzoom=10 --maxzoom=16
```

## Files in this Directory

| File | Status | Description |
|------|--------|-------------|
| `style.json` | ✓ Included | Map style (works with or without PMTiles) |
| `mock-locations.geojson` | ✓ Included | Camp and artwork marker locations |
| `tankwa-karoo.pmtiles` | ⚠️ Optional | Offline map tiles for terrain/roads |
| `README.md` | ✓ Included | This file |

## Recommended PMTiles Specs

- **Zoom levels:** 10-16
- **Target size:** 20-50MB
- **Format:** OpenMapTiles schema
- **Region:** Tankwa Karoo (-32.48, 19.90)
