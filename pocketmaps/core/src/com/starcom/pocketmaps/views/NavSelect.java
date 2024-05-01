package com.starcom.pocketmaps.views;

import org.oscim.core.GeoPoint;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.starcom.gdx.ui.GuiUtil;
import com.starcom.gdx.ui.ToastMsg;
import com.starcom.pocketmaps.geocoding.Address;
import com.starcom.pocketmaps.map.MapHandler;
import com.starcom.pocketmaps.map.MapHandlerListener;

public class NavSelect implements MapHandlerListener
{
	private static NavSelect instance = new NavSelect();
	static final String SEL_FROM_LATLON = "From Lat/Lon";
	static final String SEL_POS_ON_MAP = "Select from map";
	static final String SEL_SEARCH_LOC = "Search location";
	static final String SEL_CUR_LOC = "Current location";
	
	enum TabAction{ StartPoint, EndPoint, AddFavourit, None };
	private TabAction tabAction = TabAction.None;

	private Actor aPan, aFromDD, aToDD, aFromL, aToL, aFromLTxt, aToLTxt, aX;
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
		aFromLTxt = GuiUtil.genLabel("...", 60, y);
		y = h/4;
		aToDD = GuiUtil.genDropDown((o) -> onDropDown(o.toString(), false), x, y, SEL_CUR_LOC, SEL_FROM_LATLON, SEL_POS_ON_MAP, SEL_SEARCH_LOC);
		aToL = GuiUtil.genLabel("To:", 0, y);
		aToLTxt = GuiUtil.genLabel("...", 60, y);
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
		}
		else
		{
			((Label)aToLTxt).setText(a.toNiceString());
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
			GuiUtil.addActor(aFromDD);
			GuiUtil.addActor(aToDD);
			GuiUtil.addActor(aFromL);
			GuiUtil.addActor(aToL);
			GuiUtil.addActor(aFromLTxt);
			GuiUtil.addActor(aToLTxt);
			GuiUtil.addActor(aX);
		}
		else
		{
			if (withTopPanelSwitch) { TopPanel.getInstance().setVisible(true); }
			aPan.remove();
			aFromDD.remove();
			aToDD.remove();
			aFromL.remove();
			aToL.remove();
			aFromLTxt.remove();
			aToLTxt.remove();
			aX.remove();
		}
		this.visible = visible;
	}
}
