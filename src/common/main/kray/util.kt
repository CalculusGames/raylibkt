@file:OptIn(ExperimentalForeignApi::class)

package kray

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.reinterpret

/**
 * Converts a [COpaquePointer] (aka `void *`) to an unsigned byte array.
 * @param size The size of the dynamic array.
 * @return An unsigned byte array from the pointer
 */
fun COpaquePointer.toByteArray(size: Int): ByteArray {
	val ptr = reinterpret<ByteVar>()
	return ByteArray(size) { i -> ptr[i] }
}
