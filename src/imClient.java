import java.io.*;
import java.net.*;
import java.util.*;

import javafx.application.*;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.*;
import javafx.event.*;
import javafx.geometry.*;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.stage.*;

public class imClient extends Application{
	
	private ServerTask serverTask;
	
	private String userName;
	private String buddyName;
	private String serverHost;
	private static final int PORT_NUMBER = 26666;
	private static final String LOCALHOST = "localhost";
	private static final String LOCAL_NETWORK_ADDRESS_1 = "";
	private static final String LOCAL_NETWORK_ADDRESS_2 = "";
	private static final String IP_ADDRESS = "73.231.101.85";
	
	private VBox mainBox;
	private HBox loginBox;
	private Label userLabel;
	private TextField userNameField;
	private Button loginButton;
	private Button logoutButton;
	private HBox buddyBox;
	private Label buddyLabel;
	private TextField buddyNameField;
	private Button buddyButton;
	private FlowPane textPane;
	private ScrollPane scrollPane;
	private StackPane stackPane;
	private Text chatText;
	private TextField typeText;
	private Button sendButton;
	private HBox ipConfigBox;
	private Label ipConfigLabel;
	private ComboBox<String> ipConfigCB;
	
	
	
	public void start(Stage primaryStage)
	{
		System.setProperty("glass.accessible.force", "false");
		//serverHost = "73.231.101.85";
		serverHost = LOCALHOST;
		userName = "";
		
		
		
		mainBox = new VBox();
		mainBox.setStyle("-fx-background-color: null;");
		
		loginBox = new HBox();
		loginBox.setAlignment(Pos.CENTER_LEFT);
		userLabel = new Label("Username: ");
		userNameField = new TextField();
		loginButton = new Button("Log in");
		loginButton.setOnAction(this::login);
		logoutButton = new Button("Log out");
		logoutButton.setOnAction(this::logout);
		loginBox.getChildren().addAll(userLabel, userNameField, loginButton, logoutButton);
		loginBox.setStyle("-fx-background-color: null;");
		
		buddyBox = new HBox();
		buddyBox.setAlignment(Pos.CENTER_LEFT);
		buddyLabel = new Label("Friend: ");
		buddyNameField = new TextField();
		buddyNameField.textProperty().addListener((obs, oldText, newText) -> {
		    buddyName = newText;
		    // ...
		});
		buddyBox.getChildren().addAll(buddyLabel, buddyNameField);
		buddyBox.setStyle("-fx-background-color: null;");
		
		stackPane = new StackPane();
		stackPane.setStyle("-fx-background-color: null;");
		
		chatText = new Text();
		chatText.setStyle("-fx-background-color: null;");
		//chatText.setFill(Color.ANTIQUEWHITE);
		//chatText.setStroke(Color.ANTIQUEWHITE);
		//chatText.setFont(Font.font("Fartz", FontWeight.BOLD, 20));
		textPane = new FlowPane();
		textPane.setStyle("-fx-background-color: null;");
		scrollPane = new ScrollPane();
		scrollPane.setStyle("-fx-background: transparent;");

		
		chatText.wrappingWidthProperty().bind(textPane.widthProperty());
		textPane.getChildren().add(chatText);
		textPane.setPrefSize(480, 300);
		
		
		scrollPane.setContent(textPane);
		scrollPane.vvalueProperty().bind(textPane.heightProperty());
		
		//Image image = new Image( "resources/" , 500, 500, true, true);
		ImageView imageView = new ImageView();
		
		
		imageView.setViewport(new Rectangle2D(0, 0, 500, 300));
		imageView.setPreserveRatio(true);
		
		FlowPane screenPane = new FlowPane();
		screenPane.setMinSize(500, 300);
		screenPane.setOpacity(50);
		screenPane.setStyle("-fx-background-color: rgba(255, 255, 255, 1.0);");



		//imageView.setClip(clippingPane);
		stackPane.setPrefHeight(302);
		stackPane.setMaxHeight(302);
		
		stackPane.getChildren().addAll(imageView, screenPane, scrollPane);

		
		typeText = new TextField();
		typeText.setOnAction(this::sendMessage);
		
		sendButton = new Button("Send");
		sendButton.setOnAction(this::sendMessage);
		
		
		ipConfigBox = new HBox();
		ipConfigBox.setAlignment(Pos.CENTER_LEFT);
		ipConfigLabel = new Label("IP settings: ");
		ipConfigCB = new ComboBox<>();
		ipConfigCB.getItems().addAll("My PC", "Network 1", "Network 2", "Internet");
		ipConfigCB.getSelectionModel().selectFirst();
		ipConfigCB.getSelectionModel().selectedItemProperty().addListener(this::selectIP);
		ipConfigBox.getChildren().addAll(ipConfigLabel, ipConfigCB);
		
		mainBox.getChildren().addAll(loginBox, buddyBox, stackPane, typeText, sendButton, ipConfigBox);
		
		Scene scene = new Scene(mainBox, 500, 470, Color.ALICEBLUE);
		scene.getStylesheets().add("scrollBarFix.css");

		primaryStage.setTitle("Maim");
		primaryStage.setScene(scene);
		primaryStage.setOnCloseRequest(this::closeWindow);
		primaryStage.show();
	}
	
	
	
	
	public void login(ActionEvent event)
	{
		if(!userNameField.getText().trim().equals(""))
		{
			userNameField.setEditable(false);
			loginButton.setDisable(true);
			userName = userNameField.getText().trim();
			userNameField.setText(userName);
			chatText.setText(chatText.getText() + "Logging in as " + userName + "\n" );
			startClient();
		}
		
		
	}
	
	public void startClient()
	{
		try
		{
			Socket socket = new Socket(serverHost, PORT_NUMBER);
			
			Thread.sleep(1000);
			serverTask = new ServerTask(socket);
			Thread serverThread = new Thread(serverTask);
	        serverThread.setDaemon(true);
	        serverThread.start();
			
		}
		catch(IOException ex)
		{
			chatText.setText(chatText.getText() + "Server connection failed!\n");
			loginButton.setDisable(false);
			userNameField.setEditable(true);
		}
		catch(InterruptedException ex)
		{
			chatText.setText(chatText.getText() + "Server connection failed!\n");
			loginButton.setDisable(false);
			userNameField.setEditable(true);
		}

	}
	
	public void logout(ActionEvent event)
	{
		serverTask.closeSocket();
		loginButton.setDisable(false);
		userNameField.setEditable(true);
		chatText.setText(chatText.getText() + "Logged out!\n");
	}
	public void closeWindow(WindowEvent event)
	{
		if(serverTask != null)
		{
			serverTask.closeSocket();
		}
	}
	
	public void sendMessage(ActionEvent event)
	{
		if(!typeText.getText().trim().equals(""))
		{
			//chatText.setText(chatText.getText() + "Trying to send message\n");
			
			try 
			{
				serverTask.addNextMessage(typeText.getText());
			} 
			catch (NullPointerException e) 
			{
				chatText.setText(chatText.getText() + "You're not logged in, dummy!\n");
			}
					
			typeText.setText("");
		}
	}

	public void selectIP(ObservableValue<? extends String> ov, String oldVal, String newVal)
	{
		if(newVal.equals("My PC"))
		{
			serverHost = LOCALHOST;
		}
		else if(newVal.equals("Network 1"))
		{
			serverHost = LOCAL_NETWORK_ADDRESS_1;
		}
		else if(newVal.equals("Network 2"))
		{
			serverHost = LOCAL_NETWORK_ADDRESS_2;
		}
		else if(newVal.equals("Internet"))
		{
			serverHost = IP_ADDRESS;
		}
	}
	
	
	private class ServerTask extends Task<Void>
	{
		private Socket socket;
		private LinkedList<String> messagesToSend;
		private boolean hasMessages;
		
		public ServerTask(Socket socket)
		{
			this.socket = socket;
			messagesToSend = new LinkedList<String>();
			hasMessages = false;
		}
		
		public void addNextMessage(String message)
		{
			System.out.println("addNextMessage() is doing something");
			synchronized(messagesToSend){
				System.out.println("synchronized(messagesToSend) is doing something");
				hasMessages = true;
				messagesToSend.push(message);
				System.out.println( "\"" + messagesToSend.peek() + "\"" + "added to LinkedList");
				
			}
		}
		
		public void closeSocket()
		{
			try 
			{
				
				socket.close();
				
			} 
			catch (IOException e) 
			{
				Platform.runLater(new Runnable() {
                    @Override 
                    public void run() 
                    {
                        chatText.setText(chatText.getText() + "Error closing socket!\n");
                    }
    			});
			}
		}
		
		@Override
		protected Void call()
		{
			Platform.runLater(new Runnable() {
                @Override 
                public void run() 
                {
                    chatText.setText(chatText.getText() + "Welcome " + userName + "\n");
                    chatText.setText(chatText.getText() + "Local Port: " + socket.getLocalPort() + "\n");
                    chatText.setText(chatText.getText() + "Server:  " + socket.getRemoteSocketAddress() + "\n");
                }
			});
			
			
            try
            {
            	PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), false);
            	InputStream serverInStream = socket.getInputStream();
            	Scanner serverIn = new Scanner(serverInStream);
            	
            	serverOut.println("00:" + userName);
				serverOut.flush();
            	
            	
				//int iterations = 0;
				while (!socket.isClosed()) {
					/*
					iterations++;
					if(iterations % 10000 == 0)
					{
						System.out.println("Task is running");
					}
					*/
					if (serverInStream.available() > 0) {
						System.out.println("serverInStream is receiving data");
						if (serverIn.hasNextLine()) {
							Platform.runLater(new Runnable() {
								@Override
								public void run() {
									chatText.setText(chatText.getText() + serverIn.nextLine() + "\n");
									//scrollPane.setVvalue(scrollPane.getVmin());
								}
							});
						}

					}
					if (hasMessages) {
						System.out.println("Messages being sent");
						String nextSend = "";
						synchronized (messagesToSend) {
							nextSend = "01:" + buddyName + ":" + userName + ": " + messagesToSend.pop();
							hasMessages = !messagesToSend.isEmpty();
							System.out.println("Second synchronized(messagesToSend) is doing stuff");
						}

						serverOut.println(nextSend);
						serverOut.flush();
						System.out.println("serverOut has attempted to send " + "\"" + nextSend + "\"");
					}
				}
				
				Platform.runLater(new Runnable() {
                    @Override 
                    public void run() 
                    {
                        chatText.setText(chatText.getText() + "Socket closed!\n");
                    }
    			});
            	
            	
            }
            catch (IOException ex)
            {
            	Platform.runLater(new Runnable() {
                    @Override 
                    public void run() 
                    {
                        chatText.setText(chatText.getText() + "IO Error!\n");
                    }
    			});
            }
                    
                    
                
           
			
			return null;
		}
		
		
		
		
		
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

}