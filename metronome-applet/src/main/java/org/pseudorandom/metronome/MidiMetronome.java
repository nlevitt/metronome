package org.pseudorandom.metronome;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;

public class MidiMetronome {

	protected static final int MIDI_PERCUSSION_CHANNEL = 9; // "10" zero-based
	protected static final int ON_THE_ONE_VELOCITY = 120;
	protected static final int TICK_VELOCITY = 80;
	protected static final int TOCK_VELOCITY = 40;
	protected static final int TICK_MIDI_KEY = 77;
	protected static final int TOCK_MIDI_KEY = 77;
//  protected static final int TICK_MIDI_KEY = 61;
//  protected static final int TOCK_MIDI_KEY = 61;
//	protected static final int TICK_MIDI_KEY = 80;
//	protected static final int TOCK_MIDI_KEY = 80;

	protected double tempoBpm = 88;
	protected int beatsPerMeasure = 4;
	protected NoteValue beatValue = NoteValue.QUARTER_NOTE;
	protected NoteValue tockValue = NoteValue.SIXTEENTH_NOTE;
	protected Integer[] emphasizeBeats = null;
	
	protected Sequencer sequencer;
	protected Synthesizer synth;

	protected Timer restartTimer; // to delay restart

	public MidiMetronome() {
		try {
			sequencer = MidiSystem.getSequencer();
			synth = MidiSystem.getSynthesizer();
			sequencer.open();
			synth.open();

			sequencer.getTransmitter().setReceiver(synth.getReceiver());

			sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
			sequencer.setTempoInBPM((float) tempoBpm);
		} catch (MidiUnavailableException e) {
			throw new RuntimeException(e);
		}
	}

	public String getTimesigString() {
		return beatsPerMeasure + "/" + beatValue.numericValue;
	}
	
	protected void setTimeSignature(int beatsPerMeasure, NoteValue beatValue) throws InvalidMidiDataException {
		this.beatsPerMeasure = beatsPerMeasure;
		this.beatValue = beatValue;
		maybeRestart();
	}

	protected Sequence makeSequence() throws InvalidMidiDataException {
		if (emphasizeBeats != null) {
			return makeBalkanSequence();
		} else {
			return makeNormalSequance();
		}
	}

	protected Sequence makeNormalSequance() throws InvalidMidiDataException {
		Sequence sequence;
		int tocksPerTick = (int) beatValue.divide(tockValue);
		
		sequence  = new Sequence(Sequence.PPQ, (int) beatValue.divide(tockValue));
		Track track = sequence.createTrack();
		
		// first beat of measure
		track.add(makePercussionEvent(ShortMessage.NOTE_ON, TICK_MIDI_KEY, ON_THE_ONE_VELOCITY, 0));
		
		for (int tock = 2; tock <= tocksPerTick * beatsPerMeasure; tock++) {
			if ((tock-1) % tocksPerTick == 0) {
				track.add(makePercussionEvent(ShortMessage.NOTE_ON, TICK_MIDI_KEY, TICK_VELOCITY, tock - 1));
			} else {
				track.add(makePercussionEvent(ShortMessage.NOTE_ON, TOCK_MIDI_KEY, TOCK_VELOCITY, tock - 1));
			}
		}

		track.add(makePercussionEvent(ShortMessage.NOTE_OFF, 0, 0, beatsPerMeasure * tocksPerTick));
		
		return sequence;
	}

	protected Sequence makeBalkanSequence() throws InvalidMidiDataException {
		Sequence sequence;
		sequence  = new Sequence(Sequence.PPQ, 1);
		Track track = sequence.createTrack();

		// first beat of measure
		track.add(makePercussionEvent(ShortMessage.NOTE_ON, TICK_MIDI_KEY, ON_THE_ONE_VELOCITY, 0));
		for (int beat = 2; beat <= beatsPerMeasure; beat++) {
			if (contains(emphasizeBeats, beat)) {
				track.add(makePercussionEvent(ShortMessage.NOTE_ON, TICK_MIDI_KEY, TICK_VELOCITY, beat - 1));
			} else {
				track.add(makePercussionEvent(ShortMessage.NOTE_ON, TOCK_MIDI_KEY, TOCK_VELOCITY, beat - 1));
			}
		}
		
		track.add(makePercussionEvent(ShortMessage.NOTE_OFF, 0, 0, beatsPerMeasure));
		return sequence;
	}
	
	public static boolean contains(Integer[] arr, int n) {
		for (int i = 0; i < arr.length; i++) {
			if (arr[i].intValue() == n) {
				return true;
			}
		}
		
		return false;
	}

	public double getTempoBpm() {
		return tempoBpm;
	}

	synchronized protected void startMetronome() throws InvalidMidiDataException {
		if (!sequencer.isRunning()) {
			sequencer.setSequence(makeSequence());
			sequencer.setTempoInBPM((float) tempoBpm);
			System.out.println(getTimesigString() + " at " + tempoBpm + "bpm with tockValue=" + tockValue + " emphasizeBeats=" + Arrays.toString(emphasizeBeats));
			sequencer.start();
		}
	}
	
	protected void stopMetronome() {
		if (sequencer.isRunning()) {
			sequencer.stop();
		}
	}

	protected MidiEvent makePercussionEvent(int command, int key, int velocity, long tick) throws InvalidMidiDataException {
		ShortMessage message = new ShortMessage();
		message.setMessage(command, MIDI_PERCUSSION_CHANNEL, key, velocity);
		MidiEvent event = new MidiEvent(message, tick);
		return event;
	}

	public void setTempoBpm(double bpm) throws InvalidMidiDataException {
		this.tempoBpm = bpm;
		maybeRestart();

		if (sequencer.isRunning()) {
			System.out.println(getTimesigString() + " at " + bpm + "bpm with tockValue=" + tockValue + " emphasizeBeats=" + Arrays.toString(emphasizeBeats));
		}
	}

	protected void setEmphasizeBeats(Integer[] integers) throws InvalidMidiDataException {
		this.emphasizeBeats = integers;
		maybeRestart();
	}

	protected void maybeRestart() throws InvalidMidiDataException {
		if (sequencer.isRunning()) {
			stopMetronome();
			
			if (restartTimer != null) {
				restartTimer.cancel();
			}
			restartTimer = new Timer();
			restartTimer.schedule(new TimerTask() {
				public void run() {
					try {
						startMetronome();
					} catch (InvalidMidiDataException e) {
						e.printStackTrace();
					}
				}
			},
			600);
		}
	}
	
	protected void setTockValue(NoteValue tockValue) throws InvalidMidiDataException {
		this.emphasizeBeats = null;
		this.tockValue = tockValue;
		maybeRestart();
	}

	public NoteValue getBeatValue() {
		return beatValue;
	}

	public void setBeatValue(NoteValue beatValue) throws InvalidMidiDataException {
		setTimeSignature(beatsPerMeasure, beatValue);
	}

	public void setBeatsPerMeasure(Number value) throws InvalidMidiDataException {
		setTimeSignature(value.intValue(), beatValue);
	}

	public int getBeatsPerMeasure() {
		return beatsPerMeasure;
	}

	public NoteValue getTockValue() {
		return tockValue;
	}

	public Integer[] getEmphasizeBeats() {
		return emphasizeBeats;
	}

	public int getClicksPerMeasure() {
		if (emphasizeBeats != null) {
			int count = 1;
			for (int n: emphasizeBeats) {
				if (n > 1 && n <= beatsPerMeasure) {
					count++;
				}
			}
			return count;
		} else {
			return beatsPerMeasure;
		}
	}
	
}
