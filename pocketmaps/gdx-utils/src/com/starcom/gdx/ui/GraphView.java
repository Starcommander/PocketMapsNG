package com.starcom.gdx.ui;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

public class GraphView
{
	Label l = new Label("", GuiUtil.getDefaultSkin());
	long textColor = 0xFF000000;
	ViewPort viewPort = new ViewPort();
    Image pixImg = new Image();
	Scale scale = new Scale();
	Scale secondScale = new Scale();
	LegendRenderer legendRenderer = new LegendRenderer();
//	float tmpArr[] = new float[8];
//	short[] tmpTri = new short[] {0, 1, 2, 0, 2, 3};
	
	/** A minimal GraphView for libgdx partially compatible with jjoe64 graphview for android */
	public GraphView() {}
	
	public Scale getScale() { return scale; }
	public Scale getSecondScale() { return secondScale; }
	public void setTextColor(long color)
	{
		textColor = color;
	}
	public LegendRenderer getLegendRenderer() { return legendRenderer; }
	public ViewPort getViewport() { return viewPort; }
	
// See https://stackoverflow.com/questions/15733442/drawing-filled-polygon-with-libgdx
//	public void draw()
//	{
//		PolygonSpriteBatch batch = new PolygonSpriteBatch();
//		batch.begin();
//		
//		batch.end();
//		l.draw(batch, 0.0f);
//	}
	
	private void drawLines(Pixmap pix, ArrayList<DataPoint> dataPoints, long color, int width, int height)
	{
    	long ccc = color; //TODO: Use this color and convert
        pix.setColor(Color.BLACK);
    	int x1 = 0;
    	int y1 = 0;
    	double maxX = 0;
    	double maxY = 0;
    	double minY = 0;
    	for (DataPoint p : dataPoints)
    	{
    		if (p.x > maxX) { maxX = p.x; }
    		if (p.y < minY) { minY = p.y; }
    		if (p.y > maxY) { maxY = p.y; }
    	}
    	for (DataPoint p : dataPoints)
    	{
    		double v = (p.x/maxX) * width;
    		int x2 = (int)v;
    		v = p.y - minY;
    		v = (v/maxY) * height;
    		int y2 = (int)v;
    		if (x2 == x1) {}
    		else
    		{
    			pix.drawLine(x1, y1, x2, y2);
    		}
    	}
	}
	
	/** Just draws a frame to image, that you get via getImage. **/
	private void drawFrame(Pixmap pix, int width, int height)
	{
		pix.setColor(Color.BLACK);
		int myWidth = width > 10 ? width-10 : width;
		int myHeight = height > 10 ? height-10 : height;
		int myX = width > 10 ? 5 : 0;
		int myY = height > 10 ? 5 : 0;
		pix.drawRectangle(0, 0, width, height);
		pix.drawRectangle(myX, myY, myWidth, myHeight);
	}
	
	/** Draws the graph for current data to the image. */
	public void drawToImage(int width, int height)
	{
        Pixmap pix = new Pixmap(width, height, Format.RGBA8888);
        drawFrame(pix, width,height);
        for (LineGraphSeries<?> s : getScale().lineGraphSeriesList)
        {
        	drawLines(pix, s.dataPoints, s.color, width, height);
        }
        for (LineGraphSeries<?> s : getSecondScale().lineGraphSeriesList)
        {
        	drawLines(pix, s.dataPoints, s.color, width, height);
        }
        
        TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(new Texture(pix)));
        pix.dispose();
        pixImg.setDrawable(drawable);
	}
	
	/** Returns the image where graph is drawn. */
	public Image getImage() { return pixImg; }
	
//	private void drawQuad(PolygonSpriteBatch batch, float x1, float y1, float x2, float y2, float x3, float y3, float x4, float y4)
//	{
//		tmpArr[0] = x1;
//		tmpArr[1] = y1;
//		tmpArr[2] = x2;
//		tmpArr[3] = y2;
//		tmpArr[4] = x3;
//		tmpArr[5] = y3;
//		tmpArr[6] = x4;
//		tmpArr[7] = y4;GuiUtil
//		Texture tex = new Texture("solid");
//		PolygonRegion polyReg = new PolygonRegion(new TextureRegion(Texture.), tmpArr, tmpTri);
//		batch.draw(polyReg, 0.0f, 0.0f);
//	}
	
	public static class DataPoint
	{
		double x,y;
		public DataPoint(double x, double y)
		{
			this.x = x;
			this.y = y;
		}
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
