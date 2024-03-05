package com.starcom.pocketmaps;

import java.util.Arrays;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.starcom.pocketmaps.views.VtmBitmap;

public class Icons {
	static final String prefix = "drawable-";
  public enum Res {
    XXXHDPI,
    XHDPI,
    MDPI,
    HDPI,
    XXHDPI; }

  public enum R {
    ic_map_black_24dp(0),
    ic_speed_sign(1),
    ic_turn_left(2),
    ic_2x_keep_left(3),
    ic_keyboard_arrow_left_white_24dp(4),
    ic_play_arrow_light_green_a700_24dp(5),
    ic_turn_right(6),
    ic_map_white_24dp(7),
    ic_2x_roundabout(8),
    ic_directions_car_orange_24dp(9),
    ic_pause_orange_24dp(10),
    ic_trending_up_light_green_a700_24dp(11),
    ic_my_location_white_24dp(12),
    ic_directions_white_24dp(13),
    ic_keep_right(14),
    ic_arrow_drop_down_white_18dp(15),
    ic_remove_white_24dp(16),
    ic_reached_via(17),
    ic_roundabout(18),
    ic_directions_bike_white_24dp(19),
    ic_location_searching_white_24dp(20),
    ic_continue_on_street(21),
    ic_more_vert_white_24dp(22),
    ic_filter_center_focus_white_24dp(23),
    ic_2x_continue_on_street(24),
    ic_2x_turn_sharp_left(25),
    ic_turn_slight_left(26),
    ic_star_outline_white_24dp(27),
    ic_turn_slight_right(28),
    ic_location_start_white_24dp(29),
    ic_space_bar_white_24dp(30),
    ic_stop_white_24dp(31),
    ic_2x_keep_right(32),
    ic_search_white_24dp(33),
    ic_directions_walk_white_24dp(34),
    ic_keyboard_arrow_down_white_24dp(35),
    ic_2x_reached_via(36),
    ic_keyboard_arrow_up_white_24dp(37),
    ic_2x_turn_slight_left(38),
    ic_3d_rotation_light_green_a700_24dp(39),
    ic_2x_turn_right(40),
    ic_location_end_24dp(41),
    ic_location_start_24dp(42),
    ic_settings_white_24dp(43),
    ic_cloud_download_white_24dp(44),
    ic_clear_white_24dp(45),
    ic_stop_orange_24dp(46),
    ic_location_end_white_24dp(47),
    ic_2x_turn_slight_right(48),
    ic_my_location_dark_24dp(49),
    ic_icon_pocketmaps(50),
    ic_directions_bike_orange_24dp(51),
    ic_directions_car_white_24dp(52),
    ic_directions_run_white_24dp(53),
    ic_add_white_24dp(54),
    ic_2x_turn_sharp_right(55),
    ic_2x_turn_left(56),
    ic_navigation_black_24dp(57),
    ic_cloud_download_black_24dp(58),
    ic_2x_finish_flag(59),
    ic_timer_white_24dp(60),
    ic_share_24dp(61),
    ic_directions_walk_orange_24dp(62),
    ic_turn_sharp_left(63),
    ic_navigation_white_24dp(64),
    ic_turn_sharp_right(65),
    ic_keep_left(66),
    ic_finish_flag(67),
    ic_swipe_finger_thump_black(68);

    private int val;
    private R(int val) { this.val = val; }
    public int getInt() { return val; }

    public static R valueOf(int value) {
        return (R)Arrays.stream(values())
            .filter(rNum -> rNum.val == value)
            .findFirst().get();
    }

  }
  
  public static Image generateIcon(R ic)
  {
	  Image img = new Image(new Texture(prefix + Res.XHDPI.toString().toLowerCase() + "/" + ic.name() +  ".png"));
	  return img;
  }
  
  public static VtmBitmap generateIconVtm(R ic) //TODO: VtmBitmap instead AwtBitmap
  {
	  Texture tex = new Texture(prefix + Res.XHDPI.toString().toLowerCase() + "/" + ic.name() +  ".png");
	  VtmBitmap img = new VtmBitmap(tex);
return img;	  
//	  String name = "/" + prefix + Res.XHDPI.toString().toLowerCase() + "/" + ic.name() +  ".png";
//	  AwtBitmap img;
//	try
//	{
//		img = new AwtBitmap(Icons.class.getResourceAsStream(name));
//	  return img;
//	} catch (IOException e)
//	{
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//	return null;
  }
}
