@file:OptIn(ExperimentalForeignApi::class)

package raylib

import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.nativeHeap
import kotlinx.cinterop.ptr
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import kray.toVector2
import raylib.internal.*

/**
 * Represents a font in raylib.
 */
class Font(internal val raw: raylib.internal.Font) {

	/**
	 * The base size of the font.
	 */
	val baseSize: Int
		get() = raw.baseSize

	/**
	 * The number of glyphs in the font.
	 */
	val glyphCount: Int
		get() = raw.glyphCount

	/**
	 * The glyph padding in the font.
	 */
	val glyphPadding: Int
		get() = raw.glyphPadding

	/**
	 * The texture associated with the font.
	 */
	val texture: Texture2D
		get() = Texture2D(raw.texture)

	companion object {

		private fun from(value: CValue<raylib.internal.Font>): Font {
			val ptr = nativeHeap.alloc<raylib.internal.Font>()
			value.useContents {
				ptr.texture.height = texture.height
				ptr.texture.width = texture.width
				ptr.texture.id = texture.id
				ptr.texture.mipmaps = texture.mipmaps
				ptr.texture.format = texture.format

				ptr.baseSize = baseSize
				ptr.glyphPadding = glyphPadding
				ptr.glyphCount = glyphCount
				ptr.glyphs = glyphs
				ptr.recs = recs
			}

			return Font(ptr)
		}

		/**
		 * Gets the default font.
		 * @return The default font.
		 */
		fun default(): Font {
			val fontValue = GetFontDefault()
			val ptr = nativeHeap.alloc<raylib.internal.Font>()
			fontValue.place(ptr.ptr)
			return Font(ptr)
		}

		/**
		 * Loads a font from the specified path.
		 * @param path The path to the font file, relative to [appDir].
		 * @return The loaded font.
		 */
		fun load(path: String): Font {
			val fontValue = LoadFont(path.inAppDir())
			val ptr = nativeHeap.alloc<raylib.internal.Font>()
			fontValue.place(ptr.ptr)
			return Font(ptr)
		}

		/**
		 * Loads a font from the specified file.
		 * @param file The file to load the font from.
		 * @return The loaded font.
		 */
		fun load(file: File) = load(file.absolutePath)

		/**
		 * Loads a font from an image.
		 * @param image The image to load the font from.
		 * @param key The color key to use for transparency.
		 * @param firstChar The first character in the font (ASCII code).
		 * @return The loaded font.
		 */
		fun fromImage(image: Image, key: Color, firstChar: Int): Font {
			val fontValue = LoadFontFromImage(image.raw, key.raw(), firstChar)
			val ptr = nativeHeap.alloc<raylib.internal.Font>()
			fontValue.place(ptr.ptr)
			return Font(ptr)
		}
	}

	/**
	 * Whether the font is valid.
	 */
	val isValid: Boolean
		get() = IsFontValid(raw.readValue())

	/**
	 * Unloads the font from memory.
	 */
	fun unload() {
		UnloadFont(raw.readValue())
		nativeHeap.free(raw.rawPtr)
	}

	override fun toString(): String {
		return "Font(baseSize=$baseSize, glyphCount=$glyphCount)"
	}

}

/**
 * Draws text on the given canvas.
 * @param x The X coordinate to draw the text at.
 * @param y The Y coordinate to draw the text at.
 * @param text The text to draw.
 * @param color The color of the text. Default is white.
 * @param fontSize The size of the font. Default is the base size of the font.
 * @param font The font to use. Default is the default font.
 * @param spacing The spacing between characters. Default is 1.
 * @param rotationAxis The coordinates of the rotation origin. Default is 0.
 * @param rotation The rotation angle in degrees. Default is 0.
 */
fun Canvas.drawText(
	position: Pair<Float, Float>,
	text: String,
	color: Color = Color.WHITE,
	fontSize: Int = 12,
	font: Font = Font.default(),
	spacing: Float = 1F,
	rotationAxis: Pair<Float, Float> = 0F to 0F,
	rotation: Float = 0F,
) {
	ensureDrawing()
	DrawTextPro(
		font.raw.readValue(),
		text,
		position.toVector2(),
		rotationAxis.toVector2(),
		rotation,
		fontSize.toFloat(),
		spacing,
		color.raw()
	)
}

/**
 * Draws text on the given canvas.
 * @param x The X coordinate to draw the text at.
 * @param y The Y coordinate to draw the text at.
 * @param text The text to draw.
 * @param color The color of the text. Default is white.
 * @param fontSize The size of the font. Default is the base size of the font.
 * @param font The font to use. Default is the default font.
 * @param spacing The spacing between characters. Default is 0.
 * @param rotX The X coordinate of the rotation origin. Default is 0.
 * @param rotY The Y coordinate of the rotation origin. Default is 0.
 * @param rotation The rotation angle in degrees. Default is 0.
 */
fun Canvas.drawText(
	x: Float,
	y: Float,
	text: String,
	color: Color = Color.WHITE,
	fontSize: Int = 12,
	font: Font = Font.default(),
	spacing: Float = 1F,
	rotX: Float = 0F,
	rotY: Float = 0F,
	rotation: Float = 0F,
) {
	drawText(x to y, text,color, fontSize, font, spacing, rotX to rotY, rotation)
}

/**
 * Draws text on the given canvas.
 * @param x The X coordinate to draw the text at.
 * @param y The Y coordinate to draw the text at.
 * @param text The text to draw.
 * @param color The color of the text. Default is white.
 * @param fontSize The size of the font. Default is the base size of the font.
 * @param font The font to use. Default is the default font.
 * @param spacing The spacing between characters. Default is 0.
 * @param rotX The X coordinate of the rotation origin. Default is 0.
 * @param rotY The Y coordinate of the rotation origin. Default is 0.
 * @param rotation The rotation angle in degrees. Default is 0.
 */
fun Canvas.drawText(
	x: Int,
	y: Int,
	text: String,
	color: Color = Color.WHITE,
	fontSize: Int = 12,
	font: Font = Font.default(),
	spacing: Float = 1F,
	rotX: Float = 0F,
	rotY: Float = 0F,
	rotation: Float = 0F,
) {
	drawText(x.toFloat(), y.toFloat(), text, color, fontSize, font, spacing, rotX, rotY, rotation)
}

/**
 * Sets the line spacing for text rendering on the given canvas.
 * @param spacing The line spacing to set.
 */
fun Canvas.setLineSpacing(spacing: Int) {
	SetTextLineSpacing(spacing)
}
