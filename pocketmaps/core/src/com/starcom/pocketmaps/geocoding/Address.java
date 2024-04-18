package com.starcom.pocketmaps.geocoding;

import java.util.Locale;

public class Address
{
	public double latitude, longitude;
	public Locale locale;
	public String featureName;
	public String thoroughfare; //street, road
	public String url;
	public String postalCode;
	public String subThoroughfare;
	public String premises;
	public String adminArea;
	public String subAdminArea;
	public String countryCode;
	public String countryName;
	public String subLocality;
	public String phone;
	public String locality; //city, town, village
	public StringBuilder addressLines = new StringBuilder();

	public Address(Locale locale)
	{
		this.locale = locale;
	}
	
	/** Clears all entries for reuse. */
	public void clear()
	{
		latitude = 0;
		longitude = 0;
		featureName = null;
		thoroughfare = null;
		url = null;
		postalCode = null;
		subThoroughfare = null;
		premises = null;
		adminArea = null;
		subAdminArea = null;
		countryCode = null;
		countryName = null;
		subLocality = null;
		phone = null;
		locality = null;
		addressLines = new StringBuilder();
	}
	
	public String toNiceString()
	{
		StringBuffer sb = new StringBuffer();
		String sep = "";
		if (featureName != null) { sb.append(sep).append(featureName); sep = ", "; }
		if (locality != null) { sb.append(sep).append(locality); sep = ", "; }
		if (thoroughfare != null) { sb.append(sep).append(thoroughfare); sep = ", "; }
		if (sb.isEmpty()) { sb.append("lat=").append(latitude).append(", lon=").append(longitude); }
		return sb.toString();
	}
	
	@Override public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("featureName=").append(featureName).append("\n");
		sb.append("locale=").append(locale).append("\n");
		sb.append("latitude=").append(latitude).append("\n");
		sb.append("longitude=").append(longitude).append("\n");
		sb.append("thoroughfare=").append(thoroughfare).append("\n");
		sb.append("url=").append(url).append("\n");
		sb.append("postalCode=").append(postalCode).append("\n");
		sb.append("subThoroughfare=").append(subThoroughfare).append("\n");
		sb.append("premises=").append(premises).append("\n");
		sb.append("adminArea=").append(adminArea).append("\n");
		sb.append("subAdminArea=").append(subAdminArea).append("\n");
		sb.append("countryCode=").append(countryCode).append("\n");
		sb.append("countryName=").append(countryName).append("\n");
		sb.append("locality=").append(locality).append("\n");
		sb.append("subLocality=").append(subLocality).append("\n");
		sb.append("phone=").append(phone).append("\n");
		int idx = 0;
		for (String line : addressLines.toString().split("\n"))
		{
			idx++;
			sb.append("addressLine").append(idx).append("=").append(line).append("\n");
		}
		return sb.toString();
	  }
}
