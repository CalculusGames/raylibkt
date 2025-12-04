@file:OptIn(ExperimentalForeignApi::class)

package raylib

import kotlinx.cinterop.ExperimentalForeignApi
import raylib.internal.*

/**
 * The current mouse and cursor state.
 */
object Mouse {

	/**
	 * Whether the cursor is enabled on the screen.
	 */
	var isCursorEnabled: Boolean
		get() = IsCursorOnScreen()
		set(value) {
			if (value) EnableCursor() else DisableCursor()
		}

	/**
	 * Whether the cursor is hidden.
	 */
	var isCursorHidden: Boolean
		get() = IsCursorHidden()
		set(value) {
			if (value) HideCursor() else ShowCursor()
		}

	/**
	 * Indicates whether the cursor is currently on the screen.
	 */
	val isCursorOnScreen: Boolean
		get() = IsCursorOnScreen()

	/**
	 * The current X position of the mouse.
	 */
	var mouseX: Int
		get() = GetMouseX()
		set(value) = SetMousePosition(value, mouseY)

	/**
	 * The current Y position of the mouse.
	 */
	var mouseY: Int
		get() = GetMouseY()
		set(value) = SetMousePosition(mouseX, value)
}

/**
 * The current gamepad state, if one is connected.
 */
object GamePad {

}
