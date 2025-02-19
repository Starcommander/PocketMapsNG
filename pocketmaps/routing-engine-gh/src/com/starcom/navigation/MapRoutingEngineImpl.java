package com.starcom.navigation;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


//import org.oscim.core.GeoPoint;

import com.starcom.LoggerUtil;
import com.starcom.navigation.Enums.Vehicle;
//import com.starcom.pocketmaps.navigator.NodeAccess;
//import com.starcom.pocketmaps.navigator.QueryResult;
//import com.starcom.pocketmaps.Cfg;
//import com.starcom.pocketmaps.map.GHRequest;
//import com.starcom.pocketmaps.map.MapLayer;
//import com.starcom.pocketmaps.map.StopWatch;
//import com.starcom.pocketmaps.navigator.NaviEngine;
//import com.starcom.pocketmaps.views.MapList;
//import com.starcom.pocketmaps.map.GraphHopper;
//import com.starcom.pocketmaps.map.MapHandler;
import com.starcom.system.Threading;
import com.graphhopper.GHRequest;
//import com.starcom.pocketmaps.Cfg;
//import com.starcom.pocketmaps.Cfg.NavKey;
//import com.starcom.pocketmaps.util.GHResponse;
//import com.starcom.pocketmaps.util.GeoMath;
//import com.starcom.pocketmaps.util.Instruction;
//import com.starcom.pocketmaps.util.InstructionAnnotation;
//import com.starcom.pocketmaps.util.InstructionList;
//import com.starcom.pocketmaps.util.PathWrapper;
//import com.starcom.pocketmaps.util.PointList;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.config.Profile;
import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.FetchMode;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.GHPoint3D;
//import com.starcom.pocketmaps.util.TargetDirComputer;
import com.graphhopper.util.Parameters.Algorithms;
import com.graphhopper.util.Parameters.Routing;
import com.graphhopper.util.StopWatch;
import com.graphhopper.util.details.PathDetail;
import com.graphhopper.routing.ev.MaxSpeed;

public class MapRoutingEngineImpl implements MapRoutingEngine
{
	static Logger logger = LoggerUtil.get(MapRoutingEngineImpl.class);
	GraphHopper hopper;
	
	public MapRoutingEngineImpl() {}

	@Override
	public ListIF<MapRoutingEngine.Edge> getAllEdges()
	{
		if (hopper == null) { return ListIF.createEmpty(); }
		AllEdgesIterator it = hopper.getGraphHopperStorage().getAllEdges();
		ListIF<MapRoutingEngine.Edge> compatList = new ListIF<MapRoutingEngine.Edge>()
		{
//			boolean nextExecuted = false;
//			boolean nextExecutedResult = false;
//			@Override public boolean hasNext()
//			{
//				if (nextExecuted) { return nextExecutedResult; }
//				nextExecuted = true;
//				nextExecutedResult = it.next();
//				return it.next();
//			}
//
//			@Override public T next() { return null; }
			

			@Override public int getSize() { return it.length(); }

			@Override
			public boolean next(Edge store)
			{
				while (it.next())
				{
					PointList geo = it.fetchWayGeometry(FetchMode.PILLAR_ONLY);
					if (geo.isEmpty()) { continue; }
					store.name = it.getName();
					store.loc.lat = (float)geo.getLat(0);
					store.loc.lon = (float)geo.getLon(0);
					store.loc.alt = (float)geo.getEle(0);
					store.loc.bearing = 0;
					store.loc.speed = 0;
					store.loc.time = 0;
					return true;
				}
				return false;
			}
			
		};
		
		
		return compatList;
	}

	//if (Cfg.getValue(NavKey.TravelMode, Cfg.TRAVEL_MODE_CAR).equals(Cfg.TRAVEL_MODE_BIKE)) { hours = hours * 3.0; }
	//if (Cfg.getValue(NavKey.TravelMode, Cfg.TRAVEL_MODE_CAR).equals(Cfg.TRAVEL_MODE_FOOT)) { hours = hours * 9.0; }
	@Override
	public NaviResponse createSimpleResponse(double fromLat, double fromLon, double toLat, double toLon, Enums.Vehicle vehicle)
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
	    	
	    };
	    return resp;
	}
	
	/** Creates a graphhopper instance.
	 * @param mapFolder The path/to/continent_country
	 * @param result Result with graphhopper instance or Exception. */
	@Override
	public void init(File mapFolder)
	{
		logger.info("loading graph ... ");
		Threading.getInstance().invokeAsyncTask(() ->
		{
			GraphHopper tmpHopp = new GraphHopper().forMobile();
// Why is "shortest" missing in default config? Add!
//TODO: How to add shortest in V1.0 of graphhopper? Or even necessary?
//tmpHopp.getCHPreparationHandler().addCHConfig(CHConfig.edgeBased("shortest", Weigh))
//tmpHopp.getCHFactoryDecorator().addCHProfileAsString("shortest");
			Profile car = new Profile(Vehicle.Car.toString().toLowerCase());
			car.setVehicle(car.getName());
			Profile bike = new Profile(Vehicle.Bike.toString().toLowerCase());
			bike.setVehicle(bike.getName());
			Profile foot = new Profile(Vehicle.Foot.toString().toLowerCase());
			foot.setVehicle(foot.getName());
			tmpHopp.setProfiles(car, bike, foot);
			tmpHopp.load(mapFolder.getAbsolutePath());
			logger.info("found graph " + tmpHopp.getGraphHopperStorage().toString() + ", nodes:" + tmpHopp.getGraphHopperStorage().getNodes());
			return tmpHopp;
		}, (o) -> setEngine(o));
	}
	
	private void setEngine(Object hopperObj)
	{
		if (hopperObj instanceof GraphHopper)
		{
			hopper = (GraphHopper) hopperObj;
		}
		else if (hopperObj instanceof Exception)
		{
			((Exception) hopperObj).printStackTrace();
		}
	}

	@Override
	public void close()
	{
		if (hopper != null) { hopper.close(); }
		hopper = null;
	}

	@Override
	public NaviResponse createResponse(double fromLat, double fromLon, double toLat, double toLon, Enums.Vehicle vehicle)
	{
		if (hopper == null) { return null; }

    	StopWatch sw = new StopWatch().start();
        GHRequest req = new GHRequest(fromLat, fromLon, toLat, toLon).
                setAlgorithm(Algorithms.DIJKSTRA_BI);
        req.getHints().putObject(Routing.INSTRUCTIONS, true); //TODO: Cfg.getBoolValue(Cfg.NavKeyB.DirectionsOn, true)
        req.setProfile(vehicle.toString().toLowerCase());
        req.getPathDetails().add(MaxSpeed.KEY);
//        req.setWeighting(Cfg.getValue(Cfg.NavKey.Weighting, "fastest")); //TODO: Howto set the weightning in graphhopper 1.0?
        req.getPathDetails().add(com.graphhopper.util.Parameters.Details.AVERAGE_SPEED);

        GHResponse resp = null;
//        MapLayer ml = MapList.getInstance().findMapLayerFromLocation(new GeoPoint(toLat, toLon));
//        if (ml != null)
//        {
//        	GraphHopper hopper = ml.getPathfinder();
        	if (hopper != null)
        	{
        		resp = hopper.route(req);
        	}
//        }
        if (resp==null || resp.hasErrors())
        {
//          NaviEngine.getNaviEngine().setDirectTargetDir(true);
          Throwable error;
          if (resp != null) { error = resp.getErrors().get(0); }
          else { error = new NullPointerException("Hopper is null!!!"); }
          logger.warning("Multible errors, first: " + error);
          resp = null;
        }
//        else
//        {
//          NaviEngine.getNaviEngine().setDirectTargetDir(false);
//        }
        float time = sw.stop().getSeconds();
        logger.info("Calculating took " + time + "s.");
        return convertResponse(resp);
	}
	
	private ArrayList<Point> convertPointList(PointList gpl)
	{
		ArrayList<Point> pl = new ArrayList<>();
		for (GHPoint3D p : gpl)
		{
			Point pp = new Point(p.lat, p.lon, p.ele);
			pl.add(pp);
		}
		return pl;
	}
	
	ArrayList<PathInfo> convertDetails(List<PathDetail> d)
	{
		ArrayList<PathInfo> iList = new ArrayList<PathInfo>();
		if (d==null) { return iList; }
		for (PathDetail i : d)
		{
			PathInfo inf = new PathInfo();
			inf.first = i.getFirst();
			inf.last = i.getLast();
			inf.value = i.getValue();
			iList.add(inf);
		}
		return iList;
	}
	
	private NaviResponse convertResponse(GHResponse res)
	{
		ArrayList<Point> pl = convertPointList(res.getBest().getPoints());
		long time = res.getBest().getTime();
		double dist = res.getBest().getDistance();
		ArrayList<Instruct> newInstructions = new ArrayList<>();
		for (Instruction inst : res.getBest().getInstructions())
		{
			Instruct i = new Instruct(convertPointList(inst.getPoints()), inst.getDistance(), Sign.Finish, inst.getName(), inst.getTime());
			i.annotation = inst.getAnnotation().getMessage();
			switch(inst.getSign())
			{
				case Instruction.FINISH: i.sign = Sign.Finish; break;
				case Instruction.LEAVE_ROUNDABOUT: i.sign = Sign.LeaveRoundabout; break;
				case Instruction.TURN_SHARP_LEFT: i.sign = Sign.TurnSharpLeft; break;
				case Instruction.TURN_LEFT: i.sign = Sign.TurnLeft; break;
				case Instruction.TURN_SLIGHT_LEFT: i.sign = Sign.TurnSlightLeft; break;
				case Instruction.CONTINUE_ON_STREET: i.sign = Sign.ContinueOnStreet; break;
				case Instruction.TURN_SLIGHT_RIGHT: i.sign = Sign.TurnSlightRight; break;
				case Instruction.TURN_RIGHT: i.sign = Sign.TurnRight; break;
				case Instruction.TURN_SHARP_RIGHT: i.sign = Sign.TurnSharpRight; break;
				case Instruction.REACHED_VIA: i.sign = Sign.ReachedVia; break;
				case Instruction.USE_ROUNDABOUT: i.sign = Sign.UseRoundabout; break;
				case Instruction.KEEP_RIGHT: i.sign = Sign.KeepRight; break;
				case Instruction.KEEP_LEFT: i.sign = Sign.KeepLeft; break;
			}
			newInstructions.add(i);
		}
		ArrayList<PathInfo> maxSpeedList = convertDetails(res.getBest().getPathDetails().get("max_speed"));
		ArrayList<PathInfo> aveSpeedList = convertDetails(res.getBest().getPathDetails().get("average_speed"));
		NaviResponse nv = new NaviResponse()
		{
			@Override
			public long getTime()
			{
				return time;
			}
			
			@Override
			public ArrayList<Point> getPoints()
			{
				return pl;
			}
			
			@Override
			public double getDistance()
			{
				return dist;
			}

			@Override
			public ArrayList<Instruct> getInstructions()
			{
				return newInstructions;
			}

			@Override
			public ArrayList<PathInfo> getMaxSpeedInfos()
			{
				return maxSpeedList;
			}

			@Override
			public ArrayList<PathInfo> getAveSpeedInfos()
			{
				return aveSpeedList;
			}
		};
		return nv;
	}

	@Override
	public void findClosestStreet(Point from, Point store)
	{
		if (store == null) { store = new Point(); }
	    QueryResult pos = hopper.getLocationIndex().findClosest(from.lat, from.lon, EdgeFilter.ALL_EDGES);
	    int n = pos.getClosestEdge().getBaseNode();
	    NodeAccess nodeAccess = hopper.getGraphHopperStorage().getNodeAccess();
	    store.lat = nodeAccess.getLat(n);
	    store.lon = nodeAccess.getLon(n);
	    store.ele = nodeAccess.getEle(n);
	}
	
}
