package com.example.data.model

data class GameCategory(
    val id: String,
    val name: String,
    val icon: String,
    val description: String
) {
    companion object {
        val ALL = listOf(
            GameCategory("all", "Karışık", "🔥", "Tüm kategorilerden karışık şarkılar"),
            GameCategory("pop", "Türkçe Pop", "🇹🇷", "Tarkan, Sezen Aksu ve günümüz pop hitleri"),
            GameCategory("rap", "Türkçe Rap", "🎤", "Ceza, Sagopa, Ezhel, Motive ve ritimler"),
            GameCategory("rock", "Türkçe Rock", "🎸", "Barış Manço, Duman, Mor ve Ötesi, Şebnem Ferah"),
            GameCategory("ask", "Aşk Şarkıları", "💔", "En dokunaklı ve unutulmaz aşk melodileri"),
            GameCategory("90s", "90'lar", "🕺", "Unutulmaz 90'lar nostaljisi ve coşkusu"),
            GameCategory("2000s", "2000'ler", "📀", "Milenyumun efsanevi hitleri ve grupları"),
            GameCategory("yabanci", "Yabancı Şarkılar", "🌎", "Queen, Michael Jackson, Adele ve dünya hitleri"),
            GameCategory("film", "Film & Dizi Şarkıları", "🎬", "Ezel, Çukur, Selvi Boylum ve sinema klasikleri")
        )

        fun findByName(name: String): GameCategory {
            return ALL.find { it.name.equals(name, ignoreCase = true) }
                ?: ALL.first()
        }
    }
}
