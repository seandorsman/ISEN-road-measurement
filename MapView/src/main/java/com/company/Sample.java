package com.company;

import com.lynden.gmapsfx.javascript.object.LatLong;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Sample {
    LatLong gpsStart;
    LatLong gpsEnd;
    List<Vector> samples;

    public Sample(File file) throws IOException {
        String jsonTXT = new String(Files.readAllBytes(file.toPath()));
        parseJSON(new JSONObject(jsonTXT));
    }

    private void parseJSON(JSONObject rootObject) throws IOException {
        samples = new ArrayList<>();

        try {
            gpsStart = parseGPS(rootObject.getJSONObject("gps_start"));
            gpsEnd = parseGPS(rootObject.getJSONObject("gps_end"));
        } catch (NullPointerException e) {
            System.err.println("INVALID GPS DATA");
            throw new IOException("Incorrect gps data");
        }

        rootObject.getJSONArray("data").iterator().forEachRemaining(
                sampleObject -> {
                    JSONArray sample = ((JSONArray) sampleObject);
                    float x = ((Number) sample.get(0)).floatValue();
                    float y = ((Number) sample.get(1)).floatValue();
                    float z = ((Number) sample.get(2)).floatValue();
                    samples.add(new Vector(x, y, z));
                }
        );
    }

    private LatLong parseGPS(JSONObject latLongObject) throws NullPointerException {
        return new LatLong(
                ((Number) latLongObject.get("latitude")).doubleValue(),
                ((Number) latLongObject.get("longitude")).doubleValue()
        );
    }
}
