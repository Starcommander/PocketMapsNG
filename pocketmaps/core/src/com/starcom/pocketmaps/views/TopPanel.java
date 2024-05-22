package com.starcom.pocketmaps.views;

import org.oscim.core.GeoPoint;
import org.oscim.map.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.starcom.gdx.ui.Dialogs;
import com.starcom.gdx.ui.ListSelect;
import com.starcom.gdx.ui.ToastMsg;
import com.starcom.gdx.ui.GuiUtil;
import com.starcom.interfaces.IProgressListener.Type;
import com.starcom.pocketmaps.geocoding.Address;
import com.starcom.pocketmaps.map.MapHandler;
import com.starcom.pocketmaps.views.MapList.MapAction;

public class TopPanel
{
	static TopPanel instance = new TopPanel();
	private Map gdxMap;
	private Actor aPan,aDD;
	private boolean visible = false;
	
	public static TopPanel getInstance() { return instance; }
	
	/** Returns the Map that is used as Canvas. */
	public Map getGdxMap() { return gdxMap; }
	
	private TopPanel() {}

	public void init(Map gdxMap)
	{
		this.gdxMap = gdxMap;
		int w = Gdx.graphics.getWidth();
		int h = Gdx.graphics.getHeight()/8;
		int x = 0;
		int y = Gdx.graphics.getHeight() - h;
		aPan = GuiUtil.genPanel(x, y, w, h);
		aDD = GuiUtil.genDropDown((s) -> doMenuAction(s),30, y + 20, "AAA", "Maps...", "Navigate...", "SimpleDialog", "DebugFastNav");
		aDD.setSize(300, 60);
		MapHandler.getInstance().createAdditionalMapLayers(gdxMap);
	}
	
	public void setVisible(boolean visible)
	{
		if (this.visible == visible) { return; }
		if (visible)
		{
			GuiUtil.addActor(aPan);
			GuiUtil.addActor(aDD);
		}
		else
		{
			aPan.remove();
			aDD.remove();
		}
		this.visible = visible;
	}
	
	private static void doMenuAction(String action)
	{
		System.out.println("Menu: " + action);
		if (action.equals("Maps..."))
		{
			ListSelect ll = new ListSelect("Maps");
			ll.addElement("Download Maps", (a,x,y) -> doMenuAction("Download Maps"));
			ll.addElement("Show/Hide Maps", (a,x,y) -> doMenuAction("Show/Hide Maps"));
			ll.addElement("Delete Maps", (a,x,y) -> doMenuAction("Delete Maps"));
			ll.showAsWindow(GuiUtil.getStage());
		}
		else if (action.equals("Navigate..."))
		{
			NavSelect.getInstance().setVisible(true, true);
		}
		else if (action.equals("Download Maps"))
		{
			String url = "http://vsrv15044.customer.xenway.de/maps/map_url-0.13.0_0.json";

			com.starcom.io.Web.downloadTextfileLater(url, (t,o) ->
			{
				if (t == Type.ERROR)
				{
					ToastMsg.getInstance().toastLong(o.toString());
				}
				else if (t == Type.SUCCESS)
				{
					MapList.viewMapsDownload(o.toString());}
				}
			);
		}
		else if (action.equals("Show/Hide Maps"))
		{
			MapList.getInstance().viewMapsSelect(MapAction.ShowHide);
		}
		else if (action.equals("Delete Maps"))
		{
			MapList.getInstance().viewMapsSelect(MapAction.Delete);
		}
		else if (action.equals("SimpleDialog"))
		{
			Dialogs.showDialog(GuiUtil.getStage(), "Title", "msg", false, (o) -> System.out.println("Pressed " + o));
		}
		else if (action.equals("DebugFastNav"))
		{
			MapHandler.getInstance().setStartEndPoint(getInstance().getGdxMap(), Address.fromGeoPoint(new GeoPoint(47.730f,13.417f)), true, false);
			MapHandler.getInstance().setStartEndPoint(getInstance().getGdxMap(), Address.fromGeoPoint(new GeoPoint(47.734f,13.424f)), false, true);
			getInstance().setVisible(false);
		}
	}
}
