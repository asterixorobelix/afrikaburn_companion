# Phase 7: Tab Visibility Control - Context

**Gathered:** 2026-01-22
**Status:** Ready for planning

<domain>
## Phase Boundary

Conditionally show/hide Map and Projects tabs based on unlock conditions. Tabs are completely hidden when locked. Once either the event date passes OR the user enters the 20km geofence, tabs become permanently visible. Uses EventDateService and GeofenceService from Phases 5-6.

</domain>

<decisions>
## Implementation Decisions

### Unlock transition
- Celebration moment when tabs unlock — not silent
- Toast/snackbar message: "Welcome to AfrikaBurn!"
- Unlock triggers on next app open (not immediate mid-session)
- Condition is detected and stored, but visual reveal waits for app launch

### Claude's Discretion
- How unlock state is persisted (SQLDelight recommended)
- Tab filtering implementation in navigation
- Deep link handling for hidden content
- Toast styling and duration

</decisions>

<specifics>
## Specific Ideas

- "Welcome to AfrikaBurn!" message creates a memorable arrival moment
- Delaying reveal until next app open keeps the experience clean (no jarring mid-session tab changes)

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within phase scope

</deferred>

---

*Phase: 07-tab-visibility-control*
*Context gathered: 2026-01-22*
