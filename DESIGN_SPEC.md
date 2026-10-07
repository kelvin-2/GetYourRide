# GetYourRide — Design System Spec

> **Purpose of this document**
> This is the single source of truth for how the **GetYourRide** Android app looks.
> It is written so that an AI (or a developer) can read it and build a **website**
> that looks and feels identical to the app — same colors, type, spacing, shapes,
> and component styling — so the product is **consistent across app and web**.
>
> All values below were extracted directly from the app's Jetpack Compose source
> (`ui/theme/*` and `ui/components/*`). Where the app uses Compose units (`dp`, `sp`),
> web equivalents (`px`, `rem`) are provided. **1dp ≈ 1px** and **1sp ≈ 1px** for a
> 1:1 mapping; the rem values assume a 16px root.

---

## 0. TL;DR for an AI building the website

Build a **light, card-based UI** with:

- **Backgrounds:** very light grey `#F5F6FA`.
- **Cards:** pure white `#FFFFFF`, rounded corners (12–16px), soft shadow.
- **Primary brand color (navy):** `#1A2E5A` — used for headers, text, outlines, logo tile.
- **Accent / call-to-action (orange):** `#F57C00` — used for primary buttons, active nav, links-to-action, badges.
- **Primary buttons:** solid **orange**, white text, 10px radius, ~50px tall.
- **Secondary buttons:** **outlined navy** (1.5px border), navy text, transparent fill.
- **Text:** navy `#1A2E5A` for primary, muted navy (60% alpha) for secondary.
- **Font:** system default sans-serif (map to Inter / Roboto / system UI stack on web).
- **Field labels:** small, UPPERCASE, semibold, navy, letter-spaced.
- **Top bar:** dark navy `#0C1E42` with a centered orange bus icon + "GetYourRide" in white.

> ⚠️ **Ignore `ui/theme/Color.kt` and the purple scheme in `Theme.kt`.** Those are
> leftover Android Studio template defaults (`Purple80`, `Purple40`, etc.) and are
> **not** the real brand. The real palette lives in `GetYourRideColors.kt`. The app
> is navy + orange, not purple.

---

## 1. Color palette

### 1.1 Brand palette
Source: `ui/theme/GetYourRideColors.kt`

| Token          | Hex       | Usage |
|----------------|-----------|-------|
| `NavyPrimary`  | `#1A2E5A` | Primary brand color. Headers, primary text, outlined buttons, logo tile, field labels. |
| `OrangeAccent` | `#F57C00` | Accent / CTA. Primary buttons, active nav icon+label, action links, badges, icon highlights. |
| `GreenSuccess` | `#2E7D32` | Success states (e.g. "NSFAS Funded" badge, active status). |
| `SurfaceGrey`  | `#F5F6FA` | Screen / page background. |
| `CardWhite`    | `#FFFFFF` | Card and surface backgrounds, nav bar background. |
| `DangerRed`    | `#C62828` | Destructive actions, error messages. |

### 1.2 Text colors
| Token         | Value                       | Usage |
|---------------|-----------------------------|-------|
| `TextPrimary` | `#1A2E5A` (navy)            | Primary body text, values. |
| `TextMuted`   | navy at **60% alpha**       | Secondary text, captions, helper copy. |
| `TextHint`    | `#9E9E9E`                   | Input placeholder text. |
| `TextSecondary` (local) | `#8A8FA3`         | Row sub-labels in cards (used in InfoCard / SettingsListCard). |

### 1.3 Status / badge colors
| Token             | Hex / value   | Meaning |
|-------------------|---------------|---------|
| `StatusPending`   | `#FFC107`     | Pending |
| `StatusActive`    | `#2E7D32`     | Active (= GreenSuccess) |
| `StatusCancelled` | `#C62828`     | Cancelled (= DangerRed) |
| `StatusCompleted` | `#757575`     | Completed |

### 1.4 Neutral / support colors
| Token         | Hex       | Usage |
|---------------|-----------|-------|
| `BorderLight` | `#E0E0E0` | Dividers, unfocused input borders. |
| `IconTint`    | `#757575` | Default/inactive icons, inactive nav items. |
| `TopBarNavy`  | `#0C1E42` | Top app bar background (slightly darker than NavyPrimary). |

### 1.5 Suggested CSS variables for the website
```css
:root {
  /* Brand */
  --gyr-navy:          #1A2E5A;
  --gyr-navy-topbar:   #0C1E42;
  --gyr-orange:        #F57C00;
  --gyr-green:         #2E7D32;
  --gyr-red:           #C62828;

  /* Surfaces */
  --gyr-bg:            #F5F6FA;  /* page background */
  --gyr-card:          #FFFFFF;  /* cards */

  /* Text */
  --gyr-text:          #1A2E5A;             /* primary */
  --gyr-text-muted:    rgba(26,46,90,0.6);  /* secondary */
  --gyr-text-hint:     #9E9E9E;             /* placeholders */
  --gyr-text-sub:      #8A8FA3;             /* card sub-labels */

  /* Lines & icons */
  --gyr-border:        #E0E0E0;
  --gyr-icon:          #757575;

  /* Status */
  --gyr-pending:       #FFC107;
  --gyr-completed:     #757575;
}
```

---

## 2. Typography

Source: `ui/theme/Type.kt` + per-screen overrides. The app relies mostly on
Material 3 defaults plus explicit `fontSize` / `fontWeight` per component, so the
practical type scale is defined by **actual usage**, documented below.

### 2.1 Font family
- **App:** `FontFamily.Default` (system sans-serif — Roboto on Android).
- **Web mapping:** use a clean neutral sans. Recommended stack:
  ```css
  font-family: Inter, Roboto, -apple-system, "Segoe UI", system-ui, sans-serif;
  ```
- Base body style (`bodyLarge`): 16sp, weight 400, line-height 24sp, letter-spacing 0.5sp.

### 2.2 Practical type scale (from real screens)
Map `sp` → `px`/`rem` 1:1 (root 16px).

| Role / example                         | Size      | Weight         | Letter-spacing | Color        | Notes |
|----------------------------------------|-----------|----------------|----------------|--------------|-------|
| **App wordmark** ("GET YOUR RIDE")     | 22sp      | Bold (700)     | 1.5sp          | Navy         | All-caps hero/logo text. |
| **Screen / card heading**              | 18sp      | SemiBold (600) | —              | Navy / white | e.g. profile name. |
| **Button label**                       | 16sp      | SemiBold (600) | —              | White / navy | Primary & secondary buttons. |
| **Body / base**                        | 16sp      | Regular (400)  | 0.5sp          | Navy         | Default running text. |
| **Avatar initials**                    | 24sp      | Bold (700)     | —              | White        | Inside orange circle. |
| **Body small / helper**                | 13–14sp   | Regular/Medium | —              | Muted navy   | Captions, taglines, sub-values. |
| **Field VALUE text**                   | 14sp      | Medium (500)   | —              | Navy         | Card row values, input text. |
| **Input placeholder**                  | 14sp      | Regular (400)  | —              | Hint `#9E9E9E` | — |
| **Field LABEL** (UPPERCASE)            | 11sp      | SemiBold (600) | 0.5sp          | Navy         | Rendered `.uppercase()`. |
| **Row sub-label / overline**           | 11sp      | Medium (500)   | —              | `#8A8FA3`    | Small label above a value. |
| **Nav tab label**                      | 11sp      | Regular (400)  | —              | Orange/grey  | Bottom nav. |
| **Caption / legal footer**             | 12sp      | Regular (400)  | —              | Muted navy   | Terms, "OR" divider, step label. |
| **Badge text**                         | 9–11sp    | SemiBold (600) | —              | White/status | Nav badge 9sp, status badge 11sp. |

### 2.3 Web CSS type tokens
```css
:root {
  --fs-hero:     22px;  /* wordmark, bold, letter-spacing .094em (~1.5sp) */
  --fs-heading:  18px;  /* semibold */
  --fs-button:   16px;  /* semibold */
  --fs-body:     16px;  /* regular, letter-spacing .03em */
  --fs-sub:      14px;  /* values / input text */
  --fs-caption:  13px;
  --fs-legal:    12px;
  --fs-label:    11px;  /* UPPERCASE labels, letter-spacing .045em */
}
```
> Convention: **UPPERCASE field labels** (`text-transform: uppercase`), 11px,
> weight 600, slight letter-spacing, navy — this is a signature of the app's forms.

---

## 3. Spacing, radius & elevation

The app uses an informal **4dp grid**. Common values:

### 3.1 Spacing scale (dp → px)
`2, 4, 6, 8, 12, 14, 16, 20, 24, 32, 48, 56`

| Context                               | Value |
|---------------------------------------|-------|
| Screen horizontal padding             | 24dp (auth) / 16dp (list rows) |
| Card inner padding                    | 20dp (forms) / 16dp (rows) |
| Vertical gap between form fields      | 16dp |
| Small label-to-field gap              | 4dp |
| Row vertical padding (list items)     | 14dp |
| Section/hero top spacing              | 32–56dp |

### 3.2 Corner radius
| Element                         | Radius |
|---------------------------------|--------|
| Buttons, inputs                 | 10dp |
| Form card                       | 12dp |
| Content cards (Info/Settings)   | 16dp |
| Logo tile                       | 16dp |
| Icon chip (small square behind icon) | 10dp |
| Pill / badge                    | 20dp (fully rounded) |
| Avatar                          | circle (50%) |
| Progress segment                | 2dp |

### 3.3 Elevation (shadow)
| Element           | Elevation | Web shadow suggestion |
|-------------------|-----------|-----------------------|
| Form card         | 2dp       | `0 1px 3px rgba(0,0,0,.12)` |
| Settings card     | 2dp       | `0 1px 3px rgba(0,0,0,.12)` |
| Info card         | 4dp       | `0 2px 6px rgba(0,0,0,.14)` |
| Bottom nav        | 8dp       | `0 -2px 8px rgba(0,0,0,.10)` |

### 3.4 Component sizing
| Element                 | Size |
|-------------------------|------|
| Button height           | 50dp |
| Top bar height          | 52dp (slimmer than Material default 64dp) |
| Logo tile               | 72×72dp |
| Avatar circle           | 72dp |
| Icon chip (orange tint) | 36×36dp |
| Field leading icon      | 20dp |
| Nav icon                | default (~24dp) |
| Top-bar bus icon        | 18dp |
| Outlined button border  | 1.5dp |
| Progress bar segment    | 4dp tall, 4dp gap |

---

## 4. Core components (how each one looks)

### 4.1 Top bar (`GyrTopBar`)
- Background: `TopBarNavy` `#0C1E42`, height **52dp**.
- Centered: orange bus icon (18dp) + spacer (6dp) + "GetYourRide" text (15sp, Medium, white).
- Left: optional white back arrow; otherwise a 48dp spacer to keep title centered.
- Right: optional step label (11sp, SemiBold, white @ 80% alpha) e.g. "STEP 1 OF 3"; otherwise 48dp spacer.
- **Step progress bar** (`GyrStepProgressBar`): sits below top bar on navy; N equal
  segments (4dp tall, 2dp radius, 4dp gap). Completed segments = orange; remaining = white @ 30% alpha.

**Web:** a fixed/sticky header, navy `#0C1E42`, centered logo lockup, white icons/text.

### 4.2 Bottom navigation (`GyrBottomNav`) → maps to web **top nav / tab bar**
- Background: white, elevation 8dp.
- Tabs: Home, Rides, Track, Profile (icons filled when active, outlined when inactive).
- **Active:** icon + label **orange** `#F57C00`, with a subtle orange pill behind the
  active icon (orange @ 10% alpha).
- **Inactive:** icon + label grey `IconTint` `#757575`.
- Label 11sp. Rides tab can show an **orange badge** with unread count (white text, 9sp, "9+" cap).

**Web:** render as a top navbar or a left sidebar; keep orange = active, grey = inactive, and the badge style.

### 4.3 Buttons
**Primary (CTA):**
- Fill: `OrangeAccent` `#F57C00`; text white, 16sp SemiBold.
- Shape: 10dp radius; height 50dp; full width in forms.
- Disabled: orange @ 50% alpha.
- Loading: white circular spinner (20dp, 2dp stroke) replaces label.

```css
.btn-primary{
  background:var(--gyr-orange); color:#fff;
  font-size:16px; font-weight:600;
  height:50px; border-radius:10px; border:0; width:100%;
}
.btn-primary:disabled{ background:rgba(245,124,0,.5); }
```

**Secondary (outlined):**
- Transparent fill, **1.5px navy border**, navy text, 16sp SemiBold, 10dp radius, 50dp tall.

```css
.btn-secondary{
  background:transparent; color:var(--gyr-navy);
  border:1.5px solid var(--gyr-navy);
  font-size:16px; font-weight:600;
  height:50px; border-radius:10px; width:100%;
}
```

### 4.4 Text input (`GyrTextField` + `gyrOutlinedTextFieldColors`)
- **Label above field:** UPPERCASE, 11sp, SemiBold, navy, 0.5sp letter-spacing, 4dp gap to field.
- Outlined input, 10dp radius, single line.
- Leading icon (20dp) tinted grey `IconTint`.
- Placeholder: `TextHint` `#9E9E9E`, 14sp.
- Border: focused = navy `NavyPrimary`; unfocused = `BorderLight` `#E0E0E0`.
- Container: focused = white `CardWhite`; unfocused = `SurfaceGrey` `#F5F6FA`.
- Cursor + typed text: navy.

```css
.field-label{
  text-transform:uppercase; font-size:11px; font-weight:600;
  letter-spacing:.045em; color:var(--gyr-navy); margin-bottom:4px;
}
.field-input{
  border:1px solid var(--gyr-border); border-radius:10px;
  background:var(--gyr-bg); color:var(--gyr-text); padding:12px 14px;
}
.field-input:focus{ border-color:var(--gyr-navy); background:#fff; outline:none; }
.field-input::placeholder{ color:var(--gyr-text-hint); }
```

### 4.5 Cards
- Background white, radius 12dp (forms) / 16dp (content), elevation 2–4dp.
- Form card inner padding 20dp; vertical gap between children 16dp.
- Content cards contain **rows** separated by a 1dp divider colored `#F5F6FA` (same as page bg → a hairline gap look).

### 4.6 Info row (`InfoCard`)
- Each row: left **icon chip** = 36dp square, 10dp radius, orange @ 12% alpha background, orange icon (18dp).
- Then 12dp gap, then a two-line stack: sub-label (11sp, Medium, `#8A8FA3`) over value (14sp, Medium, navy).
- Row padding 16dp horizontal, 14dp vertical.

### 4.7 Settings row (`SettingsListCard`)
- Orange icon (20dp) + 14dp gap + **orange label** (14sp, Medium) + trailing grey chevron (18dp, `#8A8FA3`).
- Row padding 16dp H / 14dp V; rows divided by 1dp `#F5F6FA` line; card radius 16dp, elevation 2dp.

### 4.8 Profile header (`ProfileHeader`)
- Full-width **navy** `#1A2E5A` block, centered content, top padding 48dp / bottom 56dp.
- Orange circle avatar (72dp) with white bold initials (24sp).
- Name: white, 18sp SemiBold. Student number: white @ 70% alpha, 13sp.
- **Funding badge** (pill, 20dp radius, color @ 18% alpha bg):
  - NSFAS Funded → green `#2E7D32`.
  - Self-Funded → orange `#F57C00`.
  - Text 11sp SemiBold in the matching color.

### 4.9 "OR" divider (`OrDivider`)
- Horizontal line (`BorderLight`) — "OR" (12sp, muted navy, 12dp horizontal padding) — horizontal line.

### 4.10 Radio option (`NsfasRadioOption`)
- Radio with selected color = navy. Label 14sp; navy when selected, muted navy when not.

---

## 5. Example layout — Login screen (reference composition)

Use this as the canonical example of "what good looks like." A matching web
login page should reproduce this structure:

1. Page background `#F5F6FA`, content centered, 24px horizontal padding, scrollable.
2. 56px top spacer → **72px navy logo tile** (16px radius) with white bus icon (40px).
3. 16px gap → "GET YOUR RIDE" (22px, bold, letter-spaced, navy).
4. 4px gap → tagline "Reliable campus mobility for everyone." (13px, muted navy, centered).
5. 32px gap → **white card** (12px radius, soft shadow, 20px padding, 16px gaps):
   - Email field (UPPERCASE label + outlined input + mail icon).
   - Password field with "Forgot Password?" link (orange, 12px) on the label row, and a show/hide eye toggle.
   - Optional error text (13px, red).
   - **Primary orange "Login" button** (full width, 50px, 10px radius).
   - "OR" divider.
   - **Outlined navy "Create Account" button**.
6. "Want to earn while you drive? **Become a Driver**" (14px; the link part navy, bold, underlined).
7. Legal footer: Terms of Service / Privacy Policy links in navy, semibold, underlined; rest muted navy, 12px.

---

## 6. Consistency checklist for the website

When reviewing the website against the app, confirm:

- [ ] Page backgrounds are `#F5F6FA`, not pure white.
- [ ] All primary action buttons are **orange** `#F57C00`, white text, 10px radius, ~50px tall.
- [ ] Secondary buttons are **outlined navy** (1.5px), navy text, transparent fill.
- [ ] Headings, body text, and outlines use navy `#1A2E5A` (never purple).
- [ ] Form field labels are **UPPERCASE**, 11px, semibold, navy, letter-spaced.
- [ ] Inputs are outlined, 10px radius, grey border unfocused → navy border focused.
- [ ] Cards are white, 12–16px radius, with a soft shadow (not hard borders).
- [ ] The top/nav uses navy; active nav items are orange, inactive are grey `#757575`.
- [ ] Status colors match: pending `#FFC107`, success/active green `#2E7D32`, cancelled/error red `#C62828`, completed grey `#757575`.
- [ ] Font is a clean system sans (Inter/Roboto/system-ui).
- [ ] Icon chips use orange @ ~12% alpha backgrounds with orange icons.

---

## 7. Source map (where these values live in the app)

| Concern                       | File |
|-------------------------------|------|
| Brand colors, text, status, neutrals | `app/.../ui/theme/GetYourRideColors.kt` |
| Base typography               | `app/.../ui/theme/Type.kt` |
| Theme wiring (⚠ template purple, ignore for brand) | `app/.../ui/theme/Theme.kt`, `Color.kt` |
| Top bar + step progress       | `app/.../ui/components/GyrTopBar.kt` |
| Bottom navigation + scaffold  | `app/.../ui/components/GyrBottomNav.kt` |
| Inputs, OR divider, radio     | `app/.../ui/components/GyrComponents.kt` |
| Info / settings cards         | `app/.../ui/components/InfoCard.kt`, `SettingslistCard.kt` |
| Profile header + badges       | `app/.../ui/components/ProfileHeader.kt` |
| Reference screen composition  | `app/.../ui/screens/LoginScreen.kt` |
