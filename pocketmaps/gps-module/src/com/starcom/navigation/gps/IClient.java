package com.starcom.navigation.gps;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ivkos.gpsd4j.client.GpsdClient;
import com.ivkos.gpsd4j.client.GpsdClientOptions;
import com.ivkos.gpsd4j.messages.ErrorMessage;
import com.ivkos.gpsd4j.messages.PollMessage;
import com.ivkos.gpsd4j.messages.reports.TPVReport;

public interface IClient
{
	static final Logger logger = LoggerFactory.getLogger(IClient.class);
	public void start();
	public void stop();
	public void sendPollCommand(Consumer<PollMessage> responseHandler);
	public void watch(boolean enable, boolean reportMessages);
	public void addTpvHandler(Consumer<TPVReport> handler);
	public void addErrorHandler(Consumer<ErrorMessage> handler);
	
	/** Creates the native gps client, that may be PsClientImpl or GpsdClientImpl or StaticClientImpl.
	 * <br> For GPSD the environment variables GPSD_HOST and GPSD_PORT are considered with default values of "localhost:2944".
	 * @return The native client. */
	public static IClient createGpsClient()
	{
		String gpsdHost = "localhost";
		int gpsdPort = 2944;
		String env = System.getenv("GPSD_HOST");
		if (env != null && ! env.isEmpty())
		{
			gpsdHost = env;
		}
		env = System.getenv("GPSD_PORT");
		if (env != null && ! env.isEmpty())
		{
			try
			{
				gpsdPort = Integer.parseInt(env);
			}
			catch (NumberFormatException e) { logger.error("GPSD_PORT NumberFormatException, using default: " + gpsdPort); }
		}
		
    	boolean isWin = System.getProperty("os.name", "Linux").startsWith("Windows"); // Hint: apache commons lang3 -> SystemUtils.java -> IS_OS_WINDOWS
    	if (isWin)
    	{
    		return new PsClientImpl();
    	}
    	else if (StaticClientImpl.isAvailable())
    	{
    		return new StaticClientImpl();
    	}
    	else
    	{
    		GpsdClientOptions options = new GpsdClientOptions()
    		    .setReconnectOnDisconnect(true)
    		    .setConnectTimeout(3000) // ms
    		    .setIdleTimeout(0) // seconds
    		    .setReconnectAttempts(5)
    		    .setReconnectInterval(3000); // ms
    		GpsdClient gpsdClient = new GpsdClient(gpsdHost, gpsdPort, options);
    		return new GpsdClientImpl(gpsdClient);
    	}
	}
}
