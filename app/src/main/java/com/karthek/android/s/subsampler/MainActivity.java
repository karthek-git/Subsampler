package com.karthek.android.s.subsampler;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.karthek.android.s.subsampler.databinding.ActivityMainBinding;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

	ActivityMainBinding binding;
	Uri uri;
	FileInputStream inputStream;
	String fileName;
	long orig_size;
	long req_size;
	long got_size;
	ByteArrayOutputStream outputStream;
	ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
			uri -> {
				if (uri == null) return;
				this.uri = uri;
				binding.imageView.setImageURI(uri);
				Cursor cursor =
						getContentResolver().query(uri, null, null, null, null);
				int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
				int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
				cursor.moveToFirst();
				fileName = cursor.getString(nameIndex);
				orig_size = cursor.getLong(sizeIndex);
				binding.origSize.setText(Formatter.formatFileSize(this, orig_size));
				cursor.close();
				try {
					inputStream = (FileInputStream) getContentResolver().openInputStream(uri);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
	ActivityResultLauncher<String> mSaveContent =
			registerForActivityResult(new ActivityResultContracts.CreateDocument("image/jpeg"),
					result -> {
						if (result == null) return;
						try {
							outputStream.writeTo(getContentResolver().openOutputStream(result));
						} catch (IOException e) {
							e.printStackTrace();
						}
						Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
					});

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		binding = null;
		uri = null;
		outputStream = null;
		inputStream = null;
	}

	public void select_image(View view) {
		mGetContent.launch("image/*");
	}

	public void run_size(View view) {
		String s = binding.size.getText().toString();
		if (s.isEmpty()) return;
		req_size = Long.parseLong(s) * 1024;
		binding.samplingProgress.setVisibility(View.VISIBLE);
		new Thread(this::subSample).start();
	}

	private void subSample() {
		Bitmap bitmap;
		got_size = orig_size;
		outputStream = new ByteArrayOutputStream();
		try {
			inputStream.getChannel().position(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		bitmap = BitmapFactory.decodeStream(inputStream);
		int quality = 100;
		while (got_size > req_size && quality > 0) {
			quality -= 1;
			outputStream.reset();
			bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
			got_size = outputStream.size();
		}
		runOnUiThread(() -> {
			binding.samplingProgress.setVisibility(View.GONE);
			binding.imageView.setImageBitmap(bitmap);
			String s = String.format(Locale.ENGLISH, "%.2fkb", got_size / 1024f);
			binding.sampledSize.setText(s);
		});
	}

	private String getName() {
		String name = null;
		if (fileName != null) {
			int i = fileName.lastIndexOf('.');
			name = fileName.substring(0, i) + "-" + req_size / 1024 + ".jpg";
		}
		return name;
	}

	public void save_file(View view) {
		if (outputStream == null) return;
		mSaveContent.launch(getName());
	}

}