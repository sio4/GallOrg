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

	private File target_file = null;
	private Date target_date = null;

	@Override protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo_info);
		TextView fi = (TextView) findViewById(R.id.file_info);
		TextView ei = (TextView) findViewById(R.id.exif_info);

		/* get selected files and add it to src list (counting and debugging purpose.) */
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		if (Intent.ACTION_SEND.equals(intent.getAction())) {
			if (extras != null) {
				Uri fileUri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);
				fileArray.add(UriUtils.getFileFromUri(fileUri, this));
				uriList.add(fileUri);
			} else {
				Toast.makeText(this, "Error! extras == null", Toast.LENGTH_SHORT).show();
				finish();
			}
		}

		SimpleDateFormat dformat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
		/* file list for debugging. */
		Iterator < File > e = fileArray.iterator();
		while (e.hasNext()) {
			File file = (File) e.next();
			if (file.exists() && file.isFile()) {
				fi.append("\n" + file.getName());
				fi.append("\n" + file.length()/1024 + "KB");
				fi.append("\n" + dformat.format(new Date(file.lastModified())));
				target_file = file;
				try {
					ExifInterface exif = new ExifInterface(file.getAbsolutePath());
					ei.append("\n\tDate: " + exif.getAttribute(ExifInterface.TAG_DATETIME));
					ei.append("\n\tFlash: " + exif.getAttribute(ExifInterface.TAG_FLASH));
					ei.append("\n\tLat.: " + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
					ei.append("\n\tLat. Ref: " + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF));
					ei.append("\n\tLong.: " + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
					ei.append("\n\tLong. Ref: " + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF));
					ei.append("\n\tWidth: " + exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH));
					ei.append("\n\tLength: " + exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH));
					ei.append("\n\tMake: " + exif.getAttribute(ExifInterface.TAG_MAKE));
					ei.append("\n\tModel: " + exif.getAttribute(ExifInterface.TAG_MODEL));
					ei.append("\n\tOrientation: " + exif.getAttribute(ExifInterface.TAG_ORIENTATION));
					ei.append("\n\tWhite Balance: " + exif.getAttribute(ExifInterface.TAG_WHITE_BALANCE));

					if (exif.getAttribute(ExifInterface.TAG_DATETIME) != null) {
						SimpleDateFormat exifDateFormat = new SimpleDateFormat("yyy:MM:dd HH:mm:ss");
						target_date = exifDateFormat.parse(exif.getAttribute(ExifInterface.TAG_DATETIME));
					}
				} catch (IOException e1) {
					e1.printStackTrace();
					Toast.makeText(this, "IO Error!", Toast.LENGTH_SHORT).show();
				} catch (ParseException pe) {
					pe.printStackTrace();
					Toast.makeText(this, "Parse Error!", Toast.LENGTH_SHORT).show();
				}
			}
		}

		/* button binding. */
		btnOK = (Button) findViewById(R.id.btn_ok);
		btnTouch = (Button) findViewById(R.id.btn_touch);
		btnOK.setOnClickListener(this);
		btnTouch.setOnClickListener(this);
		if (target_date == null) {
			btnTouch.setEnabled(false);
		}
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
					/* any smart and perfect... right way?
					//XXX getContentResolver().delete(fileUri, null, null);
					MediaScanner scanner = new MediaScanner(this);
					scanner.scanFile(target_file.getAbsolutePath());
					*/
				}
				finish();
				break;
		}
	}
}
