package dataorganizer;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
/**
 *
 * @author Mobile2
 */
public class BlackFrameAnalysis {
	private float videoFPS;
	private final int DELAY_IN_SECONDS_BEFORE_LIGHT = 2;
	private int NumNonBlack;


	/*
	 * Reads module sample rate, video sample rate, and the video file. 
	 * Returns the offset for TMR0
	 */
	public int getLatencyOffset(int moduleSPS, String vf) throws IOException{
		Process process = Runtime.getRuntime().exec(cmdWrapper(vf));                                                               //get runtime variable to execute command line
		BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));                  //initializes BufferedReader to read the error stream of the CMD
		int lastFrame = 0;                                                                                              //sets integer for the last black frame at 0
		ArrayList<Integer> blackFrames = new ArrayList<>();                                                                  //black frames stores the black frames of the video
		ArrayList<Integer> nBlackFrames = new ArrayList<>();                                                                 //black frames stores the frames that are not black
		String s;                                                                                                       //will store the command line outputs    
		while ((s = stdError.readLine()) != null) { 
			//If line contains the string "[P"
			if(s.substring(0,2).equals("[P")){
				String blackFrame = s.split(" ")[3].split(":")[1];                                                  //parses the number of the frames from the line
				blackFrames.add(Integer.parseInt(blackFrame));                                                      //adds the frame number to the blackFrames list
				
				//Check for frame jump, if there hasn't been a black frame in less than 10 frames then 
				if(Integer.parseInt(blackFrame) - lastFrame < 10) {
					lastFrame = Integer.parseInt(blackFrame);
					NumNonBlack = Integer.parseInt(blackFrame) - lastFrame;
					System.out.println(NumNonBlack);
				}
			}
			
			//Check if the line contains the string 'fps.' it should be in the metadata
			else if(s.toLowerCase().contains("fps")) {
				//Read the FPS as a float, it will be a floating point number, 4-5 digits, and suffix of 'fps'
				videoFPS = Float.valueOf(s.substring(s.indexOf("fps")-5, s.indexOf("fps")));
			}
		}
		int lastStartBlackFrame = -1;

		int i = 0;
		for(int sample = blackFrames.get(i);i<blackFrames.size();i++) {
			if(sample != lastStartBlackFrame+1) {
				if(moduleSPS == videoFPS) {
					return Math.round(1000 / moduleSPS * (moduleSPS * DELAY_IN_SECONDS_BEFORE_LIGHT - i));
				}
				else {
					return Math.round(1000 / moduleSPS * (moduleSPS * DELAY_IN_SECONDS_BEFORE_LIGHT - (moduleSPS / videoFPS) * (i)));
				}
			}
			lastStartBlackFrame = sample;
		}
		
		/*for (Iterator<Integer> iterator = blackFrames.iterator(); iterator.hasNext();) {                              //iterates through the blackframes
                    int bf = iterator.next(); 
                    if (bf != lastStartBlackFrame+1){                                                                             //if it is not immediately after the last black frame
                        for(int i = lastStartBlackFrame+1; i<bf; i++){                                                            //iterates from the last seen black frame to the current one, aka through all of the non black frames
                            return                                                              //adds the frame to nBlackFrames 
                        }
                    }
                    firstStopBlackFrame = bf;
                }*/



		return 0;
	}

	/*
	 * Returns a String to be run as a command with the proper directory prefix, determined by os.name property and os.arch properties. 
	 */
	public String cmdWrapper(String videoName) {
		String CMD = "ffmpeg -i " + videoName + " -vf blackframe -f rawvideo -y NUL";
		String CMD1 = "ffmpeg -i " + videoName + " -to 00:00:03 -vf blackframe -f rawvideo -y NUL";                   //Command to be written into command line to run ffmpeg black frame on a certain video. Video location is written after "-i" and can be modified
		String CMD2 = "ffmpeg -ss 00:02:00 -i " + videoName + " -to 00:00:03 -vf blackframe -f rawvideo -y NUL";                   //Command to be written into command line to run ffmpeg black frame on a certain video. Video location is written after "-i" and can be modified

		FfmpegSystemWrapper SysWrap = new FfmpegSystemWrapper();
		SysWrap.setSystemInfo();

		return SysWrap.getBinRoot()+CMD1;
	}




	//Dan's uncommented mess
	public static void writeOutput(PrintWriter writer, List<Integer> nb, List<Integer> b)
	{
		int rows = 0;
		if(Integer.compare(b.size(), nb.size())>0){
			rows = nb.size();
			int lastRow = 0;
			for(int i = 0; i<rows; i++)
			{
				writer.println(Integer.toString(b.get(i))+","+Integer.toString(nb.get(i)));
				lastRow ++;
			}
			for(int j = lastRow; j<b.size();j++)
			{
				writer.println(Integer.toString(b.get(j))+", ");
			}
		}
		else
		{
			rows = b.size();
			int lastRow = 0;
			for(int i = 0; i<rows; i++)
			{
				writer.println(Integer.toString(b.get(i))+","+Integer.toString(nb.get(i)));
				lastRow ++;
			}
			for(int j = lastRow; j<nb.size();j++)
			{
				writer.println(" ,"+Integer.toString(nb.get(j)));
			}
		}

	}
}

