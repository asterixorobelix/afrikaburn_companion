# Features Research: Offline Maps

**Research Date:** 2026-01-18
**Milestone:** v3.0 Offline Map

## Table Stakes
*Must-have features for any offline event map - users will expect these as baseline functionality*

### Core Map Functionality
- **Offline map tiles**: Pre-downloaded map data that works without any network connectivity
- **GPS location tracking**: Show user's current position on the map using device magnetometer/GPS (works without internet)
- **Pinch-to-zoom**: Standard two-finger gesture for zooming in/out
- **Pan/scroll**: Drag to move around the map
- **Double-tap zoom**: Quick zoom in on tap location

### Point of Interest (POI) Display
- **Theme camp markers**: Show all registered theme camps with names
- **Art installation markers**: Display art pieces with titles
- **Service location markers**: Toilets, medical, rangers, ice sales (if applicable)
- **Visual distinction**: Different marker styles/colors for different POI types

### Search & Discovery
- **Full-text search**: Search camps and art by name (iBurn's search supports queries like "bacon" or "coffee")
- **Nearby mode**: Show what's close to current location
- **Favorites list**: Save items for quick access
- **Event filtering**: Filter by day, time, category

### Personal Markers
- **Drop pin for own camp**: Save personal camp location for easy return navigation
- **Bike/vehicle marker**: Mark where bike is parked (critical for desert navigation)
- **Multiple saved pins**: Support for friends' camps, meeting points

### Event Integration
- **Event schedule display**: Show what's happening when and where
- **Event notifications**: Reminders for favorited events (even offline)
- **Walking/biking time estimates**: Distance calculations between points

### First-Launch Requirements
- **One-time download**: App must be opened with internet before event to cache all data
- **Clear download progress**: Show what's being downloaded and remaining data
- **Storage requirements display**: Tell users how much space is needed

## Differentiators
*Features that would make AfrikaBurn Companion stand out from competitors*

### Navigation Excellence
- **Playa address system**: Show current location as grid address (e.g., "6:30 & B") like iBurn's geocoder
- **Compass mode**: Built-in compass for orientation in featureless terrain
- **Backtrack feature**: Record path taken, enable retracing steps (critical when disoriented at night)
- **Walking directions**: Simple routing between points without internet

### Smart Discovery
- **"What's happening now" view**: Real-time filtered view of current events at nearby locations
- **Category-based exploration**: Browse by type (music, workshops, food, etc.)
- **Sunrise/sunset schedule integration**: Time-based recommendations ("catch the sunrise at...")

### Battery & Performance Optimization
- **Lightweight offline mode**: Minimal battery drain when using GPS
- **Lazy GPS polling**: Only update location when app is actively used, not continuous background tracking
- **Dark mode**: Reduce battery usage at night, better visibility in dark conditions
- **Efficient map rendering**: Fast tile loading with minimal memory footprint

### User Context Awareness
- **Time-of-day theming**: Different UI for day vs. night use
- **Low-battery mode**: Reduce features to extend battery life when low
- **Quick access shortcuts**: One-tap to "My Camp" or "Center Camp"

### Enhanced Offline Capabilities
- **Offline images**: Pre-cache photos of theme camps and art installations
- **Notes on pins**: Add personal notes to saved locations
- **Share location codes**: Generate shareable text-based location codes for meeting friends

### Accessibility
- **High contrast mode**: For bright desert conditions
- **Large touch targets**: Easy to use with dusty/dirty hands
- **Voice-guided directions**: Audio navigation assistance

## Anti-Features
*Features deliberately NOT building in v3.0 - avoid complexity and scope creep*

### Social/Network Features (Require Connectivity)
- **Live friend tracking**: Real-time location sharing with friends
- **Mesh networking**: Peer-to-peer communication between devices
- **Social feeds/activity streams**: Live updates from other users
- **Chat/messaging**: In-app communication features
- **User-generated content sync**: Uploading photos, reviews, or check-ins

### Over-Engineering
- **3D map view**: Adds complexity without significant benefit in flat desert terrain
- **AR (Augmented Reality)**: AR photo booths, AR navigation overlays
- **Turn-by-turn voice navigation**: Overkill for a walkable event space
- **Offline voice search**: Complex to implement, low value
- **Real-time traffic/crowd density**: Requires network infrastructure

### Battery Drains
- **Continuous background location tracking**: Kills battery, not needed
- **Push notifications requiring server connection**: Won't work offline anyway
- **Background sync processes**: No network to sync with
- **Animated map elements**: Unnecessary battery drain

### Scope Creep
- **Ticket purchasing/validation**: Separate concern, adds complexity
- **Payment/tipping systems**: Requires network, against gift economy principles
- **Full event schedule management**: Keep focused on map/location
- **Photo gallery/media storage**: Different app category
- **Weather forecasts**: Requires network
- **Emergency SOS with location sharing**: Requires network (use dedicated emergency devices)

### Over-Personalization
- **AI-powered recommendations**: Adds complexity, questionable value
- **Personalized event suggestions**: Keep it simple
- **Social graph integration**: Facebook/Instagram connections
- **Achievement/gamification systems**: Distracting from real experience

### Maintenance Burdens
- **Custom map tile hosting**: Use established providers (Mapbox)
- **Real-time data sync**: Defeats offline-first purpose
- **User accounts/profiles**: Add friction, not needed for core use case

## User Expectations
*Expected interactions and behaviors based on established patterns*

### Gesture Interactions
| Gesture | Expected Behavior |
|---------|-------------------|
| Single tap on POI | Show info card/popup with details |
| Single tap on empty map | Dismiss any open popups |
| Double tap | Zoom in centered on tap point |
| Pinch outward | Zoom in |
| Pinch inward | Zoom out |
| Two-finger drag | Pan map |
| Long press | Drop a pin at location |
| Swipe up on info card | Expand to full details |
| Swipe down on info card | Collapse/dismiss |

### Map Behavior
- **Smooth animations**: 60fps scrolling and zooming
- **Responsive load**: Map tiles should load within 100ms from local cache
- **Location accuracy indicator**: Show confidence radius around current position
- **Auto-center option**: Button to snap back to current location
- **North-up orientation**: Default map orientation (option for heading-up)

### Search Behavior
- **Instant results**: Filter as user types (offline search)
- **Recent searches**: Quick access to previous queries
- **Category chips**: Horizontal scroll of filter categories
- **Clear filters**: Easy way to reset all filters
- **Result count**: Show how many items match current filters

### Information Architecture
- **Progressive disclosure**: Basic info first, tap for more details
- **Consistent card layout**: Same information hierarchy across all POI types
- **Clear visual hierarchy**: Important info (name, distance) most prominent
- **Offline indicators**: Clear indication when data was last updated

### Error States & Edge Cases
- **No GPS signal**: Show last known location with timestamp, offer manual positioning
- **Location outside event bounds**: Graceful handling with helpful message
- **Empty search results**: Suggest alternatives or clear filters
- **Storage full**: Warn before download fails, suggest cleanup

### Accessibility Expectations
- **Screen reader support**: All interactive elements labeled
- **Minimum touch target size**: 44x44pt for all interactive elements
- **Color-blind friendly**: Don't rely solely on color for meaning
- **Text scaling**: Respect system text size preferences

### Performance Expectations (Based on Industry Standards)
- **App launch**: < 2 seconds to interactive map
- **Map pan/zoom**: 60fps, no jank
- **Search results**: < 100ms response
- **Pin drop**: Instant haptic feedback
- **First download**: Progress indicator, pausable/resumable

### Trust & Transparency
- **Clear data freshness**: "Map data as of April 15, 2026"
- **Storage usage**: Show how much space the app uses
- **Battery usage**: Efficient, don't drain battery in background
- **No surprise permissions**: Only request location when needed, explain why

---

## Research Sources

### Festival Companion Apps
- [iBurn App](https://iburnapp.com/) - Burning Man's most popular offline guide
- [Dust - A Guide for Burners](https://apps.apple.com/us/app/dust-a-guide-for-burners/id6456943178) - Alternative Burning Man guide
- [AfrikaBurn - Creation App](https://apps.apple.com/us/app/afrikaburn-creation/id6496346176) - Official AfrikaBurn app
- [FestivApp](https://festivapp.eu/) - European festival companion
- [Headliners - Festival Planner](https://apps.apple.com/us/app/headliners-festival-planner/id6478820193) - Music festival planning app

### UX & Design Patterns
- [Map UI Patterns](https://mapuipatterns.com/) - Best practices for map applications
- [Offline Mobile App Design Best Practices](https://leancode.co/blog/offline-mobile-app-design) - LeanCode guide
- [Map UI Design Best Practices](https://www.eleken.co/blog-posts/map-ui-design) - Eleken design guide
- [Filter UI Design Patterns](https://www.setproduct.com/blog/filter-ui-design) - SetProduct filter UX guide
- [Gestures in Mobile UX](https://uxplanet.org/in-app-gestures-and-mobile-app-usability-d2e737bd5250) - UX Planet

### Festival App Development
- [App UX for 100,000 Festival-Goers](https://www.ticketfairy.com/blog/app-ux-for-100000-festival-goers-real-time-updates-offline-maps-and-safety) - Ticket Fairy insights
- [Festival Tech Overload](https://www.ticketfairy.com/blog/festival-tech-overload-in-2026-choosing-the-right-tools-without-overwhelm) - Avoiding complexity
- [Festival Mobile App Development](https://www.ticketfairy.com/blog/2025/07/08/festival-mobile-app-development-engaging-attendees-on-their-phones/) - Best practices

### Technical References
- [Mapbox iOS Gestures](https://docs.mapbox.com/ios/maps/guides/user-interaction/gestures/) - Gesture implementation
- [Pin Drop App](https://apps.apple.com/us/app/pin-drop-places-that-matter/id425356789) - Pin/marker patterns
- [MAPS.ME Offline Navigation](https://apps.apple.com/us/app/maps-me-offline-maps-gps-nav/id510623322) - Offline map reference

---
*Research completed: 2026-01-18*
