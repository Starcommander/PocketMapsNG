package com.starcom.gdx.system;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.starcom.gdx.io.Storage;

import org.apache.commons.lang3.SystemUtils;

public class App
{
	private static String appName = "app";
	
	/** Sets the global app name before initialisation.
	 * <br>Gdx.app is null at this time
	 * @param initialAppName The app name.
	 * @param skipSubdir True to skip creating a subDir, for example on android. */
	public static void setAppName(String initialAppName, boolean skipSubdir)
	{
		appName = initialAppName;
		if (!skipSubdir)
		{
			if (SystemUtils.IS_OS_WINDOWS)
			{
				Storage.setSubDir(appName);
			}
			else
			{
				Storage.setSubDir(".config/" + appName);
			}
		}
	}

}
