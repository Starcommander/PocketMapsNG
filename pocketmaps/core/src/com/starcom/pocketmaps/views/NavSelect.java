package com.starcom.pocketmaps.views;

import org.oscim.core.GeoPoint;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.starcom.gdx.ui.GuiUtil;
import com.starcom.gdx.ui.ToastMsg;
import com.starcom.pocketmaps.geocoding.Address;
import com.starcom.pocketmaps.map.MapHandler;
import com.starcom.pocketmaps.map.MapHandlerListener;

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
	private boolean visible;
	
	private NavSelect()
	{
		int w = Gdx.graphics.getWidth();
		int h = Gdx.graphics.getHeight()/8;
		int x = 0;
		int y = 0;
		aPan = GuiUtil.genPanel(x,y,w,h);
		x = w/2;
		y = h/2;
		aFromDD = GuiUtil.genDropDown((o) -> onDropDown(o.toString(), true), x, y, SEL_CUR_LOC, SEL_FROM_LATLON, SEL_POS_ON_MAP, SEL_SEARCH_LOC);
		aFromL = GuiUtil.genLabel("From:", 0, y);
		aFromLTxt = GuiUtil.genLabel(EMPTY_LOC, 60, y);
		aFromX = GuiUtil.genButton("X", (int)(w*0.7f), y, (a,xx,yy) -> onClearLocation(true));
		GuiUtil.setEnabled(aFromX, false);
		y = h/4;
		aToDD = GuiUtil.genDropDown((o) -> onDropDown(o.toString(), false), x, y, SEL_CUR_LOC, SEL_FROM_LATLON, SEL_POS_ON_MAP, SEL_SEARCH_LOC);
		aToL = GuiUtil.genLabel("To:", 0, y);
		aToLTxt = GuiUtil.genLabel(EMPTY_LOC, 60, y);
		aToX = GuiUtil.genButton("X", (int)(w*0.7f), y, (a,xx,yy) -> onClearLocation(false));
		GuiUtil.setEnabled(aToX, false);
		aX = GuiUtil.genButton("X", (int)(w*0.9f), h/2, (a,xx,yy) -> setVisible(false, true));
	}
	
	public static NavSelect getInstance() { return instance; }
	
	void onDropDown( String selection, boolean from )
	{
		System.out.println("DropDown: '" + selection + "' from=" + from);

		if (selection.equals(SEL_POS_ON_MAP))
		{
			setVisible(false, false);
			ToastMsg.getInstance().toastShort("Touch on Map to choose your start Location");
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
			PmDialogs.showLatLonDialog((p) -> onEnterLocation(p,from));
		}
	}
	

	
	private void onClearLocation(boolean isStart)
	{
		MapHandler.getInstance().setStartEndPoint(TopPanel.getInstance().getGdxMap(), null, isStart, false);
		if (isStart)
		{
			Label l = (Label)aFromLTxt;
			l.setText(EMPTY_LOC);
			GuiUtil.setEnabled(aFromX, false);
		}
		else
		{
			Label l = (Label)aToLTxt;
			l.setText(EMPTY_LOC);
			GuiUtil.setEnabled(aToX, false);
		}
	}
	
	private void onEnterLocation(GeoPoint latLon, boolean isStart)
	{
		MapHandler.getInstance().setStartEndPoint(TopPanel.getInstance().getGdxMap(), Address.fromGeoPoint(latLon), isStart, true);
		MapHandler.getInstance().centerPointOnMap(latLon, 0, 0, 0);
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
		}
		else
		{
			((Label)aToLTxt).setText(a.toNiceString());
			GuiUtil.setEnabled(aToX, true);
		}
	}

	/** Sets this View visible or invisible.
	 * @param visible Whether this NavSelectPanel should be visible.
	 * @param withTopPanelSwitch Switch between this Panel and TopPanel, so hide TopPanel, when this Panel gets visible, show otherwise. */
	public void setVisible(boolean visible, boolean withTopPanelSwitch)
	{
		if (this.visible == visible) { return; }
		if (visible)
		{
			if (withTopPanelSwitch) { TopPanel.getInstance().setVisible(false); }
			GuiUtil.addActor(aPan);
			GuiUtil.addActor(aFromLTxt);
			GuiUtil.addActor(aFromDD);
			GuiUtil.addActor(aFromX);
			GuiUtil.addActor(aFromL);
			GuiUtil.addActor(aToLTxt);
			GuiUtil.addActor(aToDD);
			GuiUtil.addActor(aToX);
			GuiUtil.addActor(aToL);
			GuiUtil.addActor(aX);
		}
		else
		{
			if (withTopPanelSwitch) { TopPanel.getInstance().setVisible(true); }
			aPan.remove();
			aFromLTxt.remove();
			aFromDD.remove();
			aFromX.remove();
			aFromL.remove();
			aToLTxt.remove();
			aToDD.remove();
			aToX.remove();
			aToL.remove();
			aX.remove();
		}
		this.visible = visible;
	}
}
