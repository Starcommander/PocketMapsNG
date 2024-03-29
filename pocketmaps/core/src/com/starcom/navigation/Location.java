package com.starcom.navigation;

public class Location
{
	BearingDistanceCache cache = new BearingDistanceCache();
	float speed;
	float lat;
	float lon;
	float alt;
	float bearing;
	long time;
	
	public Location(float lat, float lon, float speed, float bearing, float alt, long time)
	{
		set(lat, lon, speed, bearing, alt, time);
	}
	public Location(float lat, float lon, float speed, float bearing)
	{
		set(lat, lon, speed, bearing, 0.0f, System.currentTimeMillis());
	}
	public float getSpeed() { return speed; }
	public float getLatitude() { return lat; }
	public float getLongitude() { return lon; }
	public float getAltitude() { return alt; }
	public float getBearing() { return bearing; }
	public long getTime() { return time; }
	public void set(Location other)
	{
		set(other.lat, other.lon, other.speed, other.bearing, other.alt, other.time);
	}
	public void set(float lat, float lon, float speed, float bearing, float alt, long time)
	{
		this.lat = lat;
		this.lon = lon;
		this.speed = speed;
		this.bearing = bearing;
		this.alt = alt;
		this.time = time;
	}
	
	public double distanceTo(Location other)
	{
		if (lat != cache.mLat1 || lon != cache.mLon1
				|| other.lat != cache.mLat2 || other.lon != cache.mLon2) {
			computeDistanceAndBearing(lat, lon, other.lat, other.lon, cache);
		}
		return cache.mDistance;
	}
	
	public float bearingTo(Location other)
	{
		if (lat != cache.mLat1 || lon != cache.mLon1
				|| other.lat != cache.mLat2 || other.lon != cache.mLon2) {
			computeDistanceAndBearing(lat, lon, other.lat, other.lon, cache);
		}
		return cache.mInitialBearing;
	}
	
	// Copy of androids Location.java:
	// https://android.googlesource.com/platform/frameworks/base/+/refs/heads/main/core/java/android/location/Location.java
	private static void computeDistanceAndBearing(double lat1, double lon1,
            double lat2, double lon2, BearingDistanceCache results) {
        // Based on http://www.ngs.noaa.gov/PUBS_LIB/inverse.pdf
        // using the "Inverse Formula" (section 4)
        // Convert lat/long to radians
        lat1 *= Math.PI / 180.0;
        lat2 *= Math.PI / 180.0;
        lon1 *= Math.PI / 180.0;
        lon2 *= Math.PI / 180.0;
        double a = 6378137.0; // WGS84 major axis
        double b = 6356752.3142; // WGS84 semi-major axis
        double f = (a - b) / a;
        double aSqMinusBSqOverBSq = (a * a - b * b) / (b * b);
        double l = lon2 - lon1;
        double aA = 0.0;
        double u1 = Math.atan((1.0 - f) * Math.tan(lat1));
        double u2 = Math.atan((1.0 - f) * Math.tan(lat2));
        double cosU1 = Math.cos(u1);
        double cosU2 = Math.cos(u2);
        double sinU1 = Math.sin(u1);
        double sinU2 = Math.sin(u2);
        double cosU1cosU2 = cosU1 * cosU2;
        double sinU1sinU2 = sinU1 * sinU2;
        double sigma = 0.0;
        double deltaSigma = 0.0;
        double cosSqAlpha;
        double cos2SM;
        double cosSigma;
        double sinSigma;
        double cosLambda = 0.0;
        double sinLambda = 0.0;
        double lambda = l; // initial guess
        for (int iter = 0; iter < 20; iter++) {
            double lambdaOrig = lambda;
            cosLambda = Math.cos(lambda);
            sinLambda = Math.sin(lambda);
            double t1 = cosU2 * sinLambda;
            double t2 = cosU1 * sinU2 - sinU1 * cosU2 * cosLambda;
            double sinSqSigma = t1 * t1 + t2 * t2;
            sinSigma = Math.sqrt(sinSqSigma);
            cosSigma = sinU1sinU2 + cosU1cosU2 * cosLambda;
            sigma = Math.atan2(sinSigma, cosSigma);
            double sinAlpha = (sinSigma == 0) ? 0.0 :
                    cosU1cosU2 * sinLambda / sinSigma;
            cosSqAlpha = 1.0 - sinAlpha * sinAlpha;
            cos2SM = (cosSqAlpha == 0) ? 0.0 : cosSigma - 2.0 * sinU1sinU2 / cosSqAlpha;
            double uSquared = cosSqAlpha * aSqMinusBSqOverBSq;
            aA = 1 + (uSquared / 16384.0) * (4096.0 + uSquared * (-768 + uSquared * (320.0
                    - 175.0 * uSquared)));
            double bB = (uSquared / 1024.0) * (256.0 + uSquared * (-128.0 + uSquared * (74.0
                    - 47.0 * uSquared)));
            double cC = (f / 16.0) * cosSqAlpha * (4.0 + f * (4.0 - 3.0 * cosSqAlpha));
            double cos2SMSq = cos2SM * cos2SM;
            deltaSigma = bB * sinSigma * (cos2SM + (bB / 4.0) * (cosSigma * (-1.0 + 2.0 * cos2SMSq)
                    - (bB / 6.0) * cos2SM * (-3.0 + 4.0 * sinSigma * sinSigma) * (-3.0
                    + 4.0 * cos2SMSq)));
            lambda = l + (1.0 - cC) * f * sinAlpha * (sigma + cC * sinSigma * (cos2SM
                    + cC * cosSigma * (-1.0 + 2.0 * cos2SM * cos2SM)));
            double delta = (lambda - lambdaOrig) / lambda;
            if (Math.abs(delta) < 1.0e-12) {
                break;
            }
        }
        results.mDistance = (float) (b * aA * (sigma - deltaSigma));
        float initialBearing = (float) Math.atan2(cosU2 * sinLambda,
                cosU1 * sinU2 - sinU1 * cosU2 * cosLambda);
        initialBearing = (float) (initialBearing * (180.0 / Math.PI));
        results.mInitialBearing = initialBearing;
        float finalBearing = (float) Math.atan2(cosU1 * sinLambda,
                -sinU1 * cosU2 + cosU1 * sinU2 * cosLambda);
        finalBearing = (float) (finalBearing * (180.0 / Math.PI));
        results.mFinalBearing = finalBearing;
        results.mLat1 = lat1;
        results.mLat2 = lat2;
        results.mLon1 = lon1;
        results.mLon2 = lon2;
    }
	
	static class BearingDistanceCache
	{
		float mDistance;
		float mInitialBearing;
		float mFinalBearing;
		double mLat1, mLat2, mLon1, mLon2;
	}
}
