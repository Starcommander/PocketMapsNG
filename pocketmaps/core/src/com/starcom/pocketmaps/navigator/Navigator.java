package com.starcom.pocketmaps.navigator;

import com.starcom.pocketmaps.Icons.R;
import com.starcom.pocketmaps.text.Text;
import com.starcom.pocketmaps.util.UnitCalculator;
import com.starcom.pocketmaps.views.MapList;
import com.starcom.navigation.MapRoutingEngine.Instruct;
import com.starcom.navigation.MapRoutingEngine.NaviResponse;
import com.starcom.navigation.MapRoutingEngine.Sign;
import com.starcom.pocketmaps.Cfg;
import com.starcom.pocketmaps.Cfg.ConfType;
import com.starcom.pocketmaps.Cfg.NavKey;
import com.starcom.pocketmaps.Cfg.NavKeyB;

import java.util.ArrayList;
import java.util.List;

/**
 * singleton class
 * <p/>
 * Handler navigation information (text and icons)
 * <p/>
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 19, 2015.
 */
public class Navigator {
    /**
     * get from MapHandler calculate path
     */
    private NaviResponse ghResponse;
    /**
     * navigator is on or off
     */
    private boolean on;
//    private List<NavigatorListener> listeners;
    private static Navigator navigator = null;


    private Navigator() {
        this.ghResponse = null;
        this.on = false;
//        this.listeners = new ArrayList<>();
    }

    public static void reset(){
        navigator = new Navigator();
    }

    /**
     * @return Navigator object
     */
    public static Navigator getNavigator() {
        if (navigator == null) {
            navigator = new Navigator();
        }
        return navigator;
    }

    public NaviResponse getResponse() {
        return ghResponse;
    }

    public void setGhResponse(NaviResponse ghResponse) {
        this.ghResponse = ghResponse;
        if (NaviEngine.getNaviEngine().isNavigating())
        {
            NaviEngine.getNaviEngine().onUpdateInstructions(ghResponse);
        }
        else if (Cfg.getBoolValue(NavKeyB.DirectionsOn, false))
        {
            MapList.viewDirectionList(ghResponse.getInstructions());
        }
        else
        {
        	NaviEngine.getNaviEngine().setNavigating(true);
        }
    }

    /**
     * @param distance (<li>Instruction: return instructions distance </li>
     * @return a string  0.0 km (Exact one decimal place)
     */
    public String getDistance(Instruct distance) {
        if (distance.sign == Sign.Finish) return "";
        double d = distance.distance;
        return UnitCalculator.getString(d);
    }

    /**
     * @return distance of the whole journey
     */
    public String getDistance() {
        if (getResponse() == null) return UnitCalculator.getString(0);
        double d = getResponse().getDistance();
        return UnitCalculator.getString(d);
    }

    /**
     * @return a string time of the journey H:MM
     */
    public String getTime() {
        if (getResponse() == null) return " ";
        return getTimeString(getResponse().getTime());
    }
    
    /** @param time Time in ms.
     * @return a nice time string (hours and minutes) from timeMS */
    public static String getTimeString(long time)
    {
      int t = Math.round(time / 60000);
      if (t < 60) return t + " min";
      return t / 60 + " h: " + t % 60 + " m";
    }

    /**
     * @return a string time of the instruction min
     */
    public String getTime(Instruct time) {
        return Math.round(getResponse().getTime() / 60000) + " min";
    }


    /**
     * @return true is navigator is on
     */
//    public boolean isOn() {
//        return on;
//    }

//    /**
//     * set navigator on or off
//     *
//     * @param on
//     */
//    public void setOn(boolean on) {
//        this.on = on;
////        broadcast();
//    }
    
//    public void setNaviStart(Object activity, boolean on) {
//        NaviEngine.getNaviEngine().setNavigating(activity, on);
////        for (NavigatorListener listener : listeners) {
////            listener.onNaviStart(on);
////        }
//    }

//    /**
//     * broadcast changes to listeners
//     */
//    protected void broadcast() {
//        for (NavigatorListener listener : listeners) {
//            listener.onStatusChanged(isOn());
//        }
//    }

//    /**
//     * add listener to listener list
//     *
//     * @param listener
//     */
//    public void addListener(NavigatorListener listener) {
//        listeners.add(listener);
//    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (ghResponse.getInstructions() != null) {
            for (Instruct i : ghResponse.getInstructions()) {
              sb.append("------>\ntime <long>: " + i.time);
              sb.append("\n");
              sb.append("name: street name" + i.name);
              sb.append("\n");
              sb.append("annotation <InstructionAnnotation>");
              sb.append(i.annotation);
              sb.append("\n");
              sb.append("distance");
              sb.append(i.distance + "\n");
              sb.append("sign <Sign>:" + i.sign);
              sb.append("\n");
              sb.append("Points <PointsList>: " + i.points);
              sb.append("\n");
            }
        }
        return sb.toString();
    }


    /**
     * Returns icon resource.
     * @param dark (ask for dark icon resId ?)
     * @return int resId
     */
    public int getTravelModeResId(boolean dark) {
        if (dark)
        {
          if (Cfg.getValue(NavKey.TravelMode, null).toString().equals(Cfg.TRAVEL_MODE_FOOT))
          {
            return R.ic_directions_walk_orange_24dp.getInt();
          }
          else if (Cfg.getValue(NavKey.TravelMode, null).toString().equals(Cfg.TRAVEL_MODE_BIKE))
          {
            return R.ic_directions_bike_orange_24dp.getInt();
          }
          else if (Cfg.getValue(NavKey.TravelMode, null).toString().equals(Cfg.TRAVEL_MODE_CAR))
          {
            return R.ic_directions_car_orange_24dp.getInt();
          }
        }
        else
        {
          if (Cfg.getValue(NavKey.TravelMode, null).toString().equals(Cfg.TRAVEL_MODE_FOOT))
          {
            return R.ic_directions_walk_white_24dp.getInt();
          }
          else if (Cfg.getValue(NavKey.TravelMode, null).toString().equals(Cfg.TRAVEL_MODE_BIKE))
          {
            return R.ic_directions_bike_white_24dp.getInt();
          }
          else if (Cfg.getValue(NavKey.TravelMode, null).toString().equals(Cfg.TRAVEL_MODE_CAR))
          {
            return R.ic_directions_car_white_24dp.getInt();
          }
        }
        throw new NullPointerException("this method can only used when Variable class is ready!");
    }
    
    public int getTravelModeArrayIndex()
    {
      if (Cfg.getValue(NavKey.TravelMode, null).toString().equals(Cfg.TRAVEL_MODE_FOOT))
      {
        return 0;
      }
      else if (Cfg.getValue(NavKey.TravelMode, null).toString().equals(Cfg.TRAVEL_MODE_BIKE))
      {
        return 1;
      }
      else if (Cfg.getValue(NavKey.TravelMode, null).toString().equals(Cfg.TRAVEL_MODE_CAR))
      {
        return 2;
      }
      throw new NullPointerException("Non existing travel mode.");
    }
    
    public boolean setTravelModeArrayIndex(int index) {
      String selected;
      switch (index) {
          case 0:
              selected = Cfg.TRAVEL_MODE_FOOT;
              break;
          case 1:
              selected = Cfg.TRAVEL_MODE_BIKE;
              break;
          default:
              selected = Cfg.TRAVEL_MODE_CAR;
      }
      Cfg.setValue(NavKey.TravelMode, selected);
      return Cfg.save(ConfType.Navigation);
    }

    /**
     * @param itemData
     * @return int resId to instruction direction's sign icon
     */
    public int getDirectionSign(Instruct itemData) {
        switch (itemData.sign) {
            case LeaveRoundabout:
                return R.ic_roundabout.getInt();
            case TurnSharpLeft:
                return R.ic_turn_sharp_left.getInt();
            case TurnLeft:
                return R.ic_turn_left.getInt();
            case TurnSlightLeft:
                return R.ic_turn_slight_left.getInt();
            case ContinueOnStreet:
                return R.ic_continue_on_street.getInt();
            case TurnSlightRight:
                return R.ic_turn_slight_right.getInt();
            case TurnRight:
                return R.ic_turn_right.getInt();
            case TurnSharpRight:
                return R.ic_turn_sharp_right.getInt();
            case Finish:
                return R.ic_finish_flag.getInt();
            case ReachedVia:
                return R.ic_reached_via.getInt();
            case UseRoundabout:
                return R.ic_roundabout.getInt();
            case KeepRight:
                return R.ic_keep_right.getInt();
            case KeepLeft:
                return R.ic_keep_left.getInt();
        }
        return 0;
    }
    
    /**
     * @param itemData
     * @return int resId to instruction direction's sign icon
     */
    public int getDirectionSignHuge(Instruct itemData) {
        switch (itemData.sign) {
            case LeaveRoundabout:
                return R.ic_2x_roundabout.getInt();
            case TurnSharpLeft:
                return R.ic_2x_turn_sharp_left.getInt();
            case TurnLeft:
                return R.ic_2x_turn_left.getInt();
            case TurnSlightLeft:
                return R.ic_2x_turn_slight_left.getInt();
            case ContinueOnStreet:
                return R.ic_2x_continue_on_street.getInt();
            case TurnSlightRight:
                return R.ic_2x_turn_slight_right.getInt();
            case TurnRight:
                return R.ic_2x_turn_right.getInt();
            case TurnSharpRight:
                return R.ic_2x_turn_sharp_right.getInt();
            case Finish:
                return R.ic_2x_finish_flag.getInt();
            case ReachedVia:
                return R.ic_2x_reached_via.getInt();
            case UseRoundabout:
                return R.ic_2x_roundabout.getInt();
            case KeepRight:
                return R.ic_2x_keep_right.getInt();
            case KeepLeft:
              return R.ic_2x_keep_left.getInt();
        }
        return 0;
    }

    /**
     * @param instruction
     * @return direction
     */
    public String getDirectionDescription(Instruct instruction, boolean longText) {
        if (instruction.sign == Sign.Finish) return Text.getInstance().getNavivoiceNavend();
        String str;
        String streetName = instruction.name;
        Sign sign = instruction.sign;
        String dir = "";
        String dirTo = Text.getInstance().getNavivoiceOnto();
        switch (sign) {
            case ContinueOnStreet:
                dir = (Text.getInstance().getNavivoiceContinue());
                dirTo = Text.getInstance().getNavivoiceOn();
                break;
            case LeaveRoundabout:
                dir = (Text.getInstance().getNavivoiceLeaveround());
                break;
            case TurnSharpLeft:
                dir = (Text.getInstance().getNavivoiceTurnxxx().replace("xxx", Text.getInstance().getNavivoiceSharpl()));
                break;
            case TurnLeft:
                dir = (Text.getInstance().getNavivoiceTurnxxx().replace("xxx", Text.getInstance().getNavivoiceLeft()));
                break;
            case TurnSlightLeft:
                dir = (Text.getInstance().getNavivoiceTurnxxx().replace("xxx", Text.getInstance().getNavivoiceSlightl()));
                break;
            case TurnSlightRight:
                dir = (Text.getInstance().getNavivoiceTurnxxx().replace("xxx", Text.getInstance().getNavivoiceSlightr()));
                break;
            case TurnRight:
                dir = (Text.getInstance().getNavivoiceTurnxxx().replace("xxx", Text.getInstance().getNavivoiceRight()));
                break;
            case TurnSharpRight:
                dir = (Text.getInstance().getNavivoiceTurnxxx().replace("xxx", Text.getInstance().getNavivoiceSharpr()));
                break;
            case ReachedVia:
                dir = ("Reached via");
                break;
            case UseRoundabout:
                dir = (Text.getInstance().getNavivoiceUseround());
                break;
            case KeepLeft:
              dir = (Text.getInstance().getNavivoiceKeepxxx().replace("xxx", Text.getInstance().getNavivoiceLeft()));
              break;
            case KeepRight:
              dir = (Text.getInstance().getNavivoiceKeepxxx().replace("xxx", Text.getInstance().getNavivoiceRight()));
              break;
        }
        if (!longText) { return dir; }
        if (streetName == null || streetName.isEmpty()) { str = dir; }
        else { str = dir + " " + dirTo + " " + streetName; }
        return str;
    }
    
    /**
     * @param instruction
     * @return direction
     */
    public String getDirectionDescriptionFallback(Instruct instruction, boolean longText) {
        if (instruction.sign == Sign.Finish) return "Navigation End";
        String str;
        String streetName = instruction.name;
        Sign sign = instruction.sign;
        String dir = "";
        String dirTo = "onto";
        switch (sign) {
            case ContinueOnStreet:
                dir = ("Continue");
                dirTo = "on";
                break;
            case LeaveRoundabout:
                dir = ("Leave roundabout");
                break;
            case TurnSharpLeft:
                dir = ("Turn sharp left");
                break;
            case TurnLeft:
                dir = ("Turn left");
                break;
            case TurnSlightLeft:
                dir = ("Turn slight left");
                break;
            case TurnSlightRight:
                dir = ("Turn slight right");
                break;
            case TurnRight:
                dir = ("Turn right");
                break;
            case TurnSharpRight:
                dir = ("Turn sharp right");
                break;
            case ReachedVia:
                dir = ("Reached via");
                break;
            case UseRoundabout:
                dir = ("Use roundabout");
                break;
            case KeepLeft:
              dir = ("Keep left");
              break;
            case KeepRight:
              dir = ("Keep right");
              break;
        }
        if (!longText) { return dir; }
        if (streetName == null || streetName.isEmpty()) { str = dir; }
        else { str = dir + " " + dirTo + " " + streetName; }
        return str;
    }

}
