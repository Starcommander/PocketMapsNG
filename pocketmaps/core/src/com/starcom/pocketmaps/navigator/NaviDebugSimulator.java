package com.starcom.pocketmaps.navigator;

import java.util.ArrayList;

import org.oscim.core.GeoPoint;

import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
//import com.junjunguo.pocketmaps.activities.MapActivity;
import com.starcom.pocketmaps.map.Tracking;

//import android.app.Activity;

import com.starcom.LoggerUtil;
import com.starcom.gdx.system.Threading;
import com.starcom.navigation.Location;
//import android.os.Handler;
//import android.util.Log;

public class NaviDebugSimulator
{
  /** The DEBUG_SIMULATOR will simulate first generated route on naviStart and trackingStart. **/
  private static final boolean DEBUG_SIMULATOR = false;
  private static final int MAX_STEP_DISTANCE = 40;
  private static boolean debug_simulator_run = false;
  private static boolean debug_simulator_from_tracking = false;
  private static ArrayList<GeoPoint> debug_simulator_points;
  private static NaviDebugSimulator instance;
  GeoPoint checkP = new GeoPoint(0,0);
  
  public static NaviDebugSimulator getSimu()
  {
    if (instance == null) { instance = new NaviDebugSimulator(); }
    return instance;
  }
  
  public void setSimuRun(boolean run) { debug_simulator_run = run; }
  
  /** The DEBUG_SIMULATOR will simulate first generated route on naviStart and trackingStart. **/
  public void startDebugSimulator(InstructionList instructions, boolean fromTracking)
  {
    if (!DEBUG_SIMULATOR) { return; }
    debug_simulator_from_tracking = fromTracking;
    if (instructions == null) { return; }
    if (debug_simulator_points == null)
    {
      debug_simulator_points = new ArrayList<GeoPoint>();
      for (Instruction ins : instructions)
      {
        for (int i=0; i<ins.getPoints().size(); i++)
        {
          debug_simulator_points.add(new GeoPoint(ins.getPoints().getLat(i),ins.getPoints().getLon(i)));
        }
      }
    }
    final Location pLoc = new Location(0, 0, 0, 0);
    final Location lastLoc = new Location(0, 0, 0, 0);
    debug_simulator_run = true;
    runDelayed(pLoc, lastLoc, 0);
  }
  
  private void runDelayed(final Location pLoc, final Location lastLoc, final int index)
  {
	  Threading.getInstance().invokeLater(() ->
      {
        if (debug_simulator_from_tracking &&
            !Tracking.getInstance().isTracking()) { debug_simulator_run = false; }
        if (!debug_simulator_run) { return; }
        GeoPoint p = debug_simulator_points.get(index);
        int newIndex = checkDistance(index, p);
        p = checkP;
        float bearing = lastLoc.bearingTo(pLoc);
        pLoc.set((float)p.getLatitude(), (float)p.getLongitude(), 5.555f, bearing, 0, 0L);
        lastLoc.set(pLoc);
//        cur.onLocationChanged(pLoc); TODO: Implement MapActivity.onLocationChanged(L)
        log("Update position for Debug purpose! Lat=" + pLoc.getLatitude() + " Lon=" + pLoc.getLongitude());
        if (debug_simulator_points.size() > newIndex)
        {
          runDelayed(pLoc, lastLoc, newIndex);
        }
      }, 2000);
  }

  private int checkDistance(int index, GeoPoint p)
  {
    if (index <= 0)
    {
      checkP = p;
      return index + 1;
    }
    double dist = p.distance(checkP);
    if (dist > GeoPoint.latitudeDistance(MAX_STEP_DISTANCE))
    {
      float bearing = (float)checkP.bearingTo(p);
      checkP = checkP.destinationPoint(MAX_STEP_DISTANCE * 0.5, bearing);
      return index;
    }
    else
    {
      checkP = p;
      return index + 1;
    }
  }
  
  private void log(String str)
  {
	  LoggerUtil.get(NaviDebugSimulator.class).info(str);
  }
}
