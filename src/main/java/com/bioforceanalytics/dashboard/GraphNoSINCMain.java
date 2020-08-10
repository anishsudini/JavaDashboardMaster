package com.bioforceanalytics.dashboard;

import java.util.ArrayList;
import java.util.Random;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Testing class used to launch the Data Analysis Graph.
 * @deprecated not up to date and potentially broken,
 * launch through the Dashboard instead.
 */
@Deprecated
public class GraphNoSINCMain extends Application {

	private static GraphNoSINCController controller;
	private static ArrayList<Double> testDataSamples;
	private static ArrayList<Double> testDataTime;
	
    @Override
    public void start(Stage primaryStage) throws Exception {

    	FXMLLoader loader = new FXMLLoader((getClass().getResource("fxml/GraphNoSINC.fxml")));
        Parent root = loader.load();
        controller = loader.getController();
        
        primaryStage.setTitle("BioForce Experiment Graph");
        Scene scene = new Scene(root);
       
        primaryStage.setMinWidth(600);
        primaryStage.setMinHeight(400);
        primaryStage.setScene(scene);
        //scene.getStylesheets().add(getClass().getResource("css/EducatorModeCSS.css").toExternalForm());
        primaryStage.show();
        
        primaryStage.setResizable(true);

    }

    public static void main(String[] args) {    	
    	
    	// CODE FOR SAMPLE DATA -- NOT NECCESARY, BUT testDataSamples AND testDataTime MUST BE FILLED WITH SOMETHING TO USE THIS MAIN METHOD
    	int size = 96000;
    	testDataSamples = new ArrayList<Double>();
    	testDataTime = new ArrayList<Double>();
    	System.out.println(testDataSamples.size());
    	Random rand = new Random();
    	
    	
    	for(int i = 0; i < size; i++) {
    		testDataSamples.add(Math.log(i/3000.0 + 1.0) + 5 * Math.sin(i / 1000.0) * (((i-10000)/1000.0) / (1 + (((i-10000)/1000.0)*((i-10000)/1000.0)))) + (rand.nextDouble() - 0.5));
    		testDataTime.add(i / 960.0);
    	}
    	
    	
    	launch(args);
    	
    }
}
