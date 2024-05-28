
import java.io.IOException;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import swiftbot.SwiftBotAPI;

public class P4 {

	static SwiftBotAPI API = new SwiftBotAPI();
	static Timer timer = new Timer();

	static long timer1Start = 0;
	static long timer2Start = 0;
	static String ModeSelected = "";
	static int objCounter = 0;

	static Scanner userInputMode;

	static boolean isXPressed = false; // X hasn't been pressed yet so its false
	static boolean runningCU = false;
	static boolean ObjEncountered = true;
	static boolean runningSC = true; // boolean for the while loop for main code, if false then X button terminate
										// snippet is ran
	static boolean runningMain = true;

	public static class MyTimerTask extends TimerTask {
		private double store;

		public MyTimerTask(double store) {
			this.store = store;
		}

		@Override
		public void run() {

			if (store >= 100) {
				API.stopMove();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					API.move(45, -45, 1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				API.stopMove();
				try {
					iWander();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}

		public void updateStore(double store) {
			this.store = store;
		}
	}

	public static void main(String[] args) throws IOException, InterruptedException {

		API.BUTTON_X.addListener(new GpioPinListenerDigital() {// Button listener for button X
			public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
				if (event.getState().isLow()) {
					runningMain = false;
					runningSC = false;
					runningCU = false;
					ObjEncountered = false;
					timer.cancel();

					API.stopMove();// stops the robots movement
					API.stopMove();// Used twice to overcome error of the "X" button being pressed not always
									// stopping the while loops
					API.disableUnderlights();// robot lights turn off
					API.disableUnderlights();// Used twice to overcome error of the "X" button being pressed not always
												// stopping the while loops
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {

						e.printStackTrace();
					}

					// UI for termination program
					System.out.println();
					System.out.println("-----You have pressed 'X' to terminate the program-----");
					System.out.println();
					System.out.println("(NOTE: If you have pressed 'X' to terminate before choosing a mode then you must press enter 1 time before doing the below input)");
					System.out.println();
					System.out.println("Would you like to view the Log of Execution?");
					System.out.println("Enter 'Y' for yes and 'N' for no.");

					Scanner viewLog = new Scanner(System.in); // used for user to input Y or N to view the log
					String log = viewLog.next();

					boolean logrunning = true; // its in a while loop to handle errors if user input is invalid because
												// it starts the switch case again

					while (logrunning == true) {
						if (log.equals("Y")) {

							long timer1End = System.currentTimeMillis();
							long timer2End = System.currentTimeMillis();
							System.out.println();
							System.out.println("-----LOG OF EXECUTION:-----");
							System.out.println();
							System.out.println("Mode that ran:");
							if (ModeSelected.equals("CU")) {
								System.out.println("Curious SwiftBot Mode");
							} else if (ModeSelected.equals("SC")) {
								System.out.println("Scaredy SwiftBot Mode");
							}
							System.out.println();
							System.out.println("Program ran for:");

							if (ModeSelected.equals("CU")) {
								System.out.println((-(timer1Start - timer1End) / 1000) + " Seconds");
							} else if (ModeSelected.equals("SC")) {
								System.out.println((-(timer2Start - timer2End) / 1000) + " Seconds");

							}
							System.out.println();
							System.out.println("Number of times that Swiftbot encountered object:");
							System.out.println(objCounter);
							System.out.println();
							System.out.println("Thank you and goodbye!");

							API.shutdown();

							logrunning = false;

						} else if (log.equals("N")) {

							System.out.println("Thank you and goodbye!");
							logrunning = false;

							API.shutdown();
						} else {
							System.out.println("Input is incorrect please, type 'Y' or 'N', make sure you use capital letters.");

							log = viewLog.nextLine();
						}
					}

					logrunning = false;
				}
			}

		});

		double store = 0; // stores the UltraSound

		initialDisplay();

		while (runningMain) {

			userInputMode = new Scanner(System.in); // scanner for user input for mode selection

			String UserMode = userInputMode.nextLine();

			switch (UserMode) {
			case "CU":
				CU(store);

				break;
			case "SC":

				SC(store);

				break;

			case "ANY":
				System.out.println("You have chosen any SwiftBot mode");
				int random = (int) (Math.random() * 2 + 1); // randomiser generates random number between 1 and 2

				if (random == 1) {

					UserMode = "CU";
					CU(store);

				} else if (random == 2) {

					UserMode = "SC";
					SC(store);

				}

				break;


			}

		}

	}

	public static void CU(double store) throws IOException, InterruptedException {
		timer1Start = System.currentTimeMillis(); // timer for the duration of execution

		ModeSelected = "CU"; // declaration of the ModeSeleceted that the CU mode is selected

		System.out.println("You have chosen the Curious SwiftBot mode");

		double initialDistance = API.useUltrasound();
		MyTimerTask task = new MyTimerTask(initialDistance);

		timer.schedule(task, 5000, 5000);

		runningCU = true;

		if (!runningCU) { // These if statements are used to help with the issue of the terminate function
							// not always working due to the loop and it double checks if the loop is exit
			API.stopMove();
			API.disableUnderlights();
		}

		while (runningCU == true) {

			iWander();
			shootUltrasound();
			store = shootUltrasound();
			task.updateStore(store);

			ObjEncountered = true;

			if (!runningCU) {// These if statements are used to help with the issue of the terminate function
								// not always working due to the loop and it double checks if the loop is exit
				API.stopMove();
				API.disableUnderlights();
			}

			while (ObjEncountered == true) {

				shootUltrasound();
				

				if (!ObjEncountered) {// These if statements are used to help with the issue of the terminate function
										// not always working due to the loop and it double checks if the loop is exit
					API.stopMove();
					API.disableUnderlights();
				}

				if (API.useUltrasound() <= 100) {

					API.fillUnderlights(158, 253, 56);
					API.updateUnderlights();

					store = shootUltrasound();
					task.updateStore(store);

					objCounter += 1; // counter for the amount of times an object is encountered

					if (!ObjEncountered) {
						API.stopMove();
						API.disableUnderlights();
					}

					if (store == 15) { // when distance is equal to 15 it stops moving
						API.stopMove();
						API.stopMove();
						flashGreen();

						ObjEncountered = false;

						if (!ObjEncountered) {
							API.stopMove();
							API.disableUnderlights();
						}
					} else {
						if ((store > 15) && (store < 100)) { // when distance >15 it moves forward till distance
																// is 15
							API.startMove(40, 40);

							if (!ObjEncountered) {
								API.stopMove();
								API.disableUnderlights();
							}
						} else if (store < 15) { // when distance < 15 moves backwards till distance is 15
							API.startMove(-40, -40);

							if (!ObjEncountered) {
								API.stopMove();
								API.disableUnderlights();
							}
						}
						Thread.sleep(5);
					}
				}

			}
		}

	}

	public static void SC(double store) throws IOException, InterruptedException {

		timer2Start = System.currentTimeMillis();

		ModeSelected = "SC"; // declaration of ModeSelection that the SC mode has been selected

		System.out.println("You have chosen the Scaredy SwiftBot mode");

		double initialDistance2 = API.useUltrasound();
		MyTimerTask task2 = new MyTimerTask(initialDistance2);

		timer.schedule(task2, 5000, 5000);

		while (runningSC == true) {
			iWander();
			shootUltrasound();
			store = shootUltrasound();
			task2.updateStore(store);
			

			
			if (store <= 100) {


				objCounter += 1; // object counter incremented by 1

				if(runningSC) {
					API.stopMove();
				}
				if(runningSC) {
					flashRed();
				}
				if(runningSC) {
					API.fillUnderlights(255, 0, 0);
				}
				if(runningSC) {
					API.updateUnderlights();
				}
				if(runningSC) {
					API.move(42, -42, 1000); // rotates 180 degrees works on carpet in library
				}
				if(runningSC) {
					API.move(100, 100, 3000);
				}

			}

		}

	}

	public static void initialDisplay() {
		// Introduction + Statement to end the program
		System.out.println("----- Welcome to Detect Object -----");
		System.out.println("a program by Bartek F");
		System.out.println();
		System.out.println("To end the program press the 'X' button located on the SwiftBot");
		System.out.println();
		// initial Mode selection output

		System.out.println("-----Choose a mode, enter:-----");
		System.out.println();
		System.out.println("CU - for Curious SwiftBot mode");
		System.out.println("SC - for Scaredy SwiftBot mode");
		System.out.println("ANY - for any of the SwiftBot modes chosen at random");
		System.out.println();
	}

	public static void flashGreen() throws IOException {// method flashes green lights for 2 seconds

		if (runningCU == true) {
			for (int i = 0; i < 10; i++) {
				API.fillUnderlights(158, 253, 56);
				API.updateUnderlights();
				API.disableUnderlights();
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public static void flashRed() throws IOException { // method flashes red lights for 2 seconds

		if (runningSC == true) {

			for (int i = 0; i < 10; i++) {
				API.fillUnderlights(255, 0, 0);
				API.updateUnderlights();
				API.disableUnderlights();
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public static void iWander() throws IOException { // method makes the robot move at a slow speed with blur lights

		API.startMove(40, 40); // Under lights all filled in a blue colour
		API.fillUnderlights(30, 144, 255); // fills under lights to blue
		API.updateUnderlights(); // updates the under lights colour
	}

	public static double shootUltrasound() { // uses UltraSound and returns the value

		return API.useUltrasound();
	}
}