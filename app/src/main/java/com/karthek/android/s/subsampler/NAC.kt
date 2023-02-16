package com.karthek.android.s.subsampler

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import com.karthek.android.s.subsampler.ml.model.FaceDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.IOException

class NAC(private val faceDetector: FaceDetector) {
	suspend fun compress(
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
		val bitmap = BitmapFactory.decodeStream(inputStream)
		val bitmapOpts = BitmapFactory.Options().apply { inMutable = true }
		val paint = Paint()
		val faceBitmaps = faceDetector.getFaceBitmaps(bitmap)
		var quality = 100
		while (gotSize > reqSize && quality > 0) {
			quality -= 1
			outputStream.reset()
			bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
			val compressedBitmap = BitmapFactory.decodeByteArray(
				outputStream.toByteArray(), 0, outputStream.size(), bitmapOpts
			)
			faceBitmaps.forEach { faceBitmap ->
				Canvas(compressedBitmap).drawBitmap(
					faceBitmap.bitmap, faceBitmap.left, faceBitmap.top, paint
				)
			}
			outputStream.reset()
			val fq = quality.coerceAtLeast(80)
			compressedBitmap.compress(Bitmap.CompressFormat.JPEG, fq, outputStream)
			gotSize = outputStream.size().toLong()
		}
		return gotSize
	}
}