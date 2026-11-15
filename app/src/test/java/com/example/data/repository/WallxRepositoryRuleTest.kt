package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.WallpaperItem
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.robolectric.annotation.Config
import java.util.UUID

@Config(sdk = [36])
class WallxRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun ensureUserProfile_authenticatedUser_createsProfileAndBlocksCrossUserPiiRead() = runBlocking {
        val aliceEmail = "alice_${UUID.randomUUID().toString().take(8)}@test.com"
        val bobEmail = "bob_${UUID.randomUUID().toString().take(8)}@test.com"

        val aliceUid = signInTestUser(aliceEmail)
        val aliceRepo = WallxRepository(db = firestore, auth = auth, storage = null)

        val profileResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.ensureUserProfile()
        }
        assertTrue("Expected ensureUserProfile to succeed", profileResult.isSuccess)
        val aliceProfile = profileResult.getOrThrow()
        assertEquals(aliceUid, aliceProfile.userId)
        assertEquals("user", aliceProfile.role)

        // Switch to Bob and verify Bob cannot read Alice's PII user profile
        signInTestUser(bobEmail)
        val bobRepo = WallxRepository(db = firestore, auth = auth, storage = null)
        val crossReadResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getUserProfileById(aliceUid)
        }
        assertTrue("Expected cross-user profile read to fail", crossReadResult.isFailure)
        val fsError = crossReadResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull(fsError)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsError?.code)
    }

    @Test
    fun createAndObserveWallpaperRecord_uploadsBase64WallpaperAndReturnsInStream() = runBlocking {
        val userEmail = "creator_${UUID.randomUUID().toString().take(8)}@test.com"
        val uid = signInTestUser(userEmail)
        val repo = WallxRepository(db = firestore, auth = auth, storage = null)

        val wallId = "wall_${UUID.randomUUID().toString().replace("-", "").take(10)}"
        val sampleWallpaper = WallpaperItem(
            id = wallId,
            title = "Uploaded 4K Masterpiece",
            description = "Uploaded to Firestore Cloud",
            category = "AMOLED",
            tags = listOf("amoled", "4k"),
            resolution = "3840x2160 (4K)",
            fileSize = "4.2 MB",
            fileSizeBytes = 4200000L,
            mimeType = "image/jpeg",
            imageUrl = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/",
            thumbnailUrl = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/",
            storagePath = "firestore_cloud/$wallId.jpg",
            status = "published",
            isFeatured = true,
            isTrending = true,
            uploadedBy = uid
        )

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createWallpaperRecord(sampleWallpaper)
        }
        assertTrue("Uploaded wallpaper creation must succeed in Firestore: ${createResult.exceptionOrNull()}", createResult.isSuccess)

        val publishedList = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observePublishedWallpapers().first { list -> list.any { it.id == wallId } }
        }
        assertTrue("Expected uploaded wallpaper to appear in observePublishedWallpapers", publishedList.any { it.id == wallId })
    }

    private companion object {
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 5000L
    }
}
