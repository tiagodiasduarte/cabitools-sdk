package pt.tiagodiasduarte.cabitools.sdk.infrastructure.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HttpLogsDatabaseTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun `given a database from before the rename when the database is created then the old file is deleted`() =
        runTest {
            // Given
            val legacy = context.getDatabasePath(HttpLogsDatabase.LEGACY_NAME)
            legacy.parentFile?.mkdirs()
            SQLiteDatabase.openOrCreateDatabase(legacy, null).close()
            assertTrue(legacy.exists())

            // When
            val db = HttpLogsDatabase.create(context)

            // Then
            assertFalse(legacy.exists())
            assertEquals(emptyList<HttpLogSummary>(), db.httpLogsDao().observeSummaries(since = 0L).first())
            assertTrue(context.getDatabasePath(HttpLogsDatabase.NAME).exists())
            db.close()
        }

    @Test
    fun `given no database from before the rename when the database is created then it opens normally`() =
        runTest {
            // Given
            val legacy = context.getDatabasePath(HttpLogsDatabase.LEGACY_NAME)

            // When
            val db = HttpLogsDatabase.create(context)

            // Then
            assertFalse(legacy.exists())
            assertEquals(emptyList<HttpLogSummary>(), db.httpLogsDao().observeSummaries(since = 0L).first())
            db.close()
        }
}
