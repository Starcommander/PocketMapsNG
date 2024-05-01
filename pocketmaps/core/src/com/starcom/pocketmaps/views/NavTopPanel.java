package com.starcom.pocketmaps.views;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.starcom.gdx.ui.GuiUtil;
import com.starcom.pocketmaps.Icons;
import com.starcom.pocketmaps.Icons.R;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;

public class NavTopPanel
{
	private static NavTopPanel instance = new NavTopPanel();
	
	HorizontalGroup topPanel;
	private final Label topStreetLabel = new Label("Street123", GuiUtil.getDefaultSkin());
	private final Label topInstructionLabel = new Label("---", GuiUtil.getDefaultSkin());
	private final Label topDistanceLabel = new Label("0 m", GuiUtil.getDefaultSkin());
	private final Label topTimeLabel = new Label("0 m", GuiUtil.getDefaultSkin());
	private final Label speedLabel = new Label("---", GuiUtil.getDefaultSkin()); // TODO: add this when configured.
//	VerticalGroup bottomPanel = new VerticalGroup();
//	private TextButton bottomFromButton = new TextButton("From: ", GuiUtil.getDefaultSkin());
//	private TextButton bottomToButton = new TextButton("To: ", GuiUtil.getDefaultSkin());
//	private TextButton centerButton = new TextButton("[x]", GuiUtil.getDefaultSkin());
	private Actor centerButton = GuiUtil.genButton("[X]", Gdx.graphics.getWidth()-30, Gdx.graphics.getHeight()-30, null);
	private boolean visible = false;
	
	private NavTopPanel()
	{
		VerticalGroup vg = new VerticalGroup();
		vg.addActor(topStreetLabel);
		vg.addActor(topInstructionLabel);
		VerticalGroup vg2 = new VerticalGroup();
		vg2.addActor(topDistanceLabel);
		vg2.addActor(topTimeLabel);
		HorizontalGroup hg = new HorizontalGroup();
		hg.addActor(Icons.generateIcon(R.ic_finish_flag));
		hg.addActor(vg);
		hg.addActor(vg2);
		hg.setY(Gdx.graphics.getHeight()-90); //TODO: is 90 ok?
		topPanel = hg;
	}
	
	public static NavTopPanel getInstance()
	{
		return instance;
	}
	
	public Label getSpeedLabel() { return speedLabel; }
	
	public void updateInstruction(String street, String distance, String instr, Icons.R icon, String time)
	{
		topStreetLabel.setText(street);
		topDistanceLabel.setText(instr);
		topInstructionLabel.setText(instr);
		topTimeLabel.setText(time);
		topPanel.removeActorAt(0, false);
		topPanel.addActorAt(0, Icons.generateIcon(icon));
	}

	public void setVisible(boolean visible)
	{
		if (this.visible == visible) { return; }
		if (visible)
		{
			GuiUtil.getStage().addActor(topPanel);
		}
		else
		{
			topPanel.remove();
			centerButton.remove();
		}
		this.visible = visible;
	}
	
	public void showNaviCenterButton()
	{
		centerButton.remove(); // Ensure not already showing.
		GuiUtil.getStage().addActor(centerButton);
	}
	
//	void initBottomPanel()
//	{
//		bottomPanel.addActor(bottomFromButton);
//		bottomPanel.addActor(bottomToButton);
//		bottomFromButton.addListener(GuiUtil.wrapClickListener((a,x,y) -> onFromToClick(true)));
//		bottomToButton.addListener(GuiUtil.wrapClickListener((a,x,y) -> onFromToClick(false)));
//	}
	
//	void onFromToClick(final boolean from)
//	{
//		System.out.println("Pressed from=true or to=false: " + from);
//		//TODO: ActionListener --> Show ListView and allow to set.
//	}
}
