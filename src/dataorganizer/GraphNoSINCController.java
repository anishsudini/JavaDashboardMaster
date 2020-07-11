package dataorganizer;

import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Accordion;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;

public class GraphNoSINCController implements Initializable {

	// GenericTest represents a single module and associated test data
	private ArrayList<GenericTest> genericTests;

	// FIXME "dataSets" to work with multiple modules
	// tracks the axes currently graphed on the line chart
	private ArrayList<GraphData> dataSets;

	// holds all the data set panels instantiated
	private ArrayList<DataSetPanel> panels;

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

	// internal enum identifying the state of data analysis
	private GraphMode mode = GraphMode.NONE;

	// object
	private XYChart.Series<Number,Number> slope;

	// keeps track of first point in secant line calculation
	private Double[] slopePoint;

	// keeps track of first point in area calculation
	private Double[] areaPoint;

	// number of sig figs that labels are rounded to
	// TODO make this an advanced user setting
	private final int SIG_FIGS = 3;

	@FXML
	private BFALineChart<Number,Number> lineChart;

	@FXML
	private BFANumberAxis xAxis;
	
	@FXML
	private BFANumberAxis yAxis;

	@FXML
	private TextField rollingBlockTextField;

	@FXML
	private MultipleAxesLineChart multiAxis;

	@FXML
	private AnchorPane anchorPane;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		System.out.println("Initializing Data Analysis graph...");

		dataSets = new ArrayList<GraphData>();
		panels = new ArrayList<DataSetPanel>();
		genericTests = new ArrayList<GenericTest>();

		slopePoint = new Double[2];
		areaPoint = new Double[2];

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

		lineChart = multiAxis.getBaseChart();
		lineChart.setAnimated(false);
		xAxis = (BFANumberAxis) lineChart.getXAxis();
		yAxis = (BFANumberAxis) lineChart.getYAxis();
	
		// hides symbols indicating data points on graph
		lineChart.setCreateSymbols(false);
		
		redrawGraph();

		// listener that runs every tick the mouse scrolls, calculates zooming
		multiAxis.setOnScroll(new EventHandler<ScrollEvent>() {

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

				if(!event.isAltDown()) {
					zoomviewW -= zoomviewW * event.getDeltaY() / 300;
					
					zoomviewX += zoomviewW * event.getDeltaY() * (leftScrollPercentage - .5) / 300;
				}

				// decreases the zoomview width and height by an amount relative to the scroll and the current size of the zoomview (slows down zooming at high levels of zoom)
				zoomviewH -= zoomviewH * event.getDeltaY() / 300;
				
				// moves the center of the zoomview to accomodate for the zoom, accounts for the position of the mouse to try an keep it in the same spot
				zoomviewY -= zoomviewH * event.getDeltaY() * (topScrollPercentage - .5) / 300;

				redrawGraph();

			}

		});

		// listener that runs every tick the mouse is dragged, calculates panning
		multiAxis.setOnMouseDragged(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {

				if (mode == GraphMode.NONE) {

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

			}

		});

		// listener that runs when the mouse is clicked, only runs once per click, helps to differentiate between drags
		multiAxis.setOnMousePressed(new EventHandler<MouseEvent>() {

			public void handle(MouseEvent event) {

				lastMouseX = event.getX();
				lastMouseY = event.getY();

			}

		});
	}

	/**
	 * Populates the data analysis graph with a single GenericTest.
	 * This constructor should be used for a One Module test.
	 * @param g the GenericTest object storing the test's data
	 */
	public void setGenericTests(GenericTest g) {

		// g1/g2 are allowed to be null here (differentiating One/Two Module setup)
		genericTests.add(g);
		initializePanels();

	}

	/**
	 * Populates the data analysis graph with multiple GenericTests.
	 * This constructor should be used when multiple modules are used in a test.
	 * @param g array of GenericTests (each one represents one module)
	 */
	public void setGenericTests(ArrayList<GenericTest> g) {

		genericTests = g;
		initializePanels();

	}

	/**
	 * Populates the data analysis graph by creating a GenericTest from a CSV and CSVP file.
	 * @param CSVPath the location of the CSV file containing test data
	 * @param CSVPPath the location of the CSVP file containing test parameters
	 */
	public void setGenericTestFromCSV(String CSVPath) {

		// wrapper for array version of CSV reading
		setGenericTestsFromCSV(new ArrayList<String>(Arrays.asList(CSVPath)));

	}

	/**
	 * Populates the data analysis graph by creating a GenericTest from a CSV and CSVP file.
	 * @param CSVPath the location of the CSV file containing test data
	 * @param CSVPPath the location of the CSVP file containing test parameters
	 */
	public void setGenericTestsFromCSV(ArrayList<String> paths) {

		genericTests.clear();

		CSVHandler reader = new CSVHandler();

		for (String s : paths) {
			GenericTest g = new GenericTest(reader.readCSV(s), reader.readCSVP(s + "p"));
			genericTests.add(g);
		}

		initializePanels();

	}

	private void initializePanels() {

		// get reference to root element
		Accordion a = (Accordion) lineChart.getScene().lookup("#dataSetAccordion");

		// remove existing panels
		panels.clear();
		a.getPanes().clear();

		// create data set panels
		for (int i = 0; i < genericTests.size(); i++) {

			// reference to "d" necessary for addListener() anonymous class
			DataSetPanel d = new DataSetPanel(i);
			d.setText("Module " + (i+1));

			// convey checkbox ticking on/off from child class to this class
			d.currentAxis.addListener((obs, oldVal, newVal) -> {

				// TODO part of the hack w/ change listeners
				if (newVal.intValue() == -1) return;	
				graphAxis(AxisType.valueOf(newVal.intValue()), d.getGTIndex());
			
			});

			panels.add(d);
			a.getPanes().add(d);

		}

		// TODO select data set to graph based on type of GenericTest
		// (pendulum -> angular velocity/pos, inclined plane -> AccelX)
		//
		// if opening graph for first time, graph default axis/axes
		if (dataSets.size() == 0) {
			graphAxis(AxisType.AccelX, 0);
		}
		// otherwise, update all currently drawn axes
		else {
			for (GraphData d : dataSets) {
				updateAxis(d.axis, d.GTIndex);
			}
		}

	}

	/**
	 * Handles zooming/panning of the graph.
	 */
	private void redrawGraph() {

		multiAxis.setXBounds(zoomviewX - zoomviewW/2,zoomviewX + zoomviewW/2);
		//xAxisDegrees.setLowerBound(zoomviewX - zoomviewW/2);
		//xAxisDegrees.setUpperBound(zoomviewX + zoomviewW/2);
		multiAxis.setYBounds(zoomviewY - zoomviewH/2,zoomviewY + zoomviewH/2);

		if(zoomviewW > 50) {
			lineChart.setVerticalGridLinesVisible(false);
		} else {
			lineChart.setVerticalGridLinesVisible(true);
		}

		yAxis.setLowerBound(zoomviewY - zoomviewH/2);
		yAxis.setUpperBound(zoomviewY + zoomviewH/2);
		//yAxisDegrees.setLowerBound(5 * zoomviewY - 5*zoomviewH/2);
		//yAxisDegrees.setUpperBound(5 * zoomviewY + 5*zoomviewH/2);
		
		xAxis.setTickUnit(Math.pow(2, Math.floor(Math.log(zoomviewW)/Math.log(2))-2));
		yAxis.setTickUnit(Math.pow(2, Math.floor(Math.log(zoomviewH)/Math.log(2))-3));
		for(Integer i : multiAxis.axisChartMap.keySet()){
			((BFANumberAxis)(multiAxis.axisChartMap.get(i).getYAxis())).setTickUnit(Math.pow(2, Math.floor(Math.log(zoomviewH)/Math.log(2))-3) * multiAxis.getAxisScalar(i));
			((BFANumberAxis)(multiAxis.axisChartMap.get(i).getXAxis())).setTickUnit(Math.pow(2, Math.floor(Math.log(zoomviewH)/Math.log(2))-2));
		}

		// remove data analysis tools (if drawn)
		lineChart.clearArea();
		clearSlope();

	}

	/**
	 * Draws/removes an axis from the graph.
	 * @param axis the AxisType to be drawn/removed
	 * @param GTIndex the GenericTest to read data from
	 */
	public void graphAxis(AxisType axis, int GTIndex) {

		// get checkbox by looking up FXID (the name of the AxisType)
		//CheckBox c = (CheckBox) lineChart.getScene().lookup("#Toggle" + axis);

		// if axis is not already graphed:
		if (findGraphData(GTIndex, axis) == null) {

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

			// get time/samples data sets
			time = genericTests.get(GTIndex).getAxis(axis).getTime();
			data = genericTests.get(GTIndex).getAxis(axis).getSamples();

			// create (Time, Data) -> (X,Y) pairs
			for (int i = 0; i < data.size(); i+=resolution) {

				XYChart.Data<Number, Number> dataEl = new XYChart.Data<>(time.get(i), data.get(i)/multiAxis.getAxisScalar(axis.getValue()));
			
				// add tooltip with (x,y) when hovering over data point
				dataEl.setNode(new DataPointLabel(time.get(i), data.get(i), axis, GTIndex));

				seriesData.add(dataEl);

			}

			// TODO switch this to a pretty-printed version of AxisType?
			series.setName(axis.toString());

			// add ObservableList to XYChart.Series
			series.setData(seriesData);

			GraphData d = new GraphData(GTIndex, axis, series);

			// add to list of currently drawn axes
			dataSets.add(d);

			//add graph with new axis
			multiAxis.addSeries(d, Color.rgb(((axis.getValue() + 20) % 31) * 8,((axis.getValue() + 30) % 31) * 8,((axis.getValue() + 10) % 31) * 8));

			// add XYChart.Series to LineChart
			//lineChart.getData().add(series);

			// hide all data point symbols UNLESS they are for the legend
			for (Node n : lineChart.lookupAll(".chart-line-symbol")) {
				if (!n.getStyleClass().contains(".chart-legend-item-symbol")) {
					n.setStyle("-fx-background-color: transparent;");
				}
			}

			// tick the checkbox
			panels.get(GTIndex).setCheckBox(true);

		// if axis is already graphed:
		} else {

			System.out.println("Removing " + axis);

			// remove XYChart.Series from LineChart
			lineChart.getData().remove(findGraphData(GTIndex, axis).data);

			multiAxis.removeAxis(axis, GTIndex);

			// remove GraphData from list of axes
			dataSets.remove(findGraphData(GTIndex, axis));

			// untick the checkbox
			panels.get(GTIndex).setCheckBox(false);

		}

	}

	/**
	 * Redraws an axis already on the graph.
	 * @param axis the AxisType to be drawn/removed
	 * @param GTIndex the GenericTest to read data from
	 */
	public void updateAxis(AxisType axis, int GTIndex) {

		System.out.println("Updating " + axis);

		// retrieve XYChart.Series and ObservableList from HashMap
		XYChart.Series<Number, Number> series = findGraphData(GTIndex, axis).data;
		ObservableList<XYChart.Data<Number, Number>> seriesData = series.getData();

		// clear samples in ObservableList
		seriesData.clear();

		// get time/samples data sets
		List<Double> time = genericTests.get(GTIndex).getAxis(axis).getTime();
		List<Double> data = genericTests.get(GTIndex).getAxis(axis).getSamples();

		// create (Time, Data) -> (X,Y) pairs
		for (int i = 0; i < data.size(); i+=resolution) {

			XYChart.Data<Number, Number> dataEl = new XYChart.Data<>(time.get(i), data.get(i));

			// add tooltip with (x,y) when hovering over data point
			dataEl.setNode(new DataPointLabel(time.get(i), data.get(i), axis, GTIndex));

			seriesData.add(dataEl);

		}

		// add ObservableList to XYChart.Series
		series.setData(seriesData);

		// hide all data point symbols UNLESS they are for the legend
		for (Node n : lineChart.lookupAll(".chart-line-symbol")) {
			if (!n.getStyleClass().contains(".chart-legend-item-symbol")) {
				n.setStyle("-fx-background-color: transparent;");
			}
		}

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
		for (GraphData d : dataSets) {
			genericTests.get(d.GTIndex).getAxis(d.axis).applyCustomMovingAvg(blockSize);
			updateAxis(d.axis, d.GTIndex);
		}

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

		graphAxis(a, 0);

	}

	@FXML
	public void toggleSlopeMode(ActionEvent event) {

		System.out.println("Toggling slope mode...");

		if (mode == GraphMode.NONE) {
			setGraphMode(GraphMode.SLOPE);
		}
		else if (mode == GraphMode.SLOPE) {
			setGraphMode(GraphMode.NONE);
		}

	}

	@FXML
	public void toggleAreaMode(ActionEvent event) {

		System.out.println("Toggling area mode...");

		if (mode == GraphMode.NONE) {
			setGraphMode(GraphMode.AREA);
		}
		else if (mode == GraphMode.AREA) {
			setGraphMode(GraphMode.NONE);
		}

	}

	@FXML
	public void importCSV(ActionEvent event) {

		// used to load CSV test data directory
		Settings settings = new Settings();
		settings.loadConfigFile();

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Select a CSV");
		fileChooser.setInitialDirectory(new File(settings.getKeyVal("CSVSaveLocation")));

		// filters file selection to CSVs only
		FileChooser.ExtensionFilter filterCSVs = new FileChooser.ExtensionFilter("Select a File (*.csv)", "*.csv");
		fileChooser.getExtensionFilters().add(filterCSVs);

		List<File> files = fileChooser.showOpenMultipleDialog(null);

		// if user doesn't choose a file or closes window, don't continue
		if (files == null) return;

		// keep track of verified CSV/CSVP file paths
		ArrayList<String> paths = new ArrayList<String>();

		// loop through each file, checking for CSVP pair
		for (File f : files) {
			
			String CSVFilePath = f.toString();

			// if no matching CSVP file found, don't continue
			if (!new File(CSVFilePath + "p").exists()) {

				Alert alert = new Alert(AlertType.ERROR);

				alert.setHeaderText("Missing test data");
				alert.setContentText("The matching CSVP file could not be found.");
				alert.showAndWait();

				System.out.println("No matching CSVP file found for '" + CSVFilePath + "'");
				return;

			}

			paths.add(CSVFilePath);

		}

		setGenericTestsFromCSV(paths);

	}

	/**
	 * Sets the graphing mode of the application.
	 * Use this to change between viewing the graph and finding slope/area modes.
	 * @param g the {@link GraphMode} to change to.
	 */
	private void setGraphMode(GraphMode g) {

		mode = g;

		// first index is x, second index is y
		slopePoint = new Double[2];
		areaPoint = new Double[2];

		switch (g) {

			case NONE:
				lineChart.getScene().setCursor(Cursor.DEFAULT);
				break;

			case SLOPE:
			case AREA:
				lineChart.getScene().setCursor(Cursor.CROSSHAIR);
				break;

			default:
				System.out.println("Error setting graph mode");
				break;

		}

	}

	/**
	 * Graphs a line tangent to the given point.
	 */
	public void graphSlope(double x, double y, AxisType axis, int GTIndex) {

		clearSlope();

		// get slope value "m"
		double m = genericTests.get(GTIndex).getAxis(axis).getSlope(x, resolution);

		slope = new XYChart.Series<Number, Number>();
		ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();

		// Formula used is point-slope form of a line:
		// y - y0 = m(x - x0) -> y = m(x - x0) + y0

		// plot the point (x0,y0) shared by the graph and tangent line
		seriesData.add(new XYChart.Data<Number,Number>(x, y));

		// plot the point one x-unit to the left (x = x0-1)
		seriesData.add(new XYChart.Data<Number,Number>(x-1, m * ((x-1)-x) + y));

		// plot the point one x-unit to the right (x = x0+1)
		seriesData.add(new XYChart.Data<Number,Number>(x+1, m * ((x+1)-x) + y));

		// add label for slope value to the center of the line, above the tangent point
		seriesData.get(0).setNode(createSlopeLabel(m));

		slope.setName("Slope (" + axis + ")");
		slope.setData(seriesData);

		// TODO clean up, don't need to recreate XYChart.Series
		lineChart.getData().add(slope);
		slope.getNode().getStyleClass().add("slope-line");

		setGraphMode(GraphMode.NONE);

	}

	/**
	 * Graphs a secant line between the given points.
	 */
	public void graphSlope(double x1, double y1, double x2, double y2, AxisType axis, int GTIndex) {

		clearSlope();

		// get slope value "m"
		double m = genericTests.get(GTIndex).getAxis(axis).getSlope(x1, x2);

		slope = new XYChart.Series<Number, Number>();
		ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();

		// Formula used is point-slope form of a line:
		// y - y0 = m(x - x0) -> y = m(x - x0) + y0

		// plot the left endpoint of the line
		seriesData.add(new XYChart.Data<Number,Number>(x1, y1));

		// plot the midpoint of the line
		seriesData.add(new XYChart.Data<Number, Number>((x1+x2)/2, (y1+y2)/2));

		// plot the right endpoint of the line
		seriesData.add(new XYChart.Data<Number,Number>(x2, y2));

		// add label for slope value above the midpoint
		seriesData.get(1).setNode(createSlopeLabel(m));

		slope.setName("Slope (" + axis + ")");
		slope.setData(seriesData);

		// TODO clean up, don't need to recreate XYChart.Series
		lineChart.getData().add(slope);
		slope.getNode().getStyleClass().add("slope-line");

		setGraphMode(GraphMode.NONE);

	}

	/**
	 * Clears the slope line at/between points (if currently drawn).
	 */
	private void clearSlope() {
		if (slope != null) lineChart.getData().remove(slope);
	}

	/**
	 * Finds a GraphData object given its fields.
	 * @param GTIndex the GenericTest associated with the GraphData
	 * @param axis the AxisType associated with the GraphData
	 */
	private GraphData findGraphData(int GTIndex, AxisType axis) {

		for (GraphData g : dataSets) {
			if (g.GTIndex == GTIndex && g.axis == axis) return g;
		}

		return null;

	}

	/**
	 * Creates the label for the slope of a tangent/secant line.
	 * @param m the value for the slope
	 */
	private StackPane createSlopeLabel(double m) {

		double roundedM = new BigDecimal(m).round(new MathContext(SIG_FIGS)).doubleValue();
		Label label = new Label("Slope: " + roundedM);

		// add styling to label
		label.getStyleClass().addAll("hover-label");

		// place the label above the data point
		label.translateYProperty().bind(label.heightProperty().divide(-1));

		label.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);

		// make label display full floating-point number when clicked
		label.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				label.setText("Slope: " + m);
			}

		});

		// place label in StackPane and return
		StackPane pane = new StackPane();

		pane.setPrefSize(15, 15);
		pane.setStyle("-fx-background-color: transparent");
		pane.getChildren().add(label);

		return pane;

	}

	/**
	 * JavaFX component added to data points on graph.
	 */
	class DataPointLabel extends StackPane {

		DataPointLabel(double x, double y, AxisType axis, int GTIndex) {

			// round to the given number of sig figs
			final double roundedX = new BigDecimal(x).round(new MathContext(SIG_FIGS)).doubleValue();
			final double roundedY = new BigDecimal(y).round(new MathContext(SIG_FIGS)).doubleValue();

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

			setOnMouseClicked(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {

					if (mode == GraphMode.SLOPE) {

						// secant line graphing mode
						if (event.isShiftDown()) {
							System.out.println("Selected first slope point");
							slopePoint = new Double[] {x,y};
						}
						else {
							// graph tangent line
							if (slopePoint[0] == null && slopePoint[1] == null) {
								System.out.println("Graphing tangent line...");
								graphSlope(x, y, axis, 0);
							}
							// graph secant line
							else {
								System.out.println("Graphing secant line...");
								graphSlope(slopePoint[0], slopePoint[1], x, y, axis, 0);
							}
						}

					}
					else if (mode == GraphMode.AREA) {

						// select first point of area calculation
						if (areaPoint[0] == null && areaPoint[1] == null) {
							System.out.println("Selected first area point");
							areaPoint = new Double[] {x,y};
						}
						// calculate and shade area
						else {

							System.out.println("Graphing area...");

							// ensures that x1 is always less than x2
							double[] areaBounds = new double[] {areaPoint[0], x};
							Arrays.sort(areaBounds);

							// calculate the definite integral with the given limits
							double area = genericTests.get(GTIndex).getAxis(axis).getAreaUnder(areaBounds[0], areaBounds[1]);

							// p1 = (x1, y1), p2 = (x2, y2)
							XYChart.Data<Double, Double> p1 = new XYChart.Data<Double, Double>(areaPoint[0], areaPoint[1]);
							XYChart.Data<Double, Double> p2 = new XYChart.Data<Double, Double>(x, y);

							// ensure the lower bound is less than the upper bound
							if (areaPoint[0] == areaBounds[0]) {
								lineChart.graphArea(p1, p2, findGraphData(GTIndex, axis).data.getData(), area, SIG_FIGS);
							}
							else {
								lineChart.graphArea(p2, p1, findGraphData(GTIndex, axis).data.getData(), area, SIG_FIGS);
							}

							setGraphMode(GraphMode.NONE);
						}

					}
					else if (mode == GraphMode.NONE) {
						// display full floating-point number on click
						getChildren().setAll(createLabel(x, y));
					}

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
	 * Internal enum used to designate the state of data analysis;
	 * <p><code>GraphMode.NONE</code> is when the user is zooming/panning,</p>
	 * <p><code>GraphMode.SLOPE</code> is when the user is selecting a single point for a slope calculation,</p>
	 * <p>and <code>GraphMode.Area</code> is when the user is selecting the section for an area calculation.</p>
	 */
	private enum GraphMode {
		NONE,
		SLOPE,
		AREA
	}

	/**
	 * Old method of passing data to the Data Analysis Graph reading from DataOrganizer(s).
	 * @deprecated use {@link #setGenericTests(GenericTest, GenericTest)} instead.
	 */
	@Deprecated
	public void createTest(DataOrganizer d1, DataOrganizer d2) {

		// Create GenericTest object if module exists -- otherwise, "null"
		// "null" on one of these differentiates b/t One/Two Module setup
		if (d2 != null) {
			genericTests.add(new GenericTest(d1));
		}
		else {
			genericTests.add(new GenericTest(d1));
			genericTests.add(new GenericTest(d2));
		}

		// TEST CODE - TO BE REPLACED LATER
		// TODO select data set to graph based on type of GenericTest
		// (pendulum -> angular velocity/pos, inclined plane -> AccelX)
        graphAxis(AxisType.AccelX, 0);

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

		// update all currently drawn axes
		for (GraphData d : dataSets) {
			updateAxis(d.axis, d.GTIndex);
		}

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
		if (result.isPresent()) {
			graphAxis(AxisType.valueOf(result.get()), 0);
		}

	}

}