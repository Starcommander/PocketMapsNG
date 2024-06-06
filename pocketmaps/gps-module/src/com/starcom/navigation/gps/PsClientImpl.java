package com.starcom.navigation.gps;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.function.Consumer;

import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ivkos.gpsd4j.messages.ErrorMessage;
import com.ivkos.gpsd4j.messages.PollMessage;
import com.ivkos.gpsd4j.messages.reports.TPVReport;

public class PsClientImpl implements IClient
{
	private static final Logger log = LoggerFactory.getLogger(PsClientImpl.class);
	private Consumer<ErrorMessage> errorHandler = System.out::println;
	private volatile boolean watchingRun = false;
	private volatile boolean watchingSkip = false;
	private Thread watchThread = new Thread(() -> watchJob());
	private long cycleTime = 4000;
	private Consumer<TPVReport> tpvHandler;
	
	PsTpvReport executeScript()
	{
		String script = "Add-Type -AssemblyName System.Device #Required to access System.Device.Location namespace\n"
				+ "$GeoWatcher = New-Object System.Device.Location.GeoCoordinateWatcher #Create the required object\n"
				+ "$GeoWatcher.Start() #Begin resolving current locaton\n"
				+ "\n"
				+ "while (($GeoWatcher.Status -ne 'Ready') -and ($GeoWatcher.Permission -ne 'Denied')) {\n"
				+ "    Start-Sleep -Milliseconds 100 #Wait for discovery.\n"
				+ "}  \n"
				+ "\n"
				+ "if ($GeoWatcher.Permission -eq 'Denied'){\n"
				+ "    Write-Error 'Access Denied for Location Information'\n"
				+ "} else {\n"
				+ "    $GeoWatcher.Position.Location #Get the relevent results.\n"
				+ "}";
		
		String help = "Possible reasons of issue:\n"
				+ "1) Location service disabled\n"
				+ "Go to Windows-Settings --> Privacy and Security --> Location\n"
				+ "Activate Location services and also access for apps.\n"
				+ "2) No access to location\n"
				+ "Start powershell with admin privileges.\n"
				+ "Enter the following line:\n"
				+ "Set-ExecutionPolicy -ExecutionPolicy RemoteSigned\n"
				+ "Apply changes.";
		
		try
		{
			File file = File.createTempFile("script", ".ps1");
			Files.writeString(Path.of(file.getPath()), script);
			StringBuffer out = new StringBuffer();
			StringBuffer err = new StringBuffer();
			
			Process p = Runtime.getRuntime().exec("powershell " + file.getAbsolutePath());
			BufferedReader brIn = new BufferedReader(new InputStreamReader(p.getInputStream())); // On Java17 p.inputReader()
			brIn.lines().forEach((l) -> out.append(l));
			BufferedReader brEr = new BufferedReader(new InputStreamReader(p.getErrorStream())); // On Java17 p.errorReader()
			brEr.lines().forEach((l) -> err.append(l));
			//p.inputReader().lines().forEach((l) -> out.append(l));
			//p.errorReader().lines().forEach((l) -> err.append(l));
			int exitVal = p.waitFor();
			if (exitVal != 0)
			{
				throw new IOException("powershell-error:\n" + err.toString());
			}
			return new PsTpvReport(out.toString());
		}
		catch (IOException|InterruptedException e)
		{
			ErrorMessage msg = new ErrorMessage() { @Override public String getMessage() { return e.getClass().getName() + e.getMessage() + "\n\n" + help; } };
			errorHandler.accept(msg);
		}
		return null;
	}
	
	public void setCycleTime(long cycleTime)
	{
		this.cycleTime = cycleTime;
	}

	@Override
	public void start()
	{
		log.info("Starting PsClient.");
		watchingRun = true;
		watchThread.start();
	}

	@Override
	public void stop()
	{
		watchingRun = false;
		log.info("Stopping PsClient.");
	}

	@Override
	public void sendPollCommand(Consumer<PollMessage> responseHandler)
	{
		PsTpvReport r = executeScript();
		if (r==null) { return; }
		responseHandler.accept(new StaticPollMessage(r));
	}

	@Override
	public void watch(boolean enable, boolean reportMessages)
	{
		watchingSkip = !(enable && reportMessages);
	}
	
	private void watchJob()
	{
		try
		{
			while (watchingRun)
			{
				Thread.sleep(cycleTime);
				if (watchingSkip) { continue; }
				if (tpvHandler==null) { continue; }
				PsTpvReport r = executeScript();
				if (r == null) { continue; }
				tpvHandler.accept(r);
			}
			log.info("WatchThread: End of job");
		}
		catch (InterruptedException e)
		{
			log.error("Error",e);
		}
	}
	
	@Override
	public void addTpvHandler(Consumer<TPVReport> tpvHandler)
	{
		this.tpvHandler = tpvHandler;
	}

	@Override
	public void addErrorHandler(Consumer<ErrorMessage> errorHandler)
	{
		if (errorHandler == null) { throw new IllegalStateException("ErrorHandler must not be null."); }
		this.errorHandler = errorHandler;
	}

}
