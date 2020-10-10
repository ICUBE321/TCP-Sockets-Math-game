/**
 * This GameServer implements the Math game application.
 * 
 * @author Izien Iremiren
 * @since 2020-01-10
 */

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class GameServer {
	
	private ServerSocket server; //global server socket

	//server.run(portnumber) function
	public void run(int pn) {
		try {
			server = new ServerSocket(pn); //initialize server socket with port number
			while(true) { //keeps the server running
				Socket client = server.accept(); //connect to client
				System.out.println("Accepted!");
				Connection connects = new Connection(client); //create connection object and passes client socket
				connects.start(); //calls run() function in Connection class
			}
		} catch (Exception e) {
			System.out.println("Exception message: "+ e.getMessage());
		}
	}

	//connection class acting as thread
	class Connection extends Thread{
		Socket cs;
		BufferedReader is; //input stream for reading client input
		PrintWriter os; //output stream for sending message to client
		//game state variables
		String username; //client username
		String gameMode; //chosen game mode
		int timer; //timer variable for game countdown
		double correct_result, user_result, score; //correct answer variable, user's answer and user's score
		Timer time_counter = new Timer(); //timer_counter for game countdown

		//connection constructor
		public Connection(Socket cc) {
			cs = cc; //client socket representative
			try {
				//initializing streams
				is = new BufferedReader(
					new InputStreamReader(
						cs.getInputStream()));

				os = new PrintWriter(cs.getOutputStream(), true);
			} catch (Exception e) {
				System.out.println("Exception message: "+ e);
			}
		}

		//connects.start() function
		public void run() {
			try {
                    os.println(getUserName());
                    String inputLine; //holds user input
                    while((inputLine = is.readLine()) != null) {
						username = inputLine;
						runGame(); //starts the game
					}
			} catch (Exception e) {
				System.out.println("Exception message: "+ e);
			}
        }
		
		//function for asking for username
        public String getUserName() {
            String question = "Enter preferred username: ";
            return question;
		}

		//function for getting gameMode
		public String getGameMode() {
            String question = "Choose game mode: type 1 for 30s or type 2 for 60s";
            return question;
		}

		//function for starting game if user says ready
		public void launchGame(String input) {
			try {
				while(setGameMode(input) == 0) { //repeats until user selects a game mode
					os.println(getGameMode());
					input = is.readLine();
					System.out.println("game mode: "+ input + " chosen");
				};
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}
		
		//function for running the game
		public void runGame() {
			try {
				os.println("Hi there "+ username + ". If you want to exit, type exit. Choose game mode: type 1 for 30s or type 2 for 60s");
				String userInput = is.readLine();
				if(userInput.contains("exit")) { //if user chooses to exit
					exitGame();
				} else {
					launchGame(userInput); // check if user has chosen a game mode
					os.println("Enter yes to start game or no to change game mode: ");
					userInput = is.readLine();
					if(userInput.contains("yes")) {
						//game logic
						double question_count = 0;
						startTimer();
						while(timer > 0) {
							os.println(generateEquation());
							userInput = is.readLine();
							user_result = Double.parseDouble(userInput);
							//count question
							question_count++;
							//check answer using correct_result and add score if correct
							checkAnswer(user_result, correct_result);
						}
						
						os.println("Time's up!!! Press key to see score...");
						//collecting user input
						userInput = is.readLine();
						printScore(question_count);
					} else {
						runGame(); //repeat if user wants to change game mode
					}
				}
			} catch (Exception e) {
				System.out.println(username+ " closed game");
			}
		}

		//function for exiting game
		public void exitGame() {
			try {
				os.println("Closing game...Press Enter");
			} finally {
				try {
					os.close();
				} finally {
					try {
						is.close();
					} catch (IOException e) {
						System.out.println(e.getMessage());
					} finally {
						try {
							cs.close();
							System.out.println(username +"'s game closed...");
						} catch (IOException e) {
							System.out.println(e.getMessage());
						}
					}
				}
			}
		}

		//function for printing the final score and storing it
		public void printScore(double question_num) {
			try {
				File fileObject = new File(username+"'s Score");
				//current date and time
				Date current_date = Calendar.getInstance().getTime();  
            	DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");  
                String dateString = dateFormat.format(current_date);
				//save game info
				if(fileObject.createNewFile()) {
					FileWriter myWriter = new FileWriter(fileObject);
					myWriter.write("\nDate: " + dateString + ", Game mode: " + gameMode + ", Final score: " + score + "/" + question_num);
					myWriter.close();
				} else {
					//open file in append mode
					Writer output = new BufferedWriter(new FileWriter(fileObject, true));
					output.append("\nDate: " + dateString + ", Game mode: " + gameMode + ", Final score: " + score + "/" + question_num);
					output.close();
				}				
				//print total score
				os.println("You've got "+ score + " right out of " + question_num + " questions! Enter exit to quit game or continue to keep going...");
				if(is.readLine().contains("exit")) { // if user wants to leave game
					System.out.println("Client chose to exit");
					exitGame();
				} else { //if user chooses to continue
					System.out.println("Client chose to continue");
					runGame();
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		}

		//function for checking answer and updating current score
		public void checkAnswer(double userInput, double rightAnswer) {
			if(userInput == rightAnswer) {
				score++;
			}
		}

		//function to start timer
		public void startTimer() {
			time_counter.scheduleAtFixedRate( new TimerTask(){
				public void run() {
					if(timer > 0) {
						--timer;
						System.out.println("Time left is:"+ timer);
					} else {
						time_counter.cancel();
						time_counter.purge();
					}
					
				}
			}, 0, 1000);
		}

		//function to generate random equations
		public String generateEquation() {
			Random rand = new Random();
			Random operatorChoice = new Random();
			double a, b; 
			int opChoice;
			String operator, equation;
			a = rand.nextInt(50);
			b = rand.nextInt(50);
			opChoice = operatorChoice.nextInt(5);
			switch(opChoice) {
				case 0: 
					operator = "+";
					break;
				case 1:
					operator = "-";
					break;
				case 2:
					operator = "*";
					break;
				case 3:
					operator = "/";
					break;
				case 4:
					operator = "%";
					break;
				default: operator = "";
			}

			correct_result = equationSolver(a, b, opChoice); //solve the equation
			
			equation = " "+ a +" "+ operator +" "+ b + " ?";
			return equation;
		}

		//function to solve the equation
		public double equationSolver(double a, double b, int op) {
			double result;
			switch(op) {
				case 0: 
					result = a + b;
					break;
				case 1:
					result = a - b;
					break;
				case 2:
					result = a * b;
					break;
				case 3:
					result = a / b;
					break;
				case 4:
					result = a % b;
					break;
				default: result = Double.POSITIVE_INFINITY;
			}

			return result;
		}

		//function for setting gameMode
		public int setGameMode(String mode) {
			int status;
			// System.out.println("Setting game mode to "+ mode);
			if(mode.contains("1")) {
				gameMode = "30s";
				timer = 30;
				status = 1;
				// System.out.println("Setting game status to "+ status);
			} else if (mode.contains("2")) {
				gameMode = "60s";
				timer = 60;
				status = 1;
				// System.out.println("Setting game status to "+ status);
			} else {
				status = 0;
				// System.out.println("Setting game status to "+ status);
			}
			return status;
		}
	}

	public static void main(String[] args) {

		int portNumber = 3500; 
		GameServer game_server = new GameServer();
		System.out.println("Listening for connections...");
		game_server.run(portNumber);
	}
}