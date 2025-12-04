@file:OptIn(ExperimentalForeignApi::class)

package kray

import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret
import platform.darwin.ByteVar

/**
 * Converts a [COpaquePointer] (aka `void *`) to an unsigned byte array.
 * @param size The size of the dynamic array.
 * @return An unsigned byte array from the pointer
 */
fun COpaquePointer.toByteArray(size: Int): UByteArray {
	val ptr = reinterpret<ByteVar>()
	return UByteArray(size) { i -> ptr[i] }
}
