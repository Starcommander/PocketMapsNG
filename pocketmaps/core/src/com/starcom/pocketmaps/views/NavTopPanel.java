package com.starcom.pocketmaps.views;

import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.starcom.gdx.ui.AbsLayout;
import com.starcom.gdx.ui.GuiUtil;
import com.starcom.pocketmaps.Cfg;
import com.starcom.pocketmaps.Cfg.NavKeyB;
import com.starcom.pocketmaps.Icons;
import com.starcom.pocketmaps.Icons.R;
import com.starcom.pocketmaps.navigator.NaviEngine;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

public class NavTopPanel
{
	private static NavTopPanel instance = new NavTopPanel();
	
	AbsLayout topPanel = new AbsLayout(0, 0, 1, 0.2f);
	AbsLayout botPanel = new AbsLayout(0, 0.8f, 1, 0.2f);
	private final Label topStreetLabel = new Label("Street123", GuiUtil.getDefaultSkin());
	private final Label topInstructionLabel = new Label("---", GuiUtil.getDefaultSkin());
	private final Label topDistanceLabel = new Label("0 m", GuiUtil.getDefaultSkin());
	private final Label topTimeLabel = new Label("0 min", GuiUtil.getDefaultSkin());
	private final Label speedLabel = new Label("(---km/h)", GuiUtil.getDefaultSkin());
	private Actor closeButton = GuiUtil.genButton("[X]", 30, 30, (a,x,y) -> onStopNaviSelected());
	private Actor centerButton = GuiUtil.genButton("[O]", 30, 30, (a,x,y) -> onCenterButtonPressed());
	private boolean visible = false;
	
	private NavTopPanel()
	{
		Image icon = Icons.generateIcon(R.ic_2x_finish_flag);
		topPanel.setMinHeight(30);
		Actor aPan = GuiUtil.genPanel(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getWidth());
		topPanel.addChild(aPan, 0, 0, 1, 1);
		topPanel.addChild(icon, 0, 0, 0.2f, 1.0f).keepRatioW();
		topPanel.addChild(topDistanceLabel, 0.3f, 0, 0.4f, 0.4f);
		topPanel.addChild(topInstructionLabel, 0.3f, 0.6f, 0.4f, 0.4f);
		topPanel.addChild(topStreetLabel, 0.7f, 0, 0.4f, 0.4f);
		topPanel.addChild(topTimeLabel, 0.7f, 0.6f, 0.4f, 0.4f);
		topPanel.addChild(closeButton, 0.9f, 0.1f, 0.1f, 0.3f);
	}
	
	public static NavTopPanel getInstance()
	{
		return instance;
	}
	
	private void onStopNaviSelected()
	{
		setVisible(false);
		TopPanel.getInstance().setVisible(true);
		NaviEngine.getNaviEngine().setNavigating(null, false);
		com.starcom.pocketmaps.map.MapHandler.getInstance().setCustomPoint(null);
		com.starcom.pocketmaps.map.MapHandler.getInstance().resetTilt(0);
	}
	
	private void onCenterButtonPressed()
	{
		centerButton.remove();
		NaviEngine.getNaviEngine().setMapUpdatesAllowed(true);
	}
	
	public Label getSpeedLabel() { return speedLabel; }
	
	public void updateInstruction(String street, String distance, String instr, Icons.R icon, String time)
	{
		topStreetLabel.setText(street);
		topDistanceLabel.setText(distance);
		topInstructionLabel.setText(instr);
		topTimeLabel.setText(time);
		Image image = Icons.generateIcon(icon);
		topPanel.replaceChild(1, image, 0, 0, 0.2f, 1.0f).keepRatioW().setDebug("Icon");
	}

	public void setVisible(boolean visible)
	{
		if (this.visible == visible) { return; }
		if (visible)
		{
//			Actor pan = GuiUtil.genPanel(0, (int)(Gdx.graphics.getHeight() * 0.86f), Gdx.graphics.getWidth(), (int)(Gdx.graphics.getHeight() * 0.15f));
//			GuiUtil.getStage().addActor(pan);
			GuiUtil.getStage().addActor(topPanel);
			GuiUtil.getStage().addActor(botPanel);
			if (Cfg.getBoolValue(NavKeyB.ShowingSpeedLimits, true))
			{
				botPanel.addChild(speedLabel, 0, 0, 0.3f, 1);
			}
		}
		else
		{
			topPanel.remove();
			botPanel.clearChild(centerButton); // Ensure not showing.
			botPanel.clearChild(speedLabel); // Ensure not showing.
			botPanel.remove();
		}
		this.visible = visible;
	}
	
	public void showNaviCenterButton()
	{
		botPanel.clearChild(centerButton); // Ensure not already showing.
		botPanel.addChild(centerButton, 0.7f, 0.2f, 0.2f, 0.4f);
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
