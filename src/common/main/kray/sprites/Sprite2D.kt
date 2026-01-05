package kray.sprites

import kray.Positionable2D
import kray.Sizeable2D
import raylib.Image

/**
 * Represents a sprite in the Kray game engine.
 */
class Sprite2D(internal var raw: Image) : Sprite<Image>, Positionable2D, Sizeable2D {

	override var x: Float = 0F
	override var y: Float = 0F
	override val width: Int
		get() = raw.width
	override val height: Int
		get() = raw.height

	override var isDrawn: Boolean = false
		internal set
	override var static: Boolean = false
	override var collideable: Boolean = true

	/**
	 * The z-index of the sprite. Higher values are drawn on top of lower values.
	 */
	var zIndex: Int = 0

	override var currentCostumeIndex: Int = 0
		internal set

	/**
	 * An immutable list of all costumes (images) associated with this sprite.
	 */
	override val costumes = mutableListOf(raw)

	override fun resize(newWidth: Int, newHeight: Int): Sprite2D {
		if (newWidth <= 0 || newHeight <= 0)
			throw IllegalArgumentException("Width and height must be positive integers.")

		raw = raw.resize(newWidth, newHeight)
		return this
	}

	override fun changeTo(index: Int) {
		if (index < 0 || index >= costumes.size)
			throw IndexOutOfBoundsException("Index $index out of bounds for costumes of size ${costumes.size}")

		raw = costumes[index]
		currentCostumeIndex = index
	}

	override fun switchTo(costume: Image) {
		val index = costumes.indexOf(costume)
		if (index == -1) {
			throw IllegalArgumentException("The provided image is not a costume of this sprite.")
		}

		raw = costume
		currentCostumeIndex = index
	}

	override fun nextCostume() {
		if (costumes.isEmpty()) return

		val nextIndex = (currentCostumeIndex + 1) % costumes.size
		raw = costumes[nextIndex]
		currentCostumeIndex = nextIndex
	}

	override fun previousCostume() {
		if (costumes.isEmpty()) return

		val prevIndex = if (currentCostumeIndex - 1 < 0)
			costumes.size - 1
		else
			(currentCostumeIndex - 1) % costumes.size

		raw = costumes[prevIndex]
		currentCostumeIndex = prevIndex
	}

	override fun setCurrentCostume(costume: Image) {
		val index = costumes.indexOf(costume)
		if (index == -1) {
			addCostume(costume)
			raw = costume
			currentCostumeIndex = costumes.size - 1
		} else {
			raw = costume
			currentCostumeIndex = index
		}
	}

	companion object {
		/**
		 * Creates a [Sprite2D] from the given [resolver] function that provides an [Image].
		 * @param resolver The function that provides the [Image].
		 * @return A new [Sprite2D] instance.
		 */
		fun from(resolver: () -> Image): Sprite2D {
			return Sprite2D(resolver())
		}
	}
}
