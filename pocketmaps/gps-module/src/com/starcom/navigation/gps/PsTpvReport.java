package com.starcom.navigation.gps;

import com.ivkos.gpsd4j.messages.reports.TPVReport;

public class PsTpvReport extends TPVReport
{
	String out[];
	
	/** Creates a TPVReport wrapper.
	 * @param out The output of windows location service via powershell */
	public PsTpvReport(String out)
	{
		this.out = out.split("\\r?\\n");
	}
	
	private double getDouble(String name)
	{
		String s = getString(name);
		if (s == null) { return Double.NaN; }
		if (s.equals("NaN")) { return Double.NaN; }
		return Double.parseDouble(s);
	}
	
	private String getString(String name)
	{
		for (String line : out)
		{
			if (line.contains(":") && line.trim().startsWith(name + " "))
			{
				return line.split(":")[1].trim();
			}
		}
		return null;
	}
	
	@Override public Double getLongitude() { return getDouble("Longitude"); }
	@Override public Double getLatitude() { return getDouble("Latitude"); }
	@Override public Double getAltitude() { return getDouble("Altitude"); }
	@Override public Double getSpeed() { return getDouble("Speed"); }
}
