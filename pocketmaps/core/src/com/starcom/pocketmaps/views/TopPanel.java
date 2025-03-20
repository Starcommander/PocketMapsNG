package com.starcom.pocketmaps.views;

import org.oscim.core.GeoPoint;
import org.oscim.map.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.ivkos.gpsd4j.messages.reports.TPVReport;
import com.starcom.LoggerUtil;
import com.starcom.gdx.ui.AbsLayout;
import com.starcom.gdx.ui.Dialogs;
import com.starcom.gdx.ui.ListSelect;
import com.starcom.gdx.ui.ToastMsg;
import com.starcom.gdx.ui.GuiUtil;
import com.starcom.interfaces.IProgressListener.Type;
import com.starcom.navigation.Location;
import com.starcom.pocketmaps.Cfg;
import com.starcom.pocketmaps.geocoding.Address;
import com.starcom.pocketmaps.map.MapHandler;
import com.starcom.pocketmaps.navigator.NaviEngine;
import com.starcom.pocketmaps.views.MapList.MapAction;

public class TopPanel
{
	static TopPanel instance = new TopPanel();
	private Actor centerButton = GuiUtil.genButton("[O]", 30, 90, (a,x,y) -> onCenterButtonPressed());
	private Map gdxMap;
	private AbsLayout al;
	private boolean visible = false;
	
	/** This is also the main panel that keeps the GdxMap. */
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
		Actor aPan = GuiUtil.genPanel(x, y, w, h);
		Actor aDD = GuiUtil.genDropDown((s) -> doMenuAction(s),30, y + 20, "Maps...", "Navigate...", "Settings...", "Tracking...");
		al = new AbsLayout(0, 0, 1, 0.1f);
		al.setMinHeight(30);
		al.addChild(aPan, 0, 0, 1, 1);
		al.addChild(aDD, 0.1f, 0.2f, 0.25f, 0.6f);
		MapHandler.getInstance().createAdditionalMapLayers(gdxMap);
	}
	
	private void onCenterButtonPressed()
	{
		Location curLoc = NaviEngine.getNaviEngine().getCurrentLocation();
		if (curLoc!=null)
		{
			log("Using latest gps location");
			MapHandler.getInstance().centerPointOnMap(new GeoPoint( curLoc.getLatitude(), curLoc.getLongitude()), NaviEngine.BEST_NAVI_ZOOM, 0, 0);
			return;
		}
		log("Polling gps location");
		NaviEngine.getNaviEngine().getGpsClient().sendPollCommand((posLst) ->
		{
			if (posLst==null) { log("Position is null"); return; }
			if (posLst.getTPVList().isEmpty()) { log("Position list is empty"); return; }
			TPVReport pos = posLst.getTPVList().get(0);
			if (pos.getLatitude() == Double.NaN || pos.getLongitude() == Double.NaN) { log("Position is NaN"); return; }
			MapHandler.getInstance().centerPointOnMap(new GeoPoint( pos.getLatitude(), pos.getLongitude()), NaviEngine.BEST_NAVI_ZOOM, 0, 0);
		});
	}
	
	public void setVisible(boolean visible)
	{
		if (this.visible == visible) { return; }
		if (visible)
		{
			GuiUtil.addActor(al);
			GuiUtil.addActor(centerButton);
		}
		else
		{
			al.remove();
			centerButton.remove();
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
		else if (action.equals("Settings..."))
		{
			SettingsView.getInstance().showSettings();
		}
		else if (action.equals("Download Maps"))
		{
			String mapdataVersion = Cfg.getMapdataVersion();
			if (mapdataVersion == null)
			{
				ToastMsg.getInstance().toastLong("Error getting necessary version for dl maplist");
				return;
			}
			String url = "http://vsrv15044.customer.xenway.de/maps/map_url-" + mapdataVersion + ".json";

			com.starcom.io.Web.downloadTextfileLater(url, (type,txt) ->
			{
				if (type == Type.ERROR)
				{
					ToastMsg.getInstance().toastLong(txt + " V=" + mapdataVersion);
				}
				else if (type == Type.SUCCESS)
				{
					MapList.viewMapsDownload(txt);}
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
		else if (action.equals("Tracking..."))
		{
			TrackingPanel.getInstance().setVisible(true, true);
		}
	}
	
	void log(String msg)
	{
		LoggerUtil.get(TopPanel.class).info(msg);
	}
}
