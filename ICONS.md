# 🎨 Expend - Icons Guide & Specification

Ye document un sabhi icons ki list aur guidelines provide karta hai jo is app me use honge. Aap jo bhi SVG icons lake doge, unhe hum directly `app/src/main/res/drawable/` me place karenge ya Vector Asset format me convert kar lenge.

---

## 📐 SVG Technical Guidelines
Aap jo SVGs doge unka format best results ke liye aisa hona chahiye:
- **Canvas / ViewBox:** `0 0 24 24` (Standard 24x24 dp Android format)
- **Base Color:** Single flat color (prefer `#FFFFFF` ya `currentColor`). Compose automatic tinting handle kar lega (AMOLED mode me Emerald Green `#10B981` aur Day mode me Soft Teal `#0D9488`).
- **Style:** Clean, modern, 1.5px - 2px stroke ya filled minimal aesthetic.

---

## 1. 🧭 Bottom Navigation Icons (Core 5 Tabs)
Filhal inke liye built-in vector drawables configure kar diye gaye hain. Aap inke exact filenames se apne SVGs replace kar sakte ho:

| File Name | Screen | Description |
|-----------|--------|-------------|
| `ic_nav_dashboard.xml` | **Dashboard** | Overview / Home 4-square grid ya balance card icon |
| `ic_nav_transactions.xml` | **Expenses** | List / Receipt / Ledger / Credit-Debit transfer icon |
| `ic_nav_analytics.xml` | **Analytics** | Bar chart / Pie chart / Growth trend line icon |
| `ic_nav_budget.xml` | **Budgets** | Wallet / Target limit / Piggy bank icon |
| `ic_nav_settings.xml` | **Settings** | Gear / Sliders / Preferences icon |

---

## 2. ⚡ Core Action Icons (Next Increments)
Jab hum transactions add karne aur filter karne ke features banayenge:

| File Name | Usage | Description |
|-----------|-------|-------------|
| `ic_action_add.xml` | Quick Add | Plus (+) button inside floating action pill |
| `ic_action_filter.xml` | Transactions Filter | Filter funnel / sliders |
| `ic_action_search.xml` | Search Bar | Magnifying glass |
| `ic_action_calendar.xml` | Date Picker | Calendar date picker icon |
| `ic_action_delete.xml` | Delete Transaction | Trash bin icon |
| `ic_action_edit.xml` | Edit Record | Pencil / Edit note icon |
| `ic_action_back.xml` | Navigation | Arrow left / back chevron |
| `ic_action_close.xml` | Dialogs / Sheets | Cross (X) close icon |

---

## 3. 🏷️ Expense & Income Categories (Category Badges)
Future incremental updates me jab user expense category choose karega:

| Category Name | Recommended File Name | Icon Suggestion |
|---------------|-----------------------|-----------------|
| **Food & Dining** | `ic_cat_food.xml` | Fork and knife / Burger / Coffee cup |
| **Groceries** | `ic_cat_groceries.xml` | Shopping cart / Grocery bag |
| **Transport & Fuel** | `ic_cat_transport.xml` | Car / Bus / Metro / Fuel pump |
| **Shopping** | `ic_cat_shopping.xml` | Shopping bag / Tag |
| **Bills & Utilities** | `ic_cat_bills.xml` | Lightning / Receipt / Water drop |
| **Entertainment** | `ic_cat_entertainment.xml` | Movie ticket / Gamepad / Popcorn |
| **Health & Medical** | `ic_cat_health.xml` | Heartbeat pulse / First-aid cross |
| **Education** | `ic_cat_education.xml` | Graduation cap / Book |
| **Investments** | `ic_cat_investment.xml` | Stock chart / Trending up arrow |
| **Salary / Income** | `ic_cat_salary.xml` | Money stack / Downward arrow (Cash in) |
| **Other / Miscellaneous**| `ic_cat_other.xml` | Ellipsis / Star / Box |

---

## 📂 SVG Kaise Provide Karna Hai?
Jab aapke paas SVGs ready ho jayein:
1. Aap mujhe direct SVG ka raw code (`<svg ...>...</svg>`) chat me de sakte ho.
2. Ya fir SVG files ko workspace ke `app/src/main/res/drawable/` me drop kar sakte ho.
Hum unhe automatically Android Vector Drawable format me convert kar denge!
