package com.dsabsay.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class Recorder {
  private static final String ESSENTIA_WORKING_DIRECTORY = "essentia/";
  private static final String PERFORMANCE_FILENAME = "performance.wav";
  
  private TargetDataLine line;
  private volatile boolean isRecording;
  private Thread recordingThread;
  
  private volatile Throwable exceptionInThread;
  
  private Logger logger = Logger.getLogger("com.dsabsay.model.Recorder");
  
  public Recorder() {
    this.isRecording = false;
    this.exceptionInThread = null;
  }
  
  public boolean isRecording() {
    return this.isRecording;
  }
  
  /**
   * Stops the current recording, if any.
   * @throws RecorderException if there was an exception thrown in the recording thread
   */
  public String stopRecording() throws RecorderException {
    if (line != null) {
      //line.stop();
      //line.drain();
      line.close();
    }
    
    this.isRecording = false;
    
    // wait for recording thread to finish
    while (this.recordingThread.isAlive()) {
    }
    
    //check for exception
    if (this.exceptionInThread != null) {
      //throw this.exceptionInThread;
      throw new RecorderException("Exception in recording thread.");
    }
    
    return PERFORMANCE_FILENAME;
  }
  
  /**
   * Records audio and writes it to a file.
   * @throws IOException if an IO error occurs
   * @throws LineUnavailableException if the line is unavailable
   * @throws RecorderException if system does not support the Line
   */
  public void startRecording() throws IOException, LineUnavailableException, RecorderException {
    this.isRecording = true;
    
    AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
    
    if (!AudioSystem.isLineSupported(info)) {
      // Handle the error
      throw new RecorderException("Line is not supported.");
    }
    
    //Obtain and open the line
    line = (TargetDataLine) AudioSystem.getLine(info);
    line.open(format);
    
    //captureAudio();
        
    // catch exceptions from thread
    Thread.UncaughtExceptionHandler threadExceptionHandler = new Thread.UncaughtExceptionHandler() {
      public void uncaughtException(Thread th, Throwable ex) {
        // save the exception
        // check it in stopRecording()
        exceptionInThread = ex;
      }
    };
    
    // start thread
    this.recordingThread = new Thread(new RecordAudio());
    this.recordingThread.setUncaughtExceptionHandler(threadExceptionHandler);
    this.recordingThread.start();
    
  }
  
  private class RecordAudio implements Runnable {
    public void run() {
      //read data from line
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      int numBytesRead;
      byte[] dataBuf = new byte[line.getBufferSize() / 5];
      
      // Begin audio capture
      FileOutputStream outputFile = null;
      try {
        outputFile
            = new FileOutputStream(new File(ESSENTIA_WORKING_DIRECTORY + PERFORMANCE_FILENAME));
      } catch (FileNotFoundException ex) {
        // might want to throw exception here, but not sure how to handle throwing an exception
        // in a different thread
        // ideally, the exception should be handled in the controller's thread
        logger.log(Level.SEVERE, "Error starting audio capture.", ex);
        // just return if the output file cannot be opened
        return;
      }
      AudioFileFormat.Type fileType = AudioFileFormat.Type.WAVE;
      
      line.start();
      
      System.out.println("recording started");
      while (isRecording) {
        // Read the next chunk of data from the line
        numBytesRead = line.read(dataBuf, 0, dataBuf.length);
        // Save the chunk of data
        out.write(dataBuf, 0, numBytesRead);
        //out.writeTo(fileOutput);
      }
      
      // After line is stopped
      System.out.println("recording stopped");
      
      line.drain();
      //line.stop();
      
      while (line.available() > 0) {
        System.out.println("saving remaining data");
        System.out.println("line.available: " + line.available());
        // Read the next chunk of data from the line
        numBytesRead = line.read(dataBuf, 0, dataBuf.length);
        // Save the chunk of data
        out.write(dataBuf, 0, numBytesRead);
        //out.writeTo(fileOutput);
      }
      
      //line has to be stopped after saving the remaining data?
      line.stop();
      
      //copied from above
      AudioFormat format = new AudioFormat(44100, 16, 1, true, true);
      byte[] data = out.toByteArray();
      
      AudioInputStream audioInputStream
          = new AudioInputStream(new ByteArrayInputStream(data), format, data.length);
      
      System.out.println("data.length: " + data.length);
      try {
        AudioSystem.write(audioInputStream, fileType, outputFile);
      } catch (IOException ex) {
        logger.log(Level.SEVERE, "Error writing audio file.", ex);
      }
      
      
      // clean up
      line.close();
      try {
        outputFile.close();
      } catch (IOException ex) {
        logger.log(Level.SEVERE, "Error writing audio file.", ex);
      }
      
    }
  }
  
}
