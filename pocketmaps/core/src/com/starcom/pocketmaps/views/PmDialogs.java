package com.starcom.pocketmaps.views;

import java.util.logging.Logger;

import org.oscim.core.GeoPoint;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.starcom.LoggerUtil;
import com.starcom.gdx.ui.GuiUtil;
import com.starcom.gdx.ui.ToastMsg;
import com.starcom.interfaces.IObjectListener;
import com.starcom.pocketmaps.text.Text;

public class PmDialogs
{
	static Logger logger = LoggerUtil.get(PmDialogs.class);
	
	public static void showLatLonDialog(IObjectListener<GeoPoint> listener)
	{
		TextField latTF = new TextField("", GuiUtil.getDefaultSkin());
		TextField lonTF = new TextField("", GuiUtil.getDefaultSkin());
		Dialog dialog = new Dialog("Lat/Lon", GuiUtil.getDefaultSkin(), "dialog")
		{
		    public void result(Object obj)
		    {
		    	if (obj == Boolean.FALSE) { return; }
		    	float lat,lon;
		    	try
		    	{
		    		lat = Float.parseFloat(latTF.getText());
		    		lon = Float.parseFloat(lonTF.getText());
		    	}
		    	catch (NumberFormatException ex)
		    	{
		    		String msg = Text.getInstance().getErrorOccured() + ":\n" + Text.getInstance().getExample() + ": " + 33.33f;
		    		ToastMsg.getInstance().toastLong(msg);
		    		latTF.setColor(Color.RED);
		    		lonTF.setColor(Color.RED);
		    		cancel();
		    		return;
		    	}
		    	if (lat < -90 || lat > 90)
		    	{
		    		String msg = Text.getInstance().getErrorOccured() + ":\n" + Text.getInstance().getExample() + ": [" + (-90f) + "-" + 90f + "]";
		    		ToastMsg.getInstance().toastLong(msg);
		    		latTF.setColor(Color.RED);
			    	lonTF.setColor(1,1,1,1);
		    		cancel();
		    		return;
		    	}
		    	if (lon < -180 || lon > 180)
		    	{
		    		String msg = Text.getInstance().getErrorOccured() + ":\n" + Text.getInstance().getExample() + ": [" + (-180f) + "-" + 180f + "]";
		    		ToastMsg.getInstance().toastLong(msg);
		    		lonTF.setColor(Color.RED);
			    	latTF.setColor(1,1,1,1);
		    		cancel();
		    		return;
		    	}
		    	latTF.setColor(1,1,1,1);
		    	lonTF.setColor(1,1,1,1);
		    	logger.info("User entered lat=" + lat + " lon=" + lon + " and pressed ok.");
		    	if (listener != null)
		    	{
		    		listener.run(new GeoPoint(lat,lon));
		    	}
		    }
		};
		dialog.text(Text.getInstance().getEnterLatLon());
		dialog.add(latTF);
		dialog.add(lonTF);
		dialog.button("OK", true);
		dialog.button("Cancel", false);
		dialog.show(GuiUtil.getStage());
	}

}
