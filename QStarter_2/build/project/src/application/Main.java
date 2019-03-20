package application;

import java.awt.Desktop;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.controlsfx.control.StatusBar;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;


public class Main extends Application {
	BorderPane root;
	WeatherPageMaintain wpm;
	ProgramPageMaintain ppm;
	WebPageMaintain webpm; 
	EventHandler<KeyEvent> mainHandler;
	EventHandler<KeyEvent> nextHandler;
	String where = "F1";
	StatusBar bar;
	@Override
	public void start(Stage primaryStage) {
		try {
			primaryStage.setTitle("Dev by - WS");
			primaryStage.getIcons().add(new Image("file:resources/speed.png"));
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			    @Override
			    public void handle(WindowEvent event) {
			        Platform.exit();
			        System.exit(0);
			    }
			});
			root = new BorderPane();
			root.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(1))));
			wpm = new WeatherPageMaintain(root);
			ppm = new ProgramPageMaintain(primaryStage);
			webpm = new WebPageMaintain(root);
			Thread tr = new Thread(wpm);
			tr.setDaemon(true);
    		tr.start();
    		root.setCenter(wpm.getWeatherPane());
    		buildTop();
    		buildLeft();
    		buildKeySet();
/*			String hostname = "null";
			try
			{
			    InetAddress addr;
			    addr = InetAddress.getLocalHost();
			    hostname = addr.getHostName();
			}
			catch (Exception ex)
			{
			    System.out.println("Hostname can not be resolved");
			}
			System.out.println(hostname);*/

    		root.getStyleClass().add("pane");
			Scene scene = new Scene(root,650,300);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void buildLeft(){
		VBox vb = new VBox(5);
		Button homeBtn = new Button("Home");
		homeBtn.setId("main");
		homeBtn.setMinWidth(100);
		homeBtn.setMaxWidth(100);
		homeBtn.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		    	root.setCenter(wpm.getWeatherPane());
		    }
		});

		Button programBtn = new Button("Program");
		programBtn.setId("main");
		programBtn.setMinWidth(100);
		programBtn.setMaxWidth(100);
		programBtn.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		    	root.setCenter(ppm.getProgramPane());
		    }
		});
		
		Button webBtn = new Button("Web");
		webBtn.setId("main");
		webBtn.setMinWidth(100);
		webBtn.setMaxWidth(100);
		webBtn.setOnAction(new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		    	root.setCenter(webpm.getWebPane());
		    }
		});

		vb.getChildren().addAll(homeBtn, programBtn, webBtn);
		vb.setAlignment(Pos.TOP_CENTER);
		vb.setId("pane");
		vb.setPadding(new Insets(15, 5, 15, 10));
		vb.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(1))));

		root.setLeft(vb);
	}

	public void buildKeySet() {
		
		mainHandler = new EventHandler<KeyEvent>() {
	        @Override
	        public void handle(KeyEvent ke) {

	            switch (ke.getCode()) {
	                case ENTER:
	                    root.removeEventHandler(KeyEvent.KEY_PRESSED, mainHandler);
	                    root.addEventHandler(KeyEvent.KEY_PRESSED, nextHandler);
	                    break;
	                case DIGIT1:
	                	where = "1";
	                    root.setCenter(wpm.getWeatherPane());  
	                    break;
	                case DIGIT2:
	                	where = "2";
	                	root.setCenter(ppm.getProgramPane());
	                    break;
	                case DIGIT3:
	                	where = "3";
	                	root.setCenter(webpm.getWebPane());
	                    break;
	                default:
	                    break;
	            }
	        }};
	        nextHandler = new EventHandler<KeyEvent>() {
		        @Override
		        public void handle(KeyEvent ke) {

		            switch (ke.getCode()) {
			            case LEFT:
		                	if(where.equals("1")) {
		                		if(wpm.WeatherNumber != 0) {
		                			wpm.WeatherNumber--;
		                			wpm.mainWeather();
		        		    	}
		                	}
		                    break;
		                case RIGHT:
		                	if(where.equals("1")) {
		                		if(wpm.WeatherNumber < 9) {
		                			wpm.WeatherNumber++;
		                			wpm.mainWeather();
		        		    	}
		                	}
		                    break;
		                case X:
		                	root.removeEventHandler(KeyEvent.KEY_PRESSED, nextHandler);
		                	root.addEventHandler(KeyEvent.KEY_PRESSED, mainHandler);
		                    break;
		                case DIGIT1:
		                    if(where.equals("2"))
		                    	ppm.execute(0);
		                    break;
		                case DIGIT2:
		                	if(where.equals("2"))
		                    	ppm.execute(1);
		                    break;
		                case DIGIT3:
		                	if(where.equals("2"))
		                    	ppm.execute(2);
		                	break;
		                default:
		                    break;
		            }
		        }};

	   root.addEventHandler(KeyEvent.KEY_PRESSED, mainHandler);
	}

	public void buildTop() {
		HBox hb = new HBox();
		Text info = new Text("Welcome to QStarter!");
		info.setId("info");
		hb.getChildren().add(info);
		hb.setAlignment(Pos.TOP_CENTER);
		hb.setPadding(new Insets(7, 0, 7, 0));
		hb.setId("info");
		root.setTop(hb);
	}

	public static void main(String[] args) {
		launch(args);
	}
}