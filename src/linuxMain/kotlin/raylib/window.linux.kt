@file:OptIn(ExperimentalForeignApi::class)

package raylib

import kotlinx.cinterop.ExperimentalForeignApi
import platform.posix.getenv
import raylib.internal.CloseWindow

internal actual val _isHeadless: Boolean
    get() = getenv("DISPLAY") == null || getenv("WAYLAND_DISPLAY") == null

internal actual fun _close0() {
	CloseWindow()
	_closed0 = true
}
internal actual var _closed0: Boolean = false
