<a href="https://github.com/apandey-dev/expend"><img src="docs/media/Expend%20App%20Expense%20Tracker%20Promo.png" width="100%" alt="Expend - Personal Expense Tracker"></a>

<p align="center">
  <img src="docs/media/expend-mark.svg" alt="Expend" width="84" height="84">
</p>

<h1 align="center">Expend</h1>

<p align="center">
  <strong>A quiet, offline-first personal expense tracker.</strong><br>
  Built with Kotlin and Jetpack Compose. Your money data stays strictly on your device.
</p>

<p align="center">
  <a href="https://github.com/apandey-dev/expend/releases/tag/v1.0.0"><img src="https://img.shields.io/badge/Release-v1.0.0-b45f3c?style=flat-square" alt="Expend v1.0.0"></a>
  <a href="https://kotlinlang.org/"><img src="https://img.shields.io/badge/Kotlin_·_Compose-1a1a1a?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin and Jetpack Compose"></a>
  <a href="https://developer.android.com/training/data-storage/room"><img src="https://img.shields.io/badge/Room_·_SQLite-1a1a1a?style=flat-square&logo=sqlite&logoColor=white" alt="Room SQLite"></a>
  <img src="https://img.shields.io/badge/Android_8.0+-1a1a1a?style=flat-square&logo=android&logoColor=white" alt="Android 8.0 and up">
  <img src="https://img.shields.io/badge/Paise_·_Integer_Units-1a1a1a?style=flat-square" alt="Integer Minor Units">
  <a href="LICENSE"><img src="https://img.shields.io/badge/MIT-1a1a1a?style=flat-square" alt="MIT License"></a>
</p>

<p align="center">
  <a href="UI_SHOWCASE.md"><strong>🎨 View Full UI Showcase (30+ Screens)</strong></a> &nbsp;·&nbsp;
  <a href="https://github.com/apandey-dev/expend/releases/latest">Download APK</a> &nbsp;·&nbsp;
  <a href="#how-it-fits-together">Architecture</a> &nbsp;·&nbsp;
  <a href="#getting-started">Run it</a>
</p>

<p align="center"><sub>Monochrome by default. One accent, spent on purpose. 100% offline. Local Room database.</sub></p>

---

## Visual Preview

| AMOLED Night Theme | Clean Day Theme |
|:---:|:---:|
| <a href="UI_SHOWCASE.md"><img src="docs/media/screen-dark-home.jpg" width="340" alt="AMOLED Night Home Screen"></a> | <a href="UI_SHOWCASE.md"><img src="docs/media/screen-light-home.jpg" width="340" alt="Clean Day Home Screen"></a> |
| **AMOLED Night:** Pure black background with charcoal container | **Clean Day:** Paper white with dark contrast balance tile |

👉 **[Explore the Complete UI Showcase →](UI_SHOWCASE.md)** *(includes 30+ screenshots of Wallets, Credit Cards, 40-Icon Picker, Categories, and Custom Sheets)*

---

Bank accounts, credit cards, and cash scatter across different banking apps,
SMS notifications, and statement PDFs. Home opens on your total net available
balance, with monthly income and expenses at a glance — then you log an expense
in two taps, on pure dark charcoal or paper white, with the noise drained out.

## What it does

<p align="center">
  <img src="docs/media/expend-flow.png" width="100%" alt="Expend Architecture Flow: Sources (Bank Accounts, Credit Cards, Cash) log transactions into the Core Engine (Room SQLite Database, Integer Minor Units in paise, and Settlement Engine), projecting reactively to your phone (Jetpack Compose, 3 distinct transaction experiences, 100% offline)">
</p>

- **Your money data never leaves the phone.** Zero network permissions, no cloud
  accounts, no tracking SDKs, and no telemetry. Everything is persisted locally
  in SQLite via Room using integer minor units (paise) to eliminate floating-point
  rounding errors entirely.
- **Customizable multi-wallet architecture.** Users can create and configure as
  many wallets as needed to reflect their exact financial life — physical cash,
  daily bank accounts, savings reserves, digital wallets (UPI), business funds, or
  custom accounts. Each wallet supports custom naming, starting balances, notes,
  visual stamp archetypes, an optional starting balance income inclusion setting,
  and archiving when accounts are closed.
- **True credit card accounting.** Rather than treating credit cards as regular
  accounts, Expend tracks them as liabilities. Dual-section cards show live outstanding
  debt, total credit limits, and available spending headroom. Card bill settlements
  from bank accounts reduce debt without double-counting monthly expenses.
- **Three dedicated transaction experiences.** Normal Transactions preserves
  the bottom navigation bar; tapping *View All* from Home opens a dedicated, full-screen
  **All Transactions** destination; selecting a wallet opens a dedicated, filtered
  wallet transaction view.
- **Context-aware templates.** Create Transaction filters templates strictly according
  to the active transaction type: Expense transactions show only Expense templates,
  Income transactions show only Income templates, and Transfers keep templates cleanly hidden.
- **Custom 12-hour stepper time picker.** A bespoke bottom sheet built for thumbs:
  instant hour and minute adjustments with wraparound (12 ↔ 1 and 59 ↔ 0), AM/PM toggle,
  live header preview, and balanced 50/50 confirmation actions.
- **Subtle pastel category badges.** Create custom categories for any expense or income
  need. Each category features soft pastel circular backgrounds with monochrome vector
  icons (selectable from a 40-icon built-in catalog), making categories instantly recognizable
  while preserving Expend's quiet visual identity.
- **Full-screen entity workflows.** Bottom sheets are reserved strictly for selection,
  details, and lightweight actions. Creating or editing wallets, categories, templates,
  and transactions always navigates to dedicated full-screen pages with proper state restoration.
- **Versioned JSON backups (v5).** Export and import your entire financial database
  as schema-versioned, human-readable JSON files. Backward-compatible with all earlier
  migration formats.

## Getting started

> Built with modern Android standards: Kotlin 2.0+, Jetpack Compose Material 3,
> and Room SQLite.

```bash
git clone https://github.com/apandey-dev/expend.git && cd expend
./gradlew installDebug      # ~5 s once warm
```

That's the whole setup — no API keys, no Firebase setup, and no cloud tokens.

| | |
|---|---|
| **JDK** | 17 or newer |
| **Android** | 8.0 and up (`minSdk` 26, `compileSdk` / `targetSdk` 35) |
| **Gradle** | 8.10.2, via the wrapper |
| **Database** | Room SQLite 2.6.1 with automated schema migrations (v5) |
| **Units** | Minor currency units (`paise: Long`), preventing rounding errors |

## How it fits together

Expend is organized around a clean unidirectional data flow:

```
[UI / Jetpack Compose] 
       ▲  │ User Actions
       │  ▼
[MVI / ViewModels]
       ▲  │ Repository Calls
       │  ▼
[ExpendRepository]
       ▲  │ Queries & Inserts
       │  ▼
[Room SQLite DB (v5)]
```

- **Domain & Persistence:** Room entities (`TransactionEntity`, `WalletEntity`, `CategoryEntity`,
  `TemplateEntity`) store all monetary amounts in integer `paise` (`Long`). Migrations
  are strictly versioned and tested (`MIGRATION_4_5` handles credit limit and opening outstanding fields).
- **Financial Calculations:** Credit card balances are defined as `-outstandingPaise` with
  available credit computed as `creditLimitPaise - outstandingPaise`. Settlement transfers
  reduce outstanding balance without double-counting expenses in monthly summaries.
- **Presentation:** Built 100% in Jetpack Compose Material 3 with Mali typography, custom
  bottom sheets, and edge-to-edge layouts.

### Running unit tests

Every financial rule, balance formula, template filter, and backup serializer is
verified by unit tests:

```bash
./gradlew testDebugUnitTest
```

All 25 test tasks pass cleanly.

## Release Downloads

Grab the latest verified builds directly from the GitHub Releases page:

* **[Download Expend v1.0.0](https://github.com/apandey-dev/expend/releases/tag/v1.0.0)**
  * `expend-v1.0.0-release.aab` (Google Play bundle)
  * `expend-v1.0.0-release-unsigned.apk` (Release APK)
  * `expend-v1.0.0-debug.apk` (Direct testable sideload APK)

## Licence

MIT — see [LICENSE](LICENSE).

<p align="center">
  <sub>A personal finance project by <a href="https://github.com/apandey-dev">apandey-dev</a></sub><br>
  <sub>Built with Kotlin &amp; Jetpack Compose &bull; V1 Finalized &bull; 2026</sub>
</p>
