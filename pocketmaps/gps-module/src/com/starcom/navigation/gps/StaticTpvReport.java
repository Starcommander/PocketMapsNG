package com.starcom.navigation.gps;

import com.ivkos.gpsd4j.messages.reports.TPVReport;

public class StaticTpvReport extends TPVReport
{
	double lat,lon,alt,speed;
	
	/** Creates a TPVReport wrapper.
	 * @param out The output of windows location service via powershell */
	public StaticTpvReport(double lat, double lon, double alt, double speed)
	{
		this.lat = lat;
		this.lon = lon;
		this.alt = alt;
		this.speed = speed;
	}
	
	@Override public Double getLongitude() { return lat; }
	@Override public Double getLatitude() { return lon; }
	@Override public Double getAltitude() { return alt; }
	@Override public Double getSpeed() { return speed; }
}
