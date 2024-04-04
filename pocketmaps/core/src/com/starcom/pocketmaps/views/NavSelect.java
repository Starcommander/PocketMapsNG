package com.starcom.pocketmaps.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.starcom.gdx.ui.GuiUtil;

public class NavSelect
{
	static NavSelect instance = new NavSelect();
	static final String SEL_FROM_LATLON = "From Lat/Lon";
	static final String SEL_POS_ON_MAP = "Select from map";
	static final String SEL_SEARCH_LOC = "Search location";
	static final String SEL_CUR_LOC = "Current location";

	Actor aPan, aFromDD, aToDD, aFromL, aToL, aX;
	boolean visible;
	
	private NavSelect()
	{
		int w = Gdx.graphics.getWidth();
		int h = Gdx.graphics.getHeight()/8;
		int x = 0;
		int y = 0;
		aPan = GuiUtil.genPanel(x,y,w,h);
		x = w/4;
		y = h/2;
		aFromDD = GuiUtil.genDropDown((o) -> onDropDown(o.toString(), true), x, y, SEL_CUR_LOC, SEL_FROM_LATLON, SEL_POS_ON_MAP, SEL_SEARCH_LOC);
		aFromL = GuiUtil.genLabel("From:", 0, y);
		y = h/4;
		aToDD = GuiUtil.genDropDown((o) -> onDropDown(o.toString(), false), x, y, SEL_CUR_LOC, SEL_FROM_LATLON, SEL_POS_ON_MAP, SEL_SEARCH_LOC);
		aToL = GuiUtil.genLabel("To:", 0, y);
		aX = GuiUtil.genButton("X", (int)(w*0.9f), h/2, (a,xx,yy) -> setVisible(false));
	}
	
	public static NavSelect getInstance() { return instance; }
	
	static void onDropDown( String selection, boolean from )
	{
		System.out.println("DropDown: '" + selection + "' from=" + from);
	}
	
	public void setVisible(boolean visible)
	{
		if (this.visible == visible) { return; }
		if (visible)
		{
			TopPanel.getInstance().setVisible(false);
			GuiUtil.addActor(aPan);
			GuiUtil.addActor(aFromDD);
			GuiUtil.addActor(aToDD);
			GuiUtil.addActor(aFromL);
			GuiUtil.addActor(aToL);
			GuiUtil.addActor(aX);
		}
		else
		{
			TopPanel.getInstance().setVisible(true);
			aPan.remove();
			aFromDD.remove();
			aToDD.remove();
			aFromL.remove();
			aToL.remove();
			aX.remove();
		}
		this.visible = visible;
	}
}
