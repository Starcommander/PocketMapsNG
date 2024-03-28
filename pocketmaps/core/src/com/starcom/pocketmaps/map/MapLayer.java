package com.starcom.pocketmaps.map;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.oscim.core.GeoPoint;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.map.Map;
import org.oscim.theme.VtmThemes;
import org.oscim.tiling.source.mapfile.MapFileTileSource;

import com.graphhopper.GraphHopper;
import com.starcom.LoggerUtil;
import com.starcom.pocketmaps.map.MapHandler.MapEventsReceiver;

public class MapLayer
{
	static Logger logger = LoggerUtil.get(MapLayer.class);
	private BuildingLayer buildingLayer;
	private LabelLayer labelLayer;
	private VectorTileLayer vtLayer;
	MapFileTileSource tileSource;
	private String mapFile;
	private GraphHopper graphHopper;
	private Map map;
	private MapEventsReceiver mer;
	
	public MapLayer(String mapFile, Map map)
	{
		this.mapFile = mapFile;
		this.map = map;
	}
	
	public void initAndAttach()
	{
		tileSource = new MapFileTileSource();
        tileSource.setMapFile(new java.io.File(mapFile).getAbsolutePath());
        vtLayer = map.setBaseMap(tileSource);
        map.setTheme(VtmThemes.DEFAULT);
        buildingLayer = new BuildingLayer(map, vtLayer);
        labelLayer = new LabelLayer(map, vtLayer);
        map.layers().add(buildingLayer);
        map.layers().add(labelLayer);
        mer = MapHandler.getInstance().new MapEventsReceiver(map);
    	map.layers().add(mer);
        //TODO: Use real path of this map
        java.io.File f = new java.io.File("/home/paul/workspace_gdx/pocketmaps/europe_austria/");
        MapHandler.getInstance().createPathfinder(f, (o) -> //TODO: update mapFolder dynamic in createPathfinder(mapFolder, task)
        {
        	if (o instanceof GraphHopper)
        	{
        		graphHopper = (GraphHopper)o;
        	}
        	else
        	{
        		logger.log(Level.SEVERE, "Unable to generate Pathfinder" + o);
        	}
        });
	}
	
	public Map getMap() { return map; }
//	public VectorTileLayer getVectorTileLayer() { return vtLayer; }
//	public BuildingLayer getBuildingLayer() { return buildingLayer; }
//	public LabelLayer getLabelLayer() { return labelLayer; }
//	public MapEventsReceiver getTargetLayer() { return mer; }
	public GeoPoint getCenter()
	{
		if (tileSource.getMapInfo() == null) { throw new NullPointerException("Error getting boundingBox of map: " + getMapFile()); }
		return tileSource.getMapInfo().boundingBox.getCenterPoint();
	}
	public String getMapFile() { return mapFile; }
	public GraphHopper getPathfinder() { return graphHopper; }
	public void dispose()
	{
		boolean dTL = getMap().layers().remove(mer);
		if (graphHopper != null)
		{
			graphHopper.close();
			//graphHopper.clean(); //TODO: This line not necessary, as it deletes all files.
			graphHopper = null;
		}
        buildingLayer.onDetach();
        labelLayer.onDetach();
        boolean dBL = map.layers().remove(buildingLayer);
        boolean dLL = map.layers().remove(labelLayer);
        boolean dLV = map.layers().remove(vtLayer);
        vtLayer.onDetach();
        tileSource.close();
        System.out.println("Cleaned up: TargetLayer=" + dTL + " BuildLayer=" + dBL + " LabelLayer=" + dLL + " VT_Layer=" + dLV);
        vtLayer = null;
        tileSource = null;
        labelLayer = null;
        buildingLayer = null;
        map = null;
        mer = null;
	}
	
}
