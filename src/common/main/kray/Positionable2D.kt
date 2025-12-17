package kray

/**
 * Represents an object that has a position in 2D space.
 */
interface Positionable2D {

	/**
	 * The x-coordinate of the object.
	 */
	var x: Int

	/**
	 * The y-coordinate of the object.
	 */
	var y: Int

	/**
	 * Moves the object to the specified [newX] and [newY] coordinates in place.
	 * @param newX The new x-coordinate of the object.
	 * @param newY The new y-coordinate of the object.
	 */
	fun moveTo(newX: Int, newY: Int) {
		if (newX < 0 || newY < 0)
			throw IllegalArgumentException("Coordinates ($newX, $newY) must be non-negative integers.")

		x = newX
		y = newY
	}

	/**
	 * Moves the object by the specified [deltaX] and [deltaY] offsets in place.
	 * @param deltaX The offset to move the object along the x-axis.
	 * @param deltaY The offset to move the object along the y-axis.
	 */
	fun moveBy(deltaX: Int, deltaY: Int) {
		moveTo(x + deltaX, y + deltaY)
	}

}
