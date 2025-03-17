package com.starcom.navigation;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import com.starcom.navigation.MapRoutingEngine.Instruct;
import com.starcom.navigation.MapRoutingEngine.NaviResponse;
import com.starcom.navigation.MapRoutingEngine.PathInfo;
import com.starcom.navigation.MapRoutingEngine.Point;
import com.starcom.navigation.MapRoutingEngine.Sign;

public interface MapRoutingEngine
{
	public static MapRoutingEngine createInstance()
	{
		try
		{
			return (MapRoutingEngine)Class.forName("com.starcom.navigation.MapRoutingEngineImpl").getConstructor().newInstance();
		} catch (InstantiationException e)
		{
			e.printStackTrace();
		} catch (IllegalAccessException e)
		{
			e.printStackTrace();
		} catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		} catch (InvocationTargetException e)
		{
			e.printStackTrace();
		} catch (NoSuchMethodException e)
		{
			e.printStackTrace();
		} catch (SecurityException e)
		{
			e.printStackTrace();
		} catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	/** Initializes the engine and returns MapRoutingEngine to consumer, when ready. */
	public void init(File mapFolder);
	
	/** Free all resources. */
	public void close();
	
	//if (Cfg.getValue(NavKey.TravelMode, Cfg.TRAVEL_MODE_CAR).equals(Cfg.TRAVEL_MODE_BIKE)) { hours = hours * 3.0; }
	//if (Cfg.getValue(NavKey.TravelMode, Cfg.TRAVEL_MODE_CAR).equals(Cfg.TRAVEL_MODE_FOOT)) { hours = hours * 9.0; }
	/** Creates a simple line reponse, when map is missing. */
	public default NaviResponse createSimpleResponse(double fromLat, double fromLon, double toLat, double toLon, Enums.Vehicle vehicle)
	{
	    double distance = GeoMath.fastDistance(fromLat, fromLon, toLat, toLon) * GeoMath.METER_PER_DEGREE;
	    double distKm = distance / 1000.0;
	    double hours = distKm / 50; // 50km == 1h
	    if (vehicle == Enums.Vehicle.Bike) { hours = hours * 3.0; }
	    if (vehicle == Enums.Vehicle.Foot) { hours = hours * 9.0; }
	    long timeMs = (long)(hours * 60.0 * 60.0 * 1000.0);
	    ArrayList<Point> points = new ArrayList<Point>();
	    Point p = new Point(fromLat, fromLon, 0);
	    points.add(p);
	    p = new Point(toLat, toLon, 0);
	    points.add(p);
	    ArrayList<Instruct> insL = new ArrayList<>();
	    Instruct ins = new Instruct(points, distance, Sign.Finish, "Simple direction", timeMs);
	    insL.add(ins);
	    NaviResponse resp = new NaviResponse()
	    {
			@Override
			public double getDistance()
			{
				return distance;
			}

			@Override
			public long getTime()
			{
				return timeMs;
			}

			@Override
			public ArrayList<Point> getPoints()
			{
				return points;
			}

			@Override
			public ArrayList<Instruct> getInstructions()
			{
				return insL;
			}

			@Override
			public ArrayList<PathInfo> getMaxSpeedInfos()
			{
				return new ArrayList<>();
			}

			@Override
			public ArrayList<PathInfo> getAveSpeedInfos()
			{
				return new ArrayList<>();
			}

			@Override
			public boolean isSimpleLine()
			{
				return true;
			}
	    	
	    };
	    return resp;
	}
	
	/** Creates a routing reponse. May return null in case of errors or still not ready. */
	public NaviResponse createResponse(double fromLat, double fromLon, double toLat, double toLon, Enums.Vehicle vehicle);
	
	/** May return null, if nothing is in range. */
	public static Instruct findNearestInstruction(Point pFrom, ArrayList<Instruct> list, double maxRange)
	{
		Instruct nearestI = null;
		double nearestD = Double.MAX_VALUE;
		for (Instruct i : list)
		{
			for (Point p : i.points)
			{
				double diff = p.lat - pFrom.lat;
				if (diff > maxRange || diff < -maxRange)
				{
					continue;
				}
				diff = p.lon - pFrom.lon;
				if (diff > maxRange || diff < -maxRange)
				{
					continue;
				}
				diff = GeoMath.fastDistance(nearestD, nearestD, nearestD, nearestD);
				if (diff < nearestD)
				{
					nearestI = i;
				}
			}
		}
		return nearestI;
	}
	
	public void findClosestStreet(Point from, Point store);

	public ListIF<Edge> getAllEdges();
	
	public static class Point
	{
		public double lat;
		public double lon;
		public double ele;
		public Point(double lat, double lon, double ele)
		{
			this.lat = lat;
			this.lon = lon;
			this.ele = ele;
		}
		public Point() {}
	}

	public static interface NaviResponse
	{
		/** Returns the full distance in meters. */
		public double getDistance();
		/** Returns the full time-duration in ms. */
		public long getTime();
		public ArrayList<Point> getPoints();
		public ArrayList<Instruct> getInstructions();
		public ArrayList<PathInfo> getMaxSpeedInfos();
		public ArrayList<PathInfo> getAveSpeedInfos();
		public boolean isSimpleLine();
	}
	
	public static class PathInfo
	{
		public int first;
		public int last;
		public Object value;
	}
	
	public static class Edge
	{
		public String name = "";
		public Location loc = new Location(0,0,0,0);
	}
	
	public enum Sign {Finish, LeaveRoundabout, TurnSharpLeft, TurnLeft, TurnSlightLeft, ContinueOnStreet,
		TurnSlightRight, TurnRight, TurnSharpRight, ReachedVia, UseRoundabout, KeepRight, KeepLeft }
	
	public static class Instruct
	{
		public ArrayList<Point> points;
		public double distance;
		public Sign sign;
		public String name;
		public long time;
		public String annotation;
		public Instruct(ArrayList<Point> points, double distance, Sign sign, String name, long time)
		{
			this.points = points;
			this.distance = distance;
			this.sign = sign;
			this.name = name;
			this.time = time;
		}
	}

	public static interface ListIF<T>
	{
		public int getSize();
		public boolean next(T store);
		
		public static <T> ListIF<T> createEmpty()
		{
			ListIF<T> l = new ListIF<T>()
			{
				@Override public boolean next(T store) { return false; }
				@Override public int getSize() { return 0; }
			};
			return l;
		}
	}

}
