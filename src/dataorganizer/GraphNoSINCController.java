package dataorganizer;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;


import static java.lang.Thread.getAllStackTraces;

public class GraphNoSINCController implements Initializable {

    private final Rectangle userCreatedZoomRectangleBox = new Rectangle();
    private final Rectangle baselineRect = new Rectangle();


    @FXML
    private LineChart<Number, Number> lineChart;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private NumberAxis yAxis;
    @FXML
    private Pane chartContainer;
    @FXML
    private HBox graphingHbox;
    @FXML
    private Button zoomButton;
    @FXML
    private TitledPane dataSourceTitledPane;
    @FXML
    private TitledPane dataSourceTitledPaneTwo;
    @FXML
    private FlowPane dataDisplayCheckboxesFlowPane;
    @FXML
    private FlowPane dataDisplayCheckboxesFlowPaneTwo;
    @FXML
    private CheckBox displayRawDataCheckbox;
    @FXML
    private CheckBox displaySignedDataCheckbox;
    @FXML
    private CheckBox displayNormalizedDataCheckbox;
    @FXML
    private CheckBox AccelMagnitudeCheckBox;
    @FXML
    private TextField maxYValueTextField;
    @FXML
    private TextField minYValueTextField;
    @FXML
    private Label generalStatusLabel;
    @FXML
    private TextField rollingBlockTextField;
    @FXML
    private TextField accelerometerXAxisOffsetTextField;
    @FXML
    private TextField accelerometerYAxisOffsetTextField;
    @FXML
    private TextField accelerometerZAxisOffsetTextField;
    @FXML
    private TextField baselineLowerBound;
    @FXML
    private TextField baselineUpperBound;
    @FXML
    private Pane backgroundPane;


    private ObservableList<DataSeries> dataSeries = FXCollections.observableArrayList();
    private ObservableList<DataSeries> dataSeriesTwo = FXCollections.observableArrayList();
    private ObservableList<TemplateDataSeries> dataTemplateSeries = FXCollections.observableArrayList();
    private ObservableList<TemplateDataSeries> dataTemplateSeriesTwo = FXCollections.observableArrayList();
    private DataOrganizer[] dataCollector = new DataOrganizer[2];
    private GraphDataOrganizer GDO;
    private String csvFilePath;
    private int XOffsetCounter = 0;
    private int XOffsetCounterTwo = 0;
    private double yMax = 5;
    private double yMin = -5;
    private int numDataSets;
    private String filePath;
    private File fileCopy;
 
    @FXML
    private Button selectFileButton;

    @FXML
    private Accordion dataAccordion;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataAccordion.setExpandedPane(dataSourceTitledPane);
        rectangleColorPicker.setValue(Color.DODGERBLUE);
    }

    public void setDataCollector(DataOrganizer dataCollector, int index) {
        this.dataCollector[index] = dataCollector;
    }

    /*** Event Handlers ***/

    @FXML
    public void handleZoom(ActionEvent event) {                                                                            //TODO
        setUpZooming(userCreatedZoomRectangleBox, lineChart);
        doZoom(userCreatedZoomRectangleBox, lineChart);
    }

    @FXML
    public void handleBaselineRange(ActionEvent event) {                                                                //TODO
        setUpBaselineRangeSelection(baselineRect, lineChart);
    }

    @FXML
    public void handleReset(ActionEvent event) {                                                                        //Resets the Graph to its default parameters (y-Axis scale, x-Axis scale and userCreatedZoomRectangleBox is reset to (0,0))

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                maxYValueTextField.setText("5.0");
                minYValueTextField.setText("-5.0");
            }
        });

        if (dataCollector[0] != null) {
            xAxis.setUpperBound(dataCollector[0].getLengthOfTest());                    //Sets the Graph's x-Axis maximum value to the total time of the test
        }
        if (GDO != null) xAxis.setUpperBound(GDO.getLengthOfTest());
        xAxis.setLowerBound(0);                                                                                            //Sets the Graph's x-Axis minimum value to 0 - the location of the very first data sample
        //yAxis.setUpperBound(yMax);                                                                                        //Sets the Graph's y-Axis maximum value to the defined y-Axis maximum (5 by default - varies based on user entry)
        //yAxis.setLowerBound(yMin);                                                                                       //Sets the Graph's y-Axis minimum value to the defined y-Axis minimum (-5 by default - varies based on user entry)

        yAxis.setUpperBound(5);                                                                                        //Sets the Graph's y-Axis maximum value to the defined y-Axis maximum (5 by default - varies based on user entry)
        yAxis.setLowerBound(-5);

        userCreatedZoomRectangleBox.setWidth(0);                                                                        //Sets the Width of user's drag and drop zoom rectangle back to its original width value (0)
        userCreatedZoomRectangleBox.setHeight(0);                                                                        //Sets the Height of the user's drag and drop zoom rectangle back to its original height value (0)

        if (dataSeries != null) {                                                                                        //If the first data series exists (contains data) ->
            for (final DataSeries axisOfDataSeries : dataSeries) {                                                        //Iterates through each axis of the first data series
                axisOfDataSeries.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());                                //Updates the boundaries of the graph for each axis of the first data series
            }
        }

        if (dataSeriesTwo != null) {                                                                                        //If the second data series exists (contains data) ->
            for (final DataSeries axisOfDataSeriesTwo : dataSeriesTwo) {                                                //Iterates through each axis of the second data series
                axisOfDataSeriesTwo.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());                            //Updates the boundaries of the graph for each axis of the second data series
            }
        }

        if (dataTemplateSeries != null) {
            for (final TemplateDataSeries axisOfTemplate : dataTemplateSeries) {                                                //Iterates through each axis of the second data series
                axisOfTemplate.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());                            //Updates the boundaries of the graph for each axis of the second data series
            }
        }

        repopulateData();                                                                                                    //TODO
        restyleSeries();

    }

    @FXML
    public void handleDisplayRawData(ActionEvent event) {                                                                //Event handler that updates the data series to be raw data, then repopulates it within the lineChart
        displaySignedDataCheckbox.setSelected(false);                                                                    //Deselects the displaySignedDataCheckbox
        //displayNormalizedDataCheckbox.setSelected(false);																		//Deselects the displayRawDataCheckbox
        lineChart.getData().clear();                                                                                    //Removes all series currently displayed on the graph to prevent multiple data series from populating the lineChart
        for (DataSeries axisOfDataSeries : dataSeries) {                                                                    //Iterates through each axis of the first data series
            axisOfDataSeries.setDataConversionType(0);                                                                    //Sets the data conversion type to 0 (0 is the numeral used to indicate a conversion to raw data) and applies the conversion to each axis of the first data series
            axisOfDataSeries.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());                                    //Updates the boundaries of the graph for each axis of the first data series
        }
        populateData(dataSeries, lineChart);                                                                            //Repopulates the lineChart with the updated data within the dataSeries object
        styleSeries(dataSeries, lineChart);
        restyleSeries();//TODO
    }

    @FXML
    public void handleDisplaySignedData(ActionEvent event) {                                                            //Event handler that updates the data series to be signed data, then repopulates it within the lineChart
        displayRawDataCheckbox.setSelected(false);                                                                        //Deselects the displayRawDataCheckbox
        //displayNormalizedDataCheckbox.setSelected(false);																		//Deselects the displayRawDataCheckbox
        lineChart.getData().clear();                                                                                    //Removes all series currently displayed on the graph to prevent multiple data series from populating the lineChart
        for (DataSeries axisOfDataSeries : dataSeries) {                                                                    //Iterates through each axis of the first data series
            axisOfDataSeries.setDataConversionType(1);                                                                    //Sets the data conversion type to 1 (1 is the numeral used to indicate a conversion to signed data) and applies the conversion to each axis of the first data series
            axisOfDataSeries.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());                                    //Updates the boundaries of the graph for each axis of the first data series
        }
        populateData(dataSeries, lineChart);                                                                            //Repopulates the lineChart with the updated data within the dataSeries object
        styleSeries(dataSeries, lineChart);
        restyleSeries();//TODO

    }

    @FXML
    public void handleDisplayNormalizedData(ActionEvent event) {                                                        //Event handler that updates the data series to be signed data, then repopulates it within the lineChart
        displayRawDataCheckbox.setSelected(false);                                                                        //Deselects the displayRawDataCheckbox
        displaySignedDataCheckbox.setSelected(false);                                                                    //Deselects the displayRawDataCheckbox
        lineChart.getData().clear();                                                                                    //Removes all series currently displayed on the graph to prevent multiple data series from populating the lineChart
        for (DataSeries axisOfDataSeries : dataSeries) {                                                                    //Iterates through each axis of the first data series
            axisOfDataSeries.setDataConversionType(2);                                                                    //Sets the data conversion type to 1 (1 is the numeral used to indicate a conversion to signed data) and applies the conversion to each axis of the first data series
            axisOfDataSeries.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());                                    //Updates the boundaries of the graph for each axis of the first data series
        }
        populateData(dataSeries, lineChart);                                                                            //Repopulates the lineChart with the updated data within the dataSeries object
        styleSeries(dataSeries, lineChart);                                                                                //TODO

    }

    @FXML
    public void handleSetYRange(ActionEvent event) {                                                                    //Event handler that sets the maximum and minimum value of the y-Axis

        try {                                                                                                            //Catches all non-valid user inputs such as NaNs and invalid numerals
            yMax = Double.parseDouble(maxYValueTextField.getText());                                                    //Sets the global variable yMax to the input entered by the user - this variable then becomes the default maximum that will be used when the graph is reset using handleReset
            yMin = Double.parseDouble(minYValueTextField.getText());                                                    //Sets the global variable yMin to the input entered by the user - this variable then becomes the default minimum that will be used when the graph is reset using handleReset

            yAxis.setUpperBound(yMax);                                                                                    //Sets the maximum y-Axis value to the value of yMax
            yAxis.setLowerBound(yMin);                                                                                    //Sets the minimum y-Axis value to the value of yMin

            generalStatusLabel.setText("");                                                                                //Clears the error message displayed if an exception is handled

        } catch (NumberFormatException e) {                                                                                //If a number format exception is handled ->
            generalStatusLabel.setText("Enter a valid Y-Axis Value");                                                    //An error message is displayed on the Graphing interface
            maxYValueTextField.setText(Double.toString(yMax));                                                            //The maxYValueTextField is reset to the last valid value held by yMax
            minYValueTextField.setText(Double.toString(yMin));                                                            //The minYValueTextField is reset to the last valid value held by yMin

            yAxis.setUpperBound(yMax);                                                                                    //Sets the maximum y-Axis value to the last valid value of yMax
            yAxis.setLowerBound(yMin);                                                                                    //Sets the minimum y-Axis value to the last valid value of yMin
        }
    }

    @FXML
    public void addTenNullButtonHandler(ActionEvent event) {                                                            //Event handler that shifts the data being displayed on the line chart by +10 data samples
        XOffsetCounter += 10;                                                                                            //Incrementer that increments the amount offset that has been applied to the X-Axis by +10 and stores it in the XOffsetCounter variable
        if (dataSeries != null) {
            for (DataSeries axisOfDataSeries : dataSeries) {                                                            //Iterates through each axis of the data series
                axisOfDataSeries.addNulls(XOffsetCounter);                                                                    //Calls the addNulls method, passing the updated xOffset variable to each axis of data
            }
        }

        if (dataTemplateSeries != null) {
            for (TemplateDataSeries axisOfDataSeries : dataTemplateSeries) {
                axisOfDataSeries.addNulls(XOffsetCounter);
            }
        }

        repopulateData();                                                                                                //TODO
    }

    @FXML
    public void subTenNullButtonHandler(ActionEvent event) {                                                            //Event handler that shifts the data being displayed on the line chart by -10 data samples
        XOffsetCounter -= 10;                                                                                            //Incrementer that increments the amount offset that has been applied to the X-Axis by +10 and stores it in the XOffsetCounter variable
        if (dataSeries != null) {
            for (DataSeries axisOfDataSeries : dataSeries) {                                                            //Iterates through each axis of the data series
                axisOfDataSeries.addNulls(XOffsetCounter);                                                                    //Calls the addNulls method, passing the updated xOffset variable to each axis of data
            }
        }

        if (dataTemplateSeries != null) {
            for (TemplateDataSeries axisOfDataSeries : dataTemplateSeries) {
                axisOfDataSeries.addNulls(XOffsetCounter);
            }
        }
        repopulateData();                                                                                                //TODO
    }

    @FXML
    public void addOneNullButtonHandler(ActionEvent event) {                                                            //Event handler that shifts the data being displayed on the line chart by +1 data samples
        XOffsetCounter += 1;                                                                                            //Incrementer that increments the amount offset that has been applied to the X-Axis by +10 and stores it in the XOffsetCounter variable
        if (dataSeries != null) {
            for (DataSeries axisOfDataSeries : dataSeries) {                                                            //Iterates through each axis of the data series
                axisOfDataSeries.addNulls(XOffsetCounter);                                                                    //Calls the addNulls method, passing the updated xOffset variable to each axis of data
            }
        }

        if (dataTemplateSeries != null) {
            for (TemplateDataSeries axisOfDataSeries : dataTemplateSeries) {
                axisOfDataSeries.addNulls(XOffsetCounter);
            }
        }
        repopulateData();                                                                                                //TODO																							//TODO
    }

    @FXML
    public void subOneNullButtonHandler(ActionEvent event) {                                                            //Event handler that shifts the data being displayed on the line chart by -1 data samples
        XOffsetCounter -= 1;                                                                                            //Incrementer that increments the amount offset that has been applied to the X-Axis by +10 and stores it in the XOffsetCounter variable
        if (dataSeries != null) {
            for (DataSeries axisOfDataSeries : dataSeries) {                                                            //Iterates through each axis of the data series
                axisOfDataSeries.addNulls(XOffsetCounter);                                                                    //Calls the addNulls method, passing the updated xOffset variable to each axis of data
            }
        }

        if (dataTemplateSeries != null) {
            for (TemplateDataSeries axisOfDataSeries : dataTemplateSeries) {
                axisOfDataSeries.addNulls(XOffsetCounter);
            }
        }
        repopulateData();                                                                                                    //TODO
    }

    @FXML
    public void addTenNullButtonHandlerTwo(ActionEvent event) {
        XOffsetCounterTwo += 10;
        if (dataSeriesTwo != null) {
            for (DataSeries axisOfDataSeries : dataSeriesTwo) {
                axisOfDataSeries.addNulls(XOffsetCounterTwo);
            }
        }

        if (dataTemplateSeriesTwo != null) {
            for (TemplateDataSeries axisOfDataSeries : dataTemplateSeriesTwo) {
                axisOfDataSeries.addNulls(XOffsetCounterTwo);
            }
        }
        repopulateData();
    }

    @FXML
    public void subTenNullButtonHandlerTwo(ActionEvent event) {
        XOffsetCounterTwo -= 10;
        if (dataSeriesTwo != null) {
            for (DataSeries axisOfDataSeries : dataSeriesTwo) {
                axisOfDataSeries.addNulls(XOffsetCounterTwo);
            }
        }

        if (dataTemplateSeriesTwo != null) {
            for (TemplateDataSeries axisOfDataSeries : dataTemplateSeriesTwo) {
                axisOfDataSeries.addNulls(XOffsetCounterTwo);
            }
        }
        repopulateData();
    }

    @FXML
    public void addOneNullButtonHandlerTwo(ActionEvent event) {
        XOffsetCounterTwo += 1;
        if (dataSeriesTwo != null) {
            for (DataSeries axisOfDataSeries : dataSeriesTwo) {
                axisOfDataSeries.addNulls(XOffsetCounterTwo);
            }
        }

        if (dataTemplateSeriesTwo != null) {
            for (TemplateDataSeries axisOfDataSeries : dataTemplateSeriesTwo) {
                axisOfDataSeries.addNulls(XOffsetCounterTwo);
            }
        }
        repopulateData();
    }

    @FXML
    public void subOneNullButtonHandlerTwo(ActionEvent event) {
        XOffsetCounterTwo -= 1;
        if (dataSeriesTwo != null) {
            for (DataSeries axisOfDataSeries : dataSeriesTwo) {
                axisOfDataSeries.addNulls(XOffsetCounterTwo);
            }
        }

        if (dataTemplateSeriesTwo != null) {
            for (TemplateDataSeries axisOfDataSeries : dataTemplateSeriesTwo) {
                axisOfDataSeries.addNulls(XOffsetCounterTwo);
            }
        }
        repopulateData();
    }

    /**
     * This method is responsible for loading in the graph part of the SINC Technology
     * Opens a file chooser in which the user selects the CSV file of the data the module collected
     * @param event
     */
    @FXML
    public void importCSV(ActionEvent event) {                                                                                                        //Event handler that imports an external CSV into the graphing interface

        try {                                                                                                                                        //Try/Catch that catches Null Pointer Exception when no file is selected
            FileChooser fileChooser = new FileChooser();                                                                                            //Creates a FileChooser Object
            fileChooser.setTitle("Select a CSV");                                                                                                    //Sets the title of the FileChooser object
            Settings settings = new Settings();
            settings.loadConfigFile();
            fileChooser.setInitialDirectory(new File(settings.getKeyVal("CSVSaveLocation")));
            FileChooser.ExtensionFilter filterCSVs = new FileChooser.ExtensionFilter("Select a File (*.csv)", "*.csv");        //Creates a filter object that restricts the available files within the FileChooser window strictly CSV files
            fileChooser.getExtensionFilters().add(filterCSVs);                                                                                        //Adds the filter to the FileChooser
            File fileChosen = fileChooser.showOpenDialog(null);                                                                        //Assigns the user's selected file to the fileChosen variable

            if (fileChosen == null) return;
            csvFilePath = fileChosen.toString();                                                                                                    //Converts the file path assigned to the fileChosen variable to a string and assigns it to the csvFilePath variable

            if (csvFilePath != null) {                                                                                                                //Checks to make sure the given file path contains a valid value
                System.out.println(csvFilePath);
                loadCSVData();                                                                                                                        //Calls the loadCSV method
            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        }                                                                                                            //Catches the NullPointer exception
    }

    @FXML
    public void magnitudeDatasetOneCheckBoxHandler(ActionEvent event) {
        if (dataCollector[0] != null) {
            if (AccelMagnitudeCheckBox.isSelected()) {
                dataSeries.add(9, new DataSeries(dataCollector[0], 10));
                dataSeries.get(9).setActive(true);
            } else {
                dataSeries.get(9).setActive(false);
            }

            repopulateData();
            restyleSeries();
        } else {
            generalStatusLabel.setText("Please add a data set.");
        }
    }

    @FXML
    public void magnitudeDatasetTwoCheckBoxHandler(ActionEvent event) {
        if (dataCollector[1] != null) {
            if (AccelMagnitudeCheckBox.isSelected()) {
                dataSeries.add(9, new DataSeries(dataCollector[1], 10));
                dataSeries.get(9).setActive(true);
            } else {
                dataSeries.get(9).setActive(false);
            }

            repopulateData();
            restyleSeries();
        } else {
            generalStatusLabel.setText("Please add a second data set.");
        }
    }
    

    public void setCsvFilePath(String filePath){
        csvFilePath = filePath;
    }


    public void loadCSVData() {
        createListenersResize();
        DataOrganizer dataOrgoObject = new DataOrganizer();
        dataOrgoObject.createDataSamplesFromCSV(csvFilePath);
        dataOrgoObject.getSignedData();
        dataOrgoObject.setSourceID(new File(csvFilePath).getName(), 1);

        this.dataCollector[numDataSets] = dataOrgoObject;

        if (numDataSets == 0)
            dataSourceTitledPane.setText("CSV File: " + dataOrgoObject.getSourceId());
        else
            dataSourceTitledPaneTwo.setText("CSV File: " + dataOrgoObject.getSourceId());

        if (numDataSets == 0)
            for (int numDof = 1; numDof < 10; numDof++) {
                dataSeries.add(numDof - 1, new DataSeries(dataOrgoObject, numDof));
            }
        else
            for (int numDof = 1; numDof < 10; numDof++) {
                dataSeriesTwo.add(numDof - 1, new DataSeries(dataOrgoObject, numDof));
            }

        dataSeries.get(0).setActive(true);

        if (numDataSets == 0) {
            populateData(dataSeries, lineChart);
            styleSeries(dataSeries, lineChart);
        } else {
            populateData(dataSeriesTwo, lineChart);
            styleSeries(dataSeriesTwo, lineChart);
        }

        xAxis.setUpperBound(dataCollector[numDataSets].getLengthOfTest());
        xAxis.setLowerBound(0);

        userCreatedZoomRectangleBox.setManaged(true);
        userCreatedZoomRectangleBox.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.5));
        baselineRect.setManaged(true);
        baselineRect.setFill(Color.LIGHTGOLDENRODYELLOW.deriveColor(0, 1, 1, 0.5));
        chartContainer.getChildren().remove(userCreatedZoomRectangleBox);
        chartContainer.getChildren().add(userCreatedZoomRectangleBox);
        chartContainer.getChildren().remove(baselineRect);
        chartContainer.getChildren().add(baselineRect);

        setUpZooming(userCreatedZoomRectangleBox, lineChart);

        if (numDataSets == 0) {
            for (final DataSeries axisOfDataSeries : dataSeries) {
                final CheckBox dataToDisplayCheckBox = new CheckBox(axisOfDataSeries.getName());
                dataToDisplayCheckBox.setSelected(false);
                if (axisOfDataSeries.dof == 1) dataToDisplayCheckBox.setSelected(true);
                dataToDisplayCheckBox.setPadding(new Insets(5));
                // Line line = new Line(0, 10, 50, 10);

                // box.setGraphic(line);
                dataDisplayCheckboxesFlowPane.getChildren().add(dataToDisplayCheckBox);
                dataToDisplayCheckBox.setOnAction(action -> {
                    axisOfDataSeries.setActive(dataToDisplayCheckBox.isSelected());
                    repopulateData();
                    restyleSeries();
                });
            }
        } else {
            for (final DataSeries axisOfDataSeries : dataSeriesTwo) {
                dataSourceTitledPaneTwo.setDisable(false);
                dataSourceTitledPaneTwo.setExpanded(true);
                final CheckBox dataToDisplayCheckBoxTwo = new CheckBox(axisOfDataSeries.getName());
                dataToDisplayCheckBoxTwo.setSelected(false);
                if (axisOfDataSeries.dof == 1) dataToDisplayCheckBoxTwo.setSelected(true);
                dataToDisplayCheckBoxTwo.setPadding(new Insets(5));
                // Line line = new Line(0, 10, 50, 10);

                // box.setGraphic(line);
                dataDisplayCheckboxesFlowPaneTwo.getChildren().add(dataToDisplayCheckBoxTwo);
                dataToDisplayCheckBoxTwo.setOnAction(action -> {
                    axisOfDataSeries.setActive(dataToDisplayCheckBoxTwo.isSelected());
                    repopulateData();
                    restyleSeries();
                });
            }
        }

        final BooleanBinding disableControls = userCreatedZoomRectangleBox.widthProperty().lessThan(5).or(userCreatedZoomRectangleBox.heightProperty().lessThan(0));
        zoomButton.disableProperty().bind(disableControls);

        if (maxYValueTextField.getText().equals("") && minYValueTextField.getText().equals("")) {
            maxYValueTextField.setText(Double.toString(yMax));
            minYValueTextField.setText(Double.toString(yMin));
        }
        numDataSets++;
        restyleSeries();
    }

    @FXML
    public void rollingBlockHandler(ActionEvent event) {
        int rollingBlockValue = Integer.parseInt(rollingBlockTextField.getText());
        if (rollingBlockValue == 0) return;
        for (DataSeries axisOfDataSeries : dataSeries) {
            axisOfDataSeries.rollingBlock(rollingBlockValue);
            axisOfDataSeries.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());
        }

        for (DataSeries axisOfDataSeries : dataSeriesTwo) {
            axisOfDataSeries.rollingBlock(rollingBlockValue);
            axisOfDataSeries.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());
        }

        for (TemplateDataSeries axisOfDataSeries : dataTemplateSeries) {
            axisOfDataSeries.rollingBlock(rollingBlockValue);
            axisOfDataSeries.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());
        }

        for (TemplateDataSeries axisOfDataSeries : dataTemplateSeriesTwo) {
            axisOfDataSeries.rollingBlock(rollingBlockValue);
            axisOfDataSeries.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());
        }

        repopulateData();
        restyleSeries(); //restyle series so color can get kept
    }

    @FXML
    public void applyAccelerometerOffsets(ActionEvent event) {
        try {
            Double[] axisAccel = {(double) Integer.parseInt(accelerometerXAxisOffsetTextField.getText()), (double) Integer.parseInt(accelerometerYAxisOffsetTextField.getText()), (double) Integer.parseInt(accelerometerZAxisOffsetTextField.getText())};

            for (int i = 0; i < 3; i++) {
                if (axisAccel[i] > 32768) {
                    axisAccel[i] -= 65535;
                }
                axisAccel[i] = axisAccel[i] * dataCollector[0].accelSensitivity / 32768;

                dataSeries.get(i).dataOrgo.getSignedData();
                dataSeries.get(i).applyCalibrationOffset(axisAccel[i]);
                dataSeries.get(i).updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());
            }

            repopulateData();
            restyleSeries();

        } catch (NumberFormatException e) {

            generalStatusLabel.setText("Enter a number");

        }
    }

    @FXML
    public void clearDataAll() {
        lineChart.getData().clear();    //Removes the data attached to the lineChart object
        dataSourceTitledPane.setText("");   //Removes the name of the file being displayed on dataSourceTitledPane
        dataSourceTitledPaneTwo.setText("");    //Removes the name of the file being displayed on dataSourceTitledPaneTwo
        dataDisplayCheckboxesFlowPane.getChildren().clear();    //Removes all of the checkboxes generated when the First Data Series is imported to the Graphing Interface
        dataDisplayCheckboxesFlowPaneTwo.getChildren().clear();     //Removes all of the checkboxes generated when the First Data Series is imported to the Graphing Interface
        dataSeries = FXCollections.observableArrayList();
        dataSeriesTwo = FXCollections.observableArrayList();
        numDataSets = 0;    //Sets numDataSets to 0 to indicate that zero active data sets are currently loaded
    }

    @FXML
    public void clearDataSetOne() {
        lineChart.getData().clear();
        lineChart.getData().removeAll(dataSeries);  //Removes the First Data Series from the linechart object
        dataDisplayCheckboxesFlowPane.getChildren().removeAll();    //Removes all of the checkboxes generated when the First Data Series is imported to the Graphing Interface
        dataSourceTitledPane.setText("");   //Removes the name of the file being displayed on dataSourceTitledPane
        dataDisplayCheckboxesFlowPane.getChildren().clear();
        dataSeries = FXCollections.observableArrayList();
        numDataSets--;
    }

    @FXML
    public void clearDataSetTwo() {
        lineChart.getData().removeAll(dataSeriesTwo);
        dataDisplayCheckboxesFlowPaneTwo.getChildren().removeAll();
        dataSourceTitledPaneTwo.setText("");
        numDataSets--;
    }

    /* Media Player Controls*/


    @SuppressWarnings("rawtypes")
    public void createListenersResize() {
        graphingHbox.heightProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue arg0, Object arg1, Object arg2) {
                double height = (double) arg2;
                lineChart.setPrefHeight(height - 100);
            }
        });
        graphingHbox.widthProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue arg0, Object arg1, Object arg2) {
                double width = (double) arg2;
                lineChart.setPrefWidth(width - width / 4);
            }
        });
    }

    /*** Method for Preloading All Settings***/

    public void graphSettingsOnStart(String moduleSerialID) {
        createListenersResize();
        dataSourceTitledPane.setText("Module Serial ID: " + moduleSerialID);
        xAxis.setUpperBound(dataCollector[numDataSets].getLengthOfTest());
        xAxis.setMinorTickCount(dataCollector[numDataSets].getSampleRate() / 16);

        lineChart.setTitle(dataCollector[numDataSets].getName());

        for (int numDof = 1; numDof < 10; numDof++) {
            dataSeries.add(numDof - 1, new DataSeries(dataCollector[numDataSets], numDof));
        }

        dataSeries.get(0).setActive(true);

        populateData(dataSeries, lineChart);
        styleSeries(dataSeries, lineChart);

        userCreatedZoomRectangleBox.setManaged(true);
        userCreatedZoomRectangleBox.setFill(Color.LIGHTBLUE.deriveColor(0, 1, 1, 0.5));
        chartContainer.getChildren().add(userCreatedZoomRectangleBox);

        setUpZooming(userCreatedZoomRectangleBox, lineChart);

        for (final DataSeries axisOfDataSeries : dataSeries) {
            final CheckBox dataToDisplayCheckBox = new CheckBox(axisOfDataSeries.getName());
            dataToDisplayCheckBox.setSelected(false);
            if (axisOfDataSeries.dof == 1) dataToDisplayCheckBox.setSelected(true);
            dataToDisplayCheckBox.setPadding(new Insets(5));
            // Line line = new Line(0, 10, 50, 10);

            // box.setGraphic(line);
            dataDisplayCheckboxesFlowPane.getChildren().add(dataToDisplayCheckBox);
            dataToDisplayCheckBox.setOnAction(action -> {
                axisOfDataSeries.setActive(dataToDisplayCheckBox.isSelected());
                repopulateData();
                restyleSeries();
            });
        }

        final BooleanBinding disableControls = userCreatedZoomRectangleBox.widthProperty().lessThan(5).or(userCreatedZoomRectangleBox.heightProperty().lessThan(0));
        zoomButton.disableProperty().bind(disableControls);

        if (maxYValueTextField.getText().equals("") && minYValueTextField.getText().equals("")) {
            maxYValueTextField.setText(Double.toString(yMax));
            minYValueTextField.setText(Double.toString(yMin));
        }

        numDataSets++;
    }

    /*** creates the Frame-By-Frame Analysis Rectangle ***/


    

    public void setUpBaselineRangeSelection(final Rectangle rect, final Node zoomingNode) {
        final ObjectProperty<Point2D> mouseAnchor = new SimpleObjectProperty<>();
        zoomingNode.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mouseAnchor.set(new Point2D(event.getX(), event.getY()));
                rect.setWidth(0);
                rect.setHeight(0);
                zoomingNode.removeEventHandler(MouseEvent.MOUSE_PRESSED, this);
            }
        });
        zoomingNode.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double x = event.getX();
                double y = event.getY();
                rect.setX(Math.min(x, mouseAnchor.get().getX()));
                rect.setY(Math.min(y, mouseAnchor.get().getY()));
                rect.setWidth(Math.abs(x - mouseAnchor.get().getX()));
                rect.setHeight(Math.abs(y - mouseAnchor.get().getY()));
                zoomingNode.removeEventHandler(MouseEvent.MOUSE_DRAGGED, this);
                getBaselineRange(rect, (LineChart<Number, Number>) zoomingNode);
            }
        });
    }

    public void getBaselineRange(Rectangle rect, LineChart<Number, Number> chart) {
        Point2D zoomTopLeft = new Point2D(userCreatedZoomRectangleBox.getX(), userCreatedZoomRectangleBox.getY());
        Point2D zoomBottomRight = new Point2D(userCreatedZoomRectangleBox.getX() + userCreatedZoomRectangleBox.getWidth(), userCreatedZoomRectangleBox.getY() + userCreatedZoomRectangleBox.getHeight());
        final NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        Point2D yAxisInScene = yAxis.localToScene(0, 0);
        final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        Point2D xAxisInScene = xAxis.localToScene(0, 0);
        double xOffset = zoomTopLeft.getX() - yAxisInScene.getX();
        double yOffset = zoomBottomRight.getY() - xAxisInScene.getY();
        double xAxisScale = xAxis.getScale();
        double yAxisScale = yAxis.getScale();

    }

    /*** Sets Up and Performs Zooming ***/


    private void setUpZooming(final Rectangle rect, final Node zoomingNode) {
        final ObjectProperty<Point2D> mouseAnchor = new SimpleObjectProperty<>();
        zoomingNode.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mouseAnchor.set(new Point2D(event.getX(), event.getY()));
                rect.setWidth(0);
                rect.setHeight(0);
                zoomingNode.removeEventHandler(MouseEvent.MOUSE_PRESSED, this);
            }
        });
        zoomingNode.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                double x = event.getX();
                double y = event.getY();
                rect.setX(Math.min(x, mouseAnchor.get().getX()));
                rect.setY(Math.min(y, mouseAnchor.get().getY()));
                rect.setWidth(Math.abs(x - mouseAnchor.get().getX()));
                rect.setHeight(Math.abs(y - mouseAnchor.get().getY()));
                zoomingNode.removeEventHandler(MouseEvent.MOUSE_DRAGGED, this);
            }
        });
    }

    private void doZoom(Rectangle userCreatedZoomRectangleBox, LineChart<Number, Number> chart) {
        Point2D zoomTopLeft = new Point2D(userCreatedZoomRectangleBox.getX(), userCreatedZoomRectangleBox.getY());
        Point2D zoomBottomRight = new Point2D(userCreatedZoomRectangleBox.getX() + userCreatedZoomRectangleBox.getWidth(), userCreatedZoomRectangleBox.getY() + userCreatedZoomRectangleBox.getHeight());
        final NumberAxis yAxis = (NumberAxis) chart.getYAxis();
        Point2D yAxisInScene = yAxis.localToScene(0, 0);
        final NumberAxis xAxis = (NumberAxis) chart.getXAxis();
        Point2D xAxisInScene = xAxis.localToScene(0, 0);
        double xOffset = zoomTopLeft.getX() - yAxisInScene.getX() - 0;
        double yOffset = zoomBottomRight.getY() - xAxisInScene.getY() + 103;
        double xAxisScale = xAxis.getScale();
        double yAxisScale = yAxis.getScale();
        xAxis.setLowerBound((xAxis.getLowerBound() + xOffset / xAxisScale));
        xAxis.setUpperBound(xAxis.getLowerBound() + userCreatedZoomRectangleBox.getWidth() / xAxisScale);
        yAxis.setLowerBound((yAxis.getLowerBound() + yOffset / yAxisScale));
        yAxis.setUpperBound(yAxis.getLowerBound() - userCreatedZoomRectangleBox.getHeight() / yAxisScale);
        userCreatedZoomRectangleBox.setWidth(0);
        userCreatedZoomRectangleBox.setHeight(0);


        for (final DataSeries axisOfDataSeries : dataSeries) {
            if (axisOfDataSeries.isActive()) {
                axisOfDataSeries.updateZoom(xAxis.getLowerBound(), xAxis.getUpperBound());
            }
        }

        xAxis.setTickUnit(1);

        System.out.println(xAxis.getLowerBound());
        System.out.println(xAxis.getUpperBound());

    }

    /*** Data Handling and Functionality Components***/

    private void populateData(final ObservableList<DataSeries> axisOfDataSeries, final LineChart<Number, Number> lineChart) {
        for (DataSeries data : axisOfDataSeries) {
            if (data.isActive()) {
                lineChart.getData().addAll(data.getSeries());
            }
        }
    }

    private void populateTemplateData(final ObservableList<TemplateDataSeries> axisOfDataSeries, final LineChart<Number, Number> lineChart) {
        for (TemplateDataSeries data : axisOfDataSeries) {
            if (data.isActive()) {
                lineChart.getData().addAll(data.getSeries());
            }
        }
    }

    /**
     *
     */

    private void repopulateData() {
        lineChart.getData().clear();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        for (DataSeries data : dataSeries) {
            if (data.isActive()) {
                lineChart.getData().addAll(data.getSeries());
            }
        }
        for (DataSeries data : dataSeriesTwo) {
            if (data.isActive()) {
                lineChart.getData().addAll(data.getSeries());
            }
        }
        for (TemplateDataSeries data : dataTemplateSeries) {
            if (data.isActive()) {
                lineChart.getData().addAll(data.getSeries());
            }
        }
        for (TemplateDataSeries data : dataTemplateSeriesTwo) {
            if (data.isActive()) {
                lineChart.getData().addAll(data.getSeries());
            }
        }
    }


    /**
     * This method is responsible for helping to change the Color of the each dateSeries, or the lines shown on the graph.
     * The method takes in the name of a series and returns the color selected for that series.
     * The color is not the color type, but rather a hexidecimal string that represents the color. This string is used as a CSS modifier later to actually change the color.
     * @param seriesType
     * @return
     */

    public String changeColorofSeries(String seriesType) {
        if (seriesType == "Accel X") { // There is a if statement for each possible dof, as the color of each individual dataSeries can be adjusted.
            try{
                return "#" + ColorPaletteController.xAccelColor.substring(2,8); // If the "Accel X" is passed to the method, the color of Accel X line wants to be changed. Therefore, the value of the Color picker corresponding to the X Accel is returned.
            }catch(Exception e){
                return "#" + Color.RED.toString().substring(2,8);
            }
        }
        if (seriesType == "Accel Y") {
            try {
                return "#" + ColorPaletteController.yAccelColor.substring(2, 8);
            }catch(Exception e){
                return "#" + Color.DODGERBLUE.toString().substring(2,8);
            }
        }
        if (seriesType == "Accel Z") {
            try {
                return "#" + ColorPaletteController.zAccelColor.substring(2, 8);
            }catch(Exception e){
                return "#" + Color.FORESTGREEN.toString().substring(2,8);
            }
        }
        if (seriesType == "Gyro X") {
            try {
                return "#" + ColorPaletteController.xGyroColor.substring(2, 8);
            }catch(Exception e){
                return "#" + Color.GOLD.toString().substring(2,8);
            }
        }
        if (seriesType == "Gyro Y") {
            try{
            return "#" + ColorPaletteController.yGyroColor.substring(2,8);
            }
            catch(Exception e){
                return "#" + Color.CORAL.toString().substring(2,8);
            }
        }
        if (seriesType == "Gyro Z") {
            try {
                return "#" + ColorPaletteController.zGyroColor.substring(2, 8);
            }catch(Exception e) {
                return "#" + Color.MEDIUMBLUE.toString().substring(2,8);
            }
        }
        if (seriesType == "Mag X") {
            try {
                return "#" + ColorPaletteController.xMagColor.substring(2, 8);
            }catch(Exception e){
                return "#" + Color.DARKVIOLET.toString().substring(2,8);
            }
        }
        if (seriesType == "Mag Y") {
            try {
                return "#" + ColorPaletteController.yMagColor.substring(2, 8);
            }catch(Exception e){
                return "#" + Color.DARKSLATEGRAY.toString().substring(2,8);
            }
        }
        if (seriesType == "Mag Z") {
            try {
                return "#" + ColorPaletteController.zMagColor.substring(2, 8);
            }catch(Exception e){
                return "#" + Color.SADDLEBROWN.toString().substring(2,8);
            }
        }
        if (seriesType == "Accel Magnitude") {
            try {
                return "#" + ColorPaletteController.accelMagColor.substring(2, 8);
            }catch(Exception e){
                return "#" + Color.BLACK.toString().substring(2,8);
            }
        }
        //Note: Each Color Picker has a default value so a value will always be returned.
        return "#ffff00"; // This should never be returned, but is here to make sure the method always returns something.
    }

    /**
     * This method is responsible for updating the dataSeries that are shown on the lineGraph.
     *
     */

    public void restyleSeries() {
        // force a css layout pass to ensure that subsequent lookup calls work.
        lineChart.applyCss();

        int nSeries = 0; // A variable that represents which # of the selected series is being modified.
        for (DataSeries dof : dataSeries) { // For each dof in dataSeries
            if (!dof.isActive()) continue;  // Program only continues if the specific dof is ticked in the UI.
            for (int j = 0; j < dof.getSeries().size(); j++) { // iterates through the entire dof within the series.
                XYChart.Series<Number, Number> series = dof.getSeries().get(j);
                Set<Node> nodes = lineChart.lookupAll(".series" + nSeries);
                for (Node n : nodes) {
                    StringBuilder style = new StringBuilder(); // String builder object on which style changes are appended.
                    style.append("-fx-stroke: " + changeColorofSeries(dof.getName()) + "; -fx-background-color: " + changeColorofSeries(dof.getName()) + ", white; "); //dof.getName returns the name of the current dof, This name is passed to the changeColorofSeries Method which returns the corresponding color string. These styles are appended to the style string.
                    n.setStyle(style.toString()); // Sets the style of the dof to the string, which includes all the appended style changes.
                    n.toFront();
                }
                nSeries++; // moves to the next selected dof.
            }
        }
        /*
        The same is done for all dataSeries
         */
        for (DataSeries dof : dataSeriesTwo) {
            if (!dof.isActive()) continue;
            for (int j = 0; j < dof.getSeries().size(); j++) {
                XYChart.Series<Number, Number> series = dof.getSeries().get(j);
                Set<Node> nodes = lineChart.lookupAll(".series" + nSeries);
                for (Node n : nodes) {
                    StringBuilder style = new StringBuilder();
                    style.append("-fx-stroke: " + changeColorofSeries(dof.getName()) + "; -fx-background-color: " + changeColorofSeries(dof.getName()) + ", white; ");
                    n.setStyle(style.toString());
                    n.toFront();
                }
                nSeries++;
            }
        }
        for (TemplateDataSeries dof : dataTemplateSeries) {
            if (!dof.isActive()) continue;
            for (int j = 0; j < dof.getSeries().size(); j++) {
                XYChart.Series<Number, Number> series = dof.getSeries().get(j);
                Set<Node> nodes = lineChart.lookupAll(".series" + nSeries);
                for (Node n : nodes) {
                    StringBuilder style = new StringBuilder();
                    style.append("-fx-stroke: " + changeColorofSeries(dof.getName()) + "; -fx-background-color: " + changeColorofSeries(dof.getName()) + ", white; ");
                    n.setStyle(style.toString());
                    n.toFront();
                }
                nSeries++;
            }
        }
        for (TemplateDataSeries dof : dataTemplateSeriesTwo) {
            if (!dof.isActive()) continue;
            for (int j = 0; j < dof.getSeries().size(); j++) {
                XYChart.Series<Number, Number> series = dof.getSeries().get(j);
                Set<Node> nodes = lineChart.lookupAll(".series" + nSeries);
                for (Node n : nodes) {
                    StringBuilder style = new StringBuilder();
                    style.append("-fx-stroke: " + changeColorofSeries(dof.getName()) + "; -fx-background-color: " + changeColorofSeries(dof.getName()) + ", white; ");
                    n.setStyle(style.toString());
                    n.toFront();
                }
                nSeries++;
            }
        }
    }

    /**
     * Responsible for the initial styling of the the series.
     *
     * @param dataSeries
     * @param lineChart
     */

    private void styleSeries(ObservableList<DataSeries> dataSeries, final LineChart<Number, Number> lineChart) {
        // force a css layout pass to ensure that subsequent lookup calls work.
        lineChart.applyCss();

        int nSeries = 0;
        for (DataSeries dof : dataSeries) {
            if (!dof.isActive()) continue;
            for (int j = 0; j < dof.getSeries().size(); j++) {
                XYChart.Series<Number, Number> series = dof.getSeries().get(j);
                Set<Node> nodes = lineChart.lookupAll(".series" + nSeries);
                for (Node n : nodes) {
                    StringBuilder style = new StringBuilder();
                n.setStyle(style.toString());
                }
                nSeries++;
            }
        }
    }

    /**
     *
     * @param name
     * @param data
     * @return
     */

    private ObservableList<XYChart.Series<Number, Number>> createSeries(String name, List<List<Double>> data) {
        XYChart.Series<Number, Number> series = new XYChart.Series<>();
        series.setName(name);
        ObservableList<XYChart.Data<Number, Number>> seriesData = FXCollections.observableArrayList();

        for (int j = 0; j < data.get(0).size() && j < data.get(1).size(); j++) {
            seriesData.add(new XYChart.Data<>(data.get(0).get(j), data.get(1).get(j)));
        }

        series.setData(seriesData);

        return FXCollections.observableArrayList(Collections.singleton(series));
    }

    

    public class TemplateDataSeries {
        private ObservableList<XYChart.Series<Number, Number>> series;
        private GraphDataOrganizer GDO;
        private int index;
        private String name;
        private String color;
        private boolean active;

        public TemplateDataSeries(String origin, GraphDataOrganizer GraphDataOrganizerObj, int index) {
            this.index = index;
            GDO = GraphDataOrganizerObj;
            switch (this.index) {
                case (0):
                    name = origin + "Momentum X";
                    active = true;
                    break;
                case (1):
                    name = origin + "Momentum Y";
                    active = false;
                    break;
                case (2):
                    name = origin + "Momentum Z";
                    active = false;
                    break;
            }

            series = createSeries(name, GDO.getZoomedSeries(0, index));

        }

        /*
         * offsets the data in one direction or another. Add nulls on the front to move right (positive), remove data points to move left.
         */
        public void addNulls(int offset) {
            List<List<Double>> seriesData = new ArrayList<List<Double>>();
            List<Double> timeAxis = new ArrayList<Double>();
            List<Double> dataAxis = new ArrayList<Double>();

            timeAxis.addAll(GDO.createTimeAxis(xAxis.getLowerBound()));

            for (int i = 0; i < GDO.samples.get(index).size() + offset; i++) { //Loop to "end of data (int given axis) + offset"
                if (offset >= i) { //if offset is still greater than the current sample (i) continue adding padding
                    dataAxis.add(0, null);
                    continue;
                }
                dataAxis.add(i, GDO.samples.get(index).get(i - offset)); //If we have enough padding, start adding the samples
            }

            seriesData.add(timeAxis);
            seriesData.add(dataAxis);

            series = createSeries(name, seriesData); //create a series for the linechart
        }

        public String getColor() {
            return color;
        }

        public void updateZoom(double start, double end) {
            series = createSeries(name, GDO.updateZoom(start, end, index));
        }

        public ObservableList<XYChart.Series<Number, Number>> getSeries() {
            return series;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public String getName() {
            return name;
        }

        public void rollingBlock(int rollRange) {
            GDO.rollingBlock(rollRange, index);
        }
    }


    ColorPaletteController colorPaletteController;
    Color[] lineColors;

    /**
     * Responsible for opening a separate window to change the colors of each dof in the dataSeries.
     * @param event
     */
    @FXML
    public void openLineColorPalette(ActionEvent event) {
        colorPaletteController = startColorPalette();
    }


    /**
     * Acts as a Main Class for the Color Palette FXML used to restyle the colors of the curves
     * @return
     */
    public ColorPaletteController startColorPalette() {
        Stage primaryStage = new Stage();
        Parent root = null;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("ColorPalette.fxml"));
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (root != null) primaryStage.setScene(new Scene(root, 600, 550));

        primaryStage.setTitle("Color Palette");
        primaryStage.show();
        primaryStage.setResizable(false);

        return loader.getController();
    }


    @FXML
    ColorPicker rectangleColorPicker;


    /**
     * Updates the colors when the update color button is pressed
     * @param event
     */
    @FXML
    public void updateColors(ActionEvent event){

        try{
            restyleSeries();                                                                                            // restyleseries updates the colors.
        }catch(Exception e){
            generalStatusLabel.setText("Please Select Colors to be Updated");
        }
    }
}





