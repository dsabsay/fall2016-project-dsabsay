package com.dsabsay.model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class VexTabRhythmParserTester {
  /**
   * Tester for VexTabRhythmParser.
   * @param args args
   * @throws InvalidVexTabException if InvalidVexTabException is thrown
   * @throws IOException if IO error occurs
   * @throws FileNotFoundException if file not found
   */
  public static void main(String[] args) throws FileNotFoundException, IOException,
      InvalidVexTabException {
    /*
    String path
        = TestVexTabRhythmParser.class.getClassLoader().getResource("rhythm1.txt").toString();
    */
    //String path = "/resources/rhythm1.txt";
    String path = "src/main/resources/testRhythm1.txt";
    VexTabRhythmExercise exercise = new VexTabRhythmExercise(1, "test", path);
    
    List<Note> notes = new ArrayList<Note>();
    notes.add(new Note(2, false, false));
    notes.add(new Note(4, false, false));
    notes.add(new Note(4, false, true));
    notes.add(new Note(8, false, false));
    notes.add(new Note(8, false, false));
    notes.add(new Note(4, false, false));
    notes.add(new Note(4, false, false));
    notes.add(new Note(4, false, false));
    
    int[] timeSig = {4, 4};
    RhythmExercise expected = new RhythmExercise(timeSig[0], timeSig[1], notes);
    RhythmExercise parsed = exercise.getExercise();
    
  }
}
