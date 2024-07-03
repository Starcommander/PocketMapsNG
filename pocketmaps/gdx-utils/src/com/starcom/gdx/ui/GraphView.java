package com.starcom.gdx.ui;

import java.util.ArrayList;

import com.badlogic.gdx.scenes.scene2d.ui.Label;

public class GraphView
{
	Label l;
	long textColor = 0xFF000000;
	ViewPort viewPort = new ViewPort();
	Scale scale = new Scale();
	Scale secondScale = new Scale();
	LegendRenderer legendRenderer = new LegendRenderer();
	
	public Scale getScale() { return scale; }
	public Scale getSecondScale() { return secondScale; }
	public void setTextColor(long color)
	{
		textColor = color;
	}
	public LegendRenderer getLegendRenderer() { return legendRenderer; }
	public ViewPort getViewport() { return viewPort; }
// See https://stackoverflow.com/questions/15733442/drawing-filled-polygon-with-libgdx
	
	public static class DataPoint
	{
		double x,y;
	}
	public static class LineGraphSeries<T>
	{
		ArrayList<DataPoint> dataPoints = new ArrayList<>();
		long color = 0xFF000000;
		private String title = "---";
		public void setTitle(String title) { this.title = title; }
		public void setColor(long color)
		{
			this.color = color;
		}
		public void resetData(ArrayList<DataPoint> dataPoints) { this.dataPoints = dataPoints; }
		public void addData(DataPoint p) { dataPoints.add(p); }
		public double getHighestValueY()
		{
			double maxY = 0;
			for (DataPoint d : dataPoints)
			{
				if (d.y > maxY) { maxY = d.y; }
			}
			return maxY;
		}
	}
	public static class ViewPort
	{
		int minX = 0;
		double maxX = Double.NaN;
		double maxY = Double.NaN;
		boolean scaleable = false;
		boolean scrollable = false;
		public void setMinX(int minX) { this.minX = minX; }
		public void setMaxX(double maxX) { this.maxX = maxX; }
		public void setMaxY(double maxY) { this.maxY = maxY; }
		public void setScalable(boolean scaleable) { this.scaleable = scaleable; }
		public void setScrollable(boolean scrollable) { this.scrollable = scrollable; }
	}
	public static class Scale
	{
		double minY = Double.NaN;
		double maxY = Double.NaN;
		ArrayList<LineGraphSeries<?>> lineGraphSeriesList = new ArrayList<>();
		public void addSeries(LineGraphSeries<?> s)
		{
			lineGraphSeriesList.add(s);
		}
		public void setMinY(double minY) { this.minY = minY; }
		public void setMaxY(double maxY) { this.maxY = maxY; }
	}
	public static class LegendRenderer
	{
		public enum LegendAlign { TOP, BOTTOM }
		boolean visible;
		LegendAlign align = LegendAlign.BOTTOM;
		
		public void setVisible(boolean visible) { this.visible = visible; }
		public void setAlign(LegendAlign align) { this.align = align; }
	}
}
