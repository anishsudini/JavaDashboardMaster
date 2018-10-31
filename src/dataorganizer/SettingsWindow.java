package dataorganizer;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JSplitPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.Color;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.JTabbedPane;

public class SettingsWindow extends JFrame {

	private JPanel contentPane;
	private JTextField saveDirectoryTextField;
	private JCheckBox saveOnReadCheckBox;
	private JComboBox profileComboBox;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SettingsWindow frame = new SettingsWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public void updateUI() {
		Settings settings = getSettingsInstance();
		settings.loadConfigFile();
		saveDirectoryTextField.setText(settings.getKeyVal("CSVSaveLocation"));
		saveOnReadCheckBox.setSelected(Boolean.parseBoolean(settings.getKeyVal("AutoSave")));
		profileComboBox.setSelectedItem(settings.getKeyVal("DefaultProfile"));
	}
	
	public Settings getSettingsInstance() {
		Settings settings = new Settings();
		String saveDirectoryString = null;
		try {
			settings.loadConfigFile();
			saveDirectoryString = settings.getKeyVal("CSVSaveLocation");
		}catch(Exception e) {
			e.printStackTrace();
			settings.restoreDefaultConfig();
			settings.saveConfig();
		}
		return settings;
	}
	
	public void restoreDefaultsBtnHandler() {
		Settings settings = getSettingsInstance();
		settings.restoreDefaultConfig();
		settings.loadConfigFile();
	}
	
	public void saveBtnHandler() {
		Settings settings = getSettingsInstance();
		settings.loadConfigFile();
		settings.setProp("CSVSaveLocation", saveDirectoryTextField.getText());
		settings.setProp("AutoSave", String.valueOf(saveOnReadCheckBox.isSelected()));
		settings.setProp("DefaultProfile", profileComboBox.getSelectedItem().toString());
		settings.saveConfig();
	}
	
	
	/**
	 * Handles the button press of browse button. This is an action event which must handled before the rest of the program resumes. This method allows the user to navigate
	 * the file explorer and select a save location for the incoming data.
	 */
	public void saveDirectoryBrowseBtnHandler() {
		JFileChooser chooser;
		chooser = new JFileChooser(); 
		chooser.setCurrentDirectory(new java.io.File("."));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setAcceptAllFileFilterUsed(false);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			saveDirectoryTextField.setText(chooser.getSelectedFile().toString());
		}
		else {
			saveDirectoryTextField.setText(null);
		}
	}
	
	
	/**
	 * Create the frame.
	 */
	public SettingsWindow() {
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(new CardLayout(0, 0));
		
		ArrayList<String> profileList = new ArrayList<String>();
		profileList.add("Adventurer");
		profileList.add("Educator");
		profileList.add("Professional");
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		contentPane.add(tabbedPane, "name_759004656281180");
		
		JPanel directorySaveLocations = new JPanel();
		tabbedPane.addTab("Folder Locations", null, directorySaveLocations, null);
		directorySaveLocations.setLayout(null);
		
		JLabel saveDirectoryLabel = new JLabel("Save Directory:");
		saveDirectoryLabel.setBounds(12, 16, 75, 14);
		directorySaveLocations.add(saveDirectoryLabel);
		
		saveDirectoryTextField = new JTextField();
		saveDirectoryTextField.setBounds(97, 13, 240, 20);
		saveDirectoryTextField.setText((String) null);
		saveDirectoryTextField.setColumns(10);
		directorySaveLocations.add(saveDirectoryTextField);
		
		JButton saveDirectoryBrowseBtn = new JButton("Browse");
		saveDirectoryBrowseBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				saveDirectoryBrowseBtnHandler();
			}
		});
		saveDirectoryBrowseBtn.setBounds(352, 12, 67, 23);
		directorySaveLocations.add(saveDirectoryBrowseBtn);
		
		JButton saveAndExitBtn = new JButton("Save and exit");
		saveAndExitBtn.addActionListener(new java.awt.event.ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveBtnHandler();
				dispose();
			}
		});
		saveAndExitBtn.setBounds(320, 200, 99, 23);
		saveAndExitBtn.setBackground(new Color(0, 128, 0));
		directorySaveLocations.add(saveAndExitBtn);
		
		JButton restoreDefaultsBtn = new JButton("Restore defaults");
		restoreDefaultsBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				restoreDefaultsBtnHandler();
				updateUI();
			}
		});
		restoreDefaultsBtn.setBounds(10, 200, 137, 23);
		directorySaveLocations.add(restoreDefaultsBtn);
		
		JLabel lblSaveOnRead = new JLabel("Save on Read:");
		lblSaveOnRead.setBounds(12, 58, 75, 14);
		directorySaveLocations.add(lblSaveOnRead);
		
		saveOnReadCheckBox = new JCheckBox("");
		saveOnReadCheckBox.setBounds(97, 58, 21, 23);
		directorySaveLocations.add(saveOnReadCheckBox);
		
		JLabel lblNewLabel = new JLabel("Profile:");
		lblNewLabel.setBounds(12, 102, 75, 14);
		directorySaveLocations.add(lblNewLabel);
		
		profileComboBox = new JComboBox();
		profileComboBox.setModel(new DefaultComboBoxModel(new String[] {"Professional", "Educator"}));
		profileComboBox.setBounds(97, 99, 106, 20);
		directorySaveLocations.add(profileComboBox);
		
		updateUI();
	}
}
