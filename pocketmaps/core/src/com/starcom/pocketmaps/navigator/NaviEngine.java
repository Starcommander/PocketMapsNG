package com.starcom.pocketmaps.navigator;

import org.oscim.core.GeoPoint;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EdgeFilter;
import com.graphhopper.storage.NodeAccess;
import com.graphhopper.storage.index.QueryResult;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.starcom.LoggerUtil;
import com.starcom.system.Threading;
import com.starcom.navigation.Location;
import com.starcom.navigation.gps.IClient;
import com.starcom.navigation.gps.StaticClientImpl;
import com.starcom.pocketmaps.Cfg;
import com.starcom.pocketmaps.Cfg.NavKey;
import com.starcom.pocketmaps.Cfg.NavKeyB;
import com.starcom.pocketmaps.Icons.R;
import com.starcom.pocketmaps.Icons;
import com.starcom.pocketmaps.map.MapHandler;
import com.starcom.pocketmaps.map.MapLayer;
import com.starcom.pocketmaps.navigator.Navigator;
import com.starcom.pocketmaps.text.Text;
import com.starcom.pocketmaps.tracking.Tracking;
import com.starcom.pocketmaps.util.GeoMath;
//import com.starcom.pocketmaps.util.LightSensor;
import com.starcom.pocketmaps.util.UnitCalculator;
import com.starcom.pocketmaps.views.MapList;
import com.starcom.pocketmaps.views.NavTopPanel;
import com.starcom.pocketmaps.views.SettingsView;
import com.starcom.pocketmaps.views.TopPanel;

import org.oscim.utils.async.AsyncExecutor;
//import android.app.Activity;
//import android.content.Context;
//import androidx.annotation.WorkerThread;
//import androidx.annotation.UiThread;
//import android.location.Location;
//import android.os.AsyncTask.Status;
//import android.speech.tts.Voice;
//import android.util.Log;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.TextView;
import org.oscim.utils.async.AsyncTask;
import com.graphhopper.util.details.PathDetail;
import com.starcom.pocketmaps.util.SpeedUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NaviEngine
{
  private static final double MAX_WAY_TOLERANCE_METER = 30.0;
  private static final double MAX_WAY_TOLERANCE = GeoMath.DEGREE_PER_METER * MAX_WAY_TOLERANCE_METER;

  public static final int BEST_NAVI_ZOOM = 18;
  enum UiJob { Nothing, RecalcPath, UpdateInstruction, Finished };
  private UiJob uiJob = UiJob.Nothing;
  private boolean directTargetDir = false;
  NaviVoice naviVoice;
  //LightSensor lightSensor;
  SpeedUtil speedUtil = new SpeedUtil();
  boolean naviVoiceSpoken = false;
  private GeoPoint recalcFrom, recalcTo;
  private static NaviEngine instance;
  private boolean mapUpdatesAllowed = true;
  private Location pos, mCurrentLocation;
  final PointPosData nearestP = new PointPosData(); 
  private boolean active = false;
  private InstructionList instructions;
  private IClient gpsClient;
//  private ImageView navtop_image;
//  private TextView navtop_curloc;
//  private TextView navtop_nextloc;
//  private TextView navtop_when;
//  private TextView navtop_time;
//  private TextView navspeed_text;
  private float tiltMult = 1.0f;
  private float tiltMultPos = 1.0f;
//  AsyncTask<GeoPoint, NaviInstruction, NaviInstruction> naviEngineTask;
//  AsyncTask naviEngineTask;
  private Runnable naviEngineTask;
  private double partDistanceScaler = 1.0;

  private NaviEngine()
  {
	  if (Cfg.getBoolValue(NavKeyB.GpsOn, true))
	  {
gpsClient = IClient.createGpsClient();
gpsClient.start();
gpsClient.addTpvHandler((t) ->
	onLocationChanged(
			new Location(
					t.getLatitude().floatValue(),
					t.getLongitude().floatValue(),
					t.getSpeed().floatValue(),
					t.getCourse().floatValue())));
gpsClient.watch(true, true);
	  }
	  else
	  {
		  gpsClient = new StaticClientImpl();
	  }
  }
  
  public static NaviEngine getNaviEngine()
  {
    if (instance == null) { instance = new NaviEngine(); }
    return instance;
  }
  
  public static void dispose()
  {
	  if (instance == null) { return; }
	  instance.getGpsClient().stop();
  }
  
  public boolean isNavigating()
  {
    return active;
  }
  
  public String getStatistics()
  {
	return "Status: Navigating" +
			"\nWaypoints:" + (instructions==null ? "0":instructions.size()) +
			"\nNearestIdx:" + nearestP.arrIdx +
			"\n       Dist:" + nearestP.distance +
			"\n       Status:" + nearestP.status +
			"\n       DirOK:" + nearestP.isDirectionOk() +
			"\n       DistOK:" + nearestP.isDistanceOk() +
			"\nUiJob:" + uiJob;
  }
  
  public IClient getGpsClient() { return gpsClient; }
  
  /** Ensures, that voice is initialized. Use forceReset for switching engine **/
  public void naviVoiceInit(Object appContext, boolean forceReset)
  {
    if (naviVoice == null)
    {
      naviVoice = new NaviVoice(appContext);
    }
    else if (forceReset)
    {
      naviVoice.shutdownTts();
      naviVoice = new NaviVoice(appContext);
    }
  }
  
  public ArrayList<String> naviVoiceList(Object appContext)
  {
      naviVoiceInit(appContext, false);
      return naviVoice.getVoiceListCompat();
  }
  
  public ArrayList<String> naviVoiceEngineList(Object appContext)
  {
      naviVoiceInit(appContext, false);
      return naviVoice.getEngineList();
  }
  
  public void naviVoiceSpeak(Object appContext, String fallbackTxt, String txt, boolean forceResetVoice)
  {
      naviVoiceInit(appContext, false);
      if (forceResetVoice) { naviVoice.curVoice = null; }
      naviVoice.speak(fallbackTxt, txt);
  }
  
  public boolean naviVoiceIsTtsReady(Object appContext)
  {
      naviVoiceInit(appContext, false);
      return naviVoice.isTtsReady();
  }
  
  public String getNaviVoiceError()
  {
      return naviVoice.getError();
  }
  
  public void setNavigating(Object activity, boolean active)
  {
    this.active = active;
//    if (active && lightSensor==null)
//    {
//      lightSensor = new LightSensor(activity);
//    }
//    else if ((!active) && lightSensor!=null)
//    {
//      lightSensor.cleanup(activity);
//      lightSensor = null;
//    }
    speedUtil.setEnabled(active && Cfg.getBoolValue(NavKeyB.ShowingSpeedLimits, false));
    naviVoiceInit(activity, false);
    if (!active)
    {
      NavTopPanel.getInstance().setVisible(false);
      MapHandler.getInstance().setCustomPointIcon(Icons.generateIconVtm(Icons.R.ic_my_location_dark_24dp));
      if (pos != null)
      {
        GeoPoint curPos = new GeoPoint(pos.getLatitude(), pos.getLongitude());
        MapHandler.getInstance().centerPointOnMap(curPos, BEST_NAVI_ZOOM, 0, 0);
      }
      NaviDebugSimulator.getSimu().stopDebugSimulator();
      return;
    }
    NavTopPanel.getInstance().setVisible(true);
    mapUpdatesAllowed = true;
    MapHandler.getInstance().setCustomPointIcon(Icons.generateIconVtm(Icons.R.ic_navigation_black_24dp));
    naviVoiceSpoken = false;
    uiJob = UiJob.Nothing;
//    initFields(activity);
    instructions = Navigator.getNavigator().getGhResponse().getInstructions();
    speedUtil.updateList(Navigator.getNavigator().getGhResponse().getPathDetails());
    resetNewInstruction();
    if (instructions.size() > 0)
    {
      NaviDebugSimulator.getSimu().updateRoute(instructions);
    }
  }
  
  /** This allows to update pathLayer, when instructions is just a line from target to curLoc.
   *  @param directTargetDir True to update pathLayer, join to curPos. **/
  public void setDirectTargetDir(boolean directTargetDir)
  {
    this.directTargetDir = directTargetDir;
  }
  
  /** This is only called when path is calculated first time, or recalcPath. **/
  public void onUpdateInstructions(InstructionList instructions, Map<String, List<PathDetail>> pathDetails)
  {
    if (uiJob != UiJob.RecalcPath) { throw new IllegalStateException("Getting instructions but state is not RecalcPath!"); }
    if (!directTargetDir)
    {
      nearestP.checkDirectionOk(pos, instructions.get(0), naviVoice);
    }
    this.instructions = instructions;
    this.speedUtil.updateList(pathDetails);
    getNewInstruction();
    uiJob = UiJob.UpdateInstruction;
  }

  private GeoPoint findClosestStreet(GeoPoint fromPos)
  {
    MapLayer ml = MapList.getInstance().findMapLayerFromLocation(fromPos);
    if (ml == null) { return fromPos; } // No matching map loaded yet!
    GraphHopper hopper = ml.getPathfinder();
    if (hopper == null) { return fromPos; } // Not loaded yet!
    QueryResult pos = hopper.getLocationIndex().findClosest(fromPos.getLatitude(), fromPos.getLongitude(), EdgeFilter.ALL_EDGES);
    int n = pos.getClosestEdge().getBaseNode();
    NodeAccess nodeAccess = hopper.getGraphHopperStorage().getNodeAccess();
    GeoPoint gp = new GeoPoint(nodeAccess.getLat(n), nodeAccess.getLon(n));
    return gp;
  }
  
//  private void initFields(Activity activity)
//  {
//    navtop_image = activity.findViewById(R.id.navtop_image);
//    navtop_curloc = activity.findViewById(R.id.navtop_curloc);
//    navtop_nextloc = activity.findViewById(R.id.navtop_nextloc);
//    navtop_when = activity.findViewById(R.id.navtop_when);
//    navtop_time = activity.findViewById(R.id.navtop_time);
//    navspeed_text = activity.findViewById(R.id.speed_sign_text);
//    speedUtil.initTextView(navspeed_text);
//  }
  
  /** Called when map dnd-moved while navigating, or re-center again. */
  public void setMapUpdatesAllowed(boolean allowed)
  {
      if (this.mapUpdatesAllowed != allowed)
      {
        this.mapUpdatesAllowed = allowed;
        if (allowed)
        {
          MapHandler.getInstance().centerPointOnMap(new GeoPoint(pos.getLatitude(), pos.getLongitude()), BEST_NAVI_ZOOM, 0, 0);
          MapHandler.getInstance().setCustomPointIcon(Icons.generateIconVtm(Icons.R.ic_navigation_black_24dp));
        }
        else
        {
          NavTopPanel.getInstance().showNaviCenterButton();
          MapHandler.getInstance().resetTilt(0);
          MapHandler.getInstance().setCustomPointIcon(Icons.generateIconVtm(Icons.R.ic_my_location_dark_24dp));
          NavTopPanel.getInstance().showNaviCenterButton();
        }
      }
  }
  
  /** Executed from native GPS or NaviDebugSimulator */
  public void onLocationChanged(Location location) {
	  if (location == null)
	  {
		  log("Got null as location");
		  return;
	  }
	  if (location.getLatitude() == Float.NaN || location.getLongitude() == Float.NaN)
	  {
		  log("Got location with NaN");
		  return;
	  }
	  Tracking.getInstance().onLocationChanged(location);
      mCurrentLocation = location;
      if (mCurrentLocation != null) {
          GeoPoint mcLatLong = new GeoPoint(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
//          if (Tracking.getTracking(getApplicationContext()).isTracking()) { //TODO: Implement this
//              MapHandler.getMapHandler().addTrackPoint(this, mcLatLong);
//              Tracking.getTracking(getApplicationContext()).addPoint(mCurrentLocation, mapActions.getAppSettings());
//          }
          if (isNavigating())
          {
            updatePosition(mCurrentLocation);
          }
    SettingsView.getInstance().updateStatistics();
          //TODO: Tracking update
          MapHandler.getInstance().setCustomPoint(mcLatLong);
//          mapActions.showPositionBtn.setImageResource(R.drawable.ic_my_location_white_24dp); //TODO: Implement this
      } else {
//          mapActions.showPositionBtn.setImageResource(R.drawable.ic_location_searching_white_24dp); //TODO: Implement this
      }
  }
  
  /** Returns the current location, may be null. */
  public Location getCurrentLocation() { return mCurrentLocation; }
  
  private void updatePosition(Location pos)
  {
    if (active == false) { return; }
    if (uiJob == UiJob.RecalcPath) { return; }
    if (this.pos == null) { this.pos = new Location(0, 0, 0, 0); }
    this.pos.set(pos);
    GeoPoint curPos = new GeoPoint(pos.getLatitude(), pos.getLongitude());
    GeoPoint newCenter = curPos.destinationPoint(70.0 * tiltMultPos, pos.getBearing());
    if (mapUpdatesAllowed)
    {
        MapHandler.getInstance().centerPointOnMap(newCenter, BEST_NAVI_ZOOM, 360.0f - pos.getBearing(), 45.0f * tiltMult);
    }
    calculatePositionAsync(curPos);
  }

  private void calculatePositionAsync(GeoPoint curPos)
  {
    if (naviEngineTask == null)
    {
      naviEngineTask = createNaviEngineTask(curPos);
      log("Error, NaviEngineTask is still running! Drop job ...");
    }
    else
    {
      updateDirectTargetDir(curPos);
      if (naviEngineTask instanceof Thread && ((Thread)naviEngineTask).isAlive() )
      {
        log("Error, NaviEngineTask is still running! Drop job .");
      }
      else
      {
    	naviEngineTask = createNaviEngineTask(curPos);
      }
    }
  }
  
  private void updateDirectTargetDir(GeoPoint curPos)
  {
    if (!directTargetDir) { return; }
    MapHandler.getInstance().joinPathLayerToPos(curPos.getLatitude(), curPos.getLongitude());
  }
  
  private Object naviEngineTaskExecuteBG(GeoPoint geo)
  {
	  return calculatePosition(geo);
  }

  private Object naviEngineTaskExecuteUI(Object inObj)
  {
      if (inObj == null)
      {
        if (NaviEngine.this.uiJob == UiJob.RecalcPath)
        {
          if (instructions != null)
          {
            instructions = null;
            MapHandler.getInstance().calcPath(TopPanel.getInstance().getGdxMap(), recalcFrom.getLatitude(), recalcFrom.getLongitude(), recalcTo.getLatitude(), recalcTo.getLongitude());
            log("Recalculating of path!!!");
          }
        }
        else if (NaviEngine.this.uiJob == UiJob.Finished)
        {
          active = false;
        }
      }
      else if (inObj instanceof Exception)
      {
    	  ((Exception)inObj).printStackTrace();
      }
      else if (NaviEngine.this.uiJob == UiJob.UpdateInstruction)
      {
        showInstruction((NaviInstruction)inObj);
      }
      return null;
  }
  
  private Runnable createNaviEngineTask(GeoPoint geo)
  {
    return Threading.getInstance().invokeAsyncTask(() -> naviEngineTaskExecuteBG(geo), (e) -> naviEngineTaskExecuteUI(e));
  }

  private void showInstruction(NaviInstruction in)
  {
	Text t = Text.getInstance();
    if (in==null)
    {
    	NavTopPanel.getInstance().updateInstruction(t.getSearchLocation(), "0 " + UnitCalculator.getUnit(false), "---------", Icons.R.ic_2x_continue_on_street, "0 min");
//      navtop_when.setText("0 " + UnitCalculator.getUnit(false));
//      navtop_time.setText("--------");
//      navtop_curloc.setText(R.string.search_location);
//      navtop_nextloc.setText("==================");
//      navtop_image.setImageResource(R.drawable.ic_2x_continue_on_street);
      setTiltMult(1);
    }
    else if(nearestP.isDirectionOk())
    {
    	NavTopPanel.getInstance().updateInstruction(in.getCurStreet(), in.getNextDistanceString(), in.getNextInstruction(), Icons.R.valueOf(in.getNextSignResource()), in.getFullTimeString());
//      navtop_when.setText(in.getNextDistanceString());
//      navtop_time.setText(in.getFullTimeString());
//      navtop_curloc.setText(in.getCurStreet());
//      navtop_nextloc.setText(in.getNextInstruction());
//      navtop_image.setImageResource(in.getNextSignResource());
      speedUtil.showTextSign(nearestP.arrIdx);
      setTiltMult(in.getNextDistance());
    }
    else
    {
    	NavTopPanel.getInstance().updateInstruction(t.getWrongDirection(), "0 " + UnitCalculator.getUnit(false), "--------", Icons.R.ic_2x_roundabout, in.getFullTimeString());
//      navtop_when.setText("0 " + UnitCalculator.getUnit(false));
//      navtop_time.setText(in.getFullTimeString());
//      navtop_curloc.setText(R.string.wrong_direction);
//      navtop_nextloc.setText("==================");
//      navtop_image.setImageResource(R.drawable.ic_2x_roundabout);
      setTiltMult(1);
    }
  }
  
  /** When next instruction has a distance of 400m then rotate tilt to see 400m far.
   *  <br>Alternatively raise tilt on fast speed. **/
  private void setTiltMult(double nextDist)
  {
    double speedXtra = 0;
    if (pos!=null) { speedXtra = pos.getSpeed(); }
    if (speedXtra > 30.0) { speedXtra = 2; }
    else if (speedXtra < 8.0) { speedXtra = 0; }
    else
    {
      speedXtra = speedXtra - 8.0; // 0 - 22
      speedXtra = speedXtra / 22.0; // 0 - 1
      speedXtra = speedXtra * 2.0; // 0 - 2
    }
    if (nextDist > 400) { nextDist = 0; }
    else if (nextDist < 100) { nextDist = 0; }
    else
    {
      nextDist = nextDist - 100.0; // 0 - 300
      nextDist = nextDist / 300.0; // 0 - 1
      nextDist = nextDist * 2.0; // 0 - 2
    }
    if (speedXtra > nextDist) { nextDist = speedXtra; }
    tiltMultPos = (float)(1.0 + (nextDist * 0.5));
    tiltMult = (float)(1.0 + nextDist);
  }
  
  /** Running in BG. */
  private NaviInstruction calculatePosition(GeoPoint curPos)
  {
    if (uiJob == UiJob.RecalcPath) { return null; }
    if (uiJob == UiJob.Finished) { return null; }
    nearestP.setBaseData(getNearestPoint(instructions.get(0), nearestP.arrIdx, curPos));
    
    if (nearestP.arrIdx > 0)
    { // Check dist to line (backward)
      double lat1 = instructions.get(0).getPoints().getLatitude(nearestP.arrIdx);
      double lon1 = instructions.get(0).getPoints().getLongitude(nearestP.arrIdx);
      double lat2 = instructions.get(0).getPoints().getLatitude(nearestP.arrIdx-1);
      double lon2 = instructions.get(0).getPoints().getLongitude(nearestP.arrIdx-1);
      double lDist = GeoMath.distToLineSegment(curPos.getLatitude(), curPos.getLongitude(), lat1, lon1, lat2, lon2);
      if (lDist < nearestP.distance)
      {
        nearestP.distance = lDist;
        nearestP.status = PointPosData.Status.CurPosIsBackward;
      }
    }
    if (nearestP.arrIdx < instructions.get(0).getPoints().size()-1)
    { // Check dist to line (forward)
      double lat1 = instructions.get(0).getPoints().getLatitude(nearestP.arrIdx);
      double lon1 = instructions.get(0).getPoints().getLongitude(nearestP.arrIdx);
      double lat2 = instructions.get(0).getPoints().getLatitude(nearestP.arrIdx+1);
      double lon2 = instructions.get(0).getPoints().getLongitude(nearestP.arrIdx+1);
      double lDist = GeoMath.distToLineSegment(curPos.getLatitude(), curPos.getLongitude(), lat1, lon1, lat2, lon2);
      if (lDist < nearestP.distance)
      {
        nearestP.distance = lDist;
        nearestP.status = PointPosData.Status.CurPosIsForward;
      }
    }
    else if (nearestP.arrIdx == instructions.get(0).getPoints().size()-1 &&
             instructions.size()>1)
    {
      if (instructions.get(1).getPoints().size() > 0)
      { // Check dist to line (forward to next instruction)
        double lat1 = instructions.get(0).getPoints().getLatitude(nearestP.arrIdx);
        double lon1 = instructions.get(0).getPoints().getLongitude(nearestP.arrIdx);
        double lat2 = instructions.get(1).getPoints().getLatitude(0);
        double lon2 = instructions.get(1).getPoints().getLongitude(0);
        double lDist = GeoMath.distToLineSegment(curPos.getLatitude(), curPos.getLongitude(), lat1, lon1, lat2, lon2);
        if (lDist < nearestP.distance)
        {
          nearestP.distance = lDist;
          nearestP.status = PointPosData.Status.CurPosIsForward;
        }
      }
      if (instructions.get(1).getPoints().size() > 1)
      { // Check dist to line (forward next instruction p1+p2)
        double lat1 = instructions.get(1).getPoints().getLatitude(0);
        double lon1 = instructions.get(1).getPoints().getLongitude(0);
        double lat2 = instructions.get(1).getPoints().getLatitude(1);
        double lon2 = instructions.get(1).getPoints().getLongitude(1);
        double lDist = GeoMath.distToLineSegment(curPos.getLatitude(), curPos.getLongitude(), lat1, lon1, lat2, lon2);
        if (lDist < nearestP.distance)
        {
          nearestP.distance = lDist;
          nearestP.status = PointPosData.Status.CurPosIsForwardNext;
        }
      }
    }
    if (nearestP.isForward())
    {
      // Reset bearing with calculatePosition()
      nearestP.resetDirectionOk();
    }
    if (!nearestP.isDistanceOk())
    {
      double maxWayTolMeters = MAX_WAY_TOLERANCE_METER;
      if (directTargetDir) { maxWayTolMeters = maxWayTolMeters * 10.0; }
      Instruction nearestNext = instructions.find(curPos.getLatitude(), curPos.getLongitude(), maxWayTolMeters);
      if (nearestNext == null)
      {
        GeoPoint closestP = findClosestStreet(curPos);
        nearestNext = instructions.find(closestP.getLatitude(), closestP.getLongitude(), maxWayTolMeters);
      }
      if (nearestNext == null)
      {
        uiJob = UiJob.RecalcPath;
        recalcFrom = curPos;
        Instruction lastInstruction = instructions.get(instructions.size()-1);
        int lastPoint = lastInstruction.getPoints().size()-1;
        double lastPointLat = lastInstruction.getPoints().getLat(lastPoint);
        double lastPointLon = lastInstruction.getPoints().getLon(lastPoint);
        recalcTo = new GeoPoint(lastPointLat, lastPointLon);
log("NaviTask Start recalc !!!!!!");
        return null;
      }
      else
      { // Forward to nearest instruction.
        int deleteCounter = 0;
        Instruction lastDeleted = null;
        
        while (instructions.size()>0 && !instructions.get(0).equals(nearestNext))
        {
          deleteCounter++;
          lastDeleted = instructions.remove(0);
        }
        if (lastDeleted != null)
        { // Because we need the current, and not the next Instruction
          instructions.add(0, lastDeleted);
          deleteCounter --;
        }
        if (deleteCounter == 0)
        {
          PointPosData newNearestP = getNearestPoint(instructions.get(0), 0, curPos);
          //TODO: Continue-Instruction with DirectionInfo: getContinueInstruction() ?
log("NaviTask Start update far !!!!!!");
          return getUpdatedInstruction(curPos, newNearestP);
        }
log("NaviTask Start update skip-mult-" + deleteCounter + " !!!!!!");
        return getNewInstruction();
      }
    }
    else if (nearestP.isForwardNext())
    {
      speedUtil.updateInstructionDone(instructions.get(0).getPoints().size());
      instructions.remove(0);
log("NaviTask Start skip-next !!!!!!");
      return getNewInstruction();
    }
    else
    {
      // NaviTask Start update!
      return getUpdatedInstruction(curPos, nearestP);
    }
  }

  /** Returns the nearest point from this instruction. **/
  private static PointPosData getNearestPoint(Instruction instruction, int curPointIdx, GeoPoint curPos)
  {
    int nextPointIdx = curPointIdx;
    int nearestPointIdx = curPointIdx;
    double nearestDist = Double.MAX_VALUE;
    while (instruction.getPoints().size() > nextPointIdx)
    {
      double lat = instruction.getPoints().getLatitude(nextPointIdx);
      double lon = instruction.getPoints().getLongitude(nextPointIdx);
      double dist = GeoMath.fastDistance(curPos.getLatitude(), curPos.getLongitude(), lat, lon);
      if (dist < nearestDist)
      {
        nearestDist = dist;
        nearestPointIdx = nextPointIdx;
      }
      nextPointIdx++;
    }
    PointPosData p = new PointPosData();
    p.arrIdx = nearestPointIdx;
    p.distance = nearestDist;
    return p;
  }
  
  /** New instruction was reached.
   * <br> - Maybe because curPos reached nextInstruction.
   * <br> - Maybe because recalc finished.
   * <br>This does:
   * <br> - init the partDistanceScaler (compair calculated point-distances with instruction.distance)
   * <br> - reset the pointsArrayPos to 0
   * <br> - check for speaking necessary
   * @return A NaviInstruction with pointIndex 0-1. **/
  private NaviInstruction getNewInstruction()
  {
    nearestP.arrIdx = 0;
    nearestP.distance = Double.MAX_VALUE;
    uiJob = UiJob.UpdateInstruction;
    if (instructions.size() == 0)
    {
      uiJob = UiJob.Finished;
      return null;
    }
    Instruction in = instructions.get(0);
    long fullTime = countFullTime(in.getTime());
    GeoPoint curPos = new GeoPoint(in.getPoints().getLat(0), in.getPoints().getLon(0));
    double partDistance = countPartDistance(curPos, in, 0);
    if (partDistance == 0) { partDistanceScaler = 1; }
    else
    {
      partDistanceScaler = in.getDistance() / partDistance;
    }
    Instruction nextIn = null;
    if (instructions.size() > 1) { nextIn = instructions.get(1); }
    NaviInstruction nIn = new NaviInstruction(in, nextIn, fullTime);
    if (speakDistanceCheck(in.getDistance()) && nearestP.isDirectionOk())
    {
      naviVoice.speak(nIn.getVoiceTextFallback(), nIn.getVoiceText());
      naviVoiceSpoken = true;
    }
    else
    {
      naviVoiceSpoken = false;
    }
    return nIn;
  }
  
  /** No new instruction was reached.
   * <br>This does:
   * <br> - calculate the partDistance using the partDistanceScaler
   * <br> - check for speaking necessary
   * @return A NaviInstruction with pointIndex 0-1. **/
  private NaviInstruction getUpdatedInstruction(GeoPoint curPos, PointPosData nearestP)
  {
    uiJob = UiJob.UpdateInstruction;
    if (instructions.size() == 0)
    {
      uiJob = UiJob.Finished;
      return null;
    }
    Instruction in = instructions.get(0);
    long partTime = 0;
    double partDistance = countPartDistance(curPos, in, nearestP.arrIdx);
    partDistance = partDistance * partDistanceScaler;
    if (in.getDistance() <= partDistance)
    {
      partDistance = in.getDistance();
      partTime = in.getTime();
    }
    else
    {
      double partValue = partDistance / in.getDistance();
      partTime = (long)(in.getTime() * partValue);
    }
    long fullTime = countFullTime(partTime);
    Instruction nextIn = null;
    if (instructions.size() > 1) { nextIn = instructions.get(1); }
    NaviInstruction newIn = new NaviInstruction(in, nextIn, fullTime);
    newIn.updateDist(partDistance);
    if (!naviVoiceSpoken && nearestP.isDirectionOk() && speakDistanceCheck(partDistance))
    {
      naviVoice.speak(newIn.getVoiceTextFallback(), newIn.getVoiceText());
      naviVoiceSpoken = true;
    }
    return newIn;
  }
  
  public void setNaviVoiceMute(boolean mute)
  {
    if (naviVoice!= null)
    {
      naviVoice.setTtsMute(mute);
    }
  }
  
  private void resetNewInstruction()
  {
    nearestP.arrIdx = 0;
    nearestP.distance = Double.MAX_VALUE;
    uiJob = UiJob.UpdateInstruction;
    showInstruction(null);
  }
  
  private boolean speakDistanceCheck(double dist)
  {
    if (dist < 200) { return true; }
    if (pos.getSpeed() > 150 * GeoMath.KMH_TO_MSEC)
    {
      if (dist < 1500) { return true; }
    }
    else if (pos.getSpeed() > 100 * GeoMath.KMH_TO_MSEC)
    {
      if (dist < 900) { return true; }
    }
    else if (pos.getSpeed() > 70 * GeoMath.KMH_TO_MSEC)
    {
      if (dist < 500) { return true; }
    }
    else if (pos.getSpeed() > 30 * GeoMath.KMH_TO_MSEC)
    {
      if (dist < 350) { return true; }
    }
    return false;
  }

  /** Count all time of Instructions.
   * @param partTime Time of current Instruction. **/
  private long countFullTime(long partTime)
  {
    long fullTime = partTime;
    for (int i=1; i<instructions.size(); i++)
    {
      fullTime += instructions.get(i).getTime();
    }
    return fullTime;
  }
  
  /** Counts the estimated rest-distance to next instruction. **/
  private double countPartDistance(GeoPoint curPos, Instruction in, int nearestPointPos)
  {
    double partDistance = 0;
    double lastLat = curPos.getLatitude();
    double lastLon = curPos.getLongitude();
    for (int i=nearestPointPos+1; i<in.getPoints().size(); i++)
    {
      double nextLat = in.getPoints().getLat(i);
      double nextLon = in.getPoints().getLon(i);
      partDistance += GeoMath.fastDistance(lastLat, lastLon, nextLat, nextLon);
      lastLat = nextLat;
      lastLon = nextLon;
    }
    partDistance = partDistance * GeoMath.METER_PER_DEGREE;
    return partDistance;
  }

  private static void log(String str)
  {
	  LoggerUtil.get(NaviEngine.class).info(str);
  }
  
  static class PointPosData
  {
    public enum Status { CurPosIsExactly, CurPosIsBackward, CurPosIsForward, CurPosIsForwardNext };
    public int arrIdx;
    public double distance;
    public Status status = Status.CurPosIsExactly;
    private boolean wrongDir = false;
    private boolean wrongDirHint = false;
    public boolean isDistanceOk()
    {
      return (distance < MAX_WAY_TOLERANCE);
    }
    
    /** Returns true, if curPos has not reached the nearestPoint. **/
    public boolean isBackward() { return (status == Status.CurPosIsBackward); }
    /** Returns true, if curPos has already reached the nearestPoint. **/
    public boolean isForward() { return (status == Status.CurPosIsForward); }
    /** Returns true, if curPos has already reached the nearestPoint, and next point leads to next-instruction. **/
    public boolean isForwardNext() { return (status == Status.CurPosIsForwardNext); }
    public boolean isDirectionOk() { return (!wrongDir); }
    public void resetDirectionOk()
    {
      if (!isDistanceOk()) { return; }
      wrongDirHint = false;
      wrongDir = false;
    }
    public void checkDirectionOk(Location pos, Instruction in, NaviVoice v)
    {
      calculateWrongDir(pos, in);
      if (wrongDir)
      {
        if (wrongDirHint) { return; }
        wrongDirHint = true;
        v.speak("Wrong direction", Text.getInstance().getWrongDirection());
      }
    }
    
    private void calculateWrongDir(Location pos, Instruction in)
    {
      if (in.getPoints().size()<2) { return; }
      if (!wrongDir)
      {
        GeoPoint pathP1 = new GeoPoint(in.getPoints().getLat(0), in.getPoints().getLon(0));
        GeoPoint pathP2 = new GeoPoint(in.getPoints().getLat(1), in.getPoints().getLon(1));
        double bearingOk = pathP1.bearingTo(pathP2);
        double bearingCur = pos.getBearing();
        double bearingDiff = bearingOk - bearingCur;
        if (bearingDiff < 0) { bearingDiff += 360.0; } //Normalize
        if (bearingDiff > 180) { bearingDiff = 360.0 - bearingDiff; } //Normalize
        wrongDir = (bearingDiff > 100);
log("Compare bearing cur=" + bearingCur + " way=" + bearingOk + " wrong=" + wrongDir);
      }
    }
    
    public void setBaseData(PointPosData p)
    {
      this.arrIdx = p.arrIdx;
      this.distance = p.distance;
      this.status = p.status;
      if (arrIdx > 0)
      {
    	  //TODO: What happens, when moving along the path, and then moving wrong direction along the path?
        resetDirectionOk();
      }
    }
  }
}
