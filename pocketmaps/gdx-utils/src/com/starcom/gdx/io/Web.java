package com.starcom.gdx.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Map.Entry;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.files.FileHandle;
import com.starcom.gdx.system.Threading;
import com.starcom.gdx.ui.ToastMsg;
import com.starcom.interfaces.IProgressListener;
import com.starcom.interfaces.IProgressListener.Type;

public class Web
{
	/** Downloads and stores a file.
	 * @param callback Callback that will be run on progress(%), success and error.
	 * @param target The target FileHandle got by Storage.getFileHandle(s).
	 * @param fileLength The lengh of the file to download, or -1.
	 * @return The request, that can be canceled by Gdx.net.cancleHttpRequest(q) */
	public static HttpRequest download(String url, FileHandle target, IProgressListener callback)
	{
		HttpRequest rq = new HttpRequest();
		rq.setMethod(HttpMethods.GET);
		rq.setUrl(url);
		HttpResponseListener rs = new HttpResponseListener() {

			@Override
			public void handleHttpResponse(HttpResponse httpResponse)
			{
				InputStream stream = httpResponse.getResultAsStream();
				String fLenS = getHeader("Content-Length", httpResponse);
				int fLen = -1;
				if (fLenS != null)
				{
					fLen = Integer.parseInt(fLenS); // Possible NumberFormatException
				}
	
				target.delete();
				byte buffer[] = new byte[256];
				int len = 0;
				long written = 0;
				try
				{
					while (true)
					{
						len = stream.read(buffer);
						if (len < 0) { break; }
						target.writeBytes(buffer, 0, len, true);
						int progress = 50;
						if (fLen != -1)
						{
							written += len;
							float perc = ((float)written/fLen) * 100.0f;
							progress = (int)perc;
						}
						callback.onProgress(Type.PROGRESS, progress);
					}
					callback.onProgress(Type.SUCCESS, "Success");
				} catch (IOException e) { failed(e); }
			}

			@Override
			public void failed(Throwable t)
			{
				callback.onProgress(Type.ERROR, t);
				ToastMsg.getInstance().toastLong("Error downloading source.");
			}

			@Override
			public void cancelled()
			{
				callback.onProgress(Type.CANCEL, "Cancel");
				ToastMsg.getInstance().toastShort("Downloading canceled.");
			}
		};
			
		Threading.getInstance().invokeOnWorkerThread(() -> Gdx.net.sendHttpRequest(rq, rs));
		return rq;
	}
	
	/** Returns the first entry of header matching key.
	 * @param key The Key to match.
	 * @param resp The httpResponse of a request.
	 * @return The value, or null if not present. */
	private static String getHeader(String key, HttpResponse resp)
	{
		for (Entry<String, List<String>> e : resp.getHeaders().entrySet())
		{
			if (!key.equals(e.getKey())) { continue; }
			for ( String val : e.getValue())
			{
				return val;
			}
		}
		return null;
	}
}
