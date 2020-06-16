package dataorganizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class AxisDataSeries {
	
	private Double[] time;
	
	// data samples BEFORE any rolling average is applied
	// this is the base data used to calculate all rolling averages
	// this data will still be calibrated and normalized, NOT raw
	private Double[] originalData;
	
	// data samples after rolling average is applied to original data
	private Double[] smoothedData;
	
	//data for display on the graph that the user can access and modify (via applying a custom rolling average) without modifying smoothedData
	//same as smoothedData by default
	private Double[] userSmoothedData;
	
	//Array of data samples with normalization offset applied
	private Double [] normalizedData;
	
	/*normalization offset:
	 * Takes average of first second of samples and sets that as offset (e.g. gravity, initial angular velocity)
	 * to subtract from rest of data 
	 */
	private double normOffset;
	
	//default rolling block size to smooth data for integration
	private int rollBlkSize = 100;
	
	private AxisType axis;
	
	//samples per second in passed in data series
	private int sampleRate;
	
	/**
	 * AxisDataSeries constructor for data NOT natively recorded by the module OR from the magnetometer.
	 * @param time - the time axis for the data set
	 * @param data - the samples for the data set
	 * @param axis - an {@link dataorganizer.AxisType AxisType} identifying the data set
	 * @param signData - indicates whether the data should be converted from unsigned to signed
	 * @param sampleRate - the number of data samples recorded in one second
	 */
	public AxisDataSeries(List<Double> time, List<Double> data, AxisType axis, boolean signData, int sampleRate) {
		
		// casts Lists to Double[]'s
		// (this is done b/c DataOrganizer uses ArrayLists)
		// TODO change "time" and "data" to Double[] when GenericTest is updated to use SerialComm
		//
		this.time = new Double[time.size()];
		this.time = time.toArray(this.time);

		// If dealing w/ magnetometer, only save every 10th data sample removing nulls
		// This is because mag data is sampled at 1/10 the rate of accel/gyro,
		// but the List "data" is filled w/ null samples assuming 960 samples/sec
		if (axis.getValue() >= 24 && axis.getValue() <= 26) {

			for (int i = data.size() - 1; i >= 0; i--) {

				if (data.get(i) == null) {
					data.remove(i);
				}

			}

		}

		this.originalData = new Double[data.size()];
		this.originalData = data.toArray(this.originalData);

		this.axis = axis;
		this.sampleRate = sampleRate;
		
		if (signData) {
			
			for (int i = 0; i < this.originalData.length; i++) {

				// convert raw data to signed data
				if (this.originalData[i] > 32768) {
					this.originalData[i] -= 65535;
				}
				
			}
			
		}
				//create normalized data series using first second of module data
				createNormalizedData(0.0, 1.0, sampleRate);
				//creates smoothedData by applying rolling average to normalized data		
				smoothedData = applyMovingAvg(normalizedData.clone(), rollBlkSize);
				//clones smoothedData for display on graph
				userSmoothedData= smoothedData.clone();

		System.out.println(axis + " | " + "Time: " + this.time.length + " | Data: " + this.smoothedData.length);

	}
	
	/**
	 * AxisDataSeries constructor for acceleration data.
	 * @param time - the time axis for the data set
	 * @param data - the samples for the data set
	 * @param axis - an AxisType identifying the type of data
	 * @param accelOffsets - the array of offsets to be applied to the acceleration data;
	 * the first dimension indicates the axis type (X=0,Y=1,Z=2) and the second dimension
	 * stores the bounds, where index 0 is the minimum offset and index 1 is the maximum offset 
	 * @param accelSensitivity - the maximum value or "resolution" of the raw data, being 1, 2, 4, or 8 Gs;
	 * this value is multiplied by how close the measurement was to the maximum value to calculate the physical quantity.
	 * <p>In equation format: acceleration = sensitivity * (raw data / max value for data [32768])
	 * @param sampleRate - the number of data samples recorded in one second
	 */
	public AxisDataSeries(List<Double> time, List<Double> data, AxisType axis, int[] accelOffsets, int accelSensitivity, int sampleRate) {
		
		// casts Lists to Double[]'s
		// (this is done b/c DataOrganizer uses ArrayLists)
		// TODO change "time" and "data" to Double[] when GenericTest is updated to use SerialComm
		//
		this.time = new Double[time.size()];
		this.time = time.toArray(this.time);
		
		this.originalData = new Double[data.size()];
		this.originalData = data.toArray(this.originalData);
		
		this.axis = axis;
		this.sampleRate = sampleRate;

		for (int i = 0; i < this.originalData.length; i++) {
			
			// convert raw data to signed data
			if (this.originalData[i] > 32768) {
				this.originalData[i] -= 65535;
			}
			
			// subtract offsets (which are signed)
			// accel enum is 0-2 and offsets are 0-2 (X,Y,Z)
			this.originalData[i] -= accelOffsets[axis.getValue()];

			// apply sensitivity for accel
			this.originalData[i] *= ((double) accelSensitivity) / 32768;

		}
		
		//create normalized data series using first second of module data
		createNormalizedData(0.0, 1.0, sampleRate);
		//creates smoothedData by applying rolling average to normalized data		
		smoothedData = applyMovingAvg(normalizedData.clone(), rollBlkSize);
		//clones smoothedData for display on graph
		userSmoothedData= smoothedData.clone();

		System.out.println(axis + " | " + "Time: " + this.time.length + " | Data: " + this.smoothedData.length);
		
	}

	/**
	 * AxisDataSeries constructor for gyroscope data.
	 * @param time - the time axis for the data set
	 * @param data - the samples for the data set
	 * @param axis - an AxisType identifying the type of data
	 * @param gyroSensitivity - the maximum value or "resolution" of the raw data;
	 * this value is multiplied by how close the measurement was to the maximum value to calculate the physical quantity.
	 * In equation format: gyro = sensitivity * (raw data / max value for data [32768])
	 * @param sampleRate - the number of data samples recorded in one second
	 */
	public AxisDataSeries(List<Double> time, List<Double> data, AxisType axis, int gyroSensitivity, int sampleRate) {
		
		// casts Lists to Double[]'s
		// (this is done b/c DataOrganizer uses ArrayLists)
		// TODO change "time" and "data" to Double[] when GenericTest is updated to use SerialComm
		//
		this.time = new Double[time.size()];
		this.time = time.toArray(this.time);
		
		this.originalData = new Double[data.size()];
		this.originalData = data.toArray(this.originalData);
		
		this.axis = axis;
		this.sampleRate = sampleRate;

		for (int i = 0; i < this.originalData.length; i++) {
			
			// convert raw data to signed data
			if (this.originalData[i] > 32768) {
				this.originalData[i] -= 65535;
			}
			
			// apply sensitivity for gyro
			this.originalData[i] *= ((double) gyroSensitivity) / 32768;		
			
		}
		//average data from the first second to get normalization offset
				double sum = 0;
				for(int i = 0; i <sampleRate; i++) {
					sum+=originalData[i];
				}
				sum/=sampleRate;
				normOffset = sum;

				//subtract normOffset from each originalData value to create array of normalized data
				this.normalizedData = this.originalData.clone();
				for(int i =0 ; i <originalData.length; i++) {
					normalizedData[i] = originalData[i] - normOffset;
				}
		
		//create normalized data series using first second of module data
		createNormalizedData(0.0, 1.0, sampleRate);
		//creates smoothedData by applying rolling average to normalized data		
		smoothedData = applyMovingAvg(normalizedData.clone(), rollBlkSize);
		//clones smoothedData for display on graph
		userSmoothedData= smoothedData.clone();

		System.out.println(axis + " | " + "Time: " + this.time.length + " | Data: " + this.smoothedData.length);
		
	}

	/**
	 * <b>USED FOR TESTING CALCULATIONS, NOT FOR PRODUCTION</b><p>
	 * Constructor for producing linear acceleration axis.
	 * Utilizes a low-pass filter to isolate the gravity vector,
	 * then subtracts this from raw acceleration to yield linear acceleration.
	 * @deprecated filter does not work as intended, will be removed in the future
	 * @param accel - the AxisDataSeries for the raw acceleration data set
	 */
	@Deprecated
	public AxisDataSeries(AxisDataSeries accel) {

		this.time = new Double[accel.getTime().size()];
		this.time = accel.getTime().toArray(this.time);
		
		this.originalData = accel.getOriginalData().clone();

		Double[] gravity = new Double[this.originalData.length];
		Arrays.fill(gravity, 0d);

		// TODO unsure about the nature of ALPHA:
		// found it is calculated α = t / (t + dT),
		// but not sure what "t" (time constant) would be;
		// using a value that I have seen online.
		final float ALPHA = 0.9f;

		gravity[0] = originalData[0];

		for (int i = 1; i < gravity.length; i++) {

			gravity[i] = ALPHA * gravity[i-1] + (1 - ALPHA) * originalData[i];
			originalData[i] -= gravity[i];

		}

		this.smoothedData = this.originalData.clone();
		//clones smoothedData for display on graph
		userSmoothedData= smoothedData.clone();

	}
	
	
	/**
	 * 
	 * @param startTime - Time that indicates lower bound (inclusive) of normalization period
	 * @param endTime - Time that indicates lower bound (inclusive) of normalization period
	 * @param sampleRate - sampleRate for series being normalized
	 */
	public void createNormalizedData(Double startTime, Double endTime, int sampleRate) {
		//Convert times to sample #s 
		int startIndex = (int)Math.round( startTime*sampleRate);
		int endIndex = (int)Math.round( endTime*sampleRate); 
		//average data from the first second to get normalization offset
				double sum = 0;
				for(int i = startIndex; i <endIndex; i++) {
					sum+=originalData[i];
				}
				sum/=sampleRate;
				normOffset = sum;

				//subtract normOffset from each originalData value to create array of normalized data
				this.normalizedData = this.originalData.clone();
				for(int i =0 ; i <originalData.length; i++) {
					normalizedData[i] = originalData[i] - normOffset;
				}	
	}
	
	
	/**
	 * Modular method to apply a middle-based simple moving average to a data series
	 * @param array - the data series for the moving average to be applied to
	 * @param sampleBlockSize - the number of samples used to calculate the moving average.
	 */
	public Double [] applyMovingAvg(Double [] array, int sampleBlockSize) {

		Double [] newArray = array.clone();
		//if (sampleBlockSize == 0) return;
		
		for (int i = sampleBlockSize/2; i < newArray.length - sampleBlockSize/2; i++) {
			double localTotal = 0;
			for (int j = (i - sampleBlockSize/2); j<(i+sampleBlockSize/2); j++) {
				localTotal+=array[j];
			}
			newArray[i]=(localTotal/sampleBlockSize);
		}
		
		return newArray;
	}
	
	
	/**
	 * Applies middle-based moving average with block size of user's choice 
	 * Intended as wrapper method for applyMovingAvg so other classes can easily modify intended data series (userSmoothedData)
	 * @param sampleBlockSize
	 */
	public void applyCustomMovingAvg(int sampleBlockSize) {
		userSmoothedData = applyMovingAvg(smoothedData, sampleBlockSize);
	}
	
		
	// doing trapezoidal rule for integration
	public List<Double> integrate() {
		
		Double[] result = new Double[smoothedData.length];
		
		result[0] = 0.0;
		
		for(int i = 1; i < smoothedData.length; i++) {	
			result[i] = result[i-1] + (smoothedData[i] + smoothedData[i-1])/2 * (time[i] - time[i-1]);
		}
		
		// calculate the first integrated sample
		// this is our initial condition (our "+C")
		result[0] = result[1];

		return Arrays.asList(result);
	}
	
	
	//get slope of secant line between point directly before and after
	public List<Double> differentiate() {
		
		Double[] result = new Double[smoothedData.length]; 
		for(int i = 1; i < smoothedData.length - 1; i++) {
			result[i] = (smoothedData[i+1]-smoothedData[i-1])/(time[i+1]-time[i-1]);
		}
		
		//fill in first and last data point
		result[0] = result[1];
		result[smoothedData.length-1] = result[smoothedData.length-2];

		return Arrays.asList(result);
	}
	
	
	// returns slope at a specific data point (tangent line)
	public Double getSlope(Double time) {
		//uses secant line method b/w sample directly before and after
		int i = (int) Math.round(time*this.sampleRate);
		Double slope = (smoothedData[i+1]-smoothedData[i-1])/(this.time[i+1]-this.time[i-1]);
		return slope;
	}
	
	
	// returns slope between two points (secant line)
	public Double getSlope(Double startTime, Double endTime) {
		Double slope = 0.0;
		
		if (startTime == endTime) {
			int i = (int) Math.round(startTime*this.sampleRate);
			slope = (smoothedData[i+1]-smoothedData[i-1])/(this.time[i+1]-this.time[i-1]);
			return slope;
		}
		else {
		int i = (int) Math.round(startTime*this.sampleRate);
		int j = (int) Math.round(endTime*this.sampleRate);
		slope = (smoothedData[j]-smoothedData[i])/(this.time[j]-this.time[i]);}
		return slope;
	}
	
	// returns area under a section of a data curve utilizing the trapezoid method
	public Double getAreaUnder(Double startTime, Double endTime) {
		Double area = 0.0;
		int i = (int) Math.round(startTime*this.sampleRate);
		int j = (int) Math.round(endTime*this.sampleRate);
		area = ((smoothedData[i]+smoothedData[j])/2)*(this.time[j]-this.time[i]);
		return area;
	}
	

	public Double[] getSmoothedData() {
		return this.smoothedData;
	}
	
	
	public void setSmoothedData(Double[] data) {
		this.smoothedData = data;
	}
	
	
	public void setSmoothedDataPoint(int index, Double value) {
		this.smoothedData[index] = value;	
	}
	
	
	public Double[] getOriginalData() { 
		return this.originalData;
	}
	
	
	public void setOriginalData(Double[] data) {
		this.originalData = data;
	}
	
	
	public void setOriginalDataPoint(int index, Double value ) {
		this.originalData[index] = value;	
	}
	
	
	/**
	 * Used by Graph GUI to get time data from AxisDataSeries instances.
	 */
	public ArrayList<Double> getTime() {
		return new ArrayList<Double>(Arrays.asList(time));
	}
	
	
	/**
	 * Used by Graph GUI to get samples from AxisDataSeries instances.
	 */
	public ArrayList<Double> getSamples() {
		return new ArrayList<Double>(Arrays.asList(userSmoothedData));
	}
	
}