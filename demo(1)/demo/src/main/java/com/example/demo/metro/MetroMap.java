package com.example.demo.metro;

import jakarta.persistence.Entity;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class MetroMap {
    private static Map<String, double[]> stationCoordinates = new HashMap<>();
    private static Map<String, List<String>> metroNetwork = new HashMap<>();
    private static Map<String, String> stationColors = new HashMap<>();

    public static Map<String, double[]> stations () {
        return stationCoordinates;
    }
    public static List<ArrayList<String>> resultForMap(String source, String destination) throws IOException {
        // Initialize the station coordinates
        initializeStationCoordinates();

        // Initialize the metro network
        initializeMetroNetwork();

        // Initialize the metro line network
        initializeMetroLine();

//        String src = findMostSimilarString(source, stationCoordinates);
//        String dest = findMostSimilarString(destination, stationCoordinates);
//        String src = mostSimilar(source, stationCoordinates);
//        String dest = mostSimilar(destination, stationCoordinates);
        String src = findMostSimilarString(source, stationCoordinates);
        String dest = findMostSimilarString(destination, stationCoordinates);

        // Get the coordinates of the source and destination using Google Maps API
        double[] sourceCoordinates = getCoordinates(src);
        double[] destinationCoordinates = getCoordinates(dest);

        // Find the best path using A* algorithm
        List<String> bestPath = findBestPath(src, dest, sourceCoordinates, destinationCoordinates);
        List<ArrayList<String>> result = new ArrayList<>();
        for (int i = 0; i < bestPath.size(); i++) {
            String key = bestPath.get(i);
            ArrayList<String> list = new ArrayList<>();
            list.add(key);
            list.add(stationColors.get(key));
            list.add(""+stationCoordinates.get(key)[0]);
            list.add(""+stationCoordinates.get(key)[1]);
            result.add(list);
        }
        return result;
    }

    public static List<ArrayList<String>> result(String source, String destination) throws IOException {
        // Initialize the station coordinates
        initializeStationCoordinates();

        // Initialize the metro network
        initializeMetroNetwork();

        String src = findMostSimilarString(source, stationCoordinates);
        String dest = findMostSimilarString(destination, stationCoordinates);
//        String src = mostSimilar(source, stationCoordinates);
//        String dest = mostSimilar(destination, stationCoordinates);

        // Get the coordinates of the source and destination using Google Maps API
        double[] sourceCoordinates = getCoordinates(src);
        double[] destinationCoordinates = getCoordinates(dest);

        // Find the best path using A* algorithm
        List<String> bestPath = findBestPath(src, dest, sourceCoordinates, destinationCoordinates);
        ArrayList<double[]> list = new ArrayList<>();
        for (String key: bestPath) {
            list.add(stationCoordinates.get(key));
        }

        List<ArrayList<String>> aggAns = aggregatedPath(bestPath);

        return aggAns;
    }

    public static List<ArrayList<String>> aggregatedPath(List<String> path) {
        initializeMetroLine();
        List<ArrayList<String>> list = new ArrayList<>();
        if(stationColors.get(path.get(0)).equals("Junction")) {
            list.add(new ArrayList<>(List.of(path.get(0), stationColors.get(path.get(1)))));
        } else {
            list.add(new ArrayList<>(List.of(path.get(0), stationColors.get(path.get(0)))));
        }
        for (int i = 1; i < path.size() - 1; i++) {
            String fin = "";
            String line = stationColors.get(path.get(i));
            if(line.equals("Junction")) {
                if(!stationColors.get(path.get(i - 1)).equals(stationColors.get(path.get(i + 1)))) {
                    StringBuilder linechange = new StringBuilder();
                    linechange.append(stationColors.get(path.get(i - 1)));
                    linechange.append(" -> ");
                    linechange.append(stationColors.get(path.get(i + 1)));
                    fin = linechange.toString();
                    list.add(new ArrayList<>(List.of(path.get(i), fin)));
                }
            }
        }

        if(stationColors.get(path.get(path.size() - 1)).equals("Junction")) {
            list.add(new ArrayList<>(List.of(path.get(path.size() - 1), stationColors.get(path.get(path.size()- 2)))));
        } else {
            list.add(new ArrayList<>(List.of(path.get(path.size() - 1), stationColors.get(path.get(path.size() - 1)))));
        }
        return list;
    }

    public static String findMostSimilarString(String input, Map<String, double[]> hashMap) {
        String mostSimilarString = null;
        int minDistance = Integer.MAX_VALUE;
        LevenshteinDistance distance = new LevenshteinDistance();

        for (Map.Entry<String, double[]> entry : hashMap.entrySet()) {
            String key = entry.getKey();
            int currentDistance = distance.apply(input, key);
            if (currentDistance < minDistance) {
                minDistance = currentDistance;
                mostSimilarString = key;
            }
        }

        return mostSimilarString;
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

    private static void initializeMetroLine() {
        // Blue 1
        stationColors.put("Dwarka Sector 21", "Blue");
        stationColors.put("Dwarka Sector 8", "Blue");
        stationColors.put("Dwarka Sector 9", "Blue");
        stationColors.put("Dwarka Sector 10", "Blue");
        stationColors.put("Dwarka Sector 11", "Blue");
        stationColors.put("Dwarka Sector 12", "Blue");
        stationColors.put("Dwarka Sector 13", "Blue");
        stationColors.put("Dwarka Sector 14", "Blue");
        stationColors.put("Dwarka", "Blue");
        stationColors.put("Dwarka Mor", "Blue");
        stationColors.put("Nawada", "Blue");
        stationColors.put("Uttam Nagar West", "Blue");
        stationColors.put("Uttam Nagar East", "Blue");
        stationColors.put("Janakpuri West", "Blue");
        stationColors.put("Janakpuri East", "Blue");
        stationColors.put("Tilak Nagar", "Blue");
        stationColors.put("Subhash Nagar", "Blue");
        stationColors.put("Tagore Garden", "Blue");
        stationColors.put("Rajouri Garden", "Blue");
        stationColors.put("Ramesh Nagar", "Blue");
        stationColors.put("Moti Nagar", "Blue");
        stationColors.put("Kirti Nagar", "Blue");
        stationColors.put("Shadipur", "Blue");
        stationColors.put("Patel Nagar", "Blue");
        stationColors.put("Rajendra Place", "Blue");
        stationColors.put("Karol Bagh", "Blue");
        stationColors.put("Jhandewalan", "Blue");
        stationColors.put("R K Ashram Marg", "Blue");
        stationColors.put("Rajiv Chowk", "Blue");
        stationColors.put("Barakhamba", "Blue");
        stationColors.put("Mandi House", "Blue");
        stationColors.put("Pragati Maidan", "Blue");
        stationColors.put("Indraprastha", "Blue");
        stationColors.put("Yamuna Bank", "Blue");
        stationColors.put("Akshardham", "Blue");
        stationColors.put("Mayur Vihar-1", "Blue");
        stationColors.put("Mayur Vihar Extension", "Blue");
        stationColors.put("New Ashok Nagar", "Blue");
        stationColors.put("Noida Sector 15", "Blue");
        stationColors.put("Noida Sector 16", "Blue");
        stationColors.put("Noida Sector 18", "Blue");
        stationColors.put("Botanical Garden", "Blue");
        stationColors.put("Noida Golf Course", "Blue");
        stationColors.put("Noida Sector 34", "Blue");
        stationColors.put("Noida Sector 52", "Blue");
        stationColors.put("Noida Sector 61", "Blue");
        stationColors.put("Noida Sector 59", "Blue");
        stationColors.put("Noida Sector 62", "Blue");
        stationColors.put("Noida Electronic City", "Blue");

        // BlueExt
        stationColors.put("Laxmi Nagar", "BlueExt");
        stationColors.put("Nirman Vihar", "BlueExt");
        stationColors.put("Preet Vihar", "BlueExt");
        stationColors.put("Karkarduma (Blue Line)", "BlueExt");
        stationColors.put("Anand Vihar (Blue Line)", "BlueExt");
        stationColors.put("Kaushambi", "BlueExt");
        stationColors.put("Vaishali", "BlueExt");

        // Red Line
        stationColors.put("Shaheed Sthal(New Bus Adda)", "Red");
        stationColors.put("Hindon River", "Red");
        stationColors.put("Arthala", "Red");
        stationColors.put("Mohan Nagar", "Red");
        stationColors.put("Shyam park", "Red");
        stationColors.put("Major Mohit Sharma", "Red");
        stationColors.put("Raj Bagh", "Red");
        stationColors.put("Shaheed Nagar", "Red");
        stationColors.put("Dilshad Garden", "Red");
        stationColors.put("Jhil Mil", "Red");
        stationColors.put("Mansarovar Park", "Red");
        stationColors.put("Shahdara", "Red");
        stationColors.put("Welcome", "Red");
        stationColors.put("Seelampur", "Red");
        stationColors.put("Shastri Park", "Red");
        stationColors.put("Kashmere Gate", "Red");
        stationColors.put("Tis Hazari", "Red");
        stationColors.put("Pul Bangash", "Red");
        stationColors.put("Pratap Nagar", "Red");
        stationColors.put("Shastri Nagar", "Red");
        stationColors.put("Inderlok", "Red");
        stationColors.put("Kanhaiya Nagar", "Red");
        stationColors.put("Keshav Puram", "Red");
        stationColors.put("Netaji Subhash Place", "Red");
        stationColors.put("Kohat Enclave", "Red");
        stationColors.put("Pitam Pura", "Red");
        stationColors.put("Rohini East", "Red");
        stationColors.put("Rohini West", "Red");
        stationColors.put("Rithala", "Red");

        // Yellow Line
        stationColors.put("Jahangir Puri", "Yellow");
        stationColors.put("Adarsh Nagar", "Yellow");
        stationColors.put("Azadpur", "Yellow");
        stationColors.put("Model Town", "Yellow");
        stationColors.put("Gtb Nagar", "Yellow");
        stationColors.put("Vishwavidyalaya", "Yellow");
        stationColors.put("Vidhan Sabha", "Yellow");
        stationColors.put("Civil Lines", "Yellow");
        stationColors.put("Chandni Chowk", "Yellow");
        stationColors.put("New Delhi", "Yellow");
        stationColors.put("Patel Chowk", "Yellow");
        stationColors.put("Central Secretariat", "Yellow");
        stationColors.put("Udyog Bhawan", "Yellow");
        stationColors.put("Race Course", "Yellow");
        stationColors.put("Jor Bagh", "Yellow");
        stationColors.put("INA", "Yellow");
        stationColors.put("Aiims", "Yellow");
        stationColors.put("Green Park", "Yellow");
        stationColors.put("Hauz Khas", "Yellow");
        stationColors.put("Malviya Nagar", "Yellow");
        stationColors.put("Saket", "Yellow");
        stationColors.put("Qutub Minar", "Yellow");
        stationColors.put("Chattarpur", "Yellow");
        stationColors.put("Sultanpur", "Yellow");
        stationColors.put("Ghitorni", "Yellow");
        stationColors.put("Arjangarh", "Yellow");
        stationColors.put("Guru Dronacharya", "Yellow");
        stationColors.put("Sikanderpur", "Yellow");
        stationColors.put("Mg Road", "Yellow");
        stationColors.put("Iffco Chowk", "Yellow");
        stationColors.put("Huda City Center", "Yellow");

        // Green Line
        stationColors.put("Ashok Park Main", "Green");
        stationColors.put("Punjabi Bagh", "Green");
        stationColors.put("Shivaji Park", "Green");
        stationColors.put("Madipur", "Green");
        stationColors.put("Paschim Vihar (East)", "Green");
        stationColors.put("Paschim Vihar (West)", "Green");
        stationColors.put("Peera Garhi", "Green");
        stationColors.put("Udyog Nagar", "Green");
        stationColors.put("Maharaja Surajmal Stadium", "Green");
        stationColors.put("Nangloi", "Green");
        stationColors.put("Nangloi Railway Station", "Green");
        stationColors.put("Rajdhani Park", "Green");
        stationColors.put("Mundka", "Green");
        stationColors.put("Mundka Industrial Area (MIA)", "Green");
        stationColors.put("Ghevra Metro station", "Green");
        stationColors.put("Tikri Kalan", "Green");
        stationColors.put("Tikri Border", "Green");
        stationColors.put("Pandit Shree Ram Sharma", "Green");
        stationColors.put("Bahdurgarh City", "Green");
        stationColors.put("Brigadier Hoshiar Singh", "Green");
        stationColors.put("Kirti Nagar", "Green");
        stationColors.put("Satguru Ramsingh Marg", "Green");

        // Violet Line
        stationColors.put("Lal Quila", "Violet");
        stationColors.put("Jama Masjid", "Violet");
        stationColors.put("Delhi Gate", "Violet");
        stationColors.put("ITO", "Violet");
        stationColors.put("Janpath", "Violet");
        stationColors.put("Central Secretariat", "Violet");
        stationColors.put("Khan Market", "Violet");
        stationColors.put("Jawaharlal Nehru Stadium", "Violet");
        stationColors.put("Jangpura", "Violet");
        stationColors.put("Lajpat Nagar", "Violet");
        stationColors.put("Moolchand", "Violet");
        stationColors.put("Kailash Colony", "Violet");
        stationColors.put("Nehru Place", "Violet");
        stationColors.put("Harkesh Nagar Okhla", "Violet");
        stationColors.put("Kalkaji Mandir", "Violet");
        stationColors.put("Govind Puri", "Violet");
        stationColors.put("Jasola", "Violet");
        stationColors.put("Sarita Vihar", "Violet");
        stationColors.put("Mohan Estate", "Violet");
        stationColors.put("Tughlakabad", "Violet");
        stationColors.put("Badarpur Border", "Violet");
        stationColors.put("Sarai", "Violet");
        stationColors.put("N.H.P.C. Chowk", "Violet");
        stationColors.put("Mewala Maharajpur", "Violet");
        stationColors.put("Sector 28 Faridabad", "Violet");
        stationColors.put("Badkal Mor", "Violet");
        stationColors.put("Old Faridabad", "Violet");
        stationColors.put("Neelam Chowk Ajronda", "Violet");
        stationColors.put("Bata Chowk", "Violet");
        stationColors.put("Escorts Mujesar", "Violet");
        stationColors.put("Sant Surdas - Sihi", "Violet");
        stationColors.put("Raja Nahar Singh", "Violet");

        // Magenta Line
        stationColors.put("Dabri Mor - Janakpuri South", "Magenta");
        stationColors.put("Dashrath Puri", "Magenta");
        stationColors.put("Palam", "Magenta");
        stationColors.put("Sadar Bazaar Cantonment", "Magenta");
        stationColors.put("Terminal 1 IGI Airport", "Magenta");
        stationColors.put("Shankar Vihar", "Magenta");
        stationColors.put("Vasant Vihar", "Magenta");
        stationColors.put("Munirka", "Magenta");
        stationColors.put("RK Puram", "Magenta");
        stationColors.put("IIT Delhi", "Magenta");
        stationColors.put("Panchsheel Park", "Magenta");
        stationColors.put("Chirag Delhi", "Magenta");
        stationColors.put("Greater Kailash", "Magenta");
        stationColors.put("Nehru Enclave", "Magenta");
        stationColors.put("Kalkaji Mandir", "Magenta");
        stationColors.put("Okhla NSIC", "Magenta");
        stationColors.put("Sukhdev Vihar", "Magenta");
        stationColors.put("JAMIA MILLIA ISLAMIA", "Magenta");
        stationColors.put("Okhla Vihar", "Magenta");
        stationColors.put("Jasola Vihar Shaheen Bagh", "Magenta");
        stationColors.put("Kalindi Kunj", "Magenta");
        stationColors.put("Okhla Bird Sanctuary", "Magenta");

        // Aqua Line
        stationColors.put("Noida Sector 51", "Aqua");
        stationColors.put("Noida Sector 50", "Aqua");
        stationColors.put("Noida Sector 76", "Aqua");
        stationColors.put("Noida Sector 101", "Aqua");
        stationColors.put("Noida Sector 81", "Aqua");
        stationColors.put("NSEZ Noida", "Aqua");
        stationColors.put("Noida Sector 83", "Aqua");
        stationColors.put("Noida Sector 137", "Aqua");
        stationColors.put("Noida Sector 142", "Aqua");
        stationColors.put("Noida Sector 143", "Aqua");
        stationColors.put("Noida Sector 144", "Aqua");
        stationColors.put("Noida Sector 145", "Aqua");
        stationColors.put("Noida Sector 146", "Aqua");
        stationColors.put("Noida Sector 147", "Aqua");
        stationColors.put("Noida Sector 148", "Aqua");
        stationColors.put("Knowledge Park II", "Aqua");
        stationColors.put("Pari Chowk", "Aqua");
        stationColors.put("Alpha 1 Greater Noida", "Aqua");
        stationColors.put("Delta 1 Greater Noida", "Aqua");
        stationColors.put("GNIDA Office", "Aqua");
        stationColors.put("Depot Greater Noida", "Aqua");

        // Pink Line
        stationColors.put("Majlis Park", "Pink");
        stationColors.put("Shalimar Bagh", "Pink");
        stationColors.put("Netaji Subhash Place", "Pink");
        stationColors.put("Shakurpur", "Pink");
        stationColors.put("Punjabi Bagh West", "Pink");
        stationColors.put("ESI BASAI DARAPUR", "Pink");
        stationColors.put("Maya Puri", "Pink");
        stationColors.put("Naraina Vihar", "Pink");
        stationColors.put("Delhi Cantt", "Pink");
        stationColors.put("Durgabai Deshmukh South Campus", "Pink");
        stationColors.put("Sir Vishweshwaraiah Moti Bagh", "Pink");
        stationColors.put("Bhikaji Cama Place", "Pink");
        stationColors.put("Sarojini Nagar", "Pink");
        stationColors.put("Dilli Haat INA", "Pink");
        stationColors.put("South Extension", "Pink");
        stationColors.put("Lajpat Nagar", "Pink");
        stationColors.put("Vinobapuri", "Pink");
        stationColors.put("Ashram", "Pink");
        stationColors.put("Sarai Kale Khan Hazrat Nizamuddin", "Pink");
        stationColors.put("Mayur Vihar Phase-1", "Pink");
        stationColors.put("Mayur Vihar Pocket I", "Pink");
        stationColors.put("Trilokpuri Sanjay Lake", "Pink");
        stationColors.put("Vinod Nagar East", "Pink");
        stationColors.put("Mandawali - West Vinod Nagar", "Pink");
        stationColors.put("IP Extension", "Pink");
        stationColors.put("Anand Vihar", "Pink");
        stationColors.put("Karkar Duma", "Pink");
        stationColors.put("Karkarduma Court", "Pink");
        stationColors.put("Krishna Nagar", "Pink");
        stationColors.put("East Azad Nagar", "Pink");
        stationColors.put("Jaffrabad", "Pink");
        stationColors.put("Maujpur", "Pink");
        stationColors.put("Gokulpuri", "Pink");
        stationColors.put("Johri Enclave", "Pink");
        stationColors.put("Shiv Vihar", "Pink");

        // Junctions
        stationColors.put("Yamuna Bank", "Junction");
        stationColors.put("Kashmere Gate", "Junction");
        stationColors.put("Rajiv Chowk", "Junction");
        stationColors.put("Central Secretariat", "Junction");
        stationColors.put("Mandi House", "Junction");
        stationColors.put("Kirti Nagar", "Junction");
        stationColors.put("Inderlok", "Junction");
        stationColors.put("Anand Vihar", "Junction");
        stationColors.put("Lajpat Nagar", "Junction");
        stationColors.put("Hauz Khas", "Junction");
        stationColors.put("Botanical Garden", "Junction");
        stationColors.put("Janakpuri West", "Junction");
        stationColors.put("Noida Sector 52", "Junction");
        stationColors.put("Kalkaji Mandir", "Junction");
        stationColors.put("Rajouri Garden", "Junction");
        stationColors.put("Netaji Subhash Place", "Junction");
        stationColors.put("Welcome", "Junction");
        stationColors.put("Azadpur", "Junction");

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

    private static void initializeMetroNetwork() {
        // Add the metro network connections to the metroNetwork map
        // Blue Line
        List<String> blue1 = Arrays.asList(
                "Dwarka Sector 21", "Dwarka Sector 8", "Dwarka Sector 9", "Dwarka Sector 10",
                "Dwarka Sector 11", "Dwarka Sector 12", "Dwarka Sector 13", "Dwarka Sector 14",
                "Dwarka", "Dwarka Mor", "Nawada", "Uttam Nagar West", "Uttam Nagar East",
                "Janakpuri West", "Janakpuri East", "Tilak Nagar", "Subhash Nagar", "Tagore Garden",
                "Rajouri Garden", "Ramesh Nagar", "Moti Nagar", "Kirti Nagar", "Shadipur",
                "Patel Nagar", "Rajendra Place", "Karol Bagh", "Jhandewalan", "R K Ashram Marg",
                "Rajiv Chowk", "Barakhamba", "Mandi House", "Pragati Maidan", "Indraprastha",
                "Yamuna Bank", "Akshardham", "Mayur Vihar-1", "Mayur Vihar Extension",
                "New Ashok Nagar", "Noida Sector 15", "Noida Sector 16", "Noida Sector 18",
                "Botanical Garden", "Noida Golf Course", "Noida Sector 34", "Noida Sector 52",
                "Noida Sector 61", "Noida Sector 59", "Noida Sector 62", "Noida Electronic City");

        for (int i = 0; i < blue1.size(); i++) {
            String station = blue1.get(i);
            List<String> neighbors = new ArrayList<>();

            if (i > 0) {
                neighbors.add(blue1.get(i - 1)); // Add the station above
            }
            if (i < blue1.size() - 1) {
                neighbors.add(blue1.get(i + 1)); // Add the station below
            }

            metroNetwork.put(station, neighbors);
        }

        List<String> n52 = metroNetwork.get("Noida Sector 52");
        n52.add("Noida Sector 51");
        metroNetwork.put("Noida Sector 52", n52);

        List<String> RC = metroNetwork.get("Rajiv Chowk");
        RC.add("New Delhi");
        RC.add("Patel Chowk");
        metroNetwork.put("Rajiv Chowk", RC);

        List<String> red = Arrays.asList(
                "Shaheed Sthal(New Bus Adda)", "Hindon River", "Arthala", "Mohan Nagar",
                "Shyam park", "Major Mohit Sharma", "Raj Bagh", "Shaheed Nagar",
                "Dilshad Garden", "Jhil Mil", "Mansarovar Park", "Shahdara", "Welcome",
                "Seelampur", "Shastri Park", "Kashmere Gate", "Tis Hazari", "Pul Bangash",
                "Pratap Nagar", "Shastri Nagar", "Inderlok", "Kanhaiya Nagar", "Keshav Puram",
                "Netaji Subhash Place", "Kohat Enclave", "Pitam Pura", "Rohini East",
                "Rohini West", "Rithala");

        for (int i = 0; i < red.size(); i++) {
            String station = red.get(i);
            List<String> neighbors = new ArrayList<>();

            if (i > 0) {
                neighbors.add(red.get(i - 1)); // Add the station above
            }
            if (i < red.size() - 1) {
                neighbors.add(red.get(i + 1)); // Add the station below
            }

            metroNetwork.put(station, neighbors);
        }

        List<String> kg = metroNetwork.get("Kashmere Gate");
        kg.add("Civil Lines");
        kg.add("Chandni Chowk");
        metroNetwork.put("Kashmere Gate", kg);

        metroNetwork.put("Jahangir Puri", new ArrayList<>(List.of("Adarsh Nagar")));
        metroNetwork.put("Adarsh Nagar", new ArrayList<>(List.of("Jahangir Puri", "Azadpur")));
        metroNetwork.put("Azadpur", new ArrayList<>(List.of("Adarsh Nagar", "Model Town")));
        metroNetwork.put("Model Town", new ArrayList<>(List.of("Azadpur", "Gtb Nagar")));
        metroNetwork.put("Gtb Nagar", new ArrayList<>(List.of("Model Town", "Vishwavidyalaya")));
        metroNetwork.put("Vishwavidyalaya", new ArrayList<>(List.of("Gtb Nagar", "Vidhan Sabha")));
        metroNetwork.put("Vidhan Sabha", new ArrayList<>(List.of("Vishwavidyalaya", "Civil Lines")));
        metroNetwork.put("Civil Lines", new ArrayList<>(List.of("Vidhan Sabha", "Kashmere Gate")));
        // metroNetwork.put("Kashmere Gate", new ArrayList<>(List.of("Civil Lines",
        // "Chandni Chowk")));
        metroNetwork.put("Chandni Chowk", new ArrayList<>(List.of("Kashmere Gate", "New Delhi")));
        metroNetwork.put("New Delhi", new ArrayList<>(List.of("Chandni Chowk", "Rajiv Chowk")));
        // metroNetwork.put("Rajiv Chowk", new ArrayList<>(List.of("New Delhi", "Patel
        // Chowk")));
        metroNetwork.put("Patel Chowk", new ArrayList<>(List.of("Rajiv Chowk", "Central Secretariat")));
        metroNetwork.put("Central Secretariat", new ArrayList<>(List.of("Patel Chowk", "Udyog Bhawan")));
        metroNetwork.put("Udyog Bhawan", new ArrayList<>(List.of("Central Secretariat", "Race Course")));
        metroNetwork.put("Race Course", new ArrayList<>(List.of("Udyog Bhawan", "Jor Bagh")));
        metroNetwork.put("Jor Bagh", new ArrayList<>(List.of("Race Course", "INA")));
        metroNetwork.put("INA", new ArrayList<>(List.of("Jor Bagh", "Aiims")));
        metroNetwork.put("Aiims", new ArrayList<>(List.of("INA", "Green Park")));
        metroNetwork.put("Green Park", new ArrayList<>(List.of("Aiims", "Hauz Khas")));
        metroNetwork.put("Hauz Khas", new ArrayList<>(List.of("Green Park", "Malviya Nagar")));
        metroNetwork.put("Malviya Nagar", new ArrayList<>(List.of("Hauz Khas", "Saket")));
        metroNetwork.put("Saket", new ArrayList<>(List.of("Malviya Nagar", "Qutub Minar")));
        metroNetwork.put("Qutub Minar", new ArrayList<>(List.of("Saket", "Chattarpur")));
        metroNetwork.put("Chattarpur", new ArrayList<>(List.of("Qutub Minar", "Sultanpur")));
        metroNetwork.put("Sultanpur", new ArrayList<>(List.of("Chattarpur", "Ghitorni")));
        metroNetwork.put("Ghitorni", new ArrayList<>(List.of("Sultanpur", "Arjangarh")));
        metroNetwork.put("Arjangarh", new ArrayList<>(List.of("Ghitorni", "Guru Dronacharya")));
        metroNetwork.put("Guru Dronacharya", new ArrayList<>(List.of("Arjangarh", "Sikanderpur")));
        metroNetwork.put("Sikanderpur", new ArrayList<>(List.of("Guru Dronacharya", "Mg Road")));
        metroNetwork.put("Mg Road", new ArrayList<>(List.of("Sikanderpur", "Iffco Chowk")));
        metroNetwork.put("Iffco Chowk", new ArrayList<>(List.of("Mg Road", "Huda City Center")));
        metroNetwork.put("Huda City Center", new ArrayList<>(List.of("Iffco Chowk")));

        // Blue Line 2
        List<String> yb = metroNetwork.get("Yamuna Bank");
        yb.add("Laxmi Nagar");
        metroNetwork.put("Yamuna Bank", yb);
        metroNetwork.put("Laxmi Nagar", new ArrayList<>(List.of("Nirman Vihar", "Yamuna Bank")));
        metroNetwork.put("Nirman Vihar", new ArrayList<>(List.of("Preet Vihar", "Laxmi Nagar")));
        metroNetwork.put("Preet Vihar", new ArrayList<>(List.of("Nirman Vihar", "Karkarduma")));
        metroNetwork.put("Karkarduma", new ArrayList<>(List.of("Preet Vihar", "Anand Vihar")));
        metroNetwork.put("Anand Vihar", new ArrayList<>(List.of("Karkarduma", "Kaushambi")));
        metroNetwork.put("Kaushambi", new ArrayList<>(List.of("Anand Vihar", "Vaishali")));
        metroNetwork.put("Vaishali", new ArrayList<>(List.of("Kaushambi")));

        // Green
        List<String> green1 = new ArrayList<>();
        // green1.add("Inderlok");
        green1.add("Ashok Park Main");
        green1.add("Punjabi Bagh");
        green1.add("Shivaji Park");
        green1.add("Madipur");
        green1.add("Paschim Vihar (East)");
        green1.add("Paschim Vihar (West)");
        green1.add("Peera Garhi");
        green1.add("Udyog Nagar");
        green1.add("Maharaja Surajmal Stadium");
        green1.add("Nangloi");
        green1.add("Nangloi Railway Station");
        green1.add("Rajdhani Park");
        green1.add("Mundka");
        green1.add("Mundka Industrial Area (MIA)");
        green1.add("Ghevra Metro station");
        green1.add("Tikri Kalan");
        green1.add("Tikri Border");
        green1.add("Pandit Shree Ram Sharma");
        green1.add("Bahdurgarh City");
        green1.add("Brigadier Hoshiar Singh");

        List<String> in = metroNetwork.get("Inderlok");
        in.add("Ashok Park Main");
        metroNetwork.put("Inderlok", in);

        for (int i = 0; i < green1.size(); i++) {
            String station = green1.get(i);
            List<String> neighbors = new ArrayList<>();

            if (i > 0) {
                neighbors.add(green1.get(i - 1)); // Add the station above
            }

            if (i < green1.size() - 1) {
                neighbors.add(green1.get(i + 1)); // Add the station below
            }

            metroNetwork.put(station, neighbors);
        }

        List<String> as = metroNetwork.get("Ashok Park Main");
        as.add("Inderlok");
        metroNetwork.put("Ashok Park Main", as);

        // Green Line 2
        List<String> green2 = new ArrayList<>();

        // Add the station names to the list
        green2.add("Kirti Nagar");
        green2.add("Satguru Ramsingh Marg");
        green2.add("Ashok Park Main");

        for (int i = 0; i < green2.size(); i++) {
            String station = green2.get(i);
            if (metroNetwork.containsKey(station)) {
                List<String> temp = metroNetwork.get(station);
                if (i > 0) {
                    temp.add(green2.get(i - 1)); // Add the station above
                }
                if (i < green2.size() - 1) {
                    temp.add(green2.get(i + 1)); // Add the station below
                }
                metroNetwork.put(station, temp);
            } else {
                List<String> neighbors = new ArrayList<>();

                if (i > 0) {
                    neighbors.add(green2.get(i - 1)); // Add the station above
                }

                if (i < green2.size() - 1) {
                    neighbors.add(green2.get(i + 1)); // Add the station below
                }

                metroNetwork.put(station, neighbors);
            }
        }

        // Violet Line
        List<String> violet = new ArrayList<>();

        violet.add("Kashmere Gate");
        violet.add("Lal Quila");
        violet.add("Jama Masjid");
        violet.add("Delhi Gate");
        violet.add("ITO");
        violet.add("Mandi House");
        violet.add("Janpath");
        violet.add("Central Secretariat");
        violet.add("Khan Market");
        violet.add("Jawaharlal Nehru Stadium");
        violet.add("Jangpura");
        violet.add("Lajpat Nagar");
        violet.add("Moolchand");
        violet.add("Kailash Colony");
        violet.add("Nehru Place");
        violet.add("Kalkaji Mandir");
        violet.add("Govind Puri");
        violet.add("Harkesh Nagar Okhla");
        violet.add("Jasola");
        violet.add("Sarita Vihar");
        violet.add("Mohan Estate");
        violet.add("Tughlakabad");
        violet.add("Badarpur Border");
        violet.add("Sarai");
        violet.add("N.H.P.C. Chowk");
        violet.add("Mewala Maharajpur");
        violet.add("Sector 28 Faridabad");
        violet.add("Badkal Mor");
        violet.add("Old Faridabad");
        violet.add("Neelam Chowk Ajronda");
        violet.add("Bata Chowk");
        violet.add("Escorts Mujesar");
        violet.add("Sant Surdas - Sihi");
        violet.add("Raja Nahar Singh");

        for (int i = 0; i < violet.size(); i++) {
            String station = violet.get(i);
            if (metroNetwork.containsKey(station)) {
                List<String> temp = metroNetwork.get(station);
                if (i > 0) {
                    temp.add(violet.get(i - 1)); // Add the station above
                }
                if (i < violet.size() - 1) {
                    temp.add(violet.get(i + 1)); // Add the station below
                }
                metroNetwork.put(station, temp);
            } else {
                List<String> neighbors = new ArrayList<>();

                if (i > 0) {
                    neighbors.add(violet.get(i - 1)); // Add the station above
                }

                if (i < violet.size() - 1) {
                    neighbors.add(violet.get(i + 1)); // Add the station below
                }

                metroNetwork.put(station, neighbors);
            }
        }

        // Magenta Line
        List<String> Magenta = new ArrayList<>();

        // Add the station names to the list
        Magenta.add("Janakpuri West");
        Magenta.add("Dabri Mor - Janakpuri South");
        Magenta.add("Dashrath Puri");
        Magenta.add("Palam");
        Magenta.add("Sadar Bazaar Cantonment");
        Magenta.add("Terminal 1 IGI Airport");
        Magenta.add("Shankar Vihar");
        Magenta.add("Vasant Vihar");
        Magenta.add("Munirka");
        Magenta.add("RK Puram");
        Magenta.add("IIT Delhi");
        Magenta.add("Hauz Khas");
        Magenta.add("Panchsheel Park");
        Magenta.add("Chirag Delhi");
        Magenta.add("Greater Kailash");
        Magenta.add("Nehru Enclave");
        Magenta.add("Kalkaji Mandir");
        Magenta.add("Okhla NSIC");
        Magenta.add("Sukhdev Vihar");
        Magenta.add("JAMIA MILLIA ISLAMIA");
        Magenta.add("Okhla Vihar");
        Magenta.add("Jasola Vihar Shaheen Bagh");
        Magenta.add("Kalindi Kunj");
        Magenta.add("Okhla Bird Sanctuary");
        Magenta.add("Botanical Garden");

        for (int i = 0; i < Magenta.size(); i++) {
            String station = Magenta.get(i);
            if (metroNetwork.containsKey(station)) {
                List<String> temp = metroNetwork.get(station);
                if (i > 0) {
                    temp.add(Magenta.get(i - 1)); // Add the station above
                }
                if (i < Magenta.size() - 1) {
                    temp.add(Magenta.get(i + 1)); // Add the station below
                }
                metroNetwork.put(station, temp);
            } else {
                List<String> neighbors = new ArrayList<>();

                if (i > 0) {
                    neighbors.add(Magenta.get(i - 1)); // Add the station above
                }

                if (i < Magenta.size() - 1) {
                    neighbors.add(Magenta.get(i + 1)); // Add the station below
                }

                metroNetwork.put(station, neighbors);
            }
        }

        // Aqua Line
        List<String> aqua = new ArrayList<>();

        // Add the station names to the list
        aqua.add("Noida Sector 51");
        aqua.add("Noida Sector 50");
        aqua.add("Noida Sector 76");
        aqua.add("Noida Sector 101");
        aqua.add("Noida Sector 81");
        aqua.add("NSEZ Noida");
        aqua.add("Noida Sector 83");
        aqua.add("Noida Sector 137");
        aqua.add("Noida Sector 142");
        aqua.add("Noida Sector 143");
        aqua.add("Noida Sector 144");
        aqua.add("Noida Sector 145");
        aqua.add("Noida Sector 146");
        aqua.add("Noida Sector 147");
        aqua.add("Noida Sector 148");
        aqua.add("Knowledge Park II");
        aqua.add("Pari Chowk");
        aqua.add("Alpha 1 Greater Noida");
        aqua.add("Delta 1 Greater Noida");
        aqua.add("GNIDA Office");
        aqua.add("Depot Greater Noida");

        for (int i = 0; i < aqua.size(); i++) {
            String station = aqua.get(i);
            if (metroNetwork.containsKey(station)) {
                List<String> temp = metroNetwork.get(station);
                if (i > 0) {
                    temp.add(aqua.get(i - 1)); // Add the station above
                }
                if (i < aqua.size() - 1) {
                    temp.add(aqua.get(i + 1)); // Add the station below
                }
                metroNetwork.put(station, temp);
            } else {
                List<String> neighbors = new ArrayList<>();

                if (i > 0) {
                    neighbors.add(aqua.get(i - 1)); // Add the station above
                }

                if (i < aqua.size() - 1) {
                    neighbors.add(aqua.get(i + 1)); // Add the station below
                }

                metroNetwork.put(station, neighbors);
            }
        }

        List<String> n51 = metroNetwork.get("Noida Sector 51");
        n51.add("Noida Sector 52");
        metroNetwork.put("Noida Sector 51", n51);

        List<String> pink = new ArrayList<>();

        pink.add("Majlis Park");
        pink.add("Azadpur");
        pink.add("Shalimar Bagh");
        pink.add("Netaji Subhash Place");
        pink.add("Shakurpur");
        pink.add("Punjabi Bagh West");
        pink.add("ESI BASAI DARAPUR");
        pink.add("Rajouri Garden");
        pink.add("Maya Puri");
        pink.add("Naraina Vihar");
        pink.add("Delhi Cantt");
        pink.add("Durgabai Deshmukh South Campus");
        pink.add("Sir Vishweshwaraiah Moti Bagh");
        pink.add("Bhikaji Cama Place");
        pink.add("Sarojini Nagar");
        pink.add("Dilli Haat INA");
        pink.add("South Extension");
        pink.add("Lajpat Nagar");
        pink.add("Vinobapuri");
        pink.add("Ashram");
        pink.add("Sarai Kale Khan Hazrat Nizamuddin");
        pink.add("Mayur Vihar Phase-1");
        pink.add("Mayur Vihar Pocket I");
        pink.add("Trilokpuri Sanjay Lake");
        pink.add("Vinod Nagar East");
        pink.add("Mandawali - West Vinod Nagar");
        pink.add("IP Extension");
        pink.add("Anand Vihar");
        pink.add("Karkar Duma");
        pink.add("Karkarduma Court");
        pink.add("Krishna Nagar");
        pink.add("East Azad Nagar");
        pink.add("Welcome");
        pink.add("Jaffrabad");
        pink.add("Maujpur");
        pink.add("Gokulpuri");
        pink.add("Johri Enclave");
        pink.add("Shiv Vihar");

        for (int i = 0; i < pink.size(); i++) {
            String station = pink.get(i);
            if (metroNetwork.containsKey(station)) {
                List<String> temp = metroNetwork.get(station);
                if (i > 0) {
                    temp.add(pink.get(i - 1)); // Add the station above
                }
                if (i < pink.size() - 1) {
                    temp.add(pink.get(i + 1)); // Add the station below
                }
                metroNetwork.put(station, temp);
            } else {
                List<String> neighbors = new ArrayList<>();

                if (i > 0) {
                    neighbors.add(pink.get(i - 1)); // Add the station above
                }

                if (i < pink.size() - 1) {
                    neighbors.add(pink.get(i + 1)); // Add the station below
                }

                metroNetwork.put(station, neighbors);
            }
        }

        // Add more stations and their connections
    }

    private static double[] getCoordinates(String station) {
        // Return the coordinates as a double array [latitude, longitude]
        return stationCoordinates.get(station);
    }

    private static List<String> findBestPath(String source, String destination, double[] sourceCoordinates, double[] destinationCoordinates) {
        // Create a priority queue for the open set
        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::getTotalCost));

        // Create a map to store the cost from the source to each station
        Map<String, Double> costFromSource = new HashMap<>();
        costFromSource.put(source, 0.0);

        // Create a map to store the previous station in the best path
        Map<String, String> previousStation = new HashMap<>();

        // Add the source node to the open set
        openSet.add(new Node(source, 0.0, calculateHeuristic(sourceCoordinates, destinationCoordinates)));

        while (!openSet.isEmpty()) {
            // Get the node with the lowest total cost from the open set
            Node currentNode = openSet.poll();

            // Check if the current node is the destination
            if (currentNode.getName().equals(destination)) {
                // Reconstruct the best path
                List<String> bestPath = new ArrayList<>();
                String station = destination;
                while (station != null) {
                    bestPath.add(0, station);
                    station = previousStation.get(station);
                }
                return bestPath;
            }

            // Explore the neighbors of the current node
            List<String> neighbors = metroNetwork.get(currentNode.getName());
            if (neighbors != null) {
                for (String neighbor : neighbors) {

                    // Calculate the cost from the source to the neighbor
                    double cost = costFromSource.get(currentNode.getName()) + calculateDistance(stationCoordinates.get(currentNode.getName()), stationCoordinates.get(neighbor));
                    double heur =  cost + calculateDistance(stationCoordinates.get(destination), stationCoordinates.get(neighbor));

                    // Check if the neighbor has not been visited or the new cost is lower
                    if (!costFromSource.containsKey(neighbor) || heur < costFromSource.get(currentNode.getName()) + calculateDistance(stationCoordinates.get(currentNode.getName()), destinationCoordinates)) {
                        // Update the cost and heuristic for the neighbor
                        costFromSource.put(neighbor, cost);
                        double heuristic = calculateHeuristic(stationCoordinates.get(neighbor), destinationCoordinates);

                        // Update the previous station for the neighbor
                        previousStation.put(neighbor, currentNode.getName());

                        // Add the neighbor to the open set
                        openSet.add(new Node(neighbor, cost, heuristic));
                    }
                }
            }
        }

        // No path found
        return new ArrayList<>(Arrays.asList("No Path Found"));
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

    private static double calculateHeuristic(double[] coordinates1, double[] coordinates2) {
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

    private static class Node {
        private String name;
        private double cost;
        private double heuristic;

        public Node(String name, double cost, double heuristic) {
            this.name = name;
            this.cost = cost;
            this.heuristic = heuristic;
        }

        public String getName() {
            return name;
        }

        public double getTotalCost() {
            return cost + heuristic;
        }
    }
}