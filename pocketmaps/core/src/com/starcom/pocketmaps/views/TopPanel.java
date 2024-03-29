package com.starcom.pocketmaps.views;

import org.oscim.core.GeoPoint;
import org.oscim.map.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.starcom.gdx.ui.Dialogs;
import com.starcom.gdx.ui.ToastMsg;
import com.starcom.interfaces.IProgressListener.Type;
import com.starcom.pocketmaps.map.MapHandler;
import com.starcom.pocketmaps.util.PolyParser;

public class TopPanel
{
	static TopPanel instance = new TopPanel();
	private Stage guiStage;
	
	public static TopPanel getInstance() { return instance; }
	
	public Stage getGuiStage() { return guiStage; }

	public void show(Stage guiStage, Map map)
	{
		this.guiStage = guiStage;
		int w = Gdx.graphics.getWidth();
		int h = Gdx.graphics.getHeight()/8;
		int x = 0;
		int y = Gdx.graphics.getHeight() - h;
		Dialogs.showPanel(guiStage,x,y,w,h);
		Dialogs.showDropDown(guiStage, (o) -> doMenuAction(guiStage, o.toString(), map),30, y + 30, "AAA", "Download Maps", "Show/Hide Maps", "CCC", "DoNavigate");
	}
	
	private static void doMenuAction(Stage guiStage, String action, Map map)
	{
		System.out.println("Menu: " + action);
		if (action.equals("Download Maps"))
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
					MapList.viewMapsDownload(guiStage, o.toString());}
				}
			);
		}
		else if (action.equals("Show/Hide Maps"))
		{
			MapList.getInstance().viewMapsSelect(guiStage, map);
		}
		else if (action.equals("DoNavigate"))
		{
			MapHandler.getInstance().createAdditionalMapLayers(MapList.getInstance().getFocusMap()); // TODO: Not here.
//			PolyParser.addDebugLine(MapList.getInstance().getFocusMap());
////			MapHandler.getInstance().calcPath(MapList.getInstance().getFocusMap(), 48.271555f, 14.574171f, 48.247748, 14.627872);
			MapHandler.getInstance().setStartEndPoint(MapList.getInstance().getFocusMap(), new GeoPoint(48.271555f, 14.574171f), true, false);
			MapHandler.getInstance().setStartEndPoint(MapList.getInstance().getFocusMap(), new GeoPoint(48.247748, 14.627872), false,true);
////			MapHandler.getInstance().setCustomPoint(MapList.getInstance().getFocusMap(), new GeoPoint(48.271555f, 14.574171f));
		}
	}
}
