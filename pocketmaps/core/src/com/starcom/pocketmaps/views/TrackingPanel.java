package com.starcom.pocketmaps.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.starcom.gdx.ui.AbsLayout;
import com.starcom.gdx.ui.GuiUtil;
import com.starcom.pocketmaps.map.Tracking;

public class TrackingPanel
{
	private static TrackingPanel instance = new TrackingPanel();
	private static final String B_START = "Start";
	private static final String B_STOP = "Stop";
	private static final String B_SHOW = "Show";
	private static final String B_SAVE = "Save";
	private boolean visible;
	private AbsLayout al;
	private Actor aPan;
	private TextButton aStartBut, aShowBut, aSaveBut, aCloseBut;
	private Label statLab;

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
		

		aShowBut = GuiUtil.genButton(B_SHOW, 0, 0, (a,xx,yy) -> startTracking());
		al.addChild(aStartBut, 0.2f, 0.2f, 0.1f, 0.2f);
		aStartBut = GuiUtil.genButton(B_START, 0, 0, (a,xx,yy) -> startTracking());
		al.addChild(aStartBut, 0.2f, 0.2f, 0.1f, 0.2f);
		
		aCloseBut = GuiUtil.genButton("[X]", 0, 0, (a,xx,yy) -> setVisible(false, true));
		al.addChild(aCloseBut, 0.9f, 0.1f, 0.05f, 0.2f);
		statLab = GuiUtil.genLabel("---", 0, 0);
		al.addChild(statLab, 0.1f, 0.8f, 0.8f, 0.2f);
	}
	
	private void startTracking()
	{
		if (!Tracking.getInstance().isTracking())
		{
			System.out.println("starting");
			aStartBut.setText(B_STOP);
			Tracking.getInstance().startTracking();
		}
		else
		{
			System.out.println("stopping");
			aStartBut.setText(B_START);
			Tracking.getInstance().stopTracking();
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
	
	public void showData(int pCount, long timeMS)
	{
		statLab.setText("Count:" + pCount + " Time:" + (timeMS/1000) + "s");
	}
}
