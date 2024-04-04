package com.starcom.gdx.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.starcom.interfaces.IClickListener;

public class UiUtil
{
	private static Skin uiSkin;
	
	public static Skin getDefaultSkin()
	{
		if (uiSkin == null)
		{
			uiSkin = new Skin(Gdx.files.internal("default_skin/uiskin.json"));
		}
		return uiSkin;
	}
	
	public static BitmapFont getDefaultFont()
	{
		return getDefaultSkin().getFont("default-font");
	}
	
	/** Wraps the IClickListener into a (gdx-)ClickListener.
	 * <br>This allows to use lamda-expressions.
	 * @param l The listener to wrap.
	 * @return The (gdx-)ClickListener with l included. */
	public static ClickListener wrapClickListener(IClickListener l)
	{
		return new ClickListener()
		{
			public void clicked (InputEvent event, float x, float y) { l.click(event.getListenerActor(), x, y); }
		};
	}
}
