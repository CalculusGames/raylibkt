package kray

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kray.sprites.Sprite2D
import kray.sprites.drawSprite
import kray.sprites.drawnSprites
import raylib.Window
import raylib.Canvas

/**
 * The primary entrypoint of a Kray application
 */
suspend fun Kray(
	width: Int = 800,
	height: Int = 600,
	title: String = "Kray App",
	entrypoint: suspend Kray.() -> Unit
) {
	Window.open(width, height, title)
	entrypoint(Kray)
}

/**
 * The Kray game engine.
 */
object Kray {
	val window = Window
	val canvas = Canvas

	/**
	 * The provided lifecycle loop for the application.
	 */
	var loop: (suspend CoroutineScope.() -> Unit)? = null
		private set

	/**
	 * Whether the engine is currently stopped.
	 */
	var stopped = false
		private set

	/**
	 * Sets the game loop.
	 */
	suspend fun loop(logic: suspend CoroutineScope.() -> Unit) {
		if (this.loop != null) throw IllegalStateException("Already looping")
		this.loop = logic

		coroutineScope {
			while (!Window.shouldClose && !stopped) {
				logic()

				// draw registered sprites
				drawnSprites.forEach { sprite ->
					if (sprite is Sprite2D && sprite.isDrawn) {
						canvas.drawSprite(sprite, sprite.x, sprite.y)
					}
				}
			}
		}
	}

	/**
	 * Runs the given [logic] on the IO dispatcher.
	 * @param logic The logic to run.
	 * @return A [suspend] [Unit].
	 */
	suspend fun io(logic: suspend CoroutineScope.() -> Unit) {
		withContext(Dispatchers.IO) {
			logic()
		}
	}

	/**
	 * Stops the current lifecycle [loop], ending the game.
	 */
	fun stopLoop() {
		stopped = true
	}
}
