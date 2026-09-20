package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.PreferencesManager
import com.example.data.local.SongRepository
import com.example.game.AnswerValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Emojiyle Şarkıyı Bul", appName)
    }

    @Test
    fun `test answer validator exact and normalized matches`() {
        val acceptedAnswers = listOf("Şımarık", "Simarik")

        // Exact match
        assertTrue(AnswerValidator.isCorrect("Şımarık", acceptedAnswers, "Şımarık"))
        // Lowercase match
        assertTrue(AnswerValidator.isCorrect("şımarık", acceptedAnswers, "Şımarık"))
        // ASCII / without Turkish accents match
        assertTrue(AnswerValidator.isCorrect("simarik", acceptedAnswers, "Şımarık"))
        // Uppercase match
        assertTrue(AnswerValidator.isCorrect("ŞIMARIK", acceptedAnswers, "Şımarık"))
        // Extra spaces / punctuation
        assertTrue(AnswerValidator.isCorrect("  şımarık!  ", acceptedAnswers, "Şımarık"))
        // Typo tolerance (1 char off)
        assertTrue(AnswerValidator.isCorrect("sımarıkk", acceptedAnswers, "Şımarık"))

        // Wrong answer
        assertFalse(AnswerValidator.isCorrect("Dudu", acceptedAnswers, "Şımarık"))
    }

    @Test
    fun `test song repository loads bundled songs`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = SongRepository(context)

        val result = repository.loadSongs()
        assertTrue(result.isSuccess)

        val songs = repository.getAllSongs()
        assertTrue(songs.size >= 100)

        // Verify daily song is deterministic
        val date1 = Date(1773000000000L) // Fixed date
        val daily1 = repository.getDailySong(date1)
        val daily2 = repository.getDailySong(date1)
        assertNotNull(daily1)
        assertEquals(daily1?.id, daily2?.id)
    }

    @Test
    fun `test preferences manager tracking`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = PreferencesManager(context)

        prefs.resetAllProgress()
        val initialCoins = prefs.gameStats.value.coins
        assertEquals(500, initialCoins)

        // Record a correct answer
        prefs.recordCorrectAnswer(songId = 1, points = 200, coinsEarned = 30, newCombo = 3)
        val updated = prefs.gameStats.value
        assertEquals(530, updated.coins)
        assertEquals(200, updated.bestScore)
        assertEquals(3, updated.bestCombo)
        assertTrue(updated.solvedSongIds.contains(1))

        // Spend coins
        val spent = prefs.spendCoins(50)
        assertTrue(spent)
        assertEquals(480, prefs.gameStats.value.coins)
    }

    @Test
    fun `test songs have lyric snippets and emojis`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = SongRepository(context)
        repository.loadSongs()

        val songs = repository.getAllSongs()
        assertTrue(songs.isNotEmpty())
        for (song in songs) {
            assertTrue("Song ${song.title} should have at least 2 emojis", song.emojis.size >= 2)
            assertTrue("Song ${song.title} should have a non-empty lyricSnippet", song.lyricSnippet.isNotBlank())
        }
    }
}
