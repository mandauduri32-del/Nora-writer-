package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.BookDao
import com.example.data.dao.ChapterDao
import com.example.data.dao.DailyWritingLogDao
import com.example.data.dao.DocumentVersionDao
import com.example.data.dao.QuickNoteDao
import com.example.data.dao.StoryElementDao
import com.example.data.model.BookEntity
import com.example.data.model.ChapterEntity
import com.example.data.model.DailyWritingLogEntity
import com.example.data.model.DocumentVersionEntity
import com.example.data.model.QuickNoteEntity
import com.example.data.model.StoryElementEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Database(
    entities = [
        BookEntity::class,
        ChapterEntity::class,
        QuickNoteEntity::class,
        StoryElementEntity::class,
        DocumentVersionEntity::class,
        DailyWritingLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun chapterDao(): ChapterDao
    abstract fun quickNoteDao(): QuickNoteDao
    abstract fun storyElementDao(): StoryElementDao
    abstract fun documentVersionDao(): DocumentVersionDao
    abstract fun dailyWritingLogDao(): DailyWritingLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "novel_desk_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val bookDao = database.bookDao()
            val chapterDao = database.chapterDao()
            val quickNoteDao = database.quickNoteDao()
            val storyElementDao = database.storyElementDao()
            val dailyLogDao = database.dailyWritingLogDao()
            val versionDao = database.documentVersionDao()

            val now = System.currentTimeMillis()

            // 1. Starter Book: Cahaya di Balik Cakrawala
            val bookId = bookDao.insertBook(
                BookEntity(
                    title = "Cahaya di Balik Cakrawala",
                    synopsis = "Di sebuah dunia di mana matahari terbit hanya setiap seabad sekali, seorang cendekiawan muda menemukan artefak yang meramalkan gerhana abadi.",
                    genre = "Fantasi Epik",
                    author = "Aria Wiratama",
                    status = "DRAFT",
                    coverColorHex = "#1E293B",
                    targetWordCount = 60000,
                    isPinned = true,
                    isFavorite = true,
                    createdAt = now - 86400000L * 7,
                    updatedAt = now
                )
            )

            // Chapter 1
            val ch1Content = """# Bab 1: Menembus Kabut Abadi

Lembah Aethelgard diselimuti kabut tebal berwarna kelabu perak ketika lonceng kuil berdentang tiga belas kali. Angin pegunungan membawa aroma embun dan lumut basah, dingin menusuk hingga ke balik jubah wol tebal yang kukenakan.

"Kita tidak boleh terlambat, Elian," gumam Master Raymond, langkah kakinya mantap di atas jalan setapak berbatu cadas. Lentera perunggu di tangannya berayun perlahan, memancarkan lingkaran cahaya temaram yang nyaris ditelan kegelapan pagi.

Aku mempercepat langkah, menyusul sang guru. Di dalam tas kulitku, manuskrip kuno yang kami temukan di reruntuhan Menara Sunyi terasa berat, seolah menyimpan denyut kehidupan tersendiri. Simbol-simbol emas di sampulnya telah redup selama tiga ratus tahun, namun semalam—tepat saat purnama merah mencapai titik puncaknya—tinta arkaik itu kembali berpendar hangat.

"Apakah ramalan itu benar, Guru?" tanyaku lirih. "Tentang gerhana abadi yang akan menenggelamkan tujuh kerajaan ke dalam musim dingin tanpa akhir?"

Master Raymond menghentikan langkahnya di tubir jurang. Dari ketinggian ini, kami dapat melihat Menara Kristal di kejauhan memantulkan semburat pertama fajar yang rapuh.

"Kebenaran bukan sesuatu yang kita tunggu, Nak," jawabnya tenang, menoleh padaku dengan tatapan mata tuanya yang tajam. "Kebenaran adalah apa yang kita perjuangkan sebelum kegelapan merenggut harapan terakhir."

Kabut di hadapan kami perlahan terkuak, menampakkan gerbang batu berpahat ukiran naga kembar. Perjalanan sesungguhnya baru saja dimulai.
""".trimIndent()

            val ch1Words = ch1Content.split(Regex("\\s+")).filter { it.isNotBlank() }.size
            val ch1Id = chapterDao.insertChapter(
                ChapterEntity(
                    bookId = bookId,
                    title = "Bab 1: Menembus Kabut Abadi",
                    chapterNumber = 1,
                    content = ch1Content,
                    wordCount = ch1Words,
                    charCount = ch1Content.length,
                    orderIndex = 1,
                    createdAt = now - 86400000L * 5,
                    updatedAt = now - 86400000L * 2
                )
            )

            // Initial version snapshot for Chapter 1
            versionDao.insertVersion(
                DocumentVersionEntity(
                    chapterId = ch1Id,
                    title = "Bab 1: Menembus Kabut Abadi",
                    content = ch1Content,
                    wordCount = ch1Words,
                    summary = "Draf Awal Selesai",
                    timestamp = now - 86400000L * 2
                )
            )

            // Chapter 2
            val ch2Content = """# Bab 2: Pustaka Arkana

Ruang bawah tanah Pustaka Arkana memiliki keheningan yang megah. Rak-rak kayu oak setinggi sepuluh meter menjulang ke langit-langit berkubah tinggi, menyimpan ribuan gulungan perkamen yang telah bertahan melintasi pergantian zaman.

Aku membuka meja kerja kayu cendana di sudut ruangan. Cahaya kristal luminesen menggantung tenang di udara, memberikan penerangan hangat yang tidak merusak lembar-lembar naskah rapuh.

"Cari peta rasi bintang era Dinasti Solaris," perintah Master Raymond sambil memeriksa katalog katalog kuno.

Jemari tanganku bergetar saat menyentuh permukaan kertas papirus yang kasar. Di antara baris-baris puisi arkaik berbahasa Kaelith, ada sebuah diagram yang tersembunyi. Dua lingkaran saling bersinggungan di pusat cakrawala, dengan tulisan kecil di tepinya: *'Kunci kebangkitan tersimpan di balik jantung gunung es.'*

Tiba-tiba, suara langkah kaki terdengar dari koridor luar. Derit pintu besi yang berat menandakan bahwa kami tidak lagi sendirian.
""".trimIndent()

            val ch2Words = ch2Content.split(Regex("\\s+")).filter { it.isNotBlank() }.size
            val ch2Id = chapterDao.insertChapter(
                ChapterEntity(
                    bookId = bookId,
                    title = "Bab 2: Pustaka Arkana",
                    chapterNumber = 2,
                    content = ch2Content,
                    wordCount = ch2Words,
                    charCount = ch2Content.length,
                    orderIndex = 2,
                    createdAt = now - 86400000L * 3,
                    updatedAt = now - 86400000L
                )
            )

            // Chapter 3
            val ch3Content = """# Bab 3: Jejak Rahasia

Suara langkah itu semakin mendekat. Aku segera melipat peta rasi bintang dan menyelipkannya ke dalam saku jubah.

Seorang wanita muda bertudung sutra biru muncul dari balik rak buku filosofi. Di pinggangnya tersampir sebilah belati berhulu perak berukir lambang Pengawal Mahkota.

"Kalian melanggar dekret kerajaan," ucapnya dingin. Namun tatapannya tertuju pada manuskrip di atas meja kami, bukan pada kami. "Kecuali jika kalian juga mencari pecahan Prasasti Cahaya yang dicuri dari istana."

Master Raymond tersenyum tipis. "Maka tujuan kita sama, Kapten Lyra."
""".trimIndent()

            val ch3Words = ch3Content.split(Regex("\\s+")).filter { it.isNotBlank() }.size
            chapterDao.insertChapter(
                ChapterEntity(
                    bookId = bookId,
                    title = "Bab 3: Jejak Rahasia",
                    chapterNumber = 3,
                    content = ch3Content,
                    wordCount = ch3Words,
                    charCount = ch3Content.length,
                    orderIndex = 3,
                    createdAt = now - 86400000L,
                    updatedAt = now
                )
            )

            // Story Elements for the book
            storyElementDao.insertElement(
                StoryElementEntity(
                    bookId = bookId,
                    type = "CHARACTER",
                    name = "Elian Senja",
                    roleOrTag = "Protagonis",
                    description = "Cendekiawan muda magang di Pustaka Arkana, memiliki kemampuan membaca tulisan arkaik Kaelith.",
                    details = "Ciri: Memakai jubah kelabu berjahit benang tembaga, membawa tas kulit warisan ayahnya.",
                    orderIndex = 1
                )
            )

            storyElementDao.insertElement(
                StoryElementEntity(
                    bookId = bookId,
                    type = "CHARACTER",
                    name = "Master Raymond",
                    roleOrTag = "Mentor",
                    description = "Arsiparis senior penjaga rahasia Menara Sunyi, ahli astronomi purba.",
                    details = "Sifat tenang, penuh pertimbangan, memiliki masa lalu misterius di Dewan Kerajaan.",
                    orderIndex = 2
                )
            )

            storyElementDao.insertElement(
                StoryElementEntity(
                    bookId = bookId,
                    type = "LOCATION",
                    name = "Lembah Aethelgard",
                    roleOrTag = "Lokasi Awal",
                    description = "Lembah berkabut abadi di kaki pegunungan es utara, tempat berdirinya Pustaka Arkana.",
                    details = "Suhu dingin sepanjang tahun, hanya dapat dicapai melalui jembatan gantung batu cadas.",
                    orderIndex = 1
                )
            )

            storyElementDao.insertElement(
                StoryElementEntity(
                    bookId = bookId,
                    type = "PLOT_EVENT",
                    name = "Penemuan Peta Rasi Bintang",
                    roleOrTag = "Babak I - Insiden Pemicu",
                    description = "Elian menemukan diagram rahasia di lembaran papirus Pustaka Arkana yang membuka koordinat pecahan Prasasti Cahaya.",
                    details = "Diikuti dengan pertemuan mendadak dengan Kapten Lyra dari Pengawal Mahkota.",
                    orderIndex = 1
                )
            )

            // Quick Notes
            val note1Content = """- Ide nama faksi rahasia: Ordo Obsidian
- Hubungan antara Elian dan Kapten Lyra: awalnya saling curiga, lalu terikat sumpah darah
- Twist di Bab 12: Ternyata gerhana bukan fenomena alam, tapi mesin pemusnah kuno yang diaktifkan oleh penguasa terdahulu!"""

            quickNoteDao.insertNote(
                QuickNoteEntity(
                    title = "Eksplorasi Plot & Twist Cerita",
                    content = note1Content,
                    category = "Ide Cerita",
                    isPinned = true,
                    wordCount = note1Content.split(Regex("\\s+")).filter { it.isNotBlank() }.size,
                    createdAt = now - 86400000L * 3,
                    updatedAt = now
                )
            )

            val note2Content = """Nama senjata: Belati Rimefrost
Bahan pembuatan: Baja meteorik dicelupkan dalam air mata gletser
Efek: Bilah selalu mengeluarkan kristal es tipis saat ditarik dari sarungnya."""

            quickNoteDao.insertNote(
                QuickNoteEntity(
                    title = "Detail Artefak & Senjata Magis",
                    content = note2Content,
                    category = "Riset",
                    isPinned = false,
                    wordCount = note2Content.split(Regex("\\s+")).filter { it.isNotBlank() }.size,
                    createdAt = now - 86400000L * 2,
                    updatedAt = now - 86400000L
                )
            )

            // Daily Writing Logs for past 7 days
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val sampleWords = listOf(420, 680, 850, 1200, 950, 1400, 750)
            val sampleDurations = listOf(25, 40, 50, 75, 55, 90, 45)

            for (i in 6 downTo 0) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, -i)
                val dateStr = sdf.format(cal.time)
                dailyLogDao.insertOrUpdateLog(
                    DailyWritingLogEntity(
                        dateStr = dateStr,
                        wordsWritten = sampleWords[6 - i],
                        targetWords = 1000,
                        writingTimeMinutes = sampleDurations[6 - i]
                    )
                )
            }
        }
    }
}
