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
//	Skin uiSkin;
	ScrollPane scrollP;
	Window win;
	
	private ClickListener closeListener = new ClickListener()
	{
		public void clicked (InputEvent event, float x, float y) { ListSelect.this.remove(); }
	};
	
	public ListSelect(String title)
	{
		super(title, Util.getDefaultSkin());
		setPosition(Gdx.graphics.getWidth()*0.1f, 0);
		setWidth(Gdx.graphics.getWidth()*0.8f);
		setHeight(Gdx.graphics.getHeight());
//		this.uiSkin = uiSkin;
		this.scrollList = new VerticalGroup();
		
		

		//Label headL = new Label(title, Util.getDefaultSkin());
		scrollP = new ScrollPane(scrollList, Util.getDefaultSkin());
//		scrollP.setActor(scrollList);
		add(this.scrollP).width(getWidth()).height(getHeight()-60); // CloseButton=30 Header=30

		//TextButton closeB = new TextButton("Close", Util.getDefaultSkin());
		//addActor(headL);
		//addActor(scrollP);
		//addActor(closeB);
//		scrollP.addListener(createScrollListener());
		

		//win = new Window("WinTitle", uiSkin);
		//win.add(this.scrollP).width(200).height(200);
		
		row();
		TextButton closeB = new TextButton("Close", Util.getDefaultSkin());
		
		closeB.addListener(closeListener);
		add(closeB).width(200).height(30);
	}
	
//	private EventListener createScrollListener()
//	{
//		return new EventListener()
//		{
//			float lastPosY = 0;
//
//			@Override
//			public boolean handle(Event event)
//			{
//				if (elements == null) { return false; }
//				float y = scrollP.getScrollY();
//				if (lastPosY == y) { return false; }
//				updateElements();
//				lastPosY = y;
//				return false;
//			}
//		};
//	}

	public void addElement(String line, IClickListener l)
	{
		TextButton b = new TextButton(line, Util.getDefaultSkin());
		b.addListener(Util.wrapClickListener(l));
		b.addListener(closeListener);
//		scrollList.addActor(new Label(line, Util.getDefaultSkin()));
		scrollList.addActor(b);
	}
	
	public void addCheckboxElement(String line, boolean checked, IClickListener l)
	{
		CheckBox b = new CheckBox(line, Util.getDefaultSkin());
		b.addListener(Util.wrapClickListener(l));
		b.addListener(closeListener);
		b.setChecked(checked);
//		scrollList.addActor(new Label(line, Util.getDefaultSkin()));
		scrollList.addActor(b);
	}
	
	public void addElement(Actor actor, IClickListener l)
	{
		actor.addListener(Util.wrapClickListener(l));
		actor.addListener(closeListener);
		scrollList.addActor(actor);
		scrollList.addActor(new Label("-", Util.getDefaultSkin()));
	}
	

//	public void setElements(ArrayList<ListItem> elements)
//	{
//		
//	}
	
//	private void updateElements()
//	{
//		scrollList.clearChildren();
//		float fullH = 0;
//		for (ListItem item : elements)
//		{
//			fullH += item.getItemHeight();
//		}
//		scrollList.setLayoutEnabled(false);
//		scrollList.setSize(Gdx.graphics.getWidth()*0.8f, fullH);
//
//		float y = scrollList.getY();
//		float h = Gdx.graphics.getHeight();
//		if (y < 0) { y += fullH - (h/2); }
//		float minY = y - (2*h);
//		if (minY < 0) { minY = 0; }
//		float maxY = y + (3*h);
//		float curY = 0;
//int countV = 0;
//		for (ListItem item : elements)
//		{
//			curY += item.getItemHeight();
////			if (countV==0 || countV==elements.size()-1) {} // Allways add first and last one
////			else if (curY < minY) { item.cleanupItem(); continue; }
////			else if (curY > maxY) { item.cleanupItem(); continue; }
//			Actor a = item.generateItem();
//			a.setPosition(0, curY - item.getItemHeight());
//			scrollList.addActor(a);
//countV++;
//		}
//System.err.println("Update fullH=" + fullH);
//System.err.println("Update minY=" + minY + " maxY=" + maxY);
//System.err.println("Update Y=" + y + " cnt=" + countV);
//	}
	
//	private void updateElementsOld()
//	{
//		scrollList.clearChildren();
//		float fullH = 0;
//		for (ListItem item : elements)
//		{
//			fullH += item.getItemHeight();
//		}
//		float y = scrollList.getY();
//		float h = Gdx.graphics.getHeight();
//		if (y < 0) { y += fullH - (h/2); }
//		float minY = y - (2*h);
//		if (minY < 0) { minY = 0; }
//		float maxY = y + (3*h);
//		float curY = 0;
//		float spaceTop = -1;
//		float spaceBottom = -1;
//int countV = 0;
//		for (ListItem item : elements)
//		{
//			curY += item.getItemHeight();
//			if (curY < minY) { item.cleanupItem(); continue; }
//			if (curY > maxY) { item.cleanupItem(); continue; }
//			if (spaceTop < 0) { spaceTop = curY - item.getItemHeight(); }
//			Actor a = item.generateItem();
//			scrollList.addActor(a);
//countV++;
//		}
//		spaceBottom = curY - (spaceTop + h*5);
//		if (spaceBottom < 0) { spaceBottom = 0; }
//		scrollList.padTop(spaceTop);
//		scrollList.padBottom(spaceBottom);
//System.err.println("Update top=" + spaceTop + " bottom=" + spaceBottom);
//System.err.println("Update minY=" + minY + " maxY=" + maxY);
//System.err.println("Update Y=" + y + " cnt=" + countV);
//	}
	
//	public void showAsDialog()
//	{
//		Dialog dialog = new Dialog("Warning", uiSkin, "dialog") {
//		    public void result(Object obj) {
//		        System.out.println("result "+obj);
//		    }
//		};
//	}
	
	public void showAsWindow(Stage guiStage)
	{
		guiStage.addActor(this);
		guiStage.setScrollFocus(scrollP);
//guiStage.setKeyboardFocus(this.scrollP);
//		win.addActor(scrollP);
//		win
		
	}
	
//	static interface ListItem
//	{
//		public int getId();
//		public Actor generateItem();
//	}
}
