package com.starcom.gdx.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import org.oscim.backend.canvas.Color;
import org.oscim.core.GeoPoint;
import org.oscim.layers.PathLayer;
import org.oscim.map.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

public class PolyParser
{
	public static boolean doItAll(Map map) //TODO: Change package name, should not be in com.starcom.gdx!
	{
		for (FileHandle polyFile : Gdx.files.internal("polys").list())
		{
			boolean success = doIt(map, polyFile.path());
			if (!success) { return false; }
		}
		return true;
	}
	
	public static void addDebugLine(Map map) //TODO: clear this 
	{
		PathLayer pl = new PathLayer(map, Color.BLACK);
		map.layers().add(pl);
		pl.addPoint(new GeoPoint(48.271516,14.574035));
		pl.addPoint(new GeoPoint(48.274238,14.582121));
	}
	public static void addDebugLineX(Map map, ArrayList<GeoPoint> geos) //TODO: clear this 
	{
		PathLayer pl = new PathLayer(map, Color.BLACK);
		map.layers().add(pl);
		pl.addPoints(geos);
	}
	public static boolean doIt(Map map, String filePath)
	{
		try (BufferedReader in = Gdx.files.internal(filePath).reader(512))
		{
			ArrayList<PathLayer> polyList = parsePolyData(in, map);
			for (PathLayer path : polyList)
			{
				map.layers().add(path);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public static ArrayList<PathLayer> parsePolyData(BufferedReader in, Map map) throws IOException
	{
		ArrayList<PathLayer> polyList = new ArrayList<PathLayer>();
		ArrayList<GeoPoint> polyPoints = new ArrayList<GeoPoint>();
		while (true)
		{
			String line = in.readLine();
			if (line == null) { break; }
			else if (line.isEmpty()) { continue; }
			else if (line.equals("END"))
			{
				if (polyPoints.size() > 0)
				{
					PathLayer polyLayer = new PathLayer(map, Color.BLACK);
					polyLayer.addPoints(polyPoints);
					polyList.add(polyLayer);
					polyPoints.clear();
				}
			}
			else if (line.startsWith("\t"))
			{
				String lat = "";
				String lon = "";
				boolean second = false;
				for (char c : line.trim().toCharArray())
				{
					if (c == '\t') { second = true; }
					else
					{
						if (second) { lat += c; }
						else { lon += c; }
					}
				}
				double latD = Double.parseDouble(lat);
				double lonD = Double.parseDouble(lon);
				GeoPoint p = new GeoPoint(latD, lonD);
				polyPoints.add(p);
			}
		}
		return polyList;
	}
}
