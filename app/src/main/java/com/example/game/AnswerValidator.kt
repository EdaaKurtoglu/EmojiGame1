package com.example.game

import java.util.Locale

object AnswerValidator {

    private val TURKISH_LOCALE = Locale("tr", "TR")

    /**
     * Cleans and normalizes Turkish text:
     * - Preserves Turkish lowercase correctly ('İ' -> 'i', 'I' -> 'ı')
     * - Trims and collapses multiple spaces
     * - Removes punctuation and special symbols
     */
    fun normalize(input: String): String {
        return input.trim()
            .lowercase(TURKISH_LOCALE)
            .replace(Regex("[^\\p{L}\\p{Nd}\\s]"), "") // Keep letters and digits
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    /**
     * Converts characters with diacritics to their base ASCII equivalent
     * so that typing "sezen aksu" or "imkansiz" matches "imkansız" seamlessly.
     */
    fun foldToAscii(input: String): String {
        val normalized = normalize(input)
        val sb = StringBuilder(normalized.length)
        for (ch in normalized) {
            when (ch) {
                'ç' -> sb.append('c')
                'ğ' -> sb.append('g')
                'ı', 'i', 'İ', 'I' -> sb.append('i')
                'ö' -> sb.append('o')
                'ş' -> sb.append('s')
                'ü' -> sb.append('u')
                'â' -> sb.append('a')
                'î' -> sb.append('i')
                'û' -> sb.append('u')
                else -> sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Checks if user answer matches the accepted answers or song title.
     */
    fun isCorrect(userAnswer: String, acceptedAnswers: List<String>, title: String): Boolean {
        val userRaw = userAnswer.trim()
        if (userRaw.isBlank()) return false

        val userNorm = normalize(userRaw)
        val userFolded = foldToAscii(userRaw)
        val userNoSpaces = userFolded.replace(" ", "")

        val allCandidates = (acceptedAnswers + title).distinct()

        for (candidate in allCandidates) {
            val candNorm = normalize(candidate)
            val candFolded = foldToAscii(candidate)
            val candNoSpaces = candFolded.replace(" ", "")

            // 1. Direct normalized match
            if (userNorm == candNorm) return true

            // 2. ASCII-folded match (ignoring Turkish character differences like ı/i, ğ/g)
            if (userFolded == candFolded) return true

            // 3. Space-agnostic match (e.g. "Gülpembe" vs "Gül Pembe")
            if (userNoSpaces == candNoSpaces && userNoSpaces.isNotEmpty()) return true

            // 4. Minor typo tolerance for long song titles (1 edit distance for lengths >= 6)
            if (candFolded.length >= 6 && levenshteinDistance(userFolded, candFolded) <= 1) {
                return true
            }
        }

        return false
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j

        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[s1.length][s2.length]
    }
}
