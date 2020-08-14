package com.internshala.connectfour;

import com.sun.org.apache.regexp.internal.RE;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {

	private static final int COLUMS = 7;
	private  static  final  int  ROWS = 6;
	private  static  final int CIRCLE_DIAMETER = 80;
	private static  final  String discColor1 = "#24303E";
	private  static  final String discColor2 ="#4CAA88";

	private  static String PLAYER_ONE  = "player one ";
	private  static  String PLAYER_TWO = "player two";

	private  boolean isPlayerOneTurn =  true;  // to decide which one play

	private Disc[][] insertedDiscArray = new Disc[ROWS][COLUMS];  // for structural changes for developers
	@FXML
	public GridPane rootGridPane;
	@FXML
	public Pane insertedDiscPane;
	@FXML
	public Label playerNameLabel;
	@FXML
	public TextField userInputField1 ;
	@FXML
	public TextField userInputField2;
	@FXML
	public Button setBtn;


	private  boolean isAllowedToInsert =true; // Flag to avoid  same color disc Being added .

	public  void createPlayerground(){

	Shape rectangleWithholesoles = createGamestructuralGride();
	rootGridPane.add(rectangleWithholesoles,0,1);

	List<Rectangle> rectangleList = createClickableColums();
	for(Rectangle rectangle : rectangleList){
		rootGridPane.add(rectangle, 0,1);

	}
	//setBtn.setOnAction(event -> {
	//	PLAYER_ONE =userInputField1.getText();
	//	PLAYER_TWO =userInputField2.getText();
	//	playerNameLabel.setText(isPlayerOneTurn? PLAYER_ONE: PLAYER_TWO);
	//});


	}
	private Shape createGamestructuralGride(){
		Shape rectangleWithholes = new Rectangle( (COLUMS + 1 )* CIRCLE_DIAMETER , (ROWS +1) * CIRCLE_DIAMETER);
		for(int row =0; row < ROWS;row++){
			for(int col=0; col < COLUMS; col++){
				Circle circle = new Circle();
				circle.setRadius(CIRCLE_DIAMETER / 2);
				circle.setCenterX(CIRCLE_DIAMETER / 2);
				circle.setCenterY(CIRCLE_DIAMETER/2);
				circle.setSmooth(true);

				circle.setTranslateX(col * (CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER/4);
				circle.setTranslateY( row *(CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER/4);

				rectangleWithholes = Shape.subtract(rectangleWithholes,circle);

			}
		}


		rectangleWithholes.setFill(Color.WHITE);
		return 	rectangleWithholes;

	}
	// to create 7 columns
	private  List<Rectangle>  createClickableColums(){
		List<Rectangle> rectangleList  =new ArrayList<>();
		for (int col= 0;col < COLUMS; col++){
			Rectangle rectangle =new Rectangle(CIRCLE_DIAMETER,(ROWS +1) * CIRCLE_DIAMETER);
			rectangle.setFill(Color.TRANSPARENT);
			rectangle.setTranslateX(col * (CIRCLE_DIAMETER + 5)  + CIRCLE_DIAMETER/4);

			// hover effect
			rectangle.setOnMouseEntered(event ->  rectangle.setFill(Color.valueOf("#eeeeee26")));
			rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));
			//rectangle event
			final int column =col;
			rectangle.setOnMouseClicked(event -> {
				if(isAllowedToInsert) {
                    isAllowedToInsert = false;  // when disc is being dropped then no more disc will be inserted
					insertedDisc(new Disc(isPlayerOneTurn), column);
				}
			});

			rectangleList.add(rectangle);

		}

		return  rectangleList;
	}

	private void insertedDisc(Disc disc, int column){
		

		// to insert a disc at particular position
		int row = ROWS- 1;
		while(row >= 0){
			if(getDiscIfPresent(row,column)== null)  // to check the column is empty or not
				break;
			row--;
		}
		if(row < 0)    // if it is full we cannot insert anymore disc
			return;

		insertedDiscArray[row][column] =disc;    // for structural changes : For developers
         insertedDiscPane.getChildren().add(disc);// for visual Changes : players

         disc.setTranslateX(column * (CIRCLE_DIAMETER + 5)  + CIRCLE_DIAMETER/4);
         int currentRow = row;

		TranslateTransition translateTransition = new TranslateTransition(Duration.seconds(0.5),disc);
         translateTransition.setToY(row *(CIRCLE_DIAMETER + 5) + CIRCLE_DIAMETER/4);
         translateTransition.setOnFinished(event -> {
         	isAllowedToInsert =true; //Finally , when disc is dropped allow player to insert disc .
         	if(gameEnded(currentRow, column)){
         		// to decide to winner
         		gameOver();
         		return;
            }

// to toggel between player 1 and 2
         	isPlayerOneTurn = !isPlayerOneTurn;

			            PLAYER_ONE = userInputField1.getText();
			            PLAYER_TWO = userInputField2.getText();

		         playerNameLabel.setText(isPlayerOneTurn ? PLAYER_ONE: PLAYER_TWO);

         });

         translateTransition.play();
	}
	private boolean gameEnded(int row ,int column){
		//vertical points  A small example Player his  last disc at roe =2, column = 3
		// range of row values will be 1,2,3,4,5
		// index of each element present in column [row ][ column ]: 0,3  1,3   2,3  3,3   4,3   5,3 -> point2D x,y
		List<Point2D> verticalPoints = IntStream.rangeClosed(row -3,row +3)  // range of row values
				.mapToObj(r-> new Point2D(r,column)) // 0,3 ,1,3 2,3 ,3,3 4,3 5,3 -> Point2D
				.collect(Collectors.toList());


		List<Point2D> horizontalPoints = IntStream.rangeClosed(column -3,column+3)  // range of columns  values
				.mapToObj(col-> new Point2D(row,col))
				.collect(Collectors.toList());
		// to check possible diagonal combinations
		Point2D startPoint1 =new Point2D(row-3, column+3);
		List<Point2D> diagonalPoints = IntStream.rangeClosed(0,6)
				.mapToObj(i -> startPoint1.add(i,-i))
				.collect(Collectors.toList());

		Point2D startPoint2 =new Point2D(row-3, column-3);
		List<Point2D> diagonal2Points = IntStream.rangeClosed(0,6)
				.mapToObj(i -> startPoint2.add(i,i))
				.collect(Collectors.toList());

		boolean isEnded = checkCombinations(verticalPoints) || checkCombinations(horizontalPoints)
				             || checkCombinations(diagonalPoints)|| checkCombinations(diagonal2Points);

		return  isEnded ;
	}

	private boolean checkCombinations(List<Point2D> points) {
		int chain = 0;
		for (Point2D point : points) {
			int rowIndexTorArray = (int) point.getX();
			int columnIndexToArray = (int) point.getY();

			Disc disc = getDiscIfPresent(rowIndexTorArray,columnIndexToArray);

			if (disc != null && disc.isPlayerOneMove == isPlayerOneTurn) {   // if the last inserted Dics belongs to the current player
				chain++;
				if (chain == 4) {
					return true;
				}
			}else {
					chain = 0;
				}

			}
			return false;
		}
		private  Disc getDiscIfPresent(int row ,int column){ //   to prevent ArrayIndexOUtOfBoundrxception
		if(row >= ROWS || row < 0 || column >= COLUMS  || column < 0)
			return null;

		return insertedDiscArray[row][column];
		}

	private void gameOver(){
		String winner = isPlayerOneTurn? PLAYER_ONE : PLAYER_TWO;
		System.out.println(" winner is: "+ winner);

		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Connect Four");
		alert.setHeaderText("The Winner is " + winner);
		alert.setContentText("Want play Again ");
		ButtonType yesBtn = new ButtonType("Yes");
		ButtonType noBtn = new ButtonType("No, Exit");
		alert.getButtonTypes().setAll(yesBtn,noBtn);


       Platform.runLater( () ->{    // ensures all enclosing code after all animation ended
	       Optional<ButtonType> btnClicked = alert.showAndWait();
	       if(btnClicked.isPresent() && btnClicked.get() == yesBtn){
		       resetGame();               // ...user chose yes so reset the game
	       }else{
		       Platform.exit();
		       System.exit(0);  // to shut down all Threads
		       //...user chose NO.. so exit the game
	       }
       });
	}
	 public void resetGame() {
		insertedDiscPane.getChildren().clear();
		for (int row= 0; row < insertedDiscArray.length;row++){
			for(int col =0; col < insertedDiscArray[row ].length;col++){
				insertedDiscArray[row][col] = null;
			}
		}// Remove all Inserted the Disc from  Pane
		isPlayerOneTurn =true;   // let player  start the game
		playerNameLabel.setText(PLAYER_ONE);

		createPlayerground();  // Prepare a fresh playground
	}

	// this class to set the color to discs
	private static class Disc extends Circle{
		private final  boolean isPlayerOneMove;
		public  Disc(boolean isPlayerOneMove){
			this.isPlayerOneMove = isPlayerOneMove;
			setRadius(CIRCLE_DIAMETER /2);
			setFill(isPlayerOneMove ? Color.valueOf(discColor1): Color.valueOf(discColor2));
			setCenterX(CIRCLE_DIAMETER /2);
			setCenterY(CIRCLE_DIAMETER /2);
		}

	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}

}

