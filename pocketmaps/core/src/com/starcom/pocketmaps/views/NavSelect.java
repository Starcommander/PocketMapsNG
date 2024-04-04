package com.starcom.pocketmaps.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.starcom.gdx.ui.GuiUtil;

public class NavSelect
{
	static final String SEL_FROM_LATLON = "From Lat/Lon";
	static final String SEL_POS_ON_MAP = "Select from map";
	static final String SEL_SEARCH_LOC = "Search location";

	Actor aPan, aFromDD, aToDD;
	
	public NavSelect()
	{
		int w = Gdx.graphics.getWidth();
		int h = Gdx.graphics.getHeight()/8;
		int x = 0;
		int y = 0;
		aPan = GuiUtil.genPanel(x,y,w,h);
		x = w/4;
		y = h/4;
		aFromDD = GuiUtil.genDropDown((o) -> onDropDown(o.toString()), x, y, SEL_FROM_LATLON, SEL_POS_ON_MAP, SEL_SEARCH_LOC);
		aToDD = GuiUtil.genDropDown((o) -> onDropDown(o.toString()), x, y, SEL_FROM_LATLON, SEL_POS_ON_MAP, SEL_SEARCH_LOC);
	}
	
	static void onDropDown( String selection )
	{
		
	}
}
