package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.NaatLyricsDao
import com.example.data.local.NaatLyricsEntity
import com.example.data.local.OfflineNaatDao
import com.example.data.local.OfflineNaatEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class NaatLyricsRoomTest {

    private lateinit var db: AppDatabase
    private lateinit var lyricsDao: NaatLyricsDao
    private lateinit var offlineDao: OfflineNaatDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        lyricsDao = db.naatLyricsDao()
        offlineDao = db.offlineNaatDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testInsertAndRetrieveLyricsByAuthor() = runBlocking {
        val item = NaatLyricsEntity(
            id = "naat_1",
            title = "कोई तो है जो निज़ामे हस्ती चला रहा है",
            author = "मुज़फ़्फ़र वारसी",
            lyrics = "कोई तो है जो निज़ामे हस्ती चला रहा है, वही ख़ुदा है!",
            languageTags = "hi,gu,en",
            category = "Hamd Sharif"
        )
        lyricsDao.insert(item)

        val retrieved = lyricsDao.getLyricsById("naat_1").first()
        assertNotNull(retrieved)
        assertEquals("मुज़फ़्फ़र वारसी", retrieved?.author)
        assertEquals("hi,gu,en", retrieved?.languageTags)
        assertTrue(retrieved?.lyrics?.contains("निज़ामे हस्ती") == true)

        val byAuthor = lyricsDao.getLyricsByAuthor("मुज़फ़्फ़र").first()
        assertEquals(1, byAuthor.size)
        assertEquals("naat_1", byAuthor[0].id)
    }

    @Test
    fun testQueryByLanguageTag() = runBlocking {
        val item1 = NaatLyricsEntity(
            id = "naat_hi",
            title = "Title Hi",
            author = "Poet 1",
            lyrics = "Hindi Lyrics",
            languageTags = "hi"
        )
        val item2 = NaatLyricsEntity(
            id = "naat_multi",
            title = "Title Multi",
            author = "Poet 2",
            lyrics = "Multi Lyrics",
            languageTags = "hi,gu,en"
        )
        val item3 = NaatLyricsEntity(
            id = "naat_ur",
            title = "Title Urdu",
            author = "Poet 3",
            lyrics = "Urdu Lyrics",
            languageTags = "ur"
        )
        lyricsDao.insertAll(listOf(item1, item2, item3))

        val hiResults = lyricsDao.getLyricsByLanguageTag("hi").first()
        assertEquals(2, hiResults.size)

        val urResults = lyricsDao.getLyricsByLanguageTag("ur").first()
        assertEquals(1, urResults.size)
        assertEquals("naat_ur", urResults[0].id)
    }

    @Test
    fun testOfflineNaatDaoStorage() = runBlocking {
        val offlineItem = OfflineNaatEntity(
            id = "offline_1",
            title = "Offline Title",
            author = "Ahmed Raza",
            lyrics = "Mustafa Jane Rahmat Pe Lakhon Salam",
            languageTags = "hi,ur,en"
        )
        offlineDao.insert(offlineItem)

        val list = offlineDao.getAllOffline().first()
        assertEquals(1, list.size)
        assertEquals("Ahmed Raza", list[0].author)
        assertEquals("hi,ur,en", list[0].languageTags)
    }
}
