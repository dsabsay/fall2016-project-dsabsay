package com.dsabsay.repo;

import com.dsabsay.model.Exercise;
import com.dsabsay.model.InvalidVexTabException;
import com.dsabsay.model.UserConfiguration;
import com.dsabsay.model.VexTabExercise;
import com.dsabsay.model.VexTabRhythmExercise;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

//public class VexTabRhythmExercisesRepo extends VexTabExercisesRepo implements ExercisesRepo {
public class VexTabRhythmExercisesRepo implements ExercisesRepo {
  private String rhythmsPath;
  List<VexTabExercise> exercises;
  
  public VexTabRhythmExercisesRepo(UserConfiguration config) 
      throws IOException, InvalidVexTabException {
    this.rhythmsPath = config.getRhythmsPath();
    loadExercisesFromDisk();
  }
  
  private List<Exercise> convertExerciseList() {
    List<Exercise> list = new ArrayList<Exercise>();
    
    for (VexTabExercise vtExercise : this.exercises) {
      list.add((Exercise) vtExercise);
    }
    
    return list;
  }
  
  public List<Exercise> getExercises() {
    return convertExerciseList();
  }
  
  /**
   * Gets a random exercise from the repo. Returns null if there are no exercises.
   */
  //public VexTabExercise getRandomExercise() {
  public Exercise getRandomExercise() {
    // This should probably check the size of the exercises list
    if (exercises == null) {
      return null;
    }
    
    if (exercises.isEmpty()) {
      return null;
    }
    
    Random random = new Random();
    
    //return this.exercises.get((int) (Math.random() * this.exercises.size()));
    //return this.exercises.get((int) (random.nextFloat() * this.exercises.size()));
    return (Exercise) this.exercises.get((int) (random.nextFloat() * this.exercises.size()));
  }
  
  private void loadExercisesFromDisk() 
      throws IOException, InvalidVexTabException {
    
    if (rhythmsPath == null) {
      throw new FileNotFoundException("userConfig.rhythmsPath is null");
    }
    
    System.out.println("loadExercisesFromDisk");
    File folder = new File(rhythmsPath);
    System.out.println(folder);
    
    if (!folder.isDirectory()) {
      throw new FileNotFoundException("userConfig.rhythmsPath is not a valid directory");
    }
    
    this.exercises = new ArrayList<VexTabExercise>();
    
    for (File fileEntry : folder.listFiles()) {
      System.out.println("Exercise: " + fileEntry.getName());
      //need to deal with exercise id and exercise type
      String name = fileEntry.getName();
      name = name.substring(0, name.indexOf('.'));
      exercises.add(new VexTabRhythmExercise(1, "test", fileEntry.getPath(), name));
    }
    
    if (this.exercises.isEmpty()) {
      throw new FileNotFoundException();
    }
  }
}
