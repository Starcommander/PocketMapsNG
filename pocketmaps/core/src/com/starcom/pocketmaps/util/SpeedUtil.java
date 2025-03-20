package com.starcom.pocketmaps.util;

import com.starcom.pocketmaps.navigator.Navigator;
import com.starcom.pocketmaps.views.NavTopPanel;
import com.starcom.navigation.MapRoutingEngine.PathInfo;
import com.starcom.pocketmaps.Cfg;
import com.starcom.pocketmaps.Cfg.NavKey;
import com.starcom.pocketmaps.Cfg.NavKeyB;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Starcommander@github.com
 */
public class SpeedUtil
{
	ArrayList<PathInfo> maxSpeedList;
	ArrayList<PathInfo> aveSpeedList;
    
//    TextView view;
    boolean enabled = false;
    int pointsDone = 0;
    
    public SpeedUtil() {}
    
    /** Shows the text as sign.
     * @param index The path-index for showing text */
    public void showTextSign(int pIndex)
    {
    	NavTopPanel.getInstance().getSpeedLabel().setVisible(enabled);
    	if (!enabled) { return; }
        String txt = getSpeedValue(pIndex + pointsDone);
        NavTopPanel.getInstance().getSpeedLabel().setText(txt);
    }

    
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    	NavTopPanel.getInstance().getSpeedLabel().setVisible(enabled);
    }
//    
//    private void updateViewVis()
//    {
//        if (enabled) { view.setVisibility(View.VISIBLE); }
//        else { view.setVisibility(View.GONE); }
//    }
    
    /** Resets the path-list, and sets the progress (pointsDone) to 0. */
    public void updateList(ArrayList<PathInfo> maxSpeedList, ArrayList<PathInfo> aveSpeedList)
    {
        this.maxSpeedList = maxSpeedList;
        this.aveSpeedList = aveSpeedList;
        pointsDone = 0;
    }
    
    /** Adds finished instructions. */
    public void updateInstructionDone(int pLen)
    {
        pointsDone += pLen;
    }
    
    private String getSpeedValue(int pos)
    {
        if (maxSpeedList == null) { return "---"; }
        if (aveSpeedList == null) { return "---"; }
        for (PathInfo curDetail : maxSpeedList)
        {
            if (curDetail.first > pos) { continue; }
            if (curDetail.last < pos) { continue; }
            if (curDetail.value==null) { continue; }
            return "" + getUnitIntValue(Double.parseDouble(curDetail.value.toString()));
        }
        for (PathInfo curDetail : aveSpeedList)
        {
            if (curDetail.first > pos) { continue; }
            if (curDetail.last < pos) { continue; }
            if (curDetail.value==null) { continue; }
            return "" + getUnitIntValue(Double.parseDouble(curDetail.value.toString())) + "?";
        }
        return "---";
    }
    
    private int getUnitIntValue(double v)
    {
    	if (Cfg.getBoolValue(NavKeyB.IsImperialUnit, false))
        {
            v = v / (UnitCalculator.METERS_OF_MILE * 0.001);
            v = 5*(Math.round(v/5)); // Round to multible of 5
        }
        return (int)v;
    }

}
