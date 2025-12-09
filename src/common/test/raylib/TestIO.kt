package raylib

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TestIO {

	@Test
	fun testFile() {
		val file1 = File("data.txt")
		file1.writeText("Hello World")
		assertTrue { file1.exists() }
		assertTrue { file1.isFile }
		assertFalse { file1.isDirectory }
		assertTrue { file1.absolutePath.endsWith("data.txt") }
		assertEquals("Hello World", file1.readText())
		assertFalse { file1.readBytes().isEmpty() }
		assertTrue { file1.files().isEmpty() }
		assertTrue { file1.delete() }
		assertFalse { file1.exists() }

		val file2 = File("testdir")
		file2.mkdir()
		assertTrue { file2.exists() }
		assertFalse { file2.isFile }
		assertTrue { file2.isDirectory }
		assertTrue { file2.files().isEmpty() }
		assertTrue { file2.delete() }
		assertFalse { file2.exists() }

		val file3 = File("testdir2").apply { mkdir() }
		assertTrue { file3.exists() }
		assertTrue { file3.isDirectory }
		File(file3, "a.txt").apply {
			writeText("Hello World")
			assertTrue { exists() }
			assertEquals("Hello World", readText())
			assertFalse { readBytes().isEmpty() }
		}
		File(file3, "b.txt").apply {
			writeText("Hello World, again")
			assertTrue { exists() }
			assertEquals("Hello World, again", readText())
			assertFalse { readBytes().isEmpty() }
		}
		assertEquals(2, file3.files().size)
		assertTrue { file3.deleteRecursively() }
		assertFalse { file3.exists() }

		val file4 = File("calcugames.png")
		assertTrue { file4.exists() }
		assertTrue { file4.isFile }
		assertFalse { file4.readBytes().isEmpty() }
		File("calcugames.h").apply {
			assertTrue { file4.writeCode(this) }
			assertTrue { exists() }
			assertNotNull(readText())
			assertTrue { delete() }
		}
	}

	@Test
	fun testBytes() {
		val text = "Security is important! " +
			"Keep your data safe and secure. " +
			"With proper encryption and best practices, " +
			"you can protect sensitive information from unauthorized access."

		val bytes = text.encodeToByteArray()
		val ubytes = UByteArray(64) { (it * 2).toUByte() } +
			UByteArray(64) { (it * 3).toUByte() } +
			UByteArray(64) { (it * 4).toUByte() } +
			UByteArray(64)

		// Compression
		val compressed1 = bytes.compress()
		assertFalse { compressed1.isEmpty() }
		assertTrue { compressed1.size < bytes.size }
		val decompressed1 = compressed1.decompress()
		assertEquals(bytes.size, decompressed1.size)
		assertTrue { bytes.contentEquals(decompressed1) }

		val compressed2 = ubytes.compress()
		assertFalse { compressed2.isEmpty() }
		assertTrue { compressed2.size < ubytes.size }
		val decompressed2 = compressed2.decompress()
		assertEquals(ubytes.size, decompressed2.size)
		assertTrue { ubytes.contentEquals(decompressed2) }

		// Base64
		val base641 = bytes.toBase64()
		assertNotNull(base641)
		assertFalse { base641.isEmpty() }
		val decoded1 = base641.fromBase64()
		assertEquals(bytes.size, decoded1.size)
		assertTrue { bytes.contentEquals(decoded1) }

		val base642 = ubytes.toBase64()
		assertNotNull(base642)
		assertFalse { base642.isEmpty() }
		val decoded2 = base642.fromBase64U()
		assertEquals(ubytes.size, decoded2.size)
		assertTrue { ubytes.contentEquals(decoded2) }

		// Checksums
		val crc1 = bytes.crc32()
		assertEquals(0xA271149Du, crc1)
		val crc2 = ubytes.crc32()
		assertEquals(0x60555F71u, crc2)

		val md51 = bytes.md5()
		assertEquals("0f617fb820484ae7ad0e53ec17b7cf32", md51)
		val md52 = ubytes.md5()
		assertEquals("2d01fd2427dbb5d746ca2acc5c4b3cdf", md52)

//		https://github.com/raysan5/raylib/pull/5397
//		val sha11 = bytes.sha1()
//		assertEquals("2c0f22ce3a9ee65c7f0ccca1ecb5e5b53c8fa6bb", sha11)
//		val sha12 = ubytes.sha1()
//		assertEquals("9a4026e4e27148c6f7177f9001f3630e7eb291f1", sha12)
	}

}
