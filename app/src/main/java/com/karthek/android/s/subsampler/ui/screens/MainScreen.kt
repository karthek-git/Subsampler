package com.karthek.android.s.subsampler.ui.screens

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.karthek.android.s.subsampler.R
import com.karthek.android.s.subsampler.SettingsActivity
import com.karthek.android.s.subsampler.helper.formatFileSize
import com.karthek.android.s.subsampler.state.SubsampleScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
	viewModel: SubsampleScreenViewModel,
	selectImageClick: () -> Unit,
	saveClick: () -> Unit,
) {
	val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
	Scaffold(topBar = {
		LargeTopAppBar(
			title = {
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
				.padding(innerPadding.add(start = 8.dp, end = 8.dp))
				.fillMaxHeight()
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

@Composable
fun ActionContent(viewModel: SubsampleScreenViewModel, saveClick: () -> Unit) {
	if (viewModel.imageUri != null) {
		Column {
			Row(
				modifier = Modifier
					.padding(16.dp)
					.align(Alignment.CenterHorizontally)
			) {
				Column(modifier = Modifier.padding(end = 32.dp)) {
					Text(
						text = "${stringResource(id = R.string.orig_size)}:",
						color = MaterialTheme.colorScheme.primary,
						style = MaterialTheme.typography.titleMedium,
						modifier = Modifier.padding(vertical = 8.dp)
					)
					if (viewModel.showSaved) {
						Text(
							text = "${stringResource(id = R.string.new_size)}:",
							color = MaterialTheme.colorScheme.primary,
							style = MaterialTheme.typography.titleMedium,
							modifier = Modifier
						)
					}
				}
				Column {
					Text(
						text = formatFileSize(viewModel.origSize),
						modifier = Modifier.padding(vertical = 8.dp)
					)
					if (viewModel.showSaved) {
						Text(text = formatFileSize(viewModel.gotSize))
					}
				}
			}
			if (!(viewModel.showSaved)) {
				Text(
					text = stringResource(R.string.message1),
					color = MaterialTheme.colorScheme.primary,
					modifier = Modifier.padding(16.dp)
				)
			}

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
				placeholder = { Text(text = stringResource(id = R.string.req_size_place_holder)) },
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

@Composable
fun PaddingValues.add(
	start: Dp = 0.dp,
	top: Dp = 0.dp,
	end: Dp = 0.dp,
	bottom: Dp = 0.dp,
) = PaddingValues(
	start = start + calculateStartPadding(LocalLayoutDirection.current),
	top = top + calculateTopPadding(),
	end = end + calculateEndPadding(LocalLayoutDirection.current),
	bottom = bottom + calculateBottomPadding()
)