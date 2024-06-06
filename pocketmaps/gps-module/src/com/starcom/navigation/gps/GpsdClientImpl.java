package com.starcom.navigation.gps;

import java.util.function.Consumer;

import com.ivkos.gpsd4j.client.GpsdClient;
import com.ivkos.gpsd4j.messages.ErrorMessage;
import com.ivkos.gpsd4j.messages.PollMessage;
import com.ivkos.gpsd4j.messages.reports.TPVReport;

public class GpsdClientImpl implements IClient
{
	GpsdClient child;
	public GpsdClientImpl(GpsdClient child)
	{
		this.child = child;
	}
	
	@Override
	public void start()
	{
		child.start();
	}
	
	@Override
	public void stop()
	{
		child.stop();
	}
	
	@Override
	public void sendPollCommand(Consumer<PollMessage> responseHandler)
	{
		child.sendCommand(new PollMessage(), responseHandler);
	}
	
	@Override
	public void watch(boolean enable, boolean reportMessages)
	{
		child.watch(enable, reportMessages);
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
	}

}
