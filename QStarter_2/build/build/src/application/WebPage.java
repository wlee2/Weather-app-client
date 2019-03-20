package application;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import javafx.util.Pair;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
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
import javafx.stage.Modality;

class WebPageMaintain extends Application{
	WebData wd;
	BorderPane bp;
	BorderPane infoPane;
	File location;

	WebPageMaintain(BorderPane bp) {
		this.bp = bp;
		this.location = new File("webLocation.txt");
		wd = new WebData();
		deSerializing();
		infoPane = new BorderPane();
		infoPane.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(1))));
		listOfWeb();
		addBtn();
	}

	void serializing() {
		try {
			FileOutputStream fos = new FileOutputStream(this.location);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			ObjectOutputStream out = new ObjectOutputStream(bos);

			out.writeObject(this.wd);

			out.close();
			this.wd.debug();
			this.listOfWeb();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	void deSerializing() {
		try {
			FileInputStream fis = new FileInputStream(this.location);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ObjectInputStream in = new ObjectInputStream(bis);

			this.wd = (WebData) in.readObject();

			this.wd.debug();
			in.close();
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	void addBtn() {
		HBox hb = new HBox();
		Image plusImg = new Image("file:resources/www.png");
		ImageView imageView = new ImageView(plusImg);
		imageView.setPreserveRatio(true);
		imageView.setFitHeight(45);
	    imageView.setFitWidth(45);
	    imageView.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent evt) {
            	adding();
            }
	    });

		hb.getChildren().add(imageView);
		hb.setAlignment(Pos.TOP_CENTER);
		hb.setPadding(new Insets(15, 10, 15, 10));

		this.infoPane.setRight(hb);
	}

	void adding() {
		// Create the custom dialog.
		Dialog<Pair<String, String>> dialog = new Dialog<>();
		dialog.setTitle("Add Dialog");
		dialog.setHeaderText("Please, Enter Name and Web Address");

		// Set the icon (must be included in the project).
		dialog.setGraphic(new ImageView(new Image("file:resources/www.png")));

		// Set the button types.
		ButtonType confirmBtnType = new ButtonType("Confirm", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(confirmBtnType, ButtonType.CANCEL);

		// Create the username and password labels and fields.
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));

		TextField shortcutName = new TextField();
		shortcutName.setPromptText("Name");
		TextField address = new TextField();
		address.setPromptText("Address");

		grid.add(new Label("Name:"), 0, 0);
		grid.add(shortcutName, 1, 0);
		grid.add(new Label("Address:"), 0, 1);
		grid.add(address, 1, 1);

		Node confirmBtn = dialog.getDialogPane().lookupButton(confirmBtnType);
		confirmBtn.setDisable(true);

		shortcutName.textProperty().addListener((observable, oldValue, newValue) -> {
			confirmBtn.setDisable(newValue.trim().isEmpty());
		});

		dialog.getDialogPane().setContent(grid);

		Platform.runLater(() -> shortcutName.requestFocus());

		dialog.setResultConverter(dialogButton -> {
		    if (dialogButton == confirmBtnType) {
		        return new Pair<>(shortcutName.getText(), address.getText());
		    }
		    return null;
		});

		Optional<Pair<String, String>> result = dialog.showAndWait();


		result.ifPresent(contents -> {
			if(this.wd.isContain(contents.getKey())) {
				this.errMsg();
			} else {
				this.wd.addData(contents.getKey(), contents.getValue());
				System.out.println("temp = " + contents.getValue());
				serializing();
			}
		});
	}

	void listOfWeb() {
		HBox hb = new HBox(9);
		ArrayList<VBox> vb = new ArrayList<>();
		vb.add(new VBox(5));

		ArrayList<Button> btns = new ArrayList<>();
		for(String name : this.wd.getSet()) {
			btns.add(new Button(name));
		}

		int count = 0;
		int listCount = 0;
		for(Button btn : btns) {
			btn.setMinWidth(110);
			btn.setMaxWidth(110);
			btn.setId("listbutton");
			btn.setOnAction(new EventHandler<ActionEvent>() {
			    @Override
			    public void handle(ActionEvent event) {
			    	try {
			    		String address = wd.getAddressByName(btn.getText());
			    		getHostServices().showDocument(address);
			    	} catch (Exception e) {
			    		System.out.println(e);
			    	}

			    }
			});
			btn.setOnDragDetected(new EventHandler <MouseEvent> () {
			    public void handle(MouseEvent event) {
			    	Alert alert = new Alert(AlertType.CONFIRMATION);
	    			alert.setTitle("Remove \"" + btn.getText() + "\".");
	    			alert.setHeaderText("Remove The Data.");
	    			alert.setContentText("Are you sure?");

	    			Optional<ButtonType> result = alert.showAndWait();
	    			if (result.get() == ButtonType.OK){
	    				wd.remove(btn.getText());
	    				serializing();
	    			}
			    }
			});

			if((count % 5) != 0) {
				vb.get(listCount).getChildren().add(btn);
			}
			else {
				count = 0;
				listCount++;
				vb.add(new VBox(5));
				vb.get(listCount).getChildren().add(btn);
			}
			count++;
		}
		for(VBox v : vb)
			hb.getChildren().add(v);
		hb.setPadding(new Insets(10, 0, 0, 16));
		this.infoPane.setCenter(hb);
	}

	void errMsg() {
		HBox p = new HBox();
		Task<Void> task = new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				// TODO Auto-generated method stub
				updateMessage("Message: Name already exists!");
				Thread.sleep(6000);
				return null;
			}

		};

		Label err = new Label();
		err.setId("error");
		err.textProperty().bind(task.messageProperty());

		Thread thread = new Thread(task);
	    thread.setDaemon(true);
	    thread.start();

	    task.setOnSucceeded(e -> {
	        err.textProperty().unbind();
	        // this message will be seen.
	        bp.setBottom(null);
	      });

		p.getChildren().add(err);
		p.setAlignment(Pos.TOP_CENTER);
		p.setPadding(new Insets(5, 12, 5, 12));
		p.setId("errorpane");
		this.infoPane.setBottom(p);
	}

	public void start(Stage stage) throws Exception {

    }

	public BorderPane getWebPane() {
		return this.infoPane;
	}
}

class WebData implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	Map<String, String> datas;
	WebData() {
		datas = new HashMap<String, String>();
	}

	public void addData(String key, String value) {
		datas.put(key, value);
	}

	public Boolean isContain(String key) {
		return this.datas.containsKey(key);
	}

	public Set<String> getSet() {
		return datas.keySet();
	}

	public String getAddressByName(String key) {
		return datas.get(key);
	}

	public void remove(String key) {
		datas.remove(key);
	}

	public void debug() {
		for ( Map.Entry<String, String> entry : datas.entrySet() ) {
		    System.out.println(" key : " + entry.getKey() +"  value : " + entry.getValue());
		}
	}

}


