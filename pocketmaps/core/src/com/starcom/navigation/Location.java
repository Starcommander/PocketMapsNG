package com.starcom.navigation;

public class Location
{
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
		return 50; //TODO: Calculate distance
	}
	
	public float bearingTo(Location other)
	{
		return 0; //TODO: Calculate bearing
	}
}
