package org.scinix.android.gallorg;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import org.scinix.android.utils.MediaScanner;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class GallOrgShare extends Activity implements OnClickListener, OnItemSelectedListener {

	private static final String ORION_ROOT = "/sdcard/Orion/GallOrg/";

	private ArrayList<File> fileArray = new ArrayList<File> ();
	private ArrayList<Uri> uriList = new ArrayList<Uri> ();
	private Button btnMove;
	private Button btnCancel;


	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ArrayList <String> dirStringList = new ArrayList <String> ();

		setContentView(R.layout.share);
		TextView tv = (TextView) findViewById(R.id.filelist);
		TextView amount = (TextView) findViewById(R.id.amount_of_files);
		AutoCompleteTextView destination = (AutoCompleteTextView) findViewById(R.id.destination);
		Spinner exists = (Spinner) findViewById(R.id.exists);
		((CheckBox) findViewById(R.id.scanmedia)).setChecked(true);

		/* get existing album(directory) list from ORION_ROOT */
		File rootDir = new File(ORION_ROOT);
		if (rootDir.exists() && rootDir.isDirectory()) {
			ArrayList<File> dirList = new ArrayList<File> (Arrays.asList(rootDir.listFiles()));
			Iterator<File> e = dirList.iterator();
			while (e.hasNext()) {
				dirStringList.add(((File) e.next()).getName());
			}
		}

		Collections.sort(dirStringList);
		SimpleDateFormat nowFormatted = new SimpleDateFormat("yyyyMMdd");
		destination.setText((nowFormatted.format(new Date())).toString());
		dirStringList.add(0, ((nowFormatted.format(new Date())).toString()));

		String[] dirArray = new String[dirStringList.size()];
		dirStringList.toArray(dirArray);
		Log.i("gallorg", "existing dirs(array): " + Arrays.asList(dirArray).toString());

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, dirArray);
		destination.setAdapter(adapter);
		ArrayAdapter<String> adapterSpinner = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, dirArray);
		exists.setAdapter(adapterSpinner);

		exists.setOnItemSelectedListener(this);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		if (Intent.ACTION_SEND.equals(intent.getAction())) {
			if (extras != null) {
				Uri fileUri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
				fileArray.add(UriUtils.getFileFromUri(fileUri, this));
				uriList.add(fileUri);
			} else {
				tv.append(", extras == null");
			}
		} else if (Intent.ACTION_SEND_MULTIPLE.
			   equals(intent.getAction())) {
			if (extras != null) {
				ArrayList<Uri> uriArray = extras.getParcelableArrayList(Intent.EXTRA_STREAM);
				uriList.addAll(uriArray);
				Iterator < Uri > e = uriArray.iterator();
				while (e.hasNext()) {
					fileArray.add(UriUtils.getFileFromUri((Uri) e.next(), this));
				}
			} else {
				tv.append(", extras == null");
			}
		}

		Iterator < File > e = fileArray.iterator();
		while (e.hasNext()) {
			File file = (File) e.next();
			if (file.exists() && file.isFile()) {
				tv.append("* " + file.getName());
			}
			tv.append("\n");
		}

		amount.setText(Integer.toString(fileArray.size()));

		btnMove = (Button) findViewById(R.id.ok);
		btnCancel = (Button) findViewById(R.id.cancel);

		btnMove.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.ok:
				String folderName = ((TextView) findViewById(R.id.destination)).getText().toString();
				File destDir = new File(ORION_ROOT + folderName);
				destDir.mkdirs();
				Log.i("gallorg", "destination is " + folderName);

				String[] targetFiles = new String[fileArray.size()];
				int i = 0;

				Iterator<File> e = fileArray.iterator();
				while (e.hasNext()) {
					File file = (File) e.next();
					File dest = new File(ORION_ROOT + folderName + "/" + file.getName());
					file.renameTo(dest);
					targetFiles[i++] = dest.getAbsolutePath();
					Log.i("gallorg", "rename " + file.getAbsolutePath() + " to " + dest.getAbsolutePath());
				}

				if (((CheckBox) findViewById(R.id.scanmedia)).isChecked()) {
					Log.i("gallorg", "option scanmedia is checked.");
					MediaScanner scanner = new MediaScanner(this);
					scanner.scanFile(targetFiles);
					
					Iterator<Uri> eu = uriList.iterator();
					while (eu.hasNext()) {
						int count = getContentResolver().delete(eu.next(), null, null);
						Log.i("gallorg", "provider: " + Integer.toString(count) + "record deleted.");
					}
				}

				if (((CheckBox) findViewById(R.id.cleanup)).isChecked()) {
					Log.i("gallorg", "option cleanup is checked.");
					/*
					File rootDir = new File(ORION_ROOT);
					if (rootDir.exists() && rootDir.isDirectory()) {
						ArrayList<File> dirList = new ArrayList<File> (Arrays.asList(rootDir.listFiles()));
						Iterator<File> e = dirList.iterator();
						while (e.hasNext()) {
							//dirStringList.add(((File) e.next()).getName());
							File currDir = (File) e.next();
							//
						}
					}
					*/
				}

				finish();
				break;

			case R.id.cancel:
				if (((TextView) findViewById(R.id.destination)).getText().toString().equals("About...")) {
					setContentView(R.layout.about);
				} else {
					finish();
				}
				break;
		}
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		// TODO Auto-generated method stub
		//parent.getContext();
		if (!parent.getItemAtPosition(position).toString().equals(((TextView) findViewById(R.id.destination)).getText())) {
			((TextView) findViewById(R.id.destination)).setText(parent.getItemAtPosition(position).toString());
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
	}
}
