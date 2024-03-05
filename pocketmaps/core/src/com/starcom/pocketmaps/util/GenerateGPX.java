package com.starcom.pocketmaps.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.starcom.LoggerUtil;
import com.starcom.navigation.Location;

/**
 * This file is part of PocketMaps
 * <p/>
 * Created by GuoJunjun <junjunguo.com> on August 17, 2015.
 */
public class GenerateGPX {

    /**
     * XML header.
     */
    private final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";

    /**
     * GPX opening tag
     */
    private final String TAG_GPX =
            "<gpx" + " version=\"1.1\"" + " creator=\"PocketMaps by JunjunGuo.com - http://github.com/junjunguo/PocketMaps/\"" +
                    " xmlns=\"http://www.topografix.com/GPX/1/1\"" +
                    " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                    " xsi:schemaLocation=\"http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx" +
                    ".xsd \">";

    /**
     * Date format for a point timestamp.
     */
    private final SimpleDateFormat DF = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    /**
     * Writes the GPX file
     *
     * @param trackName Name of the GPX track (metadata)
     * @param db        database.
     * @param gpxFile   Target GPX file
     * @throws IOException
     */
    public void writeGpxFile(String trackName,ArrayList<Location> db, File gpxFile) throws IOException {

        String METADATA = "  <metadata>\n" +
                "    <link href=\"https://github.com/Starcommander/PocketMaps\">\n" +
                "      <text>Pocket Maps: Free offline maps with routing functions and more</text>\n" +
                "    </link>\n" +
                "    <time>" + DF.format(System.currentTimeMillis()) + "</time>\n" +
                "  </metadata>";
        if (!gpxFile.exists()) {
            gpxFile.createNewFile();
        }
        FileWriter fw = new FileWriter(gpxFile);

        fw.write(XML_HEADER + "\n");
        fw.write(TAG_GPX + "\n");
        fw.write(METADATA + "\n");
        writeTrackPoints(trackName, fw, db);
        fw.write("</gpx>");
        fw.close();
    }
    
    public ArrayList<Location> readGpxFile(File gpxFile) throws IOException, ParserConfigurationException, SAXException, ParseException
    {
    	ArrayList<Location> posList = new ArrayList<>();
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(gpxFile);
      doc.getDocumentElement().normalize();
      NodeList nList = doc.getElementsByTagName("trkpt");
      long now = System.currentTimeMillis() - (nList.getLength()*1000);
      for (int i = 0; i < nList.getLength(); i++)
      {
        Node node = nList.item(i);
        System.out.println("Current Element :" + node.getNodeName());
            
        if (node.getNodeType() == Node.ELEMENT_NODE)
        {
          Element element = (Element) node;
          log("--> Tracking-Element: " + element.getTagName());
          log("--> Tracking-Lat: " + element.getAttribute("lat"));
          double lat = Double.parseDouble(element.getAttribute("lat"));
          log("--> Tracking-Lon: " + element.getAttribute("lon"));
          double lon = Double.parseDouble(element.getAttribute("lon"));
          Node eleN = element.getElementsByTagName("ele").item(0);
          double ele = 300;
          if (eleN != null)
          {
            ele = Double.parseDouble(eleN.getTextContent());
          }
          log("--> Tracking-ele: " + ele);
          Node timeN = element.getElementsByTagName("time").item(0);
          long lTime = now + (i*1000);
          if (timeN != null)
          {
            String timeS = timeN.getTextContent();
            log("--> Tracking-time: " + timeS);
            Date timeD = DF.parse(timeS);
            lTime = timeD.getTime();
          }
          log("--> Tracking-time: " + lTime);
          Location location = new Location((float)lat,(float)lon,0.0f,0.0f,(float)ele,lTime);
          posList.add(location);
        }
      }
      return posList;
    }

    /**
     * Iterates on track points and write them.
     *
     * @param trackName Name of the track (metadata).
     * @param fw        Writer to the target file.
     * @param db        database
     * @throws IOException
     */
    private void writeTrackPoints(String trackName, FileWriter fw, ArrayList<Location> db) throws IOException {
        fw.write("\t" + "<trk>" + "\n");
        fw.write("\t\t" + "<name>" + "PocketMaps GPS track log" + "</name>" + "\n");
        fw.write("\t\t" + "<trkseg>" + "\n");
        for (Location g : db) {
            StringBuffer out = new StringBuffer();
            out.append("\t\t\t" + "<trkpt lat=\"" + g.getLatitude() + "\" " +
                    "lon=\"" + g.getLongitude() + "\">");
            out.append("<ele>" + g.getAltitude() + "</ele>");
            out.append("<time>" +
                    DF.format(new Date(g.getTime())) +
                    "</time>");

            out.append("</trkpt>" + "\n");
            fw.write(out.toString());

        }
        fw.write("\t\t" + "</trkseg>" + "\n");
        fw.write("\t" + "</trk>" + "\n");
    }

    /**
     * send message to logcat
     *
     * @param str
     */
    private void log(String str) {
    	LoggerUtil.get(this.getClass()).info("---GPX--- " + str);
    }
    
//    static class GeoPointX
//    {
//    	double lat;
//    	double lon;
//    	double alt;
//    	long time;
//    }

}
