package raylib

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TestImage {

	@Test
	fun testColor() {
		val color1 = Color.WHITE
		assertEquals(255.toUByte(), color1.r)
		assertEquals(255.toUByte(), color1.g)
		assertEquals(255.toUByte(), color1.b)
		assertEquals(255.toUByte(), color1.a)
		assertEquals(16777215, color1.rgb)
		assertEquals(4294967295, color1.rgba)
		assertEquals(4294967295, color1.argb)
		assertEquals("ffffff", color1.hex6)
		assertEquals("ffffffff", color1.hex8)

		val color2 = Color.GOLD.transparent(0.5F)
		assertEquals(255.toUByte(), color2.r)
		assertEquals(215.toUByte(), color2.g)
		assertEquals(0.toUByte(), color2.b)
		assertEquals(127.toUByte(), color2.a)
		assertEquals(16766720, color2.rgb)
		assertEquals(4292280447, color2.rgba)
		assertEquals(2147473152, color2.argb)
		assertEquals("ffd700", color2.hex6)
		assertEquals("7fffd700", color2.hex8)
	}

	@Test
	fun testImage() {
		val image1 = Image.fromColor(10, 10, Color.RED)
		assertEquals(10, image1.width)
		assertEquals(10, image1.height)
		assertEquals(Color.RED, image1[1, 1])
		assertEquals(100, image1.getColors().size)
		assertEquals(PictureFormat.UNCOMPRESSED_R8G8B8A8, image1.format)
		image1.unload()

		val image2 = Image.fromText(15, 10, "Hello World")
		assertEquals(15, image2.width)
		assertEquals(10, image2.height)
		assertEquals(PictureFormat.UNCOMPRESSED_GRAYSCALE, image2.format)
		val image20 = image2.rotateCW().apply {
			assertEquals(PictureFormat.UNCOMPRESSED_GRAYSCALE, this.format)
			assertEquals(10, this.width)
			assertEquals(15, this.height)
		}
		image20.unload()

		// don't unload images loaded from file
		val image3 = Image.load("calcugames.png")
		assertEquals(460, image3.width)
		assertEquals(460, image3.height)
		assertEquals(PictureFormat.UNCOMPRESSED_R8G8B8A8, image3.format)

		val file = File("calcugames_rotated.png")
		val image4 = image3.rotateCCW()
			.rect(10, 10, 100, 100, 3, Color.RED)
			.fillRect(13, 13, 94, 94, Color.RED_BROWN)
		image4.writeTo(file)
		assertTrue { file.exists() }
		assertEquals(460, image4.width)
		assertEquals(460, image4.height)
		assertEquals(Color.RED, image4[12, 12])
		file.delete()
		image4.unload()
	}

}
