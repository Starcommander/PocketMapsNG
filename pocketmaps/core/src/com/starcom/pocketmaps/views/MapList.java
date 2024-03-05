package com.starcom.pocketmaps.views;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

import org.json.JSONException;
import org.oscim.core.GeoPoint;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.map.Map;
import org.oscim.theme.VtmThemes;
import org.oscim.tiling.source.mapfile.MapFileTileSource;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.graphhopper.GHResponse;
import com.graphhopper.PathWrapper;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.starcom.gdx.system.Threading;
import com.starcom.gdx.ui.ListSelect;
import com.starcom.gdx.ui.ToastMsg;
import com.starcom.gdx.ui.Util;
import com.starcom.pocketmaps.tasks.Download;
import com.starcom.pocketmaps.map.MapHandler;
import com.starcom.pocketmaps.map.MapLayer;

public class MapList
{
	private static MapList instance = new MapList();
	
//	Vector<MapLayer> mapLayers = new Vector<MapLayer>();
	ArrayList<MapLayer> mapLayers = new ArrayList<MapLayer>(); //TODO: Using Vector instead?

	public static MapList getInstance()
	{
		return instance;
	}
	
	/** Throws an Exception when no map is loaded. */
	public Map getFocusMap()
	{
		return mapLayers.get(0).getMap();
	}
    
    public GeoPoint loadMap(String mapFile, Map map)
    {
    	System.out.println("Click f=" + mapFile);
    	MapLayer ml = new MapLayer(mapFile, map);
    	MapHandler.getInstance().onMapLoaded(map);
    	mapLayers.add(ml);
        return ml.getCenter();
    }
    
	public void viewMapsSelect(Stage guiStage, Map map)
	{
		if (!Threading.getInstance().isMainThread())
		{
			Threading.getInstance().invokeOnMainThread(() -> viewMapsSelect(guiStage, map));
			return;
		}
		ListSelect ll = new ListSelect("SelectMaps");
		for (FileHandle f : Download.getMapsPath(null).list())
		{
			if (f.isDirectory())
			{
				boolean isVisible = false;
				for (MapLayer m : mapLayers)
				{
					if (m.getMapFile().endsWith(f.name() + ".map"))
					{
						isVisible = true;
						break;
					}
				}
				ll.addCheckboxElement(f.name(), isVisible, (x,y) ->
				{
					File mapFile = new File(Gdx.files.getExternalStoragePath(), f.child(f.name() + ".map").path());
					loadMap(mapFile.getPath(), map);
				});
			}
		}
		ll.showAsWindow(guiStage);
	}

	public static void viewMapsDownload(Stage guiStage, String json)
	{
		int w = Gdx.graphics.getWidth();
		if (!Threading.getInstance().isMainThread())
		{
			Threading.getInstance().invokeOnMainThread(() -> viewMapsDownload(guiStage, json));
			return;
		}
		org.json.JSONObject jsonObj;
		try
		{
			jsonObj = new org.json.JSONObject(json);
		}
		catch (JSONException je)
		{
			ToastMsg.getInstance().toastLong("Json error, maybe network issue.");
			je.printStackTrace();
			return;
		}
		ListSelect ll = new ListSelect("DownloadMaps");
		org.json.JSONArray arr = jsonObj.getJSONArray("maps-0.13.0_0");
		//Image mapImage = new Image(new Texture("icon_pocketmaps.png"));
		Texture mapTextureTop = new Texture("icon_pocketmaps_top.png");
		Texture mapTextureBot = new Texture("icon_pocketmaps_bot.png");
		for (int i = 0; i < arr.length(); i++)
		{
			Object o = arr.getJSONObject(i);
			org.json.JSONObject jo = (org.json.JSONObject)o;
			String mname = jo.getString("name");
			String mcont = mname.split("_")[0];
			String msize = jo.getString("size");
			String mdate = jo.getString("time");
			
			Table table = new Table();
			table.add(new Image(mapTextureTop)).left();
			table.add(new Label(mname.substring(mcont.length()+1), Util.getDefaultSkin())).width(w/3);
			table.add(new Label(mdate, Util.getDefaultSkin())).width(w/3);
			table.row();
			table.add(new Image(mapTextureBot)).left();
			table.add(new Label("Map size: " + msize, Util.getDefaultSkin())).width(w/3);
			table.add(new Label(mcont, Util.getDefaultSkin())).width(w/3);
			
			ll.addElement(table, (x, y) -> Download.downloadMapNow(guiStage, mdate, mname));
		}
		ll.showAsWindow(guiStage);
	}
	
	public static void viewDirectionList(InstructionList instL)
	{
		ListSelect ll = new ListSelect("Directions");
		for (Instruction inst : instL)
		{
			ll.addElement(inst.getName(), (x,y) -> {});
		}
//		ll.showAsWindow(guiStage); //TODO: Howto get guiStage
	}
	
}
