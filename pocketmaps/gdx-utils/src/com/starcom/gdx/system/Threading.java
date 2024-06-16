package com.starcom.gdx.system;

import com.badlogic.gdx.Gdx;
import com.starcom.interfaces.IObjectListener;
import com.starcom.interfaces.IObjectResponse;

public class Threading
{
	private static Threading instance;
	long tid;
	
	public static Threading getInstance()
	{
		if (instance == null) { instance = new Threading(); }
		return instance;
	}
	
	private Threading() {} // Make it private. Use getInstance().

	/** Must be run on init of app. */
	public void init()
	{
		tid = Thread.currentThread().getId();
	}
	
	public boolean isMainThread()
	{
		return tid == Thread.currentThread().getId();
	}
	
	public void invokeOnMainThread(Runnable run) { Gdx.app.postRunnable(run); }
	public void invokeOnMainThread(IObjectListener run, Object arg) { Gdx.app.postRunnable(() -> run.run(arg)); }
	
	/** Ensures, that not running on ui thread. */
	public void invokeOnWorkerThread(Runnable run)
	{
		if(isMainThread()) { new Thread(run).start(); }
		else { run.run(); }
	}
	
	public void invokeLater(Runnable run, long timeMS)
	{
		new Thread( () ->
		{
			try { Thread.sleep(timeMS); } catch (Exception e) { e.printStackTrace(); }
			run.run();
		}).start();
	}
	
	/** Ensures, that not running on ui thread.
	 * Also catches Exceptions of asyncTask and passes it through runMain 
	 * @param runAsync The task that runs in an async thread and returns a result.
	 * @param runMain The task, that runs on UI thread afterwards, with result-object or an exception-object, can return anything.
	 * @return The runnable, or when async it is a thread object. */
	public Runnable invokeAsyncTask(IObjectResponse runAsync, IObjectListener runMain)
	{
		Runnable run = () ->
		{
			Object result;
			try
			{
				result = runAsync.run();
			}
			catch(Exception e)
			{
				result = e;
			}
			invokeOnMainThread(runMain, result);
		};
		if(isMainThread()) { Thread t = new Thread(run); t.start(); return t; }
		else { run.run(); return run; }
	}
	
	/** Easy check of returned Object of invokeAsyncTask whether still running.
	 * @param run The returned value, may be a Runnable or a Thread.
	 * @return Whether the async task is still running. */
	public boolean isRunning(Runnable run)
	{
		if (run == null) { return false; }
		if (run instanceof Thread) { return ((Thread) run).isAlive(); }
		return false;
	}
}
