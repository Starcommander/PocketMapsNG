package com.starcom.interfaces;

/** A ClickListener that can be wrapped via Util.wrapClickListener(l); */
public interface IClickListener
{
	/** Executed when click is done.
	 * @param actor The actor that is listening.
	 * @param x The X position relative to actor
	 * @param y The Y position relative to actor */
	public void click(Object actor, float x, float y);
}
