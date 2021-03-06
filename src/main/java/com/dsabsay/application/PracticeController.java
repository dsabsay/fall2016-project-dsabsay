package com.dsabsay.application;

import com.dsabsay.grader.GraderException;
import com.dsabsay.grader.PerformanceGrader;
import com.dsabsay.model.ControllerException;
import com.dsabsay.model.Exercise;
import com.dsabsay.model.ExtractorException;
import com.dsabsay.model.InvalidVexTabException;
import com.dsabsay.model.PerformanceRecord;
import com.dsabsay.model.PerformanceScore;
import com.dsabsay.model.Recorder;
import com.dsabsay.model.RecorderException;
import com.dsabsay.model.VexTabExercise;
import com.dsabsay.repo.ExercisesRepo;
import com.dsabsay.repo.VexTabRhythmExercisesRepo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebView;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.sound.sampled.LineUnavailableException;

public class PracticeController {
  @FXML // ResourceBundle that was given to the FXMLLoader
  private ResourceBundle resources;
  @FXML // URL location of the FXML file that was given to the FXMLLoader
  private URL location;
  @FXML
  private Button recordButton; // Value injected by FXMLLoader
  @FXML
  private Button optionsButton;
  @FXML
  private WebView webView;
  @FXML
  private Label exerciseTypeLabel;
  @FXML
  private Label exerciseNameLabel;
  @FXML
  private ProgressIndicator progressIndicator;
  @FXML
  private Label scoreLabel;
  @FXML
  private Circle recordCircle;
  @FXML
  private ListView<String> feedbackList;

  private Recorder recorder;
  private Exercise currentExercise;
  private PerformanceGrader grader;
  
  private Logger logger = Logger.getLogger("com.dsabsay.application.PracticeController");
  
  private static final float rhythmErrorMargin = (float) 0.20;

  public PracticeController(PerformanceGrader grader) {
    this.grader = grader;
  }
  
  /**
   * Initializes the controller class.
   */
  @FXML // This method is called by the FXMLLoader when initialization is complete
  void initialize() {
    
    this.recorder = new Recorder();
    
    assert recordButton != null && optionsButton != null
        : "fx:id=\"recordButton\" was not injected: check your FXML file 'Practice.fxml'.";

    if (optionsButton != null) {
      optionsButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
          try {
            MainController.getInstance().startMainMenu();
          } catch (ControllerException ex) {
            logger.log(Level.SEVERE, "Error loading main menu.", ex);
          }
        }
      });
    }
    
    if (recordButton != null) {
      recordButton.getStylesheets().add(getClass().getClassLoader()
          .getResource("practiceController.css").toExternalForm());
      
      recordButton.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
          if (!recorder.isRecording()) {
            startRecording();
          } else {
            stopRecording();
          }
        }
      });
    }
    
    NotationWebView notationWebView = new NotationWebView(webView);
    
    ExercisesRepo repo = null;
    
    //this just creates a VexTabRhythmExercisesRepo. could be generalized
    try {
      repo = new VexTabRhythmExercisesRepo(MainController.getInstance().getUserConfiguration());
    } catch (ControllerException | IOException | InvalidVexTabException ex) {
      logger.log(Level.SEVERE, "Error initializing VexTabRhythmExercisesRepo.", ex);
      Alert alert = new Alert(AlertType.ERROR);
      alert.setTitle("No exercises found");
      alert.setHeaderText(null);
      alert.setContentText("No exercises were found. Make sure the path to your exercises is "
          + "set correctly.");
      alert.showAndWait();
      return;
    }
    
    this.currentExercise = repo.getRandomExercise();
    
    try {
      //this only works for VexTab exercises
      notationWebView.displayExercise((VexTabExercise) this.currentExercise);
      //change label
    } catch (InvalidVexTabException exception) {
      logger.log(Level.SEVERE, "Error displaying exercise.", exception);
    }
    
    exerciseTypeLabel.setText(this.currentExercise.getType());
    exerciseNameLabel.setText(this.currentExercise.getName());
    
  }
  
  private void startRecording() {
    try {
      recorder.startRecording();
      //recordButton.setStyle("-fx-background-color: #ff0000");
      recordButton.getStyleClass().add("recording");
    } catch (IOException | LineUnavailableException | RecorderException ex) {
      logger.log(Level.SEVERE, "Error starting recording.", ex);
      showAlertAndWait("Recorder Error", "An error occured trying to record audio.");
    }
  }

  private void stopRecording() {
    progressIndicator.setVisible(true);
    String performanceFilename = null;
    try {
      performanceFilename = recorder.stopRecording();
    } catch (RecorderException ex) {
      logger.log(Level.SEVERE, "Error stopping recording.", ex);
      showAlertAndWait("Throwable thrown", "An error occured in the recording thread.");
    }
    
    //recordButton.setStyle(null);
    recordButton.getStyleClass().remove("recording");
    
    PerformanceScore score = null;
    try {
      score = grader.evaluatePerformance((Exercise) currentExercise, performanceFilename,
          rhythmErrorMargin);
    } catch (ExtractorException | GraderException ex) {
      logger.log(Level.SEVERE, "Error evaluating performance.", ex);
      showAlertAndWait("ExtractorExcpetion or GraderException",
          "An error occured while evaluating the performance.");
      return;
    }
    
    progressIndicator.setVisible(false);
    scoreLabel.setText(Math.round(score.getScore() * 100) + "%");
    scoreLabel.setVisible(true);
    
    //display feedback
    ObservableList<String> list = FXCollections.observableArrayList();
    list.addAll(score.getComments());
    
    feedbackList.setEditable(true);
    feedbackList.setItems(list);
    //feedbackList.setVisible(true);
    
    //save record
    PerformanceRecord record = score.createPerformanceRecord();

    try {
      MainController.getInstance().getRecordRepo().savePerformanceRecord(record);
    } catch (IOException | ControllerException ex) {
      logger.log(Level.SEVERE, "Error saving performance record.", ex);
    }
  }
  
  private void showAlertAndWait(String title, String content) {
    Alert alert = new Alert(AlertType.ERROR);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }
  
  @FXML
  private void recordCircleMouseEntered() {
    recordCircle.setFill(Paint.valueOf("#cc7c7c"));
  }
  
  @FXML
  private void recordCircleMouseExited() {
    recordCircle.setFill(Paint.valueOf("#f8b4b4"));
  }
  
  @FXML
  private void recordCircleMouseClicked() throws InterruptedException {
    if (!recorder.isRecording()) {
      // put a delay here to effectively start the recording after the user's
      // click sound (if any)
      // still need to deal with user's click sound at end of recording
      try {
        Thread.sleep(500);
      } catch (InterruptedException ex) {
        logger.log(Level.SEVERE, "Error loading main menu.", ex);
        throw ex;
      }
      
      recordCircle.setFill(Paint.valueOf("#ff6464"));
      DropShadow dropShadow = new DropShadow();
      dropShadow.setWidth(70);
      dropShadow.setWidth(70);
      dropShadow.setRadius(35);
      dropShadow.setColor(Color.valueOf("#d31414"));
      
      recordCircle.setEffect(dropShadow);
      recordCircle.setFill(Paint.valueOf("#d31414"));
      
      startRecording();
    } else {
      recordCircle.setEffect(null);
      recordCircle.setFill(Paint.valueOf("#f8b4b4"));
      
      // need to deal with user's click sound at end of recording
      stopRecording();
    }
    
  }

}
