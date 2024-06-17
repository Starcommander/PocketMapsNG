package com.starcom.navigation.gps;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ivkos.gpsd4j.client.GpsdClient;
import com.ivkos.gpsd4j.messages.ErrorMessage;
import com.ivkos.gpsd4j.messages.PollMessage;
import com.ivkos.gpsd4j.messages.reports.TPVReport;

public class GpsdClientImpl implements IClient
{
	private static final Logger log = LoggerFactory.getLogger(GpsdClientImpl.class);
	Consumer<ErrorMessage> errorHandler = System.out::println;
	
	GpsdClient child;
	public GpsdClientImpl(GpsdClient child)
	{
		this.child = child;
	}
	
	@Override
	public void start()
	{
		log.info("Starting GpsdClient.");
		child.start();
	}
	
	@Override
	public void stop()
	{
		log.info("Stopping GpsdClient.");
		child.stop();
	}
	
	@Override
	public void sendPollCommand(Consumer<PollMessage> responseHandler)
	{
		if (com.starcom.system.Threading.getInstance().isMainThread())
		{
			com.starcom.system.Threading.getInstance().invokeOnWorkerThread(() -> sendPollCommand(responseHandler));
			return;
		}
		try {
			child.sendCommand(new PollMessage(), responseHandler);
		}
		catch (IllegalStateException e)
		{
			onErrorOccured("Connection error for sendPollCommand:\n" + e.toString());
		}
	}
	
	@Override
	public void watch(boolean enable, boolean reportMessages)
	{
		if (com.starcom.system.Threading.getInstance().isMainThread())
		{
			com.starcom.system.Threading.getInstance().invokeOnWorkerThread(() -> watch(enable, reportMessages));
			return;
		}
		try {
			child.watch(enable, reportMessages);
		}
		catch (IllegalStateException e)
		{
			onErrorOccured("Connection error for watch:\n" + e.toString());
		}
	}
	
	private void onErrorOccured(String info)
	{
		ErrorMessage msg = new ErrorMessage() { @Override public String getMessage() { return info; } };
		errorHandler.accept(msg);
	}
	
	@Override
	public void addTpvHandler(Consumer<TPVReport> handler)
	{
		child.addHandler(TPVReport.class, handler);
	}
	
	@Override
	public void addErrorHandler(Consumer<ErrorMessage> handler)
	{
		child.addErrorHandler(handler);
		this.errorHandler = handler;
	}

}
