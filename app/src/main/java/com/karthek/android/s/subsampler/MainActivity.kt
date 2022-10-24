package com.karthek.android.s.subsampler

import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.karthek.android.s.subsampler.state.SubsampleScreenViewModel
import com.karthek.android.s.subsampler.ui.screens.MainScreen
import com.karthek.android.s.subsampler.ui.theme.SubsamplerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.FileInputStream
import java.io.IOException


class MainActivity : ComponentActivity() {

	private val viewModel: SubsampleScreenViewModel by viewModels()

	private var mGetContent = registerForActivityResult(PickVisualMedia()) { uri: Uri? ->
		if (uri == null) return@registerForActivityResult
		viewModel.imageUri = uri
		val cursor: Cursor =
			contentResolver.query(uri, null, null, null, null) ?: return@registerForActivityResult
		val nameIndex: Int = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
		val sizeIndex: Int = cursor.getColumnIndex(OpenableColumns.SIZE)
		cursor.moveToFirst()
		viewModel.fileName = cursor.getString(nameIndex)
		viewModel.origSize = cursor.getLong(sizeIndex)
		cursor.close()
		try {
			viewModel.inputStream = contentResolver.openInputStream(uri) as FileInputStream?
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}

	private var mSaveContent = registerForActivityResult<String, Uri>(
		ActivityResultContracts.CreateDocument("image/jpeg")
	) { result: Uri? ->
		if (result == null) return@registerForActivityResult
		try {
			contentResolver.openOutputStream(result)?.let {
				lifecycleScope.launch(Dispatchers.IO) {
					withContext(Dispatchers.IO) {
						viewModel.outputStream.writeTo(it)
					}
				}
			}
		} catch (e: IOException) {
			e.printStackTrace()
		}
		Toast.makeText(this, getString(R.string.saved), Toast.LENGTH_SHORT).show()
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		WindowCompat.setDecorFitsSystemWindows(window, false)
		setContent { ScreenContent() }
	}

	@Composable
	fun ScreenContent() {
		SubsamplerTheme {
			Surface(modifier = Modifier.fillMaxSize()) {
				MainScreen(viewModel, selectImageClick = {
					mGetContent.launch(PickVisualMediaRequest(PickVisualMedia.ImageOnly))
				}) {
					mSaveContent.launch(getSaveFileName(viewModel.fileName, viewModel.reqSize))
				}
			}
		}
	}

}