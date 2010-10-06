package org.pseudorandom.metronome;

import java.lang.reflect.InvocationTargetException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import javax.swing.JApplet;
import javax.swing.SwingUtilities;



public class MetronomeApplet extends JApplet {

	private static final long serialVersionUID = 1L;

	protected MidiMetronome metronome;
	
	public MetronomeApplet() throws InvalidMidiDataException, MidiUnavailableException {
		metronome = new MidiMetronome();
	}

    @Override
	public void destroy() {
		super.destroy();
		metronome.stopMetronome();
	}

    @Override
	public void stop() {
		super.stop();
		metronome.stopMetronome();
    }

    public void init() {
    	try {
    		SwingUtilities.invokeAndWait(new Runnable() {
    			public void run() {
    				setParameters();

    				MetronomePanel panel = new MetronomePanel(metronome);
    				panel.setOpaque(true); 
    				setContentPane(panel);
    			}

    			protected void setParameters() {
    				if (getParameter("tempoBpm") != null) try {
    					metronome.setTempoBpm(Double.parseDouble(getParameter("tempoBpm")));
    				} catch (Exception e) {
    					e.printStackTrace();
    				}

    				if (getParameter("beatsPerMeasure") != null) try {
    					metronome.setBeatsPerMeasure(Integer.parseInt(getParameter("beatsPerMeasure")));
    				} catch (Exception e) {
    					e.printStackTrace();
    				}

    				if (getParameter("beatValue") != null) try {
    					metronome.setBeatValue(NoteValue.valueOf(getParameter("beatValue")));
    				} catch (Exception e) {
    					e.printStackTrace();
    				}

    				if (getParameter("tockValue") != null) try {
    					metronome.setTockValue(NoteValue.valueOf(getParameter("tockValue")));
    				} catch (Exception e) {
    					e.printStackTrace();
    				}

    				if (getParameter("emphasizeBeats") != null) {
    					String[] strBeats = getParameter("emphasizeBeats").split("[^0-9]+");
    					if (strBeats.length > 0) {
    						Integer[] beats = new Integer[strBeats.length];
    						for (int i = 0; i < strBeats.length; i++) {
    							beats[i] = Integer.parseInt(strBeats[i]);
    						}
    						try {
    							metronome.setEmphasizeBeats(beats);
    						} catch (Exception e) {
    							e.printStackTrace();
    						}
    					}
    				}
    			}
    		});
    	} catch (InterruptedException e) {
    		e.printStackTrace();
    	} catch (InvocationTargetException e) {
    		e.printStackTrace();
    	}
    }
    
	public static void main(String[] args) throws MidiUnavailableException, InvalidMidiDataException, InterruptedException {
		final int PLAY_MS = 20000;
		final int WAIT_MS = 3000;

		// MetronomeApplet metronome = new MetronomeApplet();
		MidiMetronome metronome = new MidiMetronome();

//		metronome.stop();
//		metronome.setTimeSignature(4, NoteLength.QUARTER_NOTE);
//		// metronome.setEmphasizeBeats(new int[] {1, 3});
//		metronome.setTockLength(NoteLength.QUARTER_NOTE);
//		metronome.setTempoBpm(90);
//		metronome.play();
//		Thread.sleep(PLAY_MS/2);
//		metronome.stop();
//		metronome.setTimeSignature(4, NoteLength.QUARTER_NOTE);
//		// metronome.setEmphasizeBeats(new int[] {1, 3});
//		metronome.setTockLength(NoteLength.SIXTEENTH_NOTE);
//		metronome.setTempoBpm(90);
//		metronome.play();
//		Thread.sleep(PLAY_MS/2);
//
//		// paidushko
//		metronome.stop(); Thread.sleep(WAIT_MS);
//		metronome.setTempoBpm(330);
//		metronome.setTimeSignature(5, NoteLength.EIGHTH_NOTE);
//		metronome.setEmphasizeBeats(new int[] {1, 3});
//		metronome.play();
//		Thread.sleep(PLAY_MS/2);
//
//		// ekremov
//		metronome.stop(); Thread.sleep(WAIT_MS);
//		metronome.setTempoBpm(322);
//		metronome.setTimeSignature(7, NoteLength.EIGHTH_NOTE);
//		metronome.setEmphasizeBeats(new int[] {1, 4, 6});
//		metronome.play();
//		Thread.sleep(PLAY_MS);
//		
//		// kopanica
//		// metronome.stop(); Thread.sleep(WAIT_MS);
//		metronome.setTempoBpm(472);
//		metronome.setTimeSignature(11, NoteLength.EIGHTH_NOTE);
//		metronome.setEmphasizeBeats(new int[] {1, 3, 5, 8, 10});
//		metronome.play();
//		Thread.sleep((long) (1.5*PLAY_MS));
		
		// leventikos
		// metronome.stop(); Thread.sleep(WAIT_MS);
//		metronome.setTempoBpm(440);
//		metronome.setTimeSignature(16, NoteLength.EIGHTH_NOTE);
//		metronome.setEmphasizeBeats(new int[] {1, 3, 5, 7, 10, 12, 14});
//		metronome.play();
//		Thread.sleep(2*PLAY_MS);

		// sandansko
//		metronome.stop(); Thread.sleep(WAIT_MS);
		metronome.setTempoBpm(320);
		metronome.setTimeSignature(22, NoteValue.EIGHTH_NOTE);
		metronome.setEmphasizeBeats(new Integer[] {1, 3, 5, 7, 10, 12, 14, 16, 19, 21});
		metronome.startMetronome();
		Thread.sleep(5000);
//
//		metronome.stop();
//		metronome.setTimeSignature(11, NoteLength.EIGHTH_NOTE);
//		metronome.setEmphasizeBeats(new int[] {1, 3, 5, 8, 10});
//		metronome.setBpm(400);
//
//		Thread.sleep(2000);
//		metronome.play();
//
//		Thread.sleep(5000);
//
//		metronome.setTimeSignature(4, NoteLength.QUARTER_NOTE);
//		metronome.setTockLength(NoteLength.QUARTER_NOTE);
//		metronome.setBpm(120);
//
//		Thread.sleep(5000);
//
//		metronome.setTimeSignature(4, NoteLength.QUARTER_NOTE);
//		metronome.setTockLength(NoteLength.EIGHTH_NOTE);
//		metronome.setBpm(120);
//
//		Thread.sleep(5000);
//
//		metronome.setTimeSignature(4, NoteLength.QUARTER_NOTE);
//		metronome.setTockLength(NoteLength.SIXTEENTH_NOTE);
//		metronome.setBpm(120);
//
//		Thread.sleep(5000);
//
//		metronome.setTimeSignature(2, NoteLength.QUARTER_NOTE);
//		metronome.setTockLength(NoteLength.EIGHTH_NOTE);
//		metronome.setBpm(120);
//
//		Thread.sleep(5000);
//
//		metronome.setTimeSignature(2, NoteLength.HALF_NOTE);
//		metronome.setTockLength(NoteLength.EIGHTH_NOTE);
//		metronome.setBpm(120);
//
//		Thread.sleep(5000);
//
//		metronome.setTimeSignature(2, NoteLength.HALF_NOTE);
//		metronome.setTockLength(NoteLength.HALF_NOTE);
//		metronome.setBpm(60);
//
//		Thread.sleep(5000);
//
//		metronome.setTimeSignature(2, NoteLength.HALF_NOTE);
//		metronome.setTockLength(NoteLength.QUARTER_NOTE);
//		metronome.setBpm(60);
//
//		Thread.sleep(5000);
//
//		metronome.setTimeSignature(2, NoteLength.HALF_NOTE);
//		metronome.setTockLength(NoteLength.EIGHTH_NOTE);
//		metronome.setBpm(60);
//
//		Thread.sleep(5000);
//
//		metronome.setTimeSignature(3, NoteLength.QUARTER_NOTE);
//		metronome.setTockLength(NoteLength.EIGHTH_NOTE);
//		metronome.setBpm(60);


		// metronome.stop();
	}
}
