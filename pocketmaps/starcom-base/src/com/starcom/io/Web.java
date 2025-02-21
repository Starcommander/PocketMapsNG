package com.starcom.io;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import com.starcom.interfaces.IProgressListener;
import com.starcom.interfaces.IProgressListener.Type;

public class Web
{
	public static void downloadTextfileLater(String textFileUrl, IProgressListener<String> callback)
	{
		Thread t = new Thread(() ->
		{
			callback.onProgress(Type.PROGRESS, "50");
			String result = downloadTextfile(textFileUrl);
			if (result == null) { callback.onProgress(Type.ERROR, "Error on downloading."); }
			else { callback.onProgress(Type.SUCCESS, result); }
		});
		t.start();
	}
	
	/** Returns null on IOException. */
	public static String downloadTextfile(String textFileUrl)
	{
		StringBuilder json = new StringBuilder();
		try (BufferedReader in = new BufferedReader(new InputStreamReader(new URL(textFileUrl).openStream())))
		{
			String lineUrl;
			while ((lineUrl = in.readLine()) != null)
			{
				json.append(lineUrl);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
		return json.toString();
	}
}
