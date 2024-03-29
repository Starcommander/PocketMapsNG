package com.starcom.pocketmaps.views;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;
import java.util.logging.Logger;

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
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.graphhopper.GHResponse;
import com.graphhopper.PathWrapper;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.starcom.LoggerUtil;
import com.starcom.gdx.system.Threading;
import com.starcom.gdx.ui.ListSelect;
import com.starcom.gdx.ui.ToastMsg;
import com.starcom.gdx.ui.Util;
import com.starcom.pocketmaps.tasks.Download;
import com.starcom.pocketmaps.Cfg;
import com.starcom.pocketmaps.Cfg.ConfType;
import com.starcom.pocketmaps.Cfg.NavKey;
import com.starcom.pocketmaps.map.MapHandler;
import com.starcom.pocketmaps.map.MapLayer;

public class MapList
{
	static Logger logger = LoggerUtil.get(MapList.class);
    private final static double SCALE_DEF = 1 << 12;
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
	
	public boolean hasFocusMap() { return mapLayers.size() > 0; }
	
	/** Should only be called on init. */
	public void loadSettings(Map map)
	{
		String sel = Cfg.getValue(NavKey.MapSelection, null);
		System.out.println("--> We got settings: " + sel);
		if (sel == null) { return; }
		boolean focusDone = false;
		for (FileHandle f : Download.getMapsPath(null).list())
		{
			if (f.isDirectory())
			{
				if (sel.contains(f.name()))
				{
					File mapFile = new File(Gdx.files.getExternalStoragePath(), f.child(f.name() + ".map").path());
					GeoPoint mapCenter = loadMap(mapFile.getPath(), map);
					if (!focusDone)
					{
						map.setMapPosition(mapCenter.getLatitude(), mapCenter.getLongitude(), SCALE_DEF);
					}
				}
			}
		}
	}
    
    public GeoPoint loadMap(String mapFile, Map map)
    {
		logger.info("Loading map: " + mapFile);
    	MapLayer ml = new MapLayer(mapFile, map);
    	ml.initAndAttach();
    	mapLayers.add(ml);
        return ml.getCenter();
    }
    
    public void unloadMap(MapLayer ml)
    {
		logger.info("Discarding map: " + ml.getMapFile());
		mapLayers.remove(ml);
		ml.dispose();
    }
    
    public void unloadMaps()
    {
    	while (mapLayers.size() > 0) { unloadMap(mapLayers.get(0)); }
    }
    
    /** A view that shows the list for select or unselect maps. */
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
				ll.addCheckboxElement(f.name(), isVisible, (a,x,y) ->
				{
					CheckBox box = (CheckBox)a;
					File mapFile = new File(Gdx.files.getExternalStoragePath(), f.child(f.name() + ".map").path());
					if (box.isChecked())
					{
						GeoPoint mapCenter = loadMap(mapFile.getPath(), map);
						map.setMapPosition(mapCenter.getLatitude(), mapCenter.getLongitude(), SCALE_DEF);
						updateCfg(mapFile.getName(), true);
					}
					else
					{
						for (MapLayer ml : mapLayers)
						{
							if (ml.getMapFile().equals(mapFile.getPath()))
							{
								unloadMap(ml);
								updateCfg(mapFile.getName(), false);
								break;
							}
						}
					}
				});
			}
		}
		ll.showAsWindow(guiStage);
	}
	
	private void updateCfg(String map, boolean selected)
	{
		String sel = Cfg.getValue(NavKey.MapSelection, "");
		if (sel.isEmpty()) { sel = map; }
		else if (selected) { sel = sel + "," + map; }
		else
		{ // Unselect
			String list[] = sel.split(",");
			StringBuffer sb = new StringBuffer();
			String sep = "";
			for (String entry : list)
			{
				if (entry.isEmpty()) {}
				else if (entry.equals(map)) {}
				else
				{
					sb.append(sep).append(entry);
					sep = ",";
				}
			}
			sel = sb.toString();
		}
		logger.info("Cfg storing vis map entries: '" + sel + "'");
		Cfg.setValue(NavKey.MapSelection, sel);
		Cfg.save(ConfType.Navigation);
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
			
			ll.addElement(table, (a, x, y) -> Download.downloadMapNow(guiStage, mdate, mname));
		}
		ll.showAsWindow(guiStage);
	}
	
	public static void viewDirectionList(InstructionList instL)
	{
		ListSelect ll = new ListSelect("Directions");
		for (Instruction inst : instL)
		{
			ll.addElement(inst.getName(), (a,x,y) -> {});
		}
		ll.showAsWindow(TopPanel.getInstance().getGuiStage());
	}
	
}
