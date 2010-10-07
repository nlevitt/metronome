package org.pseudorandom.metronome;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;

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

    				MetronomePanel panel = new MetronomePanel(MetronomeApplet.this, metronome);
    				panel.setOpaque(true); 
    				setContentPane(panel);
    			}
    		});
    	} catch (InterruptedException e) {
    		e.printStackTrace();
    	} catch (InvocationTargetException e) {
    		e.printStackTrace();
    	}
    }

    protected void setParameters() {
    	if (getParameter("tempoBpm") != null && !"".equals(getParameter("tempoBpm"))) try {
    		metronome.setTempoBpm(Double.parseDouble(getParameter("tempoBpm")));
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

    	if (getParameter("beatsPerMeasure") != null && !"".equals(getParameter("beatsPerMeasure"))) try {
    		metronome.setBeatsPerMeasure(Integer.parseInt(getParameter("beatsPerMeasure")));
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

    	if (getParameter("beatValue") != null && !"".equals(getParameter("beatValue"))) try {
    		metronome.setBeatValue(NoteValue.valueOf(getParameter("beatValue")));
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

    	if (getParameter("tockValue") != null && !"".equals(getParameter("tockValue"))) try {
    		metronome.setTockValue(NoteValue.valueOf(getParameter("tockValue")));
    	} catch (Exception e) {
    		e.printStackTrace();
    	}

    	if (getParameter("emphasizeBeats") != null && !"".equals(getParameter("emphasizeBeats"))) {
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

    protected static String join(Object[] arr, String delimiter) {
    	if (arr == null || arr.length == 0) {
    		return "";
    	}
    	
    	StringBuilder buf = new StringBuilder();
    	for (int i = 0; i < arr.length - 1; i++) {
    		buf.append(arr[i]);
    		buf.append(delimiter);
    	}
    	buf.append(arr[arr.length-1]);
    	
    	return buf.toString();
    }

	public void openLink() {
		StringBuilder queryBuf = new StringBuilder();
		queryBuf.append("?bpm=" + metronome.getTempoBpm());
		queryBuf.append("&beats=" + metronome.getBeatsPerMeasure());
		queryBuf.append("&beat=" + metronome.getBeatValue().numericValue());
		queryBuf.append("&tock=" + metronome.getTockValue().numericValue());
		if (metronome.getEmphasizeBeats() != null) {
			queryBuf.append("&emp=" + join(metronome.getEmphasizeBeats(), ","));
		}
		
		try {
			URL linkUrl = new URL(getDocumentBase(), queryBuf.toString());
			getAppletContext().showDocument(linkUrl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
}
