package com.bioforceanalytics.dashboard;

import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Timer;
import java.util.TimerTask;

import javafx.beans.NamedArg;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.chart.Axis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

/**
 * Custom LineChart created solely to shade in sections of area under a curve.
 * Should not be manually instantiated, instead reference this component in FXML;
 * the fields for NumberAxes will be populated from the FXMLLoader that reads the file.
 */
public class BFALineChart<X,Y> extends LineChart<X,Y> {

    /**
     * JavaFX component containing area label.
     */
    private Pane areaPane;

    // fields used for shading in area
    private XYChart.Data<Double, Double> p1;
    private XYChart.Data<Double, Double> p2;
    private GraphData data;
    private int SIG_FIGS;
    private double area;

    // used to play SINC videos
    private MediaPlayer mediaPlayer;
    private Timer timer;

    // JavaFX SINC components
    private MediaView mediaView;
    private Pane mediaViewPane;
    private Rectangle scrubber;

    public BFALineChart(@NamedArg("xAxis") Axis<X> xAxis, @NamedArg("yAxis") Axis<Y> yAxis) {
        super(xAxis, yAxis);
    }
    
    /**
     * Clears the area label and shading of the graph.
     */
    public void clearArea() {

        // get all children nodes of LineChart
        ObservableList<Node> nodes = getPlotChildren();

        // loops backwards to avoid ConcurrentModificationException
        for (int i = nodes.size() - 1; i >= 0; i--) {

            // if node is a polygon for graphing area, remove it
            if (nodes.get(i) instanceof Polygon) {
                getPlotChildren().remove(nodes.get(i));
            }

        }

        // reset area label and remove from LineChart
        getPlotChildren().remove(areaPane);
        areaPane = new Pane();

    }

    /**
     * Internal method used to generate the JavaFX label displaying area.
     * @param a the value of the definite integral
     * @return the JavaFX Label object
     */
    private Label createAreaLabel(double a) {

		Label label = new Label("Area: " + a);

		// add styling to label
		label.getStyleClass().addAll("hover-label");

		// place the label above the data point
		label.translateYProperty().bind(label.heightProperty());

        label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);

		return label;

	}

    /**
     * Shades area under a section of a curve by drawing trapezoids between adjacent points and the x-axis.
     * Takes XYChart-based objects as arguments for ease of use in the Data Analysis Graph.
     * @param p1 the left bound of the area
     * @param p2 the right bound of the area
     * @param data the data set that should be shaded in
     * @param area the value of the definite integral from the two bounds
     * @param SIG_FIGS the number of significant figures used to round the area value
     */
    public void graphArea(XYChart.Data<Double, Double> p1, XYChart.Data<Double, Double> p2, GraphData data, double area, final int SIG_FIGS) {

        this.p1 = p1;
        this.p2 = p2;
        this.data = data;
        this.area = area;
        this.SIG_FIGS = SIG_FIGS;
        redrawArea();

    }

    /**
     * Re-renders the polygon used for shading area under a curve.
     */
    public void redrawArea() {

        ObservableList<XYChart.Data<Number, Number>> series = data.data.getData();

        if (p1 == null && p2 == null && data == null) return;
        int start = -1;
        int end = -1;

        // get index of x1 and x2 in samples
        for (int i = 0; i < series.size(); i++) {

            if (series.get(i).getXValue().equals(p1.getXValue()) && series.get(i).getYValue().equals(p1.getYValue())) {
                start = i;
            }

            if (series.get(i).getXValue().equals(p2.getXValue()) && series.get(i).getYValue().equals(p2.getYValue())) {
                end = i;
            }

        }  

        // remove area shading and label
        clearArea();

        // create area label
        double roundedArea = new BigDecimal(area).round(new MathContext(SIG_FIGS)).doubleValue();
        areaPane.getChildren().addAll(createAreaLabel(roundedArea));

        // allows mouse events to pass through polygon
        // makes selecting data points easier
        areaPane.setPickOnBounds(false);

        // set (x,y) position of the area label to halfway between the x-bounds of the area
        areaPane.setLayoutX(series.get((start + end) / 2).getNode().getLayoutX() - 50);
        areaPane.setLayoutY(series.get((start + end) / 2).getNode().getLayoutY() - 100);

        // add area label to LineChart
        getPlotChildren().add(areaPane);

        // cast axes to NumberAxes so that certain methods can be called on them
        BFANumberAxis xAxis = (BFANumberAxis) getXAxis();
        BFANumberAxis yAxis = (BFANumberAxis) getYAxis();

        // pixel position of y=0 on LineChart component
        double y0 = yAxis.getDisplayPosition(0);

        Polygon poly = new Polygon();

        // loop through all points in [p1, p2]
        for (int i = start; i <= end; i++) {      

            // pixel positions of x=series[i] and x=seriesi+1] on LineChart component
            double x1 = xAxis.getDisplayPosition(series.get(i).getXValue());

            // pixel positions of y=series[i] and y=series[i+1] on LineChart component
            double y1 = yAxis.getDisplayPosition(series.get(i).getYValue());

            // add points to polygon
            poly.getPoints().addAll(new Double[] { x1, y1 });

        }

        // populate points along the x-axis
        for (int i = end; i >= start; i--) {
            double x1 = xAxis.getDisplayPosition(series.get(i).getXValue());
            poly.getPoints().addAll(new Double[] { x1, y0 });
        }

        // set of the shaded area to the axis's color with a lower opacity
        Color c = BFAColorMenu.getColor(data.axis);
        poly.setFill(new Color(c.getRed(), c.getGreen(), c.getBlue(), 0.5));

        // add polygon to LineChart
        getPlotChildren().add(poly);

        // ensure that the area label is on top of the shaded area
        areaPane.toFront();

        // display full floating-point number on click
        areaPane.setOnMouseClicked(event -> areaPane.getChildren().addAll(createAreaLabel(area)));

    }

    /**
     * Passes references to SINC components from GraphNoSINCController to this class.
     * @param mediaView the JavaFX component that displays video 
     * @param mediaViewPane the JavaFX component that contains the media view and scrubber
     * @param scrubber the JavaFX component that displays the video scrubber
     */
    public void initSINC(MediaView mediaView, Pane mediaViewPane, Rectangle scrubber) {
        this.mediaView = mediaView;
        this.mediaViewPane = mediaViewPane;
        this.scrubber = scrubber;
    }

    /**
     * Plays a SINC video overlayed on this chart.
     * @param videoFile the File object for this video
     */
    public void playVideo(File videoFile) {
        
        // stop any previous videos/timers
        if (mediaPlayer != null) mediaPlayer.stop();
        if (timer != null) timer.cancel();

		//=============================================
		// TODO SINC testing code
		// TODO This will be reorganized in the future.
		//=============================================
		mediaPlayer = new MediaPlayer(new Media(videoFile.toURI().toString()));
		mediaView.setMediaPlayer(mediaPlayer);
		mediaPlayer.play();

		scrubber.setVisible(true);
		scrubber.setX(0);

        // needed for TimerTask
        BFALineChart<Number,Number> lineChart = (BFALineChart<Number,Number>) this;

        timer = new Timer();

		TimerTask sincTask = new TimerTask() {

			double time = 0;

			@Override
			public void run() {
				time += ((double) 1)/((double) 60);
				scrubber.setX(lineChart.getXAxis().getDisplayPosition(time));
			}

		};

		timer.scheduleAtFixedRate(sincTask, 0, 1000/60);

		// Task<Void> sincTask = new Task<Void>() {

		// 	@Override
		// 	protected Void call() {

		// 		// TODO use a "playing" flag here
		// 		while (true) {
		// 			scrubber.setX(Math.round(mediaPlayer.getCurrentTime().toMillis() / 10));
		// 		}

		// 	}

		// };

        // new Thread(sincTask).start();
        
    }

    /*
    public void graphArea(XYChart.Data<Double, Double> p1, XYChart.Data<Double, Double> p2, ObservableList<XYChart.Data<Number,Number>> data, double area, final int SIG_FIGS) {

        int start = -1;
        int end = -1;

        // get index of x1 and x2 in samples
        for (int i = 0; i < data.size(); i++) {

            if (data.get(i).getXValue().equals(p1.getXValue()) && data.get(i).getYValue().equals(p1.getYValue())) {
                start = i;
            }

            if (data.get(i).getXValue().equals(p2.getXValue()) && data.get(i).getYValue().equals(p2.getYValue())) {
                end = i;
            }

        }  

        // remove area shading and label
        clearArea();

        // create area label
        double roundedArea = new BigDecimal(area).round(new MathContext(SIG_FIGS)).doubleValue();
        areaPane.getChildren().addAll(createAreaLabel(roundedArea));

        // allows mouse events to pass through polygon
        // makes selecting data points easier
        areaPane.setPickOnBounds(false);

        // set (x,y) position of the area label to halfway between the x-bounds of the area
        areaPane.setLayoutX(data.get((start + end) / 2).getNode().getLayoutX() - 50);
        areaPane.setLayoutY(data.get((start + end) / 2).getNode().getLayoutY() - 100);

        // add area label to LineChart
        getPlotChildren().add(areaPane);

        // cast axes to NumberAxes so that certain methods can be called on them
        BFANumberAxis xAxis = (BFANumberAxis) getXAxis();
        BFANumberAxis yAxis = (BFANumberAxis) getYAxis();

        // pixel position of y=0 on LineChart component
        double y0 = yAxis.getDisplayPosition(0);

        Polygon poly = new Polygon();
        // loop through all points in [p1, p2]
        for (int i = start; i <= end; i++) {      

            // pixel positions of x=data[i] and x=data[i+1] on LineChart component
            double x1 = xAxis.getDisplayPosition(data.get(i).getXValue());

            // pixel positions of y=data[i] and y=data[i+1] on LineChart component
            double y1 = yAxis.getDisplayPosition(data.get(i).getYValue());

            // add points to polygon
            poly.getPoints().addAll(new Double[] {
                x1, y1,
            });

        }
        for(int i = end; i >= start; i--){
            double x1 = xAxis.getDisplayPosition(data.get(i).getXValue());
            poly.getPoints().addAll(new Double[] {
                x1, y0,
            });
        }

        // TODO change this to shade the same color as the graph?
        // not important at all, just a cosmetic feature that would be nice
        poly.setFill(Color.RED);

        // add polygon to LineChart
        getPlotChildren().add(poly);
        // ensure that the area label is on top of the shaded area
        areaPane.toFront();

        // display full floating-point number on click
        areaPane.setOnMouseClicked(new EventHandler<MouseEvent>() {
            
            @Override
            public void handle(MouseEvent event) {
                areaPane.getChildren().addAll(createAreaLabel(area));
            }

        });
    
    }
    */

}