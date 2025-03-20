package com.starcom.pocketmaps.navigator;

import com.starcom.navigation.MapRoutingEngine.Instruct;
import com.starcom.navigation.MapRoutingEngine.Sign;
import com.starcom.pocketmaps.Cfg;
import com.starcom.pocketmaps.Icons.R;
import com.starcom.pocketmaps.Cfg.NavKey;
import com.starcom.pocketmaps.Cfg.NavKeyB;
import com.starcom.pocketmaps.navigator.Navigator;
import com.starcom.pocketmaps.text.Text;
import com.starcom.pocketmaps.util.UnitCalculator;

public class NaviInstruction
{
  String curStreet;
  String nextInstruction;
  String nextInstructionShort;
  String nextInstructionShortFallback;
  long fullTime;
  String fullTimeString;
  double nextDistance;
  Sign nextSign;
  int nextSignResource;
  Text naviText;
  
  public NaviInstruction(Instruct in, Instruct nextIn, long fullTime)
  {
    naviText = Text.getInstance();
    if (nextIn != null)
    {
      nextSign = nextIn.sign;
      nextSignResource = Navigator.getNavigator().getDirectionSignHuge(nextIn);
      nextInstruction = Navigator.getNavigator().getDirectionDescription(nextIn, true);
      nextInstructionShort = Navigator.getNavigator().getDirectionDescription(nextIn, false);
      nextInstructionShortFallback = Navigator.getNavigator().getDirectionDescriptionFallback(nextIn, false);
    }
    else
    {
      nextSign = in.sign; // Finished?
      nextSignResource = Navigator.getNavigator().getDirectionSignHuge(in);
      nextInstruction = Navigator.getNavigator().getDirectionDescription(in, true);
      nextInstructionShort = Navigator.getNavigator().getDirectionDescription(in, false);
      nextInstructionShortFallback = Navigator.getNavigator().getDirectionDescriptionFallback(in, false);
    }
    if (nextSignResource == 0) { nextSignResource = R.ic_2x_continue_on_street.getInt(); }
    nextDistance = in.distance;
    this.fullTime = fullTime;
    fullTimeString = Navigator.getTimeString(fullTime);
    curStreet = in.name;
    if (curStreet == null) { curStreet = ""; }
  }
  
  /** The full time of instructions in ms */
  public long getFullTime() { return fullTime; }
  /** Get the distance to next instruction.
   * @return the next distance in meters. */
  public double getNextDistance() { return nextDistance; }
  /** @return A nice time string (hours and minutes) from timeMS */
  public String getFullTimeString() { return fullTimeString; }
  public int getNextSignResource() { return nextSignResource; }
  public Sign getNextSign() { return nextSign; }
  public String getCurStreet() { return curStreet; }
  public String getNextInstruction() { return nextInstruction; }
  /** Returns a nice string representation of nextDistance with unit included. */
  public String getNextDistanceString()
  {
    if (nextDistance > (UnitCalculator.METERS_OF_KM * 10.0))
    {
      String unit = " " + UnitCalculator.getUnit(true);
      return UnitCalculator.getBigDistance(nextDistance, 0) + unit;
    }
    String unit = " " + UnitCalculator.getUnit(false);
    return UnitCalculator.getShortDistance(nextDistance) + unit;
  }


  public void updateDist(double partDistance)
  {
    nextDistance = partDistance;
  }


  public String getVoiceText()
  {
    String unit = " " + naviText.getNavivoiceMeters() + ". ";
    int roundetDistance = (int)nextDistance;
    if (Cfg.getBoolValue(NavKeyB.IsImperialUnit, false))
    {
      unit = " " + naviText.getNavivoiceFeet() + ". ";
      roundetDistance = (int)(nextDistance / UnitCalculator.METERS_OF_FEET);
    }
    roundetDistance = roundetDistance/10;
    roundetDistance = roundetDistance * 10;
    return naviText.getNavivoiceIn() + " " + roundetDistance + unit + nextInstructionShort;
  }
  
  public String getVoiceTextFallback()
  {
    String unit = " meters. ";
    int roundetDistance = (int)nextDistance;
    if (Cfg.getBoolValue(NavKeyB.IsImperialUnit, false))
    {
      unit = " feet. ";
      roundetDistance = (int)(nextDistance / UnitCalculator.METERS_OF_FEET);
    }
    roundetDistance = roundetDistance/10;
    roundetDistance = roundetDistance * 10;
    return "In " + roundetDistance + unit + nextInstructionShortFallback;
  }
}
