package com.starcom.pocketmaps.tracking;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
//import com.jjoe64.graphview.GraphView;
//import com.jjoe64.graphview.LegendRenderer;
//import com.jjoe64.graphview.series.DataPoint;
//import com.jjoe64.graphview.series.LineGraphSeries;
import com.starcom.gdx.ui.GraphView;
import com.starcom.gdx.ui.GraphView.LegendRenderer;
import com.starcom.gdx.ui.GraphView.DataPoint;
import com.starcom.gdx.ui.GraphView.LineGraphSeries;
import com.starcom.gdx.ui.GuiUtil;
import com.starcom.navigation.Location;
import com.starcom.pocketmaps.Cfg;
import com.starcom.pocketmaps.Cfg.NavKeyB;
import com.starcom.pocketmaps.util.UnitCalculator;

import java.util.ArrayList;
import java.util.Locale;
/**
 * This file is part of PocketMaps
 * <p>
 * Created by GuoJunjun <junjunguo.com> on July 04, 2015.
 */
public class AnalyticsGraph {
    // status   -----------------
    public static boolean startTimer = true;
    /**
     * a sport category spinner: to choose with type of sport which also has MET value in its adapter
     */
    // duration
//    private Handler durationHandler;
//    private Handler calorieUpdateHandler;

    // graph   -----------------
    /**
     * max value for it's axis (minimum 0)
     * <p/>
     * <li>X axis: time - hours</li> <li>Y1 (left) axis: speed - km/h</li> <li>Y2 (right) axis: distance - km</li>
     */
    private double maxXaxis, maxY1axis, maxY2axis;
    /**
     * has a new point needed to update to graph view
     */
//    private boolean hasNewPoint;
    private GraphView graph = new GraphView();
    private LineGraphSeries<DataPoint> speedGraphSeries;
    private LineGraphSeries<DataPoint> distanceGraphSeries;

    public AnalyticsGraph()
    {
        initGraph();
    }
    
    public GraphView getGraph()
    {
    	return graph;
    }
    
    /**
     * new thread to update timer
     */
//    private Runnable updateTimerThread = new Runnable() {
//        public void run() {
//            updateTimeSpent();
//            
//            durationHandler.postDelayed(this, 500);
//        }
//    };

    /**
     * new thread to update calorie burned every 10 second
     */
//    private Runnable updateCalorieThread = new Runnable() {
//        public void run() {
//            updateCalorieBurned();
//            calorieUpdateHandler.postDelayed(this, 10000);
//            // reload graph
//            if (hasNewPoint) {
//                Tracking.getInstance().requestDistanceGraphSeries();
//                hasNewPoint = false;
//            }
//        }
//    };

    // ----------  graph ---------------

    /**
     * init and setup Graph Contents
     */
    private void initGraph() {
        maxXaxis = 0.1;
        maxY1axis = 10;
        maxY2axis = 0.4;

        speedGraphSeries = new LineGraphSeries<>();
        graph.getScale().addSeries(speedGraphSeries);
        graph.setTextColor(0xFF009688);

//        graph.getGridLabelRenderer().setVerticalLabelsColor(0xFF009688);
//        graph.getViewport().setYAxisBoundsManual(true);
        resetGraphY1MaxValue();
        distanceGraphSeries = new LineGraphSeries<>();
        //        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setScalable(true);
        graph.getViewport().setScrollable(true);

        graph.getViewport().setMinX(0);
        // set second scale
        graph.getSecondScale().addSeries(distanceGraphSeries);
        // the y bounds are always manual for second scale
        graph.getSecondScale().setMinY(0);
        resetGraphY2MaxValue();
        //        resetGraphXMaxValue();
//        graph.getGridLabelRenderer().setVerticalLabelsSecondScaleColor(0xFFFF5722);
        // legend
        if (Cfg.getBoolValue(NavKeyB.IsImperialUnit, false))
        {
          speedGraphSeries.setTitle("Speed mi/h");
          distanceGraphSeries.setTitle("Distance mi");
        }
        else
        {
          speedGraphSeries.setTitle("Speed km/h");
          distanceGraphSeries.setTitle("Distance km");
        }
        speedGraphSeries.setColor(0xFF009688);
        distanceGraphSeries.setColor(0xFFFF5722);
        graph.getLegendRenderer().setVisible(true);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
    }

    /**
     * auto setup max value for graph first y scale
     */
    public void resetGraphY1MaxValue() {
        double maxSpeed = Tracking.getInstance().getMaxSpeed();
        maxSpeed = UnitCalculator.getBigDistanceValue(maxSpeed);
        if (maxSpeed > maxY1axis) {
            int i = ((int) (maxSpeed + 0.9999));
            maxY1axis = i + 4 - (i % 4);
        }
        //        log("resetGraphY1MaxValue max speed: " + maxSpeed + "\n speedGraphSeries" +
        //                ".getHighestValueY() " + maxY1axis + "\nTracking().getMaxSpeed() " +
        //                Tracking.Tracking.getInstance().getMaxSpeed());
        graph.getViewport().setMaxY(maxY1axis);
    }

    /**
     * auto setup max value for graph second y scale
     */
    public void resetGraphY2MaxValue() {
        //        double max = 0.4;
        double dis = Tracking.getInstance().getDistanceKm();
        dis = UnitCalculator.getBigDistanceValue(dis);
        if (dis > maxY2axis * 0.9) {
            maxY2axis = getMaxValue(dis, maxY2axis);
        }
        //        log("max Y: " + maxY2axis);
        graph.getSecondScale().setMaxY(maxY2axis);
    }

    /**
     * @param dis
     * @param max
     * @return max * 2 until max > dis * 1.1
     */
    private double getMaxValue(double dis, double max) {
        if (max > dis * 1.1) {
            return max;
        }
        return getMaxValue(dis, max * 2);
    }

    public void resetGraphXMaxValue() {
        //        double max = 0.1;
        double time;
        if (!startTimer)
        {
          long end = Tracking.getInstance().getTimeEnd();
          time = Tracking.getInstance().getDurationInHours(end);
        }
        else
        {
        	time = Tracking.getInstance().getDurationInHours();
        }
        if (time > maxXaxis * 0.9) {
            maxXaxis = getMaxValue(time, maxXaxis);
        }
//        log("max X: " + maxXaxis + "; time: " + time);
        //        graph.getViewport().setXAxisBoundsManual(true);
        //        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(maxXaxis);
    }

//    @Override public void onResume() {
//        super.onResume();
//        if (startTimer)
//        {
//          durationHandler.postDelayed(updateTimerThread, 500);
//          calorieUpdateHandler.postDelayed(updateCalorieThread, 60000);
//        }
//        distanceUnitTV.setText(UnitCalculator.getUnit(false));
//        maxSpeedUnitTV.setText(UnitCalculator.getUnit(true) + "/h");
//        avgSpeedUnitTV.setText(UnitCalculator.getUnit(true) + "/h");
//        Tracking.getInstance().addListener(this);
//        //        graph
//        Tracking.getInstance().requestDistanceGraphSeries();
//    }

//    @Override public void onPause() {
//        super.onPause();
//        durationHandler.removeCallbacks(updateTimerThread);
//        calorieUpdateHandler.removeCallbacks(updateCalorieThread);
//        Tracking.getInstance().removeListener(this);
//    }

//    @Override public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            // Respond to the action bar's Up/Home button
//            case android.R.id.home:
//                finish();
//                return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    /**
     * Resets the graph data.
     *
     * @param dataPoints
     */
    public void resetGraphSeries(ArrayList<DataPoint> speedGraph, ArrayList<DataPoint> distanceGraph) {
        resetGraphY1MaxValue();
        resetGraphY2MaxValue();
        //        resetGraphXMaxValue();
        try
        {
          speedGraphSeries.resetData(speedGraph);
          distanceGraphSeries.resetData(distanceGraph);
        }
        catch (IllegalArgumentException e)
        {
          e.printStackTrace();
        }
        double maxV = speedGraphSeries.getHighestValueY();
        if (Cfg.getBoolValue(NavKeyB.IsImperialUnit, false))
        { // From miles convert back to km!
          double factor = UnitCalculator.METERS_OF_MILE / 1000.0;
          maxV = maxV * factor;
        }
//        Tracking.getInstance().setMaxSpeed(maxV);
//        TrackingPanel.getInstance().updateMaxSpeed(maxV);
//        if(!startTimer)
//        {
//          updateTimeSpent();
//          updateCalorieBurned();
//          updateAvgSpeed(Tracking.getInstance().getAvgSpeed());
//          resetGraphXMaxValue();
//        }
    }
    
    public void updateGraphSeries(DataPoint speed, DataPoint dist)
    {
          speedGraphSeries.addData(speed);
          distanceGraphSeries.addData(dist);
    }
    
//    public void setUpdateNewPoint()
//    {
//      hasNewPoint = true;
//    }
}
