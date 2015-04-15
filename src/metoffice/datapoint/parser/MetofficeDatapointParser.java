
package metoffice.datapoint.parser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.*;

public class MetofficeDatapointParser {

    final private static String REGIONAL_FORECAST = "regionalforecast";
    final private static String NATIONAL_PARK = "nationalpark";
    final private static String MOUNTAIN_AREA = "mountainarea";

    public static void main(String[] args) {                
        Timer timer = new Timer();
        TimerTask halfHourlyTask = new TimerTask() {
            public void run() {
                try {
                    // GL's account datapointKey
                    String datapointKey = "8475b7ba-a4ce-4aa7-8f26-a7b4838f4816";

                    queryTextsData(datapointKey, REGIONAL_FORECAST);
                    //These two don't seem to be available, contrary to what the datapoint guidelines say
                    //queryTextsData(datapointKey, NATIONAL_PARK);
                    //queryTextsData(datapointKey, MOUNTAIN_AREA);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(MetofficeDatapointParser.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UnknownHostException ex) {
                    Logger.getLogger(MetofficeDatapointParser.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };

        // Schedule the task to run starting now and then every 15 minutes.
        // Data are updated hourly but not on consistently on a precise minute, so best to make a lot of requests
        // (this also inadvertly handles UnknownHostExceptions)
        timer.schedule(halfHourlyTask, 0l, 1000 * 60 * 15);
    }

    public static void queryTextsData(String datapointKey, String type) throws MalformedURLException, UnknownHostException {
        // Retrieve the list of locations for which there are textual forecasts available
        String textLocationQuery = "http://datapoint.metoffice.gov.uk/public/data/txt/wxfcs/" + type + "/json/sitelist?key=" + datapointKey;
        URL url = new URL(textLocationQuery);

        // read from the URL
        Scanner scan = null;
        try {
            scan = new Scanner(url.openStream());
        } catch (IOException ex) {
            Logger.getLogger(MetofficeDatapointParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        String str = new String();
        while (scan.hasNext()) {
            str += scan.nextLine();
        }
        scan.close();

        // Parse JSON
        JSONObject obj = new JSONObject(str);
        for (int i = 0; i < obj.getJSONObject("Locations").getJSONArray("Location").length(); i++) {
            String id = (((JSONObject) obj.getJSONObject("Locations").getJSONArray("Location").get(i)).getString("@id"));
            //System.out.println(id);

            queryTextLocation(datapointKey, type, id);
            queryDailyDataLocation(datapointKey, type, id);
            query3HourlyDataLocation(datapointKey, type, id);
        }
    }

    public static void queryDailyDataLocation(String datapointKey, String type, String locationID) throws MalformedURLException, UnknownHostException {
        // Retrieve the forecast data for the specified location
        String dataLocationQuery = "http://datapoint.metoffice.gov.uk/public/data/val/wxfcs/all/json/" + locationID + "?res=daily&key=" + datapointKey;
        URL url = new URL(dataLocationQuery);

        // read from the URL
        Scanner scan = null;
        try {
            scan = new Scanner(url.openStream());
        } catch (IOException ex) {
            Logger.getLogger(MetofficeDatapointParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        String str = new String();
        while (scan.hasNext()) {
            str += scan.nextLine();
        }
        scan.close();

        // Parse JSON
        JSONObject obj = new JSONObject(str);
        String creationTime = obj.getJSONObject("SiteRep").getJSONObject("DV").getString("dataDate");
        String filename = "data\\Daily Forecast Data\\met_data_daily_" + type + "_" + locationID + "_" + creationTime.replaceAll(":", "-") + ".json";

        // If the forecast is more recent save it
        if (!(new File(filename)).exists()) {
            try (PrintWriter out = new PrintWriter(filename)) {
                //System.out.println("Saving " + filename);
                out.println(str);
                out.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MetofficeDatapointParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void query3HourlyDataLocation(String datapointKey, String type, String locationID) throws MalformedURLException, UnknownHostException {
        // Retrieve the forecast data for the specified location
        String dataLocationQuery = "http://datapoint.metoffice.gov.uk/public/data/val/wxfcs/all/json/" + locationID + "?res=3hourly&key=" + datapointKey;
        URL url = new URL(dataLocationQuery);

        // read from the URL
        Scanner scan = null;
        try {
            scan = new Scanner(url.openStream());
        } catch (IOException ex) {
            Logger.getLogger(MetofficeDatapointParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        String str = new String();
        while (scan.hasNext()) {
            str += scan.nextLine();
        }
        scan.close();

        // Parse JSON
        JSONObject obj = new JSONObject(str);
        String creationTime = obj.getJSONObject("SiteRep").getJSONObject("DV").getString("dataDate");
        String filename = "data\\3Hourly Forecast Data\\met_data_3hourly_" + type + "_" + locationID + "_" + creationTime.replaceAll(":", "-") + ".json";

        // If the forecast is more recent save it
        if (!(new File(filename)).exists()) {
            try (PrintWriter out = new PrintWriter(filename)) {
                //System.out.println("Saving " + filename);
                out.println(str);
                out.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MetofficeDatapointParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void queryTextLocation(String datapointKey, String type, String locationID) throws MalformedURLException, UnknownHostException {
        // Retrieve the textual forecast for the specified location
        String textLocationQuery = "http://datapoint.metoffice.gov.uk/public/data/txt/wxfcs/regionalforecast/json/" + locationID + "?key=" + datapointKey;
        URL url = new URL(textLocationQuery);

        // read from the URL
        Scanner scan = null;
        try {
            scan = new Scanner(url.openStream());
        } catch (IOException ex) {
            Logger.getLogger(MetofficeDatapointParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        String str = new String();
        while (scan.hasNext()) {
            str += scan.nextLine();
        }
        scan.close();

        // Parse JSON
        JSONObject obj = new JSONObject(str);
        String creationTime = obj.getJSONObject("RegionalFcst").getString("createdOn");
        String filename = "data\\Textual Forecasts\\met_textual_" + type + "_" + locationID + "_" + creationTime.replaceAll(":", "-") + ".json";

        // If the forecast is more recent save it
        if (!(new File(filename)).exists()) {
            try (PrintWriter out = new PrintWriter(filename)) {
                //System.out.println("Saving " + filename);
                out.println(str);
                out.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(MetofficeDatapointParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
