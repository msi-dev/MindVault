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
    assertEquals("MindVault", appName)
  }

  @Test
  fun `test native bridge metrics`() {
    val bridge = com.example.nativeengine.NativeBridge()
    val words = bridge.calculateWordCount("This is a test note for MindVault")
    assertEquals(7, words)
    val readTime = bridge.estimateReadingTimeMinutes("Short note")
    assertEquals(1, readTime)
  }
}
