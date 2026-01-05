package raylib

import kray.to
import kotlin.test.Test
import kotlin.test.assertFalse

class Test3D {

	@Test
	fun testCanvas3D() {
		if (Window.isHeadless) return
		assertFalse { Window.isHeadless }

		Window.open(800, 600, "Test Canvas 3D")

		val camera = Camera3D(
			0f to 80f to 50f,
			0f to 0f to 0f,
			0f to 1f to 0f,
			45f,
			CameraProjection3D.PERSPECTIVE
		)

		Window.fps = 60

		Canvas.draw {
			setBackgroundColor(Color.WHITE)

			camera3D(camera) {
				// Test 3D point
				draw(0, 0, 0, Color.RED)

				// Test 3D lines
				line3(10, 10, 10, 20, 20, 20, Color.GREEN)

				// Test 3D circle
				circle3(0f, 0f, 0f, 5f, 1f, 0f, 0f, 0f, Color.BLUE)

				// Test 3D triangle
				triangle3(0f, 0f, 0f, 5f, 0f, 0f, 2f, 5f, 0f, Color.YELLOW)
				triangleStrip3(Color.ORANGE, Triple(0f, 0f, 0f), Triple(5f, 0f, 0f), Triple(2f, 5f, 0f), Triple(7f, 5f, 0f))

				// Test rectangular prism
				rectPrism(0f, 0f, 0f, 5f, 5f, 5f, Color.PURPLE)
				fillRectPrism(10f, 0f, 0f, 5f, 5f, 5f, Color.MAGENTA)

				// Test sphere
				sphere(0f, 10f, 0f, 3f, Color.CYAN)
				fillSphere(10f, 10f, 0f, 3f, Color.PINK)
				fillSphere(20f, 10f, 0f, 3f, 16, 16, Color.LIME)

				// Test cylinder (wireframe and filled)
				cylinder(0f, 20f, 0f, 0f, 25f, 0f, 2f, 2f, 8, Color.BROWN)
				fillCylinder(10f, 20f, 0f, 10f, 25f, 0f, 2f, 2f, 8, Color.VIOLET)

				// Test capsule
				capsule(0f, 30f, 0f, 0f, 35f, 0f, 2f, 8, 8, Color.BLUE)
				fillCapsule(10f, 30f, 0f, 10f, 35f, 0f, 2f, 8, 8, Color.GREEN)

				// Test plane/rect3
				rect3(0f, 40f, 0f, 10f, 8f, Color.GOLD)

				// Test grid
				grid(10, 1f)
			}
		}

		// Hold for a moment to view
		Window.delaySync(3000)

		Window.close()
	}

}
