package com.example.demo.metro;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class NearestMetroStation {
    private static Map<String, double[]> stationCoordinates = new HashMap<>();
    public static String nearestMetroStation(String place) throws Exception {
        initializeStationCoordinates();
        String finst = "";
        try {
            Thread.sleep(1500);
            String encodedLocation = URLEncoder.encode(place, "UTF-8");
            String url = "https://nominatim.openstreetmap.org/search?q=" + encodedLocation + "%2C+India&format=json";
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                // Parse the JSON response to get the latitude and longitude
                // System.out.println(response.toString());
                double[] coord = new double[2];
                if (response.length() != 2) {
                    coord = parseCoordinatesFromResponse(response.toString());
                }
                double min = Double.MAX_VALUE;
                if (coord != null) {
                    for (String station: stationCoordinates.keySet()) {
                        double[] st2 = stationCoordinates.get(station);
                        double dist = calculateDistance(coord, st2);
                        if (dist < min) {
                            finst = station;
                        }
                    }
                }
            } else {
                finst = mostSimilar(place, stationCoordinates);
            }
            connection.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return finst;
    }

    public static double[] parseCoordinatesFromResponse(String response) {
        double[] coord = new double[2];
        if (response.length() == 2) {
            return null;
        }
        String[] coordinates = new String[2];
        int startIndex = response.indexOf("\"lat\":") + 6;
        int endIndex = response.indexOf(",", startIndex);
        coordinates[0] = response.substring(startIndex, endIndex).trim();

        startIndex = response.indexOf("\"lon\":") + 6;
        endIndex = response.indexOf(",", startIndex);
        coordinates[1] = response.substring(startIndex, endIndex).trim();
        coordinates[0] = coordinates[0].substring(1,coordinates[0].length()-2);
        coordinates[1] = coordinates[1].substring(1,coordinates[1].length()-2);
        coord[0] = Double.parseDouble(coordinates[0]);
        coord[1] = Double.parseDouble(coordinates[1]);
        return coord;
    }

    public static String mostSimilar(String input, Map<String, double[]> hashMap) {
        String string1 = input;
        String ans = "";
        int score = Integer.MIN_VALUE;

        for (Map.Entry<String, double[]> entry : hashMap.entrySet()) {
            String string2 = entry.getKey();
            int similarityScore = StringUtils.getFuzzyDistance(string1, string2, Locale.getDefault());
            double similarityPercentage = (1 - (double) similarityScore / Math.max(string1.length(), string2.length())) * 100;
            if (similarityPercentage > score) {
                ans = string2;
            }
        }
        return ans;
    }

    private static void initializeStationCoordinates() throws IOException {
        // Add the coordinates of the stations to the stationCoordinates map
        // static/coordinates.txt

        String filePath = "/home/rushil/Downloads/demo(1)/demo/src/main/resources/static/coordinates.txt";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(":");
                String key = parts[0];
                double[] values = {Double.parseDouble(parts[1]), Double.parseDouble(parts[2])};
                if(values[0] != 0.0) {
                    stationCoordinates.put(key, values);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Add more stations and their coordinates
    }

    private static double calculateDistance(double[] coordinates1, double[] coordinates2) {
        double lat1 = Math.toRadians(coordinates1[0]);
        double lon1 = Math.toRadians(coordinates1[1]);
        double lat2 = Math.toRadians(coordinates2[0]);
        double lon2 = Math.toRadians(coordinates2[1]);

        double dlon = lon2 - lon1;
        double dlat = lat2 - lat1;

        double a = Math.pow(Math.sin(dlat / 2), 2) + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(dlon / 2), 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return 6371 * c; // Earth's radius in km
    }
}
