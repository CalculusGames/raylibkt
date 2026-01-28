@file:OptIn(ExperimentalUuidApi::class)

package kray.physics

import kray.Kray.window
import kray.Positionable
import kray.Positionable2D
import kray.Positionable3D
import kray.sprites.Sprite2D
import kray.sprites.Sprite3D
import kray.sprites.registeredSprites
import kray.to
import raylib.Matrix4
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.sqrt
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val FPI = PI.toFloat()
private const val TWO_PI = (2 * PI).toFloat()

/**
 * Normalizes an angle in radians to the range [-π, π].
 */
private fun normalizeAngle(angle: Float): Float {
	var normalized = angle % TWO_PI
	if (normalized > FPI) normalized -= TWO_PI
	if (normalized < -FPI) normalized += TWO_PI
	return normalized
}

private val massMultipliers = mutableMapOf<Uuid, Double>()

/**
 * The mass multiplier of the [Positionable] object.
 * This is used in physics calculations to determine the effective mass of the object.
 * The default value is 1.0.
 */
var Positionable.mass: Double
	get() = massMultipliers[id] ?: 1.0
	set(value) {
		massMultipliers[id] = value
	}

/**
 * The gravitational acceleration constant used in physics calculations.
 * The default value is 9.81 m/s².
 */
var gravity: Double = 9.81
	set(value) {
		if (value < 0.0)
			throw IllegalArgumentException("Gravity cannot be negative.")

		field = value
	}

/**
 * The default friction coefficient used in physics calculations.
 * The default value is 0.5.
 */
var defaultFrictionCoefficient: Double = 0.5
	set(value) {
		if (value < 0.0)
			throw IllegalArgumentException("Default friction coefficient cannot be negative.")

		field = value
	}

private val frictionCoefficients = mutableMapOf<Uuid, Double>()

/**
 * The friction coefficient of the [Positionable] object.
 * This is used in physics calculations to determine the frictional force acting on the object.
 * The default value is [defaultFrictionCoefficient].
 */
var Positionable.frictionCoefficient: Double
	get() = frictionCoefficients[id] ?: defaultFrictionCoefficient
	set(value) {
		if (value < 0.0)
			throw IllegalArgumentException("Friction coefficient cannot be negative.")

		frictionCoefficients[id] = value
	}

/**
 * The default restitution coefficient used in physics calculations.
 *
 * Determines how bouncy objects are after collisions. The default value is 0.1.
 */
var defaultRestitutionCoefficient: Double = 0.1
	set(value) {
		if (value < 0.0)
			throw IllegalArgumentException("Default restitution coefficient cannot be negative.")

		field = value
	}

private val restitutionCoefficients = mutableMapOf<Uuid, Double>()

/**
 * The restitution coefficient of the [Positionable] object.
 * This is used in physics calculations to determine how bouncy the object is after collisions.
 * The default value is [defaultRestitutionCoefficient].
 */
var Positionable.restitutionCoefficient: Double
	get() = restitutionCoefficients[id] ?: defaultRestitutionCoefficient
	set(value) {
		if (value < 0.0)
			throw IllegalArgumentException("Restitution coefficient cannot be negative.")

		restitutionCoefficients[id] = value
	}


private val spinFactors = mutableMapOf<Uuid, Float>()
private const val snapThreshold = 0.1f

/**
 * The spin factor of the [Positionable] object.
 *
 * Determines how much spin is applied during collisions. The default value is 0.0 (no spin).
 */
var Positionable.spinFactor: Float
	get() = spinFactors[id] ?: 0.0F
	set(value) {
		spinFactors[id] = value
	}

private val acceleration2D = mutableMapOf<Uuid, Pair<Double, Double>>()

/**
 * The x-component of the 2D acceleration of the [Positionable2D] object.
 */
var Positionable2D.ax: Double
	get() = acceleration2D[id]?.first ?: 0.0
	set(value) {
		val current = acceleration2D[id] ?: Pair(0.0, 0.0)
		acceleration2D[id] = Pair(value, current.second)
	}

/**
 * The y-component of the 2D acceleration of the [Positionable2D] object.
 */
var Positionable2D.ay: Double
	get() = acceleration2D[id]?.second ?: 0.0
	set(value) {
		val current = acceleration2D[id] ?: Pair(0.0, 0.0)
		acceleration2D[id] = Pair(current.first, value)
	}

/**
 * Sets both the x and y components of the 2D acceleration of the [Positionable2D] object.
 * @param ax The x-component of the acceleration.
 * @param ay The y-component of the acceleration.
 */
fun Positionable2D.setAcceleration(ax: Double, ay: Double) {
	acceleration2D[id] = Pair(ax, ay)
}

private val velocity2D = mutableMapOf<Uuid, Pair<Double, Double>>()

/**
 * The x-component of the 2D velocity of the [Positionable2D] object.
 */
var Positionable2D.vx: Double
	get() = velocity2D[id]?.first ?: 0.0
	set(value) {
		val current = velocity2D[id] ?: Pair(0.0, 0.0)
		velocity2D[id] = Pair(value, current.second)
	}

/**
 * The y-component of the 2D velocity of the [Positionable2D] object
 */
var Positionable2D.vy: Double
	get() = velocity2D[id]?.second ?: 0.0
	set(value) {
		val current = velocity2D[id] ?: Pair(0.0, 0.0)
		velocity2D[id] = Pair(current.first, value)
	}

/**
 * Sets both the x and y components of the 2D velocity of the [Positionable2D] object.
 * @param vx The x-component of the velocity.
 * @param vy The y-component of the velocity.
 */
fun Positionable2D.setVelocity(vx: Double, vy: Double) {
	velocity2D[id] = Pair(vx, vy)
}

private val acceleration3D = mutableMapOf<Uuid, Triple<Double, Double, Double>>()

/**
 * The x-component of the 3D acceleration of the [Positionable3D] object.
 */
var Positionable3D.ax: Double
	get() = acceleration3D[id]?.first ?: 0.0
	set(value) {
		val current = acceleration3D[id] ?: Triple(0.0, 0.0, 0.0)
		acceleration3D[id] = Triple(value, current.second, current.third)
	}

/**
 * The y-component of the 3D acceleration of the [Positionable3D] object.
 */
var Positionable3D.ay: Double
	get() = acceleration3D[id]?.second ?: 0.0
	set(value) {
		val current = acceleration3D[id] ?: Triple(0.0, 0.0, 0.0)
		acceleration3D[id] = Triple(current.first, value, current.third)
	}

/**
 * The z-component of the 3D acceleration of the [Positionable3D] object.
 */
var Positionable3D.az: Double
	get() = acceleration3D[id]?.third ?: 0.0
	set(value) {
		val current = acceleration3D[id] ?: Triple(0.0, 0.0, 0.0)
		acceleration3D[id] = Triple(current.first, current.second, value)
	}

/**
 * Sets the x, y, and z components of the 3D acceleration of the [Positionable3D] object.
 * @param ax The x-component of the acceleration.
 * @param ay The y-component of the acceleration.
 * @param az The z-component of the acceleration.
 */
fun Positionable3D.setAcceleration(ax: Double, ay: Double, az: Double) {
	acceleration3D[id] = Triple(ax, ay, az)
}

private val velocity3D = mutableMapOf<Uuid, Triple<Double, Double, Double>>()

/**
 * The x-component of the 3D velocity of the [Positionable3D] object.
 */
var Positionable3D.vx: Double
	get() = velocity3D[id]?.first ?: 0.0
	set(value) {
		val current = velocity3D[id] ?: Triple(0.0, 0.0, 0.0)
		velocity3D[id] = Triple(value, current.second, current.third)
	}

/**
 * The y-component of the 3D velocity of the [Positionable3D] object.
 */
var Positionable3D.vy: Double
	get() = velocity3D[id]?.second ?: 0.0
	set(value) {
		val current = velocity3D[id] ?: Triple(0.0, 0.0, 0.0)
		velocity3D[id] = Triple(current.first, value, current.third)
	}

/**
 * The z-component of the 3D velocity of the [Positionable3D] object.
 */
var Positionable3D.vz: Double
	get() = velocity3D[id]?.third ?: 0.0
	set(value) {
		val current = velocity3D[id] ?: Triple(0.0, 0.0, 0.0)
		velocity3D[id] = Triple(current.first, current.second, value)
	}

/**
 * Sets the x, y, and z components of the 3D velocity of the [Positionable3D] object.
 * @param vx The x-component of the velocity.
 * @param vy The y-component of the velocity.
 * @param vz The z-component of the velocity.
 */
fun Positionable3D.setVelocity(vx: Double, vy: Double, vz: Double) {
	velocity3D[id] = Triple(vx, vy, vz)
}

/**
 * The y-coordinate representing the ground level in the physics simulation.
 * Objects should not fall below this y-coordinate.
 */
var groundY: Float = window.screenHeight.toFloat()

/**
 * The minimum x-coordinate boundary in the physics simulation.
 */
var minX: Float = Float.MIN_VALUE
	set(value) {
		if (value >= maxX)
			throw IllegalArgumentException("Min X must be less than maximum")

		field = value
	}

/**
 * The maximum x-coordinate boundary in the physics simulation.
 */
var maxX: Float = Float.MAX_VALUE
	set(value) {
		if (value <= minX)
			throw IllegalArgumentException("Max X must be greater than minimum.")

		field = value
	}

/**
 * The minimum z-coordinate boundary in the physics simulation.
 */
var minZ: Float = Float.MIN_VALUE
	set(value) {
		if (value >= maxZ)
			throw IllegalArgumentException("Max Z must be less than maximum.")

		field = value
	}

/**
 * The maximum z-coordinate boundary in the physics simulation.
 */
var maxZ: Float = Float.MAX_VALUE
	set(value) {
		if (value <= minZ)
			throw IllegalArgumentException("Max Z must be greater than minimum.")

		field = value
	}

/**
 * The terminal velocity of all objects in the engine.
 */
var terminalVelocity: Float = 500.0f
	set(value) {
		if (value < 0.0f)
			throw IllegalArgumentException("Max Velocity must be positive.")

		field = value
	}

/**
 * The normal threshold for vector values.
 *
 * Vector values (velocity and acceleration) that fall below this constant will be set to 0.
 */
const val NORMAL_THRESHOLD = 0.05

// collision utilities

/**
 * The precision used for collision detection sampling. This number is
 * the number of samples taken along each axis within a spatial grid cell.
 *
 * Higher values increase accuracy but decrease performance. Lower values
 * increase performance but decrease accuracy.
 *
 * Depending on the high-resolution and speed of the sprites, you may want to adjust this value.
 * Faster and smaller sprites may require a higher value for accurate collision detection.
 *
 * The default value is 64, which provides a good balance between accuracy and performance for most scenarios.
 *
 * **Changing this value in between engine ticks may cause issues with collision detection.**
 */
var collisionPrecision = 64

/**
 * The size of each cell in the spatial grid used for collision detection.
 *
 * Larger cell sizes decrease the number of cells and increase accuracy,
 * but may decrease performance.
 *
 * Smaller cell sizes increase the number of cells and decrease accuracy,
 * but may increase performance.
 *
 * The default value is 250, which provides a good balance between accuracy and performance for most scenarios.
 *
 * **Changing this value in between engine ticks may cause issues with collision detection.**
 */
var cellSize = 250

private fun updateSpatialGrid() {
	spatialGrid2D.clear()
	spatialGrid3D.clear()
	for (sprite in registeredSprites) {
		if (sprite is Sprite2D) {
			val cells = getOccupiedCells2D(sprite)
			for (cell in cells) {
				spatialGrid2D.getOrPut(cell) { mutableSetOf() }.add(sprite)
			}
		}

		if (sprite is Sprite3D) {
			val cells = getOccupiedCells3D(sprite)
			for (cell in cells) {
				spatialGrid3D.getOrPut(cell) { mutableSetOf() }.add(sprite)
			}
		}
	}
}

/// collision utilities (2D)

private data class SpatialCell2D(val x: Int, val y: Int)
private val spatialGrid2D = mutableMapOf<SpatialCell2D, MutableSet<Sprite2D>>()
private val processedCollisionPairs2D = mutableSetOf<Pair<Sprite2D, Sprite2D>>()

private fun getOccupiedCells2D(sprite: Sprite2D): Set<SpatialCell2D> {
	val cells = mutableSetOf<SpatialCell2D>()
	val minCellX = ((sprite.x + sprite.hitbox.minX) / cellSize).toInt()
	val maxCellX = ((sprite.x + sprite.hitbox.maxX) / cellSize).toInt()
	val minCellY = ((sprite.y + sprite.hitbox.minY) / cellSize).toInt()
	val maxCellY = ((sprite.y + sprite.hitbox.maxY) / cellSize).toInt()

	for (cx in minCellX..maxCellX) {
		for (cy in minCellY..maxCellY) {
			cells.add(SpatialCell2D(cx, cy))
		}
	}
	return cells
}

private fun Hitbox2D.isCollidingWith(
	other: Hitbox2D,
	sprite1: Sprite2D,
	sprite2: Sprite2D,
): Boolean {
	val box1MinX = sprite1.x + this.minX
	val box1MaxX = sprite1.x + this.maxX
	val box1MinY = sprite1.y + this.minY
	val box1MaxY = sprite1.y + this.maxY

	val box2MinX = sprite2.x + other.minX
	val box2MaxX = sprite2.x + other.maxX
	val box2MinY = sprite2.y + other.minY
	val box2MaxY = sprite2.y + other.maxY

	if (box1MaxX < box2MinX || box1MinX > box2MaxX ||
		box1MaxY < box2MinY || box1MinY > box2MaxY) {
		return false
	}

	val ominX = maxOf(box1MinX, box2MinX)
	val ominY = maxOf(box1MinY, box2MinY)
	val omaxX = minOf(box1MaxX, box2MaxX)
	val omaxY = minOf(box1MaxY, box2MaxY)

	val stepX = (omaxX - ominX) / collisionPrecision
	val stepY = (omaxY - ominY) / collisionPrecision

	for (i in 0..collisionPrecision) {
		for (j in 0..collisionPrecision) {
			val worldX = ominX + stepX * i
			val worldY = ominY + stepY * j

			val local1X = worldX - sprite1.x
			val local1Y = worldY - sprite1.y
			val local2X = worldX - sprite2.x
			val local2Y = worldY - sprite2.y

			if (this.inside(local1X, local1Y) && other.inside(local2X, local2Y)) {
				return true
			}
		}
	}

	return false
}

private fun separateSprites2D(sprite1: Sprite2D, sprite2: Sprite2D) {
	// overlap vector2
	val centerX1 = sprite1.x + sprite1.width / 2f
	val centerY1 = sprite1.y + sprite1.height / 2f
	val centerX2 = sprite2.x + sprite2.width / 2f
	val centerY2 = sprite2.y + sprite2.height / 2f

	val dx = centerX2 - centerX1
	val dy = centerY2 - centerY1
	val distance = sqrt(dx * dx + dy * dy)

	if (distance == 0f) return

	val nx = dx / distance
	val ny = dy / distance

	val halfWidth1 = (sprite1.hitbox.maxX - sprite1.hitbox.minX) / 2f
	val halfHeight1 = (sprite1.hitbox.maxY - sprite1.hitbox.minY) / 2f
	val halfWidth2 = (sprite2.hitbox.maxX - sprite2.hitbox.minX) / 2f
	val halfHeight2 = (sprite2.hitbox.maxY - sprite2.hitbox.minY) / 2f

	val projection1 = abs(nx * halfWidth1) + abs(ny * halfHeight1)
	val projection2 = abs(nx * halfWidth2) + abs(ny * halfHeight2)
	val minSeparation = projection1 + projection2

	val overlap = minSeparation - distance

	if (overlap > 0) {
		if (sprite1.static && sprite2.static) {
			// both static; don't move either
			return
		} else if (sprite1.static) {
			sprite2.x += nx * overlap
			sprite2.y += ny * overlap
		} else if (sprite2.static) {
			sprite1.x -= nx * overlap
			sprite1.y -= ny * overlap
		} else {
			// both dynamic; distribute based on inverse mass
			val totalInvMass = 1f / sprite1.mass.toFloat() + 1f / sprite2.mass.toFloat()
			val push1 = overlap * (1f / sprite1.mass.toFloat()) / totalInvMass
			val push2 = overlap * (1f / sprite2.mass.toFloat()) / totalInvMass

			sprite1.x -= nx * push1
			sprite1.y -= ny * push1
			sprite2.x += nx * push2
			sprite2.y += ny * push2
		}
	}
}

/**
 * A set of all 2D sprites that are currently colliding with this sprite.
 * Uses spatial hashing for O(n) average case complexity.
 *
 * This may not work if the engine is not running or the spatial grid has not been updated.
 */
val Sprite2D.collisions: Set<Sprite2D>
	get() {
		val collisions = mutableSetOf<Sprite2D>()
		val cells = getOccupiedCells2D(this)

		for (cell in cells) {
			val occupants = spatialGrid2D[cell] ?: continue

			for (other in occupants) {
				if (other === this) continue

				if (this.hitbox.isCollidingWith(other.hitbox, this, other)) {
					collisions.add(other)
				}
			}
		}

		return collisions
	}

/// collision utilities (3D)

private data class SpatialCell3D(val x: Int, val y: Int, val z: Int)
private val spatialGrid3D = mutableMapOf<SpatialCell3D, MutableSet<Sprite3D>>()
private val processedCollisionPairs3D = mutableSetOf<Pair<Sprite3D, Sprite3D>>()

private fun getOccupiedCells3D(sprite: Sprite3D): Set<SpatialCell3D> {
	val cells = mutableSetOf<SpatialCell3D>()
	val minCellX = ((sprite.x + sprite.hitbox.minX) / cellSize).toInt()
	val maxCellX = ((sprite.x + sprite.hitbox.maxX) / cellSize).toInt()
	val minCellY = ((sprite.y + sprite.hitbox.minY) / cellSize).toInt()
	val maxCellY = ((sprite.y + sprite.hitbox.maxY) / cellSize).toInt()
	val minCellZ = ((sprite.z + sprite.hitbox.minZ) / cellSize).toInt()
	val maxCellZ = ((sprite.z + sprite.hitbox.maxZ) / cellSize).toInt()

	for (cx in minCellX..maxCellX) {
		for (cy in minCellY..maxCellY) {
			for (cz in minCellZ..maxCellZ) {
				cells.add(SpatialCell3D(cx, cy, cz))
			}
		}
	}
	return cells
}

private fun Hitbox3D.isCollidingWith(
	other: Hitbox3D,
	sprite1: Sprite3D,
	sprite2: Sprite3D,
): Boolean {
	val box1MinX = sprite1.x + this.minX
	val box1MaxX = sprite1.x + this.maxX
	val box1MinY = sprite1.y + this.minY
	val box1MaxY = sprite1.y + this.maxY
	val box1MinZ = sprite1.z + this.minZ
	val box1MaxZ = sprite1.z + this.maxZ

	val box2MinX = sprite2.x + other.minX
	val box2MaxX = sprite2.x + other.maxX
	val box2MinY = sprite2.y + other.minY
	val box2MaxY = sprite2.y + other.maxY
	val box2MinZ = sprite2.z + other.minZ
	val box2MaxZ = sprite2.z + other.maxZ

	if (box1MaxX < box2MinX || box1MinX > box2MaxX ||
		box1MaxY < box2MinY || box1MinY > box2MaxY ||
		box1MaxZ < box2MinZ || box1MinZ > box2MaxZ) {
		return false
	}

	val ominX = maxOf(box1MinX, box2MinX)
	val ominY = maxOf(box1MinY, box2MinY)
	val ominZ = maxOf(box1MinZ, box2MinZ)
	val omaxX = minOf(box1MaxX, box2MaxX)
	val omaxY = minOf(box1MaxY, box2MaxY)
	val omaxZ = minOf(box1MaxZ, box2MaxZ)

	val stepX = (omaxX - ominX) / collisionPrecision
	val stepY = (omaxY - ominY) / collisionPrecision
	val stepZ = (omaxZ - ominZ) / collisionPrecision

	for (i in 0..collisionPrecision) {
		for (j in 0..collisionPrecision) {
			for (k in 0..collisionPrecision) {
				val worldX = ominX + stepX * i
				val worldY = ominY + stepY * j
				val worldZ = ominZ + stepZ * k

				val local1X = worldX - sprite1.x
				val local1Y = worldY - sprite1.y
				val local1Z = worldZ - sprite1.z
				val local2X = worldX - sprite2.x
				val local2Y = worldY - sprite2.y
				val local2Z = worldZ - sprite2.z

				if (this.inside(local1X, local1Y, local1Z) && other.inside(local2X, local2Y, local2Z)) {
					return true
				}
			}
		}
	}

	return false
}

private fun separateSprites3D(sprite1: Sprite3D, sprite2: Sprite3D) {
	// overlap vector3
	val centerX1 = sprite1.x + sprite1.width / 2f
	val centerY1 = sprite1.y + sprite1.height / 2f
	val centerZ1 = sprite1.z + sprite1.depth / 2f
	val centerX2 = sprite2.x + sprite2.width / 2f
	val centerY2 = sprite2.y + sprite2.height / 2f
	val centerZ2 = sprite2.z + sprite2.depth / 2f

	val dx = centerX2 - centerX1
	val dy = centerY2 - centerY1
	val dz = centerZ2 - centerZ1
	val distance = sqrt(dx * dx + dy * dy + dz * dz)

	if (distance == 0f) return

	val nx = dx / distance
	val ny = dy / distance
	val nz = dz / distance

	// calculate minimum separation distance
	val halfWidth1 = (sprite1.hitbox.maxX - sprite1.hitbox.minX) / 2f
	val halfHeight1 = (sprite1.hitbox.maxY - sprite1.hitbox.minY) / 2f
	val halfDepth1 = (sprite1.hitbox.maxZ - sprite1.hitbox.minZ) / 2f
	val halfWidth2 = (sprite2.hitbox.maxX - sprite2.hitbox.minX) / 2f
	val halfHeight2 = (sprite2.hitbox.maxY - sprite2.hitbox.minY) / 2f
	val halfDepth2 = (sprite2.hitbox.maxZ - sprite2.hitbox.minZ) / 2f

	val projection1 = abs(nx * halfWidth1) + abs(ny * halfHeight1) + abs(nz * halfDepth1)
	val projection2 = abs(nx * halfWidth2) + abs(ny * halfHeight2) + abs(nz * halfDepth2)
	val minSeparation = projection1 + projection2

	val overlap = minSeparation - distance

	if (overlap > 0) {
		if (sprite1.static && sprite2.static) {
			// both static; don't move either
			return
		} else if (sprite1.static) {
			sprite2.x += nx * overlap
			sprite2.y += ny * overlap
			sprite2.z += nz * overlap
		} else if (sprite2.static) {
			sprite1.x -= nx * overlap
			sprite1.y -= ny * overlap
			sprite1.z -= nz * overlap
		} else {
			// both dynamic; distribute based on inverse mass
			val totalInvMass = 1f / sprite1.mass.toFloat() + 1f / sprite2.mass.toFloat()
			val push1 = overlap * (1f / sprite1.mass.toFloat()) / totalInvMass
			val push2 = overlap * (1f / sprite2.mass.toFloat()) / totalInvMass

			sprite1.x -= nx * push1
			sprite1.y -= ny * push1
			sprite1.z -= nz * push1
			sprite2.x += nx * push2
			sprite2.y += ny * push2
			sprite2.z += nz * push2
		}
	}
}

/**
 * A set of all 3D sprites that are currently colliding with this sprite.
 * Uses spatial hashing for O(n) average case complexity.
 *
 * This may not work if the engine is not running or the spatial grid has not been updated.
 */
val Sprite3D.collisions: Set<Sprite3D>
	get() {
		val collisions = mutableSetOf<Sprite3D>()
		val cells = getOccupiedCells3D(this)

		for (cell in cells) {
			val occupants = spatialGrid3D[cell] ?: continue

			for (other in occupants) {
				if (other === this) continue

				if (this.hitbox.isCollidingWith(other.hitbox, this, other)) {
					collisions.add(other)
				}
			}
		}

		return collisions
	}

// spins

private val pendingSpins2D = mutableMapOf<Uuid, Float>()
private val pendingSpins3D = mutableMapOf<Uuid, Triple<Float, Float, Float>>()
private val storedRotations3D = mutableMapOf<Uuid, Triple<Float, Float, Float>>()

/**
 * The stored rotation of the 3D sprite as Euler angles (pitch, yaw, roll) in radians.
 * This is the source of truth for rotation, separate from the transform matrix.
 */
var Sprite3D.storedRotation: Triple<Float, Float, Float>
	get() = storedRotations3D[id] ?: (0f to 0f to 0f)
	set(value) {
		// Normalize angles to [-π, π] range
		val normalizedPitch = normalizeAngle(value.first)
		val normalizedYaw = normalizeAngle(value.second)
		val normalizedRoll = normalizeAngle(value.third)

		storedRotations3D[id] = (normalizedPitch to normalizedYaw to normalizedRoll)
		// Update the actual transform to match
		transform = Matrix4.IDENTITY.rotate(normalizedPitch, normalizedYaw, normalizedRoll)
	}

/**
 * The spin decay factor per frame. Higher values = slower decay.
 */
var spinDecayFactor: Float = 0.97f
	set(value) {
		require(value in 0f..1f) { "Spin decay factor must be between 0 and 1" }
		field = value
	}

/**
 * Adds spin to a sprite that will be applied gradually over multiple frames.
 * @param degrees The amount of spin to add in degrees
 */
fun Sprite2D.lerpSpin(degrees: Float) {
	pendingSpins2D[id] = pendingSpins2D.getOrElse(id) { 0f } + degrees
}

/**
 * Adds rotation to a 3D sprite that will be applied gradually over multiple frames.
 * @param pitch The pitch rotation to add in radians
 * @param yaw The yaw rotation to add in radians
 * @param roll The roll rotation to add in radians
 */
fun Sprite3D.lerpRotation(pitch: Float, yaw: Float, roll: Float) {
	if (abs(pitch) < NORMAL_THRESHOLD && abs(yaw) < NORMAL_THRESHOLD && abs(roll) < NORMAL_THRESHOLD) return

	val current = pendingSpins3D.getOrElse(id) { Triple(0f, 0f, 0f) }
	pendingSpins3D[id] = Triple(
		current.first + pitch,
		current.second + yaw,
		current.third + roll
	)
}

private val targetRotations2D = mutableMapOf<Uuid, Float>()

/**
 * The target rotation steps to move toward then on the ground.
 *
 * When a sprite has a [spinFactor] greater than `0`, it will spin when colliding
 * with other objects or floors. This value represents the step to move toward its
 * default orientation. For example, the default value is `90`, which means that it
 * will try and flip itself up on either a `0`, `90`, `180`, or `270` degree axis
 * (ensuring that a rectangular object is flat on the ground).
 *
 * Setting this to `0` will make it stand back up on its original axis.
 */
var Sprite2D.targetRotation: Float
	get() = targetRotations2D[id] ?: 90f
	set(value) {
		if (value !in 0f..360f)
			throw IllegalArgumentException("Rotation must be in degrees, between 0F and 360F")

		targetRotations2D[id] = value
	}

private val targetRotations3D = mutableMapOf<Uuid, Triple<Float, Float, Float>>()

/**
 * The target rotation steps to move toward then on the ground.
 *
 * When a sprite has a [spinFactor] greater than `0`, it will spin when colliding
 * with other objects or floors. This value represents the step to move toward its
 * default orientation as a pair of `pitch` (X) to `roll` (Z). For example, the default value is `180` on XZ and `360` on Y, which means that it
 * will try and flip itself up on either a `0`, or `180` degree axis on both X and Z
 * (ensuring that a rectangular object is flat on the ground) and can face any direction on Y.
 *
 * Setting this to `0` to `0` to `0` will make it stand back up on its original axis.
 */
var Sprite3D.targetRotation: Triple<Float, Float, Float>
	get() = targetRotations3D[id] ?: (180f to 360f to 180f)
	set(value) {
		if (value.first !in 0f..360f || value.second !in 0f..360f || value.third !in 0f..360f)
			throw IllegalArgumentException("Rotation components must be in degrees, between 0F and 360F")

		targetRotations3D[id] = value
	}

/**
 * Advances the physics engine by one tick, updating the positions and velocities of all non-static objects
 * based on the applied forces such as gravity and friction.
 * @return A set of all [Positionable] objects that were moved during this tick.
 */
fun engineTick(): Set<Positionable> {
	// pre-sprite checks
	updateSpatialGrid()
	processedCollisionPairs2D.clear()
	processedCollisionPairs3D.clear()

	val changed = mutableSetOf<Positionable>()
	val dt = window.frameTime.coerceAtMost(0.03f)
	val fps = window.fps

	for (sprite in registeredSprites) {
		if (sprite.static) continue

		when (sprite) {
			is Sprite2D -> {
				val oldX = sprite.x
				val oldY = sprite.y

				val onGround = sprite.isBelow(groundY - 1f)

				// 1. apply gravity unless on ground
				sprite.ay -= gravity * dt

				// 2. apply friction if on ground
				if (onGround) {
					val normalForce = sprite.mass * gravity
					val frictionForce = sprite.frictionCoefficient * normalForce
					val frictionAccel = (frictionForce / sprite.mass) * dt * fps

					// 2a. horizontal friction
					if (sprite.vx > 0) {
						sprite.vx -= frictionAccel
						if (sprite.vx < 0) sprite.vx = 0.0
					} else if (sprite.vx < 0) {
						sprite.vx += frictionAccel
						if (sprite.vx > 0) sprite.vx = 0.0
					}

					// 2b. rotational friction
					val currentSpin = pendingSpins2D[sprite.id]
					if (currentSpin != null && abs(currentSpin) > NORMAL_THRESHOLD) {
						val velocityFactor = if (abs(sprite.vx) < NORMAL_THRESHOLD * 2) {
							spinDecayFactor / 1.15f
						} else {
							spinDecayFactor
						}

						val newSpin = currentSpin * velocityFactor
						if (abs(newSpin) < NORMAL_THRESHOLD) {
							pendingSpins2D.remove(sprite.id)
						} else {
							pendingSpins2D[sprite.id] = newSpin
						}
					}

					// 2c. torque to return to flat orientation
					var delta: Float
					if (sprite.targetRotation != 0f) {
						val targetRotation = round(sprite.rotation / sprite.targetRotation) * sprite.targetRotation
						delta = sprite.rotation - targetRotation
						while (delta > 180f) delta -= 360f
						while (delta < -180f) delta += 360f
					} else {
						delta = sprite.rotation
					}

					if (abs(delta) < snapThreshold) delta = 0f
					if (abs(delta) > NORMAL_THRESHOLD) {
						val restoreTorque = -delta * 0.2f
						sprite.lerpSpin(restoreTorque)
					}

					// 2d. stop vertical motion when on ground
					if (abs(sprite.vy) < NORMAL_THRESHOLD * 5) {
						sprite.vy = 0.0
						sprite.ay = 0.0
					}
				}

				// 3. apply acceleration to velocity
				sprite.vx += sprite.ax
				sprite.vy += sprite.ay

				// 3b. normalize vector values
				if (abs(sprite.vx) < NORMAL_THRESHOLD) sprite.vx = 0.0
				if (abs(sprite.vy) < NORMAL_THRESHOLD) sprite.vy = 0.0
				if (abs(sprite.ax) < NORMAL_THRESHOLD) sprite.ax = 0.0
				if (abs(sprite.ay) < NORMAL_THRESHOLD) sprite.ay = 0.0

				// 4. terminal velocity
				if (sprite.vy > terminalVelocity) sprite.vy = terminalVelocity.toDouble()
				if (sprite.vy < -terminalVelocity) sprite.vy = -terminalVelocity.toDouble()
				if (sprite.ay > terminalVelocity) sprite.ay = terminalVelocity.toDouble()
				if (sprite.ay < -terminalVelocity) sprite.ay = -terminalVelocity.toDouble()

				// 5. apply velocity to position
				sprite.x += (sprite.vx * dt).toFloat()
				sprite.y -= (sprite.vy * dt).toFloat() // Y is inverted

				// 6a. apply collisions with other sprites
				val collisions = sprite.collisions
				for (other in collisions) {
					val pair = if (sprite.hashCode() < other.hashCode()) {
						sprite to other
					} else {
						other to sprite
					}

					if (pair in processedCollisionPairs2D) continue
					processedCollisionPairs2D.add(pair)
					separateSprites2D(sprite, other)

					if (sprite.static && other.static) continue

					// calculate collision response
					val cx1 = sprite.x + sprite.width / 2f
					val cy1 = sprite.y + sprite.height / 2f
					val cx2 = other.x + other.width / 2f
					val cy2 = other.y + other.height / 2f

					val dx = cx2 - cx1
					val dy = cy2 - cy1
					val distance = sqrt(dx * dx + dy * dy).toDouble()

					// don't divide by tiny distances
					if (distance > 0.01) {
						val nx = (dx / distance)
						val ny = (dy / distance)

						val rvx = sprite.vx - other.vx
						val rvy = sprite.vy - other.vy

						val vn = rvx * nx + rvy * ny
						if (vn > 0) continue

						// calculate restitution
						val restitution = when {
							sprite.static -> other.restitutionCoefficient
							other.static -> sprite.restitutionCoefficient
							else -> (sprite.restitutionCoefficient + other.restitutionCoefficient) / 2.0
						}

						val impulseMag = -(1 + restitution) * vn / (1 / sprite.mass + 1 / other.mass)
						val ix = impulseMag * nx
						val iy = impulseMag * ny

						// apply impulses to velocities
						if (!sprite.static) {
							sprite.vx += ix / sprite.mass
							sprite.vy += iy / sprite.mass
						}

						if (!other.static) {
							other.vx -= ix / other.mass
							other.vy -= iy / other.mass
						}

						// apply spin from tangential velocity
						if (!sprite.static && sprite.spinFactor > 0) {
							val tangentialVel = rvx * (-ny) + rvy * nx
							sprite.lerpSpin((tangentialVel * sprite.spinFactor).toFloat())
						}

						if (!other.static && other.spinFactor > 0) {
							val tangentialVel = -rvx * (-ny) - rvy * nx
							other.lerpSpin((tangentialVel * other.spinFactor).toFloat())
						}
					}
				}

				// 6b. apply collision with X boundaries
				if (sprite.x + sprite.hitbox.minX < minX) {
					sprite.x = minX - sprite.hitbox.minX
					sprite.vx = -sprite.vx * sprite.restitutionCoefficient

					if (sprite.spinFactor > 0 && abs(sprite.vy) > NORMAL_THRESHOLD) {
						sprite.lerpSpin((-sprite.vy * sprite.spinFactor * 10f).toFloat())
					}
				} else if (sprite.x + sprite.hitbox.maxX > maxX) {
					sprite.x = maxX - sprite.hitbox.maxX
					sprite.vx = -sprite.vx * sprite.restitutionCoefficient

					if (sprite.spinFactor > 0 && abs(sprite.vy) > NORMAL_THRESHOLD) {
						sprite.lerpSpin((sprite.vy * sprite.spinFactor * 10f).toFloat())
					}
				}

				// 7. ensure sprite does not fall below ground level
				if (sprite.y + sprite.hitbox.maxY >= groundY) {
					sprite.y = groundY - sprite.hitbox.maxY

					if (sprite.vy < -NORMAL_THRESHOLD * 5) {
						sprite.vy = -sprite.vy * sprite.restitutionCoefficient

						if (sprite.spinFactor > 0 && abs(sprite.vx) > NORMAL_THRESHOLD) {
							sprite.lerpSpin((sprite.vx * sprite.spinFactor * 10f).toFloat())
						}
					} else {
						sprite.vy = 0.0
					}

					sprite.ay = 0.0
				}

				// 8. apply pending spins with decay
				val pendingSpin = pendingSpins2D[sprite.id]
				if (pendingSpin != null && abs(pendingSpin) > NORMAL_THRESHOLD) {
					val spinThisFrame = pendingSpin * (1f - spinDecayFactor)
					sprite.spin(spinThisFrame)

					val remainingSpin = pendingSpin * spinDecayFactor
					if (abs(remainingSpin) < NORMAL_THRESHOLD) {
						pendingSpins2D.remove(sprite.id)
					} else {
						pendingSpins2D[sprite.id] = remainingSpin
					}
				}

				// finish by checking if position changed
				if (sprite.x != oldX || sprite.y != oldY) {
					changed.add(sprite)
				}
			}

			is Sprite3D -> {
				val oldX = sprite.x
				val oldY = sprite.y
				val oldZ = sprite.z

				val onGround = sprite.isBelow(groundY + 0.3f)

				// 1. apply gravity unless on ground
				sprite.ay -= gravity * dt

				// 2. apply friction if on ground
				if (onGround) {
					val normalForce = sprite.mass * gravity
					val frictionForce = sprite.frictionCoefficient * normalForce
					val frictionAccel = (frictionForce / sprite.mass) * dt * fps

					if (sprite.vx > 0) {
						sprite.vx -= frictionAccel
						if (sprite.vx < 0) sprite.vx = 0.0
					} else if (sprite.vx < 0) {
						sprite.vx += frictionAccel
						if (sprite.vx > 0) sprite.vx = 0.0
					}

					if (sprite.vz > 0) {
						sprite.vz -= frictionAccel
						if (sprite.vz < 0) sprite.vz = 0.0
					} else if (sprite.vz < 0) {
						sprite.vz += frictionAccel
						if (sprite.vz > 0) sprite.vz = 0.0
					}

					// 2b. rotational friction
					val currentSpin = pendingSpins3D[sprite.id]
					if (currentSpin != null) {
						val (pitch, yaw, roll) = currentSpin

						val velocityFactor = if (abs(sprite.vx) < NORMAL_THRESHOLD * 2 && abs(sprite.vz) < NORMAL_THRESHOLD * 2) {
							spinDecayFactor / 1.2f
						} else {
							spinDecayFactor
						}

						if (abs(pitch) > NORMAL_THRESHOLD || abs(yaw) > NORMAL_THRESHOLD || abs(roll) > NORMAL_THRESHOLD) {
							pendingSpins3D[sprite.id] = Triple(
								pitch * velocityFactor,
								yaw * velocityFactor,
								roll * velocityFactor
							)
						} else {
							pendingSpins3D.remove(sprite.id)
						}
					}

					// 2c. torque to return to flat orientation
					val (storedPitch, storedYaw, storedRoll) = sprite.storedRotation
					val (tPitchDeg, tYawDeg, tRollDeg) = sprite.targetRotation

					// Convert stored rotation to degrees
					val pitchDeg = storedPitch * 180f / FPI
					val yawDeg = storedYaw * 180f / FPI
					val rollDeg = storedRoll * 180f / FPI

					// Calculate delta for pitch (exactly like 2D)
					var deltaPitch: Float = when (tPitchDeg) {
						0f -> pitchDeg
						360f -> 0f
						else -> {
							var d = pitchDeg - round(pitchDeg / tPitchDeg) * tPitchDeg
							while (d > 180f) d -= 360f
							while (d < -180f) d += 360f
							d
						}
					}

					// Calculate delta for yaw (exactly like 2D)
					var deltaYaw: Float = when (tYawDeg) {
						0f -> yawDeg
						360f -> 0f
						else -> {
							var d = yawDeg - round(yawDeg / tYawDeg) * tYawDeg
							while (d > 180f) d -= 360f
							while (d < -180f) d += 360f
							d
						}
					}

					// Calculate delta for roll (exactly like 2D)
					var deltaRoll: Float = when (tRollDeg) {
						0f -> rollDeg
						360f -> 0f
						else -> {
							var d = rollDeg - round(rollDeg / tRollDeg) * tRollDeg
							while (d > 180f) d -= 360f
							while (d < -180f) d += 360f
							d
						}
					}

					// Snap to 0 if close enough
					if (abs(deltaPitch) < snapThreshold) deltaPitch = 0f
					if (abs(deltaYaw) < snapThreshold) deltaYaw = 0f
					if (abs(deltaRoll) < snapThreshold) deltaRoll = 0f

					// Apply restoring torque if significant
					if (abs(deltaPitch) > NORMAL_THRESHOLD || abs(deltaYaw) > NORMAL_THRESHOLD || abs(deltaRoll) > NORMAL_THRESHOLD) {
						// If sprite is nearly stationary, directly apply correction
						if (abs(sprite.vx) < NORMAL_THRESHOLD * 2 && abs(sprite.vz) < NORMAL_THRESHOLD * 2) {
							// Direct correction when stationary
							val correctionFactor = 0.15f
							sprite.storedRotation = (
								storedPitch - deltaPitch * correctionFactor * FPI / 180f to
								storedYaw - deltaYaw * correctionFactor * FPI / 180f to
								storedRoll - deltaRoll * correctionFactor * FPI / 180f
							)
						} else {
							// Add torque when moving
							val restoreTorquePitch = -deltaPitch * 0.2f * FPI / 180f
							val restoreTorqueYaw = -deltaYaw * 0.2f * FPI / 180f
							val restoreTorqueRoll = -deltaRoll * 0.2f * FPI / 180f
							sprite.lerpRotation(restoreTorquePitch, restoreTorqueYaw, restoreTorqueRoll)
						}
					}

					// 2d. stop vertical motion when on ground
					if (abs(sprite.vy) < NORMAL_THRESHOLD * 5) {
						sprite.vy = 0.0
						sprite.ay = 0.0
					}
				}

				// 3. apply acceleration to velocity
				sprite.vx += sprite.ax
				sprite.vy += sprite.ay
				sprite.vz += sprite.az

				// 3b. normalize vector values
				if (abs(sprite.vx) < NORMAL_THRESHOLD) sprite.vx = 0.0
				if (abs(sprite.vy) < NORMAL_THRESHOLD) sprite.vy = 0.0
				if (abs(sprite.vz) < NORMAL_THRESHOLD) sprite.vz = 0.0
				if (abs(sprite.ax) < NORMAL_THRESHOLD) sprite.ax = 0.0
				if (abs(sprite.ay) < NORMAL_THRESHOLD) sprite.ay = 0.0
				if (abs(sprite.az) < NORMAL_THRESHOLD) sprite.az = 0.0

				// 4. terminal velocity
				if (sprite.vy > terminalVelocity) sprite.vy = terminalVelocity.toDouble()
				if (sprite.vy < -terminalVelocity) sprite.vy = -terminalVelocity.toDouble()
				if (sprite.ay > terminalVelocity) sprite.ay = terminalVelocity.toDouble()
				if (sprite.ay < -terminalVelocity) sprite.ay = -terminalVelocity.toDouble()

				// 5. apply velocity to position
				sprite.x += (sprite.vx * dt).toFloat()
				sprite.y += (sprite.vy * dt).toFloat()
				sprite.z += (sprite.vz * dt).toFloat()

				// 6a. apply collisions with other sprites
				val collisions = sprite.collisions
				for (other in collisions) {
					val pair = if (sprite.hashCode() < other.hashCode()) {
						sprite to other
					} else {
						other to sprite
					}

					if (pair in processedCollisionPairs3D) continue
					processedCollisionPairs3D.add(pair)
					separateSprites3D(sprite, other)

					if (sprite.static && other.static) continue

					// calculate collision response
					val cx1 = sprite.x + sprite.width / 2f
					val cy1 = sprite.y + sprite.height / 2f
					val cx2 = other.x + other.width / 2f
					val cy2 = other.y + other.height / 2f
					val cz1 = sprite.z + sprite.depth / 2f
					val cz2 = other.z + other.depth / 2f

					val dx = cx2 - cx1
					val dy = cy2 - cy1
					val dz = cz2 - cz1
					val distance = sqrt(dx * dx + dy * dy + dz * dz).toDouble()

					// don't divide by tiny distances
					if (distance > 0.01) {
						val nx = (dx / distance)
						val ny = (dy / distance)
						val nz = (dz / distance)

						val rvx = sprite.vx - other.vx
						val rvy = sprite.vy - other.vy
						val rvz = sprite.vz - other.vz

						val vn = rvx * nx + rvy * ny + rvz * nz
						if (vn > 0) continue

						// calculate restitution
						val restitution = when {
							sprite.static -> other.restitutionCoefficient
							other.static -> sprite.restitutionCoefficient
							else -> (sprite.restitutionCoefficient + other.restitutionCoefficient) / 2.0
						}

						val impulseMag = -(1 + restitution) * vn / (1 / sprite.mass + 1 / other.mass)
						val ix = impulseMag * nx
						val iy = impulseMag * ny
						val iz = impulseMag * nz

						// apply impulses to velocities
						if (!sprite.static) {
							sprite.vx += ix / sprite.mass
							sprite.vy += iy / sprite.mass
							sprite.vz += iz / sprite.mass
						}

						if (!other.static) {
							other.vx -= ix / other.mass
							other.vy -= iy / other.mass
							other.vz -= iz / other.mass
						}

						// apply rotation from tangential velocity
						if (!sprite.static && sprite.spinFactor > 0) {
							val tx = rvx - vn * nx
							val ty = rvy - vn * ny
							val tz = rvz - vn * nz
							val tangentialSpeed = sqrt(tx * tx + ty * ty + tz * tz)

							val rax = ny * tz - nz * ty
							val ray = nz * tx - nx * tz
							val raz = nx * ty - ny * tx
							val axisMag = sqrt(rax * rax + ray * ray + raz * raz)

							if (tangentialSpeed > 0 && axisMag > 0) {
								val angle = tangentialSpeed * sprite.spinFactor * 0.01
								val pitch = ((rax / axisMag) * angle).toFloat()
								val yaw = ((ray / axisMag) * angle).toFloat()
								val roll = ((raz / axisMag) * angle).toFloat()
								sprite.lerpRotation(pitch, yaw, roll)
							}
						}

						if (!other.static && other.spinFactor > 0) {
							val tx = -rvx + vn * nx
							val ty = -rvy + vn * ny
							val tz = -rvz + vn * nz
							val tangentialSpeed = sqrt(tx * tx + ty * ty + tz * tz)

							val rax = ny * tz - nz * ty
							val ray = nz * tx - nx * tz
							val raz = nx * ty - ny * tx
							val axisMag = sqrt(rax * rax + ray * ray + raz * raz)

							if (tangentialSpeed > 0 && axisMag > 0) {
								val angle = tangentialSpeed * other.spinFactor * 0.01
								val pitch = ((rax / axisMag) * angle).toFloat()
								val yaw = ((ray / axisMag) * angle).toFloat()
								val roll = ((raz / axisMag) * angle).toFloat()
								other.lerpRotation(pitch, yaw, roll)
							}
						}
					}
				}

				// 6b. apply collision with X boundaries
				if (sprite.x + sprite.hitbox.minX < minX) {
					sprite.x = minX - sprite.hitbox.minX
					sprite.vx = -sprite.vx * sprite.restitutionCoefficient

					if (sprite.spinFactor > 0) {
						val yaw = (sprite.vz * sprite.spinFactor * 10f).toFloat()
						val roll = (-sprite.vy * sprite.spinFactor * 10f).toFloat()
						sprite.lerpRotation(0f, yaw, roll)
					}
				} else if (sprite.x + sprite.hitbox.maxX > maxX) {
					sprite.x = maxX - sprite.hitbox.maxX
					sprite.vx = -sprite.vx * sprite.restitutionCoefficient

					if (sprite.spinFactor > 0) {
						val yaw = (-sprite.vz * sprite.spinFactor * 10f).toFloat()
						val roll = (sprite.vy * sprite.spinFactor * 10f).toFloat()
						sprite.lerpRotation(0f, yaw, roll)
					}
				}

				// 6c. apply collision with Z boundaries
				if (sprite.z + sprite.hitbox.minZ < minZ) {
					sprite.z = minZ - sprite.hitbox.minZ
					sprite.vz = -sprite.vz * sprite.restitutionCoefficient

					if (sprite.spinFactor > 0) {
						val pitch = (-sprite.vy * sprite.spinFactor * 10f).toFloat()
						val yaw = (sprite.vx * sprite.spinFactor * 10f).toFloat()
						sprite.lerpRotation(pitch, yaw, 0f)
					}
				} else if (sprite.z + sprite.hitbox.maxZ > maxZ) {
					sprite.z = maxZ - sprite.hitbox.maxZ
					sprite.vz = -sprite.vz * sprite.restitutionCoefficient

					if (sprite.spinFactor > 0) {
						val pitch = (sprite.vy * sprite.spinFactor * 10f).toFloat()
						val yaw = (-sprite.vx * sprite.spinFactor * 10f).toFloat()
						sprite.lerpRotation(pitch, yaw, 0f)
					}
				}

				// 7. ensure sprite does not fall below ground level
				if (sprite.y + sprite.hitbox.minY <= groundY) {
					sprite.y = groundY - sprite.hitbox.minY

					if (sprite.vy < -NORMAL_THRESHOLD * 5) {
						sprite.vy = -sprite.vy * sprite.restitutionCoefficient

						if (sprite.spinFactor > 0 && (abs(sprite.vx) > NORMAL_THRESHOLD || abs(sprite.vz) > NORMAL_THRESHOLD)) {
							val pitch = (sprite.vz * sprite.spinFactor * 10f).toFloat()
							val roll = (-sprite.vx * sprite.spinFactor * 10f).toFloat()
							sprite.lerpRotation(pitch, 0f, roll)
						}
					} else {
						sprite.vy = 0.0
					}

					sprite.ay = 0.0
				}

				// 8. apply pending rotations with decay
				val pendingSpin = pendingSpins3D[sprite.id]
				if (pendingSpin != null) {
					val (pitch, yaw, roll) = pendingSpin

					if (abs(pitch) > NORMAL_THRESHOLD || abs(yaw) > NORMAL_THRESHOLD || abs(roll) > NORMAL_THRESHOLD) {
						val pitchFrame = pitch * (1f - spinDecayFactor)
						val yawFrame = yaw * (1f - spinDecayFactor)
						val rollFrame = roll * (1f - spinDecayFactor)

						// Apply to stored rotation instead of transform
						val (currentPitch, currentYaw, currentRoll) = sprite.storedRotation
						sprite.storedRotation = (
							currentPitch + pitchFrame to
							currentYaw + yawFrame to
							currentRoll + rollFrame
						)

						val remainingPitch = pitch * spinDecayFactor
						val remainingYaw = yaw * spinDecayFactor
						val remainingRoll = roll * spinDecayFactor

						if (abs(remainingPitch) < NORMAL_THRESHOLD &&
							abs(remainingYaw) < NORMAL_THRESHOLD &&
							abs(remainingRoll) < NORMAL_THRESHOLD) {
							pendingSpins3D.remove(sprite.id)
						} else {
							pendingSpins3D[sprite.id] = Triple(remainingPitch, remainingYaw, remainingRoll)
						}
					}
				}

				// finish by checking if position changed
				if (sprite.x != oldX || sprite.y != oldY || sprite.z != oldZ) {
					changed.add(sprite)
				}
			}
		}
	}

	return changed
}
