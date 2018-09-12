package dataorganizer;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JTabbedPane;
import java.awt.GridLayout;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JLabel;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.border.CompoundBorder;
import javax.swing.border.BevelBorder;
import javax.swing.border.TitledBorder;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import purejavacomm.PortInUseException;
import purejavacomm.UnsupportedCommOperationException;

import javax.swing.UIManager;
import java.awt.Color;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.beans.PropertyChangeEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.CardLayout;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.SwingConstants;
import javax.swing.JRadioButton;
import javax.swing.JToggleButton;
import javax.swing.JCheckBox;
import java.awt.FlowLayout;
import javax.swing.JProgressBar;
import javax.swing.border.SoftBevelBorder;
import java.awt.SystemColor;
import javax.swing.border.LineBorder;

public class EducatorMode extends JFrame {


	//Test Parameter Variables and Constants
	public static final int NUM_TEST_PARAMETERS = 13;
	public static final int NUM_ID_INFO_PARAMETERS = 3;
	public static final int CURRENT_FIRMWARE_ID = 23;
	private DataOrganizer dataOrgo;

	private JPanel contentPane;
	private JComboBox gyroSensitivityCombobox;
	private JComboBox gyroFilterCombobox;
	private JCheckBox timedTestCheckbox;
	private JProgressBar progressBar;
	private JComboBox testTypeCombobox;
	private GraphController lineGraph;
	private MediaPlayerController mediaController;
	private JButton applyConfigurationsBtn;
	private JButton nextBtnOne;
	private JButton backBtnThree;
	private JButton nextBtnThree;
	private JButton readTestBtn;
	private JButton pairNewRemoteButton;
	private JButton unpairAllRemotesButton;
	private JButton testRemotesButton;
	private JButton exitTestModeButton;
	private ButtonGroup group;
	private static SerialComm serialHandler;
	private Integer wIndex = 1; 
	private JPanel testTakingPanel;
	private JPanel stepOne;
	private JPanel stepTwo;
	private JPanel stepThree;
	private JPanel stepFour;
	private JLabel generalStatusLabelOne;
	private JLabel generalStatusLabelTwo;
	private JLabel generalStatusLabelThree;
	private JLabel generalStatusLabelFive;

	private HashMap<String, ArrayList<Integer>> testTypeHashMap = new HashMap<String, ArrayList<Integer>>();
	ArrayList<Integer> testParams = new ArrayList<Integer>();
	private String testType;
	private JPanel stepFive;
	private JPanel navPanel;
	private JButton backBtnFive;
	private JButton nextBtnFive;
	private JButton eraseBtn;
	private JButton noBtn;
	private JButton btnLaunchMotionVisualization;
	private JPanel panel_5;
	private JButton configForCalButton;
	private JPanel videoBrowsePanel;
	private JTextField videoFilePathTextField;
	private JButton btnBrowse;
	private JButton importCalDataButton;
	private JPanel calOffsetsPanel;
	private JTextField tmr0OffsetTextField;
	private JTextField delayAfterTextField;
	private JButton applyOffsetButton;
	private JLabel generalStatusLabel;
	private JButton nextBtnTwo;
	private JButton backBtnTwo;
	private JLabel lblNewLabel;
	private JLabel lblStepaTest;
	private JLabel lblStepbExit;
	private JLabel lblNewLabel_1;
	private JLabel lblStepRead;
	private JTextField firstGliderAndModuleMassTextField;
	private JLabel lblNewLabel_2;
	private JTextField secondGliderAndModuleMassTextField;
	private JLabel label;
	private JLabel lblMassOfSecond;
	private JLabel lblStepaEnter;
	private JPanel conservationOfEnergyLabPane;
	private JLabel lblNewLabel_4;
	private JLabel lblDistanceDModule;
	private JTextField distanceModuleFallsTextField;
	private JLabel lblNewLabel_5;
	private JTextField massOfTheModuleTextField;
	private JLabel lblMassOfThe;
	private JLabel lblKg;
	private JTextField momentOfInertiaDMTextField;
	private JLabel lblI_1;
	private JLabel lblMomentOfInertia;
	private JTextField textField;
	private JLabel label_1;
	private JLabel lblRadiusOfThe;
	private JPanel momentumLabPane;
	private JTextField handMassTextField;
	private JLabel lblNewLabel_3;
	private JLabel lblMassOfThe_1;
	private JTextField personMassTextField;
	private JLabel lblDistanceFromYour;
	private JTextField wingSpanTextField;
	private JLabel lblShoulderWidth;
	private JTextField shoulderWidthTextField;
	private JPanel spinnyStoolDemo;
	private JLabel lblMassOfThe_2;
	private JLabel lblNewLabel_6;
	private JLabel label_2;
	private JLabel lblM;
	private JLabel label_3;
	private JButton mediaPlayerBtn;
	private JButton graphTestBtn;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		//Set the look and feel to whatever the system default is.
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} 
		catch(Exception e) {
			System.out.println("Error Setting Look and Feel: " + e);
		}


		serialHandler = new SerialComm();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					EducatorMode frame = new EducatorMode();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}


	/**
	 * Get the desired tick threshold for the desired sample rate. This effectively sets the sample rate of the module
	 * @param accelGyroSampleRate
	 * @return
	 */
	public int getTickThreshold(int accelGyroSampleRate) {
		switch (accelGyroSampleRate) {
		case(60):		
			return 33173;
		case(120):
			return 33021;
		case (240):
			return 16343;
		case (480):
			return 8021;
		case (500):
			return 7679;
		case (960):
			return 3848;
		default:	//960-96
			return 3813;
		}
	}

	public void fillTestTypeHashMap(int timedTest) {
		ArrayList<Integer> testParams = new ArrayList<Integer>();

		//0 Num Tests (Will not be saved by firmware, always send 0), this is to maintain consistent ArrayList indexing across the program
		testParams.add(0);
		//1 Timer0 Tick Threshold
		testParams.add(getTickThreshold(960));
		//2 Delay after start (Will not be overridden in firmware unless accessed by calibration panel)
		testParams.add(0);
		//3 Battery timeout flag
		testParams.add(300);
		//4 Timed test flag
		testParams.add(timedTest);
		//5 Trigger on release flag
		testParams.add(1);
		//6 Test Length
		testParams.add(30);
		//7 Accel Gyro Sample Rate
		testParams.add(960);
		//8 Mag Sample Rate
		testParams.add(96);
		//9 Accel Sensitivity
		testParams.add(4);
		//10 Gyro Sensitivity
		testParams.add(1000);
		//11 Accel Filter
		testParams.add(92);
		//12 Gyro Filter
		testParams.add(92);

		testTypeHashMap.put("Conservation of Momentum (Elastic Collision)", testParams);

		testParams.clear();

		//0 Num Tests (Will not be saved by firmware, always send 0), this is to maintain consistent ArrayList indexing across the program
		testParams.add(0);
		//1 Timer0 Tick Threshold
		testParams.add(getTickThreshold(960));
		//2 Delay after start (Will not be overridden in firmware unless accessed by calibration panel)
		testParams.add(0);
		//3 Battery timeout flag
		testParams.add(300);
		//4 Timed test flag
		testParams.add(timedTest);
		//5 Trigger on release flag
		testParams.add(1);
		//6 Test Length
		testParams.add(30);
		//7 Accel Gyro Sample Rate
		testParams.add(960);
		//8 Mag Sample Rate
		testParams.add(96);
		//9 Accel Sensitivity
		testParams.add(4);
		//10 Gyro Sensitivity
		testParams.add(2000);
		//11 Accel Filter
		testParams.add(92);
		//12 Gyro Filter
		testParams.add(92);

		testTypeHashMap.put("Conservation of Angular Momentum", testParams);

		testParams.clear();

		//0 Num Tests (Will not be saved by firmware, always send 0), this is to maintain consistent ArrayList indexing across the program
		testParams.add(0);
		//1 Timer0 Tick Threshold
		testParams.add(getTickThreshold(960));
		//2 Delay after start (Will not be overridden in firmware unless accessed by calibration panel)
		testParams.add(0);
		//3 Battery timeout flag
		testParams.add(300);
		//4 Timed test flag
		testParams.add(timedTest);
		//5 Trigger on release flag
		testParams.add(1);
		//6 Test Length
		testParams.add(30);
		//7 Accel Gyro Sample Rate
		testParams.add(960);
		//8 Mag Sample Rate
		testParams.add(96);
		//9 Accel Sensitivity
		testParams.add(16);
		//10 Gyro Sensitivity
		testParams.add(2000);
		//11 Accel Filter
		testParams.add(92);
		//12 Gyro Filter
		testParams.add(92);

		testTypeHashMap.put("Conservation of Energy", testParams);

		testParams.clear();

		//0 Num Tests (Will not be saved by firmware, always send 0), this is to maintain consistent ArrayList indexing across the program
		testParams.add(0);
		//1 Timer0 Tick Threshold
		testParams.add(getTickThreshold(960));
		//2 Delay after start (Will not be overridden in firmware unless accessed by calibration panel)
		testParams.add(0);
		//3 Battery timeout flag
		testParams.add(300);
		//4 Timed test flag
		testParams.add(timedTest);
		//5 Trigger on release flag
		testParams.add(1);
		//6 Test Length
		testParams.add(30);
		//7 Accel Gyro Sample Rate
		testParams.add(960);
		//8 Mag Sample Rate
		testParams.add(96);
		//9 Accel Sensitivity
		testParams.add(4);
		//10 Gyro Sensitivity
		testParams.add(1000);
		//11 Accel Filter
		testParams.add(92);
		//12 Gyro Filter
		testParams.add(92);

		testTypeHashMap.put("Inclined Plane", testParams);

		testParams.clear();

		//0 Num Tests (Will not be saved by firmware, always send 0), this is to maintain consistent ArrayList indexing across the program
		testParams.add(0);
		//1 Timer0 Tick Threshold
		testParams.add(getTickThreshold(960));
		//2 Delay after start (Will not be overridden in firmware unless accessed by calibration panel)
		testParams.add(0);
		//3 Battery timeout flag
		testParams.add(300);
		//4 Timed test flag
		testParams.add(timedTest);
		//5 Trigger on release flag
		testParams.add(1);
		//6 Test Length
		testParams.add(30);
		//7 Accel Gyro Sample Rate
		testParams.add(960);
		//8 Mag Sample Rate
		testParams.add(96);
		//9 Accel Sensitivity
		testParams.add(8);
		//10 Gyro Sensitivity
		testParams.add(2000);
		//11 Accel Filter
		testParams.add(92);
		//12 Gyro Filter
		testParams.add(92);

		testTypeHashMap.put("Physical Pendulum", testParams);

		testParams.clear();

		//0 Num Tests (Will not be saved by firmware, always send 0), this is to maintain consistent ArrayList indexing across the program
		testParams.add(0);
		//1 Timer0 Tick Threshold
		testParams.add(getTickThreshold(960));
		//2 Delay after start (Will not be overridden in firmware unless accessed by calibration panel)
		testParams.add(0);
		//3 Battery timeout flag
		testParams.add(300);
		//4 Timed test flag
		testParams.add(timedTest);
		//5 Trigger on release flag
		testParams.add(1);
		//6 Test Length
		testParams.add(30);
		//7 Accel Gyro Sample Rate
		testParams.add(960);
		//8 Mag Sample Rate
		testParams.add(96);
		//9 Accel Sensitivity
		testParams.add(8);
		//10 Gyro Sensitivity
		testParams.add(1000);
		//11 Accel Filter
		testParams.add(92);
		//12 Gyro Filter
		testParams.add(92);

		testTypeHashMap.put("Spring Test - Simple Harmonics", testParams);

		testParams.clear();
	}
	
	public void initFX(DataOrganizer dataOrgo, ActionEvent e) {
		Platform.setImplicitExit(false);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if(graphTestBtn == e.getSource()) {
					lineGraph = startGraphing();
					lineGraph.setDataCollector(dataOrgo, 0); //Always use index 0 for live data, since we create a new instance of the graph.
					lineGraph.graphSettingsOnStart(dataOrgo.getSerialID());
				}
				if(mediaPlayerBtn == e.getSource()) {
					mediaController = startMediaPlayer();
					mediaController.scaleVideoAtStart();
					shareFrameGraphAndMedia(lineGraph, mediaController);
				}
			}
		});
	}
	
	public void shareFrameGraphAndMedia(GraphController graph, MediaPlayerController MPC) {
		Runnable updatePosInGraph = new Runnable() {
			public void run() {
				try {
					int currentFrame = -1;
					while(true) {
						if(MPC.hasVideoSelected()) {
							while(currentFrame != MPC.getCurrentFrame()) {
								Thread.sleep(10);
								graph.updateCirclePos(MPC.getCurrentFrame(), MPC.getFPS());
								currentFrame = MPC.getCurrentFrame();
							}
						}
						Thread.sleep(100);
					} 
				}catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		Thread updatePosInGraphThread = new Thread(updatePosInGraph);
		updatePosInGraphThread.start();
	}
	
	public MediaPlayerController startMediaPlayer() {
		Stage primaryStage = new Stage();
		Parent root = null;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("MediaPlayerStructure.fxml"));
		try {
			root = loader.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    primaryStage.setTitle("Video Player");
	    if(root!=null) primaryStage.setScene(new Scene(root, 1280, 720));
	    primaryStage.show();
	    primaryStage.setResizable(false);
	    return loader.getController();
	}
	
	

	public GraphController startGraphing() {
		Stage primaryStage = new Stage();
		Parent root = null;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("GraphStructure.fxml"));
		try {
			root = loader.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(root!=null) primaryStage.setScene(new Scene(root, 1000, 700));
		
	    primaryStage.setTitle("Graph");
		primaryStage.show();
		primaryStage.setResizable(false);
		
		return loader.getController();
	}
	
	public void importCalDataHandler() {
		Runnable getConfigsOperation = new Runnable() {
			public void run() {	
				configForCalButton.setEnabled(false);
				importCalDataButton.setEnabled(false);
				applyOffsetButton.setEnabled(false);
				try {

					BlackFrameAnalysis bfo = new BlackFrameAnalysis(videoFilePathTextField.getText());

					delayAfterTextField.setText(Integer.toString(bfo.getDelayAfterStart()));
					tmr0OffsetTextField.setText(Integer.toString(bfo.getTMR0Offset()));

					configForCalButton.setEnabled(true);
					importCalDataButton.setEnabled(true);
					applyOffsetButton.setEnabled(true);

				} catch (IOException e) {
					generalStatusLabel.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
			}
		};
		
		Thread getConfigsOperationThread = new Thread(getConfigsOperation);
		getConfigsOperationThread.start();
		
	}
	
	public void readExtraTestParamsForTemplate(){
		//params.put(testType).put(variable) = x,y,content
		class CellData{
			public int X;
			public int Y;
			public String content;
		}
		
		HashMap<String, CellData> param = new HashMap<>();
		CellData cell = new CellData();
		
		
		switch(testType) {
			case "Conservation of Momentum (Elastic Collision)":
				
				//Write param to hashmap and location of template to write in
				cell.X = 3;
				cell.Y = 3;
				cell.content = testTypeHashMap.get(testType).get(7).toString();	//Sample Rate
				param.put("SampleRate", cell);
				
				//Write param to hashmap and location of template to write in
				cell.X = 3;
				cell.Y = 4;
				cell.content = testTypeHashMap.get(testType).get(9).toString();	//Accel Sensitivity
				param.put("AccelSensitivity", cell);
				
				
				//Write param to hashmap and location of template to write in
				cell.X = 3;
				cell.Y = 5;
				cell.content = testTypeHashMap.get(testType).get(10).toString();	//Gyro Rate
				param.put("GyroSensitivity", cell);
				
				cell.X = 3;
				cell.Y = 8;
				cell.content = firstGliderAndModuleMassTextField.getText();
				param.put("firstGliderAndModuleMassTextField", cell);
				
				cell.X = 3;
				cell.Y = 9;
				cell.content = secondGliderAndModuleMassTextField.getText();
				param.put("secondGliderAndModuleMassTextField", cell);
				break;
				
				
			case "Physical Spring": 
				cell.X = 3;
				cell.Y = 3;
				cell.content = testTypeHashMap.get(testType).get(7).toString();	//Sample Rate
				param.put("sampleRate", cell);
				
				cell.X = 3;
				cell.Y = 4;
				cell.content = testTypeHashMap.get(testType).get(9).toString();	//Accel Sensitivity
				param.put("AccelSensitivity", cell);
				
				cell.X = 3;
				cell.Y = 5;
				cell.content = testTypeHashMap.get(testType).get(10).toString();//Gyro Sensitivity
				param.put("GyroSensitivity", cell);
				break;
			case "Spinny Stool":
				
		}
	}
	
	
	public void applyOffsetsHandler() {
		Runnable getConfigsOperation = new Runnable() {
			public void run() {
				configForCalButton.setEnabled(false);
				importCalDataButton.setEnabled(false);
				applyOffsetButton.setEnabled(false);

				try {
					if(!serialHandler.applyCalibrationOffsets(Integer.parseInt(tmr0OffsetTextField.getText()), Integer.parseInt(delayAfterTextField.getText()))) { //Constant 0 because we dont do Timer0 Calibration... yet
						generalStatusLabel.setText("Error Communicating With Module");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(255, 0, 0));
					}
					else {
						generalStatusLabel.setText("Offset Successfully Applied, Camera and Module are now Synced");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(51, 204, 51));
					}

					configForCalButton.setEnabled(true);
					importCalDataButton.setEnabled(true);
					applyOffsetButton.setEnabled(true);

				}
				catch (IOException e) {
					generalStatusLabel.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (PortInUseException e) {
					generalStatusLabel.setText("Serial Port Already In Use");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabel.setText("Check Dongle Compatability");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
			}
		};
		Thread applyOffsetsHandlerThread = new Thread(getConfigsOperation);
		applyOffsetsHandlerThread.start();

	}
	
	/**
	 * Handles the button press of browse button. This is an action event which must handled before the rest of the program resumes. This method allows the user to navigate
	 * the file explorer and select a save location for the incoming data.
	 */
	public void videoBrowseButtonHandler() {
		JFileChooser chooser;
		chooser = new JFileChooser(); 
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			videoFilePathTextField.setText(chooser.getSelectedFile().toString());
		}
		else {
			videoFilePathTextField.setText(null);
		}
	}
	
	public void configForCalHandler() {
		Runnable calforConfigOperation = new Runnable() {
			public void run() {
				configForCalButton.setEnabled(false);
				importCalDataButton.setEnabled(true);
				applyOffsetButton.setEnabled(false);

				try {
					if(!serialHandler.configForCalibration()) {
						generalStatusLabel.setText("Error Communicating With Module");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(255, 0, 0));
					}
					else {
						generalStatusLabel.setText("Module Configured for Calibration, Use Configuration Tab to Exit");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(51, 204, 51));
					}

					configForCalButton.setEnabled(true);
					importCalDataButton.setEnabled(true);
					applyOffsetButton.setEnabled(true);
				}
				catch (IOException e) {
					generalStatusLabel.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (PortInUseException e) {
					generalStatusLabel.setText("Serial Port Already In Use");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabel.setText("Check Dongle Compatability");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
			}
		};
		Thread calConfigThread = new Thread(calforConfigOperation);
		calConfigThread.start();
	}
	
	/**
	 * Executed when pair new remote button is pressed. Since this is an action event, it must complete before GUI changes will be visible 
	 */
	public void pairNewRemoteHandler() {
		//Specify new operation that can be run in a separate thread
		Runnable pairNewRemoteOperation = new Runnable() {
			public void run() {
				//Disable buttons that should not be used in the middle of a sequence
				pairNewRemoteButton.setEnabled(false);
				unpairAllRemotesButton.setEnabled(false);
				testRemotesButton.setEnabled(false);

				generalStatusLabelTwo.setText("Module Listening for New Remote, Hold 'A' or 'B' Button to Pair");
				progressBar.setValue(0);
				progressBar.setForeground(new Color(51, 204, 51));

				try {
					if(serialHandler.pairNewRemote()) {
						generalStatusLabelTwo.setText("New Remote Successfully Paired");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(51, 204, 51));
					}
					else {
						generalStatusLabelTwo.setText("Pair Unsuccessful, Receiver Timed Out");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(255, 0, 0));
					}


				}
				catch (IOException e) {
					generalStatusLabelTwo.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (PortInUseException e) {
					generalStatusLabelTwo.setText("Serial Port Already In Use");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabelTwo.setText("Check Dongle Compatability");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}

				//Enable buttons that can now be used since the bulk erase completed
				pairNewRemoteButton.setEnabled(true);
				unpairAllRemotesButton.setEnabled(true);
				testRemotesButton.setEnabled(true);


			}
		};

		//Define a new thread to run the operation previously defined
		Thread pairNewRemoteThread = new Thread(pairNewRemoteOperation);
		//Start the thread
		pairNewRemoteThread.start();
	}


	/**
	 * Executed when unpair all remotes button is pressed. Since this is an action event, it must complete before GUI changes will be visible 
	 */
	public void unpairAllRemotesHandler() {
		//Specify new operation that can be run in a separate thread
		Runnable unpairAllRemotesOperation = new Runnable() {
			public void run() {
				//Disable buttons that should not be used in the middle of a sequence
				pairNewRemoteButton.setEnabled(false);
				unpairAllRemotesButton.setEnabled(false);
				testRemotesButton.setEnabled(false);

				generalStatusLabelTwo.setText("Unpairing all Remotes...");
				progressBar.setValue(0);
				progressBar.setForeground(new Color(51, 204, 51));

				try {
					serialHandler.unpairAllRemotes();
				}
				catch (IOException e) {
					generalStatusLabelTwo.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (PortInUseException e) {
					generalStatusLabelTwo.setText("Serial Port Already In Use");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabelTwo.setText("Check Dongle Compatability");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}


				pairNewRemoteButton.setEnabled(true);
				unpairAllRemotesButton.setEnabled(true);
				testRemotesButton.setEnabled(true);

				generalStatusLabelTwo.setText("All Remotes Unpaired, There are 0 Remotes Paired to this Module");
				progressBar.setValue(0);
				progressBar.setForeground(new Color(51, 204, 51));
			}
		};

		//Define a new thread to run the operation previously defined
		Thread unpairAllRemotesThread = new Thread(unpairAllRemotesOperation);
		//Start the thread
		unpairAllRemotesThread.start();
	}

	/**
	 * Runs a thread that will put the module in a test remote mode that will automatically update the GUI based on which remote button is pressed 
	 * Executed when test remote button is pressed. Since this is an action event, it must complete before GUI changes will be visible 
	 */
	public void testRemotesHandler() {
		//Specify new operation that can be run in a separate thread
		Runnable testRemoteOperation = new Runnable() {
			public void run() {
				//Disable buttons that should not be used in the middle of a sequence
				pairNewRemoteButton.setEnabled(false);
				unpairAllRemotesButton.setEnabled(false);
				testRemotesButton.setEnabled(false);
				backBtnTwo.setEnabled(false);
				nextBtnTwo.setEnabled(false);
				exitTestModeButton.setEnabled(true);


				//Notify the user that the bulk erase sequence has began
				generalStatusLabelTwo.setText("Press a Button on a Remote to Test if it is Paired");
				progressBar.setValue(0);
				progressBar.setForeground(new Color(51, 204, 51));

				try {
					if(!serialHandler.testRemotes(generalStatusLabelTwo)) {
						generalStatusLabelTwo.setText("Error Communicating with Module");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(255, 0, 0));
					}
				}
				catch (IOException e) {
					generalStatusLabelTwo.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (PortInUseException e) {
					generalStatusLabelTwo.setText("Serial Port Already In Use");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabelTwo.setText("Check Dongle Compatability");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}

				//Enable button
				pairNewRemoteButton.setEnabled(true);
				unpairAllRemotesButton.setEnabled(true);
				testRemotesButton.setEnabled(true);
				exitTestModeButton.setEnabled(false);
				backBtnTwo.setEnabled(true);
				nextBtnTwo.setEnabled(true);

				//Notify the user that the sequence has completed
				generalStatusLabelTwo.setText("Test Mode Successfully Exited");
				progressBar.setValue(100);
				progressBar.setForeground(new Color(51, 204, 51));
			}
		};

		//Define a new thread to run the operation previously defined
		Thread testRemoteThread = new Thread(testRemoteOperation);
		//Start the thread
		testRemoteThread.start();
	}

	/**
	 * Sets flag that will cause the testRemoteThread to exit the test remote mode
	 * Executed when exit remote test mode button is pressed. Since this is an action event, it must complete before GUI changes will be visible 
	 */
	public void exitTestModeHandler() {
		serialHandler.exitRemoteTest();
	}

	public String getSelectedButtonText(ButtonGroup buttonGroup) {
		for (Enumeration<AbstractButton> buttons = buttonGroup.getElements(); buttons.hasMoreElements();) {
			AbstractButton button = buttons.nextElement();

			if (button.isSelected()) {
				return button.getText();
			}
		}

		return null;
	}

	public void updateBywIndex(Integer index) {
		testTakingPanel.removeAll();
		switch(index) {
		case 1:
			testTakingPanel.add(stepOne);
		case 2:
			testTakingPanel.add(stepTwo);
		case 3:
			testTakingPanel.add(stepThree);
		case 4:
			testTakingPanel.add(stepFour);
		case 5:
			testTakingPanel.add(stepFive);
		default:
			if(index>5) {
				testTakingPanel.add(stepOne);
				wIndex = 1;
				repaint();
			}
		}
	}


	/**
	 * Handles the button press of the sector erase button. This is an action event which must handled before the rest of the program resumes. To prevent the dashboard from stalling,
	 * a thread is created to run the desired operation in the background then the handler is promptly exited so the program can resume.
	 */
	public void sectorEraseHandler() {
		//Specify new operation that can be run in a separate thread
		Runnable sectorEraseOperation = new Runnable() {
			public void run() {
				//Disable buttons that should not be used in the middle of a sequence
				eraseBtn.setEnabled(false);
				//Notify the user that the bulk erase sequence has began
				generalStatusLabelFive.setText("Sector Erasing...");
				progressBar.setValue(0);
				progressBar.setForeground(new Color(51, 204, 51));

				try {
					if(serialHandler.sectorEraseModule()) {
						//Notify the user that the sequence has completed
						generalStatusLabelFive.setText("Sector Erase Complete");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(51, 204, 51));
					}
					else {

						//Notify the user that the sequence has failed
						generalStatusLabelFive.setText("Sector Erase Failed");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(255, 0, 0));
					}
					//Enable buttons that can now be used since the sector erase completed
					eraseBtn.setEnabled(true);
				}
				catch (IOException e) {
					generalStatusLabelFive.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (PortInUseException e) {
					generalStatusLabelFive.setText("Serial Port Already In Use");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabelFive.setText("Check Dongle Compatability");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
			}
		};

		//Define a new thread to run the operation previously defined
		Thread sectorEraseThread = new Thread(sectorEraseOperation);
		//Start the thread
		sectorEraseThread.start();
	}


	public boolean findModuleCommPort() {
		class threadHack{
			private boolean status = false;

			public boolean getStatus(){
				return status;
			}
			public void setStatus(boolean x) {
				status = x;
			}
		}
		final threadHack th = new threadHack();

		Runnable findModuleOperation = new Runnable() {
			public void run() {
				try {
					ArrayList<String> commPortIDList = serialHandler.findPorts();
					boolean moduleFound = false;
					int commPortIndex = 0;
					while (!moduleFound && commPortIndex < commPortIDList.size()) {

						//Get the string identifier (name) of the current port
						String selectedCommID = commPortIDList.toArray()[commPortIndex].toString();      

						//Open the serial port with the selected name, initialize input and output streams, set necessary flags so the whole program know that everything is initialized
						if(serialHandler.openSerialPort(selectedCommID)){

							int attemptCounter = 0;
							while (attemptCounter < 3 && !moduleFound) {
								try {
									ArrayList<Integer> moduleIDInfo = serialHandler.getModuleInfo(NUM_ID_INFO_PARAMETERS);

									if (moduleIDInfo != null) {
										moduleFound = true;

										if (moduleIDInfo.get(2) != CURRENT_FIRMWARE_ID) {
											generalStatusLabelOne.setText("Incompatable Firmware Version: " + moduleIDInfo.get(2) + ", Program Module with Version " + CURRENT_FIRMWARE_ID);
										}
										else {
											generalStatusLabelOne.setText("Successfully Connected to Module");
										}
									}
									else {
										attemptCounter++;
									}
								}
								catch (IOException e) {
									attemptCounter++;
								}
								catch (PortInUseException e) {
									attemptCounter++;
								}
								catch (UnsupportedCommOperationException e) {
									attemptCounter++;
								}
							}

						}
						commPortIndex++;
					}
					if (!moduleFound) {
						generalStatusLabelOne.setText("Could Not Locate a Module, Check Connections and Try Manually Connecting");
						th.setStatus(false);
					}

				}
				catch (IOException e) {
					generalStatusLabelOne.setText("Could Not Locate a Module, Check Connections and Try Manually Connecting");
					th.setStatus(false);
				}
				catch (PortInUseException e) {
					generalStatusLabelOne.setText("Could Not Locate a Module, Check Connections and Try Manually Connecting");
					th.setStatus(false);
				}
			}
		};
		Thread findModuleThread = new Thread(findModuleOperation);
		findModuleThread.run();
		return th.getStatus();
	}

	/**
	 * Gets a 3 letter abbreviation for the passed in month for the automatic test title generation
	 * @param month an integer 0-11 that corresponds to the month with 0 = January and 11 = December
	 * @return The 3 letter abbreviation for the month
	 */
	public String getMonth(int month) { 
		switch (month) {
		case (0):
			return "JAN";
		case (1):
			return "FEB";
		case (2):
			return "MAR";
		case (3):
			return "APR";
		case (4):
			return "MAY";
		case (5):
			return "JUN";
		case (6):
			return "JUL";
		case (7):
			return "AUG";
		case (8):
			return "SEP";
		case (9):
			return "OCT";
		case (10):
			return "NOV";
		case (11):
			return "DEC";
		}
		return "NOP";
	}

	/**
	 * Handles the button press of read data from module button. This includes reading the test parameters, updating the read gui, and reading/converting the data to .csv. This is an action event which must handled before the rest of the program resumes. To prevent the dashboard from stalling,
	 * a thread is created to run the desired operation in the background then the handler is promptly exited so the program can resume. See the method calls within the runnable for more info
	 * on what this handler actually does.
	 */
	public void readButtonHandler() {
		//Define operation that can be run in separate thread
		Runnable readOperation = new Runnable() {
			public void run() {
				//Disable read button while read is in progress
				backBtnThree.setEnabled(false);
				nextBtnThree.setEnabled(false);
				readTestBtn.setEnabled(false);

				try {
					generalStatusLabelThree.setText("Reading Data from Module...");

					//Read test parameters from module and store it in testParameters
					ArrayList<Integer> testParameters = serialHandler.readTestParams(NUM_TEST_PARAMETERS);

					//Executes if the reading of the test parameters was successful
					if (testParameters != null) {

						int expectedTestNum = testParameters.get(0);

						//Assign local variables to their newly received values from the module
						int timedTestFlag = testParameters.get(4);
						//Trigger on release is 8
						int testLength = testParameters.get(6);
						int accelGyroSampleRate = testParameters.get(7);
						int magSampleRate = testParameters.get(8);
						int accelSensitivity = testParameters.get(9);
						int gyroSensitivity = testParameters.get(10);
						int accelFilter = testParameters.get(11);
						int gyroFilter = testParameters.get(12);


						double bytesPerSample = 18;
						if (accelGyroSampleRate / magSampleRate == 10) {
							bytesPerSample = 12.6;
						}



						String nameOfFile = "";

						//Executes if there are tests on the module
						if(expectedTestNum > 0) { 

							//Get date for file name
							Date date = new Date();

							//Assign file name
							nameOfFile += (" " + accelGyroSampleRate + "-" + magSampleRate + " " + accelSensitivity + "G-" + accelFilter + " " + gyroSensitivity + "dps-" + gyroFilter + " MAG-N " + date.getDate() + getMonth(date.getMonth()) + (date.getYear() - 100) + ".csv");

							HashMap<Integer, ArrayList<Integer>> testData;

							//Store the test data from the dashboard passing in enough info that the progress bar will be accurately updated
							testData = serialHandler.readTestData(expectedTestNum, progressBar, generalStatusLabelThree);

							generalStatusLabelThree.setText("All Data Received from Module");

							//Executes if the data was received properly (null = fail)
							if(testData != null) {
								for (int testIndex = 0; testIndex < testData.size(); testIndex++) {

									int [] finalData = new int[testData.get(testIndex).size()];

									for(int byteIndex = 0; byteIndex < testData.get(testIndex).size(); byteIndex++) {
										if (testData.get(testIndex).get(byteIndex) != -1){
											finalData[byteIndex] = testData.get(testIndex).get(byteIndex);
										}
										else {
											finalData[byteIndex] = -1;
											break;
										}
									}
									String tempName = "(#" + (testIndex+1) + ") " + nameOfFile; 
									dataOrgo = new DataOrganizer();
									//Define operation that can be run in separate thread
									Runnable organizerOperation = new Runnable() {
										public void run() {
											//Organize data into .CSV
											dataOrgo.createDataSmpsRawData(finalData);
											dataOrgo.getSignedData();
											dataOrgo.createCSVP();
											dataOrgo.createCSV(true, true); //Create CSV file, do label (column labels) the data (includes time axis), and sign the data
											//CSVBuilder.sortData(finalData, tempName, (accelGyroSampleRate / magSampleRate), settings.getKeyVal("CSVSaveLocation"), (getSelectedButtonText(group) == "Data (Excel)"), (timedTestFlag==1), testParameters); 
										}
									};

									//Set thread to execute previously defined operation
									Thread organizerThread = new Thread(organizerOperation);
									//Start thread
									organizerThread.start();
								}
							}
							else {
								generalStatusLabelThree.setText("Error Reading From Module, Try Again");
								progressBar.setValue(100);
								progressBar.setForeground(new Color(255, 0, 0));
							}
						}
						else {
							generalStatusLabelThree.setText("No Tests Found on Module");
							progressBar.setValue(100);
							progressBar.setForeground(new Color(255, 0, 0));
						}
					}
					else {
						generalStatusLabelThree.setText("Error Reading From Module, Try Again");
						progressBar.setValue(100);
						progressBar.setForeground(new Color(255, 0, 0));
					}

				}

				catch (IOException e) {
					generalStatusLabelThree.setText("Error Communicating With Serial Dongle");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (PortInUseException e) {
					generalStatusLabelThree.setText("Serial Port Already In Use");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabelThree.setText("Check Dongle Compatability");
					progressBar.setValue(100);
					progressBar.setForeground(new Color(255, 0, 0));
				}

				//Re-enable read button upon read completion
				backBtnThree.setEnabled(true);
				nextBtnThree.setEnabled(true);
				readTestBtn.setEnabled(true);
			}
		};

		//Set thread to execute previously defined operation
		Thread readThread = new Thread(readOperation);
		//Start thread
		readThread.start();

	}

	/**
	 * Handles the button press of the write configuration button. This is an action event which must handled before the rest of the program resumes. To prevent the dashboard from stalling,
	 * a thread is created to run the desired operation in the background then the handler is promptly exited so the program can resume. See the method calls within the runnable for more info
	 * on what this handler actually does.
	 */
	private void writeButtonHandler() {
		//Define no operation that can be run in a thread
		Runnable sendParamOperation = new Runnable() {
			public void run() {
				//Disable write config button while the sendParameters() method is running
				applyConfigurationsBtn.setEnabled(false);
				nextBtnOne.setEnabled(false);
				if(findModuleCommPort()) {
					generalStatusLabelOne.setText("Initial connection to module successful");
				}
				try {
					if(!serialHandler.sendTestParams(testTypeHashMap.get(testTypeCombobox.getSelectedItem().toString()))) {
						generalStatusLabelOne.setText("Module Not Responding, parameter write failed.");
					}
					else {
						generalStatusLabelOne.setText("Module Configuration Successful, Parameters Have Been Updated");
					}
				}
				catch (NumberFormatException e) {
					generalStatusLabelOne.setText("Please Fill out Every Field");
				}
				catch (IOException e) {
					generalStatusLabelOne.setText("Error Communicating With Serial Dongle");
				}
				catch (PortInUseException e) {
					generalStatusLabelOne.setText("Serial Port Already In Use");
				}
				catch (UnsupportedCommOperationException e) {
					generalStatusLabelOne.setText("Check Dongle Compatability");
				}

				//Re-enable the write config button when the routine has completed
				applyConfigurationsBtn.setEnabled(true);
				nextBtnOne.setEnabled(true);
			}
		};


		//Assign new operation to a thread so that it can be run in the background
		Thread paramThread = new Thread(sendParamOperation);
		//Start the new thread
		paramThread.start();
	}


	/**
	 * Create the frame.
	 */
	public EducatorMode() {
		setTitle("Adventure Modules - Educator Mode");
		initComponents();
		findModuleCommPort();
	}

	public void initComponents() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 704, 560);
		contentPane = new JPanel();
		contentPane.setBackground(SystemColor.control);
		contentPane.setBorder(new CompoundBorder());
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel mainPanel = new JPanel();
		mainPanel.setBackground(SystemColor.control);
		contentPane.add(mainPanel, BorderLayout.CENTER);
		mainPanel.setLayout(new GridLayout(0, 1, 0, 0));

		JTabbedPane mainTabbedPane = new JTabbedPane(JTabbedPane.LEFT);
		mainTabbedPane.setForeground(SystemColor.windowText);
		mainTabbedPane.setFont(new Font("Tahoma", Font.PLAIN, 16));
		mainPanel.add(mainTabbedPane);

		testTakingPanel = new JPanel();
		testTakingPanel.setForeground(Color.DARK_GRAY);
		testTakingPanel.setBackground(Color.DARK_GRAY);
		mainTabbedPane.addTab("Run Experiment", null, testTakingPanel, null);
		mainTabbedPane.setEnabledAt(0, true);
		testTakingPanel.setLayout(new CardLayout(0, 0));

		stepOne = new JPanel();
		stepOne.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true));
		testTakingPanel.add(stepOne, "stepOne");
		stepOne.setLayout(null);

		testTypeCombobox = new JComboBox();
		testTypeCombobox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				stepOne.remove(momentumLabPane);
				stepOne.remove(conservationOfEnergyLabPane);
				stepOne.add(spinnyStoolDemo);
				
				fillTestTypeHashMap(timedTestCheckbox.isSelected()?1:0);
				testType = testTypeCombobox.getSelectedItem().toString();
				testTypeHashMap.get(testTypeCombobox.getSelectedItem());
				
				switch(testType) {
					case "Conservation of Momentum (Elastic Collision)":
						stepOne.add(momentumLabPane);
						break;
					case "Conservation of Energy":
						stepOne.add(conservationOfEnergyLabPane);
						break;
					case "Spinny Stool":
						stepOne.add(spinnyStoolDemo);
						break;
				}
				repaint();
				
			}
		});

		testTypeCombobox.setBounds(10, 61, 506, 26);
		stepOne.add(testTypeCombobox);
		testTypeCombobox.setModel(new DefaultComboBoxModel(new String[] {"Conservation of Momentum (Elastic Collision)", "Conservation of Angular Momentum", "Conservation of Energy", "Inclined Plane", "Physical Pendulum", "Spinny Stool", "Spring Test - Simple Harmonics"}));

		applyConfigurationsBtn = new JButton("Apply Configurations");
		applyConfigurationsBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				writeButtonHandler();
				readExtraTestParamsForTemplate();
			}
		});
		applyConfigurationsBtn.setBounds(10, 348, 506, 39);
		stepOne.add(applyConfigurationsBtn);

		JPanel navPanelOne = new JPanel();
		navPanelOne.setBounds(10, 398, 506, 108);
		stepOne.add(navPanelOne);
		navPanelOne.setLayout(null);

		generalStatusLabelOne = new JLabel("");
		generalStatusLabelOne.setHorizontalAlignment(SwingConstants.CENTER);
		generalStatusLabelOne.setBounds(22, 37, 365, 26);
		navPanelOne.add(generalStatusLabelOne);

		nextBtnOne = new JButton("Next");
		nextBtnOne.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateBywIndex((wIndex += 1));
			}
		});
		nextBtnOne.setBounds(413, 20, 93, 88);
		navPanelOne.add(nextBtnOne);

		timedTestCheckbox = new JCheckBox("Timed Test");
		timedTestCheckbox.setBounds(0, 0, 97, 23);
		navPanelOne.add(timedTestCheckbox);

		noBtn = new JButton("New button");
		noBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new SettingsWindow().setVisible(true);
			}
		});
		noBtn.setBounds(576, 11, 32, 16);
		stepOne.add(noBtn);
		
		JLabel lblStepSelect = new JLabel("Step 1: Select the test you would like to perform.");
		lblStepSelect.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblStepSelect.setBounds(10, 24, 292, 25);
		stepOne.add(lblStepSelect);
		
		JLabel lblStepaApply = new JLabel("Step 1B: Apply your configurations");
		lblStepaApply.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblStepaApply.setBounds(10, 306, 229, 31);
		stepOne.add(lblStepaApply);
		
		spinnyStoolDemo = new JPanel();
		spinnyStoolDemo.setBounds(10, 111, 506, 184);
		spinnyStoolDemo.setLayout(null);
		
		handMassTextField = new JTextField();
		handMassTextField.setBounds(10, 75, 86, 20);
		spinnyStoolDemo.add(handMassTextField);
		handMassTextField.setColumns(10);
		
		JLabel lblStepaEnter_1 = new JLabel("Step 1A: Enter the masses and distances");
		lblStepaEnter_1.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblStepaEnter_1.setBounds(10, 11, 231, 20);
		spinnyStoolDemo.add(lblStepaEnter_1);
		
		lblNewLabel_3 = new JLabel("Mass of the hand weights");
		lblNewLabel_3.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel_3.setBounds(10, 52, 175, 20);
		spinnyStoolDemo.add(lblNewLabel_3);
		
		lblMassOfThe_1 = new JLabel("Mass of the person");
		lblMassOfThe_1.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblMassOfThe_1.setBounds(10, 106, 175, 20);
		
		personMassTextField = new JTextField();
		personMassTextField.setColumns(10);
		personMassTextField.setBounds(10, 129, 86, 20);
		spinnyStoolDemo.add(personMassTextField);
		
		lblDistanceFromYour = new JLabel("Wing span");
		lblDistanceFromYour.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblDistanceFromYour.setBounds(275, 52, 267, 20);
		spinnyStoolDemo.add(lblDistanceFromYour);
		
		wingSpanTextField = new JTextField();
		wingSpanTextField.setColumns(10);
		wingSpanTextField.setBounds(275, 75, 86, 20);
		spinnyStoolDemo.add(wingSpanTextField);
		
		lblShoulderWidth = new JLabel("Shoulder width");
		lblShoulderWidth.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblShoulderWidth.setBounds(275, 106, 104, 20);
		spinnyStoolDemo.add(lblShoulderWidth);
		
		shoulderWidthTextField = new JTextField();
		shoulderWidthTextField.setColumns(10);
		shoulderWidthTextField.setBounds(275, 129, 86, 20);
		spinnyStoolDemo.add(shoulderWidthTextField);
		
		lblMassOfThe_2 = new JLabel("Mass of the person");
		lblMassOfThe_2.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblMassOfThe_2.setBounds(10, 106, 146, 20);
		spinnyStoolDemo.add(lblMassOfThe_2);
		
		lblNewLabel_6 = new JLabel("kg");
		lblNewLabel_6.setBounds(106, 78, 46, 14);
		spinnyStoolDemo.add(lblNewLabel_6);
		
		label_2 = new JLabel("kg");
		label_2.setBounds(106, 132, 46, 14);
		spinnyStoolDemo.add(label_2);
		
		lblM = new JLabel("m");
		lblM.setBounds(371, 78, 46, 14);
		spinnyStoolDemo.add(lblM);
		
		label_3 = new JLabel("m");
		label_3.setBounds(371, 132, 46, 14);
		spinnyStoolDemo.add(label_3);
		
		conservationOfEnergyLabPane = new JPanel();
		conservationOfEnergyLabPane.setBounds(10, 109, 506, 196);
		conservationOfEnergyLabPane.setLayout(null);
		
		lblNewLabel_4 = new JLabel("Step 1A: Enter the following parameters");
		lblNewLabel_4.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel_4.setBounds(10, 11, 236, 14);
		conservationOfEnergyLabPane.add(lblNewLabel_4);
		
		lblDistanceDModule = new JLabel("Distance module falls from rest");
		lblDistanceDModule.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblDistanceDModule.setBounds(10, 49, 167, 14);
		conservationOfEnergyLabPane.add(lblDistanceDModule);
		
		distanceModuleFallsTextField = new JTextField();
		distanceModuleFallsTextField.setBounds(10, 74, 86, 20);
		conservationOfEnergyLabPane.add(distanceModuleFallsTextField);
		distanceModuleFallsTextField.setColumns(10);
		
		lblNewLabel_5 = new JLabel("m");
		lblNewLabel_5.setBounds(106, 77, 46, 14);
		conservationOfEnergyLabPane.add(lblNewLabel_5);
		
		massOfTheModuleTextField = new JTextField();
		massOfTheModuleTextField.setColumns(10);
		massOfTheModuleTextField.setBounds(10, 145, 86, 20);
		conservationOfEnergyLabPane.add(massOfTheModuleTextField);
		
		lblMassOfThe = new JLabel("Mass of the module and holder");
		lblMassOfThe.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblMassOfThe.setBounds(10, 120, 183, 14);
		conservationOfEnergyLabPane.add(lblMassOfThe);
		
		lblKg = new JLabel("kg");
		lblKg.setBounds(106, 148, 46, 14);
		conservationOfEnergyLabPane.add(lblKg);
		
		momentOfInertiaDMTextField = new JTextField();
		momentOfInertiaDMTextField.setColumns(10);
		momentOfInertiaDMTextField.setBounds(238, 74, 86, 20);
		conservationOfEnergyLabPane.add(momentOfInertiaDMTextField);
		
		lblI_1 = new JLabel("I");
		lblI_1.setBounds(334, 77, 46, 14);
		conservationOfEnergyLabPane.add(lblI_1);
		
		lblMomentOfInertia = new JLabel("Moment of Inertia of disc and module");
		lblMomentOfInertia.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblMomentOfInertia.setBounds(238, 49, 214, 14);
		conservationOfEnergyLabPane.add(lblMomentOfInertia);
		
		textField = new JTextField();
		textField.setColumns(10);
		textField.setBounds(238, 145, 86, 20);
		conservationOfEnergyLabPane.add(textField);
		
		label_1 = new JLabel("I");
		label_1.setBounds(334, 148, 46, 14);
		conservationOfEnergyLabPane.add(label_1);
		
		lblRadiusOfThe = new JLabel("Radius of the disc where string is attached");
		lblRadiusOfThe.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblRadiusOfThe.setBounds(238, 120, 243, 14);
		conservationOfEnergyLabPane.add(lblRadiusOfThe);
		
		momentumLabPane = new JPanel();
		momentumLabPane.setBounds(10, 109, 506, 196);
		momentumLabPane.setLayout(null);
		
		firstGliderAndModuleMassTextField = new JTextField();
		firstGliderAndModuleMassTextField.setBounds(10, 125, 86, 20);
		momentumLabPane.add(firstGliderAndModuleMassTextField);
		firstGliderAndModuleMassTextField.setColumns(10);
		
		JLabel lblMassOfGlider = new JLabel("Mass of first glider and module");
		lblMassOfGlider.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblMassOfGlider.setBounds(10, 100, 174, 14);
		momentumLabPane.add(lblMassOfGlider);
		
		lblNewLabel_2 = new JLabel("kg");
		lblNewLabel_2.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel_2.setBounds(106, 128, 46, 14);
		momentumLabPane.add(lblNewLabel_2);
		
		secondGliderAndModuleMassTextField = new JTextField();
		secondGliderAndModuleMassTextField.setColumns(10);
		secondGliderAndModuleMassTextField.setBounds(262, 125, 86, 20);
		momentumLabPane.add(secondGliderAndModuleMassTextField);
		
		label = new JLabel("kg");
		label.setFont(new Font("Tahoma", Font.PLAIN, 12));
		label.setBounds(358, 128, 46, 14);
		momentumLabPane.add(label);
		
		lblMassOfSecond = new JLabel("Mass of second glider and module");
		lblMassOfSecond.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblMassOfSecond.setBounds(262, 100, 191, 14);
		momentumLabPane.add(lblMassOfSecond);
		
		lblStepaEnter = new JLabel("Step 1A: Enter the masses of the gliders and modules");
		lblStepaEnter.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblStepaEnter.setBounds(10, 23, 307, 14);
		momentumLabPane.add(lblStepaEnter);

		stepTwo = new JPanel();
		stepTwo.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true));
		testTakingPanel.add(stepTwo, "name_92124154026185");

		pairNewRemoteButton = new JButton("Pair New Remote");
		pairNewRemoteButton.setBounds(2, 31, 522, 74);
		pairNewRemoteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				pairNewRemoteHandler();
			}
		});
		stepTwo.setLayout(null);
		
		lblNewLabel = new JLabel("Step 2: Pair a remote");
		lblNewLabel.setBounds(204, 11, 118, 15);
		stepTwo.add(lblNewLabel);
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));
		pairNewRemoteButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		stepTwo.add(pairNewRemoteButton);

		unpairAllRemotesButton = new JButton("Unpair All Remotes");
		unpairAllRemotesButton.setBounds(2, 102, 522, 74);
		unpairAllRemotesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				unpairAllRemotesHandler();
			}
		});
		unpairAllRemotesButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		stepTwo.add(unpairAllRemotesButton);

		testRemotesButton = new JButton("Test Paired Remotes");
		testRemotesButton.setBounds(2, 213, 522, 74);
		testRemotesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				testRemotesHandler();
			}
		});
		testRemotesButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		stepTwo.add(testRemotesButton);

		exitTestModeButton = new JButton("Exit Test Mode");
		exitTestModeButton.setBounds(2, 332, 522, 74);
		exitTestModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exitTestModeHandler();
			}
		});
		exitTestModeButton.setEnabled(false);
		exitTestModeButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		stepTwo.add(exitTestModeButton);

		JPanel navPanelTwo = new JPanel();
		navPanelTwo.setBounds(12, 407, 504, 98);
		stepTwo.add(navPanelTwo);
		navPanelTwo.setLayout(null);

		generalStatusLabelTwo = new JLabel("");
		generalStatusLabelTwo.setHorizontalAlignment(SwingConstants.CENTER);
		generalStatusLabelTwo.setBounds(113, 33, 284, 24);
		navPanelTwo.add(generalStatusLabelTwo);

		nextBtnTwo = new JButton("Next");
		nextBtnTwo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateBywIndex((wIndex += 1));
			}
		});
		nextBtnTwo.setBounds(411, 11, 93, 88);
		navPanelTwo.add(nextBtnTwo);

		backBtnTwo = new JButton("Back");
		backBtnTwo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateBywIndex((wIndex -= 1));
			}
		});
		backBtnTwo.setBounds(0, 11, 93, 88);
		navPanelTwo.add(backBtnTwo);
		
		lblStepaTest = new JLabel("Step 2A: Test the paired remote");
		lblStepaTest.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblStepaTest.setBounds(170, 187, 189, 15);
		stepTwo.add(lblStepaTest);
		
		lblStepbExit = new JLabel("Step 2B: Exit remote testing");
		lblStepbExit.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblStepbExit.setBounds(182, 298, 164, 23);
		stepTwo.add(lblStepbExit);

		stepThree = new JPanel();
		stepThree.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true));
		testTakingPanel.add(stepThree, "name_92908667926372");
		stepThree.setLayout(null);

		JPanel outputPanel = new JPanel();
		outputPanel.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true));
		outputPanel.setBounds(10, 11, 506, 234);
		stepThree.add(outputPanel);
		outputPanel.setLayout(null);

		group = new ButtonGroup();

		JRadioButton dataExcelRadioBtn = new JRadioButton("Data (Spreadsheet)");
		dataExcelRadioBtn.setHorizontalAlignment(SwingConstants.CENTER);
		dataExcelRadioBtn.setFont(new Font("Tahoma", Font.PLAIN, 16));
		dataExcelRadioBtn.setBounds(112, 48, 317, 50);
		outputPanel.add(dataExcelRadioBtn);

		group.add(dataExcelRadioBtn);

		JRadioButton motionVisualizationRadioBtn = new JRadioButton("Motion Visualization");
		motionVisualizationRadioBtn.setBounds(112, 188, 317, 36);
		outputPanel.add(motionVisualizationRadioBtn);
		motionVisualizationRadioBtn.setHorizontalAlignment(SwingConstants.CENTER);
		motionVisualizationRadioBtn.setFont(new Font("Tahoma", Font.PLAIN, 16));
		group.add(motionVisualizationRadioBtn);

		JRadioButton graphRadioBtn = new JRadioButton("Graph (Using template)");
		graphRadioBtn.setBounds(112, 101, 317, 48);
		outputPanel.add(graphRadioBtn);
		graphRadioBtn.setFont(new Font("Tahoma", Font.PLAIN, 16));
		graphRadioBtn.setHorizontalAlignment(SwingConstants.CENTER);
		graphRadioBtn.setEnabled(false);
		group.add(graphRadioBtn);

		JRadioButton rdbtnBothgraphingAnd = new JRadioButton("Both (Graphing and Spreadsheet output)");
		rdbtnBothgraphingAnd.setEnabled(false);
		rdbtnBothgraphingAnd.setFont(new Font("Tahoma", Font.PLAIN, 16));
		rdbtnBothgraphingAnd.setBounds(112, 157, 317, 23);
		outputPanel.add(rdbtnBothgraphingAnd);
		
		lblNewLabel_1 = new JLabel("Step 3: Select your output type");
		lblNewLabel_1.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblNewLabel_1.setBounds(173, 11, 184, 14);
		outputPanel.add(lblNewLabel_1);

		readTestBtn = new JButton("Read Test");
		readTestBtn.setBounds(10, 311, 506, 77);
		readTestBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				readButtonHandler();
			}
		});
		readTestBtn.setFont(new Font("Tahoma", Font.PLAIN, 16));
		stepThree.add(readTestBtn);

		JPanel navPanelThree = new JPanel();
		navPanelThree.setBounds(10, 404, 506, 101);
		stepThree.add(navPanelThree);
		navPanelThree.setLayout(null);

		generalStatusLabelThree = new JLabel("");
		generalStatusLabelThree.setHorizontalAlignment(SwingConstants.CENTER);
		generalStatusLabelThree.setBounds(103, 63, 300, 25);
		navPanelThree.add(generalStatusLabelThree);

		backBtnThree = new JButton("Back");
		backBtnThree.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateBywIndex((wIndex -= 1));
			}
		});
		backBtnThree.setBounds(0, 11, 93, 88);
		navPanelThree.add(backBtnThree);

		nextBtnThree = new JButton("Next");
		nextBtnThree.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateBywIndex((wIndex += 1));
			}
		});
		nextBtnThree.setBounds(413, 11, 93, 88);
		navPanelThree.add(nextBtnThree);

		progressBar = new JProgressBar();
		progressBar.setBounds(103, 39, 300, 14);
		navPanelThree.add(progressBar);
		
		lblStepRead = new JLabel("Step 3A: Read all tests from your Adventure Module.");
		lblStepRead.setBounds(151, 273, 301, 14);
		stepThree.add(lblStepRead);
		lblStepRead.setFont(new Font("Tahoma", Font.PLAIN, 12));

		stepFour = new JPanel();
		stepFour.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true));
		testTakingPanel.add(stepFour, "name_96253525137854");
		stepFour.setLayout(new GridLayout(5, 1, 0, 0));

		panel_5 = new JPanel();
		stepFour.add(panel_5);

		JPanel panel_4 = new JPanel();
		stepFour.add(panel_4);
		panel_4.setLayout(new BorderLayout(0, 0));

		btnLaunchMotionVisualization = new JButton("Launch Motion Visualization");
		btnLaunchMotionVisualization.setFont(new Font("Tahoma", Font.PLAIN, 40));
		panel_4.add(btnLaunchMotionVisualization);

		JPanel panel_3 = new JPanel();
		stepFour.add(panel_3);

		JPanel panel_2 = new JPanel();
		stepFour.add(panel_2);

		JPanel navPanelFour = new JPanel();
		stepFour.add(navPanelFour);
		navPanelFour.setLayout(null);

		JButton backBtnFour = new JButton("Back");
		backBtnFour.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateBywIndex((wIndex -= 1));
			}
		});
		backBtnFour.setBounds(10, 0, 93, 88);
		navPanelFour.add(backBtnFour);

		JButton nextBtnFour = new JButton("Next");
		nextBtnFour.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateBywIndex((wIndex += 1));
			}
		});
		nextBtnFour.setBounds(419, 0, 93, 88);
		navPanelFour.add(nextBtnFour);

		stepFive = new JPanel();
		stepFive.setBorder(new LineBorder(Color.LIGHT_GRAY, 2, true));
		testTakingPanel.add(stepFive, "name_116075093988858");
		stepFive.setLayout(null);

		eraseBtn = new JButton("Erase");
		eraseBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				sectorEraseHandler();
			}
		});

		navPanel = new JPanel();
		navPanel.setBounds(10, 406, 506, 101);
		stepFive.add(navPanel);
		navPanel.setLayout(null);

		backBtnFive = new JButton("Back");
		backBtnFive.setBounds(0, 11, 93, 88);
		backBtnFive.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateBywIndex((wIndex -= 1));
			}
		});
		navPanel.add(backBtnFive);

		nextBtnFive = new JButton("Next");
		nextBtnFive.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateBywIndex((wIndex += 1));
			}
		});
		nextBtnFive.setBounds(413, 11, 93, 88);
		navPanel.add(nextBtnFive);

		generalStatusLabelFive = new JLabel("");
		generalStatusLabelFive.setHorizontalAlignment(SwingConstants.CENTER);
		generalStatusLabelFive.setBounds(113, 30, 293, 28);
		navPanel.add(generalStatusLabelFive);
		eraseBtn.setFont(new Font("Tahoma", Font.PLAIN, 24));
		eraseBtn.setBounds(10, 60, 506, 345);
		stepFive.add(eraseBtn);
		
		JLabel lblStepErase = new JLabel("Step 5: Erase the data from the module. ");
		lblStepErase.setBounds(242, 23, 46, 14);
		stepFive.add(lblStepErase);

		JPanel calibrationPanel = new JPanel();
		mainTabbedPane.addTab("Calibration Panel", null, calibrationPanel, null);
		calibrationPanel.setLayout(new GridLayout(6, 1, 0, 0));
		
		configForCalButton = new JButton("Configure Module for Calibration");
		configForCalButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				configForCalHandler();
			}
		});
		configForCalButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		calibrationPanel.add(configForCalButton);
		
		videoBrowsePanel = new JPanel();
		calibrationPanel.add(videoBrowsePanel);
		videoBrowsePanel.setLayout(null);
		
		videoFilePathTextField = new JTextField();
		videoFilePathTextField.setBounds(0, 0, 444, 84);
		videoFilePathTextField.setBorder(new TitledBorder(null, "File Name", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		videoBrowsePanel.add(videoFilePathTextField);
		videoFilePathTextField.setColumns(10);
		
		btnBrowse = new JButton("Browse");
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				videoBrowseButtonHandler();
			}
		});
		btnBrowse.setBounds(443, 0, 105, 84);
		videoBrowsePanel.add(btnBrowse);
		
		importCalDataButton = new JButton("Import calibration data and calculate offset");
		importCalDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				importCalDataHandler();
			}
		});
		importCalDataButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		calibrationPanel.add(importCalDataButton);
		
		calOffsetsPanel = new JPanel();
		calibrationPanel.add(calOffsetsPanel);
		calOffsetsPanel.setLayout(new GridLayout(0, 2, 0, 0));
		
		tmr0OffsetTextField = new JTextField();
		tmr0OffsetTextField.setBorder(new TitledBorder(null, "Timer0 Calibration Offset (Ticks)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		calOffsetsPanel.add(tmr0OffsetTextField);
		tmr0OffsetTextField.setColumns(10);
		
		delayAfterTextField = new JTextField();
		delayAfterTextField.setBorder(new TitledBorder(null, "Delay After Start (milliseconds)", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		calOffsetsPanel.add(delayAfterTextField);
		delayAfterTextField.setColumns(10);
		
		applyOffsetButton = new JButton("Apply Offset");
		applyOffsetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applyOffsetsHandler();
			}
		});
		applyOffsetButton.setFont(new Font("Tahoma", Font.PLAIN, 16));
		calibrationPanel.add(applyOffsetButton);
		
		generalStatusLabel = new JLabel("");
		calibrationPanel.add(generalStatusLabel);

		JPanel motionVisualizationPanel = new JPanel();
		mainTabbedPane.addTab("Motion Visualization", null, motionVisualizationPanel, null);
		motionVisualizationPanel.setLayout(null);
		
		graphTestBtn = new JButton("Graph");
		graphTestBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				initFX(dataOrgo, arg0);
			}
		});
		graphTestBtn.setBounds(10, 11, 506, 232);
		motionVisualizationPanel.add(graphTestBtn);
		
		mediaPlayerBtn = new JButton("Media Player");
		mediaPlayerBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				initFX(dataOrgo, arg0); 
			}
		});
		mediaPlayerBtn.setBounds(10, 242, 506, 263);
		motionVisualizationPanel.add(mediaPlayerBtn);
		
		for (int i = 0; i < mainTabbedPane.getTabCount(); i++) {
			mainTabbedPane.setBackgroundAt(i, Color.LIGHT_GRAY);
			mainTabbedPane.getComponentAt(i).setBackground(Color.LIGHT_GRAY);
		}
	}
}
