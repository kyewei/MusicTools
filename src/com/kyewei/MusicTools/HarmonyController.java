package com.kyewei.MusicTools;

import javax.sound.midi.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


import javax.sound.midi.MidiSystem;

/**
 * Created by Kye on 2014-07-18.
 */


public class HarmonyController {
    private HarmonyEngine engine;
    //private JFrame mainFrame;
    private HarmonyView panel;
    private Sequencer sequencer;

    public HarmonyController(HarmonyEngine engine, HarmonyView panel) {
        this.engine = engine;
        //this.mainFrame = panel.mainFrame;
        this.panel = panel;
        this.panel.scorePanel.updateReference(engine.getSoprano(), engine.getAlto(), engine.getTenor(), engine.getBass());
        this.panel.scorePanel.updateCurrentChord(engine.currentChord);
        this.panel.scorePanel.updateKey(engine.key);
        setupActionListeners();
    }

    public void setupActionListeners() {
        panel.button0.addActionListener(new Action("Action", KeyEvent.VK_A));
        panel.button1.addActionListener(new Action());
        panel.button2.addActionListener(new Action());
        panel.HTButton1.addActionListener(new Action());
        panel.HTButton2.addActionListener(new Action());
        panel.button4.addActionListener(new Action());

        panel.makeBass.addActionListener(new Action());
        panel.makeChord.addActionListener(new Action());
        panel.prevButton.addActionListener(new Action());
        panel.nextButton.addActionListener(new Action());
        panel.completeAll.addActionListener(new Action());
        panel.playMidi.addActionListener(new Action());

        panel.exit.addActionListener(new MenuAction());
        panel.parallel5.addActionListener(new MenuAction());
        panel.parallel8.addActionListener(new MenuAction());
        panel.hidden5.addActionListener(new MenuAction());
        panel.hidden8.addActionListener(new MenuAction());
        panel.keyThroughSign.addActionListener(new MenuAction());
        panel.lyExport.addActionListener(new MenuAction());
        panel.exportButton.addActionListener(new MenuAction());
    }

    public class MenuAction extends AbstractAction {
        public MenuAction() {
            super();
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == panel.parallel5) {
                engine.checkParallelFifths = !engine.checkParallelFifths;
            } else if (e.getSource() == panel.parallel8) {
                engine.checkParallelOctaves = !engine.checkParallelOctaves;
            } else if (e.getSource() == panel.hidden5) {
                engine.checkHiddenFifths = !engine.checkHiddenFifths;
            } else if (e.getSource() == panel.hidden8) {
                engine.checkHiddenOctaves = !engine.checkHiddenOctaves;
            } else if (e.getSource() == panel.keyThroughSign) {
                String[] options = {"Cb", "Gb", "Db", "Ab", "Eb", "Bb", "F", "C", "G", "D", "A", "E", "B", "F#", "C#"};

                int result = JOptionPane.showOptionDialog(null,
                        "What Major Key?",
                        "Key Changer", 0, JOptionPane.QUESTION_MESSAGE,
                        null, options, "C");
                //System.out.println("Answer: "+code);
                if (result != -1) {
                    engine.key = new Pitch(options[result]);
                    engine.scale = new MajorScale(engine.key);
                    panel.scorePanel.updateKey(engine.key);
                    panel.keyInfo.setText(options[result]);
                    //engine.reset();
                    engine.currentChord = 0;
                    for (int i = 0; i < engine.soprano.length; ++i) {
                        engine.chord[i].updateKey(engine.scale.scale[engine.currentProgression[i] - 1]);
                        engine.soprano[i] = null;
                        engine.alto[i] = null;
                        engine.tenor[i] = null;
                        engine.bass[i] = null;
                    }
                    if (!engine.usedProper)
                        engine.buildBass();
                    else //if (engine.usedProper)
                        engine.buildProperBass();
                    panel.progressionInfo.setText(engine.convertProgressionToRoman());

                }
                panel.scorePanel.updateCurrentChord(engine.currentChord);
                panel.scorePanel.updateReference(engine.getSoprano(), engine.getAlto(), engine.getTenor(), engine.getBass());

                //mainFrame.repaint();
                panel.repaint();
            } else if (e.getSource() == panel.lyExport || e.getSource() == panel.exportButton) {
                panel.lilypondOutputDisplay.setVisible(true);
                panel.lilypondOutput.setText("");

                String separator = System.getProperty("line.separator");

                panel.lilypondOutput.append("%{ Generated by MusicTools %}");
                panel.lilypondOutput.append(separator + "\\version \"2.18.2\"");
                panel.lilypondOutput.append(separator + "\\header{" + separator + "  title = \"Chord Progression\"" + separator + "}");
                panel.lilypondOutput.append(separator + "settings = {" + separator + "  \\key " + engine.key.printForLilypondAbsolute() + " \\major" + separator + "  \\time 4/4" + separator + "}");

                panel.lilypondOutput.append(separator + "soprano = {" + separator + "  ");
                for (Note note : engine.soprano)
                    if (note != null)
                        panel.lilypondOutput.append(note.printForLilypondAbsolute() + " ");
                panel.lilypondOutput.append(separator + "}");

                panel.lilypondOutput.append(separator + "alto = {" + separator + "  ");
                for (Note note : engine.alto)
                    if (note != null)
                        panel.lilypondOutput.append(note.printForLilypondAbsolute() + " ");
                panel.lilypondOutput.append(separator + "}");

                panel.lilypondOutput.append(separator + "tenor = {" + separator + "  ");
                for (Note note : engine.tenor)
                    if (note != null)
                        panel.lilypondOutput.append(note.printForLilypondAbsolute() + " ");
                panel.lilypondOutput.append(separator + "}");

                panel.lilypondOutput.append(separator + "bass = {" + separator + "  ");
                for (Note note : engine.bass)
                    if (note != null)
                        panel.lilypondOutput.append(note.printForLilypondAbsolute() + " ");
                panel.lilypondOutput.append(separator + "}");

                panel.lilypondOutput.append(separator + "\\score {");
                panel.lilypondOutput.append(separator + "  \\new ChoirStaff <<");
                panel.lilypondOutput.append(separator + "    \\new Staff = \"sa\" <<");
                panel.lilypondOutput.append(separator + "      \\clef treble");
                panel.lilypondOutput.append(separator + "      \\set Staff.midiInstrument = #\"choir aahs\"");
                panel.lilypondOutput.append(separator + "      \\new Voice = \"soprano\" {");
                panel.lilypondOutput.append(separator + "        \\voiceOne");
                panel.lilypondOutput.append(separator + "        << \\settings \\soprano >>");
                panel.lilypondOutput.append(separator + "      }");
                panel.lilypondOutput.append(separator + "      \\new Voice = \"alto\" {");
                panel.lilypondOutput.append(separator + "        \\voiceTwo");
                panel.lilypondOutput.append(separator + "        << \\settings \\alto >>");
                panel.lilypondOutput.append(separator + "      }");
                panel.lilypondOutput.append(separator + "    >>");
                panel.lilypondOutput.append(separator + "    \\new Staff = \"tb\" <<");
                panel.lilypondOutput.append(separator + "      \\clef bass");
                panel.lilypondOutput.append(separator + "      \\set Staff.midiInstrument = #\"choir aahs\"");
                panel.lilypondOutput.append(separator + "      \\new Voice = \"tenor\" {");
                panel.lilypondOutput.append(separator + "        \\voiceOne");
                panel.lilypondOutput.append(separator + "        << \\settings \\tenor >>");
                panel.lilypondOutput.append(separator + "      }");
                panel.lilypondOutput.append(separator + "      \\new Voice = \"bass\" {");
                panel.lilypondOutput.append(separator + "        \\voiceTwo");
                panel.lilypondOutput.append(separator + "        << \\settings \\bass >>");
                panel.lilypondOutput.append(separator + "      }");
                panel.lilypondOutput.append(separator + "    >>");
                panel.lilypondOutput.append(separator + "  >>");
                panel.lilypondOutput.append(separator + "  \\layout {}");
                panel.lilypondOutput.append(separator + "  \\midi {}");
                panel.lilypondOutput.append(separator + "}");

                if (e.getSource() == panel.exportButton) {
                    JFileChooser choose = panel.fc;

                    int result = choose.showSaveDialog(panel);

                    if (result == JFileChooser.APPROVE_OPTION) {
                        try {
                            File file = choose.getSelectedFile();
                            BufferedWriter output = new BufferedWriter(new FileWriter(file));

                            output.write(panel.lilypondOutput.getText());
                            output.flush();
                            output.close();

                        } catch (IOException error) {
                            error.printStackTrace();
                        }

                    }
                }

            } else if (e.getSource() == panel.exit) {
                System.exit(0);
            }
        }
    }

    private class Action extends AbstractAction {

        public Action(String name, Integer mnemonic) {
            super(name);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public Action() {
            super();
        }

        private MidiEvent createNoteOnEvent(int nKey, long lTick, int VELOCITY) {
            return createNoteEvent(ShortMessage.NOTE_ON, nKey, VELOCITY, lTick);
        }
        private MidiEvent createNoteOffEvent(int nKey, long lTick, int VELOCITY) {
            return createNoteEvent(ShortMessage.NOTE_OFF, nKey, 0, lTick);
        }
        private MidiEvent createNoteEvent(int nCommand, int nKey, int nVelocity, long lTick) {
            ShortMessage message = new ShortMessage();
            try {
                // always on channel 1
                message.setMessage(nCommand, 0, nKey, nVelocity);
            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
            }
            MidiEvent event = new MidiEvent(message, lTick);
            return event;
        }


        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == panel.button0 || e.getSource() == panel.button1) {
                engine.reset();

                int numberOfChords = engine.numberOfChords;

                if (e.getSource() == panel.button0)
                    engine.currentProgression = engine.makeNLongChordProgression(numberOfChords, 1, 1, 1);
                else if (e.getSource() == panel.button1)
                    engine.currentProgression = engine.makeNLongChordProgression(numberOfChords, 2, 1, 1);

                engine.usedProper = false;
                engine.buildBass();

                panel.progressionInfo.setText(engine.convertProgressionToRoman());
                panel.numberOfChordsInfo.setText("" + engine.numberOfChords);

                panel.scorePanel.updateCurrentChord(engine.currentChord);
                panel.scorePanel.updateReference(engine.getSoprano(), engine.getAlto(), engine.getTenor(), engine.getBass());

                //mainFrame.repaint();

            } else if (e.getSource() == panel.button2) {
                boolean result;
                do {
                    Object[] arrays = engine.makeProperProgression();
                    engine.numberOfChords = (int) (Integer) (arrays[3]);
                    engine.reset();
                    engine.currentProgression = (int[]) (arrays[0]);
                    engine.chord = (Chord[]) (arrays[1]);
                    engine.tonicization = (int[]) (arrays[2]);
                    engine.usedProper = true;
                    result = engine.buildProperBass();
                } while (!result);

                panel.progressionInfo.setText(engine.convertProgressionToRoman());
                panel.numberOfChordsInfo.setText("" + engine.numberOfChords);

                panel.scorePanel.updateCurrentChord(engine.currentChord);
                panel.scorePanel.updateReference(engine.getSoprano(), engine.getAlto(), engine.getTenor(), engine.getBass());

                //mainFrame.repaint();
            } else if (e.getSource() == panel.makeBass) {
                if (!engine.usedProper)
                    engine.buildBass();
                else //if (engine.usedProper)
                    engine.buildProperBass();

                panel.progressionInfo.setText(engine.convertProgressionToRoman());
                panel.numberOfChordsInfo.setText("" + engine.numberOfChords);

                panel.scorePanel.updateCurrentChord(engine.currentChord);

                //mainFrame.repaint();
            } else if (e.getSource() == panel.makeChord) {
                engine.nextDriver();
                panel.scorePanel.updateCurrentChord(engine.currentChord);

                //mainFrame.repaint();
            } else if (e.getSource() == panel.completeAll) {
                engine.doAllTheThings();

                panel.scorePanel.updateCurrentChord(engine.currentChord);
                //mainFrame.repaint();
            } else if (e.getSource() == panel.playMidi) {

                if (engine.currentChord < engine.bass.length)
                    return;

                int channel = 0;
                int duration = 1000; // 1 sec
                int volume = 100;

                try {
                    if (sequencer == null) {
                        sequencer = MidiSystem.getSequencer();
                        sequencer.open();
                    }
                    if (sequencer == null)
                        return;


                    Sequence sequence = new Sequence(Sequence.PPQ, 1);
                    Track track = sequence.createTrack();

                    for (int i = 0; i < engine.bass.length; ++i) {
                        track.add(createNoteOnEvent(engine.bass[i].getChromaticNumber() + 12, i * 2, volume));
                        track.add(createNoteOnEvent(engine.tenor[i].getChromaticNumber() + 12, i * 2, volume));
                        track.add(createNoteOnEvent(engine.alto[i].getChromaticNumber() + 12, i * 2, volume));
                        track.add(createNoteOnEvent(engine.soprano[i].getChromaticNumber() + 12, i * 2, volume));
                        track.add(createNoteOffEvent(engine.bass[i].getChromaticNumber() + 12, (i + 1) * 2, 0));
                        track.add(createNoteOffEvent(engine.tenor[i].getChromaticNumber() + 12, (i + 1) * 2, 0));
                        track.add(createNoteOffEvent(engine.alto[i].getChromaticNumber() + 12, (i + 1) * 2, 0));
                        track.add(createNoteOffEvent(engine.soprano[i].getChromaticNumber() + 12, (i + 1) * 2, 0));
                    }
                    sequencer.setSequence(sequence);
                    sequencer.start();

                } catch (Exception err) {
                    err.printStackTrace();
                }

            } else if (e.getSource() == panel.prevButton) {
                engine.goPrev();
                panel.scorePanel.updateCurrentChord(engine.currentChord);

                //mainFrame.repaint();
            } else if (e.getSource() == panel.nextButton) {
                engine.goNext();
                panel.scorePanel.updateCurrentChord(engine.currentChord);

                //mainFrame.repaint();
            } else if (e.getSource() == panel.button4 || e.getSource() == panel.HTButton1 || e.getSource() == panel.HTButton2) {

                String input = "";
                if (e.getSource() == panel.button4)
                    input = JOptionPane.showInputDialog("Enter chord progression separated by dashes '-' using functional chord notation: ");
                else if (e.getSource() == panel.HTButton1)
                    input = engine.makeProgressionFromHTData(1);
                else if (e.getSource() == panel.HTButton2)
                    input = engine.makeProgressionFromHTData(2);

                if (input != null && !input.equals("")) {
                    String[] input2 = input.trim().split("-");
                    int size = input2.length;
                    Chord[] chordx = new Chord[size];
                    int[] toniz = new int[size];

                    int[] chpro = new int[size];

                    engine.numberOfChords = size;
                    engine.reset();

                    for (int i = 0; i < input2.length; ++i) {
                        int[] temp = engine.recognizeFunctionalChordSymbol(input2[i]);
                        toniz[i] = temp[6];
                        chpro[i] = temp[0];

                        Pitch temp2 = new Pitch(engine.scale.scale[(temp[0] - 1) % 7]);
                        char tonicizequality = (temp[6] == 0 || temp[6] == 3 || temp[6] == 4 ? 'P' : 'M'); //P1, P4, P5
                        temp2 = Pitch.getHigherPitchWithInterval(temp2, temp[6]+1, tonicizequality);
                        char modify = (temp[7] == -1 ? 'b' : (temp[7] == 1 ? '#' : ' '));
                        if (temp[7] == -1) { // lowered pitches
                            temp2 =Pitch.getHigherPitchWithInterval(temp2, 3, 'm');
                            temp2 =Pitch.getHigherPitchWithInterval(temp2, -3, 'M');
                        }
                        if (temp[7] == 1) { // raised pitches
                            temp2 =Pitch.getHigherPitchWithInterval(temp2, -3, 'm');
                            temp2 =Pitch.getHigherPitchWithInterval(temp2, 3, 'M');
                        }
                        chordx[i] = new Chord(temp2, temp[1], temp[2], (char) (temp[3]), (char) (temp[4]), (char) (temp[5]), modify);
                    }
                    engine.usedProper = true;
                    engine.currentProgression = chpro;
                    engine.chord = chordx;
                    engine.tonicization = toniz;

                    // gotta build bassline
                    engine.buildProperBass();

                    panel.progressionInfo.setText(engine.convertProgressionToRoman());
                    panel.numberOfChordsInfo.setText("" + engine.numberOfChords);

                    panel.scorePanel.updateCurrentChord(engine.currentChord);
                    panel.scorePanel.updateReference(engine.getSoprano(), engine.getAlto(), engine.getTenor(), engine.getBass());

                    //mainFrame.repaint();
                }
            }
            //mainFrame.repaint();
            panel.repaint();


        }
    }


}
