package com.dsabsay.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/*
 * Stores information about the user's configuration.
 * This information will be read from the disk when the application is loaded.
 */
public class UserConfiguration {
  private HashMap<String, String> melodyTypes;
  private HashMap<String, String> rhythmTypes;
  private String sightSingingRecordsPath = "src/main/records/sightSingingRecords.ser";
  private String rhythmRecordsPath = "src/main/records/rhythmRecords.ser";
  private String pathToConfig = "src/main/resources/userConfiguration";

  private static final String DEFAULT_RHYTHMS_PATH = "src/main/exercises/testRhythmExercises";
  private String rhythmsPath;
  
  // to use singleton pattern, the constructor needs to be private
  // the instance of this class is retrieved via a getInstance() method
  public UserConfiguration() throws IOException {
    //readUserConfig();
    this.rhythmsPath = DEFAULT_RHYTHMS_PATH;
  }

  public String getRhythmsPath() {
    return rhythmsPath;
  }

  public void setRhythmsPath(String rhythmsPath) {
    this.rhythmsPath = rhythmsPath;
  }

  public HashMap<String, String> getMelodyTypes() {
    return melodyTypes;
  }

  public void setMelodyTypes(HashMap<String, String> melodyTypes) {
    this.melodyTypes = melodyTypes;
  }

  public HashMap<String, String> getRhythmTypes() {
    return rhythmTypes;
  }

  public void setRhythmTypes(HashMap<String, String> rhythmTypes) {
    this.rhythmTypes = rhythmTypes;
  }

  public String getSightSingingRecordsPath() {
    return sightSingingRecordsPath;
  }

  public void setSightSingingRecordsPath(String sightSingingRecordsPath) {
    this.sightSingingRecordsPath = sightSingingRecordsPath;
  }

  public String getRhythmRecordsPath() {
    return rhythmRecordsPath;
  }

  public void setRhythmRecordsPath(String rhythmRecordsPath) {
    this.rhythmRecordsPath = rhythmRecordsPath;
  }

  /**
   * Reads user configuration settings from the disk
   * and loads them into this instance of UserConfiguration.
   */
  public void readUserConfig() throws IOException {
    BufferedReader reader = new BufferedReader(new FileReader(pathToConfig));
    //this.rhythmRecordsPath = reader.readLine();
    //this.rhythmsPath = reader.readLine();
    reader.close();
  }

  /**
   * Saves user configuration settings stored in this instance of UserConfiguration to disk.
   */
  public void saveUserConfig() throws IOException {
    BufferedWriter writer = new BufferedWriter(new FileWriter(pathToConfig));
    //writer.write(rhythmRecordsPath);
    writer.write(rhythmsPath);
    writer.close();    
  }
}
