package com.starcom.pocketmaps.views;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.starcom.gdx.ui.Util;
import com.starcom.pocketmaps.Icons;
import com.starcom.pocketmaps.Icons.R;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;

public class NavView
{
	private static NavView instance = new NavView();
	
	HorizontalGroup topPanel;
	public final Label topStreetLabel = new Label("Street123", Util.getDefaultSkin());
	public final Label topInstructionLabel = new Label("---", Util.getDefaultSkin());
	public final Label topDistanceLabel = new Label("0 m", Util.getDefaultSkin());
	public final Label topTimeLabel = new Label("0 m", Util.getDefaultSkin());
	public final Label speedLabel = new Label("---", Util.getDefaultSkin()); // TODO: add this when configured.
	VerticalGroup bottomPanel = new VerticalGroup();
	TextButton bottomFromButton = new TextButton("From: ", Util.getDefaultSkin());
	TextButton bottomToButton = new TextButton("To: ", Util.getDefaultSkin());
	TextButton centerButton = new TextButton("[x]", Util.getDefaultSkin());
	
	private NavView()
	{
		initTopPanel();
	}
	
	public static NavView getInstance()
	{
		return instance;
	}
	
	public void updateInstruction(String street, String distance, String instr, Icons.R icon, String time)
	{
		topStreetLabel.setText(street);
		topDistanceLabel.setText(instr);
		topInstructionLabel.setText(instr);
		topTimeLabel.setText(time);
		topPanel.removeActorAt(0, false);
		topPanel.addActorAt(0, Icons.generateIcon(icon));
	}

	public WidgetGroup getTopPanel() { return topPanel; }
	public WidgetGroup getBottomPanel() { return bottomPanel; }
	public void showNaviCenterButton(boolean doshow)
	{
		centerButton.setVisible(doshow); //TODO: Add centerbutton first.
	}

	void initTopPanel()
	{
		VerticalGroup vg = new VerticalGroup();
		HorizontalGroup hg = new HorizontalGroup();
		vg.addActor(topStreetLabel);
		vg.addActor(topInstructionLabel);
		hg.removeActorAt(0, false);
		hg.addActor(Icons.generateIcon(R.ic_finish_flag));
		hg.addActor(vg);
		topPanel = hg;
	}
	
	void initBottomPanel()
	{
		bottomPanel.addActor(bottomFromButton);
		bottomPanel.addActor(bottomToButton);
		bottomFromButton.addListener(Util.wrapClickListener((x,y) -> onFromToClick(true)));
		bottomToButton.addListener(Util.wrapClickListener((x,y) -> onFromToClick(false)));
	}
	
	void onFromToClick(final boolean from)
	{
		System.out.println("Pressed from=true or to=false: " + from);
		//TODO: ActionListener --> Show ListView and allow to set.
	}
}
