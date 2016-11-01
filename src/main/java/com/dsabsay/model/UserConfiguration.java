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
  private String sightSingingRecordsPath;
  private String rhythmRecordsPath;
  private String pathToConfig = "src/main/resources/userConfiguration";

  private String rhythmsPath;

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
  public void readUserConfig() {

    try {
      FileReader file = new FileReader(pathToConfig);
      BufferedReader reader = new BufferedReader(file);
      this.rhythmRecordsPath = reader.readLine();
      reader.close();
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }

  /**
   * Saves user configuration settings stored in this instance of UserConfiguration to disk.
   */
  public void saveUserConfig() {
    try {
      FileWriter file = new FileWriter(pathToConfig);
      BufferedWriter writer = new BufferedWriter(file);
      writer.write(rhythmRecordsPath);
      writer.close();
    } catch (IOException exception) {
      exception.printStackTrace();
    }
  }
}