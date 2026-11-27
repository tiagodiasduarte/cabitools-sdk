package pt.tiagodiasduarte.cabitools.sdk.infrastructure.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * The SDK's HTTP logs database. Logs expire within a week at most, so schema changes drop the
 * tables instead of migrating them.
 */
@Database(entities = [HttpLogEntity::class, HttpLogBodiesEntity::class], version = 1, exportSchema = true)
@TypeConverters(HeadersConverter::class)
internal abstract class HttpLogsDatabase : RoomDatabase() {

    abstract fun httpLogsDao(): HttpLogsDao

    companion object {
        const val NAME = "cabitools_http_logs.db"

        /** The database's name before the SDK was renamed to Cabitools. Deleted so it doesn't stay on devices. */
        const val LEGACY_NAME = "appsettings_http_logs.db"

        fun create(context: Context): HttpLogsDatabase {
            val appContext = context.applicationContext
            // Returns false when the file doesn't exist, so it's safe on every start.
            appContext.deleteDatabase(LEGACY_NAME)
            return Room.databaseBuilder(appContext, HttpLogsDatabase::class.java, NAME)
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
        }
    }
}
