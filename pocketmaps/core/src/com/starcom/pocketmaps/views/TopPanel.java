package com.starcom.pocketmaps.views;

import org.oscim.core.GeoPoint;
import org.oscim.map.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.starcom.gdx.ui.Dialogs;
import com.starcom.gdx.ui.ListSelect;
import com.starcom.gdx.ui.ToastMsg;
import com.starcom.gdx.ui.GuiUtil;
import com.starcom.interfaces.IProgressListener.Type;
import com.starcom.pocketmaps.map.MapHandler;
import com.starcom.pocketmaps.util.PolyParser;
import com.starcom.pocketmaps.views.MapList.MapAction;

public class TopPanel
{
	static TopPanel instance = new TopPanel();
	private Map gdxMap;
	
	public static TopPanel getInstance() { return instance; }
	
	/** Returns the Map that is used as Canvas. */
	public Map getGdxMap() { return gdxMap; }

	public void show(Map gdxMap)
	{
		this.gdxMap = gdxMap;
		int w = Gdx.graphics.getWidth();
		int h = Gdx.graphics.getHeight()/8;
		int x = 0;
		int y = Gdx.graphics.getHeight() - h;
		Actor pp = GuiUtil.genPanel(x, y, w, h);
		Actor dd = GuiUtil.genDropDown((o) -> doMenuAction(o.toString()),30, y + 30, "AAA", "Maps...", "CCC", "DoNavigate");
		GuiUtil.getStage().addActor(dd);
		GuiUtil.getStage().addActor(pp);
		MapHandler.getInstance().createAdditionalMapLayers(gdxMap);
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
		else if (action.equals("DoNavigate"))
		{
//			PolyParser.addDebugLine(MapList.getInstance().getFocusMap());
////			MapHandler.getInstance().calcPath(MapList.getInstance().getFocusMap(), 48.271555f, 14.574171f, 48.247748, 14.627872);
			MapHandler.getInstance().setStartEndPoint(TopPanel.getInstance().getGdxMap(), new GeoPoint(48.271555f, 14.574171f), true, false);
			MapHandler.getInstance().setStartEndPoint(TopPanel.getInstance().getGdxMap(), new GeoPoint(48.247748, 14.627872), false,true);
////			MapHandler.getInstance().setCustomPoint(MapList.getInstance().getFocusMap(), new GeoPoint(48.271555f, 14.574171f));
		}
	}
}
