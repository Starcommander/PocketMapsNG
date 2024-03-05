package com.starcom.pocketmaps.map;

import org.oscim.core.GeoPoint;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on June 26, 2015.
 */
public interface MapHandlerListener {
    /**
     * when use press on the screen to get a location form map
     *
     * @param latLong
     */
    void onPressLocation(GeoPoint latLong);

}
