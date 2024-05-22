package com.starcom.gdx.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;

public class AbsLayout extends WidgetGroup
{
	int minWidth = 0;
	int minHeight = 0;
	float x;
	float y;
	float w;
	float h;
	
	/** Creates the Layout with absolute values.
	 * @param x The x location between 0 and 1.
	 * @param y The y location between 0 and 1. (Beginning from top)
	 * @param w The width between 0 and 1.
	 * @param x The height between 0 and 1. */
	public AbsLayout(float x, float y, float w, float h)
	{
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
	}
	
	/** Sets the minimum width, so that resizing the window will never lead to smaller widget. */
	public void setMinWidth(int minWidth)
	{
		this.minWidth = minWidth;
	}

	/** Sets the minimum height, so that resizing the window will never lead to smaller widget. */
	public void setMinHeight(int minHeight)
	{
		this.minHeight = minHeight;
	}
	
	/** Adds a child with relative location and size.
	 * @return The layoutModel for further modification. */
	public AbsUserObj addChild(Actor actor, float x, float y, float w, float h)
	{
		if (actor.getUserObject() != null)
		{
			throw new IllegalStateException("Actor has already a user object!");
		}
		AbsUserObj uObj = new AbsUserObj(x, y, w, h);
		actor.setUserObject(uObj);
		super.addActor(actor);
		return uObj;
	}
	
	/** Replaces a child and uses relative location and size.
	 * @return The layoutModel for further modification. */
	public AbsUserObj replaceChild(int index, Actor actor, float x, float y, float w, float h)
	{
		Actor a = super.removeActorAt(index, false);
		if (a == null)
		{
			throw new IllegalStateException("Actor for remove does not exist!");
		}
		a.setUserObject(null);
		a.remove(); // Maybe not necessary, as actor already removed.
		AbsUserObj uObj = new AbsUserObj(x, y, w, h);
		actor.setUserObject(uObj);
		super.addActorAt(index, actor);
		return uObj;
	}
	
	@Override
	public void setParent(Group p)
	{
		super.setParent(p);
		invalidate(); // Ensure to layout components
	}
	
	@Override
	public void layout()
	{
		Group parent = getParent();
		Stage stage = getStage();
		float ww = 0.0f;
		float hh = 0.0f;
		float xx = 0.0f;
		float yy = 0.0f;
		float parentW = 0.0f;
		float parentH = 0.0f;
		if (stage != null && parent == stage.getRoot())
		{
			parentW = stage.getWidth();
			parentH = stage.getHeight();
		}
		else
		{
			parentW = parent.getWidth();
			parentH = parent.getHeight();
		}
		ww = parentW * w;
		hh = parentH * h;
		if (ww < minWidth) { ww = minWidth; }
		if (hh < minHeight) { hh = minHeight; }
		xx = parentW * x;
		yy = parentH * y; // First from top
		if ((yy + hh) > parentH)
		{ // Clip may happen, when minHeight was forced.
			yy = minHeight; // Bottom
		}
		else
		{
			yy = parentH - yy; // From bottom
		}
		setSize(ww,hh);
		setX(xx);
		setY(yy);
		for (Actor c : getChildren())
		{
			Object o = c.getUserObject();
			if (o == null) { continue; }
			if (o instanceof AbsUserObj)
			{
				AbsUserObj a = (AbsUserObj)o;
				if (a.keepRatioW && a.ratio == 0)
				{ //Calculate ratio first time only
					a.ratio = c.getWidth()/c.getHeight();
				}
				if (!a.keepHight) { c.setHeight(hh * a.h); }
				if (a.keepRatioW)
				{
					c.setWidth(c.getHeight() * a.ratio);
				}
				else
				{
					c.setWidth(ww * a.w);
				}
				c.setX(ww * a.x);
				c.setY(0 - (hh * a.y) - c.getHeight());
				if (a.logDebug != null)
				{
					System.out.println(a.logDebug + ": x=" + c.getX() + " y=" + c.getY() + " w=" + c.getWidth() + " h=" + c.getHeight());
				}
			}
		}
	}
	
	public class AbsUserObj
	{
		float x;
		float y;
		float w;
		float h;
		String logDebug;
		boolean keepHight;
		boolean keepRatioW;
		float ratio;
		
		public AbsUserObj(float x, float y, float w, float h)
		{
			this.x = x;
			this.y = y;
			this.w = w;
			this.h = h;
		}
		
		public AbsUserObj setDebug(String txt) { logDebug = txt; return this; }
		public AbsUserObj setKeepHight() { keepHight = true; return this; }
		public AbsUserObj keepRatioW() { keepRatioW = true; return this; }
	}
}
