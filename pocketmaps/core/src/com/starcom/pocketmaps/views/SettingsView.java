package com.starcom.pocketmaps.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.starcom.gdx.ui.Dialogs;
import com.starcom.gdx.ui.GuiUtil;
import com.starcom.gdx.ui.ListSelect;
import com.starcom.pocketmaps.Cfg;
import com.starcom.pocketmaps.Cfg.ConfType;
import com.starcom.pocketmaps.Cfg.NavKeyB;
import com.starcom.pocketmaps.navigator.NaviDebugSimulator;
import com.starcom.pocketmaps.navigator.NaviEngine;

public class SettingsView
{
	private static final String SEL_DEBUG_BUTTON = "Debugging";
	private static final String SEL_DEBUG_SIMU = "DebugSimulation";
	private static final String SEL_DEBUG_STAT = "DebugStatistics";
	private static final String SEL_GPS_ON = "GPS on";
	
	Actor debugButton;
	Dialog debugStatWindow;
	
	private static SettingsView instance = new SettingsView();
	
	public static SettingsView getInstance() { return instance; }
	
	private SettingsView()
	{
		onToggleDebug(Cfg.getBoolValue(NavKeyB.Debugging, false));
	}
	
	private void onToggleDebug(boolean status)
	{

		if (!status && debugButton != null)
		{
			debugButton.remove();
			debugButton = null;
		}
		else if (status)
		{
			debugButton = GuiUtil.genButton(">(O)<", 50, Gdx.graphics.getHeight()/2, (a,x,y) -> showDebugSettings());
			GuiUtil.addActor(debugButton);
		}
	}
	
	public void showSettings()
	{
		ListSelect ll = new ListSelect("Settings", (b) -> onClose(b));
		ll.addCheckboxElement(SEL_DEBUG_BUTTON, (debugButton!=null), (a,x,y) -> onSelect(SEL_DEBUG_BUTTON));
		System.out.println("DebugButton: "+(debugButton!=null));
		if (debugButton!=null)
		{
			addDebugSettings(ll);
		}
		ll.showAsWindow(GuiUtil.getStage());
	}
	
	private void showDebugSettings()
	{
		ListSelect ll = new ListSelect("Settings", (b) -> onClose(b));
		addDebugSettings(ll);
		ll.showAsWindow(GuiUtil.getStage());
	}
	
	private void addDebugSettings(ListSelect ll)
	{
		boolean bool = NaviDebugSimulator.getSimu().isRunning();
		ll.addCheckboxElement(SEL_DEBUG_SIMU, bool, (a,x,y) -> onSelect(SEL_DEBUG_SIMU));
		bool = debugStatWindow != null && debugStatWindow.hasParent();
		ll.addCheckboxElement(SEL_DEBUG_STAT, bool, (a,x,y) -> onSelect(SEL_DEBUG_STAT));
		bool = Cfg.getBoolValue(NavKeyB.GpsOn, true);
		ll.addCheckboxElement(SEL_GPS_ON, bool, (a,x,y) -> onSelect(SEL_GPS_ON));
	}
	protected void onSelect(String selection)
	{
		if (selection == SEL_DEBUG_BUTTON)
		{
			boolean bool = Cfg.getBoolValue(NavKeyB.Debugging, false);
			Cfg.setBoolValue(NavKeyB.Debugging, !bool);
			Cfg.save(ConfType.Navigation);
			onToggleDebug(!bool);
		}
		else if (selection == SEL_DEBUG_SIMU)
		{
			if (NaviDebugSimulator.getSimu().isRunning())
			{
				NaviDebugSimulator.getSimu().stopDebugSimulator();
			}
			else
			{
				NaviDebugSimulator.getSimu().startDebugSimulator();
			}
		}
		else if (selection == SEL_DEBUG_STAT)
		{
			if (debugStatWindow != null && debugStatWindow.hasParent())
			{ // Already showing
				debugStatWindow.remove();
				return;
			}
			String msg = "Status: none";
			debugStatWindow = Dialogs.genDialog(GuiUtil.getStage(), "======== DebugStatistics ========", msg, false, (o) -> System.out.println("Debug window closed"));
			debugStatWindow.setModal(false);
			updateStatistics();
			debugStatWindow.show(GuiUtil.getStage());
			
		}
		else if (selection == SEL_GPS_ON)
		{
			boolean bool = Cfg.getBoolValue(NavKeyB.GpsOn, true);
			Cfg.setBoolValue(NavKeyB.GpsOn, !bool);
			Cfg.save(ConfType.Navigation);
		}
	}
	
	public void updateStatistics()
	{
		if (debugStatWindow != null && NaviEngine.getNaviEngine().isNavigating())
		{
			debugStatWindow.text(NaviEngine.getNaviEngine().getStatistics());
		}
	}
	
	protected void onClose(boolean commit)
	{
		System.out.println("Settings closed");
	}
}
