---
name: "Cứu Trợ Khẩn Cấp"
description: "Emergency Response Platform — connecting disaster victims with rescue centers across Vietnam"
colors:
  primary: "#f86e64"
  primary-subtle: "rgba(248, 110, 100, 0.15)"
  surface-bg: "#1E1E1E"
  surface-medium: "#252525"
  surface-card: "#2E2E2E"
  surface-elevated: "#303030"
  border-default: "#3A3A3A"
  border-light: "rgba(255, 255, 255, 0.06)"
  text-primary: "#F0F0F0"
  text-body: "#E0E0E0"
  text-muted: "#888888"
  text-subtle: "rgba(255, 255, 255, 0.55)"
  status-high: "#f86e64"
  status-medium: "#FF8A50"
  status-low: "#D4A84B"
  status-resolved: "#66BB6A"
typography:
  display:
    fontFamily: "'DM Sans', sans-serif"
    fontSize: "22px"
    fontWeight: 600
    lineHeight: 1.3
    letterSpacing: "normal"
  title:
    fontFamily: "'DM Sans', sans-serif"
    fontSize: "15px"
    fontWeight: 600
    lineHeight: 1.3
    letterSpacing: "normal"
  body:
    fontFamily: "'DM Sans', sans-serif"
    fontSize: "13px"
    fontWeight: 400
    lineHeight: 1.4
    letterSpacing: "normal"
  label:
    fontFamily: "'DM Sans', sans-serif"
    fontSize: "10px"
    fontWeight: 600
    lineHeight: 1
    letterSpacing: "0.04em"
rounded:
  sm: "4px"
  md: "8px"
  lg: "10px"
  xl: "12px"
  "2xl": "16px"
spacing:
  xs: "4px"
  sm: "8px"
  md: "16px"
  lg: "24px"
  xl: "40px"
components:
  button-icon:
    backgroundColor: "#3A3A3A"
    textColor: "#888888"
    rounded: "50%"
    padding: "0"
    size: "32px"
  button-icon-hover:
    backgroundColor: "#4A4A4A"
    textColor: "#E0E0E0"
  tab-active:
    backgroundColor: "{colors.primary}"
    textColor: "#FFFFFF"
    rounded: "{rounded.md}"
    typography: "title"
    padding: "10px 22px"
  tab-inactive:
    backgroundColor: "#3A3A3A"
    textColor: "#999999"
    rounded: "{rounded.md}"
    typography: "title"
    padding: "10px 22px"
  card:
    backgroundColor: "{colors.surface-card}"
    rounded: "{rounded.lg}"
    padding: "16px"
  card-hover:
    backgroundColor: "{colors.surface-card}"
    rounded: "{rounded.lg}"
    padding: "16px"
  card-active:
    backgroundColor: "{colors.surface-card}"
    rounded: "{rounded.lg}"
    padding: "16px"
  sidebar-icon:
    backgroundColor: "transparent"
    textColor: "#666666"
    rounded: "50%"
    size: "36px"
  sidebar-icon-active:
    backgroundColor: "{colors.primary}"
    textColor: "#FFFFFF"
  sidebar-icon-hover:
    backgroundColor: "{colors.primary-subtle}"
    textColor: "{colors.primary}"
---

# Design System: Cứu Trợ Khẩn Cấp

## 1. Overview

**Creative North Star: "The Signal Room"**

Every distress call is a signal that needs attention. The interface is pared down to focus, precision, and alertness — like a mission control room where each notification arrives with clarity and each action is deliberate. There is no decoration that doesn't serve the task. When someone's home is flooding, the chrome gets out of the way.

The system is dark by default: deep charcoal surfaces create a canvas where urgency signals — the coral accent, the colored status badges — command attention through contrast. Tonal layering replaces shadows as the primary depth mechanism; lighter surfaces sit forward, darker ones recede. This keeps the visual field clean and legible under the low-light conditions where emergency operations often take place.

The visual register is **product** (dashboard / admin tool). Density is acceptable where operators need it; whitespace is generous where citizens submit calls. Every pixel earns its place through **The Signal Room** test: would this element distract or clarify someone in a high-stakes decision?

**Key Characteristics:**
- Deep, tonal dark theme with layered charcoal surfaces
- Coral accent for urgency signals only — never decorative
- Fixed rem scale, single family (DM Sans), tight hierarchy
- Pill-shaped components (tabs, badges, tags) as the dominant affordance
- Circular icon buttons (32px) for toolbar actions
- Vietnamese-first: all UI copy, data, and geography
- No shadows at rest; tonal layering for depth; subtle shadows for interactive feedback

### Light Mode

A light theme companion is planned but not yet designed. When implemented, it should invert the tonal ramp while preserving the same contrast ratios, accent color, and component vocabulary. The dark surface ramp (`#1E1E1E` → `#303030`) maps to a light ramp (`#F0F0F0` → `#D0D0D0`) with the same relative ordering.

## 2. Colors

A restrained palette organized around a single coral accent. The accent covers ≤15% of any screen — its rarity is the point.

### Primary

- **Signal Coral** (`#f86e64`): The only accent. Used for active tabs, active sidebar icons, urgency badges (high), call-card active borders, map incident markers, and status tags. Never used for background fills, decorative elements, or inactive states.

### Neutral

- **Pit Black** (`#1E1E1E`): Deepest surface — page background and sidebar. Maximum contrast against body text.
- **Tool Steel** (`#252525`): App body background. The default content surface.
- **Charcoal Card** (`#2E2E2E`): Card and panel surface. Sits one step above the background in the tonal hierarchy.
- **Zinc Lid** (`#303030`): Main container and elevated surfaces.
- **Rail Gray** (`#3A3A3A`): Borders, dividers, inactive tab backgrounds.
- **Frost White** (`#F0F0F0`): Primary text — headings, card titles, active labels.
- **Cloud Gray** (`#E0E0E0`): Body text — descriptions, detail values.
- **Ash Muted** (`#888888`): Secondary text — timestamps, meta labels, placeholder content.
- **Ghost White** (`rgba(255, 255, 255, 0.55)`): Lowest-priority text — top bar secondary info, captions.

### Status

- **High / Active / Error** — Coral (`#f86e64`): Urgency ≥80, "active" status, error states. Background: `rgba(248, 110, 100, 0.15)`.
- **Medium / In Progress** — Warm Ember (`#FF8A50`): Urgency 60-79, "in progress" status. Background: `rgba(230, 81, 0, 0.15)`.
- **Low** — Pale Gold (`#D4A84B`): Urgency <60. Background: `rgba(141, 110, 0, 0.15)`.
- **Resolved** — Meadow Green (`#66BB6A`): Resolved/delivered status. Background: `rgba(46, 125, 50, 0.15)`.

### Named Rules

**The One-Fire Rule.** The coral accent covers ≤15% of any screen. When everything is urgent, nothing is.

**The Semantic-Only Rule.** Color is never the sole conveyor of meaning. Every status has a text label, an icon, and a badge shape alongside its color.

## 3. Typography

**Display & Body Font:** DM Sans (Google Fonts, variable weight 100–1000, axis `opsz` 9–40)

A single family across the entire system. DM Sans combines geometric precision with humanist warmth — technical enough for operational dashboards, readable enough for stressed citizens submitting calls.

**Character:** Calm, legible, unfussy. No display/body pairing is needed for a product dashboard. The weight contrast between hierarchy levels (400 → 500 → 600) provides all the emphasis the interface needs.

### Hierarchy

- **Display** (600, 22px, 1.3): Section headings, page titles. `text-wrap: balance`.
- **Title** (600, 15px, 1.3): Card titles, call names. The dominant label size.
- **Body** (400, 13px, 1.4): Descriptions, detail values, tab labels. Max line length 65–75ch for prose.
- **Label** (600, 10px, 1, `0.04em` letter-spacing): Status tags, urgency badges, supplies tags, small meta. Uppercase for status tags.
- **Brand** (600, 13px, `0.04em` letter-spacing): Top bar brand name. Uppercase.

### Named Rules

**The Fixed-Scale Rule.** All font sizes are in fixed `px`. No fluid `clamp()` sizing — product UI is viewed at consistent DPI. An h1 that shrinks in a sidebar is worse, not better.

**The Tight Scale Rule.** Ratio between steps is ~1.15. A tighter scale prevents the visual noise that exaggerated heading sizes create in dashboard contexts.

## 4. Elevation

The system uses **tonal layering** as its primary depth mechanism. Surfaces are distinguished by background lightness rather than by shadow. Darker surfaces recede (page background, sidebar), lighter ones advance (cards, elevated panels). This keeps the interface clean, reduces visual noise, and is especially legible in low-light conditions.

Shadows exist only as a response to **interactive state** — hover, active, focus — never as a rest-state depth cue.

### Shadow Vocabulary

- **Card Hover** (`0 2px 8px rgba(0,0,0,0.2)`): Applied to call cards on hover. Signals interactivity.
- **Card Active** (`0 2px 12px rgba(248,110,100,0.18)`): Applied to the selected/expanded card. Accent-colored shadow ties the glow to the brand signal.
- **Map Overlay** (`0 1px 4px rgba(0,0,0,0.3)`): Subtle lift for map control buttons against the tile layer.

### Named Rules

**The Tonal-By-Default Rule.** Surfaces are flat at rest. Use background lightness, not shadows, to establish depth. Shadows are a response to state, not a layout tool.

## 5. Components

### Buttons

- **Shape:** Circular for icon buttons (50% radius, 32px). Pill-shaped for text buttons.
- **Icon Button:** Dark base (`#3A3A3A`, `#888` icon). Hover: lighter base (`#4A4A4A`, `#E0E0E0` icon). No outline.
- **Active/Current:** Fills with coral accent (`#f86e64`), white icon. Used for the currently selected sidebar tool.
- **Primary Action (future):** Full coral fill, white text, 8px radius, `padding: 10px 22px`. Bold and immediate.

### Tabs (Status Filter)

- **Shape:** Rounded pill (8px radius).
- **Active:** Coral fill (`#f86e64`), white text, bold weight.
- **Inactive:** Dark fill (`#3A3A3A`), muted text (`#999`).
- **Hover:** Inactive tabs shift lighter (`#444`, text `#CCC`).
- **Typography:** 14px/500 (title weight).
- **Spacing:** 10px vertical, 22px horizontal padding. 8px gap between tabs.

### Call Cards

- **Shape:** Rounded rectangle (10px radius). 1px border.
- **Default:** `#2E2E2E` background, `#3A3A3A` border.
- **Hover:** Border lightens to `#555`, subtle shadow (`0 2px 8px rgba(0,0,0,0.2)`).
- **Active/Expanded:** Coral border (`#f86e64`), accent shadow (`0 2px 12px rgba(248,110,100,0.18)`).
- **Internal padding:** 16px.
- **Card list gap:** 10px.

### Status Tags

- **Shape:** Small rounded pill (4px radius).
- **Typography:** 10px/600, uppercase, letter-spacing `0.04em`.
- **Colors:** Semantic background + text per status level:
  - Active: `rgba(248,110,100,0.15)` bg, `#f86e64` text
  - In Progress: `rgba(230,81,0,0.15)` bg, `#FF8A50` text
  - Resolved: `rgba(46,125,50,0.15)` bg, `#66BB6A` text

### Urgency Badges

- **Shape:** Small rounded pill (4px radius).
- **Typography:** 11px/600. Prefixed with numeric score.
- **Colors:** Same semantic scheme as status tags.
  - High (≥80): Coral theme
  - Medium (60-79): Ember theme
  - Low (<60): Gold theme

### Supplies Tags

- **Shape:** Small rounded pill (4px radius).
- **Colors:** Purple on deep purple — `#B39DDB` text, `#2A2540` background.
- **Typography:** 10px/600.
- **Margin:** 2px horizontal gap between tags.

### Filter Icon (Toolbar)

- **Shape:** Standard icon (inline SVG, 18px).
- **Color:** Muted (`#666`), lightens on hover (`#999`).
- **Placement:** Top-right of content header, aligned with the title.

### Sidebar Icons

- **Shape:** Circle (36px, 50% radius).
- **Default:** Transparent bg, `#666` icon.
- **Active:** Coral fill (`#f86e64`), white icon.
- **Hover (inactive):** Subtle coral tint (`rgba(248,110,100,0.12)`), coral icon.
- **Spacing:** 28px gap between icons vertically. 20px top padding.
- **Layout:** 60px fixed width sidebar on desktop; horizontal bar on mobile.

### Expanded Call Detail Section

- **Trigger:** Card click expands to reveal detail grid.
- **Divider:** 1px `#3A3A3A` top border separates detail from card summary.
- **Grid:** 2-column layout, 8px row gap, 20px column gap.
- **Labels:** 12px, muted (`#888`).
- **Values:** 13px/600, body text color (`#E0E0E0`).
- **Action row:** 4 icon buttons in a row, separated by 8px gap, top border divider.

### Map Panel

- **Shape:** Fills remaining content area. Rounded 12px (outer container provides the radius).
- **Background:** `#333` (visible behind tiles during load).
- **Overlay controls:** Top-right — locate button + zoom button. Dark icon buttons with map-specific shadow.
- **Center label:** Bottom-center — dark pill (`rgba(0,0,0,0.7)`), white 12px text. Shows region + active call count.

## 6. Do's and Don'ts

### Do:

- **Do** use the coral accent sparingly — ≤15% of any screen. Its rarity signals genuine urgency.
- **Do** use tonal layering for depth. Keep surfaces flat at rest; shadows only on hover/active.
- **Do** pair every status color with a text label. Color alone is never sufficient to convey status.
- **Do** use DM Sans across the entire interface. No second font family.
- **Do** use fixed px sizes, not fluid `clamp()`. This is a product dashboard, not a marketing page.
- **Do** keep line length at 65–75ch for prose descriptions in cards.
- **Do** use circular (50% radius) icon buttons at exactly 32px for toolbar actions.
- **Do** use pill-shaped components (8px radius) for tabs, status tags, and badges.
- **Do** use the `text-wrap: balance` property on headings (h1–h3) for even line lengths.
- **Do** ensure all text meets WCAG AA contrast: body ≥4.5:1, large text ≥3:1.

### Don't:

- **Don't** use gradient text (`background-clip: text`). Single solid colors only.
- **Don't** use glassmorphism (decorative blur/glass cards).
- **Don't** use the hero-metric template (big number + small label + gradient accent). This is a dashboard, not a SaaS landing page.
- **Don't** use side-stripe borders (`border-left`/`border-right` >1px as accent). Use full borders or background tints instead.
- **Don't** use tiny uppercase tracked eyebrow text ("ABOUT" / "PROCESS" / "STATUS") above every section. One deliberate kicker is voice; an eyebrow on every section is AI grammar.
- **Don't** use numbered section markers (`01 / 02 / 03`) as default scaffolding.
- **Don't** use the accent color for inactive states, decorative fills, or chrome. It communicates urgency; overuse dilutes it.
- **Don't** use identical card grids (same-sized cards with icon + heading + text repeated endlessly). Vary the content presentation.
- **Don't** let text overflow its container. Test headings at all breakpoints; if they overflow, reduce clamp max or rewrite the copy.
- **Don't** use the cream/sand/beige warm-neutral background default. This is a dark-toned operational interface.
- **Don't** invent custom affordances for standard tasks (scrollbars, form controls, modals). Standard patterns earn user trust faster.
