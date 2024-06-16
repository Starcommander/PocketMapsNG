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
		//TODO: This is blocking, and throws an IllegalStateException in case of disconnected.
		//child.sendCommand(new PollMessage(), responseHandler);
	}
	
	@Override
	public void watch(boolean enable, boolean reportMessages)
	{
		//TODO: This is blocking, and throws an IllegalStateException in case of disconnected.
		//child.watch(enable, reportMessages);
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
