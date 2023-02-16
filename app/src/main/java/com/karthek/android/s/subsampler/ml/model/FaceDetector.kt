package com.karthek.android.s.subsampler.ml.model


import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.formats.proto.DetectionProto
import com.google.mediapipe.solutions.facedetection.FaceDetection
import com.google.mediapipe.solutions.facedetection.FaceDetectionOptions
import com.google.mediapipe.solutions.facedetection.FaceDetectionResult
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FaceDetector(context: Context) {
	private val faceDetection: FaceDetection

	init {
		val faceDetectionOptions = FaceDetectionOptions.builder()
			.setStaticImageMode(true)
			.setModelSelection(1)
			.build()
		faceDetection = FaceDetection(context, faceDetectionOptions)
	}

	suspend fun getFaceBitmaps(bitmap: Bitmap): List<FaceBitmap> {
		return suspendCoroutine { continuation ->
			faceDetection.send(bitmap)
			faceDetection.setResultListener { result ->
				continuation.resume(getFaceBitmaps(bitmap, result))
			}
		}
	}

	private fun getFaceBitmaps(bitmap: Bitmap, result: FaceDetectionResult): List<FaceBitmap> {
		val faceBitmaps = mutableListOf<FaceBitmap>()
		val detectionList = result.multiFaceDetections()
		if (detectionList.isEmpty()) return faceBitmaps
		detectionList.forEach { detection ->
			try {
				faceBitmaps.add(getFaceBitmap(bitmap, detection))
			} catch (e: Exception) {
				e.printStackTrace()
			}
		}
		return faceBitmaps
	}

	private fun getFaceBitmap(bitmap: Bitmap, detection: DetectionProto.Detection): FaceBitmap {
		val width = bitmap.width
		val height = bitmap.height
		val boundingBox = detection.locationData.relativeBoundingBox
		val left = ((boundingBox.xmin) * width).toInt().coerceIn(0, width)
		val top = ((boundingBox.ymin) * height).toInt().coerceIn(0, height)
		val right = ((boundingBox.width) * width).toInt().coerceAtMost(width - left)
		val bottom = ((boundingBox.height) * height).toInt().coerceAtMost(height - top)
		return FaceBitmap(
			Bitmap.createBitmap(bitmap, left, top, right, bottom),
			left.toFloat(),
			top.toFloat()
		)
	}
}

data class FaceBitmap(val bitmap: Bitmap, val left: Float, val top: Float)
