package com.starcom.pocketmaps.navigator;

import java.util.ArrayList;

import org.oscim.core.GeoPoint;

import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.starcom.LoggerUtil;
import com.starcom.gdx.ui.ToastMsg;
import com.starcom.system.Threading;
import com.starcom.navigation.Location;
import com.starcom.pocketmaps.tracking.Tracking;

public class NaviDebugSimulator
{
  /** The DEBUG_SIMULATOR will simulate first generated route. **/
  private static final int MAX_STEP_DISTANCE = 40;
  private static volatile boolean debug_simulator_run = false;
  private static volatile boolean debug_simulator_running = false;
  private static ArrayList<GeoPoint> debug_simulator_points = new ArrayList<GeoPoint>();
  private static NaviDebugSimulator instance = new NaviDebugSimulator();
  GeoPoint checkP = new GeoPoint(0,0);
  
  private NaviDebugSimulator() {}
  
  public static NaviDebugSimulator getSimu()
  {
    return instance;
  }
  
  public void stopDebugSimulator() { debug_simulator_run = false; }
  
  public void updateRoute(InstructionList instructions)
  {
    if (debug_simulator_running) { return; }
    if (instructions == null || (instructions.size() == 0))
    {
    	return;
    }
    debug_simulator_points.clear();
      for (Instruction ins : instructions)
      {
        for (int i=0; i<ins.getPoints().size(); i++)
        {
          debug_simulator_points.add(new GeoPoint(ins.getPoints().getLat(i),ins.getPoints().getLon(i)));
        }
      }
  }
  
  /** The DEBUG_SIMULATOR will simulate the last generated route on naviStart and trackingStart. **/
  public void startDebugSimulator()
  {
	if (debug_simulator_running)
	{
		logUser("Simulator is already running");
		debug_simulator_run = false;
		return;
	}
    final Location pLoc = new Location(0, 0, 0, 0);
    final Location lastLoc = new Location(0, 0, 0, 0);
    debug_simulator_run = true;
    runDelayed(pLoc, lastLoc, 0);
  }
  
  public boolean isRunning() { return debug_simulator_running; }
  
  private void runDelayed(final Location pLoc, final Location lastLoc, final int index)
  {
	  if (debug_simulator_points.isEmpty())
	  {
		  logUser("There is no route to simulate");
		    debug_simulator_run = false;
		  return;
	  }
	  debug_simulator_running = true;
	  Threading.getInstance().invokeLater(() ->
      {
        if (!debug_simulator_run) { debug_simulator_running = false; return; } //Stopped
        GeoPoint p = debug_simulator_points.get(index);
        int newIndex = checkDistance(index, p);
        p = checkP;
        lastLoc.set(pLoc);
        pLoc.set((float)p.getLatitude(), (float)p.getLongitude(), 5.555f, 0, 0, 0L);
        float bearing = lastLoc.bearingTo(pLoc);
        pLoc.setBearing(bearing);
        NaviEngine.getNaviEngine().onLocationChanged(pLoc);
        log("Update position for Debug purpose! Lat=" + pLoc.getLatitude() + " Lon=" + pLoc.getLongitude());
        if (debug_simulator_points.size() > newIndex)
        {
          runDelayed(pLoc, lastLoc, newIndex);
        }
        else
       	{
        	debug_simulator_running = false;
        	logUser("Simulator finished");
        }
      }, 2000);
  }

  /** Checks the distance to next waypoint, and sets checkP.
   * @param index The current index to check.
   * @param p The next waypoint.
   * @return The new index for next call. */
  private int checkDistance(int index, GeoPoint p)
  {
    if (index < 0)
    {
    	throw new IllegalStateException("Index must never be less than 0.");
    }
    else if (index == 0)
    { // First time
      checkP = p;
      return 1;
    }
    double dist = p.distance(checkP);
    if (dist > GeoPoint.latitudeDistance(MAX_STEP_DISTANCE))
    { // Split next waypoint
      float bearing = (float)checkP.bearingTo(p);
      checkP = checkP.destinationPoint(MAX_STEP_DISTANCE * 0.5, bearing);
      return index;
    }
    else
    { // Just use existing new waypoint
      checkP = p;
      return index + 1;
    }
  }
  
  private void log(String str)
  {
	  LoggerUtil.get(NaviDebugSimulator.class).info(str);
  }
  
  private void logUser(String str)
  {
	  log(str);
	  ToastMsg.getInstance().toastShort(str);
  }
}
