package org.scinix.android.gallorg;

import java.io.File;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class UriUtils {
	public static File getFileFromUri(Uri uri, Activity activity) {
		String filePath = null;
		String scheme = uri.getScheme();
		 filePath = uri.getPath();
		if (filePath != null && scheme != null
		    && scheme.equals("file")) {
			return new File(filePath);
		}

		String[] projection = {
		MediaStore.Images.ImageColumns.DATA};
		Cursor c =
		    activity.managedQuery(uri, projection, null, null,
					  null);
		if (c != null && c.moveToFirst()) {
			filePath = c.getString(0);
		}
		if (filePath != null) {
			return new File(filePath);
		}
		return null;
	}
}
