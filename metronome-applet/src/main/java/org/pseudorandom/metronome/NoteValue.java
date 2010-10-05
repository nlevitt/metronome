/**
 * 
 */
package org.pseudorandom.metronome;

public enum NoteValue { 
	WHOLE_NOTE(1, "whole note"),
	HALF_NOTE(2, "half note"), 
	QUARTER_NOTE(4, "quarter note"), 
	EIGHTH_NOTE(8, "eighth note"), 
	SIXTEENTH_NOTE(16, "sixteenth note"), 
	THIRTYSECOND_NOTE(32, "32nd note"), 
	SIXTYFOURTH_NOTE(64, "64th note"); 
	
	public String toString() {
		return prettyName;
	}
	
	protected int numericValue;
	protected String prettyName;

	// value is 1/length 
	NoteValue(int value, String prettyName) {
		this.numericValue = value;
		this.prettyName = prettyName;
	}
	
	public int divide(NoteValue b) {
		return b.numericValue / this.numericValue;
	}

	public int numericValue() {
		return numericValue;
	}
}