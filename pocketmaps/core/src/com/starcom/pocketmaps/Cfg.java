package com.starcom.pocketmaps;

import java.io.File;
import java.io.IOException;
import com.starcom.io.ManifestReader;
import com.starcom.navigation.Enums;

public class Cfg
{
	public final static String TRAVEL_MODE_BIKE = Enums.Vehicle.Bike.toString().toLowerCase();
	public final static String TRAVEL_MODE_CAR = Enums.Vehicle.Car.toString().toLowerCase();
	public final static String TRAVEL_MODE_FOOT = Enums.Vehicle.Foot.toString().toLowerCase();
	public enum NavKey { TravelMode, Weighting, TtsEngine, TtsWantedVoice, MapSelection }
	public enum NavKeyB { DirectionsOn, IsImperialUnit, ShowingSpeedLimits, SpeakingSpeedLimits, TtsOn, Debugging, GpsOn }
	public enum GeoKeyI { SearchBits }
	public enum GeoKey { OfflineCountry }
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

	public static void setBoolValue(NavKeyB key, boolean val)
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
	 * @param fname The name of configuration-type, should use an enum instead string */
	public static boolean save(ConfType fname)
	{
		return Settings.save(fname.toString());
	}

	/** Returns a version that has to match the versions of mapdata.
	 * @return The version, or null, if any manifest information is missing. */
	public static String getMapdataVersion()
	{
		try
		{
			String ghVersion = ManifestReader.readFirstMainAttribute("NaviRouterVersion");
			String mapVersion = ManifestReader.readFirstMainAttribute("MapVersion");
			if (ghVersion != null && mapVersion != null)
			{
				return mapVersion + "-" + ghVersion + "_0";
			}
		}
		catch (IOException e) { e.printStackTrace(); }
		return null;
	}

}
