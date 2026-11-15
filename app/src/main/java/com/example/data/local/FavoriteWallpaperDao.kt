package com.example.data.local

import kotlinx.coroutines.flow.Flow

interface FavoriteWallpaperDao {

    fun observeFavoritesForUser(userId: String): Flow<List<CachedFavoriteWallpaperEntity>>

    fun observeAllCachedFavorites(): Flow<List<CachedFavoriteWallpaperEntity>>

    suspend fun upsertFavorite(entity: CachedFavoriteWallpaperEntity)

    suspend fun upsertAllFavorites(entities: List<CachedFavoriteWallpaperEntity>)

    suspend fun deleteFavorite(userId: String, wallpaperId: String)

    suspend fun deleteByWallpaperId(wallpaperId: String)

    suspend fun getCachedFavorite(userId: String, wallpaperId: String): CachedFavoriteWallpaperEntity?

    suspend fun isFavoriteCached(userId: String, wallpaperId: String): Boolean

    suspend fun clearFavoritesForUser(userId: String)
}
