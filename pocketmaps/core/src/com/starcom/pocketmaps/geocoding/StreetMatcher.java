package com.starcom.pocketmaps.geocoding;

import java.util.ArrayList;
import java.util.Locale;

import com.starcom.pocketmaps.util.GeoMath;

public class StreetMatcher extends CityMatcher
{
  public StreetMatcher(String searchS, boolean explicitSearch)
  {
    super(searchS, explicitSearch);
  }
  
  public static boolean addToList(String searchCountry, ArrayList<Address> addressList, String streetName, double lat, double lon, Locale locale)
  {
    for (Address curAddr : addressList)
    {
      if (!streetName.equals(curAddr.thoroughfare)) { continue; }
      double d = GeoMath.fastDistance(lat, lon, curAddr.latitude, curAddr.longitude);
      d = d / GeoMath.DEGREE_PER_METER;
      if (d > 1000) { continue; }
      return false;
    }
    Address address = new Address(locale);
    address.countryName = searchCountry;
    address.latitude = lat;
    address.longitude = lon;
    address.thoroughfare = streetName;

    addressList.add(address);
    return true;
  }
}
