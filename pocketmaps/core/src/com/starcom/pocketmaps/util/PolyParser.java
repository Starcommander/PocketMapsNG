package com.starcom.pocketmaps.util;

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
	public static boolean doItAll(Map map)
	{
		for (FileHandle polyFile : Gdx.files.internal("polys").list())
		{
			boolean success = doIt(map, polyFile.path());
			if (!success) { return false; }
		}
		return true;
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
