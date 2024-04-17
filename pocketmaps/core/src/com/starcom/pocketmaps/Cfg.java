package com.starcom.pocketmaps;

import java.io.File;

public class Cfg
{
	public final static String TRAVEL_MODE_BIKE = "bike";
	public final static String TRAVEL_MODE_CAR = "car";
	public final static String TRAVEL_MODE_FOOT = "foot";
	public enum NavKey { TravelMode, Weighting, TtsEngine, TtsWantedVoice, MapSelection }
	public enum NavKeyB { DirectionsOn, IsImperialUnit, ShowingSpeedLimits, SpeakingSpeedLimits, TtsOn }
	public enum GeoKeyI { SearchBits }
	public enum GeoKey { offlineCountry }
	public enum ConfType { Navigation, Geocoding } //TODO: old used 'base' unuseable, better 'SearchHints'
	
	public static void setDirectory(File newDir)
	{
		Settings.setDirectory(newDir);
	}
	
	public static boolean getBoolValue(NavKeyB key, boolean def)
	{
		return Settings.getBoolValue(ConfType.Navigation.toString(), key.toString(), def);
	}
	
	public static int getIntValue(NavKey key, int def)
	{
		return Settings.getIntValue(ConfType.Navigation.toString(), key.toString(), def);
	}
	
	public static float getFloatValue(NavKey key, float def)
	{
		return Settings.getFloatValue(ConfType.Navigation.toString(), key.toString(), def);
	}
	
	public static String getValue(NavKey key, String def)
	{
		return Settings.getValue(ConfType.Navigation.toString(), key.toString(), def);
	}
	
	public static String getValue(GeoKey key, String def)
	{
		return Settings.getValue(ConfType.Geocoding.toString(), key.toString(), def);
	}
	
	public static int getIntValue(GeoKeyI key, int def)
	{
		return Settings.getIntValue(ConfType.Geocoding.toString(), key.toString(), def);
	}
	
	public static void setValue(NavKey key, String val)
	{
		Settings.setValue(ConfType.Navigation.toString(), key.toString(), val);
	}

	public static void setBoolValue(NavKey key, boolean val)
	{
		Settings.setValue(ConfType.Navigation.toString(), key.toString(), "" + val);
	}

	public static void setIntValue(NavKey key, int val)
	{
		Settings.setValue(ConfType.Navigation.toString(), key.toString(), "" + val);
	}

	public static void setFloatValue(NavKey key, float val)
	{
		Settings.setValue(ConfType.Navigation.toString(), key.toString(), "" + val);
	}
	
	public static void setValue(GeoKey key, String val)
	{
		Settings.setValue(ConfType.Geocoding.toString(), key.toString(), val);
	}

	public static void setIntValue(GeoKeyI key, int val)
	{
		Settings.setValue(ConfType.Geocoding.toString(), key.toString(), "" + val);
	}
	
	/** Stores the settings with name.
	 * @param fname The name of configruation-type, should use an enum instead string */
	public static boolean save(ConfType fname)
	{
		return Settings.save(fname.toString());
	}

}
