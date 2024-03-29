package com.karthek.android.s.subsampler.ui.screens

import android.content.Intent
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.karthek.android.s.subsampler.R
import com.karthek.android.s.subsampler.SettingsActivity
import com.karthek.android.s.subsampler.state.SubsampleScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
	viewModel: SubsampleScreenViewModel,
	selectImageClick: () -> Unit,
	saveClick: () -> Unit,
) {
	val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
	Scaffold(topBar = {
		TopAppBar(title = {
			Text(
				text = stringResource(id = R.string.app_name),
				maxLines = 1,
				overflow = TextOverflow.Ellipsis
			)
		}, actions = {
			val context = LocalContext.current
			IconButton(onClick = {
				context.startActivity(Intent(context, SettingsActivity::class.java))
			}) {
				Icon(
					imageVector = Icons.Outlined.MoreVert,
					contentDescription = stringResource(id = R.string.more)
				)
			}
		}, scrollBehavior = scrollBehavior
		)
	}, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) { innerPadding ->
		Column(
			modifier = Modifier
				.padding(innerPadding)
				.padding(8.dp)
				.imePadding()
				.verticalScroll(rememberScrollState())
		) {
			ImagePreview(
				viewModel = viewModel,
				selectImageClick = selectImageClick,
				modifier = Modifier
					.fillMaxWidth()
					.height(240.dp)
			)
			ActionContent(viewModel, saveClick)
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePreview(
	viewModel: SubsampleScreenViewModel,
	selectImageClick: () -> Unit,
	modifier: Modifier,
) {
	Card(
		onClick = selectImageClick, modifier = modifier.padding(8.dp)
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
					modifier = Modifier.fillMaxSize()
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
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ActionContent(viewModel: SubsampleScreenViewModel, saveClick: () -> Unit) {
	if (viewModel.imageUri != null) {
		Column {
			Text(
				text = "${stringResource(id = R.string.orig_size)} : ${
					Formatter.formatFileSize(
						LocalContext.current, viewModel.origSize
					)
				}", textAlign = TextAlign.Center, modifier = Modifier
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
				color = if (viewModel.showSaved) Color.Unspecified
				else MaterialTheme.colorScheme.primary,
				textAlign = TextAlign.Center,
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp)
			)

			val keyboardController = LocalSoftwareKeyboardController.current
			var isError by rememberSaveable { mutableStateOf(false) }

			OutlinedTextField(
				value = viewModel.textFieldValue,
				onValueChange = { e_s ->
					viewModel.textFieldValue = e_s
					val parsedValue = e_s.toLongOrNull()
					isError = ((parsedValue == null) || (parsedValue <= 0L))
					if (!isError && parsedValue != null) {
						viewModel.reqSize = parsedValue
					}
				},
				isError = isError,
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
				enabled = (!(isError)
						&& (viewModel.textFieldValue.isNotEmpty())
						&& (!viewModel.samplingInProgress)),
				onClick = { viewModel.runForSize() },
				modifier = Modifier
					.padding(16.dp)
					.align(Alignment.CenterHorizontally)
			) {
				Text(text = stringResource(id = R.string.run))
			}

			Button(
				enabled = viewModel.showSaved,
				onClick = saveClick,
				modifier = Modifier
					.fillMaxWidth()
					.padding(16.dp)
			) {
				Text(text = stringResource(id = R.string.save))
			}
		}
	}
}