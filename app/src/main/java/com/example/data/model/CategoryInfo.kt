package com.example.data.model

data class CategoryInfo(
    val id: String,
    val titleHindi: String,
    val titleGujarati: String,
    val titleEnglish: String,
    val description: String = ""
)

object DefaultCategories {
    val ALL = listOf(
        CategoryInfo(
            id = "hamd",
            titleHindi = "हम्द शरीफ",
            titleGujarati = "હમ્દ શરીફ",
            titleEnglish = "Hamd Sharif",
            description = "Praise of Allah Almighty"
        ),
        CategoryInfo(
            id = "naat",
            titleHindi = "नात शरीफ",
            titleGujarati = "નાત શરીફ",
            titleEnglish = "Naat Sharif",
            description = "In praise of the Holy Prophet Muhammad ﷺ"
        ),
        CategoryInfo(
            id = "manqabat",
            titleHindi = "मनक़बत शरीफ",
            titleGujarati = "મનકબત શરીફ",
            titleEnglish = "Manqabat Sharif",
            description = "Praise of the Holy Ahle Bait & Companions"
        ),
        CategoryInfo(
            id = "panjtan",
            titleHindi = "पंजतन पाक",
            titleGujarati = "પંજતન પાક",
            titleEnglish = "Panjtan Pak",
            description = "Praise of the blessed Panjtan Pak"
        ),
        CategoryInfo(
            id = "gouse_azam",
            titleHindi = "गौसे आज़म",
            titleGujarati = "ગૌસે આઝમ",
            titleEnglish = "Gouse Azam",
            description = "Kalam of Huzoor Ghouse-e-Azam Dastagir"
        ),
        CategoryInfo(
            id = "gareeb_nawaz",
            titleHindi = "गरीब नवाज",
            titleGujarati = "ગરીબ નવાજ",
            titleEnglish = "Gareeb Nawaz",
            description = "Kalam of Sultan-ul-Hind Khwaja Gareeb Nawaz"
        ),
        CategoryInfo(
            id = "kalame_raza",
            titleHindi = "कलामे रज़ा",
            titleGujarati = "કલામે રઝા",
            titleEnglish = "Kalame Raza",
            description = "Spiritual poetry of Ala Hazrat Imam Ahmad Raza"
        ),
        CategoryInfo(
            id = "salami",
            titleHindi = "सलामी",
            titleGujarati = "સલામી",
            titleEnglish = "Salami",
            description = "Blessings and Salutations upon the Prophet ﷺ"
        )
    )

    fun findById(id: String): CategoryInfo? = ALL.find { it.id == id || it.titleHindi == id || it.titleEnglish.equals(id, ignoreCase = true) }
}
