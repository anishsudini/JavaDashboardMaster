package dataorganizer;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;

public class GraphNoSINCController implements Initializable {

	// TODO implement 2nd module functionality ("genericTestTwo" not used currently)

	// GenericTest represents a single module and associated test data
	private GenericTest genericTestOne;

	// "genericTestTwo" will NOT be assigned if running a single module test
	private GenericTest genericTestTwo;
	
	private int testLength;
	
	private Map<AxisType, XYChart.Series<Number, Number>> dataSets;
	
	// the interval at which samples are drawn to the screen
	// if value is 20 (default), every 20th sample will be rendered
	private int resolution;
	
	// zooming + scrolling fields
	private double mouseX;
	private double mouseY;
	private double zoomviewScalarX;
	private double zoomviewScalarY;
	private double leftScrollPercentage;
	private double topScrollPercentage;
	private double zoomviewX; 
	private double zoomviewY;
	private double zoomviewW;
	private double zoomviewH;
	private double resetZoomviewX;
	private double resetZoomviewY;
	private double resetZoomviewH;
	private double resetZoomviewW;
	
	private double lastMouseX;
	private double lastMouseY;

	
	private double scrollCenterX;
	private double scrollCenterY;
	

	@FXML
	private LineChart<Number,Number> lineChart;
	
	@FXML
	private NumberAxis xAxis;
	
	@FXML
	private NumberAxis yAxis;

	@FXML
	private TextField rollingBlockTextField;


	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
		dataSets = new HashMap<AxisType, XYChart.Series<Number, Number>>();

		resolution = 20;
		zoomviewScalarX = 1;
		zoomviewScalarY = 1;
		resetZoomviewX = 0;
		resetZoomviewY = 0;
		resetZoomviewW = 10;
		resetZoomviewH = 5;
		zoomviewX = 0;
		zoomviewY = 0;
		zoomviewW = 10;
		zoomviewH = 5;

		// hides symbols indicating data points on graph
		lineChart.setCreateSymbols(false);
		lineChart.setStyle("-fx");
		
		// listener that runs every tick the mouse scrolls, calculates zooming
		lineChart.setOnScroll(new EventHandler<ScrollEvent>() {

			public void handle(ScrollEvent event) {	
				
				// saves the mouse location of the scroll event to x and y variables
				scrollCenterX = event.getX();
				scrollCenterY = event.getY();
				
				/**
				 * calculates the percentage of scroll either on the left or top of the screen
				 * e.g. if the mouse is at the middle of the screen, leftScrollPercentage is 0.5, if it is three quarters to the right, it is 0.75
				 */
				leftScrollPercentage = (scrollCenterX - 48)/(lineChart.getWidth() - 63);
				topScrollPercentage = (scrollCenterY - 17)/(lineChart.getHeight() - 88);
				
				// decreases the zoomview width and height by an amount relative to the scroll and the current size of the zoomview (slows down zooming at high levels of zoom)
				zoomviewW -= zoomviewW * event.getDeltaY() / 300;
				zoomviewH -= zoomviewH * event.getDeltaY() / 300;
				
				// enforces a minimum zoom by limiting the size of the viewport to at least 0.05 in graph space. Can be adjusted
				if(zoomviewW < .05) zoomviewW = .05;
				if(zoomviewH < .05) zoomviewH = .05;
				
				// moves the center of the zoomview to accomodate for the zoom, accounts for the position of the mouse to try an keep it in the same spot
				zoomviewX += zoomviewW * event.getDeltaY() * (leftScrollPercentage - .5) / 300;
				zoomviewY -= zoomviewH * event.getDeltaY() * (topScrollPercentage - .5) / 300;

				redrawGraph();
				
			}
			
		});
		
		// listener that runs every tick the mouse is dragged, calculates scrolling
		lineChart.setOnMouseDragged(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {

				// get the mouse x and y position relative to the line chart
				mouseX = event.getX();
				mouseY = event.getY();
				
				// calculate a scalar to convert pixel space into graph space (mouse data in pixels, zoomview in whatever units the graph is in)
				zoomviewScalarX = (xAxis.getUpperBound() - xAxis.getLowerBound())/(lineChart.getWidth() - yAxis.getWidth());
				zoomviewScalarY = (yAxis.getUpperBound() - yAxis.getLowerBound())/(lineChart.getHeight() - xAxis.getHeight());

				// adds the change in mouse position this tick to the zoom view, converted into graph space
				zoomviewX -= (mouseX - lastMouseX) * zoomviewScalarX;
				zoomviewY += (mouseY - lastMouseY) * zoomviewScalarY;
				
				redrawGraph();
				
				// sets last tick's mouse data as this tick's
				lastMouseX = mouseX;
				lastMouseY = mouseY;
			}
			
		});
		
		// listener that runs when the mouse is clicked, only runs once per click, helps to differentiate between drags
		lineChart.setOnMousePressed(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {
				
				lastMouseX = event.getX();
				lastMouseY = event.getY();

			}
			
		});
	}

	/**
	 * <p>Populates the data analysis graph with GenericTests.</p>
	 * <p>g2 will be null for a One Module setup.</p>
	 * @param g1 GenericTest associated with module 1
	 * @param g2 GenericTest associated with module 2 (if applicable)
	 */
	public void setGenericTests(GenericTest g1, GenericTest g2) {

		// g1/g2 are allowed to be null here (differentiating One/Two Module setup)
		genericTestOne = g1;
		genericTestTwo = g2;

		// the AxisType is arbitrary (all non-magnetometer axes are same length)
		testLength = g1.getAxis(AxisType.AccelX).getOriginalData().length;
	
		// TEST CODE - TO BE REPLACED LATER
		// TODO select data set to graph based on type of GenericTest
		// (pendulum -> angular velocity/pos, inclined plane -> AccelX)
	 	graphAxis(AxisType.AccelX);
		
	}
	
	/**
	 * Handles zooming/panning of the graph.
	 */
	public void redrawGraph() {

		xAxis.setLowerBound(zoomviewX - zoomviewW/2);
		xAxis.setUpperBound(zoomviewX + zoomviewW/2);
		
		yAxis.setLowerBound(zoomviewY - zoomviewH/2);
		yAxis.setUpperBound(zoomviewY + zoomviewH/2);

	}

	/**
	 * Draws/removes an axis from the graph.
	 * @param axis the AxisType to be drawn/removed
	 */
	public void graphAxis(AxisType axis) {

		// get index of data set in line chart (if -1, does not exist)
		int dataIndex = lineChart.getData().indexOf(dataSets.get(axis));

		// get checkbox by looking up FXID (the name of the AxisType)
		CheckBox c = (CheckBox) lineChart.getScene().lookup("#Toggle" + axis);
		
		// if axis is not already graphed:
		if (dataIndex == -1) {

			System.out.println("Graphing " + axis);

			/*
			Hierarchy of graph in application:

			1) LineChart - contains all data sets and graphs, does not change
			2) XYChart.Series - child of LineChart; there can be multiple of these under one LineChart
			3) ObservableList - numerical data set component, only one per XYChart.Series
			*/
			XYChart.Series<Number, Number> series = new XYChart.Series<Number, Number>();
			ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();

			List<Double> time;
			List<Double> data;
			
			// TODO CODE FOR TESTING -- NOT FOR PRODUCTION
			// check if axis type is "simulated", otherwise use genericTest data
			if(axis == AxisType.Simulation) {
				int size = 9600;
				time = new ArrayList<Double>();
				data = new ArrayList<Double>();
				testLength = size;
				Random rand = new Random();
				for(int i = 0; i < size; i+= resolution) {
		    		data.add(Math.log(i/3000.0 + 1.0) + 5 * Math.sin(i / 1000.0) * (((i-10000)/1000.0) / (1 + (((i-10000)/1000.0)*((i-10000)/1000.0)))) + (rand.nextDouble() - 0.5));
		    		time.add(i / 960.0);
		    	}
			}
			else {	
				// get time/samples data sets
				time = genericTestOne.getAxis(axis).getTime();
				data = genericTestOne.getAxis(axis).getSamples();
			}

			// create (Time, Data) -> (X,Y) pairs
			for (int i = 0; i < data.size(); i+=resolution) {

				XYChart.Data<Number, Number> dataEl = new XYChart.Data<>(time.get(i), data.get(i));
			
				// add tooltip with (x,y) when hovering over data point
				dataEl.setNode(new DataPointLabel(time.get(i), data.get(i)));

				seriesData.add(dataEl);

			}
	
			// TODO switch this to a pretty-printed version of AxisType?
			series.setName(axis.toString());

			// add ObservableList to XYChart.Series
			series.setData(seriesData);

			// add to HashMap of currently drawn axes
			dataSets.put(axis, series);
			
			// add XYChart.Series to LineChart
			lineChart.getData().add(series);

			// hide all data point symbols UNLESS they are for the legend
			for (Node n : lineChart.lookupAll(".chart-line-symbol")) {
				if (!n.getStyleClass().contains(".chart-legend-item-symbol")) {
					n.setStyle("-fx-background-color: transparent;");
				}
			}

			// tick the checkbox
			c.setSelected(true);

		// if axis is already graphed:
		} else {

			System.out.println("Removing " + axis);

			// remove XYChart.Series from LineChart
			lineChart.getData().remove(dataSets.get(axis));

			// remove axis & XYChart.Series key-value pair from HashMap
			dataSets.remove(axis);

			// untick the checkbox
			c.setSelected(false);

		}

	}

	/**
	 * Redraws an axis already on the graph.
	 * @param axis the AxisType to be drawn/removed
	 */
	public void updateAxis(AxisType axis) {

		System.out.println("Updating " + axis);

		// retrieve XYChart.Series and ObservableList from HashMap
		XYChart.Series<Number, Number> series = dataSets.get(axis);
		ObservableList<XYChart.Data<Number, Number>> seriesData = series.getData();

		// clear samples in ObservableList
		seriesData.clear();

		// get time/samples data sets
		List<Double> time = genericTestOne.getAxis(axis).getTime();
		List<Double> data = genericTestOne.getAxis(axis).getSamples();

		// create (Time, Data) -> (X,Y) pairs
		for (int i = 0; i < data.size(); i+=resolution) {

			XYChart.Data<Number, Number> dataEl = new XYChart.Data<>(time.get(i), data.get(i));
			
			// add tooltip with (x,y) when hovering over data point
			dataEl.setNode(new DataPointLabel(time.get(i), data.get(i)));

			seriesData.add(dataEl);

		}
		
		// add ObservableList to XYChart.Series
		series.setData(seriesData);

	}

	@FXML
	public void handleReset(ActionEvent event) {
		
		zoomviewX = resetZoomviewX;
		zoomviewY = resetZoomviewY;
		zoomviewW = resetZoomviewW;
		zoomviewH = resetZoomviewH;
		redrawGraph();
		
	}

	@FXML
	public void rollingBlockHandler(ActionEvent event) {
		
		int sampleBlockSize = 0;

		try {
			sampleBlockSize = Integer.parseInt(rollingBlockTextField.getText());
		}
		catch (NumberFormatException e) {

			Alert alert = new Alert(AlertType.ERROR);
			alert.setHeaderText("Invalid input");
			alert.setContentText("Please change your rolling average block size to a numerical value.");

			alert.showAndWait();

		}
		catch (Exception e) {
			e.printStackTrace();
		}

		// workaround to "local variable defined in enclosing scope must be final or effectively final"
		final int blockSize = sampleBlockSize;

		// apply moving avgs to all currently drawn axes
		dataSets.forEach((axis, series) -> {
			genericTestOne.getAxis(axis).applyMovingAvg(blockSize);
			updateAxis(axis);
		});

	}
	
	/**
	 * Called by JavaFX when a data set's checkbox is ticked.
	 */
	@FXML
	public void chooseGraphAxis(ActionEvent event) {

		// get AxisType from checkbox
		CheckBox c = (CheckBox) event.getSource();
		String axis = (String) c.getId().replace("Toggle", "");
		AxisType a = AxisType.valueOf(axis);

		graphAxis(a);

	}

	/**
	 * JavaFX component added to data points on graph.
	 */
	class DataPointLabel extends StackPane {
		
		DataPointLabel(double x, double y) {

			// round to two decimal places
			final double roundedX = Math.round(x * 100.0) / 100.0;
			final double roundedY = Math.round(y * 100.0) / 100.0;

			setPrefSize(15, 15);

			// allows mouse events to pass through label
			// makes selecting nearby data points easier
			setPickOnBounds(false);

			// when mouse hovers over data point, display label
			setOnMouseEntered(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {

					// add the hover (x,y) label
					getChildren().setAll(createLabel(roundedX, roundedY));

					// temporarily draw the data point symbol
					// this is done by removing the "transparent" style
					setStyle("");

					// ensure the label is on top of the graph
					toFront();
				}
				
			});

			// when mouse stops hovering over data point, remove label
			setOnMouseExited(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {

					// hide the data point symbol
					setStyle("-fx-background-color: transparent");

					// hide the label from the graph
					getChildren().clear();
				}
				
			});

		}

		// helper method to generate data point (x,y) label
		private Label createLabel(double x, double y) {

			Label label = new Label("(" + x + ", " + y + ")");

			// add styling to label
			label.getStyleClass().addAll("hover-label");

			// place the label above the data point
			label.translateYProperty().bind(label.heightProperty().divide(-1));

			label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);

      		return label;

		}

	}
	
	/**
	 * Old method of passing data to NewGraph reading from DataOrganizer(s).
	 * @deprecated use {@link #setGenericTests(GenericTest, GenericTest)} instead.
	 */
	@Deprecated
	public void createTest(DataOrganizer d1, DataOrganizer d2) {
		
		// Create GenericTest object if module exists -- otherwise, "null"
		// "null" on one of these differentiates b/t One/Two Module setup
		if (d1 != null) genericTestOne = new GenericTest(d1);
        if (d2 != null) genericTestTwo = new GenericTest(d2);
		
		// TEST CODE - TO BE REPLACED LATER
		// TODO select data set to graph based on type of GenericTest
		// (pendulum -> angular velocity/pos, inclined plane -> AccelX)
        graphAxis(AxisType.AccelX);
		
	}

	//=========================================================
	// DEBUG CODE BELOW USED FOR TESTING -- NOT FOR PRODUCTION
	//=========================================================
	@FXML
	private CheckBox debugIgnoreResCheckbox;
	
	@FXML
	public void debugShowAllSamples(ActionEvent event) {

		CheckBox c = (CheckBox) event.getSource();

		// if checked, set resolution to 1 (no skipping over data samples);
		// otherwise, default to graphing every 20th sample
		resolution = c.isSelected() ? 1 : 20;

		// update all currently displayed graphs
		dataSets.forEach((axis,series) -> {
			updateAxis(axis);
		});
		
	}

	@FXML
	public void debugGraphAxis(ActionEvent event) {
		
		List<String> choices = new ArrayList<>();
		
		for (int i = 0; i < AxisType.values().length; i++) {

			choices.add(AxisType.valueOf(i).toString());

		}

		ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
		dialog.setTitle("[DEBUG] Graph Axis");
		dialog.setHeaderText("This is a testing feature to graph an AxisDataSeries.");
		dialog.setContentText("Choose axis:");

		Optional<String> result = dialog.showAndWait();
		if (result.isPresent()){
			graphAxis(AxisType.valueOf(result.get()));
		}

	}

}