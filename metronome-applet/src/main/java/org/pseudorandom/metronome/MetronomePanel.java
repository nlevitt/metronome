package org.pseudorandom.metronome;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.LinkedList;

import javax.sound.midi.InvalidMidiDataException;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;



/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
class MetronomePanel extends JPanel {
	protected static final int MAX_BEATS_PER_MEASURE = 32;

	private static final long serialVersionUID = 1L;
	
	private JToggleButton onOffButton;
	private JSpinner tempoSpinner;
	private JLabel beatValueLabel;
	private JPanel jPanel1;
	private JLabel timesigTopLabel;
	private JLabel timesigBottomLabel;
	private JLabel jLabel4;
	private JLabel title;
	private JRadioButton emphasizeBeatsRadioButton;
	private JComboBox tockValueCombo;
	private JRadioButton tockValueRadioButton;
	private JComboBox beatValueCombo;
	private JSpinner beatsPerMeasureSpinner;
	private JLabel beatsPerMeasureLabel;
	private JLabel jLabel2;
	private DefaultComboBoxModel beatValueComboModel;
	private SpinnerNumberModel beatsPerMeasureSpinnerModel;
	private DefaultComboBoxModel tockValueComboModel;
	private JCheckBox[] emphasizeBeatsCheckboxes;

	protected MidiMetronome nome;
	protected MidiMetronome metronome;
	protected SpinnerNumberModel tempoSpinnerModel;
	
	protected MetronomePanel(MidiMetronome nome) {
		super();

		metronome = nome;
		
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
			emphasizeBeatsCheckboxes[i].setVisible(i < metronome.getBeatsPerMeasure());
			
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
		tempoSpinnerModel.setValue(metronome.getTempoBpm());
	}

	protected void setTempoBpm(Number bpm) {
		try {
			metronome.setTempoBpm(bpm.doubleValue());
		} catch (InvalidMidiDataException e1) {
			e1.printStackTrace();
		}
	}	
	
	private void initGUI() {
		try {
			{
				GridBagLayout thisLayout = new GridBagLayout();
				this.setLayout(thisLayout);
				this.setPreferredSize(new java.awt.Dimension(667, 618));
				JLabel jLabel1;
				{
					jLabel1 = new JLabel();
					jLabel1.setText("Tempo:");
				}
				{
					tempoSpinnerModel = new SpinnerNumberModel(100, 1, 999, 1);
					tempoSpinner = new JSpinner(tempoSpinnerModel);
				}
				{
					onOffButton = new JToggleButton();
					onOffButton.setText("metronome start/stop");
				}
				{
					beatValueComboModel = 
						new DefaultComboBoxModel(NoteValue.values());
					beatValueCombo = new JComboBox(beatValueComboModel);
				}
				{
					beatsPerMeasureSpinnerModel = new SpinnerNumberModel(metronome.getBeatsPerMeasure(), 1, MAX_BEATS_PER_MEASURE, 1);
					beatsPerMeasureSpinner = new JSpinner();
					beatsPerMeasureSpinner.setModel(beatsPerMeasureSpinnerModel);
				}
				{
					beatsPerMeasureLabel = new JLabel();
					this.add(beatsPerMeasureLabel, new GridBagConstraints(0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
					this.add(beatsPerMeasureSpinner, new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					beatsPerMeasureSpinner.setFont(new java.awt.Font("Dialog",0,14));
					this.add(beatValueCombo, new GridBagConstraints(2, 8, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
					beatValueCombo.setFont(new java.awt.Font("Dialog",0,14));
					this.add(jLabel1, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
					jLabel1.setFont(new java.awt.Font("Dialog",0,14));
					this.add(tempoSpinner, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					tempoSpinner.setFont(new java.awt.Font("Dialog",0,14));
					this.add(onOffButton, new GridBagConstraints(0, 14, 5, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
					onOffButton.setFont(new java.awt.Font("Dialog",0,14));
					{
						jLabel2 = new JLabel();
						this.add(jLabel2, new GridBagConstraints(4, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						FlowLayout jLabel2Layout = new FlowLayout();
						jLabel2.setLayout(jLabel2Layout);
						jLabel2.setText("BPM");
						jLabel2.setFont(new java.awt.Font("Dialog",0,14));
					}
					{
						beatValueLabel = new JLabel();
						this.add(beatValueLabel, new GridBagConstraints(0, 8, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						beatValueLabel.setText("Beat value:");
						beatValueLabel.setFont(new java.awt.Font("Dialog",0,14));
					}
					{
						tockValueRadioButton = new JRadioButton();
						this.add(tockValueRadioButton, new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						tockValueRadioButton.setText("Ghost beats:");
						tockValueRadioButton.setFont(new java.awt.Font("Dialog",0,14));
					}
					{
						tockValueComboModel = 
							new DefaultComboBoxModel();
						updateTockValueChoices(metronome.getBeatValue());
						tockValueCombo = new JComboBox();
						tockValueCombo.setModel(tockValueComboModel);
						this.add(tockValueCombo, new GridBagConstraints(2, 10, 3, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						tockValueCombo.setFont(new java.awt.Font("Dialog",0,14));
					}
					{
						emphasizeBeatsRadioButton = new JRadioButton();
						this.add(emphasizeBeatsRadioButton, new GridBagConstraints(0, 12, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						emphasizeBeatsRadioButton.setText("Emphasize Beats:");
						emphasizeBeatsRadioButton.setFont(new java.awt.Font("Dialog",0,14));
					}
					{
						title = new JLabel();
						this.add(title, new GridBagConstraints(0, 1, 5, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						title.setText("The Confusing Metronome.");
						title.setFont(new java.awt.Font("Dialog",1,18));
					}
					{
						jLabel4 = new JLabel();
						this.add(jLabel4, new GridBagConstraints(0, 5, 3, 2, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						jLabel4.setText("Time signature:");
						jLabel4.setFont(new java.awt.Font("Dialog",1,14));
					}
					{
						timesigBottomLabel = new JLabel();
						this.add(timesigBottomLabel, new GridBagConstraints(4, 6, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(-6, 0, 0, 0), 0, 0));
						timesigBottomLabel.setText("" + metronome.getBeatValue().numericValue());
						timesigBottomLabel.setFont(new java.awt.Font("Dialog",1,18));
						// timesigBottomLabel.setForeground(new java.awt.Color(0,159,25));
					}
					{
						timesigTopLabel = new JLabel();
						this.add(timesigTopLabel, new GridBagConstraints(4, 5, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						timesigTopLabel.setText(""+ metronome.getBeatsPerMeasure());
						timesigTopLabel.setFont(new java.awt.Font("Dialog",1,18));
						// timesigTopLabel.setForeground(new java.awt.Color(0,159,25));
					}
					{
						jPanel1 = new JPanel();
						FlowLayout jPanel1Layout = new FlowLayout();
						jPanel1Layout.setHgap(0);
						jPanel1.setLayout(jPanel1Layout);
						this.add(jPanel1, new GridBagConstraints(0, 13, 5, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
						{
							emphasizeBeatsCheckboxes = new JCheckBox[MAX_BEATS_PER_MEASURE];
							for (int i = 0; i < emphasizeBeatsCheckboxes.length; i++) {
								emphasizeBeatsCheckboxes[i] = new JCheckBox();
								jPanel1.add(emphasizeBeatsCheckboxes[i]);
							}
						}

					}
					beatsPerMeasureLabel.setText("Beats per measure:");
					beatsPerMeasureLabel.setFont(new java.awt.Font("Dialog",0,14));
				}
				thisLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1, 0.0};
				thisLayout.rowHeights = new int[] {15, 15, 25, 11, 25, 10, 5, 15, 13, 25, 13, 25, 14, 12, 7, 25};
				thisLayout.columnWeights = new double[] {0.1, 0.0, 0.0, 0.0, 0.1};
				thisLayout.columnWidths = new int[] {7, 10, 53, 10, 7};
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
