package com.karthek.android.s.subsampler.state

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.karthek.android.s.subsampler.NAC
import com.karthek.android.s.subsampler.ml.model.FaceDetector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.FileInputStream

class SubsampleScreenViewModel(appContext: Application) : AndroidViewModel(appContext) {
	var imageUri by mutableStateOf<Uri?>(null)
	var textFieldValue by mutableStateOf("")
	var samplingInProgress by mutableStateOf(false)
	var showSaved by mutableStateOf(false)
	var inputStream: FileInputStream? = null
	var fileName: String? = null
	var origSize by mutableStateOf(0L)
	var reqSize = 0L
	var gotSize by mutableStateOf(0L)
	val outputStream by lazy { ByteArrayOutputStream() }

	private val nac by lazy { NAC(FaceDetector(appContext)) }

	fun runForSize() {
		viewModelScope.launch {
			samplingInProgress = true
			gotSize = withContext(Dispatchers.Default) {
				inputStream?.let {
					nac.compress(origSize, reqSize * 1024, it, outputStream)
				} ?: 0
			}
			samplingInProgress = false
			showSaved = true
		}
	}
}