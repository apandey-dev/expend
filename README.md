# 💰 Expend - Modern Expense Tracker (Android Only)

**Expend** is a sleek, professional, Android-only expense tracker built from the ground up using **Kotlin** and **Jetpack Compose**. It features an **AMOLED-native pitch black dark theme**, an eye-friendly **day theme with zero pure `#FFFFFF` white**, typography powered by Google's **Mali font**, and a modern **5-tab bottom navigation bar** where only the active tab reveals its label.

---

## ✨ Features Implemented in Phase 1
- **Jetpack Compose + Material3:** 100% declarative modern Android UI.
- **Custom Bottom Navigation Bar:**
  - 5 key expense-tracking destinations: **Dashboard**, **Expenses**, **Analytics**, **Budgets**, and **Settings**.
  - **Active Tab Label Rule:** Only the active tab expands to show its title; inactive tabs display only their icon.
  - Smooth spring animations & glowing accent indicators.
- **AMOLED-Friendly Pitch Black Theme:** True `#000000` dark theme with high-contrast emerald/teal accents.
- **Soft Daylight Theme (Zero Pure White):** No `#FFFFFF` anywhere. Soft cool-tinted daylight base `#EFF2F5` and elevated card surfaces `#E2E7ED` for maximum eye comfort.
- **Google Mali Typography:** Configured in `app/src/main/res/font/` across Regular, Medium, SemiBold, and Bold weights.
- **5 Placeholder Screens:** Polished empty states with badges, subtitles, and interactive theme switcher in Settings.
- **Custom Icons Specification:** Comprehensive guide in [`ICONS.md`](./ICONS.md) for custom SVGs.

---

## 🏗️ Architecture & Project Structure
```
expend/
├── ICONS.md                            # Comprehensive icons naming & spec guide
├── app/
│   ├── build.gradle.kts
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── res/
│       │   ├── font/                   # Mali font family (.ttf)
│       │   ├── drawable/               # High-res vector icons
│       │   └── values/
│       └── java/com/expend/tracker/
│           ├── MainActivity.kt         # Edge-to-edge entry point
│           └── ui/
│               ├── theme/              # Color, Type (Mali), Theme
│               ├── navigation/         # NavItem, ExpendBottomNavBar
│               └── screens/            # 5 tab screens + MainScaffold
```

---

## 🚀 Incremental Roadmap
- [x] **Phase 1: Project foundation & Navigation**
  - Git repository initialization
  - Mali font integration
  - AMOLED + Zero-pure-white Soft Day theme
  - 5-tab icon-based bottom navigation bar with active-only label behavior
  - Wireless debugging deployment
- [ ] **Phase 2: Database & Domain Layer**
  - Room Database with SQLite for offline-first privacy
  - Expense & Income entity, Category, Wallet accounts
- [ ] **Phase 3: Transaction Entry & Quick Add**
  - Custom numeric keypad sheet
  - Category selector with custom SVGs
- [ ] **Phase 4: Analytics & Visual Charts**
  - Donut spend charts, weekly/monthly spending trends
- [ ] **Phase 5: Budget Limits, Alerts & Export**
