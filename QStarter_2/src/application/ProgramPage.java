package application;

import java.awt.Desktop;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Optional;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderStroke;

class ProgramPageMaintain{
	LocationData ld;
	String str;
	BorderPane infoPane;
	File location;

	ProgramPageMaintain(Stage stage) {
		ld = new LocationData();
		this.location = new File("location.txt");
		deSerializing();
		infoPane = new BorderPane();
		infoPane.setBorder(new Border(new BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(1))));
		listOfBtn();
		addLocationBtn(stage);
	}

	public BorderPane getProgramPane() {
		return this.infoPane;
	}

	void serializing() {
		try {
			FileOutputStream fos = new FileOutputStream(this.location);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			ObjectOutputStream out = new ObjectOutputStream(bos);

			out.writeObject(this.ld);
			this.ld.debug();

			out.close();
			listOfBtn();
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	void deSerializing() {
		try {
			FileInputStream fis = new FileInputStream(this.location);
			BufferedInputStream bis = new BufferedInputStream(fis);
			ObjectInputStream in = new ObjectInputStream(bis);

			this.ld = (LocationData) in.readObject();

			this.ld.debug();

			in.close();
		} catch (Exception e) {
			System.out.println(e);
		}

	}

	void finder(Stage stage) {
		try {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open Resource File");
			fileChooser.getExtensionFilters().addAll(
			        new ExtensionFilter("All Files", "*.*"));
			File selectedFile = fileChooser.showOpenDialog(stage);
			if(selectedFile != null) {
				TextInputDialog dialog = new TextInputDialog();
				dialog.setTitle("Text Input Dialog");
				dialog.setHeaderText("Program Modifiy");
				dialog.setContentText("Please enter shortcut name:");

				// Traditional way to get the response value.
				Optional<String> result = dialog.showAndWait();
				if (result.isPresent()){
					ld.addFile(selectedFile, result.get());
					serializing();
				}
			}
		} catch(Exception e) {
			System.out.println(e);
		}
	}

	void listOfBtn() {
		HBox buttonListBox = new HBox(9);
		ArrayList<Button> btns = new ArrayList<>();
		for( int i = 0; i < ld.datas.size(); i++) {
			btns.add(new Button(ld.datas.get(i).name));
			btns.get(i).setMinWidth(110);
			btns.get(i).setMaxWidth(110);
			btns.get(i).setId("listbutton");
		}

		int count = 0;
		int listCount = 0;
		ArrayList<VBox> vb = new ArrayList<>();
		vb.add(new VBox(5));
		for(Button btn : btns) {
			btn.setOnDragDetected(new EventHandler <MouseEvent> () {
			    public void handle(MouseEvent event) {
			        /* drag was detected, start a drag-and-drop gesture*/
			        /* allow any transfer mode */
			    	for(int i = 0; i < ld.datas.size(); i++) {
			    		if(ld.datas.get(i).name.equals(btn.getText())) {
			    			Alert alert = new Alert(AlertType.CONFIRMATION);
			    			alert.setTitle("Remove \"" + ld.datas.get(i).name + "\".");
			    			alert.setHeaderText("Remove The Data.");
			    			alert.setContentText("Are you sure?");

			    			Optional<ButtonType> result = alert.showAndWait();
			    			if (result.get() == ButtonType.OK){
			    				ld.datas.remove(i);
			    				serializing();
			    			}
			    		}
			    	}
			    }
			});

			if(count <= 5) {
				btn.setOnAction(new EventHandler<ActionEvent>() {
				    @Override
				    public void handle(ActionEvent event) {
				    	try {
				    		for(int i = 0; i < ld.datas.size(); i++)
				    			if(ld.datas.get(i).name.equals(btn.getText()))
				    					Desktop.getDesktop().open(ld.datas.get(i).file);
				    	} catch (Exception e) {
				    		System.out.println(e);
				    	}

				    }
				});
				vb.get(listCount).getChildren().add(btn);
			}
			else {
				count = 0;
				listCount++;
				vb.add(new VBox(5));
				btn.setOnAction(new EventHandler<ActionEvent>() {
				    @Override
				    public void handle(ActionEvent event) {
				    	try {
				    		for(int i = 0; i < ld.datas.size(); i++)
				    			if(ld.datas.get(i).name.equals(btn.getText()))
				    					Desktop.getDesktop().open(ld.datas.get(i).file);
				    	} catch (Exception e) {
				    		System.out.println(e);
				    	}

				    }
				});
				vb.get(listCount).getChildren().add(btn);
			}
			count++;
		}
		for(VBox v : vb) {
			buttonListBox.getChildren().addAll(v);
		}
		buttonListBox.setPadding(new Insets(10, 0, 0, 25));
		this.infoPane.setCenter(buttonListBox);
	}

	void addLocationBtn(Stage stage) {
		HBox hb = new HBox();
		Image plusImg = new Image("file:resources/Finder.png");
		ImageView imageView = new ImageView(plusImg);
		imageView.setPreserveRatio(true);
		imageView.setFitHeight(45);
	    imageView.setFitWidth(45);
	    imageView.setOnMousePressed(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent evt) {
            	finder(stage);
            }
	    });

		hb.getChildren().add(imageView);
		hb.setAlignment(Pos.TOP_CENTER);
		hb.setPadding(new Insets(15, 10, 15, 10));

		this.infoPane.setRight(hb);
	}

	public void execute(int i) {
		try {
			if(i < ld.datas.size())
				Desktop.getDesktop().open(ld.datas.get(i).file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}


class LocationData implements Serializable{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public ArrayList<Data> datas;

	LocationData() {
		this.datas = new ArrayList<>();
	}

	public void addFile(File f, String n) {
		datas.add(new Data(f, n));
	}

	public void debug() {
		for(int i = 0; i < datas.size(); i++) {
			System.out.println(datas.get(i).file.toString() + " // " + datas.get(i).name);
		}
	}
}

class Data implements Serializable{
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	public File file;
	public String name;

	Data(File file, String name) {
		this.file = file;
		this.name = name;
	}

	public void add(File file, String name) {
		this.file = file;
		this.name = name;
	}
}

