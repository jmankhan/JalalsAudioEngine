import java.util.ArrayList;
import java.util.Arrays;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.CompoundControl;
import javax.sound.sampled.Control;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.Port;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;

/**
 * This audio engine will be designed to import and playback mp3 files, allowing for as much control as possible over the sound
 * @author jmankhan
 * @version 1/27/2015 v0.1
 * Latest update: search and display all lines available to system
 */
public class JalalsAudioEngine {

	public JalalsAudioEngine() {
		
	}

	/**
	 * Searches through all mixers available for access and prints their name, description, and type
	 * @return ArrayList<Mixer.Info> of each Mixer available for use
	 */
	public ArrayList<Mixer.Info> showMixers() {
		ArrayList<Mixer.Info> mixerInfo = new ArrayList<Mixer.Info> (Arrays.asList(AudioSystem.getMixerInfo()));
		
		//create line objects to use for identification through comparison
		Line.Info sourceDataLineInfo = new Line.Info(SourceDataLine.class);
		Line.Info targetDataLineInfo = new Line.Info(TargetDataLine.class);
		Line.Info portInfo			 = new Line.Info(Port.class);
		
		StringBuilder display = new StringBuilder();
		
		//loop through all mixers that Java can find, and identify their type, name, and description
		for(Mixer.Info m : mixerInfo) {
			Mixer mixer = AudioSystem.getMixer(m);
			
			if(mixer.isLineSupported(sourceDataLineInfo))
				display.append("SourceDataLine: " + m.getName() + " - " + m.getDescription() + "\n");
			if(mixer.isLineSupported(targetDataLineInfo))
				display.append("TargetDataLine: " + m.getName() + " - " + m.getDescription() + "\n");
			if(mixer.isLineSupported(portInfo))
				display.append("Port: " + m.getName() + " - " + m.getDescription() + "\n");
			
		}
		System.out.println(display.toString());
		
		return mixerInfo;
	}
	
	/**
	 * Each port is actually a Port Mixer, which contains more Lines for access, so this method will 
	 * access and search through each available port and display the Lines available for use and their controls
	 */
	public void searchPorts() {
		ArrayList<Mixer.Info> mixerInfo = new ArrayList<Mixer.Info> (Arrays.asList(AudioSystem.getMixerInfo()));

		Line.Info portInfo			 = new Line.Info(Port.class);

		StringBuilder display = new StringBuilder();

		for(Mixer.Info m : mixerInfo) {
			Mixer mixer = AudioSystem.getMixer(m);
			
			//found a port mixer
			if(mixer.isLineSupported(portInfo)) {
				display.append(m.getName() + " - " + m.getDescription() + "\n");
				
				//should be multiple SourceDataLines in each port
				ArrayList<Line.Info> sourceInfos = new ArrayList<Line.Info> (Arrays.asList(mixer.getSourceLineInfo()));

				//search through each SourceDataLine in arraylist, identify it and its controls
				for(Line.Info srcInfo : sourceInfos) {
					Port.Info pi = (Port.Info) srcInfo;
					
					//should always evaluate to source, since we are in SourceDataLines
					display.append("\t" + pi.getName() + ", " + (pi.isSource()? "source \n" : "target \n"));
					
					try {
						Line inLine = mixer.getLine(srcInfo);
						showControls(inLine, display);
						inLine.close();
					} catch (LineUnavailableException e) {e.printStackTrace();}
				}
				
				//should be multiple TargetDataLines in each port
				ArrayList<Line.Info> targetInfos = new ArrayList<Line.Info> (Arrays.asList(mixer.getTargetLineInfo()));
				
				//search through eachTargetDataLine in arraylist, identify it and its controls
				for(Line.Info tgtInfo:targetInfos) {
					Port.Info pi = (Port.Info) tgtInfo;
					
					//should always evaluate to target since we are in TargetDataLines
					display.append("\t" + pi.getName() + ", " + (pi.isSource() ? "source \n" : "target \n"));
					
					try {
						Line inLine = mixer.getLine(tgtInfo);
						showControls(inLine, display);
						inLine.close();
					} catch(LineUnavailableException e) {e.printStackTrace();}
				}
			}
		}
		System.out.println(display.toString());
	}
	
	/**
	 * Searches through a Line and identifies all its controls, their values, and their range
	 * @param inLine Line to search
	 * @param display StringBuilder to save information to
	 * @throws LineUnavailableException if Line cannot be accessed
	 */
	public void showControls(Line inLine, StringBuilder display) throws LineUnavailableException {
		inLine.open();
		
		ArrayList<Control> controls = new ArrayList<Control> (Arrays.asList(inLine.getControls()));
		for(Control c : controls) {
			display.append("\t\t" + c.toString() + "\n");
			
			//controls can contain other controls, so check for that
			if(c instanceof CompoundControl) {
				CompoundControl cc = (CompoundControl) c;
				
				//yet another nested control... who came up with this?
				ArrayList<Control> iControls = new ArrayList<Control> (Arrays.asList(cc.getMemberControls()));

				//iterate through the other nested control 
				for(Control i : iControls) {
					display.append("\t\t\t" + i.toString() + "\n");
				}
			}
		}
	}
	
	public static void main(String args[]) {
		
		JalalsAudioEngine j = new JalalsAudioEngine();
		j.searchPorts();
	}
}
