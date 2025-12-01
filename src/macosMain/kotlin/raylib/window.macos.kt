@file:OptIn(ExperimentalForeignApi::class)

package raylib

import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreGraphics.CGMainDisplayID
import platform.CoreGraphics.CGSessionCopyCurrentDictionary
import platform.CoreGraphics.kCGNullDirectDisplay

internal actual val _isHeadless: Boolean
	get() = CGMainDisplayID() == kCGNullDirectDisplay || CGSessionCopyCurrentDictionary() == null
