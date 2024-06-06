package com.starcom.navigation.gps;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ivkos.gpsd4j.messages.ErrorMessage;
import com.ivkos.gpsd4j.messages.PollMessage;
import com.ivkos.gpsd4j.messages.reports.TPVReport;

public class StaticClientImpl implements IClient
{
	private static final Logger log = LoggerFactory.getLogger(StaticClientImpl.class);
	private Consumer<ErrorMessage> errorHandler = System.out::println;
	private static Consumer<TPVReport> tpvHandler;
	private static boolean watching = false;
private static TPVReport lastTpvReport;

	@Override
	public void start()
	{
		log.info("Starting StaticClient.");
	}

	@Override
	public void stop()
	{
		log.info("Stopping StaticClient.");
	}

/** This is called by native location service to update location */
public static void onUpdate(double lat, double lon, double alt, double speed)
{
	if (!watching) { return; }
	lastTpvReport = new StaticTpvReport(lat,lon,alt,speed);
	if (tpvHandler != null)
	{
		tpvHandler.accept(lastTpvReport);
	}
}

	@Override
	public void sendPollCommand(Consumer<PollMessage> responseHandler)
	{
		responseHandler.accept(new StaticPollMessage(lastTpvReport));
	}

	@Override
	public void watch(boolean enable, boolean reportMessages)
	{
		watching = (enable && reportMessages);
	}
	
	@Override
	public void addTpvHandler(Consumer<TPVReport> tpvHandler)
	{
		StaticClientImpl.tpvHandler = tpvHandler;
	}

	@Override
	public void addErrorHandler(Consumer<ErrorMessage> errorHandler)
	{
		if (errorHandler == null) { throw new IllegalStateException("ErrorHandler must not be null."); }
		this.errorHandler = errorHandler;
	}

}
