package com.example.data.model

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val target: Int,
    val currentProgress: Int,
    val isUnlocked: Boolean,
    val rewardCoins: Int
)
