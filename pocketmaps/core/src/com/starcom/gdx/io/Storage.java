package com.starcom.gdx.io;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class Storage
{
	private static String subDir = "";
	
	/** Where on Android the gdx-storage has already an extra directory, on desktop we should set a sub-dir. */
	public static void setSubDir(String newSubDir)
	{
		if (newSubDir == null) { subDir = ""; }
		else if (newSubDir.isEmpty()) { subDir = ""; }
		else if (newSubDir.endsWith("/")) { subDir = newSubDir; }
		else { subDir = newSubDir + "/"; }
	}

	/** Obtains a FileHandle for storage operations.
	 * @param path The relative path. */
	public static FileHandle getFileHandle(String path)
	{
		if (!Gdx.files.isExternalStorageAvailable()) { return null; }
		if (subDir != null)
		{
			Gdx.files.external(subDir).mkdirs();
		}
		return Gdx.files.external(subDir + path);
	}

}
