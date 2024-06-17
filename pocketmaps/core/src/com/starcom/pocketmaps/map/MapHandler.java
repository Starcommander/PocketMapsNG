package com.starcom.pocketmaps.map;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.oscim.backend.canvas.Color;
import org.oscim.core.GeoPoint;
import org.oscim.core.MapPosition;
import org.oscim.event.Gesture;
import org.oscim.event.GestureListener;
import org.oscim.event.MotionEvent;
import org.oscim.layers.Layer;
import org.oscim.layers.PathLayer;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.marker.MarkerSymbol;
import org.oscim.map.Layers;
import org.oscim.map.Map;
import com.badlogic.gdx.Gdx;
import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.PathWrapper;
import com.graphhopper.routing.util.AllEdgesIterator;
import com.graphhopper.util.Constants;
import com.starcom.LoggerUtil;
import com.starcom.system.Threading;
import com.starcom.gdx.ui.ToastMsg;
import com.starcom.pocketmaps.Cfg;
import com.starcom.pocketmaps.Cfg.NavKeyB;
import com.starcom.pocketmaps.navigator.NaviEngine;
import com.starcom.pocketmaps.navigator.Navigator;
import com.starcom.pocketmaps.Icons;
import com.starcom.pocketmaps.geocoding.Address;
import com.starcom.pocketmaps.views.MapList;
import com.starcom.pocketmaps.views.NavSelect;
import com.starcom.pocketmaps.views.TopPanel;
import com.starcom.pocketmaps.views.VtmBitmap;
import com.starcom.pocketmaps.util.TargetDirComputer;
import com.graphhopper.util.Parameters.Algorithms;
import com.graphhopper.util.Parameters.Routing;
import com.graphhopper.util.PointList;
import com.graphhopper.util.StopWatch;

public class MapHandler
{
	static Logger logger = LoggerUtil.get(MapHandler.class);
  private static MapHandler mapHandler;
  private AtomicBoolean calcPathActive = new AtomicBoolean();
  MapPosition tmpPos = new MapPosition();
  private GeoPoint startMarker;
  private GeoPoint endMarker;
  private ItemizedLayer itemizedLayer;
  private ItemizedLayer customLayer;
  private PathLayer pathLayer;
//  private PathLayer polylineTrack;
  private MapHandlerListener mapHandlerListener = null;
//  private String currentArea;
//  File mapsFolder;
////  FloatingActionButton naviCenterBtn;
  PointList trackingPointList = new PointList();
////  private int customIcon = R.drawable.ic_my_location_dark_24dp;
  VtmBitmap customIcon = Icons.generateIconVtm(Icons.R.ic_my_location_dark_24dp); //TODO: VtmBitmap instead AwtBitmap
//  private MapFileTileSource tileSource;
  
  public static MapHandler getInstance()
  {
    if (mapHandler == null)
    {
      mapHandler = new MapHandler();
    }
    return mapHandler;
  }

//   /**
//    * reset class, build a new instance
//    */
//  public static void reset()
//  {
//    mapHandler = new MapHandler();
//  }

  private MapHandler() {}

//  public void init(MapView mapView, String currentArea, File mapsFolder)
//  {
//    this.mapView = mapView;
//    this.currentArea = currentArea;
//    this.mapsFolder = mapsFolder; // path/to/map/area-gh/
//  }
  
//  public void init(Map map)
//  {
//	  getInstance().map = map;
//  }
  
//  public MapFileTileSource getTileSource() { return tileSource; }

  /** Creates the marker layers for path and curPos */
  public void createAdditionalMapLayers(Map map)
  {
	    if (itemizedLayer != null) { throw new IllegalStateException("ItemizedLayer is already initialized."); }
	    itemizedLayer = new ItemizedLayer(map, (MarkerSymbol) null);
	    map.layers().add(itemizedLayer);
	    customLayer = new ItemizedLayer(map, (MarkerSymbol) null);
	    map.layers().add(customLayer);
	    map.layers().add(new MapEventsReceiver(map));
  }
  
  /** Creates a graphhopper instance.
   * @param mapFolder The path/to/continent_country
   * @param result Result with graphhopper instance or Exception. */
  public void createPathfinder(File mapFolder, Consumer result)
  {
	  logger.info("loading graph (" + Constants.VERSION + ") ... ");
	  Threading.getInstance().invokeAsyncTask(() ->
	  {
		  GraphHopper tmpHopp = new GraphHopper().forMobile();
          // Why is "shortest" missing in default config? Add!
          tmpHopp.getCHFactoryDecorator().addCHProfileAsString("shortest");
          tmpHopp.load(mapFolder.getAbsolutePath());
          logger.info("found graph " + tmpHopp.getGraphHopperStorage().toString() + ", nodes:" + tmpHopp.getGraphHopperStorage().getNodes());
          return tmpHopp;
	  }, result);
	  
//	  
//	  Threading.getInstance().invokeOnWorkerThread(() ->
//	  {
//          GraphHopper tmpHopp = new GraphHopper().forMobile();
//          // Why is "shortest" missing in default config? Add!
//          tmpHopp.getCHFactoryDecorator().addCHProfileAsString("shortest");
//          tmpHopp.load(new File(mapsFolder, currentArea).getAbsolutePath() + "-gh");
//          log("found graph " + tmpHopp.getGraphHopperStorage().toString() + ", nodes:" + tmpHopp.getGraphHopperStorage().getNodes());
//          hopper = tmpHopp;
//          Threading.getInstance().invokeOnMainThread(() ->
//          {
//        	  if (MapHandler.this.hopper == null)
//        	  {
//        		  logger.severe("An error happened while creating graph");
//        		  return;
//        	  }
//        	  
//          });
//	  });
//	  
//      new GHAsyncTask<Void, Void, Path>() {
//          protected Path saveDoInBackground(Void... v) throws Exception {
//              GraphHopper tmpHopp = new GraphHopper().forMobile();
//              // Why is "shortest" missing in default config? Add!
//              tmpHopp.getCHFactoryDecorator().addCHProfileAsString("shortest");
//              tmpHopp.load(new File(mapsFolder, currentArea).getAbsolutePath() + "-gh");
//              log("found graph " + tmpHopp.getGraphHopperStorage().toString() + ", nodes:" + tmpHopp.getGraphHopperStorage().getNodes());
//              hopper = tmpHopp;
//              return null;
//          }
//
//          protected void onPostExecute(Path o) {
//              if (hasError()) {
//                  logUser(activity, "An error happened while creating graph:"
//                          + getErrorMessage());
//              } else {
//                  logUser(activity, "Finished loading graph.");
//              }
//
//              GeoPoint g = ShowLocationActivity.locationGeoPoint;
//              String lss = ShowLocationActivity.locationSearchString;
//              if (g != null)
//              {
//                activity.getMapActions().onPressLocationEndPoint(g);
//                ShowLocationActivity.locationGeoPoint = null;
//              }
//              else if (lss != null)
//              {
//                activity.getMapActions().startGeocodeActivity(null, null, false, false);
//              }
//              prepareInProgress = false;
//          }
//      }.execute();
  }
	  
//public void loadMapx(File areaFolder, Map map) {
//    // Map events receiver
//    mapView.map().layers().add(new MapEventsReceiver(mapView.map()));
//
//    // Map file source
//    tileSource = new MapFileTileSource();
//    tileSource.setMapFile(new File(areaFolder, currentArea + ".map").getAbsolutePath());
//    VectorTileLayer l = mapView.map().setBaseMap(tileSource);
//    mapView.map().setTheme(VtmThemes.DEFAULT);
//    mapView.map().layers().add(new BuildingLayer(mapView.map(), l));
//    mapView.map().layers().add(new LabelLayer(mapView.map(), l));
//    
//    // Markers layer
//    itemizedLayer = new ItemizedLayer(mapView.map(), (MarkerSymbol) null);
//    mapView.map().layers().add(itemizedLayer);
//    customLayer = new ItemizedLayer(mapView.map(), (MarkerSymbol) null);
//    mapView.map().layers().add(customLayer);
//
//    // Map position
//    GeoPoint mapCenter = tileSource.getMapInfo().boundingBox.getCenterPoint();
//    mapView.map().setMapPosition(mapCenter.getLatitude(), mapCenter.getLongitude(), 1 << 12);
//
//    ViewGroup.LayoutParams params =
//        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//    activity.addContentView(mapView, params);
//    
//    loadGraphStorage(activity);
//  }
//  
//  void loadGraphStorage(final MapActivity activity) {
//      logUser(activity, "loading graph (" + Constants.VERSION + ") ... ");
//      new GHAsyncTask<Void, Void, Path>() {
//          protected Path saveDoInBackground(Void... v) throws Exception {
//              GraphHopper tmpHopp = new GraphHopper().forMobile();
//              // Why is "shortest" missing in default config? Add!
//              tmpHopp.getCHFactoryDecorator().addCHProfileAsString("shortest");
//              tmpHopp.load(new File(mapsFolder, currentArea).getAbsolutePath() + "-gh");
//              log("found graph " + tmpHopp.getGraphHopperStorage().toString() + ", nodes:" + tmpHopp.getGraphHopperStorage().getNodes());
//              hopper = tmpHopp;
//              return null;
//          }
//
//          protected void onPostExecute(Path o) {
//              if (hasError()) {
//                  logUser(activity, "An error happened while creating graph:"
//                          + getErrorMessage());
//              } else {
//                  logUser(activity, "Finished loading graph.");
//              }
//
//              GeoPoint g = ShowLocationActivity.locationGeoPoint;
//              String lss = ShowLocationActivity.locationSearchString;
//              if (g != null)
//              {
//                activity.getMapActions().onPressLocationEndPoint(g);
//                ShowLocationActivity.locationGeoPoint = null;
//              }
//              else if (lss != null)
//              {
//                activity.getMapActions().startGeocodeActivity(null, null, false, false);
//              }
//              prepareInProgress = false;
//          }
//      }.execute();
//  }
  
  public AllEdgesIterator getAllEdges(String searchCountry)
  {
    MapLayer mapLayer = MapList.getInstance().findMapLayerFromCountry(searchCountry);
    if (mapLayer.getPathfinder()==null) { return null; }
    if (mapLayer.getPathfinder().getGraphHopperStorage()==null) { return null; }
    return mapLayer.getPathfinder().getGraphHopperStorage().getAllEdges();
  }

  /**
   * center the LatLong point in the map and zoom map to zoomLevel
   *
   * @param latLong The position to center
   * @param zoomLevel (if 0 use current zoomlevel)
   * @param bearing The bearing/rotation
   * @param tilt The tilt, mostly modified in relation to speed and distance to next point.
   */
  public void centerPointOnMap(GeoPoint latLong, int zoomLevel, float bearing, float tilt)
  {
	if (!Threading.getInstance().isMainThread())
	{
		final int z = zoomLevel;
		Threading.getInstance().invokeOnMainThread((o) -> centerPointOnMap(latLong, z, bearing, tilt), "");
		return;
	}

	logger.info("Center on map: " + latLong.getLatitude() + ":" + latLong.getLongitude() + "/" + zoomLevel + ":" + bearing + ":" + tilt);
    Map map = TopPanel.getInstance().getGdxMap(); // Must exist at this time.
    if (zoomLevel == 0)
    {
      zoomLevel = map.getMapPosition().zoomLevel;
    }
    double scale = 1 << zoomLevel;
    tmpPos.setPosition(latLong);
    tmpPos.setScale(scale);
    tmpPos.setBearing(bearing);
    tmpPos.setTilt(tilt);
    map.animator().animateTo(300, tmpPos);
	map.updateMap();
	logger.info("Center on map done.");
  }
  
  /** Resets the tilt angle-rotation. */
  public void resetTilt(float tilt)
  {
      Map map = TopPanel.getInstance().getGdxMap(); 
      map.setMapPosition(map.getMapPosition().setTilt(tilt));
  }

  /** Set start or end Point-Marker.
   *  @param p The Point to set, or null.
   *  @param isStart True for startpoint false for endpoint.
   *  @param recalculate True to calculate path, when booth points are set.
   *  @return Whether the path will be recalculated. **/
  public boolean setStartEndPoint(Map map, Address p, boolean isStart, boolean recalculate)
  {
    boolean result = false;
    boolean refreshBoth = false;
    if (startMarker!=null && endMarker!=null && p!=null) { refreshBoth = true; }
      
    if (isStart)
    {
      startMarker = p==null ? null : p.toGeoPoint();
    }
    else { endMarker = p==null ? null : p.toGeoPoint(); }

    // remove routing layers
    if ((startMarker==null || endMarker==null) || refreshBoth)
    {
      if (pathLayer!=null) { pathLayer.clearPath(); }
      itemizedLayer.removeAllItems();
    }
    if (startMarker!=null) //TODO: UEbeltaeter.
    {
      itemizedLayer.addItem(createMarkerItem(startMarker, Icons.generateIconVtm(Icons.R.ic_location_start_24dp), 0.5f, 1.0f)); //TODO: Use this
//      itemizedLayer.addItem(createMarkerItem(startMarker, new VtmBitmap(), 0.5f, 1.0f));
    }
    if (endMarker!=null)
    {
      itemizedLayer.addItem(createMarkerItem(endMarker, Icons.generateIconVtm(Icons.R.ic_location_end_24dp), 0.5f, 1.0f)); //TODO: Use this
//      itemizedLayer.addItem(createMarkerItem(endMarker, new VtmBitmap(), 0.5f, 1.0f));
    }
    if (startMarker!=null && endMarker!=null && recalculate)
    {
      recalcPath(map);
      result = true;
    }
    map.updateMap(true);
    if (p != null) { NavSelect.getInstance().setLocation(p, isStart); }
    return result;
  }
  
  public void recalcPath(Map map)
  {
    calcPath(map, startMarker.getLatitude(), startMarker.getLongitude(), endMarker.getLatitude(), endMarker.getLongitude());
  }

  /** Set the custom Point for current location.
   *  Sets the offset to center.
   *  @param New location, or null to delete **/
  public void setCustomPoint(GeoPoint p)
  {
    if (customLayer==null) { return; } // Not loaded yet.
    customLayer.removeAllItems();
    if (p!=null)
    {
      customLayer.addItem(createMarkerItem(p, customIcon, 0.5f, 0.5f));
      TopPanel.getInstance().getGdxMap().updateMap(true);
    }
  }
  
  public void setCustomPointIcon(VtmBitmap customIcon)
  {
    this.customIcon = customIcon;
    if (customLayer.getItemList().size() > 0)
    { // RefreshIcon
      MarkerItem curSymbol = (MarkerItem)customLayer.getItemList().get(0);
      MarkerSymbol marker = createMarkerItem(new GeoPoint(0,0), customIcon, 0.5f, 0.5f).getMarker();
      curSymbol.setMarker(marker);
    }
  }

  private MarkerItem createMarkerItem(GeoPoint p, VtmBitmap bitmap, float offsetX, float offsetY) { //TODO: VtmBitmap instead AwtBitmap
      MarkerSymbol markerSymbol = new MarkerSymbol(bitmap, offsetX, offsetY);
      MarkerItem markerItem = new MarkerItem("", "", p);
//      MarkerItem markerItem = new MarkerItem("Holy", "shit", p); //TODO: Use above line
      markerItem.setMarker(markerSymbol);
      return markerItem;
  }

    /**
     * @return true if already loaded
     */
//    boolean isReady() {
//      return !prepareInProgress;
//    }

    /**
     * start tracking : reset polylineTrack & trackingPointList & remove polylineTrack if exist
     */
//    public void startTrack(Activity activity) {
//        if (polylineTrack != null) {
//            removeLayer(mapView.map().layers(), polylineTrack);
//        }
//        polylineTrack = null;
//        trackingPointList.clear();
//        if (polylineTrack != null) { polylineTrack.clearPath(); }
//        polylineTrack = updatePathLayer(activity, polylineTrack, trackingPointList, 0x99003399, 4);
//        NaviEngine.getNaviEngine().startDebugSimulator(activity, true);
//    }

    /**
     * add a tracking point
     *
     * @param point
     */
//    public void addTrackPoint(Map map, GeoPoint point) {
//      trackingPointList.add(point.getLatitude(), point.getLongitude());
//      updatePathLayer(map, polylineTrack, trackingPointList, 0x9900cc33, 4);
//      map.updateMap(true);
//    }
    
  /**
   * remove a layer from map layers
   *
   * @param layers
   * @param layer
   */
  public static void removeLayer(Layers layers, Layer layer)
  {
    if (layers != null && layer != null && layers.contains(layer))
    {
      layers.remove(layer);
    }
  }

//
//    /**
//     * assign a new GraphHopper
//     *
//     * @param hopper
//     */
//    public void setHopper(GraphHopper hopper) {
//        this.hopper = hopper;
//    }

    /** Sets a listener for 1 next touch-event.
     * @param mapHandlerListener The listener, that will be called on next touch/click event, and disposed.
     */
    public void setMapHandlerListener(MapHandlerListener mapHandlerListener) {
    	if (this.mapHandlerListener != null) { throw new IllegalStateException("MapHandlerListener already set."); }
    	if (mapHandlerListener == null) { throw new IllegalStateException("MapHandlerListener must not be null."); }
        this.mapHandlerListener = mapHandlerListener;
    }
    
    private Object calcPathNow(final double fromLat, final double fromLon,
            final double toLat, final double toLon)
    {
    	StopWatch sw = new StopWatch().start();
        GHRequest req = new GHRequest(fromLat, fromLon, toLat, toLon).
                setAlgorithm(Algorithms.DIJKSTRA_BI);
        req.getHints().put(Routing.INSTRUCTIONS, Cfg.getBoolValue(Cfg.NavKeyB.DirectionsOn, true));
        req.setVehicle(Cfg.getValue(Cfg.NavKey.TravelMode, Cfg.TRAVEL_MODE_CAR));
        req.setWeighting(Cfg.getValue(Cfg.NavKey.Weighting, "fastest"));
        if (Cfg.getBoolValue(Cfg.NavKeyB.ShowingSpeedLimits, false) || Cfg.getBoolValue(Cfg.NavKeyB.SpeakingSpeedLimits, false))
        {
            req.getPathDetails().add(com.graphhopper.routing.profiles.MaxSpeed.KEY);
            req.getPathDetails().add(com.graphhopper.util.Parameters.Details.AVERAGE_SPEED);
        }
        GHResponse resp = null;
        MapLayer ml = MapList.getInstance().findMapLayerFromLocation(new GeoPoint(toLat, toLon));
        if (ml != null)
        {
        	GraphHopper hopper = ml.getPathfinder();
        	if (hopper != null)
        	{
        		resp = hopper.route(req);
        	}
        }
        if (resp==null || resp.hasErrors())
        {
          NaviEngine.getNaviEngine().setDirectTargetDir(true);
          Throwable error;
          if (resp != null) { error = resp.getErrors().get(0); }
          else { error = new NullPointerException("Hopper is null!!!"); }
          logger.warning("Multible errors, first: " + error);
          resp = TargetDirComputer.getInstance().createTargetdirResponse(fromLat, fromLon, toLat, toLon);
        }
        else
        {
          NaviEngine.getNaviEngine().setDirectTargetDir(false);
        }
        float time = sw.stop().getSeconds();
        logger.info("Calculating took " + time + "s.");
        return resp;
    }

    private void calcPathDone(Map map, Object response)
    {
    	if (response instanceof Exception)
    	{
    		logger.log(Level.SEVERE, "Error calculating path", (Exception)response);
    	}
    	else
    	{
    		GHResponse ghResp = (GHResponse) response;
            if (!ghResp.hasErrors()) {
                PathWrapper resp = ghResp.getBest();
                logUser("The route is " + (int) (resp.getDistance() / 100) / 10f
                        + "km long, time:" + resp.getTime() / 60000f + "min.");
                updatePathLayer(map, resp.getPoints(), Color.MAGENTA, 4);
                map.updateMap(true);
                Navigator.getNavigator().setGhResponse(resp);
            } else {
                logUser("Multible errors: " + ghResp.getErrors().size());
                logger.warning("Multible errors, first: " + ghResp.getErrors().get(0));
            }
            calcPathActive.set(false);
        
    	}
    }
    
    public void calcPath(final Map map, final double fromLat, final double fromLon,
                         final double toLat, final double toLon) {
        boolean wasActive = calcPathActive.getAndSet(true);
        if (wasActive)
        {
        	logger.info("Skip calculating path as busy.");
        	return;
        }
        else
        {
          logger.info("calculating path ...");
        }
        
        Threading.getInstance().invokeAsyncTask(() -> { return calcPathNow(fromLat, fromLon, toLat, toLon); }, (o) -> { calcPathDone(map, o); });
    }
    
  private void updatePathLayer(Map map,PointList pointList, int color, int strokeWidth) {
      if (pathLayer==null) {
    	  pathLayer = createPathLayer(map, color, strokeWidth);
          map.layers().add(pathLayer);
      }
      ArrayList<GeoPoint> geoPoints = new ArrayList<>();
      //TODO: Search for a more efficient way
      for (int i = 0; i < pointList.getSize(); i++)
      {
          geoPoints.add(new GeoPoint(pointList.getLatitude(i), pointList.getLongitude(i)));
      }
      pathLayer.setPoints(geoPoints);
System.out.println("We set " + geoPoints.size() + " points."); //TODO: Delete this line
//PolyParser.addDebugLineX(map, geoPoints);
  }
  
  public void joinPathLayerToPos(double lat, double lon) //TODO: This function may be useful
  {
//    try
//    {
//      List<GeoPoint> geoPoints = new ArrayList<>();
//      geoPoints.add(new GeoPoint(lat,lon));
//      geoPoints.add(pathLayer.getPoints().get(1));
//      pathLayer.setPoints(geoPoints);
//    }
//    catch (Exception e) { logger.severe("Error: " + e); }
  }
    
  private PathLayer createPathLayer(Map map, int color, int strokeWidth)
  {
	  strokeWidth = (int)((float)strokeWidth * Gdx.graphics.getDensity());
      PathLayer newPathLayer = new PathLayer(map, color, strokeWidth); //TODO: Use the above one.
      return newPathLayer;
  }

//  public void showNaviCenterBtn(boolean visible)
//  {
//    if (visible)
//    {
//      naviCenterBtn.setVisibility(View.VISIBLE);
//    }
//    else
//    {
//      naviCenterBtn.setVisibility(View.GONE);
//    }
//  }
//
//  public void setNaviCenterBtn(final FloatingActionButton naviCenterBtn)
//  {
//    this.naviCenterBtn = naviCenterBtn;
//  }
    
  class MapEventsReceiver extends Layer implements GestureListener
  {

      MapEventsReceiver(org.oscim.map.Map map) {
          super(map);
      }

      @Override
      public boolean onGesture(Gesture g, MotionEvent e) {
          if (g instanceof Gesture.Tap) {
              GeoPoint p = mMap.viewport().fromScreenPoint(e.getX(), e.getY());
              if (mapHandlerListener != null)
              {
                mapHandlerListener.onPressLocation(p);
                mapHandlerListener = null;
              }
          }
          return false;
      }
  }
  
  private void logUser(String str)
  {
	logger.info(str);
    ToastMsg.getInstance().toastLong(str);
  }
}

