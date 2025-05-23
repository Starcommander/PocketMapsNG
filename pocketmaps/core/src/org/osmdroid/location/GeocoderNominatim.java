package org.osmdroid.location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.starcom.io.Web;
import com.starcom.pocketmaps.geocoding.Address;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Implements an equivalent to Android Geocoder class, based on OpenStreetMap
 * data and Nominatim API. <br>
 * See http://wiki.openstreetmap.org/wiki/Nominatim or
 * http://open.mapquestapi.com/nominatim/
 *
 * @author M.Kergall
 * @author P.Kashofer -> Modify for search locations without Android.
 */
public class GeocoderNominatim {

    final static Logger log = LoggerFactory.getLogger(GeocoderNominatim.class);

    public static final String NOMINATIM_SERVICE_URL = "https://nominatim.openstreetmap.org/";
    public static final String MAPQUEST_SERVICE_URL = "https://open.mapquestapi.com/nominatim/v1/";

    protected Locale mLocale;
    protected String mServiceUrl;

    public GeocoderNominatim(Locale locale) {
        mLocale = locale;
        setService(NOMINATIM_SERVICE_URL); //default service
    }

    static public boolean isPresent() {
        return true;
    }

    /**
     * Specify the url of the Nominatim service provider to use. Can be one of
     * the predefined (NOMINATIM_SERVICE_URL or MAPQUEST_SERVICE_URL), or
     * another one, your local instance of Nominatim for instance.
     *
     * @param serviceUrl ...
     */
    public void setService(String serviceUrl) {
        mServiceUrl = serviceUrl;
    }

    /**
     * Build an Android Address object from the Nominatim address in JSON
     * format. Current implementation is mainly targeting french addresses, and
     * will be quite basic on other countries.
     *
     * @param jResult ...
     * @return ...
     * @throws JSONException ...
     */
    protected Address buildAndroidAddress(JSONObject jResult) throws JSONException {
        Address gAddress = new Address(mLocale);
        gAddress.latitude = jResult.getDouble("lat");
        gAddress.longitude = jResult.getDouble("lon");

        JSONObject jAddress = jResult.getJSONObject("address");

        if (jAddress.has("road")) {
        	gAddress.thoroughfare = jAddress.getString("road");
        	gAddress.addressLines.append(jAddress.getString("road")).append("\n");
        }
        if (jAddress.has("suburb")) {
            //gAddress.setAddressLine(addressIndex++, jAddress.getString("suburb"));
            //not kept => often introduce "noise" in the address.
        	gAddress.subLocality = jAddress.getString("suburb");
        }
        if (jAddress.has("postcode")) {
            gAddress.postalCode = jAddress.getString("postcode");
        	gAddress.addressLines.append(jAddress.getString("postcode")).append("\n");
        }

        if (jAddress.has("city")) {
            gAddress.locality = jAddress.getString("city");
        	gAddress.addressLines.append(jAddress.getString("city")).append("\n");
        } else if (jAddress.has("town")) {
            gAddress.locality = jAddress.getString("town");
        	gAddress.addressLines.append(jAddress.getString("town")).append("\n");
        } else if (jAddress.has("village")) {
            gAddress.locality = jAddress.getString("village");
        	gAddress.addressLines.append(jAddress.getString("village")).append("\n");
        }

        if (jAddress.has("county")) { //France: departement
            gAddress.subAdminArea = jAddress.getString("county");
        }
        if (jAddress.has("state")) { //France: region
            gAddress.adminArea = jAddress.getString("state");
        }
        if (jAddress.has("country")) {
            gAddress.countryName = jAddress.getString("country");
        	gAddress.addressLines.append(jAddress.getString("country")).append("\n");
        }
        if (jAddress.has("country_code"))
            gAddress.countryCode = jAddress.getString("country_code");

        /* Other possible OSM tags in Nominatim results not handled yet: subway,
         * golf_course, bus_stop, parking,... house, house_number, building
         * city_district (13e Arrondissement) road => or highway, ... sub-city
         * (like suburb) => locality, isolated_dwelling, hamlet ...
         * state_district */

        return gAddress;
    }

    /**
     * @param latitude   ...
     * @param longitude  ...
     * @param maxResults ...
     * @return ...
     * @throws IOException ...
     */
    public List<Address> getFromLocation(double latitude, double longitude, int maxResults)
            throws IOException {
        String url = mServiceUrl
                + "reverse?"
                + "format=json"
                + "&accept-language=" + mLocale.getLanguage()
                //+ "&addressdetails=1"
                + "&lat=" + latitude
                + "&lon=" + longitude;
        log.debug("GeocoderNominatim::getFromLocation:" + url);
        String result = Web.downloadTextfile(url);// BonusPackHelper.requestStringFromUrl(url); //TODO: Test this
        //log.debug(result);
        if (result == null)
            throw new IOException();
        try {
            JSONObject jResult = new JSONObject(result);
            Address gAddress = buildAndroidAddress(jResult);
            List<Address> list = new ArrayList<Address>();
            list.add(gAddress);
            return list;
        } catch (JSONException e) {
            throw new IOException();
        }
    }

    public List<Address> getFromLocationName(String locationName, int maxResults,
                                             double lowerLeftLatitude, double lowerLeftLongitude,
                                             double upperRightLatitude, double upperRightLongitude)
            throws IOException {
        String url = mServiceUrl
                + "search?"
                + "format=json"
                + "&accept-language=" + mLocale.getLanguage()
                + "&addressdetails=1"
                + "&limit=" + maxResults
                + "&q=" + URLEncoder.encode(locationName, "UTF-8");
        if (lowerLeftLatitude != 0.0 && lowerLeftLongitude != 0.0) {
            //viewbox = left, top, right, bottom:
            url += "&viewbox=" + lowerLeftLongitude
                    + "," + upperRightLatitude
                    + "," + upperRightLongitude
                    + "," + lowerLeftLatitude
                    + "&bounded=1";
        }
        log.debug("GeocoderNominatim::getFromLocationName:" + url);
        String result = Web.downloadTextfile(url);// BonusPackHelper.requestStringFromUrl(url);
        //log.debug(result);
        if (result == null)
            throw new IOException();
        try {
            JSONArray jResults = new JSONArray(result);
            List<Address> list = new ArrayList<Address>();
            for (int i = 0; i < jResults.length(); i++) {
                JSONObject jResult = jResults.getJSONObject(i);
                Address gAddress = buildAndroidAddress(jResult);
                list.add(gAddress);
            }
            return list;
        } catch (JSONException e) {
            throw new IOException();
        }
    }

    public List<Address> getFromLocationName(String locationName, int maxResults)
            throws IOException {
        return getFromLocationName(locationName, maxResults, 0.0, 0.0, 0.0, 0.0);
    }

}
