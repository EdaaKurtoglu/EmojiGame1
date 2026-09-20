package com.example.data.model

enum class GameMode(
    val title: String,
    val description: String,
    val icon: String
) {
    KLASIK(
        title = "Klasik Oyun",
        description = "3 canla başla, serileri yakala ve en yüksek skora ulaş!",
        icon = "▶"
    ),
    GUNUN_SARKISI(
        title = "Günün Şarkısı",
        description = "Her gün yenilenen gizemli şarkıyı tahmin et ve bonus kazan!",
        icon = "🔥"
    ),
    ZAMANA_KARSI(
        title = "Zamana Karşı",
        description = "60 saniyede bilebildiğin kadar çok şarkı bil!",
        icon = "⏱️"
    ),
    SERBEST_OYUN(
        title = "Serbest Oyun",
        description = "Can kaygısı olmadan dilediğin kategoride rahatça oyna.",
        icon = "🎧"
    )
}
