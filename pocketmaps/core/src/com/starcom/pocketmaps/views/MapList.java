package com.starcom.pocketmaps.views;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import org.json.JSONException;
import org.oscim.core.BoundingBox;
import org.oscim.core.GeoPoint;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.starcom.LoggerUtil;
import com.starcom.system.Threading;
import com.starcom.gdx.ui.ListSelect;
import com.starcom.gdx.ui.ToastMsg;
import com.starcom.navigation.MapRoutingEngine.Instruct;
import com.starcom.gdx.ui.GuiUtil;
import com.starcom.pocketmaps.tasks.Download;
import com.starcom.pocketmaps.Cfg;
import com.starcom.pocketmaps.Cfg.ConfType;
import com.starcom.pocketmaps.Cfg.NavKey;
import com.starcom.pocketmaps.map.MapLayer;
import com.starcom.pocketmaps.map.MapLayer.MapFileType;
import com.starcom.pocketmaps.navigator.NaviEngine;

public class MapList
{
	public enum MapAction {ShowHide, Delete}
	
	static Logger logger = LoggerUtil.get(MapList.class);
    private final static double SCALE_DEF = 1 << 12;
	private static MapList instance = new MapList();
	
//	Vector<MapLayer> mapLayers = new Vector<MapLayer>();
	ArrayList<MapLayer> mapLayers = new ArrayList<MapLayer>(); //TODO: Using Vector instead?

	public static MapList getInstance()
	{
		return instance;
	}
	
	public MapLayer findMapLayerFromLocation(GeoPoint pos)
	{
		MapLayer matchingMap = null;
		for (MapLayer l : mapLayers)
		{
			BoundingBox bbox = l.getBoundingBox();
			if (bbox.contains(pos))
			{
				if (matchingMap != null)
				{
					if (checkLocationInside(pos, matchingMap)) { return matchingMap; }
					else { matchingMap = l; }
				}
				return l;
			}
		}
		return null;
	}
	
	public MapLayer findMapLayerFromCountry(String countryName, String contName)
	{
		for (MapLayer l : mapLayers)
		{
			String cName = l.getMapFile(MapFileType.Country);
			String curContName = l.getMapFile(MapFileType.Continent);
			if (cName.equals(countryName) && curContName.equals(contName)) { return l; }
		}
		return null;
	}
	
	private boolean checkLocationInside(GeoPoint pos, MapLayer matchingMap)
	{
		//TODO: Fine tuning: Check complete path if inside.
		return true;
	}
	
	/** Should only be called on init. */
	public void loadSettings()
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
					GeoPoint mapCenter = loadMap(mapFile.getPath());
					if (!focusDone)
					{
						TopPanel.getInstance().getGdxMap().setMapPosition(mapCenter.getLatitude(), mapCenter.getLongitude(), SCALE_DEF);
					}
				}
			}
		}
	}
    
    public GeoPoint loadMap(String mapFile)
    {
		logger.info("Loading map: " + mapFile);
    	MapLayer ml = new MapLayer(mapFile);
    	ml.initAndAttach();
    	mapLayers.add(ml);
        return ml.getCenter();
    }
    
    public void unloadMap(MapLayer ml)
    {
		logger.info("Discarding map: " + ml.getMapFile(MapFileType.FullPath));
		mapLayers.remove(ml);
		ml.dispose();
    }
    
    public void unloadMaps()
    {
    	while (mapLayers.size() > 0) { unloadMap(mapLayers.get(0)); }
    }
    
    /** A view that shows the list for select or unselect maps. */
	public void viewMapsSelect(MapAction atype)
	{
		if (!Threading.getInstance().isMainThread())
		{
			Threading.getInstance().invokeOnMainThread(() -> viewMapsSelect(atype));
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
					if (m.getMapFile(MapFileType.FullPath).endsWith(f.name() + ".map"))
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
					{ //Show map.
						if (atype != MapAction.Delete)
						{
							GeoPoint mapCenter = loadMap(mapFile.getPath());
							TopPanel.getInstance().getGdxMap().setMapPosition(mapCenter.getLatitude(), mapCenter.getLongitude(), SCALE_DEF);
							updateCfg(mapFile.getName(), true);
						}
					}
					else
					{ //Hide and clear map.
						for (MapLayer ml : mapLayers)
						{
							if (ml.getMapFile(MapFileType.FullPath).equals(mapFile.getPath()))
							{
								unloadMap(ml);
								updateCfg(mapFile.getName(), false);
								break;
							}
						}
					}
					if (atype == MapAction.Delete)
					{ //Clear map directory.
						f.deleteDirectory();
						ToastMsg.getInstance().toastShort("Deleted: " + f.name());
					}
				});
			}
		}
		ll.showAsWindow(GuiUtil.getStage());
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

	/** Shows the dialog with maps that are available.
	 * @param json The maps available on server as json content. */
	public static void viewMapsDownload(String json)
	{
		int w = Gdx.graphics.getWidth();
		if (!Threading.getInstance().isMainThread())
		{
			Threading.getInstance().invokeOnMainThread(() -> viewMapsDownload(json));
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
		ll.showFilter(true);
		String mapdataVersion = Cfg.getMapdataVersion();
		if (mapdataVersion == null)
		{
			ToastMsg.getInstance().toastLong("Error getting necessary version for maplist");
			return;
		}
		org.json.JSONArray arr;
		try
		{
			arr = jsonObj.getJSONArray("maps-" + mapdataVersion);
		}
		catch (JSONException jsonEx)
		{
			ToastMsg.getInstance().toastLong("Missing compatible maps for: " + mapdataVersion);
			return;
		}
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
			table.add(new Label(mname.substring(mcont.length()+1), GuiUtil.getDefaultSkin())).width(w/3);
			table.add(new Label(mdate, GuiUtil.getDefaultSkin())).width(w/3);
			table.row();
			table.add(new Image(mapTextureBot)).left();
			table.add(new Label("Map size: " + msize, GuiUtil.getDefaultSkin())).width(w/3);
			table.add(new Label(mcont, GuiUtil.getDefaultSkin())).width(w/3);
			table.setUserObject(jo.getString("name"));
			
			ll.addElement(table, (a, x, y) -> Download.downloadMapNow(GuiUtil.getStage(), mdate, mname));
		}
		ll.showAsWindow(GuiUtil.getStage());
	}
	
	public static void viewDirectionList(ArrayList<Instruct> instL)
	{
		NavSelect.getInstance().setVisible(false,false);
		ListSelect ll = new ListSelect("Directions", "Navigate", (b) -> NaviEngine.getNaviEngine().setNavigating(true));
		for (Instruct inst : instL)
		{
			ll.addElement(inst.name, (a,x,y) -> {});
		}
		ll.showAsWindow(GuiUtil.getStage());
	}
	
}
