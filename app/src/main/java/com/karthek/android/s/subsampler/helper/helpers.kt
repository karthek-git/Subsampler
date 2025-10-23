package com.karthek.android.s.subsampler.helper

import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

const val unitSize = 1024.0

fun formatFileSize(nBytes: Long): String {
	if (nBytes <= 0) return "0 B"
	val units = arrayOf("B", "KB", "MB", "GB", "TB")
	val digitGroups = (log10(nBytes.toDouble()) / log10(unitSize)).toInt()
	return DecimalFormat("#,##0.#")
		.format(nBytes / unitSize.pow(digitGroups.toDouble())) +
			" " + units[digitGroups]
}