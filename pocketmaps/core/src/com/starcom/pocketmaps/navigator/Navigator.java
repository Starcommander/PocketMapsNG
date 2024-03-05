package com.starcom.pocketmaps.navigator;

import com.graphhopper.PathWrapper;
import com.graphhopper.util.Helper;
import com.graphhopper.util.Instruction;
import com.starcom.pocketmaps.Icons.R;
import com.starcom.pocketmaps.text.Text;
import com.starcom.pocketmaps.util.UnitCalculator;
import com.starcom.pocketmaps.views.MapList;
import com.starcom.pocketmaps.Cfg;
import com.starcom.pocketmaps.Cfg.ConfType;
import com.starcom.pocketmaps.Cfg.NavKey;
import com.starcom.pocketmaps.Cfg.NavKeyB;

import java.util.ArrayList;
import java.util.List;

/**
 * singleton class
 * <p/>
 * Handler navigation information
 * <p/>
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 19, 2015.
 */
public class Navigator {
    /**
     * get from MapHandler calculate path
     */
    private PathWrapper ghResponse;
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

    public PathWrapper getGhResponse() {
        return ghResponse;
    }

    public void setGhResponse(PathWrapper ghResponse) {
        this.ghResponse = ghResponse;
        if (NaviEngine.getNaviEngine().isNavigating())
        {
          NaviEngine.getNaviEngine().onUpdateInstructions(ghResponse.getInstructions(), ghResponse.getPathDetails());
        }
        else if (Cfg.getBoolValue(NavKeyB.DirectionsOn, true))
        {
          MapList.viewDirectionList(ghResponse.getInstructions()); // Navigator.getNavigator().setGhResponse(resp);
        }
        else
        {
//          NaviEngine.getNaviEngine().setNavigating(aaa, true); //TODO
        }
    }

    /**
     * @param distance (<li>Instruction: return instructions distance </li>
     * @return a string  0.0 km (Exact one decimal place)
     */
    public String getDistance(Instruction distance) {
        if (distance.getSign() == Instruction.FINISH) return "";
        double d = distance.getDistance();
        return UnitCalculator.getString(d);
    }

    /**
     * @return distance of the whole journey
     */
    public String getDistance() {
        if (getGhResponse() == null) return UnitCalculator.getString(0);
        double d = getGhResponse().getDistance();
        return UnitCalculator.getString(d);
    }

    /**
     * @return a string time of the journey H:MM
     */
    public String getTime() {
        if (getGhResponse() == null) return " ";
        return getTimeString(getGhResponse().getTime());
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
    public String getTime(Instruction time) {
        return Math.round(getGhResponse().getTime() / 60000) + " min";
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
    
    public void setNaviStart(Object activity, boolean on) {
        NaviEngine.getNaviEngine().setNavigating(activity, on);
//        for (NavigatorListener listener : listeners) {
//            listener.onNaviStart(on);
//        }
    }

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
            for (Instruction i : ghResponse.getInstructions()) {
              sb.append("------>\ntime <long>: " + i.getTime());
              sb.append("\n");
              sb.append("name: street name" + i.getName());
              sb.append("\n");
              sb.append("annotation <InstructionAnnotation>");
              sb.append(i.getAnnotation().toString());
              sb.append("\n");
              sb.append("distance");
              sb.append(i.getDistance() + "\n");
              sb.append("sign <int>:" + i.getSign());
              sb.append("\n");
              sb.append("Points <PointsList>: " + i.getPoints());
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
    public int getDirectionSign(Instruction itemData) {
        switch (itemData.getSign()) {
            case Instruction.LEAVE_ROUNDABOUT:
                return R.ic_roundabout.getInt();
            case Instruction.TURN_SHARP_LEFT:
                return R.ic_turn_sharp_left.getInt();
            case Instruction.TURN_LEFT:
                return R.ic_turn_left.getInt();
            case Instruction.TURN_SLIGHT_LEFT:
                return R.ic_turn_slight_left.getInt();
            case Instruction.CONTINUE_ON_STREET:
                return R.ic_continue_on_street.getInt();
            case Instruction.TURN_SLIGHT_RIGHT:
                return R.ic_turn_slight_right.getInt();
            case Instruction.TURN_RIGHT:
                return R.ic_turn_right.getInt();
            case Instruction.TURN_SHARP_RIGHT:
                return R.ic_turn_sharp_right.getInt();
            case Instruction.FINISH:
                return R.ic_finish_flag.getInt();
            case Instruction.REACHED_VIA:
                return R.ic_reached_via.getInt();
            case Instruction.USE_ROUNDABOUT:
                return R.ic_roundabout.getInt();
            case Instruction.KEEP_RIGHT:
                return R.ic_keep_right.getInt();
            case Instruction.KEEP_LEFT:
                return R.ic_keep_left.getInt();
        }
        return 0;
    }
    
    /**
     * @param itemData
     * @return int resId to instruction direction's sign icon
     */
    public int getDirectionSignHuge(Instruction itemData) {
        switch (itemData.getSign()) {
            case Instruction.LEAVE_ROUNDABOUT:
                return R.ic_2x_roundabout.getInt();
            case Instruction.TURN_SHARP_LEFT:
                return R.ic_2x_turn_sharp_left.getInt();
            case Instruction.TURN_LEFT:
                return R.ic_2x_turn_left.getInt();
            case Instruction.TURN_SLIGHT_LEFT:
                return R.ic_2x_turn_slight_left.getInt();
            case Instruction.CONTINUE_ON_STREET:
                return R.ic_2x_continue_on_street.getInt();
            case Instruction.TURN_SLIGHT_RIGHT:
                return R.ic_2x_turn_slight_right.getInt();
            case Instruction.TURN_RIGHT:
                return R.ic_2x_turn_right.getInt();
            case Instruction.TURN_SHARP_RIGHT:
                return R.ic_2x_turn_sharp_right.getInt();
            case Instruction.FINISH:
                return R.ic_2x_finish_flag.getInt();
            case Instruction.REACHED_VIA:
                return R.ic_2x_reached_via.getInt();
            case Instruction.USE_ROUNDABOUT:
                return R.ic_2x_roundabout.getInt();
            case Instruction.KEEP_RIGHT:
                return R.ic_2x_keep_right.getInt();
            case Instruction.KEEP_LEFT:
              return R.ic_2x_keep_left.getInt();
        }
        return 0;
    }

    /**
     * @param instruction
     * @return direction
     */
    public String getDirectionDescription(Instruction instruction, boolean longText) {
        if (instruction.getSign() == 4) return Text.getInstance().getNavivoiceNavend();
        String str;
        String streetName = instruction.getName();
        int sign = instruction.getSign();
        String dir = "";
        String dirTo = Text.getInstance().getNavivoiceOnto();
        switch (sign) {
            case Instruction.CONTINUE_ON_STREET:
                dir = (Text.getInstance().getNavivoiceContinue());
                dirTo = Text.getInstance().getNavivoiceOn();
                break;
            case Instruction.LEAVE_ROUNDABOUT:
                dir = (Text.getInstance().getNavivoiceLeaveround());
                break;
            case Instruction.TURN_SHARP_LEFT:
                dir = (Text.getInstance().getNavivoiceTurnxxx().replace("xxx", Text.getInstance().getNavivoiceSharpl()));
                break;
            case Instruction.TURN_LEFT:
                dir = (Text.getInstance().getNavivoiceTurnxxx().replace("xxx", Text.getInstance().getNavivoiceLeft()));
                break;
            case Instruction.TURN_SLIGHT_LEFT:
                dir = (Text.getInstance().getNavivoiceTurnxxx().replace("xxx", Text.getInstance().getNavivoiceSlightl()));
                break;
            case Instruction.TURN_SLIGHT_RIGHT:
                dir = (Text.getInstance().getNavivoiceTurnxxx().replace("xxx", Text.getInstance().getNavivoiceSlightr()));
                break;
            case Instruction.TURN_RIGHT:
                dir = (Text.getInstance().getNavivoiceTurnxxx().replace("xxx", Text.getInstance().getNavivoiceRight()));
                break;
            case Instruction.TURN_SHARP_RIGHT:
                dir = (Text.getInstance().getNavivoiceTurnxxx().replace("xxx", Text.getInstance().getNavivoiceSharpr()));
                break;
            case Instruction.REACHED_VIA:
                dir = ("Reached via");
                break;
            case Instruction.USE_ROUNDABOUT:
                dir = (Text.getInstance().getNavivoiceUseround());
                break;
            case Instruction.KEEP_LEFT:
              dir = (Text.getInstance().getNavivoiceKeepxxx().replace("xxx", Text.getInstance().getNavivoiceLeft()));
              break;
            case Instruction.KEEP_RIGHT:
              dir = (Text.getInstance().getNavivoiceKeepxxx().replace("xxx", Text.getInstance().getNavivoiceRight()));
              break;
        }
        if (!longText) { return dir; }
        str = Helper.isEmpty(streetName) ? dir : (dir + " " + dirTo + " " + streetName);
        return str;
    }
    
    /**
     * @param instruction
     * @return direction
     */
    public String getDirectionDescriptionFallback(Instruction instruction, boolean longText) {
        if (instruction.getSign() == 4) return "Navigation End";
        String str;
        String streetName = instruction.getName();
        int sign = instruction.getSign();
        String dir = "";
        String dirTo = "onto";
        switch (sign) {
            case Instruction.CONTINUE_ON_STREET:
                dir = ("Continue");
                dirTo = "on";
                break;
            case Instruction.LEAVE_ROUNDABOUT:
                dir = ("Leave roundabout");
                break;
            case Instruction.TURN_SHARP_LEFT:
                dir = ("Turn sharp left");
                break;
            case Instruction.TURN_LEFT:
                dir = ("Turn left");
                break;
            case Instruction.TURN_SLIGHT_LEFT:
                dir = ("Turn slight left");
                break;
            case Instruction.TURN_SLIGHT_RIGHT:
                dir = ("Turn slight right");
                break;
            case Instruction.TURN_RIGHT:
                dir = ("Turn right");
                break;
            case Instruction.TURN_SHARP_RIGHT:
                dir = ("Turn sharp right");
                break;
            case Instruction.REACHED_VIA:
                dir = ("Reached via");
                break;
            case Instruction.USE_ROUNDABOUT:
                dir = ("Use roundabout");
                break;
            case Instruction.KEEP_LEFT:
              dir = ("Keep left");
              break;
            case Instruction.KEEP_RIGHT:
              dir = ("Keep right");
              break;
        }
        if (!longText) { return dir; }
        str = Helper.isEmpty(streetName) ? dir : (dir + " " + dirTo + " " + streetName);
        return str;
    }

}
