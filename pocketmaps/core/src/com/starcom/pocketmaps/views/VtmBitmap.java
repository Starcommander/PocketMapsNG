package com.starcom.pocketmaps.views;

import org.oscim.backend.GL;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/** Contverts a Gdx-Image to a vtm internal Bitmap. */
public class VtmBitmap implements org.oscim.backend.canvas.Bitmap
{
	Pixmap pix;
	
	public VtmBitmap(Texture t)
	{
		t.getTextureData().prepare();
		pix = t.getTextureData().consumePixmap();
	}
	
	public VtmBitmap(String asset)
	{
		this(new Texture(asset));
	}

	@Override
	public int getWidth()
	{
		return pix.getWidth();
	}

	@Override
	public int getHeight()
	{
		return pix.getHeight();
	}

	@Override
	public void recycle()
	{
		pix.dispose();
	}

	@Override
	public int[] getPixels()
	{
		return null;
	}

	@Override
	public void eraseColor(int color)
	{
		throw new IllegalStateException("EraseColor on VtmBitmap not implemented yet");
	}

	@Override
	public void uploadToTexture(boolean replace)
	{
        Gdx.gl20.glTexImage2D(GL.TEXTURE_2D, 0, pix.getGLInternalFormat(), getWidth(),
                getHeight(), 0, pix.getGLFormat(), pix.getGLType(), pix.getPixels());
	}

	@Override
	public boolean isValid()
	{
		return pix != null;
	}

	@Override
	public byte[] getPngEncodedData()
	{
		return null;
	}

	@Override
	public void scaleTo(int width, int height)
	{
		throw new IllegalStateException("ScaleTo on VtmBitmap not implemented yet");
	}

}
