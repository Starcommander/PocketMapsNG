package com.starcom.pocketmaps.views;

import java.util.Locale;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.starcom.gdx.io.Storage;
import com.starcom.gdx.ui.AbsLayout;
import com.starcom.gdx.ui.Dialogs;
import com.starcom.gdx.ui.GraphView.DataPoint;
import com.starcom.gdx.ui.GuiUtil;
import com.starcom.gdx.ui.ListSelect;
import com.starcom.navigation.Location;
import com.starcom.pocketmaps.tracking.AnalyticsGraph;
import com.starcom.pocketmaps.tracking.Tracking;
import com.starcom.pocketmaps.tracking.Tracking.TrackingStatus;
import com.starcom.pocketmaps.util.Calorie;
import com.starcom.pocketmaps.util.UnitCalculator;
import com.starcom.system.Threading;

public class TrackingPanel
{
	private static TrackingPanel instance = new TrackingPanel();
	private static final String B_START = "Start";
	private static final String B_STOP = "Stop";
	private static final String B_RESET = "Reset";
	private static final String B_SHOW = "Show";
	private static final String B_SAVE = "Save";
	private static final String B_LOAD = "Load";
    private static final String SB_WALK = "walk";
    private static final String SB_BIKE = "bike";
    private static final String SB_CAR = "car";
    private static final String TR_PATH = "tracking";
	private boolean visible;
	private AbsLayout al, alFull;
	private Actor aPan;
	private AnalyticsGraph graph = new AnalyticsGraph();
	private TextButton aStartBut, aShowBut, aSaveBut, aCloseBut;
	private Label statLab;
    private SelectBox<String> spinner;
    private Label durationTV, avgSpeedTV, maxSpeedTV, distanceTV, distanceUnitTV, caloriesTV, maxSpeedUnitTV, avgSpeedUnitTV;

	private TrackingPanel()
	{
		int w = Gdx.graphics.getWidth();
		int h = Gdx.graphics.getHeight()/8;
		int x = 0;
		int y = 0;
		aPan = GuiUtil.genPanel(x,y,w,h);
		al = new AbsLayout(0, 0.875f, 1.0f, 0.125f);
		al.setMinHeight(60);
		al.addChild(aPan, 0, 0, 1, 1);
		
		aStartBut = GuiUtil.genButton(B_START, 0, 0, (a,xx,yy) -> startTracking());
		al.addChild(aStartBut, 0.2f, 0.2f, 0.1f, 0.2f);

		aShowBut = GuiUtil.genButton(B_SHOW, 0, 0, (a,xx,yy) -> showAnalytics());
		al.addChild(aShowBut, 0.4f, 0.2f, 0.1f, 0.2f);
		
		aSaveBut = GuiUtil.genButton(B_LOAD, 0, 0, (a,xx,yy) -> loadSaveAnalytics());
		al.addChild(aSaveBut, 0.2f, 0.6f, 0.1f, 0.2f);
		
		aCloseBut = GuiUtil.genButton("[X]", 0, 0, (a,xx,yy) -> setVisible(false, true));
		al.addChild(aCloseBut, 0.9f, 0.1f, 0.05f, 0.2f);
		
		statLab = GuiUtil.genLabel("---", 0, 0);
		al.addChild(statLab, 0.1f, 0.8f, 0.8f, 0.2f);
		
		alFull = new AbsLayout(0, 0, 1, 1);
    	spinner = GuiUtil.genDropDown((s) -> updateCalorieBurned(), 0, 0, SB_WALK, SB_BIKE, SB_CAR);
        distanceTV = GuiUtil.genLabel("Distance", 0, 0);
        distanceUnitTV = GuiUtil.genLabel("m", 0, 0);
        caloriesTV = GuiUtil.genLabel("Calories", 0, 0);
        maxSpeedTV = GuiUtil.genLabel("MaxSpeed", 0, 0);
        durationTV = GuiUtil.genLabel("Duration", 0, 0);
        avgSpeedTV = GuiUtil.genLabel("AvgSpeed", 0, 0);
        maxSpeedUnitTV = GuiUtil.genLabel("km/h", 0, 0);
        avgSpeedUnitTV = GuiUtil.genLabel("km/h", 0, 0);
        Actor aBackBut = GuiUtil.genButton(B_SHOW, 0, 0, (a,xx,yy) -> showAnalytics());
		alFull.addChild(aBackBut, 0, 0.1f, 0.1f, 0.1f);
        alFull.addChild(spinner, 0.1f, 0.3f, 0.3f, 0.1f);
        alFull.addChild(distanceTV, 0.1f, 0.1f, 0.3f, 0.1f);
        alFull.addChild(distanceUnitTV, 0.1f, 0.2f, 0.3f, 0.1f);
        alFull.addChild(caloriesTV, 0.1f, 0.5f, 0.3f, 0.1f);
        alFull.addChild(maxSpeedTV, 0.6f, 0.1f, 0.3f, 0.1f);
        alFull.addChild(durationTV, 0.6f, 0.3f, 0.3f, 0.1f);
        alFull.addChild(avgSpeedTV, 0.6f, 0.5f, 0.3f, 0.1f);
        alFull.addChild(maxSpeedUnitTV, 0.6f, 0.2f, 0.3f, 0.1f);
        alFull.addChild(avgSpeedUnitTV, 0.6f, 0.6f, 0.3f, 0.1f);
        alFull.addChild(graph.getGraph().getImage(), 0f, 0.6f, 1f, 0.4f);
	}
	
	private void loadSaveAnalytics()
	{
		if (aSaveBut.getText().toString().equals(B_SAVE))
		{
			Dialog d = Dialogs.genTextInputDialog(GuiUtil.getStage(), "Enter filename", (f) -> Tracking.getInstance().saveAsGPX(f));
			d.show(GuiUtil.getStage());
		}
		else // B_LOAD
		{
			ListSelect l = new ListSelect("Load tracking file");
			for (FileHandle f : Storage.getFileHandle(TR_PATH).list())
			{
				l.addElement(f.name(), (a,x,y) -> Tracking.getInstance().loadData(f.file()));
			}
			l.showAsWindow(GuiUtil.getStage());
		}
	}
	
	private void showAnalytics()
	{
		if (al.getParent() == null)
		{
			alFull.remove();
			GuiUtil.getStage().addActor(al);
		}
		else
		{
			al.remove();
			GuiUtil.getStage().addActor(alFull);
		}
	}

	private void startTracking()
	{
		boolean b = aStartBut.getText().toString().equals(B_START);
		System.out.println("'" + aStartBut.getText() +"'='" + B_START + "'="  +b);
		if (aStartBut.getText().toString().equals(B_START))
		{
			//TODO: First test status with: Tracking.getInstance().getStatus() != TrackingStatus.Running?
			aSaveBut.setText(B_SAVE);
			GuiUtil.setEnabled(aSaveBut, false);
			System.out.println("starting");
			aStartBut.setText(B_STOP);
			Tracking.getInstance().startTracking();
		}
		else if (aStartBut.getText().toString().equals(B_STOP))
		{
			GuiUtil.setEnabled(aSaveBut, true);
			System.out.println("stopping");
			aStartBut.setText(B_RESET);
			Tracking.getInstance().stopTracking();
		}
		else if (aStartBut.getText().toString().equals(B_RESET))
		{
			aStartBut.setText(B_START);
			aSaveBut.setText(B_LOAD);
			GuiUtil.setEnabled(aSaveBut, true);
		}
	}
	
	public static TrackingPanel getInstance() { return instance; }
	
	/** Sets this View visible or invisible.
	 * @param visible Whether this NavSelectPanel should be visible.
	 * @param withTopPanelSwitch Switch between this Panel and TopPanel, so hide TopPanel, when this Panel gets visible, show otherwise. */
	public void setVisible(boolean visible, boolean withTopPanelSwitch)
	{
		if (this.visible == visible) { return; }
		if (visible)
		{
			if (withTopPanelSwitch) { TopPanel.getInstance().setVisible(false); }
			GuiUtil.addActor(al);
		}
		else
		{
			if (withTopPanelSwitch) { TopPanel.getInstance().setVisible(true); }
			al.remove();
		}
		this.visible = visible;
	}

    public void updateTrackingData(int pCount, long timeMS, double distanceToLastPoint, double speed)
    {
		statLab.setText("Count:" + pCount + " Time:" + (timeMS/1000) + "s");
        updateDistance(Tracking.getInstance().getDistance());
        updateAvgSpeed(Tracking.getInstance().getAvgSpeed());
        updateMaxSpeed(Tracking.getInstance().getMaxSpeed());
        updateCalorieBurned();
    	updateTimeSpent();
    	if (alFull.getParent() != null)
    	{
    		double fullDistance = Tracking.getInstance().getDistance();
    		int w = Gdx.graphics.getWidth();
    		int h = (int)(Gdx.graphics.getHeight()*0.4f);
    		graph.updateGraphSeries(new DataPoint((timeMS/1000), distanceToLastPoint), new DataPoint((timeMS/1000), fullDistance));
    		Threading.getInstance().invokeOnMainThread(() -> graph.getGraph().drawToImage(w,h));
    		graph.getGraph().getImage().layout();  //TODO: untested
    		graph.getGraph().drawToImage(Gdx.graphics.getWidth(), (int)(Gdx.graphics.getHeight()*0.4f)); //TODO: untested
    	}
    }
    
    /**
     * update avg speedGraphSeries
     *
     * @param avgSpeed in km/h
     */
    private void updateAvgSpeed(Double avgSpeed) {
        avgSpeedTV.setText(UnitCalculator.getBigDistance(avgSpeed * 1000.0, 2));
    }

    /**
     * update max speedGraphSeries
     *
     * @param maxSpeed in km/h
     */
    private void updateMaxSpeed(Double maxSpeed) {
        maxSpeedTV.setText(UnitCalculator.getBigDistance(maxSpeed * 1000.0, 2));
    }

    private void updateCalorieBurned() {
        long endTime = System.currentTimeMillis();
        if (Tracking.getInstance().getStatus() == TrackingStatus.Stopped) { endTime = Tracking.getInstance().getTimeEnd(); } //TODO: Also handle TrackingStatus.Pause
        double speedKmh = Tracking.getInstance().getAvgSpeed();
        double met = Calorie.getMET(speedKmh, getSportCategory());
        double cals = Calorie.calorieBurned(met, Tracking.getInstance().getDurationInHours(endTime));
        caloriesTV.setText(String.format(Locale.getDefault(), "%.2f", cals));
    }
    
    private void updateTimeSpent()
    {
      long endTime = System.currentTimeMillis();
      if (Tracking.getInstance().getStatus() == TrackingStatus.Stopped) { endTime = Tracking.getInstance().getTimeEnd(); } //TODO: Also handle TrackingStatus.Pause
      long updatedTime = endTime - Tracking.getInstance().getTimeStart();
      int secs = (int) (updatedTime / 1000);
      int mins = secs / 60;
      secs = secs % 60;
      int hours = mins / 60;
      mins = mins % 60;
      durationTV.setText("" + String.format(Locale.getDefault(), "%02d", hours) + ":" + String.format(Locale.getDefault(), "%02d", mins) + ":" +
              String.format(Locale.getDefault(), "%02d", secs));
    }

    /**
     * get activity type (MET) from selected spinner (object)
     */
    private Calorie.Type getSportCategory() {
    	if (SB_WALK.equals(spinner.getSelected()))
    	{
    		return Calorie.Type.Run;
    	}
    	else if (SB_BIKE.equals(spinner.getSelected()))
    	{
    		return Calorie.Type.Bike;
    	}
        return Calorie.Type.Car;
    }

    /**
     * update distanceGraphSeries
     *
     * @param distance in meter.
     */
    private void updateDistance(Double distance) {
      
        if (distance < UnitCalculator.getMultValue()) {
            distanceTV.setText(UnitCalculator.getShortDistance(distance));
            distanceUnitTV.setText(UnitCalculator.getUnit(false));
        } else {
            distanceTV.setText(UnitCalculator.getBigDistance(distance, 2));
            distanceUnitTV.setText(UnitCalculator.getUnit(true));
        }
    }
}
