package com.starcom.pocketmaps;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.starcom.LoggerUtil;

public class Settings
{ //TODO: Move this class to base
	private static Settings instance = new Settings();
	private File dir;
	
	private HashMap<String,Properties> prop = new HashMap<String,Properties>();
	private Logger logger = LoggerUtil.get(Settings.class);
	
	/** Sets the directory, where to store the settings. */
	public static void setDirectory(File newDir)
	{
		if (instance.dir != null)
		{
			throw new IllegalStateException("Directory already set");
		}
		instance.dir = newDir;
	}
	
	public static boolean getBoolValue(String fname, String key, boolean def)
	{
		String val = instance.getSettings(fname).getProperty(key);
		if (val == null) return def;
		return val.equals("" + true);
	}
	
	public static int getIntValue(String fname, String key, int def)
	{
		String val = instance.getSettings(fname).getProperty(key);
		if (val == null) return def;
		return Integer.parseInt(val);
	}
	
	public static float getFloatValue(String fname, String key, float def)
	{
		String val = instance.getSettings(fname).getProperty(key);
		if (val == null) return def;
		return Float.parseFloat(val);
	}
	
	public static String getValue(String fname, String key, String def)
	{
		return instance.getSettings(fname).getProperty(key,def);
	}
	
	public static void setValue(String fname, String key, String val)
	{
		instance.getSettings(fname).setProperty(key, val);
	}
	
	/** Get the settings from name.
	 * @param fname The name of configruation-type, should use an enum instead string */
	private Properties getSettings(String fname)
	{
		if (dir == null || !dir.isDirectory())
		{
			throw new IllegalStateException("Set directory first correctly");
		}
		Properties settings = prop.get(fname);
		if (settings != null) { return settings; }
		settings = new Properties();
		File propFile = new File(dir, fname + ".properties");
		if (propFile.exists())
		{
			try (FileInputStream fis = new FileInputStream(propFile))
			{
				settings.load(fis);
				
			}
			catch (IOException e)
			{
				logger.log(Level.SEVERE, "Error reading settings", e);
			}
		}
		prop.put(fname, settings);
		return settings;
	}

	/** Stores the settings with name.
	 * @param fname The name of configruation-type, should use an enum instead string */
	public static boolean save(String fname)
	{
		File storeFile = new File(instance.dir, fname + ".properties");
		Properties p = instance.getSettings(fname);
		try (FileOutputStream fos = new FileOutputStream(storeFile))
		{
		  p.store(fos, fname);
		}
		catch (IOException e)
		{
			instance.logger.severe("" + e);
			return false;
		}
		return true;
	}

}
