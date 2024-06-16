package com.starcom.system;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.starcom.system.PendingJobs.JobCallback.StateType;

public class PendingJobs
{
	private static PendingJobs instance;
	private File jobDir;
	private HashMap<String, JobWorker> jobs = new HashMap<String, JobWorker>();
	
	private PendingJobs(File jobDir)
	{
		this.jobDir = jobDir;
	}
	
	public void init(File jobDir, JobFactory jf) throws IOException, ClassNotFoundException, NoSuchMethodException
	{
		if (instance == null)
		{
			instance = new PendingJobs(jobDir);
			instance.loadAndRunPendingJobs(jf);
		}
		else { throw new IllegalStateException("Instance of PendingJobs initialized twice."); }
	}
	
	public PendingJobs getInstance()
	{
		if (instance == null) { throw new IllegalStateException("Instance of PendingJobs not initialized."); }
		return instance;
	}
	
	public boolean hasJobs()
	{
		return jobs.size() > 0;
	}
	
//	public void clearPendingJobs()
//	{
//		for (File propFile : jobDir.listFiles())
//		{
//			safeDelete(propFile);
//		}
//	}
	
	private void loadAndRunPendingJobs(JobFactory jf) throws IOException, ClassNotFoundException, NoSuchMethodException
	{
		for (File propFile : jobDir.listFiles())
		{
			Properties prop = new Properties();
			try(FileInputStream fis = new FileInputStream(propFile))
			{
				prop.load(new FileInputStream(propFile));
				jobs.put(propFile.getName(), jf.runInThread(prop));
//				jobs.add(jf.runInThread(prop, () -> safeDelete(propFile)));
			}
		}
	}
	
	public String createAndRunJob(JobFactory jf, Properties prop) throws IOException
	{
		String id = java.util.UUID.randomUUID().toString();
		prop.store(new FileOutputStream(new File(jobDir, id)), "PendingJobs");
		jobs.put(id, jf.runInThread(prop));
		return id;
	}
	
	public JobWorker getJob(String id)
	{
		return jobs.get(id);
	}
	
	public static interface JobFactory
	{
		public JobWorker runInThread(Properties prop);
	}
	
//	public static interface JobWorkerPs
//	{
//		void run(Properties prop, JobCallback onFinish);
//		public String getProperty(String key);
//		public void setCallbackAndTrigger(JobCallback callback);
////		public int getProgress(); // In Percent
////		public String getProgressName(); //Downloading, Unzipping, Finish, ...
//		public void cancel();
//	}
	
	public static abstract class JobWorker
	{
		private File propFile;
		private Properties prop = new Properties();
		JobCallback callback = JobCallback.createNullCallback();
		private int lastPercent = 0;
		private JobCallback.StateType lastStatus = JobCallback.StateType.Progress;
		private Object lastMsg = "Undefined";
		
		public JobWorker(File propFile) throws FileNotFoundException, IOException
		{
			this.propFile = propFile;
			prop.load(new FileInputStream(propFile));
		}
		
		/** Must execute triggerStatus(i,s,m) with states. */
		abstract void run();
		public abstract void cancel();
		
		private synchronized void triggerStatusInternal(int percent, JobCallback.StateType status, Object msg, JobCallback newCallback)
		{
			if (newCallback != null) // We want to change the callback, with last state.
			{
				callback = newCallback;
			}
			else
			{ // We want to update state
				lastPercent = percent;
				lastStatus = status;
				lastMsg = msg;
				if (status != StateType.Progress) { propFile.delete(); }
			}
			callback.onProgress(lastPercent, lastStatus, lastMsg);
		}
		
		void triggerStatus(int percent, JobCallback.StateType status, Object msg)
		{
			triggerStatusInternal(percent, status, msg, null);
		}
		
		public String getProperty(String key) { return prop.getProperty(key); }
		
		/** Ensures, that the callback is also triggered now.
		 * @param The new callback, or JobCallback.createNullCallback() for stop listening. */
		public void setCallbackAndTrigger(JobCallback callback)
		{
			triggerStatusInternal(0, null, null, callback);
		}
	}
	
	public static interface JobCallback
	{
		public enum StateType {	Error, Finish, Progress	};
		public void onProgress(int percent, StateType status, Object msg);
		public static JobCallback createNullCallback() { return (p,s,m) -> {}; }
	}
}
