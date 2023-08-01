package com.example.demo.metro;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;

@Configuration
@EnableWebMvc
@RestController
@RequestMapping(path = "/")
@CrossOrigin("http://localhost:3000")
public class ResultController {
    @GetMapping("/inputs")
    public List<ArrayList<String>> displayStrings(String source, String destination) throws Exception {
        List<ArrayList<String>> strings = Metro.result(source, destination);
        return strings;
    }

    @GetMapping("/drdn")
    public List<String> dropDown(String source, String destination)  {
        Map<String, double[]> fr = Metro.stations();
        List<String> strings = new ArrayList<>();
        for (String str: fr.keySet()) {
            strings.add(str);
        }
        return strings;
    }

    @PostMapping("/output")
    public List<ArrayList<String>> handleRequest(@RequestBody MyRequestData requestData) throws Exception {
        List<ArrayList<String>> result = displayStrings(requestData.input1, requestData.input2);
        return result;
    }

    @PostMapping("/mapline")
    public List<ArrayList<String>> handleMapRequest(@RequestBody MyRequestData requestData) throws Exception {
        List<ArrayList<String>> result = getCoordinates(requestData.input1, requestData.input2);
        return result;
    }

    public List<ArrayList<String>> getCoordinates(String source, String destination) throws Exception {
        List<ArrayList<String>> strings = MetroMap.resultForMap(source, destination);
        return strings;
    }

    public static class MyRequestData {
        private String input1;
        private String input2;

        public MyRequestData(String input1, String input2) {
            this.input1 = input1;
            this.input2 = input2;
        }

        public MyRequestData() {
        }

        public String getInput1() {
            return input1;
        }

        public void setInput1(String input1) {
            this.input1 = input1;
        }

        public String getInput2() {
            return input2;
        }

        public void setInput2(String input2) {
            this.input2 = input2;
        }

    }
}

