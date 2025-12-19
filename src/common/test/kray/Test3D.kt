package kray

import kray.shaders.LIGHTING_SHADER
import raylib.*
import raylib.Shader.UniformLocation
import kotlin.test.Test
import kotlin.test.assertFalse

class Test3D {

	@Test
	fun testMesh() {
		if (Window.isHeadless) return
		assertFalse { Window.isHeadless }

		Window.open(800, 600, "Test Mesh")

		val shader = LIGHTING_SHADER
		shader.setDefaultLocations(UniformLocation.MATRIX_MVP, UniformLocation.VECTOR_VIEW)
		shader.setValue("ambient", floatArrayOf(0.2F, 0.2F, 0.2F, 1.0F))
		shader.addLight(10F, 8F, 6F)

		val material = Material.default()
		material.shader = shader

		val camera = Camera3D(
			20f to 30f to 20f,
			0f to 0f to 0f,
			0f to 1f to 0f,
			45f,
			CameraProjection3D.PERSPECTIVE
		)

		val cube = Mesh.cube(5F)
		val cone = Mesh.cone(2F, 5F)

		Window.fps = 60
		Window.lifecycleForFrames(60 * 5) {
			camera.update(CameraMode3D.ORBITAL)

			shader.setValue(shader.getDefaultLocation(UniformLocation.VECTOR_VIEW), camera.position)
			Canvas.draw {
				setBackgroundColor(Color.GRAY)

				camera3D(camera) {
					material.setMapColor(MaterialMap.Texture.ALBEDO, Color.RED)
					drawMesh(cube, material, 0, 0, 0)
					material.setMapColor(MaterialMap.Texture.ALBEDO, Color.BLUE)
					drawMesh(cone, material, 0, 5, 0)
					material.setMapColor(MaterialMap.Texture.ALBEDO, Color.GREEN)
					drawMesh(cube, material, 0, 12, 0)

					grid() // not visible with shader
				}
			}
		}
	}

	// Utility functions for adding lights to the LIGHTING_SHADER

	/**
	 * The maximum number of lights supported in [LIGHTING_SHADER].
	 */
	val maxLights = 4
	var currentLightsCount = 0

	/**
	 * Adds a light to the shader.
	 * @param position The position of the light in 3D space.
	 * @param target The target point the light is pointing to.
	 * @param color The color of the light. Defaults to white.
	 * @param directional Whether the light is directional or point light.
	 * If the light is directional, the position is treated as a direction vector.
	 * If the light is a point, the light will attenuate with distance.
	 */
	private fun Shader.addLight(
		position: Triple<Float, Float, Float>,
		target: Triple<Float, Float, Float> = 0F to 0F to 0F,
		color: Color = Color.WHITE,
		directional: Boolean = true,
	) {
		setValue("lights[$currentLightsCount].enabled", true)
		setValue("lights[$currentLightsCount].position", position)
		setValue("lights[$currentLightsCount].target", target)
		setValue("lights[$currentLightsCount].color", color)
		setValue("lights[$currentLightsCount].directional", directional)
	}

	/**
	 * Adds a light to the shader using individual float parameters for position and target.
	 * @param x The x-coordinate of the light's position.
	 * @param y The y-coordinate of the light's position.
	 * @param z The z-coordinate of the light's position.
	 * @param targetX The x-coordinate of the light's target point.
	 * @param targetY The y-coordinate of the light's target point.
	 * @param targetZ The z-coordinate of the light's target point.
	 * @param color The color of the light. Defaults to white.
	 * @param directional Whether the light is directional or point light.
	 * If the light is directional, the position is treated as a direction vector.
	 * If the light is a point, the light will attenuate with distance.
	 */
	private fun Shader.addLight(
		x: Float,
		y: Float,
		z: Float,
		targetX: Float = 0F,
		targetY: Float = 0F,
		targetZ: Float = 0F,
		color: Color = Color.WHITE,
		directional: Boolean = true
	) = addLight(
		x to y to z,
		targetX to targetY to targetZ,
		color,
		directional
	)
}
