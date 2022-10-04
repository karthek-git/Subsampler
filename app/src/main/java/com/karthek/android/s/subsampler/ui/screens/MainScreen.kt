package com.karthek.android.s.subsampler.ui.screens

import android.text.format.Formatter
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.karthek.android.s.subsampler.R
import com.karthek.android.s.subsampler.state.SubsampleScreenViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MainScreen(
	viewModel: SubsampleScreenViewModel,
	selectImageClick: () -> Unit,
	shareClick: () -> Unit,
	saveClick: () -> Unit
) {
	Scaffold(topBar = {
		TopAppBar(title = {
			Text(
				text = stringResource(id = R.string.app_name),
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}, actions = {
			IconButton(onClick = { /*TODO*/ }) {
				Icon(
					imageVector = Icons.Outlined.MoreVert,
					contentDescription = stringResource(id = R.string.more)
				)
			}
		})
	}) { innerPadding ->
		Column(
			modifier = Modifier
				.padding(innerPadding)
				.padding(8.dp)
				.verticalScroll(rememberScrollState())
		) {
			Card(
				onClick = selectImageClick, modifier = Modifier
					.fillMaxWidth()
					.height(240.dp)
					.padding(8.dp)
			) {
				Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
					if (viewModel.imageUri == null) {

						Icon(
							imageVector = Icons.Outlined.AddAPhoto,
							contentDescription = stringResource(id = R.string.select_photo),
						)

					} else {
						SubcomposeAsyncImage(
							model = viewModel.imageUri,
							contentDescription = stringResource(id = R.string.select_photo),
							loading = {
								CircularProgressIndicator(modifier = Modifier.requiredSize(48.dp))
							},
							modifier = Modifier
								.fillMaxSize()
						)
					}
					if (viewModel.samplingInProgress) {
						LinearProgressIndicator(
							modifier = Modifier
								.fillMaxWidth()
								.align(Alignment.BottomCenter)
						)
					}
				}
			}
			if (viewModel.imageUri != null) {
				Text(
					text = "${stringResource(id = R.string.orig_size)} : ${
						Formatter.formatFileSize(
							LocalContext.current,
							viewModel.origSize
						)
					}",
					textAlign = TextAlign.Center,
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp)
				)
				Text(
					text = if (viewModel.showSaved) {
						"${stringResource(id = R.string.new_size)} : ${
							viewModel.gotSize / 1024
						} KB"
					} else {
						stringResource(id = R.string.message1)
					},
					color = if (viewModel.showSaved) Color.Unspecified else Color.Blue,
					textAlign = TextAlign.Center,
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp)
				)

				val keyboardController = LocalSoftwareKeyboardController.current

				OutlinedTextField(
					value = viewModel.textFieldValue,
					onValueChange = { e_s ->
						with(viewModel) {
							textFieldValue = e_s
							val parsedValue = textFieldValue.toLongOrNull()
							isError = ((parsedValue == null) || (parsedValue <= 0L))
							if (!isError && parsedValue != null) {
								reqSize = parsedValue
							}
						}
					}, isError = viewModel.isError,
					label = { Text(text = stringResource(id = R.string.req_size)) },
					singleLine = true,
					keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
					keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
					modifier = Modifier
						.fillMaxWidth()
						.padding(16.dp)
						.align(Alignment.CenterHorizontally)
				)
				Button(
					enabled = (!(viewModel.isError) && (viewModel.textFieldValue.isNotEmpty())),
					onClick = { viewModel.runForSize() },
					modifier = Modifier
						.padding(16.dp)
						.align(Alignment.CenterHorizontally)
				) {
					Text(text = stringResource(id = R.string.run))
				}
				Row(
					horizontalArrangement = Arrangement.SpaceEvenly,
					modifier = Modifier.fillMaxWidth()
				) {
					Button(enabled = viewModel.showSaved, onClick = shareClick) {
						Text(text = stringResource(id = R.string.share))
					}
					Button(enabled = viewModel.showSaved, onClick = saveClick) {
						Text(text = stringResource(id = R.string.save))
					}
				}
			}
		}
	}
}