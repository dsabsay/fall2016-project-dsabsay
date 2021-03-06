package com.dsabsay.grader;

import com.dsabsay.model.Exercise;
import com.dsabsay.model.ExtractorException;
import com.dsabsay.model.Note;
import com.dsabsay.model.PerformanceScore;
import com.dsabsay.model.RhythmExercise;
import com.dsabsay.model.RhythmScore;
import com.dsabsay.model.VexTabRhythmExercise;

import java.util.ArrayList;
import java.util.List;

//public class SimpleRhythmGrader extends VexTabRhythmGrader {
//public class SimpleRhythmGrader extends RhythmGrader {
public class SimpleRhythmGrader implements PerformanceGrader {
  
  public SimpleRhythmGrader() {
    
  }

  /**
   * Evaluates the performance and returns the PerformanceScore object containing the results.
   * @param exercise the exercise being graded
   * @param performanceFilename the filename of the audio file of the performance
   * @param rhythmErrorMargin amount of allowable error
   * @return the score for the performance
   * @throws GraderException if the exercise given is not a VexTabRhythmExercise
   */
  public PerformanceScore evaluatePerformance(Exercise exercise, String performanceFilename,
      float rhythmErrorMargin) throws ExtractorException, GraderException {
    
    // check if exercise is the right type
    if (!(exercise instanceof VexTabRhythmExercise)) {
      throw new GraderException("Invalid Exercise type!");
    }
    
    //errorMargin (in beats)
    final float errorMargin = (float) 0.20;
    
    RhythmExtractor extractor = new RhythmExtractor();
    RhythmExtractorResults results;

    results = extractor.processPerformance(performanceFilename);
    
    SimpleRhythmGrader grader = new SimpleRhythmGrader();
    
    //still not sure what will happen if the exercise is not a VexTabRhythmExercise
    PerformanceScore score = grader.evaluatePerformanceSimpler((VexTabRhythmExercise) exercise,
        results, errorMargin);
    
    return score;
  }
  
  /**
   * Evaluates the rhythm performance.
   * @param vextabExercise the exercise
   * @param performance the performance (parsed as a RhythmExtractorResults object)
   * @param errorMargin acceptable margin of error for onsets
   * @return PerformanceScore containing the calculated score
   */
  public PerformanceScore evaluatePerformanceSimpler(VexTabRhythmExercise vextabExercise,
      RhythmExtractorResults performance, float errorMargin) {
    int numCorrect = 0;
    int numWrong = 0;
    //this gets number of notes including rests!
    //maybe just subtract rests for now
    //int numNotesInExercise = vextabExercise.getExercise().getNotes().size();
    
    //note onsets (in beats)
    List<Float> exerciseNoteOnsets = getNoteOnsets(performance.getBpm(),
        vextabExercise.getExercise());
    
    System.out.print("Exercise Note Onsets: ");
    for (float a : exerciseNoteOnsets) {
      System.out.print(a + ", ");
    }
    
    System.out.println();
    
    List<Float> performanceNoteOnsets = getNoteOnsets(performance);

    System.out.print("Performance Note Onsets: ");
    for (float a : performanceNoteOnsets) {
      System.out.print(a + ", ");
    }
    
    System.out.println();
    
    for (float f : performanceNoteOnsets) {
      if (hasNote(exerciseNoteOnsets, f, errorMargin)) {
        numCorrect++;
      } else {
        numWrong++;
      }
    }
    
    System.out.println("numCorrect: " + numCorrect);
    System.out.println("numWrong: " + numWrong);
    
    int numNotesInExercise = numNotesWithoutRests(vextabExercise.getExercise().getNotes());
    float rhythmScore = (numCorrect - numWrong) / (float) numNotesInExercise;
    
    List<String> comments = new ArrayList<String>();
    if (rhythmScore > .8) {
      comments.add("Great job!");
    } else {
      comments.add("Try again.");
    }
    comments.add("Correct Notes: " + numCorrect);
    comments.add("Wrong Notes: " + numWrong);

    // the score calculation might be improved by factoring how "wrong" the wrong notes were
    // maybe calculate the number of notes in the exercise that are missing in the performance?
    PerformanceScore score = new RhythmScore(rhythmScore, comments, vextabExercise);
    
    return score;
  }
  
  //searches for the note onset in exerciseNoteOnsets
  private boolean hasNote(List<Float> exerciseNoteOnsets, float onset, float errorMargin) {
    //just iterate through entire list for now
    for (float f : exerciseNoteOnsets) {
      if (onset < f + errorMargin && onset > f - errorMargin) {
        return true;
      }
    }
    
    return false;
  }
  
  //converts a float duration to number of beats (based on the bpm)
  //assumes duration is measured in seconds
  private float tickToBeats(float duration, float bpm) {
    float beatsPerSecond = bpm / (float) 60;
    float beats = duration * beatsPerSecond;
    
    return beats;
  }
  
  private List<Float> getNoteOnsets(RhythmExtractorResults performance) {
    float bpm = performance.getBpm();
    List<Float> noteOnsets = new ArrayList<Float>();
    
    //add first note onset = 0;
    noteOnsets.add((float) 1.0);
    
    float start = performance.getOnsets().get(0);
    
    List<Float> onsets = performance.getOnsets();
    
    for (int i = 1; i < onsets.size(); i++) {
      float onset = onsets.get(i) - start;
      noteOnsets.add(tickToBeats(onset, bpm) + 1);
    }
    
    return noteOnsets;
  }
  
  //gets onsets (in beats) for the notes in the exercise
  //make public to test it
  // this doesn't work for 16th notes?
  /**
   * Get list of note onsets. Only public for testing purposes.
   * @param bpm bpm of the performance
   * @param exercise the exercise
   * @return list of floats representing the onset time of each note in the exercise
   */
  public List<Float> getNoteOnsets(float bpm, RhythmExercise exercise) {
    List<Float> noteOnsets = new ArrayList<Float>();
    
    //add first note onset = 0
    //noteOnsets.add((float) 0.0);
    //int timeSigNumerator = exercise.getTimeSig()[0];
    int timeSigDenominator = exercise.getTimeSig()[1];
    
    float totalBeats = 1;
    
    //need to skip last note, or remember there is an onset for the next beat
    //after the end of the rhythm
    //this is getting rests too
    for (Note note : exercise.getNotes()) {
      int rhythmicValue = note.getRhythmicValue();
      //get number of beats for this note
      //float beats = rhythmicValue / (float) timeSigDenominator;
      float beats = timeSigDenominator / (float) rhythmicValue;
      //System.out.println("rhythmValue: " + rhythmicValue + " beats: " + beats);
      
      
      //only add if note is not a rest
      if (!note.getIsRest()) {
        noteOnsets.add(totalBeats);
      }
      
      totalBeats += beats;
    }
    
    //remove last onset
    //noteOnsets.remove(noteOnsets.size() - 1);
    
    return noteOnsets;
  }
  
  private int numNotesWithoutRests(List<Note> notes) {
    int num = 0;
    
    for (Note n : notes) {
      if (!n.getIsRest()) {
        num++;
      }
    }
    
    return num;
  }
  
}
