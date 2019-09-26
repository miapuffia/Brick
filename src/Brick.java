import java.awt.MouseInfo;
import java.awt.Robot;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Brick extends Application {
	private boolean gameLoopRunning = false;
	private boolean firstRun = true;
	private int width = 176 * 2;
	private int height = 132 * 2;
	private int round = 1;
	
	private Group ballGroup = new Group();
	private Group brickGroup = new Group();
	private Group mainGroup = new Group();
	private Scene scene;

	Rectangle playerRect = new Rectangle();
	private Circle ball;
	Label scoreLabel = new Label("0");
	
	private double hVelocity = 0.0;
	private double vVelocity = 0.0;
	
	public static void main(String[] args) {
		Application.launch(args);
	}

	public void start(Stage primaryStage) throws Exception {
		Robot robot = new Robot(); 
		
		Kernel32.SYSTEM_POWER_STATUS batteryStatus = new Kernel32.SYSTEM_POWER_STATUS();
		Kernel32.INSTANCE.GetSystemPowerStatus(batteryStatus);
		
		final long startNanoTime = System.nanoTime();

		AnimationTimer gameLoop = new AnimationTimer() {
			public void handle(long currentNanoTime) {
				double t = (currentNanoTime - startNanoTime) / 1_000_000_000.0;
				
				if(Math.abs(vVelocity) <= 8) {
					vVelocity += (vVelocity / Math.abs(vVelocity)) * t * 0.00005;
				}
				
				//Left wall collision
				if(ball.getLayoutX() - ball.getRadius() <= 0) {
					hVelocity = Math.abs(hVelocity);
				}
				//Right wall collision
				else if(ball.getLayoutX() + ball.getRadius() >= width) {
					hVelocity = -1 * Math.abs(hVelocity);
				}
				
				ball.setLayoutX(ball.getLayoutX() + hVelocity);
				
				//Top wall collision
				if(ball.getLayoutY() - ball.getRadius() <= 32) {
					vVelocity = Math.abs(vVelocity);
				}
				//Bottom wall collision
				else if(ball.getLayoutY() + ball.getRadius() >= height) {
					mainGroup.getChildren().remove(ball);
					
					if(ballGroup.getChildren().size() == 0) {
						scene.setCursor(Cursor.DEFAULT);
						
						scene.setOnMouseClicked(e -> {});
						scene.setOnMouseMoved(e -> {});
						scene.setOnMouseExited(e -> {});
						
						return;
					}
					
					Platform.runLater(() -> {
						try {
							Thread.sleep(1000);
						} catch(InterruptedException e) {}
						
						replaceBall();
					});
				}
				
				ball.setLayoutY(ball.getLayoutY() + vVelocity);
				
				detectCollision();
			}
		};
		
		Rectangle topBarRectangle = new Rectangle(width, 32);
		topBarRectangle.setFill(new LinearGradient(topBarRectangle.getWidth(), 0, topBarRectangle.getWidth(), topBarRectangle.getHeight(), false, CycleMethod.NO_CYCLE, new Stop(0, Color.GHOSTWHITE), new Stop(0.9, Color.GHOSTWHITE), new Stop(1, Color.GREY)));
		
		LinearGradient lg = new LinearGradient(7, 0, 7, 20, false, CycleMethod.NO_CYCLE, new Stop(0, Color.DODGERBLUE), new Stop(0.4, Color.CYAN), new Stop(0.6, Color.CYAN), new Stop(1, Color.DODGERBLUE));

		Polygon playPolygon = new Polygon(new double[] {
			0, 0,
			20, 10,
			0, 20
		});
		playPolygon.setFill(lg);
		
		Rectangle pauseRectangle1 = new Rectangle(lg.getEndX(), lg.getEndY());
		pauseRectangle1.setFill(lg);
		
		Rectangle pauseRectangle2 = new Rectangle(lg.getEndX(), lg.getEndY());
		pauseRectangle2.setLayoutX(13);
		pauseRectangle2.setFill(lg);
		
		Group pauseGroup = new Group(pauseRectangle1, pauseRectangle2);
		
		StackPane iconStackPane = new StackPane(pauseGroup);
		iconStackPane.setLayoutY(6);
		iconStackPane.setLayoutX(10);
		
		ballGroup.setLayoutX(60);
		ballGroup.setLayoutY(16);
		
		for(int i = 0; i < 3; i++) {
			ballGroup.getChildren().add(getBall());
		}
		
		scoreLabel.setFont(new Font(30));
		
		StackPane scoreStackPane = new StackPane(scoreLabel);
		scoreStackPane.setLayoutY(-7);
		scoreStackPane.setLayoutX(width - 215);
		scoreStackPane.setPrefWidth(150);
		scoreStackPane.setAlignment(Pos.CENTER_RIGHT);
		
		Rectangle batteryOuter = new Rectangle(40, 20);
		batteryOuter.setFill(new LinearGradient(batteryOuter.getWidth(), 0, batteryOuter.getWidth(), batteryOuter.getHeight(), false, CycleMethod.NO_CYCLE, new Stop(0, Color.GREY), new Stop(1, Color.LIGHTGREY)));
		batteryOuter.setStrokeWidth(1);
		batteryOuter.setStroke(Color.BLACK);
		
		Rectangle batteryNotch = new Rectangle(4, 6);
		batteryNotch.setX(batteryOuter.getWidth());
		batteryNotch.setY((batteryOuter.getHeight() - batteryNotch.getHeight()) / 2);
		batteryNotch.setWidth(4);
		batteryNotch.setHeight(6);
		batteryNotch.setFill(Color.TRANSPARENT);
		batteryNotch.setStrokeWidth(1);
		batteryNotch.setStroke(Color.BLACK);
		
		Rectangle batteryInner = new Rectangle(batteryOuter.getX() + 1, 1, (batteryOuter.getWidth() * batteryStatus.BatteryLifePercent * 0.01) - 1, batteryOuter.getHeight() - 2);
		batteryInner.setFill(new LinearGradient(batteryOuter.getWidth(), 0, batteryOuter.getWidth(), batteryOuter.getHeight(), false, CycleMethod.NO_CYCLE, new Stop(0, Color.LAWNGREEN), new Stop(0.3, Color.LAWNGREEN), new Stop(1, Color.LIMEGREEN)));
		
		Group batteryGroup = new Group(batteryOuter, batteryNotch, batteryInner);
		
		batteryGroup.setLayoutY(6);
		batteryGroup.setLayoutX(width - (batteryOuter.getWidth() + batteryNotch.getWidth()) - 4);
		
		mainGroup.getChildren().addAll(topBarRectangle, playerRect, iconStackPane, ballGroup, scoreStackPane, batteryGroup, brickGroup);
		
		placeBricks(0);
		
		scene = new Scene(mainGroup, width, height);
		scene.setFill(Color.PALETURQUOISE);
		
		playerRect.setWidth(64);
		playerRect.setHeight(9);
		playerRect.setX(0);
		playerRect.setY(scene.getHeight() - playerRect.getHeight());

		scene.setOnMouseClicked(e -> {
			if(gameLoopRunning) {
				gameLoopRunning = false;
				gameLoop.stop();
				scene.setCursor(Cursor.DEFAULT);
				iconStackPane.getChildren().clear();
				iconStackPane.getChildren().add(pauseGroup);
			} else {
				if(firstRun) {
					firstRun = false;
					
					replaceBall();
				}
				
				gameLoopRunning = true;
				gameLoop.start();
				scene.setCursor(Cursor.NONE);
				iconStackPane.getChildren().clear();
				iconStackPane.getChildren().add(playPolygon);
			}
		});
		
		//These setOnMouseMoved and setOnMouseExited methods make escaping the window very difficult
		scene.setOnMouseMoved(e -> {
			if(gameLoopRunning) {
				if(e.getSceneX() < scene.getWidth() - playerRect.getWidth()) {
					playerRect.setX(e.getSceneX());
				} else {
					playerRect.setX(scene.getWidth() - playerRect.getWidth());
				}
				
				robot.mouseMove((int) MouseInfo.getPointerInfo().getLocation().getX(), (int) (primaryStage.getY() + (scene.getWidth() / 2)));
			}
		});
		
		scene.setOnMouseExited(e -> {
			if(gameLoopRunning) {
				if(MouseInfo.getPointerInfo().getLocation().getX() < primaryStage.getX() + 8) {
					robot.mouseMove((int) primaryStage.getX() + 8, (int) (primaryStage.getY() + (scene.getWidth() / 2)));
				} else if(MouseInfo.getPointerInfo().getLocation().getX() > primaryStage.getX() + scene.getWidth()) {
					robot.mouseMove((int) (primaryStage.getX() + scene.getWidth()), (int) (primaryStage.getY() + (scene.getWidth() / 2)));
				}
				
				if(MouseInfo.getPointerInfo().getLocation().getY() < primaryStage.getY() + (scene.getWidth() / 2)) {
					robot.mouseMove((int) MouseInfo.getPointerInfo().getLocation().getX(), (int) (primaryStage.getY() + (scene.getWidth() / 2)));
				} else if(MouseInfo.getPointerInfo().getLocation().getY() > primaryStage.getY() + scene.getHeight()) {
					robot.mouseMove((int) MouseInfo.getPointerInfo().getLocation().getX(), (int) (primaryStage.getY() + (scene.getWidth() / 2)));
				}
			}
		});
		
		primaryStage.setScene(scene);
		primaryStage.setTitle("Brick");
		primaryStage.setResizable(false);
		primaryStage.setAlwaysOnTop(true);
		primaryStage.initStyle(StageStyle.UTILITY);
		primaryStage.show();
	}
	
	private void placeBricks(int round) {
		Color currentColor;
		DropShadow ds;
		
		round %= 6;
		
		switch(round) {
			case 5:
				currentColor = Color.BLUEVIOLET;
				ds = new DropShadow(BlurType.THREE_PASS_BOX, ColorConverter.brighter(currentColor), 1, 1, -1, -1);
				ds.setInput(new DropShadow(BlurType.THREE_PASS_BOX, currentColor.darker(), 1, 1, 1, 1));
				
				for(int i = 0; i < 8; i++) {
					Rectangle brick = new Rectangle(40, 9, currentColor);
					brick.setX(2 + (44 * i));
					brick.setY(120);
					brick.setEffect(ds);
					
					brickGroup.getChildren().add(brick);
				}
			
			case 4:
				currentColor = Color.BLUE;
				ds = new DropShadow(BlurType.THREE_PASS_BOX, ColorConverter.brighter(currentColor), 1, 1, -1, -1);
				ds.setInput(new DropShadow(BlurType.THREE_PASS_BOX, currentColor.darker(), 1, 1, 1, 1));
				
				for(int i = 0; i < 8; i++) {
					Rectangle brick = new Rectangle(40, 9, currentColor);
					brick.setX(2 + (44 * i));
					brick.setY(104);
					brick.setEffect(ds);
					
					brickGroup.getChildren().add(brick);
				}
			
			case 3:
				currentColor = Color.DEEPSKYBLUE;
				ds = new DropShadow(BlurType.THREE_PASS_BOX, ColorConverter.brighter(currentColor), 1, 1, -1, -1);
				ds.setInput(new DropShadow(BlurType.THREE_PASS_BOX, currentColor.darker(), 1, 1, 1, 1));
				
				for(int i = 0; i < 8; i++) {
					Rectangle brick = new Rectangle(40, 9, currentColor);
					brick.setX(2 + (44 * i));
					brick.setY(88);
					brick.setEffect(ds);
					
					brickGroup.getChildren().add(brick);
				}
			
			case 2:
				currentColor = Color.RED;
				ds = new DropShadow(BlurType.THREE_PASS_BOX, ColorConverter.brighter(currentColor), 1, 1, -1, -1);
				ds.setInput(new DropShadow(BlurType.THREE_PASS_BOX, currentColor.darker(), 1, 1, 1, 1));
				
				for(int i = 0; i < 8; i++) {
					Rectangle brick = new Rectangle(40, 9, currentColor);
					brick.setX(2 + (44 * i));
					brick.setY(72);
					brick.setEffect(ds);
					
					brickGroup.getChildren().add(brick);
				}
			
			case 1:
				currentColor = Color.ORANGE;
				ds = new DropShadow(BlurType.THREE_PASS_BOX, ColorConverter.brighter(currentColor), 1, 1, -1, -1);
				ds.setInput(new DropShadow(BlurType.THREE_PASS_BOX, currentColor.darker(), 1, 1, 1, 1));
				
				for(int i = 0; i < 8; i++) {
					Rectangle brick = new Rectangle(40, 9, currentColor);
					brick.setX(2 + (44 * i));
					brick.setY(56);
					brick.setEffect(ds);
					
					brickGroup.getChildren().add(brick);
				}
			
			case 0:
				currentColor = Color.LIMEGREEN;
				ds = new DropShadow(BlurType.THREE_PASS_BOX, ColorConverter.brighter(currentColor), 1, 1, -1, -1);
				ds.setInput(new DropShadow(BlurType.THREE_PASS_BOX, currentColor.darker(), 1, 1, 1, 1));
				
				for(int i = 0; i < 8; i++) {
					Rectangle brick = new Rectangle(40, 9, currentColor);
					brick.setX(2 + (44 * i));
					brick.setY(40);
					brick.setEffect(ds);
					
					brickGroup.getChildren().add(brick);
				}
		}
	}
	
	private Circle getBall() {
		Circle ball = new Circle(7);
		ball.setLayoutX(20 * ballGroup.getChildren().size());
		ball.setFill(new RadialGradient(0, 0, 0, -3, 7, false, CycleMethod.NO_CYCLE, new Stop(0, Color.WHITE), new Stop(1, Color.BLACK)));
		
		return ball;
	}
	
	private void detectCollision() {
		Node collisionNode = null;
		
		//Brick collision
		for(Node brick : brickGroup.getChildren()) {
			//The top/bottom collision detection uses a different algorithm for the second and third clause than the left/right collision detection but it seems to work well for now
			//Detect top collision
			if(
				ball.getLayoutY() + ball.getRadius() >= ((Rectangle) brick).getY()
				&& ball.getLayoutY() - ball.getRadius() < ((Rectangle) brick).getY() + ((Rectangle) brick).getHeight()
				&& ball.getLayoutX() - ball.getRadius() > ((Rectangle) brick).getX()
				&& ball.getLayoutX() + ball.getRadius() < ((Rectangle) brick).getX() + ((Rectangle) brick).getWidth()
			) {
				ball.setLayoutY(ball.getLayoutY() - 1);
				vVelocity *= -1;
				collisionNode = brick;
				break;
			}
			
			//Detect bottom collision
			if (
				ball.getLayoutY() - ball.getRadius() <= ((Rectangle) brick).getY() + ((Rectangle) brick).getHeight()
				&& ball.getLayoutY() + ball.getRadius() > ((Rectangle) brick).getY()
				&& ball.getLayoutX() - ball.getRadius() > ((Rectangle) brick).getX()
				&& ball.getLayoutX() + ball.getRadius() < ((Rectangle) brick).getX() + ((Rectangle) brick).getWidth()
			) {
				ball.setLayoutY(ball.getLayoutY() + 1);
				vVelocity *= -1;
				collisionNode = brick;
				break;
			}
			
			//Detect left collision
			if(
				ball.getLayoutX() + ball.getRadius() >= ((Rectangle) brick).getX()
				&& ball.getLayoutX() - ball.getRadius() < ((Rectangle) brick).getX() + ((Rectangle) brick).getWidth()
				&& (
				(
					((Rectangle) brick).getY() >= ball.getLayoutY() - ball.getRadius()
					&& ((Rectangle) brick).getY() <= ball.getLayoutY() + ball.getRadius()
				) || (
					((Rectangle) brick).getY() + ((Rectangle) brick).getHeight() >= ball.getLayoutY() - ball.getRadius()
					&& ((Rectangle) brick).getY() + ((Rectangle) brick).getHeight() <= ball.getLayoutY() + ball.getRadius()
				)
			)
			) {
				ball.setLayoutX(ball.getLayoutX() - 1);
				hVelocity *= -1;
				collisionNode = brick;
				break;
			}
			
			//Detect right collision
			if (
				ball.getLayoutX() - ball.getRadius() <= ((Rectangle) brick).getX() + ((Rectangle) brick).getWidth()
				&& ball.getLayoutX() + ball.getRadius() > ((Rectangle) brick).getX()
				&& (
					(
						((Rectangle) brick).getY() >= ball.getLayoutY() - ball.getRadius()
						&& ((Rectangle) brick).getY() <= ball.getLayoutY() + ball.getRadius()
					) || (
						((Rectangle) brick).getY() + ((Rectangle) brick).getHeight() >= ball.getLayoutY() - ball.getRadius()
						&& ((Rectangle) brick).getY() + ((Rectangle) brick).getHeight() <= ball.getLayoutY() + ball.getRadius()
					)
				)
			) {
				ball.setLayoutX(ball.getLayoutX() + 1);
				hVelocity *= -1;
				collisionNode = brick;
				break;
			}
		}
		
		//Collision with a brick is detected
		if(collisionNode != null) {
			brickGroup.getChildren().remove(collisionNode);
			scoreLabel.setText((Integer.parseInt(scoreLabel.getText()) + 1) + "");
			
			//No more bricks are left
			if(brickGroup.getChildren().size() == 0) {
				round++;
				hVelocity = 0;
				vVelocity = 0;
				
				ball.setLayoutX((width / 2));
				ball.setLayoutY((height / 2));
				ball.setVisible(false);
				
				Platform.runLater(() -> {
					try {
						Thread.sleep(1000);
					} catch(InterruptedException e) {}
					
					ballGroup.getChildren().add(getBall());
					
					placeBricks(round);
					
					ball.setVisible(true);
					hVelocity = 2;
					vVelocity = -2;
				});
			}
		} else {
			if(vVelocity > 5) {
				vVelocity--;
			}
		}
		
		//Player bar collision
		if(
			ball.getLayoutY() + ball.getRadius() >= playerRect.getY()
			&& ball.getLayoutX() + ball.getRadius() > playerRect.getX()
			&& ball.getLayoutX() - ball.getRadius() < playerRect.getX() + playerRect.getWidth()
		) {
			ball.setLayoutY(ball.getLayoutY() - 1);
			vVelocity *= -1;
			
			double magnitude = (ball.getLayoutX() - playerRect.getX()) / playerRect.getWidth();

			magnitude = ((magnitude * 2) - 1) * 2;
			
			if(hVelocity <= 9) {
				hVelocity += magnitude;
			}
		}
	}
	
	private void replaceBall() {
		ballGroup.getChildren().remove(ballGroup.getChildren().size() - 1);
		
		ball = getBall();
		ball.setLayoutX((width / 2));
		ball.setLayoutY((height / 2));
		
		mainGroup.getChildren().add(ball);
		
		hVelocity = 2;
		vVelocity = -2;
	}
}