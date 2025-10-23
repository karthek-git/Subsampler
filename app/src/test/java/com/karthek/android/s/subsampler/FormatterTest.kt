package com.karthek.android.s.subsampler

import com.karthek.android.s.subsampler.helper.formatFileSize
import org.junit.Assert.assertEquals
import org.junit.Test

class FormatterTest {

	@Test
	fun floatArraysFromByteArrays() {
		assertEquals("900 B", formatFileSize(900))
		assertEquals("9 KB", formatFileSize(1024*9-9))
		assertEquals("9 MB", formatFileSize(1024*1024*9))
		assertEquals("9 GB", formatFileSize(9663676416))
		assertEquals("9216 GB", formatFileSize(1024*1024*1024*1024*9))
	}
}