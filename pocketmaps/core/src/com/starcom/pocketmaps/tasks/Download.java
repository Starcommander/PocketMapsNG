package com.starcom.pocketmaps.tasks;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.starcom.gdx.io.Storage;
import com.starcom.gdx.io.Web;
import com.starcom.gdx.system.Threading;
import com.starcom.gdx.ui.Dialogs;
import com.starcom.gdx.ui.ToastMsg;
import com.starcom.interfaces.IProgressListener;
import com.starcom.io.Unzip;

public class Download
{
	public static void downloadMapNow(Stage guiStage, String mdate, String mname)
	{
		String link = "http://vsrv15044.customer.xenway.de/maps/maps/" + mdate + "/" + mname + ".ghz";
		System.out.println("Map selected: " + mname);
		Web.download(link,
				getMapsPath(mname + ".ghz.part"),
				Dialogs.showProgress(guiStage,
						() -> Threading.getInstance().invokeOnMainThread(() -> unzipMapNow(guiStage, mname)),
						o -> ToastMsg.getInstance().toastLong(o.toString())));
	}

	private static void unzipMapNow(Stage guiStage, String mname)
	{
		IProgressListener callback = Dialogs.showProgress(guiStage, () ->
		{
			getMapsPath(mname + ".part").moveTo(getMapsPath(mname));
			getMapsPath(mname + ".ghz").delete();
			toastShort("Got: " + mname);
		}, (o) ->
		{
			toastLong("Unzip err: " + o);
		});
		Threading.getInstance().invokeOnWorkerThread(() ->
		{
			FileHandle zipFile = getMapsPath(mname + ".ghz");
			getMapsPath(mname + ".ghz.part").moveTo(zipFile);
			Unzip.unzip(zipFile.file(),
				getMapsPath(mname + ".part").file(),
				callback
			);
		});
	}
	
	/** Get the local path, where the maps are located.
	 * @param subDir The map-subdir, or null to get the base path. */
	public static FileHandle getMapsPath(String subDir)
	{
		if (subDir == null) { subDir = ""; }
		FileHandle mapsDir = Storage.getFileHandle("maps");
		if (!mapsDir.isDirectory()) { mapsDir.mkdirs(); }
		return Storage.getFileHandle("maps/" + subDir);
	}
	
	private static void toastShort(String s) { ToastMsg.getInstance().toastShort(s); }
	private static void toastLong(String s) { ToastMsg.getInstance().toastLong(s); }
}
