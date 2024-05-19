package com.starcom.navigation;

public interface ILocationService
{
	public void setLocationListener(ILocationListener l);
	
	/** May return null, when not present.
	 * @return The native LocationService */
	public static ILocationService getLocationService()
	{
		try
		{
			Object o = Class.forName(ILocationService.class.getPackageName() + ".impl.LocationService").getConstructor().newInstance();
			return (ILocationService)o;
		}
		catch (Exception e)
		{
			return null;
		}
	}
}
