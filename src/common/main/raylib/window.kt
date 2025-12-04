@file:OptIn(ExperimentalForeignApi::class)

package raylib

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.toKString
import kotlinx.cinterop.useContents
import platform.posix.getenv
import raylib.internal.*

/**
 * The window management object.
 */
object Window {

	/**
	 * The current window title.
	 */
	var currentTitle: String? = null
		internal set

	/**
	 * The width of the current screen.
	 */
	val screenWidth: Int
		get() = GetScreenWidth()

	/**
	 * The height of the current screen.
	 */
	val screenHeight: Int
		get() = GetScreenHeight()

	/**
	 * The width of the render area.
	 */
	val renderWidth: Int
		get() = GetRenderWidth()

	/**
	 * The height of the render area.
	 */
	val renderHeight: Int
		get() = GetRenderHeight()

	/**
	 * Indicates whether the system is running in headless mode (no monitors connected).
	 *
	 * Note: macOS headless detection is not 100% reliable.
	 */
	val isHeadless: Boolean
		get() = _isHeadless || getenv("CI") != null || getenv("HEADLESS") != null

	/**
	 * The number of monitors connected to the system.
	 */
	val monitorCount: Int
		get() = GetMonitorCount()

	/**
	 * The current monitor ID.
	 */
	val monitorId: Int
		get() = GetCurrentMonitor()

	/**
	 * The X position of the current monitor in pixels.
	 *
	 * This determines the monitor's position in a multi-monitor setup. For example,
	 * if a monitor is positioned to the left of the primary monitor, this value may be negative.
	 */
	val monitorX: Float
		get() = GetMonitorPosition(monitorId).useContents { x }

	/**
	 * The Y position of the current monitor in pixels.
	 *
	 * This determines the monitor's position in a multi-monitor setup. For example,
	 * if a monitor is positioned above the primary monitor, this value may be negative.
	 */
	val monitorY: Float
		get() = GetMonitorPosition(monitorId).useContents { y }

	/**
	 * The width of the current monitor.
	 */
	val monitorWidth: Int
		get() = GetMonitorWidth(monitorId)

	/**
	 * The height of the current monitor.
	 */
	val monitorHeight: Int
		get() = GetMonitorHeight(monitorId)

	/**
	 * The physical width of the current monitor in millimeters.
	 */
	val monitorPhysicalWidth: Int
		get() = GetMonitorPhysicalWidth(monitorId)

	/**
	 * The physical height of the current monitor in millimeters.
	 */
	val monitorPhysicalHeight: Int
		get() = GetMonitorPhysicalHeight(monitorId)

	/**
	 * The current refresh rate of the monitor in Hz.
	 */
	val monitorRefreshRate: Int
		get() = GetMonitorRefreshRate(monitorId)

	/**
	 * The name of the current monitor.
	 */
	val monitorName: String?
		get() = GetMonitorName(monitorId)?.toKString()

    /**
     * Initializes the window with the specified width, height, and title.
     * @param width The width of the window.
     * @param height The height of the window.
     * @param title The title of the window.
	 * @param loop Optional lifecycle loop (see [lifecycle])
	 * @param targetFps The target FPS to set for the game (defaults to 60 when [loop] is provided)
     */
	fun open(
		width: Int,
		height: Int,
		title: String = "Raylib Window",
		loop: (Window.() -> Unit)? = null,
		targetFps: Int? = null
	) {
        InitWindow(width, height, title)
		currentTitle = title

		if (targetFps != null) {
			this.fps = targetFps
		}

		if (loop != null) {
			if (targetFps == null) this.fps = 60
			lifecycle(loop)
		}
    }

	/**
	 * Runs a game loop while [shouldClose] is false.
	 *
	 * This should serve as the main entrypoint to your game. The function will be called repeatedly until
	 * the game closes.
	 * @param loop The game loop to call until the window should be closed
	 */
	fun lifecycle(loop: Window.() -> Unit) {
		while (!shouldClose) {
			loop()
		}
	}

	/**
	 * The X position of the window on the screen.
	 */
	val windowX: Float
		get() = GetWindowPosition().useContents { x }

	/**
	 * The Y position of the window on the screen.
	 */
	val windowY: Float
		get() = GetWindowPosition().useContents { y }

	/**
	 * The current scale factor of the window on the X axis for HighDPI support.
	 */
	val windowScaleDPIX: Float
		get() = GetWindowScaleDPI().useContents { x }

	/**
	 * The current scale factor of the window on the Y axis for HighDPI support.
	 */
	val windowScaleDPIY: Float
		get() = GetWindowScaleDPI().useContents { y }

    /**
     * Closes the window.
     */
    fun close() {
		_close0()
    }

    /**
     * Indicates whether the window has been initialized successfully.
     */
    val ready: Boolean
        get() = IsWindowReady()

	/**
	 * Whether the window should close.
	 *
	 * This is determined by whether the 'X' button was clicked, the computer is shutting down,
	 * or other triggers that would cause the window to close.
	 */
	val shouldClose: Boolean
		get() = WindowShouldClose()

    /**
     * Indicates whether the window is hidden from view.
     */
    val hidden: Boolean
        get() = IsWindowHidden()

    /**
     * Indicates whether the window is currently minimized.
     */
    val minimized: Boolean
        get() = IsWindowMinimized()

    /**
     * Indicates whether the window is currently maximized.
     */
    val maximized: Boolean
        get() = IsWindowMaximized()

    /**
     * Indicates whether the window is currently focused.
     */
    val focused: Boolean
        get() = IsWindowFocused()

    /**
     * Indicates whether the window has been resized in the last frame.
     */
    val wasResized: Boolean
        get() = IsWindowResized()

    /**
     * The various windows state flags available.
     */
    enum class State(internal val value: UInt) {
        /**
         * Set to try enabling V-Sync on GPU
         */
        VSYNC_HINT(0x00000040u),
        /**
         * Set to run program in fullscreen
         */
        FULLSCREEN_MODE(0x00000002u),
        /**
         * Set to allow resizable window
         */
        WINDOW_RESIZABLE(0x00000004u),
        /**
         * Set to disable window decoration (frame and buttons)
         */
        WINDOW_UNDECORATED(0x00000008u),
        /**
         * Set to hide window
         */
        WINDOW_HIDDEN(0x00000080u),
        /**
         * Set to minimize window (iconify)
         */
        WINDOW_MINIMIZED(0x00000200u),
        /**
         * Set to maximize window (expanded to monitor)
         */
        WINDOW_MAXIMIZED(0x00000400u),
        /**
         * Set the window to be non-focused
         */
        WINDOW_UNFOCUSED(0x00000800u),
        /**
         * Set the window to be always on top
         */
        WINDOW_TOPMOST(0x00001000u),
        /**
         * Set to allow windows running while minimized
         */
        WINDOW_ALWAYS_RUN(0x00000100u),
        /**
         * Set to allow transparent framebuffer
         */
        WINDOW_TRANSPARENT(0x00000010u),
        /**
         * Set to support HighDPI
         */
        WINDOW_HIGHDPI(0x00002000u),
        /**
         * Set to support mouse passthrough, only supported when WINDOW_UNDECORATED
         */
        WINDOW_MOUSE_PASSTHROUGH(0x00004000u),
        /**
         * Set to run program in borderless windowed mode
         */
        BORDERLESS_WINDOWED_MODE(0x00008000u),
        /**
         * Set to try enabling MSAA 4X
         */
        MSAA_4X_HINT(0x00000020u),
        /**
         * Set to try enabling interlaced video format (for V3D)
         */
        INTERLACED_HINT(0x00010000u)

        ;

        internal companion object {
            fun from(values: UInt): Set<State> {
                return entries.filter { (it.value and values) != 0u }.toSet()
            }
        }
    }

    /**
     * Determines if a specific window flag is enabled.
     * @param flag The window state flag to check.
     * @return True if the flag is enabled, false otherwise.
     */
    fun isFlagEnabled(flag: State): Boolean {
        return IsWindowState(flag.value)
    }

    /**
     * Sets specific window state flags.
     * @param flags The set of window state flags to enable.
     */
    fun setFlags(flags: Set<State>) {
        val combined = flags.fold(0u) { acc, state -> acc or state.value }
        SetWindowState(combined)
    }

    /**
     * The state of fullscreen on the window.
     */
    var fullscreen: Boolean
        get() = IsWindowFullscreen()
        set(value) {
            val state = IsWindowFullscreen()
            if (value != state) ToggleFullscreen()
        }

    /**
     * Toggles the fullscreen state of the window.
     */
    fun toggleFullscreen() {
        ToggleFullscreen()
    }

    /**
     * Toggles the borderless windowed mode of the window.
     */
    fun toggleBorderlessWindowed() {
        ToggleBorderlessWindowed()
    }

    /**
     * Minimizes the window.
     */
    fun minimize() {
        MinimizeWindow()
    }

    /**
     * Maximizes the window.
     */
    fun maximize() {
        MaximizeWindow()
    }

    /**
     * Restores the window to its normal state that isn't minimized or maximized.
     */
    fun restore() {
        RestoreWindow()
    }

    /**
     * The system clipboard text.
     */
    var clipboardText: String?
        get() = GetClipboardText()?.toKString()
        set(value) = SetClipboardText(value)

	/**
	 * The image stored in the system clipboard.
	 */
	val clipboardImage: Image
		get() = Image(GetClipboardImage())

	/**
	 * Enables waiting for events when canvas drawing is finished.
	 */
	fun enableEventWaiting() {
		EnableEventWaiting()
	}

	/**
	 * Disables waiting for events when canvas drawing is finished.
	 */
	fun disableEventWaiting() {
		DisableEventWaiting()
	}

	/**
	 * The number of seconds since [open] was called.
	 */
	val time: Double
		get() = GetTime()

	/**
	 * The target frames per second in the window.
	 *
	 * The getter will return the current FPS; setter will set the target FPS.
	 */
	var fps: Int
		get() = GetFPS()
		set(value) = SetTargetFPS(value)

}

/**
 * The canvas management object.
 */
object Canvas {

	/**
	 * Whether the canvas is currently in a drawing state.
	 */
	var inDrawingState: Boolean = false
		private set

	/**
	 * Sets the background color of the canvas.
	 * @param color The color to set as the background.
	 */
	fun setBackgroundColor(color: Color) {
		ClearBackground(color.raw())
	}

	/**
	 * Begins the drawing process on the canvas.
	 *
	 * Note that [end] must be called after finishing drawing to finalize.
	 */
	fun start() {
		if (inDrawingState) return
		BeginDrawing()
		inDrawingState = true
	}

	/**
	 * Ends the drawing process on the canvas.
	 */
	fun end() {
		if (!inDrawingState) return
 		EndDrawing()
		inDrawingState = false
	}

	/**
	 * Draws on the canvas using the provided callback function.
	 *
	 * [start] and [end] are called automatically.
	 * @param callback The drawing operations to perform on the canvas.
	 */
	fun draw(callback: Canvas.() -> Unit) {
		if (!inDrawingState) start()
		this.callback()
		if (inDrawingState) end()
	}

}

// Expect

internal expect val _isHeadless: Boolean

internal expect fun _close0()
