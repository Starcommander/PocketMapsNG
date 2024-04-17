package com.starcom.pocketmaps.geocoding;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import org.osmdroid.location.GeocoderNominatim;

import com.starcom.LoggerUtil;
import com.starcom.interfaces.IProgressListener;

public class GeocoderGlobal
{
  Logger logger = LoggerUtil.get(GeocoderGlobal.class);
  private static boolean stopping = false;
  Locale locale;
  
  public GeocoderGlobal(Locale locale)
  {
    this.locale = locale;
  }
  
  public static void stopRunningActions()
  {
    stopping = true;
  }
  
  protected static boolean isStopRunningActions()
  {
    return stopping;
  }
  
  public List<Address> find_google(String searchS)
  {
//    stopping = false;
//    log("Google geocoding started");
//    Geocoder geocoder = new Geocoder(context, locale);
//    if (!Geocoder.isPresent()) { return null; }
//    try
//    {
//      List<Address> result = geocoder.getFromLocationName(searchS, 50);
//      return result;
//    }
//    catch (IOException e)
//    {
//      e.printStackTrace();
//    }
	//TODO: Implement this
    return null;
  }
  
  public List<Address> find_osm(String searchS)
  {
    stopping = false;
    logger.info("OSM geocoding started");
    logger.info("OSM geocoding started");
    GeocoderNominatim geocoder = new GeocoderNominatim(locale);
    if (!GeocoderNominatim.isPresent()) { return null; }
    try
    {
      List<Address> result = geocoder.getFromLocationName(searchS, 50);
      return result;
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return null;
  }
  
  public List<Address> find_local(String searchCountry, String searchS, IProgressListener progressListener)
  {
    stopping = false;
    logger.info("Offline geocoding started");
    GeocoderLocal geocoder = new GeocoderLocal(locale);
    if (!GeocoderLocal.isPresent()) { return null; }
    try
    {
      List<Address> result = geocoder.getFromLocationName(searchCountry, searchS, 50, progressListener);
      return result;
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return null;
  }
}
