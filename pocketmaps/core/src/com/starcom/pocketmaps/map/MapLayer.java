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
	
	public MapLayer(String mapFile, Map map)
	{
		this.mapFile = mapFile;
		this.map = map;
		tileSource = new MapFileTileSource();
        tileSource.setMapFile(new java.io.File(mapFile).getAbsolutePath());
        vtLayer = map.setBaseMap(tileSource);
        map.setTheme(VtmThemes.DEFAULT);
        buildingLayer = new BuildingLayer(map, vtLayer);
        labelLayer = new LabelLayer(map, vtLayer);
        map.layers().add(buildingLayer);
        map.layers().add(labelLayer);
        java.io.File f = new java.io.File("/home/paul/workspace_gdx/pocketmaps/europe_austria/");
        MapHandler.getInstance().createPathfinder(f, (o) -> //TODO: update mapFolder dynamic in createPathfinder(mapFolder, task)
        {
        	if (o instanceof GraphHopper)
        	{
        		graphHopper = (GraphHopper)o;
        	}
        	else
        	{
        		logger.log(Level.SEVERE, "Unable to generate Pathfinder", o);
        	}
        });
	}
	
	public Map getMap() { return map; }
	public VectorTileLayer getVectorTileLayer() { return vtLayer; }
	public BuildingLayer getBuildingLayer() { return buildingLayer; }
	public LabelLayer getLabelLayer() { return labelLayer; }
	public GeoPoint getCenter() { return tileSource.getMapInfo().boundingBox.getCenterPoint(); }
	public String getMapFile() { return mapFile; }
	public GraphHopper getPathfinder() { return graphHopper; }
	
}
