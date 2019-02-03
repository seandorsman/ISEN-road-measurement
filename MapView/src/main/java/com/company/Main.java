package com.company;

import com.company.database.DatabaseBigQuery;
import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.javascript.event.MapStateEventType;
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.LatLong;
import com.lynden.gmapsfx.javascript.object.MapOptions;
import com.lynden.gmapsfx.javascript.object.MapTypeIdEnum;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Box;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main extends Application {
    private static final File SAMPLES_LOCATION = new File("output");
    private static List<Sample> samples = new ArrayList<>();

    private GoogleMap map;
    private Canvas mapDrawingCanvas;

    private LatLong center;

    public static void main(String[] args) {
        try {
            new DatabaseBigQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        //launch();
    }

    @Override
    public void start(Stage primaryStage) {
        GoogleMapView mapView = new GoogleMapView(null, "AIzaSyAVT-vFXrzoz5A8SAhF_Bp5eK6cklaO5ds");

        BorderPane root = new BorderPane();
        Scene scene = new Scene(root, 500, 500);

        mapView.addMapInializedListener(() -> initMap(mapView));

        VBox leftMenu = new VBox();
        leftMenu.prefWidth(100);

        mapDrawingCanvas = new Canvas();
        mapDrawingCanvas.widthProperty().bind(scene.widthProperty());
        mapDrawingCanvas.heightProperty().bind(scene.heightProperty());
        mapDrawingCanvas.setMouseTransparent(true);

        StackPane map = new StackPane(mapView, mapDrawingCanvas);

        root.setLeft(leftMenu);
        root.setCenter(map);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void initMap(GoogleMapView mapView) {
        MapOptions mapOptions = new MapOptions();

        mapOptions.center(new LatLong(52.22004, 4.55671))
                .mapType(MapTypeIdEnum.ROADMAP)
                .zoom(17);

        map = mapView.createMap(mapOptions, false);

        map.addStateEventHandler(MapStateEventType.dragend, () -> Platform.runLater(() -> update(map)));

        loadSamples(SAMPLES_LOCATION, samples);
    }

    private void update(GoogleMap map) {
        GraphicsContext gc = mapDrawingCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, mapDrawingCanvas.getWidth(), mapDrawingCanvas.getHeight());

        gc.setLineWidth(5);

        samples.forEach(sample -> {

            Point2D b = map.fromLatLngToPoint(sample.gpsEnd);

            double distance         = sample.gpsStart.distanceFrom(sample.gpsEnd);
            double peak             = sample.samples.stream().mapToDouble(Vector::length).max().orElse(Double.MAX_VALUE);
            double iterations       = (int) Math.ceil(distance); // Meters ceiled
            int samplesPerIteration = (int) Math.ceil(sample.samples.size() / iterations);

            for (int meter = 0, sampleStart = 0, sampleEnd = samplesPerIteration;  meter < iterations; meter++, sampleStart += samplesPerIteration, sampleEnd += Math.min(samplesPerIteration, sample.samples.size())) {

                Point2D a = map.fromLatLngToPoint(interpolate(1.0 - ((double)meter / iterations), sample.gpsStart, sample.gpsEnd));

                int finalSampleStart = sampleStart;
                int finalSampleEnd = sampleEnd;
                double meterAVG = sample.samples.stream().filter(vector ->  {
                    int index = sample.samples.indexOf(vector);
                    return index > finalSampleStart && index < finalSampleEnd;
                }).mapToDouble(Vector::length).average().orElse(-1);


                gc.beginPath();
                if (meterAVG == -1) {
                    gc.setStroke(Color.BLACK);
                } else {
                    gc.setStroke(
                            Color.hsb(
                                    (meterAVG*360) % 360,
                                    1.0,
                                    1.0
                            )
                    );
                }

                gc.moveTo(a.getX(), a.getY());
                gc.lineTo(b.getX(), b.getY());
                gc.stroke();
                gc.closePath();
            }


            //gc.fillOval(a.getX(), a.getY(), 20,20);
        });

    }

    private LatLong interpolate(double t, LatLong a, LatLong b) {
        double trev = 1-t;

        return new LatLong(
                a.getLatitude()*t + b.getLatitude()*trev,
                a.getLongitude()*t + b.getLongitude()*trev
        );
    }

    private boolean loadSamples(File sampleFolder, List<Sample> samples) {
        if (sampleFolder.isDirectory()) {
            File[] files = sampleFolder.listFiles();

            if (files == null)
                return false;

            Arrays.stream(files).forEach(
                    file -> {
                        try {
                            samples.add(new Sample(file));
                        } catch (Exception e) {
                            System.out.printf("Failed to parse file %s\n", file.getAbsolutePath());
                            e.printStackTrace();
                        }
                    }
            );

            // Remove samples with 0 0 coordinates
            samples.removeIf(sample ->
                            sample.gpsStart.getLatitude() == 0 ||
                            sample.gpsStart.getLongitude() == 0 ||
                            sample.gpsEnd.getLatitude() == 0 ||
                            sample.gpsEnd.getLongitude() == 0
            );

            // Get average coords of all samples
            double latitude = samples.stream().mapToDouble(sample -> sample.gpsStart.getLatitude()).average().orElse(-1.0);
            double longitude = samples.stream().mapToDouble(sample -> sample.gpsStart.getLongitude()).average().orElse(-1.0);

            map.setCenter(new LatLong(latitude, longitude));

            return true;
        }

        return false;
    }
}
