package com.starcom.pocketmaps.views;

import org.oscim.core.GeoPoint;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.starcom.gdx.ui.AbsLayout;
import com.starcom.gdx.ui.GuiUtil;
import com.starcom.gdx.ui.ToastMsg;
import com.starcom.navigation.Enums.Vehicle;
import com.starcom.pocketmaps.Cfg;
import com.starcom.pocketmaps.geocoding.Address;
import com.starcom.pocketmaps.map.MapHandler;
import com.starcom.pocketmaps.map.MapHandlerListener;
import com.starcom.pocketmaps.navigator.NaviEngine;

public class NavSelect implements MapHandlerListener
{
	public static String EMPTY_LOC = "...";
	private static NavSelect instance = new NavSelect();
	static final String SEL_FROM_LATLON = "From Lat/Lon";
	static final String SEL_POS_ON_MAP = "Select from map";
	static final String SEL_SEARCH_LOC = "Search location";
	static final String SEL_CUR_LOC = "Current location";
	
	enum TabAction{ StartPoint, EndPoint, AddFavourit, None };
	private TabAction tabAction = TabAction.None;

	private Actor aPan, aFromDD, aToDD, aFromL, aToL, aFromLTxt, aToLTxt, aX, aFromX, aToX;
	SelectBox<String> aTravelModeDD;
	AbsLayout al;
	private boolean visible;
	
	private NavSelect()
	{
		int w = Gdx.graphics.getWidth();
		int h = Gdx.graphics.getHeight()/8;
		int x = 0;
		int y = 0;
		aPan = GuiUtil.genPanel(x,y,w,h);
		al = new AbsLayout(0, 0.875f, 1.0f, 0.125f);
		al.setMinHeight(60);
		al.addChild(aPan, 0, 0, 1, 1);

		aFromDD = GuiUtil.genDropDown((o) -> onDropDown(o.toString(), true), x, y, SEL_CUR_LOC, SEL_FROM_LATLON, SEL_POS_ON_MAP, SEL_SEARCH_LOC);
		aFromL = GuiUtil.genLabel("From:", 0, y);
		aFromLTxt = GuiUtil.genLabel(EMPTY_LOC, 60, y);
		aFromX = GuiUtil.genButton("X", (int)(w*0.7f), y, (a,xx,yy) -> onClearLocation(true));
		GuiUtil.setEnabled(aFromX, false);

		aToDD = GuiUtil.genDropDown((o) -> onDropDown(o.toString(), false), x, y, SEL_CUR_LOC, SEL_FROM_LATLON, SEL_POS_ON_MAP, SEL_SEARCH_LOC);
		aToL = GuiUtil.genLabel("To:", 0, y);
		aToLTxt = GuiUtil.genLabel(EMPTY_LOC, 60, y);
		aToX = GuiUtil.genButton("X", (int)(w*0.7f), y, (a,xx,yy) -> onClearLocation(false));
		GuiUtil.setEnabled(aToX, false);
		aX = GuiUtil.genButton("X", (int)(w*0.9f), h/2, (a,xx,yy) -> setVisible(false, true));
		
		aTravelModeDD = GuiUtil.genDropDown((o) -> onDropDown("TM:" + o.toString(), true), x, y, true, Vehicle.Car.toString(), Vehicle.Bike.toString(), Vehicle.Foot.toString());
		String travelMode = Cfg.getValue(Cfg.NavKey.TravelMode, Cfg.TRAVEL_MODE_CAR);
		aTravelModeDD.setSelected(travelMode);
		
		al.addChild(aFromL, 0.0f, 0.1f, 0.6f, 0.3f);
		al.addChild(aToL, 0.0f, 0.6f, 0.6f, 0.3f);
		al.addChild(aFromLTxt, 0.1f, 0.1f, 0.6f, 0.3f);
		al.addChild(aToLTxt, 0.1f, 0.6f, 0.6f, 0.3f);
		al.addChild(aFromDD, 0.1f, 0.1f, 0.5f, 0.3f);
		al.addChild(aToDD, 0.1f, 0.6f, 0.5f, 0.3f);
		al.addChild(aFromX, 0.65f, 0.1f, 0.1f, 0.3f);
		al.addChild(aToX, 0.65f, 0.6f, 0.1f, 0.3f);
		al.addChild(aX, 0.9f, 0.0f, 0.1f, 0.3f);
		al.addChild(aTravelModeDD, 0.9f, 0.4f, 0.1f, 0.6f);
		aToLTxt.remove(); // Hide allows to reAddChild.
		aFromLTxt.remove(); // Hide allows to reAddChild.
	}
	
	public static NavSelect getInstance() { return instance; }
	
	void onDropDown( String selection, boolean from )
	{
		System.out.println("DropDown: '" + selection + "' from=" + from);

		if (selection.equals(SEL_POS_ON_MAP))
		{
			setVisible(false, false);
			ToastMsg.getInstance().toastShort("Touch on Map to choose your Location");
			if (from)
			{
				tabAction = TabAction.StartPoint;
			}
			else
			{
				tabAction = TabAction.EndPoint;
			}
			MapHandler.getInstance().setMapHandlerListener(this);
		}
		else if (selection.equals(SEL_SEARCH_LOC))
		{
			SearchPanel.getInstance().setVisible(true, from);
			setVisible(false, false);
		}
		else if (selection.equals(SEL_FROM_LATLON))
		{
			PmDialogs.showLatLonDialog((p) -> onEnterLocation(p,from, true));
		}
		else if (selection.equals(SEL_CUR_LOC))
		{
			if (NaviEngine.getNaviEngine().getCurrentLocation() == null)
			{
				ToastMsg.getInstance().toastShort("No current location found");
			}
			else
			{
				float lat = NaviEngine.getNaviEngine().getCurrentLocation().getLatitude();
				float lon = NaviEngine.getNaviEngine().getCurrentLocation().getLongitude();
				GeoPoint p = new GeoPoint(lat,lon);
				onEnterLocation(p, from, false);
			}
		}
		else if (selection.startsWith("TM:"))
		{
			if (selection.endsWith(Vehicle.Car.toString()))
			{
				Cfg.setValue(Cfg.NavKey.TravelMode, Cfg.TRAVEL_MODE_CAR);
			}
			else if (selection.endsWith(Vehicle.Bike.toString()))
			{
				Cfg.setValue(Cfg.NavKey.TravelMode, Cfg.TRAVEL_MODE_BIKE);
			}
			else if (selection.endsWith(Vehicle.Foot.toString()))
			{
				Cfg.setValue(Cfg.NavKey.TravelMode, Cfg.TRAVEL_MODE_FOOT);
			}
			Cfg.save(Cfg.ConfType.Navigation);
			GeoPoint s = MapHandler.getInstance().getStartEndPoint(true);
			GeoPoint e = MapHandler.getInstance().getStartEndPoint(false);
			if (s != null && e != null)
			{
				onEnterLocation(e, false, true);
			}
		}
	}
	

	
	private void onClearLocation(boolean isStart)
	{
		((TextButton)aX).setText("X");
		MapHandler.getInstance().setStartEndPoint(TopPanel.getInstance().getGdxMap(), null, isStart, false);
		if (isStart)
		{
			GuiUtil.setEnabled(aFromX, false);
			al.reAddChild(aFromDD);
			aFromLTxt.remove();
		}
		else
		{
			GuiUtil.setEnabled(aToX, false);
			al.reAddChild(aToDD);
			aToLTxt.remove();
		}
	}
	
	private void onEnterLocation(GeoPoint latLon, boolean isStart, boolean doCenter)
	{
		MapHandler.getInstance().setStartEndPoint(TopPanel.getInstance().getGdxMap(), Address.fromGeoPoint(latLon), isStart, true);
		if (doCenter) { MapHandler.getInstance().centerPointOnMap(latLon, 0, 0, 0); }
	}

	@Override public void onPressLocation(GeoPoint latLon)
	{
		if (tabAction == TabAction.StartPoint)
		{
			MapHandler.getInstance().setStartEndPoint(TopPanel.getInstance().getGdxMap(), Address.fromGeoPoint(latLon), true, true);
			setVisible(true, true);
		}
		else if (tabAction == TabAction.EndPoint)
		{
			MapHandler.getInstance().setStartEndPoint(TopPanel.getInstance().getGdxMap(), Address.fromGeoPoint(latLon), false, true);
			setVisible(true, true);
		}
		tabAction = TabAction.None;
	}
	
	public void setLocation(Address a, boolean isStart)
	{
		if (isStart)
		{
			((Label)aFromLTxt).setText(a.toNiceString());
			GuiUtil.setEnabled(aFromX, true);
			aFromDD.remove();
			al.reAddChild(aFromLTxt);
		}
		else
		{
			((Label)aToLTxt).setText(a.toNiceString());
			GuiUtil.setEnabled(aToX, true);
			aToDD.remove();
			al.reAddChild(aToLTxt);
		}
	}

	/** Sets this View visible or invisible.
	 * @param visible Whether this NavSelectPanel should be visible.
	 * @param withTopPanelSwitch Switch between this Panel and TopPanel, so hide TopPanel, when this Panel gets visible, show otherwise. */
	public void setVisible(boolean visible, boolean withTopPanelSwitch)
	{
		if (this.visible == visible) { return; }
		this.visible = visible;
		if (visible)
		{
			if (MapHandler.getInstance().getStartEndPoint(true) != null && MapHandler.getInstance().getStartEndPoint(false) != null)
			{
				((TextButton)aX).setText("[restart]");
			}
			else
			{
				((TextButton)aX).setText("X");
			}
			if (withTopPanelSwitch) { TopPanel.getInstance().setVisible(false); }
			GuiUtil.addActor(al);
		}
		else
		{
			if (((TextButton)aX).getText().toString().equals("[restart]"))
			{
				NaviEngine.getNaviEngine().setNavigating(true);
				al.remove();
				return;
			}
			if (withTopPanelSwitch) { TopPanel.getInstance().setVisible(true); }
			al.remove();
		}
	}
}
