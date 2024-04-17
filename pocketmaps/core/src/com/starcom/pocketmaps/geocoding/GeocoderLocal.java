package com.starcom.pocketmaps.geocoding;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

import com.graphhopper.routing.util.AllEdgesIterator;
import com.starcom.pocketmaps.Cfg;
import com.starcom.pocketmaps.Cfg.NavKey;
import com.starcom.pocketmaps.map.MapHandler;
import com.starcom.pocketmaps.util.GeoMath;
import com.starcom.pocketmaps.views.MapList;
import com.starcom.LoggerUtil;
import com.starcom.interfaces.IProgressListener;
import com.starcom.interfaces.IProgressListener.Type;
import com.starcom.pocketmaps.map.MapLayer.MapFileType;

public class GeocoderLocal
{
	Logger logger = LoggerUtil.get(GeocoderLocal.class);
//  public final static int ADDR_TYPE_FOUND = 0;
//  public final static int ADDR_TYPE_CITY = 1;
//  public final static int ADDR_TYPE_CITY_EN = 2;
//  public final static int ADDR_TYPE_POSTCODE = 3;
//  public final static int ADDR_TYPE_STREET = 4;
//  public final static int ADDR_TYPE_COUNTRY = 5;
  public final static int BIT_MULT = 1;
  public final static int BIT_EXPL = 2;
  public final static int BIT_CITY = 4;
  public final static int BIT_STREET = 8;

  private Locale locale;
  private boolean bMultiMatchOnly;
  private boolean bExplicitSearch;
  private boolean bStreetNodes;
  private boolean bCityNodes;
  
  public GeocoderLocal(Locale locale)
  {
    this.locale = locale;
  }
  
  public static boolean isPresent() { return true; }
  
  
  public List<Address> getFromLocationName(String searchCountry, String searchS, int maxCount, IProgressListener progressListener) throws IOException
  {
    getSettings();
    progressListener.onProgress(Type.PROGRESS, "Searching city...");
    ArrayList<Address> addrList = new ArrayList<Address>();
    if (bCityNodes && !bMultiMatchOnly)
    {
      ArrayList<Address> nodes = findCity(searchCountry, searchS, maxCount);
      if (nodes == null) { return null; }
      addrList.addAll(nodes);
    }
    progressListener.onProgress(Type.PROGRESS, "Searching streets...");
    if (addrList.size() < maxCount && bStreetNodes)
    {
      ArrayList<Address> nodes = searchNodes(searchCountry, searchS, maxCount - addrList.size(), progressListener);
      if (nodes == null) { return null; }
      addrList.addAll(nodes);
    }
    return addrList;
  }

  private void getSettings()
  {
	int bits = Cfg.getIntValue(Cfg.GeoKeyI.SearchBits, 6);
    bMultiMatchOnly = (bits & BIT_MULT) > 0;
    bExplicitSearch = (bits & BIT_EXPL) > 0;
    bCityNodes = (bits & BIT_CITY) > 0;
    bStreetNodes = (bits & BIT_STREET) > 0;
  }
  
  /** For more information of street-matches. **/
  private String findNearestCity(String searchCountry, double lat, double lon)
  {
	String mapsPath = new File(MapList.getInstance().findMapLayerFromCountry(searchCountry).getMapFile(MapFileType.FullPath)).getParent();
    mapsPath = new File(mapsPath, "city_nodes.txt").getPath();
    String nearestName = null;
    double nearestDist = 0;
    String curName = "";
    double curLat = 0;
    double curLon = 0;
    double curDist = 0;
    try(FileReader r = new FileReader(mapsPath);
        BufferedReader br = new BufferedReader(r))
    {
      String line;
      while ((line = br.readLine())!=null)
      {
        if (GeocoderGlobal.isStopRunningActions()) { return null; }
        if (line.startsWith("name="))
        {
          curName = readSubString("name", line);
        }
        else if (line.startsWith("name:en="))
        {
          if (curName.isEmpty())
          {
            curName = readSubString("name:en", line);
          }
        }
        else if (line.startsWith("lat="))
        {
          curLat = readSubDouble("lat", line);
        }
        else if (line.startsWith("lon="))
        {
          if (curName.isEmpty()) { continue; }
          curLon = readSubDouble("lon", line);
          curDist = GeoMath.fastDistance(curLat, curLon, lat, lon);
          if (nearestName == null || curDist < nearestDist)
          {
            nearestDist = curDist;
            nearestName = curName;
          }
        }
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    return nearestName;
  }
  
  private ArrayList<Address> findCity(String searchCountry, String searchS, int maxCount) throws IOException
  {
    ArrayList<Address> result = new ArrayList<Address>();
    CityMatcher cityMatcher = new CityMatcher(searchS, bExplicitSearch);

	String mapsPath = new File(MapList.getInstance().findMapLayerFromCountry(searchCountry).getMapFile(MapFileType.FullPath)).getParent();
    mapsPath = new File(mapsPath, "city_nodes.txt").getPath();
    Address curAddress = new Address(locale);
    try(FileReader r = new FileReader(mapsPath);
        BufferedReader br = new BufferedReader(r))
    {
      String line;
      while ((line = br.readLine())!=null)
      {
        if (GeocoderGlobal.isStopRunningActions()) { return null; }
        if (result.size() >= maxCount) { break; }
        if (line.startsWith("name="))
        {
          curAddress = clearAddress(curAddress);
          String name = readSubString("name", line);
          curAddress.locality = name;
          boolean isMatching = cityMatcher.isMatching(name, false);
          if (isMatching)
          {
            result.add(curAddress);
            curAddress.countryName = searchCountry;
            curAddress.featureName = name;
            logger.info("Added address: " + name);
          }
        }
        else if (line.startsWith("name:en="))
        {
          String name = readSubString("name:en", line);
          curAddress.addressLines.append(name).append("\n");
          if (curAddress.countryName == null)
          { // Is still not attached!
            boolean isMatching = cityMatcher.isMatching(name, false);
            if (isMatching)
            {
              result.add(curAddress);
              curAddress.countryName = searchCountry;
              curAddress.featureName =  name;
              logger.info("Added address: " + name);
            }
          }
        }
        else if (line.startsWith("post="))
        {
          String name = readSubString("post", line);
          curAddress.postalCode = name;
          if (curAddress.countryName == null)
          { // Is still not attached!
            boolean isMatching = cityMatcher.isMatching(name, true);
            if (isMatching)
            {
              result.add(curAddress);
              curAddress.countryName = searchCountry;
              curAddress.featureName = name;
              logger.info("Added address: " + name);
            }
          }
        }
        else if (line.startsWith("lat="))
        {
          curAddress.latitude = readSubDouble("lat", line);
        }
        else if (line.startsWith("lon="))
        {
          curAddress.longitude = readSubDouble("lon", line);
        }
      }
    }
    catch (IOException e)
    {
      throw e;
    }
    return result;
  }
  
  private Address clearAddress(Address curAddress)
  {
    if (curAddress.countryName == null)
    { // Clear this curAddress for reuse!
      curAddress.clear();
    }
    else { curAddress = new Address(locale); }
    return curAddress;
  }

  private double readSubDouble(String key, String txt)
  {
    String s = readSubString(key, txt);
    if (s==null)
    {
      logger.severe("Double for key not found: " + key);
      return 0;
    }
    return Double.parseDouble(s);
  }
  
  private String readSubString(String key, String txt)
  {
    return txt.substring(key.length()+1);
  }
  
  /** Search all edges for matching text. **/
  ArrayList<Address> searchNodes(String searchCountry, String txt, int maxMatches, IProgressListener progressListener)
  {
    txt = txt.toLowerCase();
    ArrayList<Address> addressList = new ArrayList<Address>();
    StreetMatcher streetMatcher = new StreetMatcher(txt, bExplicitSearch);
    CityMatcher cityMatcher = streetMatcher;
    
    AllEdgesIterator edgeList = MapHandler.getInstance().getAllEdges(searchCountry);
    if (edgeList == null) { return null; }
    logger.info("SEARCH_EDGE Start ...");
    int counter = 0;
    int lastProgress = 5;
    while (edgeList.next())
    {
      counter++;
      if (GeocoderGlobal.isStopRunningActions()) { return null; }
      if (edgeList.getName().isEmpty()) { continue; }
      if (edgeList.fetchWayGeometry(0).isEmpty()) { continue; }
      int newProgress = (counter*100) / edgeList.length();
      if (newProgress > lastProgress)
      {
        progressListener.onProgress(IProgressListener.Type.PROGRESS, "" + ((counter*100) / edgeList.length()) + "%");
        lastProgress = newProgress;
      }
      if (streetMatcher.isMatching(edgeList.getName(), false))
      {
    	logger.info("SEARCH_EDGE Status: " + counter + "/" + edgeList.length());
        boolean b = StreetMatcher.addToList(searchCountry,
                                            addressList,
                                            edgeList.getName(),
                                            edgeList.fetchWayGeometry(0).get(0).lat,
                                            edgeList.fetchWayGeometry(0).get(0).lon,
                                            locale);
        if (b)
        {
          String c = findNearestCity(searchCountry, edgeList.fetchWayGeometry(0).get(0).lat, edgeList.fetchWayGeometry(0).get(0).lon);
          if (bMultiMatchOnly && !cityMatcher.isMatching(c, false))
          {
            addressList.remove(addressList.size()-1);
          }
          else
          {
        	logger.info("SEARCH_EDGE found=" + edgeList.getName() + " on " + c);
            addressList.get(addressList.size()-1).locality = c;
          }
        }
      }
      if (addressList.size() >= maxMatches) { break; }
    }
    logger.info("SEARCH_EDGE Stop on length=" + addressList.size());
    return addressList;
  }

}
