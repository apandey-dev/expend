package com.expend.tracker.data.models

import androidx.annotation.DrawableRes
import com.expend.tracker.R

data class CategoryIconItem(
    val id: String,
    val label: String,
    @DrawableRes val iconRes: Int
)

object CategoryIconRegistry {
    val DEFAULT_ICON_ID = "ic_cat_general"
    @DrawableRes val DEFAULT_ICON_RES = R.drawable.ic_cat_general

    val allIcons: List<CategoryIconItem> = listOf(
        CategoryIconItem("ic_cat_food", "Food & Dining", R.drawable.ic_cat_food),
        CategoryIconItem("ic_cat_groceries", "Groceries", R.drawable.ic_cat_groceries),
        CategoryIconItem("ic_cat_transport", "Transport", R.drawable.ic_cat_transport),
        CategoryIconItem("ic_cat_fuel", "Fuel", R.drawable.ic_cat_fuel),
        CategoryIconItem("ic_cat_shopping", "Shopping", R.drawable.ic_cat_shopping),
        CategoryIconItem("ic_cat_bills", "Bills & Utilities", R.drawable.ic_cat_bills),
        CategoryIconItem("ic_cat_entertainment", "Entertainment", R.drawable.ic_cat_entertainment),
        CategoryIconItem("ic_cat_health", "Health & Medical", R.drawable.ic_cat_health),
        CategoryIconItem("ic_cat_education", "Education", R.drawable.ic_cat_education),
        CategoryIconItem("ic_cat_salary", "Salary & Income", R.drawable.ic_cat_salary),
        CategoryIconItem("ic_cat_freelance", "Freelance & Work", R.drawable.ic_cat_freelance),
        CategoryIconItem("ic_cat_business", "Business", R.drawable.ic_cat_business),
        CategoryIconItem("ic_cat_investment", "Investments", R.drawable.ic_cat_investment),
        CategoryIconItem("ic_cat_gift", "Gifts", R.drawable.ic_cat_gift),
        CategoryIconItem("ic_cat_home", "Housing & Rent", R.drawable.ic_cat_home),
        CategoryIconItem("ic_cat_cafe", "Cafe & Snacks", R.drawable.ic_cat_cafe),
        CategoryIconItem("ic_cat_fastfood", "Fast Food", R.drawable.ic_cat_fastfood),
        CategoryIconItem("ic_cat_flight", "Flights & Travel", R.drawable.ic_cat_flight),
        CategoryIconItem("ic_cat_train", "Transit & Commute", R.drawable.ic_cat_train),
        CategoryIconItem("ic_cat_fitness", "Fitness & Gym", R.drawable.ic_cat_fitness),
        CategoryIconItem("ic_cat_personal_care", "Personal Care", R.drawable.ic_cat_personal_care),
        CategoryIconItem("ic_cat_electronics", "Electronics", R.drawable.ic_cat_electronics),
        CategoryIconItem("ic_cat_clothing", "Clothing", R.drawable.ic_cat_clothing),
        CategoryIconItem("ic_cat_subscription", "Subscriptions", R.drawable.ic_cat_subscription),
        CategoryIconItem("ic_cat_phone", "Phone & Internet", R.drawable.ic_cat_phone),
        CategoryIconItem("ic_cat_water", "Water Bill", R.drawable.ic_cat_water),
        CategoryIconItem("ic_cat_electricity", "Electricity", R.drawable.ic_cat_electricity),
        CategoryIconItem("ic_cat_maintenance", "Maintenance", R.drawable.ic_cat_maintenance),
        CategoryIconItem("ic_cat_pet", "Pets", R.drawable.ic_cat_pet),
        CategoryIconItem("ic_cat_insurance", "Insurance", R.drawable.ic_cat_insurance),
        CategoryIconItem("ic_cat_taxes", "Taxes", R.drawable.ic_cat_taxes),
        CategoryIconItem("ic_cat_donations", "Charity & Donations", R.drawable.ic_cat_donations),
        CategoryIconItem("ic_cat_bonus", "Bonus & Awards", R.drawable.ic_cat_bonus),
        CategoryIconItem("ic_cat_interest", "Interest & Dividends", R.drawable.ic_cat_interest),
        CategoryIconItem("ic_cat_refund", "Refunds", R.drawable.ic_cat_refund),
        CategoryIconItem("ic_cat_sale", "Sales & Assets", R.drawable.ic_cat_sale),
        CategoryIconItem("ic_cat_childcare", "Family & Kids", R.drawable.ic_cat_childcare),
        CategoryIconItem("ic_cat_books", "Books & Courses", R.drawable.ic_cat_books),
        CategoryIconItem("ic_cat_savings", "Savings", R.drawable.ic_cat_savings),
        CategoryIconItem("ic_cat_general", "General", R.drawable.ic_cat_general)
    )

    private val iconMap: Map<String, Int> = allIcons.associate { it.id to it.iconRes }

    /**
     * Safe resolution with fallback for missing or unknown icon IDs.
     */
    @DrawableRes
    fun getIconResource(iconId: String?): Int {
        if (iconId.isNullOrEmpty()) return DEFAULT_ICON_RES
        return iconMap[iconId] ?: DEFAULT_ICON_RES
    }

    /**
     * Guess default icon from category name if not specified.
     */
    fun defaultIconForName(name: String): String {
        val lower = name.lowercase()
        return when {
            "food" in lower || "dining" in lower || "restaurant" in lower -> "ic_cat_food"
            "grocer" in lower || "supermarket" in lower -> "ic_cat_groceries"
            "transport" in lower || "cab" in lower || "taxi" in lower || "uber" in lower -> "ic_cat_transport"
            "fuel" in lower || "petrol" in lower || "diesel" in lower || "gas" in lower -> "ic_cat_fuel"
            "shop" in lower || "market" in lower -> "ic_cat_shopping"
            "bill" in lower || "utilit" in lower -> "ic_cat_bills"
            "entertain" in lower || "movie" in lower || "game" in lower -> "ic_cat_entertainment"
            "health" in lower || "doctor" in lower || "medic" in lower || "pharmacy" in lower -> "ic_cat_health"
            "educat" in lower || "school" in lower || "college" in lower || "course" in lower -> "ic_cat_education"
            "salar" in lower || "paycheck" in lower -> "ic_cat_salary"
            "freelance" in lower || "contract" in lower -> "ic_cat_freelance"
            "business" in lower -> "ic_cat_business"
            "invest" in lower || "stock" in lower || "mutual" in lower -> "ic_cat_investment"
            "gift" in lower -> "ic_cat_gift"
            "rent" in lower || "house" in lower || "home" in lower -> "ic_cat_home"
            "cafe" in lower || "coffee" in lower || "tea" in lower -> "ic_cat_cafe"
            "fast" in lower || "pizza" in lower || "burger" in lower -> "ic_cat_fastfood"
            "flight" in lower || "travel" in lower || "trip" in lower -> "ic_cat_flight"
            "train" in lower || "metro" in lower || "bus" in lower -> "ic_cat_train"
            "gym" in lower || "fitness" in lower || "workout" in lower -> "ic_cat_fitness"
            "cloth" in lower || "apparel" in lower || "wear" in lower -> "ic_cat_clothing"
            "subscri" in lower || "netflix" in lower || "prime" in lower || "spotify" in lower -> "ic_cat_subscription"
            "phone" in lower || "recharge" in lower || "wifi" in lower || "internet" in lower -> "ic_cat_phone"
            "water" in lower -> "ic_cat_water"
            "electr" in lower || "power" in lower -> "ic_cat_electricity"
            "repair" in lower || "maintain" in lower -> "ic_cat_maintenance"
            "pet" in lower || "dog" in lower || "cat" in lower -> "ic_cat_pet"
            "insur" in lower -> "ic_cat_insurance"
            "tax" in lower -> "ic_cat_taxes"
            "donat" in lower || "charity" in lower -> "ic_cat_donations"
            "bonus" in lower || "reward" in lower -> "ic_cat_bonus"
            "interest" in lower || "dividend" in lower -> "ic_cat_interest"
            "refund" in lower -> "ic_cat_refund"
            "sale" in lower -> "ic_cat_sale"
            "child" in lower || "kid" in lower || "baby" in lower -> "ic_cat_childcare"
            "book" in lower -> "ic_cat_books"
            "saving" in lower -> "ic_cat_savings"
            else -> DEFAULT_ICON_ID
        }
    }
}
