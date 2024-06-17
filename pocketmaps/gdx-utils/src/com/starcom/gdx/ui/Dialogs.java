package com.starcom.gdx.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar.ProgressBarStyle;
import com.starcom.system.Threading;
import com.starcom.interfaces.IProgressListener;
import com.starcom.interfaces.IObjectListener;

public class Dialogs
{
	
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
		
		barStyle.background = GuiUtil.genColoredDrawable(width, height, Color.RED, true);
		barStyle.knob = GuiUtil.genColoredDrawable(0, height, Color.GREEN, true);
		barStyle.knobBefore = GuiUtil.genColoredDrawable(width, height, Color.GREEN, true);
		
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
	public static void showDialog(Stage guiStage, String title, String msg, boolean cancel, IObjectListener<Boolean> listener)
	{
		Dialog dialog = new Dialog(title, GuiUtil.getDefaultSkin(), "dialog")
		{
		    public void result(Object obj)
		    {
		    	if (listener != null) { listener.run(obj == Boolean.TRUE); }
		    }
		};
		dialog.text(msg);
		dialog.button("OK", true);
		if (cancel) { dialog.button("Cancel", false); }
		dialog.show(guiStage);
	}
}
