package org.pseudorandom.metronome;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.pseudorandom.metronome.MidiMetronome.TickListener;

class MetronomePanel extends JPanel implements TickListener {
	protected static final int MAX_BEATS_PER_MEASURE = 25;

	private static final long serialVersionUID = 1L;
	
	protected MidiMetronome nome;
	protected MidiMetronome metronome;
	
	private JToggleButton onOffButton;
	private JSpinner tempoSpinner;
	private JLabel timesigTopLabel;
	private JLabel timesigBottomLabel;
	private JRadioButton tockValueRadioButton;
	private JComboBox tockValueCombo;
	private JComboBox beatValueCombo;
	private JSpinner beatsPerMeasureSpinner;
	private JLabel beatsPerMeasureLabel;
	private DefaultComboBoxModel beatValueComboModel;
	private DefaultComboBoxModel tockValueComboModel;
	private JRadioButton emphasizeBeatsRadioButton;
	private JLabel link;
	private JCheckBox[] emphasizeBeatsCheckboxes;
	private JButton tapButton;
	private NumberEditor tempoSpinnerEditor;
	private JRadioButton[] beatsRadios;

	private MetronomeApplet applet;

	protected MetronomePanel(MetronomeApplet applet, MidiMetronome nome) {
		super();

		this.applet = applet;
		this.metronome = nome;
		
		nome.setTickListener(this);
		
		initGUI();

		onOffButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				toggleMetronome(((JToggleButton) event.getSource()).isSelected());
			}
		});
		
		tempoSpinner.setValue(metronome.getTempoBpm());
		tempoSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setTempoBpm((Number) ((JSpinner) e.getSource()).getValue());
			}
		});
		
		beatValueCombo.setSelectedItem(metronome.getBeatValue());
		beatValueCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				setBeatValue((NoteValue) ((JComboBox) e.getSource()).getSelectedItem());
			}
		});
		
		beatsPerMeasureSpinner.setValue(metronome.getBeatsPerMeasure());
		beatsPerMeasureSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setBeatsPerMeasure((Number) ((JSpinner) e.getSource()).getValue());
			}
		});

		tockValueCombo.setSelectedItem(metronome.getTockValue());
		tockValueCombo.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				if (tockValueRadioButton.isSelected()) {
					setTockValue(((JComboBox) e.getSource()).getSelectedItem());
				}
			}
		});
		
		ButtonGroup g = new ButtonGroup();
		g.add(tockValueRadioButton);
		g.add(emphasizeBeatsRadioButton);
		if (metronome.getEmphasizeBeats() != null) {
			emphasizeBeatsRadioButton.setSelected(true);
		} else {
			tockValueRadioButton.setSelected(true);
		}
		
		emphasizeBeatsRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableEmphasizeBeats();
			}
		});
		
		tockValueRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enableTockValue();
			}
		});
		
		updateEmphasizeBeatsCheckBoxes();
		for (int i = 0; i < emphasizeBeatsCheckboxes.length; i++) {
			emphasizeBeatsCheckboxes[i].addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					setEmphasizeBeatsFromCheckboxes();
				}
			});
		}
		
		link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		link.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				MetronomePanel.this.applet.openLink();
			}
		});
		
		tapButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				handleFindTempoClick(e.getWhen());
			}
		});
	}

	private int tapCount = -1;
	private long firstTap = -1l;
	private Timer tapFinishTimer;

	protected void handleFindTempoClick(long when) {
		System.err.println("SwingUtilities.isEventDispatchThread()=" + SwingUtilities.isEventDispatchThread());
		if (tapCount < 0 || firstTap < 0) {
			tapCount = 0;
			firstTap = when;
		} else {
			tapCount++;
			tempoSpinner.setValue(calcTempoBpm(when));
	        tempoSpinnerEditor.getTextField().setForeground(Color.WHITE);
	        tempoSpinnerEditor.getTextField().setBackground(Color.BLUE);
		}
		
		// set a timer to stop waiting for clicks
		if (tapFinishTimer != null) {
			tapFinishTimer.cancel();
		}
		tapFinishTimer = new Timer();
		tapFinishTimer.schedule(new TimerTask() {
			public void run() {
				tapCount = -1;
				firstTap = -1;
		        tempoSpinnerEditor.getTextField().setForeground(Color.BLACK);
		        tempoSpinnerEditor.getTextField().setBackground(Color.WHITE);
			}
		},
		1600);
	}
	
	protected double calcTempoBpm(long lastTap) {
		double beats = tapCount * ((double) metronome.getBeatsPerMeasure() / metronome.getTapsPerMeasure());
		double minutes = (lastTap - firstTap) / 60000.0; 
		double bpm = beats / minutes;
		// System.out.println(new Date() + " " + clickCount + " clicks in " + minutes + " minutes comes to " + bpm + " bpm");
		return bpm;
	}

	protected void setEmphasizeBeatsFromCheckboxes() {
		try {
			LinkedList<Integer> l = new LinkedList<Integer>();
			for (int i = 0; i < emphasizeBeatsCheckboxes.length && i < metronome.getBeatsPerMeasure(); i++) {
				if (emphasizeBeatsCheckboxes[i].isSelected()) {
					l.add(i+1);
				}
			}
			metronome.setEmphasizeBeats(l.toArray(new Integer[0]));
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	protected void updateEmphasizeBeatsCheckBoxes() {
		// always put an emphasis on the 1
		emphasizeBeatsCheckboxes[0].setSelected(true);
		emphasizeBeatsCheckboxes[0].setEnabled(false);

		for (int i = 1; i < emphasizeBeatsCheckboxes.length; i++) {
			emphasizeBeatsCheckboxes[i].setEnabled(emphasizeBeatsRadioButton.isSelected() && i < metronome.getBeatsPerMeasure());
			// System.out.println("should checkbox[" + i + "] be visible? is " + i + "<" + metronome.getBeatsPerMeasure() + " ? " + (i < metronome.getBeatsPerMeasure())); 
			emphasizeBeatsCheckboxes[i].setVisible(i < metronome.getBeatsPerMeasure());
			beatsRadios[i].setVisible(i < metronome.getBeatsPerMeasure());
			
			if (metronome.getEmphasizeBeats() != null) {
				emphasizeBeatsCheckboxes[i].setSelected(MidiMetronome.contains(metronome.getEmphasizeBeats(), i+1));
			} // else don't touch value that's there
		}
	}

	protected void enableTockValue() {
		tockValueCombo.setEnabled(true);
		setTockValue(tockValueCombo.getSelectedItem());
		updateEmphasizeBeatsCheckBoxes();
	}

	protected void enableEmphasizeBeats() {
		tockValueCombo.setEnabled(false);
		updateEmphasizeBeatsCheckBoxes();
		setEmphasizeBeatsFromCheckboxes();
	}
	
	protected void setTockValue(Object object) {
		try {
			if (object instanceof NoteValue) {
				metronome.setTockValue((NoteValue) object);
			} else {
				metronome.setTockValue(metronome.getBeatValue());
			}
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	protected void setBeatsPerMeasure(Number value) {
		try {
			metronome.setBeatsPerMeasure(value);
			timesigTopLabel.setText(value.toString());
			updateEmphasizeBeatsCheckBoxes();
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	protected void setBeatValue(NoteValue selectedItem) {
		try {
			metronome.setBeatValue(selectedItem);
			timesigBottomLabel.setText("" + selectedItem.numericValue());
			updateTockValueChoices(selectedItem);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	protected void updateTockValueChoices(NoteValue selectedItem) {
		Object previouslySelected = tockValueComboModel.getSelectedItem();
		tockValueComboModel.removeAllElements();
		String blankItem = "";
		tockValueComboModel.addElement(blankItem);
		tockValueComboModel.setSelectedItem(blankItem);
		for (NoteValue n: NoteValue.values()) {
			if (n.numericValue() > selectedItem.numericValue()) {
				tockValueComboModel.addElement(n);
				if (n.equals(previouslySelected)) {
					tockValueComboModel.setSelectedItem(n);
				}
			}
		}
	}

	protected void toggleMetronome(boolean on) {
		try {
			if (on) {
				if (metronome != null) {
					metronome.startMetronome();
				}
			} else {
				if (metronome != null) {
					metronome.stopMetronome();
				}
			}
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}

	public void setMetronome(MidiMetronome metronome) {
		this.metronome = metronome;
		tempoSpinner.setValue(metronome.getTempoBpm());
	}

	protected void setTempoBpm(Number bpm) {
		try {
			metronome.setTempoBpm(bpm.doubleValue());
		} catch (InvalidMidiDataException e1) {
			e1.printStackTrace();
		}
	}	

	protected void initGUI() {
		try {
			GridBagLayout layout = new GridBagLayout();
			this.setLayout(layout);

			layout.rowWeights = new double[] {0.0, 0, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0, 0.1, 0.0};
			layout.rowHeights = new int[] {15, 15, 15, 15, 25, 11, 25, 10, 5, 15, 13, 25, 13, 25, 14, 12, 7, 25};
			layout.columnWeights = new double[] {0.1, 0.0, 0.0, 0.0, 0.1};
			layout.columnWidths = new int[] {7, 10, 53, 10, 7};

			int gridx = 1;
			
			JLabel title = new JLabel("The Confusing Metronome.");

			// set our preferred font based on default e.g. 12pt becomes 14pt
			Font defaultFont = title.getFont();
			Font ourPlainFont = defaultFont.deriveFont(defaultFont.getSize() * 7f / 6f);  
			Font bigFont = ourPlainFont.deriveFont(ourPlainFont.getSize() * 18f / 14f);

			title.setFont(bigFont);
			this.add(title, new GridBagConstraints(0, gridx, 5, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			new GridBagConstraints();

			gridx += 2;
			JLabel tempoLabel = new JLabel("Tempo:");
			tempoLabel.setFont(ourPlainFont);
			this.add(tempoLabel, new GridBagConstraints(0, gridx, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			SpinnerNumberModel tempoSpinnerModel = new SpinnerNumberModel(100, 1, 999, 1);
			tempoSpinner = new JSpinner(tempoSpinnerModel);
			tempoSpinner.setFont(ourPlainFont);
			tempoSpinnerEditor = new JSpinner.NumberEditor(tempoSpinner, "0.0");
			tempoSpinner.setEditor(tempoSpinnerEditor);
			this.add(tempoSpinner, new GridBagConstraints(2, gridx, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			JLabel tempoBpmLabel = new JLabel("BPM");
			tempoBpmLabel.setFont(ourPlainFont);
			this.add(tempoBpmLabel, new GridBagConstraints(4, gridx, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			
			gridx += 2;
			JLabel tapLabel = new JLabel("Find tempo:");
			tapLabel.setFont(ourPlainFont);
			this.add(tapLabel, new GridBagConstraints(0, gridx, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			tapButton = new JButton("tap beat");
			tapButton.setFont(ourPlainFont);
			this.add(tapButton, new GridBagConstraints(2, gridx, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			
			gridx += 2;
			JLabel timesigLabel = new JLabel("Time signature:");
			timesigLabel.setFont(ourPlainFont);
			this.add(timesigLabel, new GridBagConstraints(0, gridx, 3, 2, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			
			timesigBottomLabel = new JLabel("" + metronome.getBeatValue().numericValue());
			timesigBottomLabel.setFont(bigFont);
			timesigTopLabel = new JLabel(""+ metronome.getBeatsPerMeasure());
			timesigTopLabel.setFont(bigFont);
			this.add(timesigTopLabel);
			this.add(timesigBottomLabel);
			this.add(timesigTopLabel, new GridBagConstraints(4, gridx, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			this.add(timesigBottomLabel, new GridBagConstraints(4, gridx+1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(-6, 0, 0, 0), 0, 0));

			gridx += 2;
			beatsPerMeasureLabel = new JLabel("Beats per measure:");
			beatsPerMeasureLabel.setFont(ourPlainFont);
			this.add(beatsPerMeasureLabel, new GridBagConstraints(0, gridx, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			SpinnerNumberModel beatsPerMeasureSpinnerModel = new SpinnerNumberModel(metronome.getBeatsPerMeasure(), 1, MAX_BEATS_PER_MEASURE, 1);
			beatsPerMeasureSpinner = new JSpinner(beatsPerMeasureSpinnerModel);
			beatsPerMeasureSpinner.setFont(ourPlainFont);
			this.add(beatsPerMeasureSpinner, new GridBagConstraints(2, gridx, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			
			gridx += 1;
			JLabel beatValueLabel = new JLabel("Beat value:");
			this.add(beatValueLabel, new GridBagConstraints(0, gridx, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			beatValueLabel.setFont(ourPlainFont);
			beatValueComboModel = new DefaultComboBoxModel(NoteValue.values());
			beatValueCombo = new JComboBox(beatValueComboModel);
			beatValueCombo.setFont(ourPlainFont);
			this.add(beatValueCombo, new GridBagConstraints(2, gridx, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

			gridx += 2;
			tockValueRadioButton = new JRadioButton("Ghost beats:");
			this.add(tockValueRadioButton, new GridBagConstraints(0, gridx, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			tockValueRadioButton.setFont(ourPlainFont);
			tockValueComboModel = new DefaultComboBoxModel();
			updateTockValueChoices(metronome.getBeatValue());
			tockValueCombo = new JComboBox();
			tockValueCombo.setFont(ourPlainFont);
			tockValueCombo.setModel(tockValueComboModel);
			this.add(tockValueCombo, new GridBagConstraints(2, gridx, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

			gridx += 2;
			emphasizeBeatsRadioButton = new JRadioButton("Accent beats:");
			this.add(emphasizeBeatsRadioButton, new GridBagConstraints(0, gridx, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			emphasizeBeatsRadioButton.setFont(ourPlainFont);
			JPanel emphasizeBeatsPanel = new JPanel();
			FlowLayout emphasizeBeatsPanelLayout = new FlowLayout();
			emphasizeBeatsPanelLayout.setHgap(0);
			emphasizeBeatsPanel.setLayout(emphasizeBeatsPanelLayout);
			this.add(emphasizeBeatsPanel, new GridBagConstraints(0, gridx+1, 5, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			emphasizeBeatsCheckboxes = new JCheckBox[MAX_BEATS_PER_MEASURE];
			for (int i = 0; i < emphasizeBeatsCheckboxes.length; i++) {
				emphasizeBeatsCheckboxes[i] = new JCheckBox();
				emphasizeBeatsPanel.add(emphasizeBeatsCheckboxes[i]);
			}

			JPanel panel = new JPanel();
			FlowLayout flow = new FlowLayout();
			flow.setHgap(0);
			panel.setLayout(flow);
			this.add(panel, new GridBagConstraints(0, gridx+2, 5, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			ButtonGroup g = new ButtonGroup();
			beatsRadios = new JRadioButton[MAX_BEATS_PER_MEASURE];
			for (int i = 0; i < beatsRadios.length; i++) {
				beatsRadios[i] = new JRadioButton();
				g.add(beatsRadios[i]);
				panel.add(beatsRadios[i]);
				// beatsRadios[i].setEnabled(false);
			}

			gridx += 3;
			onOffButton = new JToggleButton();
			onOffButton.setText("start/stop");
			onOffButton.setFont(ourPlainFont);
			this.add(onOffButton, new GridBagConstraints(0, gridx, 5, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

			gridx++;
			link = new JLabel("<html><body><u>link to this metronome setting</u></body></html>");
			link.setForeground(Color.BLUE);
			link.setFont(ourPlainFont);
			this.add(link, new GridBagConstraints(0, gridx, 5, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	// called by MidiMetronome on tick
	public void tock(final long tock) {
		// 	System.err.println(getClass().getName() + ".tick() SwingUtilities.isEventDispatchThread()=" + SwingUtilities.isEventDispatchThread());
		
		final int tocksPerBeat = metronome.getTocksPerBeat();
		if (metronome.getEmphasizeBeats() != null) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// System.err.println(getClass().getName() + ".run() SwingUtilities.isEventDispatchThread()=" + SwingUtilities.isEventDispatchThread() + " beatsRadios[" + (int) (tock-1) + "]=" + beatsRadios[(int) (tock-1)]);
					beatsRadios[(int) (tock-1)].setSelected(true);
				}
			});
		} else if ((tock-1) % tocksPerBeat == 0) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					// System.err.println(getClass().getName() + ".run() SwingUtilities.isEventDispatchThread()=" + SwingUtilities.isEventDispatchThread() + " beatsRadios[" + (int) (tock-1) / tocksPerBeat + "]=" + beatsRadios[(int) (tock-1)]);
					beatsRadios[(int) (tock-1) / tocksPerBeat].setSelected(true);
				}
			});
		}
	}
}
