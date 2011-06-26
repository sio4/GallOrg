package org.scinix.android.gallorg;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import android.app.Activity;
import android.content.Intent;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class GallOrgInfo extends Activity implements OnClickListener {

	private ArrayList<File> fileArray = new ArrayList<File> ();
	private ArrayList<Uri> uriList = new ArrayList<Uri> ();
	private Button btnOK;
	private Button btnTouch;

	private File target_file;
	private Date target_date;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_info);
		TextView tv = (TextView) findViewById(R.id.textView1);

		/* get selected files and add it to src list (counting and debugging purpose.) */
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
		}

		/* file list for debugging. */
		Iterator < File > e = fileArray.iterator();
		while (e.hasNext()) {
			File file = (File) e.next();
			if (file.exists() && file.isFile()) {
				tv.append("* " + file.getName() + "\n");
				Date fdate = new Date(file.lastModified());
				tv.append("File Date: " + fdate.toString() + "\n");
				target_file = file;
				try {
					ExifInterface exif = new ExifInterface(file.getAbsolutePath());
					tv.append("Date: " + exif.getAttribute(ExifInterface.TAG_DATETIME) + "\n");
					tv.append("Flash: " + exif.getAttribute(ExifInterface.TAG_FLASH) + "\n");
					tv.append("Lat.: " + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) + "\n");
					tv.append("Lat. Ref: " + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF) + "\n");
					tv.append("Long.: " + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) + "\n");
					tv.append("Long. Ref: " + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF) + "\n");
					tv.append("Width: " + exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH) + "\n");
					tv.append("Length: " + exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH) + "\n");
					tv.append("Make: " + exif.getAttribute(ExifInterface.TAG_MAKE) + "\n");
					tv.append("Model: " + exif.getAttribute(ExifInterface.TAG_MODEL) + "\n");
					tv.append("Orientation: " + exif.getAttribute(ExifInterface.TAG_ORIENTATION) + "\n");
					tv.append("White Balance: " + exif.getAttribute(ExifInterface.TAG_WHITE_BALANCE) + "\n");

					SimpleDateFormat exifDateFormat = new SimpleDateFormat("yyy:MM:dd HH:mm:ss");
					target_date = exifDateFormat.parse(exif.getAttribute(ExifInterface.TAG_DATETIME));
				} catch (IOException e1) {
					e1.printStackTrace();
					Toast.makeText(this, "IO Error!", Toast.LENGTH_SHORT).show();
				} catch (ParseException pe) {
					pe.printStackTrace();
					Toast.makeText(this, "Parse Error!", Toast.LENGTH_SHORT).show();
				}
			}
			tv.append("\n");
		}

		/* button binding. */
		btnOK = (Button) findViewById(R.id.btn_ok);
		btnTouch = (Button) findViewById(R.id.btn_touch);
		btnOK.setOnClickListener(this);
		btnTouch.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_ok:
				finish();
				break;
			case R.id.btn_touch:
				Toast.makeText(this, R.string.msg_touch, Toast.LENGTH_SHORT).show();
				target_file.setLastModified(target_date.getTime());

				/* update media */
				Iterator<Uri> eu = uriList.iterator();
				while (eu.hasNext()) {
					Uri fileUri = eu.next();
					getContentResolver().notifyChange(fileUri, null);
					/*
					getContentResolver().delete(fileUri, null, null);
					MediaScanner scanner = new MediaScanner(this);
					canner.scanFile(target_file.getAbsolutePath());
					*/
				}
				break;
		}
	}

}
