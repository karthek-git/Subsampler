package com.karthek.android.s.subsampler

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.IOException


suspend fun subSample(
	origSize: Long,
	reqSize: Long,
	inputStream: FileInputStream,
	outputStream: ByteArrayOutputStream,
): Long {
	var gotSize = origSize
	try {
		withContext(Dispatchers.IO) {
			inputStream.channel.position(0)
		}
	} catch (e: IOException) {
		e.printStackTrace()
	}
	val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
	var quality = 100
	while (gotSize > reqSize && quality > 0) {
		quality -= 1
		outputStream.reset()
		bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
		gotSize = outputStream.size().toLong()
	}
	return gotSize
}

fun getSaveFileName(fileName: String?, reqSize: Long): String? {
	return fileName?.let {
		"${fileName.substring(0, fileName.lastIndexOf('.'))}-$reqSize.jpg"
	}
}