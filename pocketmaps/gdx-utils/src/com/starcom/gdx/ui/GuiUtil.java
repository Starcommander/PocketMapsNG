package com.starcom.gdx.ui;

import java.util.function.Consumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.starcom.interfaces.IClickListener;

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

	/** Add GUI elements. */
	public static void addActor(Actor a)
	{
		if (uiStage == null) { throw new NullPointerException("Stage must be set first."); }
		uiStage.addActor(a);
	}
	
	/** Gets the guiStage, where to add GUI elements. */
	public static Stage getStage()
	{
		if (uiStage == null) { throw new NullPointerException("Stage must be set first."); }
		return uiStage;
	}
	
	/** Wraps the IClickListener into a (gdx-)ClickListener.
	 * <br>This allows to use lamda-expressions.
	 * @param l The listener to wrap.
	 * @return The (gdx-)ClickListener with l included. */
	public static ClickListener wrapClickListener(IClickListener<Actor> l)
	{
		return new ClickListener()
		{
			@Override
			public void clicked (InputEvent event, float x, float y) { l.click(event.getListenerActor(), x, y); }
		};
	}
	
	/** Same as wrapClickListener, but fires changeEvent with x and y as 0. */
	public static ChangeListener wrapChangeListener(IClickListener<Actor> l)
	{
		return new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor) { l.click(actor, 0, 0); }
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
	public static SelectBox<String> genDropDown(Consumer<String> l, int x, int y, String ...items)
	{
		SelectBox<String> selectBox = new SelectBox<String>(GuiUtil.getDefaultSkin())
		{
			@Override
			public void draw (Batch batch, float parentAlpha)
			{
				validate();

				Drawable background = getBackgroundDrawable();

				Color color = getColor();
				float x = getX(), y = getY();
				float width = getWidth(), height = getHeight();

				batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
				if (background != null) background.draw(batch, x, y, width, height);
			}
		};
		selectBox.setItems(items);
		selectBox.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				l.accept(selectBox.getSelected());
			}
		});
		selectBox.setPosition(x, y);
		return selectBox;
	}
	
	/** Creates a checkBox menu. */
	public static Actor genCheckBox(Consumer<Boolean> l, int x, int y, String txt, boolean checked)
	{
		CheckBox selectBox = new CheckBox(txt, GuiUtil.getDefaultSkin());
		selectBox.addListener(new ChangeListener()
		{
			@Override
			public void changed(ChangeEvent event, Actor actor)
			{
				l.accept(selectBox.isChecked());
			}
		});
		selectBox.setPosition(x, y);
		selectBox.setChecked(checked);
		return selectBox;
	}
	
	/** Creates a text label. */
	public static Label genLabel(String txt, int x, int y)
	{
		Label l = new Label(txt, getDefaultSkin());
		l.setX(x);
		l.setY(y);
		return l;
	}

	/** Creates a text button. */
	public static TextButton genButton(String txt, int x, int y, IClickListener<Actor> onClick)
	{
		TextButton l = new TextButton(txt, getDefaultSkin());
		l.setX(x);
		l.setY(y);
		l.addListener(GuiUtil.wrapClickListener(onClick));
		return l;
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
    
    /** Allows to disable (gray out) or enable an actor, for example a button. */
    public static void setEnabled(Actor a, boolean enabled)
    {
    	if (enabled)
    	{
    		if (a instanceof TextButton) { ((TextButton)a).getLabel().setColor(Color.WHITE); }
    		a.setTouchable(Touchable.enabled);
    		a.setColor(1,1,1,1);
    	}
    	else
    	{
    		if (a instanceof TextButton) { ((TextButton)a).getLabel().setColor(Color.BLACK); }
    		a.setTouchable(Touchable.disabled);
    		a.setColor(Color.GRAY);
    	}
    }
}
