package com.starcom.navigation.gps;

import java.util.function.Consumer;

import com.ivkos.gpsd4j.client.GpsdClient;
import com.ivkos.gpsd4j.client.GpsdClientOptions;
import com.ivkos.gpsd4j.messages.ErrorMessage;
import com.ivkos.gpsd4j.messages.PollMessage;
import com.ivkos.gpsd4j.messages.reports.TPVReport;

public interface IClient
{
	public void start();
	public void stop();
	public void sendPollCommand(Consumer<PollMessage> responseHandler);
	public void watch(boolean enable, boolean reportMessages);
	public void addTpvHandler(Consumer<TPVReport> handler);
	public void addErrorHandler(Consumer<ErrorMessage> handler);
	
	/** Creates the native gps client, that may be PsClientImpl or GpsdClientImpl, using default values.
	 * @return The native client. */
	public static IClient createGpsClient() { return createGpsClient("localhost", 2944); }
	
	/** Creates the native gps client, that may be PsClientImpl or GpsdClientImpl.
	 * @param gpsdHost The host, default is localhost.
	 * @param gpsdPort The port, default is 2944.
	 * @return The native client. */
	public static IClient createGpsClient(String gpsdHost, int gpsdPort)
	{
    	boolean isWin = System.getProperties().get("os.name").toString().startsWith("Windows"); // Hint: apache commons lang3 -> SystemUtils.java -> IS_OS_WINDOWS
    	if (isWin)
    	{
    		return new PsClientImpl();
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
    	//TODO: On Android use StaticClientImpl.
	}
}
