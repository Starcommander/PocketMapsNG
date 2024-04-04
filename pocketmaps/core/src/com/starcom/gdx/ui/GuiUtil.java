package com.starcom.gdx.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.starcom.interfaces.IClickListener;
import com.starcom.interfaces.IObjectListener;

public class GuiUtil
{
	private static Skin uiSkin;
	private static Stage uiStage;
	
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
	
	public static void setStage(Stage stage)
	{
		uiStage = stage;
	}
	
	/** Gets the guiStage, where to add GUI elements via addActor(). */
	public static Stage getStage()
	{
		if (uiStage == null) { throw new NullPointerException("Stage must be set first."); }
		return uiStage;
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
	
	/** Creates a gray panel. */
	public static Actor genPanel(int x, int y, int w, int h) { return genPanel(x,y,w,h,Color.GRAY); }
	/** Creates a panel. */
	public static Actor genPanel(int x, int y, int w, int h, Color c)
	{
		Image pix = new Image(genColoredDrawable(w,h,c, false));
		pix.setPosition(x, y);
		return pix;
	}

	/** Creates a dropDown menu. */
	public static Actor genDropDown(IObjectListener l, int x, int y, String ...items)
	{
		SelectBox<String> selectBox=new SelectBox<String>(GuiUtil.getDefaultSkin());
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
		return selectBox;
	}
	
    /**
     * Creates an image of determined size filled with determined color.
     * 
     * @param width of an image.
     * @param height of an image.
     * @param color of an image fill.
     * @param asCircle whether drawable should be a circle or a quad.
     * @return {@link Drawable} of determined size filled with determined color.
     */
    public static Drawable genColoredDrawable(int width, int height, Color color, boolean asCircle) {
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
}
