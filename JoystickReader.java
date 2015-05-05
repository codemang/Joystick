import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JFrame;
import java.awt.Color;
import java.io.*;

public class JoystickReader implements SerialPortEventListener {
	SerialPort serialPort;
  int i = 0;
	/** The port we're normally going to use. */
	private static final String PORT_NAMES[] = { 
			"/dev/ttyUSB0", // Linux
			"COM1", // Windows
      "/dev/tty.usbmodem1411",
	};

  private static final int allKeystrokes[] = {
    KeyEvent.VK_W,
    KeyEvent.VK_W,
    KeyEvent.VK_W,
    KeyEvent.VK_W,
    KeyEvent.VK_W,
    KeyEvent.VK_W,
    KeyEvent.VK_A,
    KeyEvent.VK_D,
  };

  private static final String allMessages[] = {
    "BUTTON1",
    "BUTTON2",
    "BUTTON3",
    "BUTTON4",
    "UP",
    "DOWN",
    "LEFT",
    "RIGHT",
  };

	/**
	 * A BufferedReader which will be fed by a InputStreamReader converting the
	 * bytes into characters making the displayed results codepage independent
	 */
	private BufferedReader input;
	/** The output stream to the port */
	private OutputStream output;
	/** Milliseconds to block while waiting for port open */
	private static final int TIME_OUT = 2000;
	/** Default bits per second for COM port. */
	private static final int DATA_RATE = 9600;

	public void initialize() {
		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		// First, Find an instance of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum
					.nextElement();
			for (String portName : PORT_NAMES) {
				if (currPortId.getName().equals(portName)) {
					portId = currPortId;
					break;
				}
			}
		}
		System.out.println("Port ID: "+portId);
		if (portId == null) {
			System.out.println("Could not find COM port.");
			return;
		}

		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

			// open the streams
			input = new BufferedReader(new InputStreamReader(
					serialPort.getInputStream()));
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			System.err.println(e.toString());
		}
	}

	/**
	 * This should be called when you stop using the port. This will prevent
	 * port locking on platforms like Linux.
	 */
	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	/**
	 * Handle an event on the serial port.
	 */
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
        int c;

        while ((c=input.read()) != -1) {
          handleMessage(c);
        }

			} catch (Exception e) {
				System.err.println(e.toString());
			}
		}
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {

		}
	}

  public static synchronized void printOptions() throws IOException {
    System.out.println("\n\nChoose which game you would like to play.\n");
    File folder = new File("./Games");
    File[] listOfFiles = folder.listFiles();

    
    for (int i = 0; i < listOfFiles.length; i++) {
      if (listOfFiles[i].isFile()) {
        System.out.println("   " + (i + 1) + ": " + listOfFiles[i].getName());
      }
    }

    int gameSelection;
    while (true) {
      System.out.print("\nEnter Game Number: ");
      int ch;
      String input = "";
      while ((ch = System.in.read ()) != '\n')
        input += (char)ch;

      try {
        gameSelection = Integer.parseInt(input);
        if (gameSelection > listOfFiles.length)
          System.out.println("Error: you must choose a game number within the available indices.");
        else 
          break;
      }
      catch (NumberFormatException e) {
        System.out.println("Error: you must input a number to select a game, try again.");
      }
    }
  }
  /**
   * 
   */
  public synchronized void handleMessage(int messageIndex) {
    System.out.println(allMessages[messageIndex]);
    if (messageIndex >= 0 && messageIndex <= 7) {
					try {
						Robot robot = new Robot();
						robot.keyPress(allKeystrokes[messageIndex]);
            try { Thread.sleep(100); } 
            catch (InterruptedException e) { e.printStackTrace(); }

						robot.keyRelease(allKeystrokes[messageIndex]);
					} catch (AWTException e) {
						e.printStackTrace();
					}
    }
  }

	public static void main(String[] args) throws Exception {
		JoystickReader main = new JoystickReader();
		main.initialize();
		Thread t = new Thread() {
			public void run() {
				// the following line will keep this app alive for 1000 seconds,
				// waiting for events to occur and responding to them (printing
				// incoming messages to console).
				try {
					Thread.sleep(1000000);
				} catch (InterruptedException ie) {
				}
			}
		};
		t.start();
    printOptions();
	}
}
