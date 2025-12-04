package raylib

import platform.posix.getenv
import raylib.internal.WindowShouldClose
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TestWindow {

	@Test
	fun testWindow() {
		if (Window.isHeadless) return
		assertFalse { Window.isHeadless }

		Window.open(800, 600, "Test Window")
		assertTrue { Window.ready }
		assertEquals("Test Window", Window.currentTitle)

		// Clipboard
		Window.clipboardText = "Kray"
		assertEquals("Kray", Window.clipboardText)

		// FPS & Time
		println("${Window.fps} FPS")
		println("${Window.time} seconds passed")

		Window.close()
		assertFalse { Window.ready }
	}

}
