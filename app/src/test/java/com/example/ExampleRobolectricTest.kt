package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Pixora Wallpapers", appName)
  }

  @Test
  fun `verify liquid glass presets and optics defaults`() {
    val defaultOptics = com.example.ui.theme.LiquidGlassOptics()
    org.junit.Assert.assertEquals(60f, defaultOptics.dockBlurDp)
    org.junit.Assert.assertTrue(defaultOptics.enable120HzSmoothness)
    org.junit.Assert.assertEquals(145f, defaultOptics.refractionStrength)
    org.junit.Assert.assertEquals(72f, defaultOptics.refractionDepth)
    org.junit.Assert.assertTrue(defaultOptics.glassOpacity in 0.10f..1.0f)
    org.junit.Assert.assertTrue(defaultOptics.glowIntensity >= 0.0f)

    // Verify 2 TB Admin & Cloud Vault limit
    org.junit.Assert.assertEquals(2L * 1024L * 1024L * 1024L * 1024L, com.example.data.model.MAX_UPLOAD_BYTES)

    // Verify expanded App UI Designs catalog in Settings (10 Premium + 12 Classic = 22 designs)
    val designs = com.example.ui.theme.AppDesignCatalog.allDesigns
    org.junit.Assert.assertTrue(designs.size >= 22)
    org.junit.Assert.assertEquals("ios27_crystal", com.example.ui.theme.AppDesignCatalog.byId("ios27_crystal").id)
    org.junit.Assert.assertEquals(3, com.example.ui.theme.AppDesignCatalog.proStudio3Col.gridColumns)
    org.junit.Assert.assertEquals(1, com.example.ui.theme.AppDesignCatalog.cinemaImmersive.gridColumns)

    // Verify Interactive Preview Modes and Bitmap Blur/Dimming Renderer
    val previewModes = com.example.ui.screens.InteractivePreviewMode.entries
    org.junit.Assert.assertEquals(3, previewModes.size)
    val srcBitmap = android.graphics.Bitmap.createBitmap(120, 200, android.graphics.Bitmap.Config.ARGB_8888)
    val renderedBitmap = com.example.util.WallpaperDownloader.renderEffectsOntoBitmap(
      source = srcBitmap,
      blurRadiusDp = 14f,
      topBlurStrengthDp = 20f,
      dimAlpha = 0.25f
    )
    org.junit.Assert.assertEquals(120, renderedBitmap.width)
    org.junit.Assert.assertEquals(200, renderedBitmap.height)
  }

  @Test
  fun `verify wallpapers show immediately on startup and uploaded wallpapers appear first`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    com.google.firebase.FirebaseApp.initializeApp(context)
    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance(
      context.getString(R.string.firestore_database_id)
    )
    val repo = com.example.data.repository.WallxRepository(db = db, storage = null, appContext = context)
    val viewModel = com.example.ui.viewmodel.WallxViewModel(
      repository = repo,
      currentUserId = "test_user_uid",
      appContext = context
    )

    // Wallpapers must always be visible immediately on startup even when Firestore read quota is exhausted
    org.junit.Assert.assertTrue(
      "Expected wallpapers to show immediately on cold start",
      viewModel.publishedWallpapers.value.size >= 12
    )

    // Verify Admin upload immediately appears at the very top (#1) of publishedWallpapers and normalizes non-existent local file:/ to cloud thumbnailUrl
    val uploadedWall = com.example.data.model.WallpaperItem(
      id = "wall_admin_test_999",
      title = "Admin Liquid 8K Test",
      category = "Anime",
      imageUrl = "file:///non_existent_other_device_vault/wall_admin_test_999.jpg",
      thumbnailUrl = "data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/",
      status = "published",
      isFeatured = true,
      isTrending = true
    )
    viewModel.createOrUpdateWallpaperDirectAdmin(uploadedWall, isNew = true)
    org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()

    val firstItem = viewModel.publishedWallpapers.value.firstOrNull()
    org.junit.Assert.assertNotNull("Expected publishedWallpapers to be non-empty", firstItem)
    org.junit.Assert.assertEquals("wall_admin_test_999", firstItem!!.id)
    org.junit.Assert.assertTrue(
      "Expected non-existent file:/ imageUrl to normalize to playable cloud data:image thumbnailUrl",
      firstItem.imageUrl.startsWith("data:image")
    )
  }
}
