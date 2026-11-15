package com.example

import com.example.data.model.WallpaperItem
import com.example.data.model.WallxCatalogSeed
import com.example.data.model.isRemovedByUserRequest
import org.junit.Assert.*
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun defaultWallpapersPopulated_adminUploadedRetained() {
    assertTrue(WallxCatalogSeed.defaultWallpapers("admin_uid").size >= 12)

    val adminUpload = WallpaperItem(
      id = "wall_admin_1700000000000",
      title = "Admin Uploaded 4K",
      imageUrl = "https://example.com/wall.jpg",
      status = "published"
    )
    assertFalse(adminUpload.isRemovedByUserRequest())
  }
}
