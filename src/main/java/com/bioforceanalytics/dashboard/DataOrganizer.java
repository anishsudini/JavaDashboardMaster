package com.bioforceanalytics.dashboard;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.filechooser.FileSystemView;

public class DataOrganizer {

	private List<List<Double>> dataSamples; // Can be thought of as 2-d list, the "x" can be thought of as each of the dofs, the y is the sample number and the correcting sample data for each dof.
	private List<List<Double>> signedDataSamples;
	private List<List<Double>> normalizedDataSamples;
	private List<Integer> testParameters;
	private List<Double> dofTime;
	public int[][] MPUMinMax; //first dimension is for axis(1), second is for min(0)/max(1) 
	private String nameOfTest;
	private int sampleRate;
	private int timer0;
	private int magSampleRate;
	private int magInterval;
	public int accelSensitivity;
	public int gyroSensitivity;
	private int magSensitivity;
	private double lengthOfTest;
	private int numDof = 9;
	private int numParams = 13;
	private String dataSourceID;
	private int sourceID;
	private String moduleSerialID;

	private int delayAfterStart; //
	private int lineNum;
	private double max = 0;
	private double min = 0;

	public DataOrganizer(ArrayList<Integer> testParameters, String testName) {
		this.nameOfTest = testName;
		this.testParameters = testParameters;
		timer0 = testParameters.get(1);
		delayAfterStart = testParameters.get(2);
		lengthOfTest = testParameters.get(6);
		sampleRate = testParameters.get(7);
		magSampleRate = testParameters.get(8);
		accelSensitivity = testParameters.get(9);
		gyroSensitivity = testParameters.get(10);
		magSensitivity = 4800;
		magInterval = sampleRate / magSampleRate;
	}

	public DataOrganizer() {
		numDof = 9;
		sampleRate = 960;
		lengthOfTest = 6.9;
	}

	public void setSourceID(String dataSourceID, int sourceID) {
		this.sourceID = sourceID;
		this.dataSourceID = dataSourceID;
	}

	public int getSourceID() {
		return this.sourceID;
	}

	public String getSourceId() {
		return this.dataSourceID;
	}

	public String getSerialID() {
		return this.moduleSerialID;
	}
	public void setSerialID(String ID) {
		this.moduleSerialID = ID;
	}

	public List<Integer> getTestParameters(){
		return testParameters;
	}

	public int[][] getMPUMinMax(){
		return MPUMinMax;
	}


	public List<List<Double>> createDataSmpsRawData(int[] data) {

		boolean endCondition = false;
		int byteCounter = 0;

		dataSamples = new ArrayList<List<Double>>();
		for (int dof = 0; dof < 10; dof++) {
			List<Double> temp = new ArrayList<Double>();
			dataSamples.add(dof, temp);
		}
		/*
		 // Adds null Samples for when the delay is less than 0 (DelayAfterStart depends on Camera Delay)
		 */
		if (delayAfterStart < 0) {
			int delayAdditionalLineNums = (int) Math.round(((double) delayAfterStart / -1000.0) * (double) (timer0 / 4)); // sampleRate is equal to timer0 / 4. ((timer0 * 250)/4) is the unsimplified expression.

			for (int i = 0; i < delayAdditionalLineNums; i++) {
				dataSamples.get(0).add((double) i / ((double)timer0*250)/1000);
				if (magInterval == 10 && i % 10 == 0) {
					for (int dof9 = 1; dof9 < 10; dof9++) {
						dataSamples.get(dof9).add(0.0);
					}
				} else if (magInterval == 10 && i % 10 != 0) {
					for (int dof6 = 1; dof6 < 7; dof6++) {
						dataSamples.get(dof6).add(0.0);
					}
				} else if (magInterval == 1) {
					for (int dof9 = 1; dof9 < 10; dof9++) {
						dataSamples.get(dof9).add(0.0);
					}
				}
			}
			lineNum = delayAdditionalLineNums;
		} else {
			lineNum = 0;
		}

		while (!endCondition) {
			for (int i = 0; i < magInterval && !endCondition; i++) {
				dataSamples.get(0).add(lineNum, (double) lineNum / ((double)timer0*250)/1000); // adds the time to the first column;

				if (i == 0) {
					for (int dof9 = 1; dof9 < 10; dof9++) { // starts at 1 because 0 is time
						if (data[byteCounter] == -1 || data[byteCounter + 1] == -1) {
							endCondition = true;
							break;
						}
						dataSamples.get(dof9).add(lineNum, (double) ((data[byteCounter] * 256) + data[byteCounter + 1]));
						byteCounter += 2;
					}
				} else {
					for (int dof6 = 1; dof6 < 10; dof6++) {
						if (data[byteCounter] == -1 || data[byteCounter + 1] == -1) {
							endCondition = true;
							break;
						}
						if (dof6 < 7) {
							dataSamples.get(dof6).add(lineNum, (double) (data[byteCounter] * 256) + data[byteCounter + 1]);
							byteCounter += 2;
						} else {
							dataSamples.get(dof6).add(lineNum, null);
						}
					}
				}
				lineNum++;
				// System.out.println(byteCounter - tempByteCounter);
			}

		}
		lineNum--;// Subtracts 1 since there isn't actaully a sample for the final increment of lineNum
		lengthOfTest = (double) lineNum / (double) sampleRate; // Dimension analysis principle; Samples / (Samples / Unit Time) = Time
		return dataSamples;
	}
	
	public List<List<Double>> getSignedData() {
		signedDataSamples = new ArrayList<List<Double>>();
		for (int dof = 0; dof < 10; dof++) {
			List<Double> temp = new ArrayList<Double>();
			signedDataSamples.add(dof, temp);
		}

		signedDataSamples.get(0).addAll(dataSamples.get(0));

		for (int dof = 1; dof < 10; dof++) { // For each degree of freedom
			if(dof < 7) //If the dof is accel or gyro - Accel and Gyro and sampled differently than mag
				for (int smp = 0; smp < dataSamples.get(dof).size()-1; smp++) { // smp - Sample ; For each sample
					if (dof < 4) { // If the dof is accel
						double curVal = dataSamples.get(dof).get(smp);// Actual Conversion and Raw Data to Signed Data
						if (curVal > 32768) {
							curVal -= 65535;
						}
						curVal = (curVal * accelSensitivity) / 32768;
						signedDataSamples.get(dof).add(smp, curVal);
					} 
					else { // This means the dof is Gyro
						double curVal = dataSamples.get(dof).get(smp);
						if (curVal > 32768) {
							curVal -= 65535;
						}
						curVal = (curVal * gyroSensitivity) / 32768;
						signedDataSamples.get(dof).add(smp, curVal);
					} 
					//else {
					//signedDataSamples.get(dof).add(smp, dataSamples.get(dof).get(smp));
					/*if(curVal != null) {
							if (curVal > 32768) {
								curVal -= 65535;
							}
							curVal = (curVal * magSensitivity) / 32768;*/
					//signedDataSamples.get(dof).add(smp, curVal);
					//	}
				}
			if(dof > 6) // When the dof in question is mag
				for(int smp = 0; smp < (dataSamples.get(0).size()/10); smp++) { // Divided by 10 as MAG is sampled once for every 10 times that Accel is sampled.
					signedDataSamples.get(dof).add(dataSamples.get(dof).get(smp));
				}
		}

		return signedDataSamples;
	}

	/**
	 * Creates new .CSVP file for storing the test parameters of a given test, We need this alongside the CSV file for graphing purposes.
	 */
	public int createCSVP() {
		String CSVPath = Settings.get("CSVSaveLocation"); //Pull up the directory of chosen as CSV location files
		PrintWriter dataFile = null;
		try {
			dataFile = new PrintWriter(CSVPath + File.separator + nameOfTest + "p"); //Create new file in CSV Directory, file extension is .csvp 
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -1;
		}
		for(int i = 0; i < testParameters.size(); i++) { //Write all parameters to the file. We really only needed like 3 at the time of writing but this was easier and probably more effective in the future.
			dataFile.println(testParameters.get(i).toString());
		}
		
		for(int i = 0; i < 9; i++) {
			dataFile.println(MPUMinMax[i][0]);
			dataFile.println(MPUMinMax[i][1]);
		}

		for(int i = testParameters.size(); i<32; i++){ // This makes literally no sense but for some reason the data can't be graphed unless the csvp has 32 values. After the actual test parameters it doesn't matter what there is but there has to be something. It is unclear when this can be removed.
			dataFile.println(0);
		}

		dataFile.close();
		return 0;
	}

	/*
	 * Read the test parameters from the file we stored them in. This file will be in the same directory as the .csv and has the 
	 * extension .csvp (extension is misleading. They are really separated by newlines, which is actually data inefficient I realize as I write this. \cr\lf is two bytes and a comma is one. no biggie right? )
	 */
	public int readAndSetTestParameters(String pathToFile) {
		File f = new File(pathToFile); //Create a file object of the .csvp, just to get the file name. I was told this way is more efficient than substrings. Its definitely shorter than string manipulation. Plus I would have had to taken other OS file separators into account
		this.nameOfTest = f.getName(); //set the name of the test
		BufferedReader CSVPFile = null; //Reader for reading from the file
		String lineText = "";
		testParameters = new ArrayList<Integer>(); //Reinstantiate the testParameters object. Just for a quick while loop. 
		try {
			CSVPFile = new BufferedReader(new FileReader(pathToFile)); //open the file for reading
		} catch (FileNotFoundException e) {
			return -1;			//File Permissions error
		}
		try {
			for(int i = 0; i < numParams; i++){ 
				lineText = CSVPFile.readLine();
				testParameters.add(testParameters.size(), Integer.parseInt(lineText)); //Parse as an int and add to test params
			}
			MPUMinMax = new int[9][2];
			for(int i = 0; i < 9; i++) {
				MPUMinMax[i][0] = Integer.valueOf(CSVPFile.readLine());
				MPUMinMax[i][1] = Integer.valueOf(CSVPFile.readLine());
			}
		}catch (NumberFormatException e) {
			e.printStackTrace();
			//NFE really shouldn't happen but it would mean that the file is corrupt.
			try {
				CSVPFile.close(); //Try to close the file
			} catch (IOException e1) {
				// I guess we can't close the corrupt file either.
			}
			return -2;	//Corrupt .CSVP
		} catch(IOException e) {
			e.printStackTrace();
			try {
				CSVPFile.close();
			} catch (IOException e1) {
				// I guess we can't close the corrupt file either.
			}
			return -3; //Permissions error as well.
		}

		/*
		 * Just set these variables because thats where we reference them from most of the time.
		 */
		timer0 = testParameters.get(1);
		delayAfterStart = testParameters.get(2);
		lengthOfTest = testParameters.get(6);
		sampleRate = testParameters.get(7);
		magSampleRate = testParameters.get(8);
		accelSensitivity = testParameters.get(9);
		gyroSensitivity = testParameters.get(10);
		magSensitivity = 4800; //TODO: not constant

		try {
			CSVPFile.close();
		} catch (IOException e1) {
			// I guess we can't close the file either.
		}

		return 0;
	}

	public void setTestParameters(List<Integer> params) {
		lengthOfTest = params.get(6);
		sampleRate = params.get(7);
		magSampleRate = params.get(8);
		accelSensitivity = params.get(9);
		gyroSensitivity = params.get(10);
		magSensitivity = 4800; 
	}

	/**
	 * Creation of a modified data set with an applied Rolling Average. Used for applying in average in graphing to smooth data.
	 * @param dataSet
	 * @param rollRange
	 * @param dof
	 * @return
	 */
	
	public List<List<Double>> rollingBlock(int dataSet, int rollRange, int dof) {
		List<List<Double>> modifiedDataSmps = new ArrayList<List<Double>>();
		switch(dataSet) {
		case 0:
			modifiedDataSmps = dataSamples;
			break;
		case 1: 
			modifiedDataSmps = signedDataSamples;
			break;
		}
		
		for(int j = 0; j < modifiedDataSmps.get(dof).size(); j++) { //iterate smps
			double avg = 0.0;
			for(int i = 0; i < rollRange && i+j < modifiedDataSmps.get(dof).size(); i++) { 								//Sum 10 smps, do not exceed size of dof data size
				if(dof > 6 && modifiedDataSmps.get(dof).get(i+j) == null) continue;
				avg += modifiedDataSmps.get(dof).get(j+i);
			}
			avg = avg/rollRange; //divide for average
			modifiedDataSmps.get(dof).set(j, avg);
		}

		return modifiedDataSmps;
	}

	/**
	 * Creates a CSV file by extracting the data from the module and running it through a 2D List that is extracted from
	 * and loaded into a spreadsheet then from the Settings class they import the "CSVSaveLocation"
	 * @param labelData
	 * @param signedData
	 * @return
	 */
	public int createCSV(boolean labelData, boolean signedData) { 														// The two passed booleans determine if the output data is signed and labeled.
		List<List<Double>> modifiedDataSmps;

		if(!signedData)
			modifiedDataSmps = dataSamples;
		else
			modifiedDataSmps = signedDataSamples;


		StringBuilder builder = new StringBuilder();
		PrintWriter DataFile;
		if (!labelData) {
			for (int smp = 0; smp < lineNum - 1; smp++) {
				for (int dof = 1; dof <= numDof; dof++) {
					if (modifiedDataSmps.get(dof).get(smp) != null) {
						builder.append(modifiedDataSmps.get(dof).get(smp));
						builder.append(",");

					}
				}
				builder.append("\n");
			}
		} else {
			builder.append("t,AccelX,AccelY,AccelZ,GyroX,GyroY,GyroZ,MagX,MagY,MagZ,\n");
			for (int smp = 0; smp < lineNum - 1; smp++) {
				for (int dof = 0; dof < numDof; dof++) {
					if (modifiedDataSmps.get(dof).get(smp) != null) {
						builder.append(modifiedDataSmps.get(dof).get(smp));
						builder.append(",");

					}
				}
				builder.append("\n");
			}
		}

		String fileOutputDirectory = Settings.get("CSVSaveLocation");

		try {
			if (fileOutputDirectory != null) {
				DataFile = new PrintWriter(new File(fileOutputDirectory + File.separator + nameOfTest));
			} else {
				DataFile = new PrintWriter(new File((FileSystemView.getFileSystemView() // Creates .CSV file in default
						// directory which is documents
						.getDefaultDirectory().toString() + File.separator + nameOfTest)));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		}
		DataFile.write(builder.toString()); // writes the string buffer to the .CSV creating the file
		DataFile.close(); // close the .CSV
		return 0;

	}
//	public String pathToCSVFile(){
//		Settings settings = new Settings();
//		return settings.getKeyVal("CSVSaveLocation") + File.separator + nameOfTest;
//	}

	public int createCSVsForTwoModuleEducationModeTest(boolean labelData, boolean signedData, int moduleNumber){
		List<List<Double>> modifiedDataSmps;

		if(!signedData)
			modifiedDataSmps = dataSamples;
		else
			modifiedDataSmps = signedDataSamples;


		StringBuilder builder = new StringBuilder();
		PrintWriter DataFile;
		if (!labelData) {
			for (int smp = 0; smp < lineNum - 1; smp++) {
				for (int dof = 1; dof <= numDof; dof++) {
					if (modifiedDataSmps.get(dof).get(smp) != null) {
						builder.append(modifiedDataSmps.get(dof).get(smp));
						builder.append(",");

					}
				}
				builder.append("\n");
			}
		} else {
			builder.append("t,AccelX,AccelY,AccelZ,GyroX,GyroY,GyroZ,MagX,MagY,MagZ,\n");
			for (int smp = 0; smp < lineNum - 1; smp++) {
				for (int dof = 0; dof < numDof; dof++) {
					if (modifiedDataSmps.get(dof).get(smp) != null) {
						builder.append(modifiedDataSmps.get(dof).get(smp));
						builder.append(",");

					}
				}
				builder.append("\n");
			}
		}

		String fileOutputDirectory = Settings.get("CSVSaveLocation");

		if (moduleNumber == 1){
			try {
				if (fileOutputDirectory != null) {
					DataFile = new PrintWriter(new File(fileOutputDirectory + File.separator + nameOfTest));
				} else {
					DataFile = new PrintWriter(new File((FileSystemView.getFileSystemView() // Creates .CSV file in default
							// directory which is documents
							.getDefaultDirectory().toString() + File.separator + "Module 1 " + nameOfTest)));
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return -1;
			}
			DataFile.write(builder.toString()); // writes the string buffer to the .CSV creating the file
			DataFile.close(); // close the .CSV
			return 0;
		}else{
			try {
				if (fileOutputDirectory != null) {
					DataFile = new PrintWriter(new File(fileOutputDirectory + File.separator + nameOfTest));
				} else {
					DataFile = new PrintWriter(new File((FileSystemView.getFileSystemView() // Creates .CSV file in default
							// directory which is documents
							.getDefaultDirectory().toString() + File.separator + "Module 2 " + nameOfTest)));
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				return -1;
			}
			DataFile.write(builder.toString()); // writes the string buffer to the .CSV creating the file
			DataFile.close(); // close the .CSV
			return 0;
		}
	}


	public int createDataSamplesFromCSV(String CSVFilePath) { 
		int errNum;
		if((errNum = readAndSetTestParameters(CSVFilePath + 'p')) != 0) { //CSVP file. Should be kept with CSV File
			return errNum;
		}
		dataSamples = new ArrayList<List<Double>>();	

		double interval = (1.0 / sampleRate);
		int numSample = (int) (sampleRate * lengthOfTest);

		for (int dof = 1; dof <= numDof+1; dof++) {
			List<Double> temp = new ArrayList<Double>();
			dataSamples.add(temp);
		}

		BufferedReader br = null;

		try {
			String line = "";
			br = new BufferedReader(new FileReader(CSVFilePath));

			while ((line = br.readLine()) != null) {
				
				String[] sample = line.split(",");
				
				if(sample.length < 7) {
					dataSamples.get(7).add(null);
					dataSamples.get(8).add(null);
					dataSamples.get(9).add(null);
				}
				
				for (int str = 0; str < sample.length; str++) {
					
					try {
						dataSamples.get(str+1).add(Double.parseDouble(sample[str]));
						min = Math.min(Double.parseDouble(sample[str]), min);
						max = Math.max(Double.parseDouble(sample[str]), max);
					} catch (NumberFormatException nfe) {
						nfe.printStackTrace();
						break;
					}
				}
			}
		} catch (FileNotFoundException ex) {
			System.out.println("NO FILE FOUND");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		for(int i = 0; i <dataSamples.get(1).size();i++) {
			dataSamples.get(0).add( (double)i / (double)sampleRate);
		}
		return 0;
	}


	public void applyAccelOffset(double AccelOffset, int dof) {
		for(int i = 0; i < signedDataSamples.get(dof).size(); i++) {
			signedDataSamples.get(dof).set(i, signedDataSamples.get(dof).get(i) + AccelOffset);
		}
	}

	public List<List<Double>> getDataSamples() {
		return dataSamples;
	}


	public void setDataSmps(List<List<Double>> dataSmps) {
		this.dataSamples = dataSmps;
	}


	public double getLengthOfTest() {
		this.lengthOfTest = (double)dataSamples.get(0).get(dataSamples.get(0).size()-1);
		return lengthOfTest;
	}

	public int getNumDof() {
		return numDof;
	}

	public int getSampleRate() {
		return sampleRate;
	}
	
	public int getMagSampleRate() {
		return magSampleRate;
	}

	public String getName() {
		return nameOfTest;
	}

	public void setName(String name) {
		this.nameOfTest = name;
	}

	public List<List<Double>> returnSignedData(){
		return signedDataSamples;
	}

	public List<List<Double>> getByConversionType(int dataConversionType) {
		switch(dataConversionType) {
		case 1: return signedDataSamples;
		case 2: return dataSamples;
		case 3: return normalizedDataSamples;
		}
		return null;
	}

	// "dofTime" is NOT populated by "createDataSamplesFromCSV()"
	// calculate this manually using "getSampleRate()" and "getLengthOfTest()"
	//
	public List<Double> getTimeAxis(){
		return dofTime;
	}



	public List<List<Double>> getZoomedSeries(double start, double end, int dofNum, int dataConversionType) {
		if(dofNum == 10) return getMagnitudeSeries(start, end, dataConversionType);

		List<List<Double>> modifiedDataSmps = new ArrayList<List<Double>>();
		switch(dataConversionType) {
		case(0):
			modifiedDataSmps = dataSamples;
		break;
		case(1): 
			modifiedDataSmps = signedDataSamples;
		break;
		case(2):
			modifiedDataSmps = normalizedDataSamples;
		break;
		}


		int numSamples = (int) Math.round((end - start) * sampleRate);

		double rate = 7000.0 / (double) numSamples;
		double newSps = (sampleRate * rate);
		double modifier = sampleRate / newSps;

		List<List<Double>> dofData = new ArrayList<List<Double>>();

		dofTime = new ArrayList<Double>();
		List<Double> dofAxis = new ArrayList<Double>();

		if (modifier < 1)
			modifier = 1;
		for (int sample = 0; sample < 7000 && ((start * sampleRate) + sample*modifier) < (modifiedDataSmps.get(0).size() - 1); sample++) {
			dofTime.add(sample, modifiedDataSmps.get(0).get((int) ((start * sampleRate) + (int) (sample * modifier))));
		}

		dofData.add(0, dofTime);

		for (int sample = 0; sample < 7000 && (start * sampleRate) + (int) (sample * modifier) < (modifiedDataSmps.get(dofNum).size() - 1); sample++) {
			dofAxis.add(sample, modifiedDataSmps.get(dofNum).get((int) ((start * sampleRate) + (int) (sample * modifier))));
		}

		dofData.add(1, dofAxis);

		return dofData;
	}

	/**
	 * Used in EducatorModerControllerFX
	 * @return
	 */

	public List<List<Double>> getRawDataSamples() {
		return dataSamples;
	}

	/*
	 * Creates a series for linechart, using 
	 */
	public List<List<Double>> getMagnitudeSeries(double start, double end, int dataConversionType) {
		List<List<Double>> modifiedDataSmps = new ArrayList<List<Double>>();
		List<List<Double>> dofData = new ArrayList<List<Double>>();
		List<Double> dofTime = new ArrayList<Double>();
		List<Double> dofAxis = new ArrayList<Double>();

		switch(dataConversionType) {
		case(0): 
			modifiedDataSmps = dataSamples;
		break;
		case(1): 
			modifiedDataSmps = signedDataSamples;
		break;
		case(2):
			modifiedDataSmps = normalizedDataSamples;
		}

		int numSamples = (int) Math.round((end - start) * sampleRate);

		double rate = 7000.0 / (double) numSamples;
		double newSps = (sampleRate * rate);
		double modifier = sampleRate / newSps;

		if (modifier < 1)
			modifier = 1;

		for (int sample = 0; sample < 7000 && ((start * sampleRate) + sample) < (modifiedDataSmps.get(0).size() - 1); sample++) {
			dofTime.add(sample, modifiedDataSmps.get(0).get((int) ((start * sampleRate) + (int) (sample * modifier))));
		}

		dofData.add(0, dofTime);

		for(int sample = 0; sample < 7000 && (start * sampleRate) + (int) (sample * modifier) < modifiedDataSmps.get(0).size() - 1; sample++) {

			dofAxis.add(Math.sqrt(
					Math.pow(modifiedDataSmps.get(1).get((int) ((start * sampleRate) + (int) (sample * modifier))), 2)
					+ Math.pow(modifiedDataSmps.get(2).get((int) ((start * sampleRate) + (int) (sample * modifier))), 2) 
					+ Math.pow(modifiedDataSmps.get(3).get((int) ((start * sampleRate) + (int) (sample * modifier))), 2))
					);

		}
		dofData.add(1, dofAxis);

		return dofData;
	}

	public void setMPUMinMax(int[][] mpuMinMax) {
		MPUMinMax = mpuMinMax;
	}


	public List<String> getMPUOffsetString(){
		List<String> mpuOffsetString = new ArrayList<String>();
		for(int axi = 0; axi < MPUMinMax.length; axi++) {
			mpuOffsetString.add(String.valueOf((MPUMinMax[axi][0]+MPUMinMax[axi][1]/2)));
		}
		return mpuOffsetString;
	}
	
	public int[] getMPUOffsets() {
		
		// this length of 9 is unnecessary
		// only 3 entries are being used, 0-2
		// 0: Accel X offset, 1: Accel Y offset, 2: Accel Z offset
		
		int[] mpuOffsets = new int[9];
		for(int axi = 0; axi < MPUMinMax.length; axi++) {
			mpuOffsets[axi] = (MPUMinMax[axi][0]+MPUMinMax[axi][1])/2;
		}
		return mpuOffsets;
	}

	/**
	 * Sets internal private variable MpuMinMax. First array is axis, and second is min[0]/max[1]
	 * @return the offsets for accel, gyro, and mag. (The offsets follow the same ordering convention we use with dataSamples, without the time axis (accel,gyro,mag)(x,y,z))
	 */
	public int[] getCalibrationOffsets(String csvFile, int readBlockLength, int stdDevMax){
		ArrayList<ArrayList<Integer>> inRangeMeans = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> offsetIndexes = new ArrayList<ArrayList<Integer>>();
		ArrayList<ArrayList<Integer>> means = new ArrayList<ArrayList<Integer>>(); //axis.index*readBlockLength
		ArrayList<ArrayList<Integer>> stdDevBlock = new ArrayList<ArrayList<Integer>>();

		for(int i = 0; i < dataSamples.get(0).size()%readBlockLength; i++) {
			means.add(new ArrayList<Integer>());
			stdDevBlock.add(new ArrayList<Integer>());
		}

		for(int axi = 1; axi < 4; axi++) { //Find the mean for $(readBlockLength) blocks of samples in each axis
			for(int k = 0; k < dataSamples.get(axi).size(); k+=readBlockLength) {
				int avg = 0;
				for(int i = 0; i < readBlockLength && i+k < dataSamples.get(axi).size(); i++) {
					int curVal = (int)Math.round(dataSamples.get(axi).get(i+k));
					if (curVal > 32768) {
						curVal -= 65535;
					}
					avg += curVal;
				}
				avg = avg/readBlockLength;
				means.get(axi).add(avg);
			}
		}

		for(int axi = 1; axi < 4; axi++) { //Calculate stdDev for each block of 500 
			for(int block = 0; block < dataSamples.get(axi).size(); block+=readBlockLength) {
				int blockAvg = 0;
				for(int sample = 0; sample < readBlockLength && sample+block < dataSamples.get(axi).size(); sample++) {
					int curVal = (int)Math.round(dataSamples.get(axi).get(sample+block));
					if (curVal > 32768) {
						curVal -= 65535;
					}
					blockAvg += Math.pow((curVal - means.get(axi).get(block/readBlockLength)), 2);
				}
				blockAvg = (int) Math.sqrt(blockAvg/readBlockLength);
				stdDevBlock.get(axi).add(blockAvg);
			}
		}

		inRangeMeans.add(new ArrayList<Integer>());
		offsetIndexes.add(new ArrayList<Integer>());
		for(int axi = 1; axi < 4; axi++) {
			inRangeMeans.add(new ArrayList<Integer>());
			offsetIndexes.add(new ArrayList<Integer>());
			int avg = 0;
			for(int i = 0; i < means.get(axi).size(); i++) { //iterate through means
				if(means.get(axi).get(i) < (-2048 + 1800) && means.get(axi).get(i) > (-2048 - 1800)) { //find means inside -1g range
					if(stdDevMax > stdDevBlock.get(axi).get(i)) { //confirm std deviation is in range
						for(int offsetCalcSampleCounter = i*readBlockLength; offsetCalcSampleCounter < i*readBlockLength+readBlockLength; offsetCalcSampleCounter++) { //create average for sample block
							int curVal = (int)Math.round(dataSamples.get(axi).get(offsetCalcSampleCounter));
							if (curVal > 32768) {
								curVal -= 65535;
							}
							avg += curVal;
						}
						avg = avg/readBlockLength;
						offsetIndexes.get(axi).add(i);
						System.out.println(avg);
						inRangeMeans.get(axi).add(avg);
					}
				}
				else if(means.get(axi).get(i) < (2048 + 1800) && means.get(axi).get(i) > (2048 - 1800)) { //find means inside +1g range
					if(stdDevMax > stdDevBlock.get(axi).get(i)) { //confirm std deviation is in range
						for(int offsetCalcSampleCounter = i*readBlockLength; offsetCalcSampleCounter < i*readBlockLength+readBlockLength; offsetCalcSampleCounter++) { //create average for sample block
							int curVal = (int)Math.round(dataSamples.get(axi).get(offsetCalcSampleCounter));
							if (curVal > 32768) {
								curVal -= 65535;
							}
							avg += curVal;
						}
						avg = avg/readBlockLength;
						offsetIndexes.get(axi).add(i);
						System.out.println(avg);
						inRangeMeans.get(axi).add(avg);
					}
				}
			}
		}



		int[] axisOffsets = new int[9];

		int MpuMinMax[][] = new int[9][2];

		/*for(int axi = 4; axi < 7; axi++) { //Iterate for Gyro min/max
			int avg = 0;

			System.out.println(inRangeMeans.get(axi-3));
			System.out.println(Collections.min(inRangeMeans.get(axi-3)));
			System.out.println(means.get(axi-3));
			System.out.println(means.get(axi-3).indexOf(Collections.min(inRangeMeans.get(axi-3))));
			
			int start = readBlockLength*inRangeMeans.get(axi-3).indexOf(Collections.min(inRangeMeans.get(axi-3))); //Start in the data where we derive accel mpu offsets
			int end = start+readBlockLength; //End where we would stop in the data where we derive accel mpu offsets
			for(int offsetSampleCounter = start; offsetSampleCounter < end; offsetSampleCounter++) {
				avg += dataSamples.get(axi).get(offsetSampleCounter);
			}
			MpuMinMax[axi][0] = avg/readBlockLength;

			start = readBlockLength*inRangeMeans.get(axi-4).indexOf(Collections.max(inRangeMeans.get(axi-4))); //Start in the data where we derive accel mpu offsets
			for(int offsetSampleCounter = start; offsetSampleCounter < end; offsetSampleCounter++) {
				avg += dataSamples.get(axi).get(offsetSampleCounter);
			}
			MpuMinMax[axi][1] = avg/readBlockLength;

		}*/


		/*
		for(int axi = 7; axi < 10; axi++) { //Iterate for mag min/max
			int avg = 0;

			for(int offsetIndex = 0; offsetIndex < offsetIndexes.get(axi-4).size(); offsetIndex++) {
				avg = 0;
				int start = readBlockLength*inRangeMeans.get(axi-7).indexOf(Collections.min(inRangeMeans.get(axi-7))); //Start in the data where we derive accel mpu offsets
				int end = offsetIndexes.get(axi-4).get(offsetIndex)*readBlockLength+readBlockLength; //End where we would stop in the data where we derive accel mpu offsets
				for(int offsetSampleCounter = start; offsetSampleCounter < end; offsetSampleCounter++) {
					avg += dataSamples.get(axi).get(offsetSampleCounter);
				}
				MpuMinMax[axi][0] = avg/readBlockLength;

				start = readBlockLength*inRangeMeans.get(axi-7).indexOf(Collections.max(inRangeMeans.get(axi-7))); //Start in the data where we derive accel mpu offsets
				for(int offsetSampleCounter = start; offsetSampleCounter < end; offsetSampleCounter++) {
					avg += dataSamples.get(axi).get(offsetSampleCounter);
				}
				MpuMinMax[axi][1] = avg/readBlockLength;

			}
		}*/
		System.out.println("testtest1");
		System.out.println("testtest2");
		//System.out.println(Collections.min(inRangeMeans.get(0)));
		System.out.println("testtest3");

		for(int axi = 1; axi < inRangeMeans.size(); axi++ ) { 
			MpuMinMax[axi][0] = Collections.min(inRangeMeans.get(axi));
			MpuMinMax[axi][1] = Collections.max(inRangeMeans.get(axi));
			int offset = ((MpuMinMax[axi][0]+MpuMinMax[axi][1])/2);
			axisOffsets[axi-1] = offset;
		}


		axisOffsets[4] = MpuMinMax[4][0]+MpuMinMax[4][1]/2;
		axisOffsets[5] = MpuMinMax[5][0]+MpuMinMax[5][1]/2;
		axisOffsets[6] = MpuMinMax[6][0]+MpuMinMax[6][1]/2;
		axisOffsets[7] = MpuMinMax[7][0]+MpuMinMax[7][1]/2;
		axisOffsets[8] = MpuMinMax[8][0]+MpuMinMax[8][1]/2;


		MPUMinMax = MpuMinMax;
		return axisOffsets;
	}


	public double maxTestValAxis(int axi) {
		double max = -32768;

		for(Double sample : dataSamples.get(axi)){
			if(sample != null)
				if(sample>max)
					max = sample;
		}
		return max;
	}

	public double minTestValAxis(int axi) {
		double min = 32768;

		for(Double sample: dataSamples.get(axi)){
			if(sample != null)
				if(sample<min)
					min = sample;
		}
		return min;
	}
}
