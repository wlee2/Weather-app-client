package application;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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
import javafx.util.Duration;


class WeatherPageMaintain extends Task<Object>{
	BorderPane bp;
	WDataArray wd;
	public boolean threadLoop;
	Socket socket;
	ObjectInputStream is;
	ObjectOutputStream os;
	int WeatherNumber;
	BorderPane infoPane;
	public Date updatedTime;
	String otherAddress;

	WeatherPageMaintain(BorderPane root) {
		this.otherAddress = "";
		this.bp = root;
		this.infoPane = new BorderPane();
		this.infoPane.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(1))));
		wd = new WDataArray();
		threadLoop = true;
		this.WeatherNumber = 0;
		updatedTime = new Date();
		updateTimer();
		mainWeather();
		buildButton();
	}

	public int getStatus() {
		int returnIndex = 0;
		try {
			File file = new File("resources/data/setting.txt");
			if(file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));

				String line = "";

				line = br.readLine();
				int index = line.indexOf("=");
				String option = line.substring(index+1);

				line = br.readLine();
				index = line.indexOf("=");
				this.otherAddress = line.substring(index+1);

				if(option.equals("0"))
					returnIndex = 0;
				else if(option.equals("1"))
					returnIndex = 1;
				else if(option.equals("2"))
					returnIndex = 2;
				else {
					fixOption(0);
				}
				br.close();
			}
		} catch (Exception e){
			System.out.println(e);
		}
		return returnIndex;
	}
	
	public void fixOption(int index) {
		File file = new File("resources/data/setting.txt");
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(file));
			bw.write("option=" + index);
			bw.newLine();
			bw.write("otherAddress=0.0.0.0");
			bw.close();
		} catch(Exception e) {
			System.out.println(e);
		}
	}

	@Override
	protected Void call() throws Exception {
		// TODO Auto-generated method stub
		int option = this.getStatus();
		while(true) {
			try {
				if(option == 0)
					socket = new Socket("woosenecac.com", 8750);
				else if(option == 1)
					socket = new Socket("192.168.2.15", 8750);
				else
					socket = new Socket(this.otherAddress, 8750);
				is = new ObjectInputStream(socket.getInputStream());
				os = new ObjectOutputStream(socket.getOutputStream());
				//String str = "this user is connecting with you";
				//os.writeObject(str);
				while((wd = (WDataArray) is.readObject()) != null) {
					for(WData temp : wd.dataList)
						temp.Debug();
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							mainWeather();
							updatedTime = new Date();
						}
					});
				}

				socket.close();
				if(this.threadLoop == false)
					break;
			} catch (Exception e) {
				System.out.println(e);
				wd = new WDataArray();
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						mainWeather();
						updatedTime = new Date();
					}
				});
			}
			try {
				Thread.sleep(5 * 1000);
				if(option == 0){
					option = 1;
					this.fixOption(1);
				}
				else {
					option = 0;
					this.fixOption(0);
				}
			} catch(Exception e) {

			}
		}
		return null;

	}

	public void updateTimer() {
		HBox hb = new HBox();
		Text time = new Text();
		Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
			LocalDateTime now = LocalDateTime.now();
		    Instant instant = now.atZone(ZoneId.systemDefault()).toInstant();
			Date date = Date.from(instant);
			long diff = date.getTime() - updatedTime.getTime();
			long secondD = 300 - TimeUnit.MILLISECONDS.toSeconds(diff);
	        time.setText("update: "+ secondD + " seconds left.");
	    }),
	         new KeyFrame(Duration.seconds(1))
	    );

	    clock.setCycleCount(Animation.INDEFINITE);
	    clock.play();

	    hb.getChildren().addAll(time);
	    time.setId("timer");
	    hb.setAlignment(Pos.CENTER);
	    hb.setPadding(new Insets(10, 50, 10, 0));
	    this.infoPane.setBottom(hb);
	}

	public void buildButton() {
		Image next = new Image("file:resources/right-arrow.png");
		Image nextPressed = new Image("file:resources/right-selected.png");
		ImageView nextImg = new ImageView(next);
		nextImg.setPreserveRatio(true);
		nextImg.setFitHeight(35);
		nextImg.setFitWidth(35);
		nextImg.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent evt) {
            	nextImg.setImage(nextPressed);
            	if(WeatherNumber < (wd.size() - 1)) {
		    		WeatherNumber++;
		    		mainWeather();
		    	}
            }
        });
		nextImg.setOnMouseReleased(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent evt) {
            		nextImg.setImage(next);

            }
        });


		Image before = new Image(("file:resources/left-arrow.png"));
		Image beforePressed = new Image(("file:resources/left-selected.png"));
		ImageView beforeImg = new ImageView(before);
		beforeImg.setPreserveRatio(true);
		beforeImg.setFitHeight(35);
		beforeImg.setFitWidth(35);
		beforeImg.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent evt) {
            	beforeImg.setImage(beforePressed);
            	if(WeatherNumber != 0) {
		    		WeatherNumber--;
		    		mainWeather();
		    	}
            }
        });
		beforeImg.setOnMouseReleased(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent evt) {
            	beforeImg.setImage(before);
            }
        });


		VBox b1 = new VBox();
		b1.getChildren().add(nextImg);
		b1.setAlignment(Pos.TOP_CENTER);
		b1.setPadding(new Insets(80, 10, 0, 10));
		VBox b2 = new VBox();
		b2.getChildren().add(beforeImg);
		b2.setAlignment(Pos.TOP_CENTER);
		b2.setPadding(new Insets(80, 10, 0, 10));

		this.infoPane.setLeft(b2);
		this.infoPane.setRight(b1);
	}

	public void mainWeather() {
		try {
			if(WeatherNumber >= (wd.size()))
				WeatherNumber = wd.size() - 1;
			HBox[] hb = new HBox[5];
			VBox vb = new VBox(8);
			Text city = new Text("Toronto , CA");
			city.setId("contents");

			Image image = new Image(("file:resources/" + wd.getCondition(WeatherNumber) + ".png"));
			ImageView imageView = new ImageView(image);
			imageView.setPreserveRatio(true);
			imageView.setFitHeight(100);
		    imageView.setFitWidth(100);
			Text condition = new Text("Condition: " + wd.getCondition(WeatherNumber));
			condition.setId("contents");
			Text description = new Text("Description: " + wd.getDescription(WeatherNumber));
			description.setId("contents");
			Text temp_max = new Text("Temp Max/Avg/Min: " + wd.getTemp_Max(WeatherNumber));
			temp_max.setId("contents");
			Text temp_avg = new Text("/ " + wd.getTemp(WeatherNumber));
			temp_avg.setId("contents");
			Text temp_min = new Text("/ " + wd.getTemp_Min(WeatherNumber));
			temp_min.setId("contents");
			Text date = new Text("Update date: " + wd.getDate(WeatherNumber));
			date.setId("contents");

			hb[0] = new HBox();
			hb[0].getChildren().addAll(city);

			hb[1] = new HBox();
			hb[1].setSpacing(10);
			if(image.isError()) {
				hb[1].getChildren().addAll(condition);
			}
			else {
				hb[1].getChildren().addAll(imageView);
			}

			hb[2] = new HBox();
			hb[2].setSpacing(10);
			hb[2].getChildren().addAll(description);

			hb[3] = new HBox();
			hb[3].setSpacing(10);
			hb[3].getChildren().addAll(temp_max, temp_avg, temp_min);

			hb[4] = new HBox();
			hb[4].setSpacing(10);
			hb[4].getChildren().addAll(date);
			hb[4].setAlignment(Pos.CENTER);
			hb[4].setPadding(new Insets(10, 50, 10, 0));

			vb.getChildren().addAll(hb[0], hb[1], hb[2], hb[3]);
			vb.setPadding(new Insets(10, 12, 15, 40));
			vb.setAlignment(Pos.TOP_CENTER);
			this.infoPane.setCenter(vb);
			this.infoPane.setTop(hb[4]);
			if(wd.getCondition(WeatherNumber) == null)
				errControl();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	public void errControl() {
		HBox errBox = new HBox(10);
		Text err = new Text("Error: " + wd.dataList.get(WeatherNumber).err);
		err.setId("error");
		errBox.getChildren().add(err);
		errBox.setAlignment(Pos.CENTER);
		errBox.setPadding(new Insets(10, 50, 10, 0));
		this.infoPane.setTop(errBox);
	}

	public BorderPane getWeatherPane() {
		return this.infoPane;
	}
}


class WDataArray implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public ArrayList<WData> dataList;

	WDataArray() {
		dataList = new ArrayList<>();
		this.dataList.add(new WData());
	}

	public void add(WData wd) {
		this.dataList.add(wd);
	}

	public void Debug(int i) {
		this.dataList.get(i).Debug();
	}
	public int size() {
		return this.dataList.size();
	}

	public String getCondition(int i) {
		return this.dataList.get(i).getCondition();
	}
	public String getDescription(int i) {
		return this.dataList.get(i).getDescription();
	}
	public String getTemp(int i) {
		return this.dataList.get(i).getTemp();
	}
	public String getTemp_Min(int i) {
		return this.dataList.get(i).getTemp_Min();
	}
	public String getTemp_Max(int i) {
		return this.dataList.get(i).getTemp_Max();
	}
	public String getDate(int i) {
		return this.dataList.get(i).getDate();
	}

}

class WData implements Serializable {
	private static final long serialVersionUID = 1454313033318093811L;
	String condition;
	String description;
	String temp_min;
	String temp_normal;
	String temp_max;
	String date;
	String err;

	WData() {
		this.condition = null;
		this.description = null;
		this.temp_min = null;
		this.temp_normal = null;
		this.temp_max = null;
		this.date = null;
		this.err = "Server is closed: Sorry, it can't be solved!";
	}

	public void Set(String condition, String description, String temp_max, String temp_min, String temp_normal, String date, String err) {
		this.condition = condition;
		this.description = description;
		this.temp_max = temp_max;
		this.temp_min = temp_min;
		this.temp_normal = temp_normal;
		this.date = date;
		this.err = err;
	}

	public String getCondition() {
		return this.condition;
	}
	public String getDescription() {
		return this.description;
	}
	public String getTemp() {
		return this.temp_normal;
	}
	public String getTemp_Min() {
		return this.temp_min;
	}
	public String getTemp_Max() {
		return this.temp_max;
	}
	public String getDate() {
		return this.date;
	}

	public void Debug() {
			System.out.println("Date: " + this.date);
			System.out.println("Condition: " + this.condition);
			System.out.println("Description: " + this.description);
			System.out.println("Temp Max/Avg/Min: " + this.temp_max + " / " + this.temp_normal + " / " + this.temp_min);
	}
}
