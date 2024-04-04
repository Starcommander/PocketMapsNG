package com.starcom.pocketmaps.util;

import com.graphhopper.util.details.PathDetail;
import com.starcom.pocketmaps.navigator.Navigator;
import com.starcom.pocketmaps.views.NavTopPanel;
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
    List<PathDetail> maxSpeedList;
    List<PathDetail> aveSpeedList;
    
//    TextView view;
    boolean enabled = false;
    int pointsDone = 0;
    
    public SpeedUtil() {}
    
    /** Shows the text as sign.
     * @param index The path-index for showing text */
    public void showTextSign(int pIndex)
    {
    	NavTopPanel.getInstance().speedLabel.setVisible(enabled);
    	if (!enabled) { return; }
        String txt = getSpeedValue(pIndex + pointsDone);
        NavTopPanel.getInstance().speedLabel.setText(txt);
    }

    
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    	NavTopPanel.getInstance().speedLabel.setVisible(enabled);
    }
//    
//    private void updateViewVis()
//    {
//        if (enabled) { view.setVisibility(View.VISIBLE); }
//        else { view.setVisibility(View.GONE); }
//    }
    
    /** Resets the path-list, and sets the progress (pointsDone) to 0. */
    public void updateList(Map<String, List<PathDetail>> pathDetails)
    {
        maxSpeedList = pathDetails.get("max_speed");
        aveSpeedList = pathDetails.get("average_speed");
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
        for (PathDetail curDetail : maxSpeedList)
        {
            if (curDetail.getFirst() > pos) { continue; }
            if (curDetail.getLast() < pos) { continue; }
            if (curDetail.getValue()==null) { continue; }
            return "" + getUnitIntValue(Double.parseDouble(curDetail.getValue().toString()));
        }
        for (PathDetail curDetail : aveSpeedList)
        {
            if (curDetail.getFirst() > pos) { continue; }
            if (curDetail.getLast() < pos) { continue; }
            if (curDetail.getValue()==null) { continue; }
            return "" + getUnitIntValue(Double.parseDouble(curDetail.getValue().toString())) + "?";
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
