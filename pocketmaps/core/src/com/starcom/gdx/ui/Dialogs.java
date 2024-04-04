package com.starcom.gdx.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.starcom.gdx.system.Threading;
import com.starcom.interfaces.IProgressListener;
import com.starcom.pocketmaps.views.TopPanel;
import com.starcom.interfaces.IObjectListener;

public class Dialogs
{
	
	public static Actor showPanel(int x, int y, int w, int h)
	{
		Image pix = new Image(getColoredDrawable(w,h,Color.GRAY, false));
		pix.setPosition(x, y);
		TopPanel.getInstance().getGuiStage().addActor(pix);
		return pix;
	}
	
	public static Actor showDropDown(IObjectListener l, int x, int y, String ...items)
	{
		SelectBox<String> selectBox=new SelectBox<String>(UiUtil.getDefaultSkin());
		selectBox.setItems(items);
		selectBox.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				l.run(selectBox.getSelected());
			}
		});
		selectBox.setPosition(x, y);
		TopPanel.getInstance().getGuiStage().addActor(selectBox);
		return selectBox;
	}
	
    /**
     * Creates an image of determined size filled with determined color.
     * 
     * @param width of an image.
     * @param height of an image.
     * @param color of an image fill.
     * @return {@link Drawable} of determined size filled with determined color.
     */
    private static Drawable getColoredDrawable(int width, int height, Color color, boolean asCircle) {
            Pixmap pixmap = new Pixmap(width, height, Format.RGBA8888);
            pixmap.setColor(color);
            if (asCircle)
           	{
            	pixmap.fillCircle(height/2, height/2, height/2);
            	pixmap.fillCircle(width - (height/2), height/2, height/2);
            	pixmap.fillRectangle(height/2, 0, width-height, height);
           	}
            else { pixmap.fill(); }
            
            TextureRegionDrawable drawable = new TextureRegionDrawable(new TextureRegion(new Texture(pixmap)));
            pixmap.dispose();
            return drawable;
    }

    /** Creates a ProgressListener and shows the progress.
     * @param guiStage The GuiStage.
     * @param onSuccess Runnable for success, or null.
     * @param onError Listener for error, or null.
     * @throws IllegalStateException when this function is executed in a worker Thread. */
	public static IProgressListener showProgress(Stage guiStage, Runnable onSuccess, IObjectListener onError)
	{
		if (!Threading.getInstance().isMainThread())
		{
			throw new IllegalStateException("Show progress only allowed in main thread");
		}
		ProgressBarStyle barStyle = new ProgressBarStyle();
		int width = 400;
		int height = 30;
		
		barStyle.background = getColoredDrawable(width, height, Color.RED, true);
		barStyle.knob = getColoredDrawable(0, height, Color.GREEN, true);
		barStyle.knobBefore = getColoredDrawable(width, height, Color.GREEN, true);
		
		ProgressBar bar = new ProgressBar(0, 100, 1, false, barStyle);
		
		bar.setPosition(100, 100);
		bar.setSize(290, bar.getPrefHeight());
		bar.setAnimateDuration(0.5f);
		guiStage.addActor(bar);
		return new IProgressListener()
		{
			int lastPerc = -1;
			@Override
			public void onProgress(Type type, Object txt)
			{
				if (type == Type.PROGRESS)
				{
					Integer perc = (Integer)txt;
					if (perc != lastPerc) { bar.setValue(perc); }
				}
				else if (type == Type.SUCCESS)
				{
					bar.remove();
					if (onSuccess != null)
					{
						onSuccess.run();
					}
				}
				else if (type == Type.CANCEL)
				{
					bar.remove();
				}
				else if (type == Type.ERROR)
				{
					bar.remove();
					if (onError != null)
					{
						onError.run(txt);
					}
				}
			}
		};
	}
	
	/** Shows a simple dialog.
	 * @param guiStage The guiStage where to attach.
	 * @param title The Dialog title.
	 * @param msg The Message.
	 * @param cancel True to also show cancel-button.
	 * @param listener The listener for result of OK="true" and CANCEL="false", listener may also be null. */
	public static void showDialog(Stage guiStage, String title, String msg, boolean cancel, IObjectListener listener)
	{
		Dialog dialog = new Dialog(title, UiUtil.getDefaultSkin(), "dialog")
		{
		    public void result(Object obj)
		    {
		    	if (listener != null) { listener.run(obj); }
		    }
		};
		dialog.text(msg);
		dialog.button("OK", true);
		if (cancel) { dialog.button("Cancel", false); }
		dialog.show(guiStage);
	}
}
