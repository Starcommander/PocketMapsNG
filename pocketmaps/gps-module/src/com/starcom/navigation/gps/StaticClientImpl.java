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
	private static Consumer<ErrorMessage> errorHandler = System.out::println;
	private static Consumer<TPVReport> tpvHandler;
	private static boolean watching = false;
private static TPVReport lastTpvReport;
private static boolean available = false;

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
	
	public static boolean isAvailable() { return available; }

	/** This is initially called by native location service to ensure using this StaticClient */
	public static void setAvailable()
	{
		available = true;
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

	/** This is called by native location service when error occurred. */
	public static void onErrorOccured(String info)
	{
		ErrorMessage msg = new ErrorMessage() { @Override public String getMessage() { return info; } };
		errorHandler.accept(msg);
	}

	@Override
	public void sendPollCommand(Consumer<PollMessage> responseHandler)
	{
		if (lastTpvReport==null) { return; } //TODO: Any action instead of just looking for last one.
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
