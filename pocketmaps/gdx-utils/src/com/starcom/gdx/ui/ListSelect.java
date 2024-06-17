package com.starcom.gdx.ui;

import java.util.function.Consumer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.starcom.interfaces.IClickListener;

public class ListSelect extends Window
{
	VerticalGroup scrollList;
	ScrollPane scrollP;
	
	private ClickListener closeListener = new ClickListener()
	{
		public void clicked (InputEvent event, float x, float y) { ListSelect.this.remove(); }
	};

	/** Creates a ListSelectView with close button only.
	 * @param title The title of this window. */
	public ListSelect(String title)
	{
		this(title, null);
	}

	/** Creates a ListSelectView with close button only.
	 * @param title The title of this window.
	 * @param onClose Will be executed, when close pressed (always with false), may also be null. */
	public ListSelect(String title, Consumer<Boolean> onClose)
	{
		this(title, null, onClose);
	}
	
	/** Creates a ListSelectView.
	 * @param title The title of this window.
	 * @param extraButton Extra button name, or null for close button only.
	 * @param onClose Will be executed, when close pressed (false), or when extra button pressed (true), may also be null. */
	public ListSelect(String title, String extraButton, Consumer<Boolean> onClose)
	{
		super(title, GuiUtil.getDefaultSkin());
		setPosition(Gdx.graphics.getWidth()*0.1f, 0);
		setWidth(Gdx.graphics.getWidth()*0.8f);
		setHeight(Gdx.graphics.getHeight());
		this.scrollList = new VerticalGroup();
		
		scrollP = new ScrollPane(scrollList, GuiUtil.getDefaultSkin());
		add(this.scrollP).width(getWidth()).height(getHeight()-60).colspan(2); // CloseButton=30 Header=30

		row();
		TextButton closeB = new TextButton("Close", GuiUtil.getDefaultSkin());
		if (onClose!=null) { closeB.addListener(GuiUtil.wrapClickListener((e,x,y) -> onClose.accept(false))); }
		
		closeB.addListener(closeListener);
		if (extraButton != null)
		{
			TextButton extraB = new TextButton(extraButton, GuiUtil.getDefaultSkin());
			if (onClose!=null) { extraB.addListener(GuiUtil.wrapClickListener((e,x,y) -> onClose.accept(true))); }
			extraB.addListener(closeListener);
			add(extraB).width(200).height(30);
			add(closeB).width(200).height(30);
		}
		else
		{
			add(closeB).width(200).height(30).colspan(2);
		}
		setModal(true);
	}

	public void addElement(String line, IClickListener<Actor> l)
	{
		TextButton b = new TextButton(line, GuiUtil.getDefaultSkin());
		b.addListener(GuiUtil.wrapClickListener(l));
		b.addListener(closeListener);
		scrollList.addActor(b);
	}
	
	public void addCheckboxElement(String line, boolean checked, IClickListener<Actor> l)
	{
		CheckBox b = new CheckBox(line, GuiUtil.getDefaultSkin());
		b.addListener(GuiUtil.wrapClickListener(l));
		b.addListener(closeListener);
		b.setChecked(checked);
		scrollList.addActor(b);
	}
	
	public void addElement(Actor actor, IClickListener<Actor> l)
	{
		actor.addListener(GuiUtil.wrapClickListener(l));
		actor.addListener(closeListener);
		scrollList.addActor(actor);
		scrollList.addActor(new Label("-", GuiUtil.getDefaultSkin()));
	}
	
	public void showAsWindow(Stage guiStage)
	{
		guiStage.addActor(this);
		guiStage.setScrollFocus(scrollP);
		
	}
}
