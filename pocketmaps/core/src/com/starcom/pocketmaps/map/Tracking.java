package com.starcom.pocketmaps.map;

import com.starcom.pocketmaps.Cfg;
//import com.jjoe64.graphview.series.DataPoint;
//import com.starcom.pocketmaps.fragments.AppSettings;
//import com.junjunguo.pocketmaps.db.DBtrackingPoints;
//import com.junjunguo.pocketmaps.model.listeners.TrackingListener;
import com.starcom.pocketmaps.util.GenerateGPX;
import com.starcom.pocketmaps.views.MapList;
import com.starcom.pocketmaps.views.TopPanel;
import com.starcom.pocketmaps.views.TrackingPanel;
//import com.junjunguo.pocketmaps.util.Variable;
import com.starcom.system.Threading;
import com.starcom.gdx.ui.ToastMsg;
import com.starcom.navigation.Location;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.oscim.core.GeoPoint;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on August 16, 2015.
 */
public class Tracking {
    private static Tracking tracking = new Tracking();
    private double avgSpeed, maxSpeed, distance;
    private Location startLocation;
    private long timeStart, timeEnd;

    private boolean isOnTracking;
    private ArrayList<Location> dBtrackingPoints = new ArrayList<Location>();
//    private List<TrackingListener> listeners;

    private Tracking() {
//        isOnTracking = false;
//        dBtrackingPoints = new DBtrackingPoints(applicationContext);
//        listeners = new ArrayList<>();
    }

    public static Tracking getInstance() {
        return tracking;
    }

    /**
     * stop Tracking: is on tracking false
     */
    public void stopTracking() {
        isOnTracking = false;
        initAnalytics();
//        appSettings.updateAnalytics(0, 0); //TODO: View speed=0 distance=0
    }

    /**
     * set avg speed & distance to 0 & start location = null;
     */
    private void initAnalytics() {
        avgSpeed = 0; // km/h
        maxSpeed = 0; // km/h
        distance = 0; // meter
        timeStart = System.currentTimeMillis();
        startLocation = null;
    }

    /**
     * init and start tracking
     */
    public void startTracking() {
        init();
        initAnalytics();
//        MapHandler.getInstance().startTrack(); TODO: Reset 
        isOnTracking = true;
    }

    public void init() {
        dBtrackingPoints.clear();
        isOnTracking = false;
    }
    
    public void loadData(final File gpxFile) {
      try
      {
        isOnTracking = false;
        initAnalytics();
        init();
        ToastMsg.getInstance().toastLong("loading ...");
        Threading.getInstance().invokeAsyncTask(() -> new GenerateGPX().readGpxFile(gpxFile), (o) ->  loadDataPost(o));
//        new AsyncTask<Void, Void, ArrayList<Location>>() {
//          @Override
//          protected ArrayList<Location> doInBackground(Void... params)
//          {
//            try
//            {
//              return new GenerateGPX().readGpxFile(gpxFile);
//            }
//            catch (Exception e) { e.printStackTrace(); }
//            return null;
//          }
//          @Override
//          protected void onPostExecute(ArrayList<Location> posList)
//          {
//            if (posList == null) { return; } // On exception
//            appSettings.openAnalyticsActivity(false);
//            MapHandler.getMapHandler().startTrack(activity);
//            boolean first = true;
//            for (Location pos : posList)
//            {
//              if (first)
//              { // Center on map.
//                GeoPoint firstP = new GeoPoint(pos.getLatitude(), pos.getLongitude());
//                MapHandler.getMapHandler().centerPointOnMap(firstP, 0, 0, 0);
//                setTimeStart(pos.getTime());
//                first = false;
//              }
//              MapHandler.getMapHandler().addTrackPoint(activity, new GeoPoint(pos.getLatitude(), pos.getLongitude()));
//              addPoint(pos, appSettings);
//            }
//          }
//        }.execute();
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    
    private void loadDataPost(Object o)
    {
    	ArrayList<Location> posList = (ArrayList<Location>)o;
        if (posList == null) { return; } // On exception
//        appSettings.openAnalyticsActivity(false); TODO: View
//        MapHandler.getMapHandler().startTrack(activity); TODO: Start
        boolean first = true;
        for (Location pos : posList)
        {
          if (first)
          { // Center on map.
            GeoPoint firstP = new GeoPoint(pos.getLatitude(), pos.getLongitude());
            MapHandler.getInstance().centerPointOnMap(firstP, 0, 0, 0);
            setTimeStart(pos.getTime());
            first = false;
          }
//          MapHandler.getInstance().addTrackPoint(new GeoPoint(pos.getLatitude(), pos.getLongitude())); TODO: Add
          addPoint(pos);
        }
    }

    /**
     * @return average speed in km/h
     */
    public double getAvgSpeed() {
        return avgSpeed;
    }

    /**
     * @return max speed in km/h
     */
    public double getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(double maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    /**
     * @return total distance through in meters
     */
    public double getDistance() {
        return distance;
    }

    /**
     * @return total distance through in km
     */
    public double getDistanceKm() {
        return distance / 1000.0;
    }

    /**
     * @return tracking start time in milliseconds
     */
    public long getTimeStart() {
        return timeStart;
    }
    
    /**
     * @return tracking end time in milliseconds, only used for loading old TrackingData
     */
    public long getTimeEnd() {
        return timeEnd;
    }
    
    /**
     * Tracking start time in milliseconds.
     * This function is used for loading old TrackingData.
     */
    public void setTimeStart(long timeStart) {
        this.timeStart = timeStart;
    }
    
    /**
     * Tracking end time in milliseconds.
     * This function is used for loading old TrackingData.
     */
    public void setTimeEnd(long timeEnd) {
      this.timeEnd = timeEnd;
  }
    
    /**
     * @return total points recorded --> database row count
     */
    public int getTotalPoints() {
        return dBtrackingPoints.size();
//        int p = dBtrackingPoints.getRowCount();
//        dBtrackingPoints.close();
//        return p;
    }

    /**
     * @return true if is on tracking
     */
    public boolean isTracking() {
        return isOnTracking;
    }

    /**
     * add a location point to points list
     *
     * @param location
     */
    private void addPoint(Location location) {
        dBtrackingPoints.add(location);
//        dBtrackingPoints.addLocation(location);
//        dBtrackingPoints.close();
        updateDisSpeed(location);// update first
        updateMaxSpeed(location);// update after updateDisSpeed
        startLocation = location;
    }
    
    public void onLocationChanged(Location location)
    {
    	if (isOnTracking)
    	{
    		addPoint(location);
    		long time = System.currentTimeMillis() - timeStart;
    		TrackingPanel.getInstance().showData(dBtrackingPoints.size(), time);
    	}
    }

    /**
     * distance DataPoint series  DataPoint (x, y) x = increased time, y = increased distance
     * <p/>
     * Listener will handler the return data
     */
//    public void requestDistanceGraphSeries() {
//    	dBtrackingPoints.stream().forEach(null); TODO: Implement if necessary
//        new AsyncTask<URL, Integer, DataPoint[][]>() {
//            protected DataPoint[][] doInBackground(URL... params) {
//                try {
//                    dBtrackingPoints.open();
//                    DataPoint[][] dp = dBtrackingPoints.readGraphSeries();
//                    dBtrackingPoints.close();
//                    return dp;
//                } catch (Exception e) {e.printStackTrace();}
//                return null;
//            }
//
//            protected void onPostExecute(DataPoint[][] dataPoints) {
//                super.onPostExecute(dataPoints);
//                broadcast(null, null, null, dataPoints);
//            }
//        }.execute();
//    }

    /**
     * update distance and speed
     *
     * @param location
     */
    private void updateDisSpeed(Location location) {
        if (startLocation != null) {
            double disPoints = startLocation.distanceTo(location);
            distance += disPoints;
            long duration = getDurationInMilliS(location.getTime());
            avgSpeed = (distance) / (duration / (60 * 60));
//            if (appSettings.getAppSettingsVP().getVisibility() == View.VISIBLE) { TODO: Implement this view
//                appSettings.updateAnalytics(avgSpeed, distance);
//            }
//            broadcast(avgSpeed, null, distance, null); TODO: Implement if necessary
        }
    }

    /**
     * @return duration in milli second
     */
    public long getDurationInMilliS() {
      long now = System.currentTimeMillis();
        return (now - timeStart);
    }
    
    /**
     * @return duration in milli second
     */
    public long getDurationInMilliS(long endTime) {
      return endTime - timeStart;
    }

    /**
     * @return duration in hours
     */
    public double getDurationInHours() {
        return (getDurationInMilliS() / (60 * 60 * 1000.0));
    }
    
    /**
     * @return duration in hours, but with different endTime
     */
    public double getDurationInHours(long endTime) {
        return getDurationInMilliS(endTime) / (60 * 60 * 1000.0);
    }
    
    /**
     * update max speed and broadcast DataPoint for speeds and distances
     *
     * @param location
     */
    private void updateMaxSpeed(Location location) {
        if (startLocation != null) {
            // velocity: m/s
            double velocity =
                    (startLocation.distanceTo(location)) / ((location.getTime() - startLocation.getTime()) / (1000.0));
//            broadcastNewPoint(); TODO: Implement if necessary
            //            TODO: improve noise reduce (Kalman filter)
            // TODO: http://dsp.stackexchange.com/questions/8860/more-on-kalman-filter-for-position-and-velocity
            velocity = velocity * (6 * 6 / 10);// velocity: km/h
            //            if (maxSpeed < velocity && velocity < (maxSpeed + 32) * 10) {
            if (maxSpeed < velocity) {
                maxSpeed = (float) velocity;
//                broadcast(null, maxSpeed, null, null); TODO: Implement if necessary
            }
        }
    }


//    private void broadcastNewPoint() {
//        for (TrackingListener tl : listeners) {
//            tl.setUpdateNewPoint();
//        }
//    }

    /**
     * set null if do not need to update
     *
     * @param avgSpeed in km/h
     * @param maxSpeed in km/h
     * @param distance in m
     */

//    private void broadcast(Double avgSpeed, Double maxSpeed, Double distance, DataPoint[][] dataPoints) {
//        for (TrackingListener tl : listeners) {
//            if (avgSpeed != null) {
//                tl.updateAvgSpeed(avgSpeed);
//            }
//            if (maxSpeed != null) {
//                tl.updateMaxSpeed(maxSpeed);
//            }
//            if (distance != null) {
//                tl.updateDistance(distance);
//            }
//            if (dataPoints != null) {
//                tl.updateDistanceGraphSeries(dataPoints);
//            }
//        }
//    }

    /**
     * remove from listeners list
     *
     * @param listener
     */
//    public void removeListener(TrackingListener listener) {
//        listeners.remove(listener);
//    }

    /**
     * add to listeners list
     *
     * @param listener
     */
//    public void addListener(TrackingListener listener) {
//        listeners.add(listener);
//    }

    /**
     * export location data from database to GPX file
     *
     * @param name folder name
     */
    public void saveAsGPX(final String name) {
        final File trackFolder = new File("/tmp/track"); // TODO: Set from Cfg
        trackFolder.mkdirs();
        final File gpxFile = new File(trackFolder, name);
        if (!gpxFile.exists()) {
            try {
                gpxFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Threading.getInstance().invokeAsyncTask(() ->
        {
        	new GenerateGPX().writeGpxFile(name, dBtrackingPoints, gpxFile);
        	return null;
        }, (o) ->
        {
        	if (gpxFile.exists()) { gpxFile.renameTo(new File(trackFolder, name + ".gpx")); }
        });
//        new AsyncTask<Object, Object, Object>() {
//            protected Object doInBackground(Object... params) {
//                try {
//                    new GenerateGPX().writeGpxFile(name, dBtrackingPoints, gpxFile);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return null;
//            }
//
//            protected void onPostExecute(Object o) {
//                super.onPostExecute(o);
//                if (gpxFile.exists()) {
//                    gpxFile.renameTo(new File(trackFolder, name + ".gpx"));
//                }
//            }
//        }.execute();
    }
}
