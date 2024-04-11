package com.starcom.gdx.ui;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.starcom.gdx.io.Storage;
import com.starcom.gdx.io.Web;
import com.starcom.interfaces.IClickListener;

public class ListSelect extends Window
{
	VerticalGroup scrollList;
	ScrollPane scrollP;
	
	private ClickListener closeListener = new ClickListener()
	{
		public void clicked (InputEvent event, float x, float y) { ListSelect.this.remove(); }
	};
	
	public ListSelect(String title)
	{
		super(title, GuiUtil.getDefaultSkin());
		setPosition(Gdx.graphics.getWidth()*0.1f, 0);
		setWidth(Gdx.graphics.getWidth()*0.8f);
		setHeight(Gdx.graphics.getHeight());
		this.scrollList = new VerticalGroup();
		
		scrollP = new ScrollPane(scrollList, GuiUtil.getDefaultSkin());
		add(this.scrollP).width(getWidth()).height(getHeight()-60); // CloseButton=30 Header=30

		row();
		TextButton closeB = new TextButton("Close", GuiUtil.getDefaultSkin());
		
		closeB.addListener(closeListener);
		add(closeB).width(200).height(30);
		setModal(true);
	}

	public void addElement(String line, IClickListener l)
	{
		TextButton b = new TextButton(line, GuiUtil.getDefaultSkin());
		b.addListener(GuiUtil.wrapClickListener(l));
		b.addListener(closeListener);
		scrollList.addActor(b);
	}
	
	public void addCheckboxElement(String line, boolean checked, IClickListener l)
	{
		CheckBox b = new CheckBox(line, GuiUtil.getDefaultSkin());
		b.addListener(GuiUtil.wrapClickListener(l));
		b.addListener(closeListener);
		b.setChecked(checked);
		scrollList.addActor(b);
	}
	
	public void addElement(Actor actor, IClickListener l)
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
