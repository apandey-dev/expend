# Expend v1.0.0 — Quiet, Offline-First Personal Expense Tracker

We are proud to announce the **V1.0.0 finalized release** of **Expend** for Android.

Expend is an offline-first, quiet personal finance tracker designed to help you track credit cards, multi-wallet balances, and recurring expenses without third-party tracking, ads, or cloud lock-in.

---

### Highlights in V1.0.0

* **True Credit Card Accounting:**
  * Dual-section credit card cards with dark charcoal header and live debt tracking.
  * Displays outstanding balance, total credit limit, and available spending headroom.
  * Bill settlement transfers from bank accounts reduce debt without double-counting monthly expenses.
  * Initial balance income inclusion option.

* **Normal Wallet Cards & 7 Wallet Types:**
  * Clean, minimal cards featuring wallet type glyph, balance summary, and full ledger history.
  * Supports Cash, Bank Account, Digital Wallet, Credit Card, Savings, Business, and Other.

* **Context-Aware Templates:**
  * Template selection filters strictly by transaction type (Expense templates for expenses, Income templates for earnings).
  * Seamless one-tap transaction prefilling.

* **Custom 12-Hour Stepper Time Picker:**
  * Custom `ModalBottomSheet` with independent date and time controls.
  * Steppers with wrap-around logic (12 ↔ 1 and 59 ↔ 0) and AM/PM segmented toggle.
  * 50/50 confirmation actions.

* **Subtle Pastel Category Badges:**
  * Categories feature soft pastel circular backgrounds behind monochrome vector icons.
  * Instant visual distinction without breaking Expend's minimal aesthetic.

* **Three Dedicated Transaction Experiences:**
  * **Normal Transactions:** Complete ledger with bottom navigation visible.
  * **All Transactions:** Standalone full-screen destination accessed from Home *View All*.
  * **Wallet Transactions:** Contextual filtered view with hidden bottom navigation.

* **Financial Engine & Room Database (v5 Schema):**
  * All currency math computed in integer minor units (`paise: Long`), eliminating floating-point rounding errors.
  * Room SQLite database with automated, verified migrations (`MIGRATION_4_5`).
  * 100% offline — zero network permissions in manifest.

* **Schema-Versioned JSON Backups (v5):**
  * Export and import complete ledgers in human-readable JSON.
  * Backward-compatible with all earlier database schema versions.

---

### Release Assets

* **`expend-v1.0.0-release.aab`** (11.3 MB) — Android App Bundle for Google Play distribution.
* **`expend-v1.0.0-release-unsigned.apk`** (11.6 MB) — Release-optimized APK (unsigned; requires personal signing key or Play App Signing).
* **`expend-v1.0.0-debug.apk`** (21.8 MB) — Pre-signed with Android debug key for instant direct sideloading and testing on Android 8.0+ devices.
