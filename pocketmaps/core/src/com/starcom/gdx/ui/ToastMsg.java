package com.starcom.gdx.ui;

import java.util.Iterator;
import java.util.LinkedList;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Align;
import com.starcom.gdx.system.Threading;

/* Source: https://github.com/wentsa/Toast-LibGDX */

public class ToastMsg
{
	private static ToastMsg instance;
	private final LinkedList<Toast> toasts = new LinkedList<Toast>();
	private Toast.ToastFactory toastFactory = new Toast.ToastFactory.Builder().font(Util.getDefaultFont()).build();

	public static ToastMsg getInstance()
	{
		if (instance == null)
		{
			instance = new ToastMsg();
		}
		return instance;
	}
	
	private ToastMsg() {} // Make it private. Use getInstance().

	/**
	 * Displays long toast
	 */
	public void toastLong(String text)
	{
		Threading.getInstance().invokeOnMainThread(() ->
		{
			toasts.add(toastFactory.create(text, Toast.Length.LONG));
			Gdx.graphics.requestRendering();
		});
	}

	/**
	 * Displays short toast
	 */
	public void toastShort(String text)
	{
		Threading.getInstance().invokeOnMainThread(() ->
		{
			toasts.add(toastFactory.create(text, Toast.Length.SHORT));
			Gdx.graphics.requestRendering();
		});
	}

	/** Must be called on App.render(). */
	public void render()
	{
		// handle toast queue and display
		Iterator<Toast> it = toasts.iterator();
		if (it.hasNext()) { Gdx.graphics.requestRendering(); }
		while (it.hasNext())
		{
			Toast t = it.next();
			if (!t.render(Gdx.graphics.getDeltaTime()))
			{
				it.remove(); // toast finished -> remove
			}
			else
			{
				break; // first toast still active, break the loop
			}
		}
	}

	public static class Toast
	{
		public enum Length
		{
			SHORT(2), LONG(3.5f);

			private final float duration; // in seconds

			Length(float duration)
			{
				this.duration = duration;
			}
		}

		private String msg;
		private final BitmapFont font;
		private float fadingDuration;
		private final Color fontColor;
		private SpriteBatch spriteBatch = new SpriteBatch();
		private ShapeRenderer renderer = new ShapeRenderer();

		private float opacity = 1f;
		private int toastWidth;
		private int toastHeight;
		private float timeToLive;
		private float positionX, positionY, positionYadd; // left bottom corner
		private float fontX, fontY; // left top corner
		private int fontWidth;

		Toast(String text, Length length, BitmapFont font, Color backgroundColor, float fadingDuration,
				float maxRelativeWidth, Color fontColor, float positionY, Integer customMargin)
		{
			this.msg = text;
			this.font = font;
			this.fadingDuration = fadingDuration;
			this.positionY = positionY;
			this.fontColor = fontColor;

			this.timeToLive = length.duration;
			renderer.setColor(backgroundColor);

			// measure text box
			GlyphLayout layoutSimple = new GlyphLayout();
			layoutSimple.setText(this.font, text);

			int lineHeight = (int) layoutSimple.height;
			fontWidth = (int) (layoutSimple.width);
			int fontHeight = (int) (layoutSimple.height);

			int margin = customMargin == null ? lineHeight * 2 : customMargin;

			float screenWidth = Gdx.graphics.getWidth();
			float maxTextWidth = screenWidth * maxRelativeWidth;
			if (fontWidth > maxTextWidth)
			{
				BitmapFontCache cache = new BitmapFontCache(this.font, true);
				GlyphLayout layout = cache.addText(text, 0, 0, maxTextWidth, Align.center, true);
				fontWidth = (int) layout.width;
				fontHeight = (int) layout.height;
			}

			toastHeight = fontHeight + 2 * margin;
			toastWidth = fontWidth + 2 * margin;

			positionX = (screenWidth / 2) - toastWidth / 2;

			fontX = positionX + margin;
			fontY = positionY + margin + fontHeight;
		}

		/**
		 * Displays toast<br/>
		 * Must be called at the end of {@link Game#render()}<br/>
		 * 
		 * @param delta {@link Graphics#getDeltaTime()}
		 * @return activeness of the toast (true while being displayed, false otherwise)
		 */
		public boolean render(float delta)
		{
			timeToLive -= delta;
			if (timeToLive < 0)
			{
				return false;
			}
			if (timeToLive < 1)
			{
				float screenHeight = Gdx.graphics.getHeight();
				positionYadd =  (1-timeToLive) * -(screenHeight/3);
			}


			renderer.begin(ShapeRenderer.ShapeType.Filled);
			renderer.circle(positionX, positionY + positionYadd + toastHeight / 2, toastHeight / 2);
			renderer.rect(positionX, positionY + positionYadd, toastWidth, toastHeight);
			renderer.circle(positionX + toastWidth, positionY + positionYadd + toastHeight / 2, toastHeight / 2);
			renderer.end();

			spriteBatch.begin();

			if (timeToLive > 0 && opacity > 0.15)
			{
				if (timeToLive < fadingDuration)
				{
					opacity = timeToLive / fadingDuration;
				}

				font.setColor(fontColor.r, fontColor.g, fontColor.b, fontColor.a * opacity);
				font.draw(spriteBatch, msg, fontX, fontY + positionYadd, fontWidth, Align.center, true);
			}

			spriteBatch.end();

			return true;
		}

		/**
		 * Factory for creating toasts
		 */
		public static class ToastFactory
		{

			private BitmapFont font;
			private Color backgroundColor = new Color(55f / 256, 55f / 256, 55f / 256, 1);
			private Color fontColor = new Color(1, 1, 1, 1);
			private float positionY;
			private float fadingDuration = 0.5f;
			private float maxRelativeWidth = 0.65f;
			private Integer customMargin;

			private ToastFactory()
			{
				float screenHeight = Gdx.graphics.getHeight();
				positionY = screenHeight/3;
//				float bottomGap = 100;
//				positionY = bottomGap + ((screenHeight - bottomGap) / 10);
			}

			/**
			 * Creates new toast
			 * 
			 * @param text   message
			 * @param length toast duration
			 * @return newly created toast
			 */
			public Toast create(String text, Length length)
			{
				return new Toast(text, length, font, backgroundColor, fadingDuration, maxRelativeWidth, fontColor,
						positionY, customMargin);
			}

			/**
			 * Builder for creating factory
			 */
			public static class Builder
			{

				private boolean built = false;
				private ToastFactory factory = new ToastFactory();

				/**
				 * Specify font for toasts
				 * 
				 * @param font font
				 * @return this
				 */
				public Builder font(BitmapFont font)
				{
					check();
					factory.font = font;
					return this;
				}

				/**
				 * Specify background color for toasts.<br/>
				 * Note: Alpha channel is not supported (yet).<br/>
				 * Default: rgb(55,55,55)
				 * 
				 * @param color background color
				 * @return this
				 */
				public Builder backgroundColor(Color color)
				{
					check();
					factory.backgroundColor = color;
					return this;
				}

				/**
				 * Specify font color for toasts.<br/>
				 * Default: white
				 * 
				 * @param color font color
				 * @return this
				 */
				public Builder fontColor(Color color)
				{
					check();
					factory.fontColor = color;
					return this;
				}

				/**
				 * Specify vertical position for toasts<br/>
				 * Default: bottom part
				 * 
				 * @param positionY vertical position of bottom left corner
				 * @return this
				 */
				public Builder positionY(float positionY)
				{
					check();
					factory.positionY = positionY;
					return this;
				}

				/**
				 * Specify fading duration for toasts<br/>
				 * Default: 0.5s
				 * 
				 * @param fadingDuration duration in seconds which it takes to disappear
				 * @return this
				 */
				public Builder fadingDuration(float fadingDuration)
				{
					check();
					if (fadingDuration < 0)
					{
						throw new IllegalArgumentException("Duration must be non-negative number");
					}
					factory.fadingDuration = fadingDuration;
					return this;
				}

				/**
				 * Specify max text width for toasts<br/>
				 * Default: 0.65
				 * 
				 * @param maxTextRelativeWidth max text width relative to screen (Eg. 0.5 = max
				 *                             text width is equal to 50% of screen width)
				 * @return this
				 */
				public Builder maxTextRelativeWidth(float maxTextRelativeWidth)
				{
					check();
					factory.maxRelativeWidth = maxTextRelativeWidth;
					return this;
				}

				/**
				 * Specify text margin for toasts<br/>
				 * Default: line height
				 * 
				 * @param margin margin in px
				 * @return this
				 */
				public Builder margin(int margin)
				{
					check();
					factory.customMargin = margin;
					return this;
				}

				/**
				 * Builds factory
				 * 
				 * @return new factory
				 */
				public ToastFactory build()
				{
					check();
					if (factory.font == null)
					{
						throw new IllegalStateException("Font is not set");
					}
					built = true;
					return factory;
				}

				private void check()
				{
					if (built)
					{
						throw new IllegalStateException("Builder can be used only once");
					}
				}
			}
		}
	}
}
