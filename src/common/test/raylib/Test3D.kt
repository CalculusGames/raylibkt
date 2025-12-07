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
				circle3(0, 0, 0, 5f, 1f, 0f, 0f, 0f, Color.BLUE)

				// Test 3D triangle
				triangle3(0, 0, 0, 5, 0, 0, 2, 5, 0, Color.YELLOW)
				triangleStrip3(Color.ORANGE, Triple(0, 0, 0), Triple(5, 0, 0), Triple(2, 5, 0), Triple(7, 5, 0))

				// Test rectangular prism
				rectPrism(0, 0, 0, 5f, 5f, 5f, Color.PURPLE)
				fillRectPrism(10, 0, 0, 5f, 5f, 5f, Color.MAGENTA)

				// Test sphere
				sphere(0, 10, 0, 3f, Color.CYAN)
				fillSphere(10, 10, 0, 3f, Color.PINK)
				fillSphere(20, 10, 0, 3f, 16, 16, Color.LIME)

				// Test cylinder (wireframe and filled)
				cylinder(0, 20, 0, 0, 25, 0, 2f, 2f, 8, Color.BROWN)
				fillCylinder(10, 20, 0, 10, 25, 0, 2f, 2f, 8, Color.VIOLET)

				// Test capsule
				capsule(0, 30, 0, 0, 35, 0, 2f, 8, 8, Color.BLUE)
				fillCapsule(10, 30, 0, 10, 35, 0, 2f, 8, 8, Color.GREEN)

				// Test plane/rect3
				rect3(0, 40, 0, 10f, 8f, Color.GOLD)

				// Test grid
				grid(10, 1f)
			}
		}

		// Hold for a moment to view
		Window.delay(3000)

		Window.close()
	}

}
