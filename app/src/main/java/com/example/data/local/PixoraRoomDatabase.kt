package com.example.data.local

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class PixoraRoomDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context.applicationContext, "pixora_offline_favorites.db", null, 1) {

    private val allCachedState = MutableStateFlow<List<CachedFavoriteWallpaperEntity>>(emptyList())

    private val daoImpl = object : FavoriteWallpaperDao {
        override fun observeFavoritesForUser(userId: String): Flow<List<CachedFavoriteWallpaperEntity>> {
            return allCachedState.map { list ->
                list.filter { it.userId == userId }.sortedByDescending { it.createdAtMillis }
            }
        }

        override fun observeAllCachedFavorites(): Flow<List<CachedFavoriteWallpaperEntity>> {
            return allCachedState
        }

        override suspend fun upsertFavorite(entity: CachedFavoriteWallpaperEntity) {
            withContext(Dispatchers.IO) {
                writableDatabase.insertWithOnConflict(
                    TABLE_NAME,
                    null,
                    entity.toContentValues(),
                    SQLiteDatabase.CONFLICT_REPLACE
                )
                refreshStateFromDisk()
            }
        }

        override suspend fun upsertAllFavorites(entities: List<CachedFavoriteWallpaperEntity>) {
            if (entities.isEmpty()) return
            withContext(Dispatchers.IO) {
                val db = writableDatabase
                db.beginTransaction()
                try {
                    for (entity in entities) {
                        db.insertWithOnConflict(
                            TABLE_NAME,
                            null,
                            entity.toContentValues(),
                            SQLiteDatabase.CONFLICT_REPLACE
                        )
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
                refreshStateFromDisk()
            }
        }

        override suspend fun deleteFavorite(userId: String, wallpaperId: String) {
            withContext(Dispatchers.IO) {
                writableDatabase.delete(
                    TABLE_NAME,
                    "userId = ? AND wallpaperId = ?",
                    arrayOf(userId, wallpaperId)
                )
                refreshStateFromDisk()
            }
        }

        override suspend fun deleteByWallpaperId(wallpaperId: String) {
            withContext(Dispatchers.IO) {
                writableDatabase.delete(
                    TABLE_NAME,
                    "wallpaperId = ?",
                    arrayOf(wallpaperId)
                )
                refreshStateFromDisk()
            }
        }

        override suspend fun getCachedFavorite(
            userId: String,
            wallpaperId: String
        ): CachedFavoriteWallpaperEntity? = withContext(Dispatchers.IO) {
            readableDatabase.query(
                TABLE_NAME,
                null,
                "userId = ? AND wallpaperId = ?",
                arrayOf(userId, wallpaperId),
                null,
                null,
                "createdAtMillis DESC",
                "1"
            ).use { cursor ->
                if (cursor.moveToFirst()) cursor.toEntity() else null
            }
        }

        override suspend fun isFavoriteCached(userId: String, wallpaperId: String): Boolean =
            withContext(Dispatchers.IO) {
                getCachedFavorite(userId, wallpaperId) != null
            }

        override suspend fun clearFavoritesForUser(userId: String) {
            withContext(Dispatchers.IO) {
                writableDatabase.delete(
                    TABLE_NAME,
                    "userId = ?",
                    arrayOf(userId)
                )
                refreshStateFromDisk()
            }
        }
    }

    init {
        runCatching { refreshStateFromDisk() }
    }

    fun favoriteWallpaperDao(): FavoriteWallpaperDao = daoImpl

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                id TEXT PRIMARY KEY NOT NULL,
                userId TEXT NOT NULL,
                wallpaperId TEXT NOT NULL,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                category TEXT NOT NULL,
                tagsCsv TEXT NOT NULL,
                resolution TEXT NOT NULL,
                fileSize TEXT NOT NULL,
                fileSizeBytes INTEGER NOT NULL,
                mimeType TEXT NOT NULL,
                imageUrl TEXT NOT NULL,
                thumbnailUrl TEXT NOT NULL,
                localCachedImageUri TEXT NOT NULL,
                createdAtMillis INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_${TABLE_NAME}_userId ON $TABLE_NAME(userId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_${TABLE_NAME}_wallpaperId ON $TABLE_NAME(wallpaperId)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    private fun refreshStateFromDisk() {
        val loaded = mutableListOf<CachedFavoriteWallpaperEntity>()
        readableDatabase.query(
            TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            "createdAtMillis DESC"
        ).use { cursor ->
            while (cursor.moveToNext()) {
                loaded.add(cursor.toEntity())
            }
        }
        allCachedState.value = loaded
    }

    private fun CachedFavoriteWallpaperEntity.toContentValues(): ContentValues {
        return ContentValues().apply {
            put("id", id)
            put("userId", userId)
            put("wallpaperId", wallpaperId)
            put("title", title)
            put("description", description)
            put("category", category)
            put("tagsCsv", tagsCsv)
            put("resolution", resolution)
            put("fileSize", fileSize)
            put("fileSizeBytes", fileSizeBytes)
            put("mimeType", mimeType)
            put("imageUrl", imageUrl)
            put("thumbnailUrl", thumbnailUrl)
            put("localCachedImageUri", localCachedImageUri)
            put("createdAtMillis", createdAtMillis)
        }
    }

    private fun Cursor.toEntity(): CachedFavoriteWallpaperEntity {
        return CachedFavoriteWallpaperEntity(
            id = getString(getColumnIndexOrThrow("id")),
            userId = getString(getColumnIndexOrThrow("userId")),
            wallpaperId = getString(getColumnIndexOrThrow("wallpaperId")),
            title = getString(getColumnIndexOrThrow("title")),
            description = getString(getColumnIndexOrThrow("description")),
            category = getString(getColumnIndexOrThrow("category")),
            tagsCsv = getString(getColumnIndexOrThrow("tagsCsv")),
            resolution = getString(getColumnIndexOrThrow("resolution")),
            fileSize = getString(getColumnIndexOrThrow("fileSize")),
            fileSizeBytes = getLong(getColumnIndexOrThrow("fileSizeBytes")),
            mimeType = getString(getColumnIndexOrThrow("mimeType")),
            imageUrl = getString(getColumnIndexOrThrow("imageUrl")),
            thumbnailUrl = getString(getColumnIndexOrThrow("thumbnailUrl")),
            localCachedImageUri = getString(getColumnIndexOrThrow("localCachedImageUri")),
            createdAtMillis = getLong(getColumnIndexOrThrow("createdAtMillis"))
        )
    }

    companion object {
        private const val TABLE_NAME = "cached_favorite_wallpapers"

        @Volatile
        private var INSTANCE: PixoraRoomDatabase? = null

        fun getInstance(context: Context): PixoraRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = PixoraRoomDatabase(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
