package de.dhbw.e_mobility.e_app.bluetooth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import de.dhbw.e_mobility.e_app.ActivityHandler;

public class BluetoothConnectionService {

	private enum ServiceState {
		INIT, LOGOUT, CONNECTING, CONNECTED, LOGIN, LOGGED_IN;
	}

	private ServiceState serviceState;
	private BluetoothCommands lastCommand;

	// Get ActivityHandler object
	private ActivityHandler activityHandler = ActivityHandler.getInstance();

	// Private objects
	private ConnectThread connectThread;
	private ConnectedThread connectedThread;
	private HoldConnectionThread holdConnectionThread;

	/**
	 * Constructor.
	 */
	public BluetoothConnectionService() {
		serviceState = ServiceState.INIT;
		lastCommand = null;
		connectThread = null;
		connectedThread = null;
		holdConnectionThread = null;
	}

	// Checks the if there is a connection currently
	public void checkConnectionAndLogin(BluetoothDevice device) {
		if (serviceState == ServiceState.LOGGED_IN) {
			activityHandler.fireToHandler(
					ActivityHandler.HANDLLER_DEVICE_PROVIDER,
					BluetoothInfoState.LOGGED_IN);
		} else {
			if (serviceState == ServiceState.LOGOUT) {
				init();
			}
			connect(device);
		}
	}

	// Initialize state and threads
	private void init() {
		serviceState = ServiceState.INIT;
		cancelHoldConnectionThread();
		if (connectedThread != null) {
			connectedThread.init();
			connectedThread = null;
		}
		cancelConnectThread();
	}

	// Starts the ConnectThread
	private synchronized void connect(BluetoothDevice theDevice) {
		// Cancel connect thread for reconnecting
		// if (serviceState == ServiceState.CONNECTING) {
		cancelConnectThread();
		// }
		// Cancel any thread currently running a connection
		cancelConnectedThread();

		// Start the thread to connect with the given device
		connectThread = new ConnectThread(theDevice);
		serviceState = ServiceState.CONNECTING;
		connectThread.start();
	}

	/**
	 * This thread runs while attempting to make an outgoing connection with a
	 * device. It runs straight through; the connection either succeeds or
	 * fails.
	 */
	private class ConnectThread extends Thread {
		private BluetoothSocket bluetoothSocket;

		public ConnectThread(BluetoothDevice theDevice) {
			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			BluetoothSocket tmpSocket = null;
			try {
				Method m = theDevice.getClass().getMethod("createRfcommSocket",
						new Class[] { int.class });
				tmpSocket = (BluetoothSocket) m.invoke(theDevice, 1);
			} catch (SecurityException e) {
				Log.e("CONNECTION-SERVICE",
						"Make createRfcommSocket() failed [SecurityException]",
						e);
			} catch (NoSuchMethodException e) {
				Log.e("CONNECTION-SERVICE",
						"Make createRfcommSocket() failed [NoSuchMethodException]",
						e);
			} catch (IllegalArgumentException e) {
				Log.e("CONNECTION-SERVICE",
						"Run createRfcommSocket() failed [IllegalArgumentException]",
						e);
			} catch (IllegalAccessException e) {
				Log.e("CONNECTION-SERVICE",
						"Run createRfcommSocket() failed [IllegalAccessException]",
						e);
			} catch (InvocationTargetException e) {
				Log.e("CONNECTION-SERVICE",
						"Run createRfcommSocket() failed [InvocationTargetException]",
						e);
			}
			bluetoothSocket = tmpSocket;
		}

		@Override
		public void run() {
			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				bluetoothSocket.connect();
				Log.d("CONNECTION-SERVICE", "Connection sucessfull!");
			} catch (IOException e) {
				Log.e("CONNECTION-SERVICE", "Connection failed!", e);

				// activityHandler.fireToast("Keine Verbindung möglich!");
				// activityHandler.fireToHandler(
				// ActivityHandler.HANDLLER_DEVICE_PROVIDER,
				// BluetoothInfoState.DISCONNECTED);
				// TODO Meldung ausgeben...
				serviceState = ServiceState.INIT;
				bluetoothSocket = null;

				// init();
				return;
			}
			// Start the connected thread
			connected(bluetoothSocket);
		}

		private void init() {
			try {
				if (bluetoothSocket != null) {
					bluetoothSocket.close();
					bluetoothSocket = null;
				}
			} catch (IOException e) {
				Log.e("CONNECTION-SERVICE", "Closing socket failed", e);
			}
		}
	}

	// Starts the ConnectedThread
	private synchronized void connected(BluetoothSocket theSocket) {
		// Cancel any thread currently running a connection
		cancelConnectedThread();

		// Start the thread to manage the connection and perform transmissions
		connectedThread = new ConnectedThread(theSocket);
		// serviceState = ServiceState.CONNECTED;
		connectedThread.start();
	}

	/**
	 * This thread runs during a connection with a remote device. It handles all
	 * incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread {
		private InputStream inputStream;
		private OutputStream outputStream;

		public ConnectedThread(BluetoothSocket theSocket) {
			init();
			// Get the BluetoothSocket input and output streams
			InputStream tmpInStream = null;
			OutputStream tmpOutStream = null;
			try {
				tmpInStream = theSocket.getInputStream();
				tmpOutStream = theSocket.getOutputStream();
			} catch (IOException e) {
				Log.e("CONNECTION-SERVICE", "Couldn't get streams from socket",
						e);
			}
			inputStream = tmpInStream;
			outputStream = tmpOutStream;
		}

		@Override
		public void run() {
			final BufferedReader reader = new BufferedReader(
					new InputStreamReader(inputStream));

			// Keep listening to the InputStream while connected
			String line = "";
			try {
				while (line != null) {
					if (serviceState == ServiceState.INIT) {
						break;
					}
					// TODO Test for reading duration
					long time = System.currentTimeMillis();
					line = reader.readLine();
					// TODO Test for reading duration
					// Log.v("CONNECTION-SERVICE",
					// "Duration: "
					// + String.valueOf(System.currentTimeMillis()
					// - time));
					if (!line.equals("")) {
						if (lastCommand != null) {
							// Log.d("CONNECTION-SERVICE", "LASTCOM: "
							// + lastCommand.toString());
						} else {
							Log.d("CONNECTION-SERVICE", "LASTCOM: NULL");
						}
						Log.v("CONNECTION-SERVICE", line);
						if (line.equals("error")) {
							// TODO reaction of error (unimportant)
						} else if (line.equals("ok")) {
							if (isLastCommand(BluetoothCommands.LOGIN)) {
								// if (serviceState == ServiceState.LOGIN) {
								serviceState = ServiceState.LOGGED_IN;

								// Start the HoldConnectionThread
								holdConnectionThread = new HoldConnectionThread();
								holdConnectionThread.start();
								activityHandler
										.fireToHandler(
												ActivityHandler.HANDLLER_DEVICE_PROVIDER,
												BluetoothInfoState.LOGGED_IN);

								// Asking for parameter list
								write(BluetoothCommands.AT_PARAM_LIST);
							} else if (isLastCommand(BluetoothCommands.AT_LOGOUT)) {
								// } else if (serviceState ==
								// ServiceState.LOGOUT) {
								serviceState = ServiceState.INIT;
								init();
								activityHandler
										.fireToHandler(
												ActivityHandler.HANDLLER_DEVICE_PROVIDER,
												BluetoothInfoState.NONE);
								cancelConnectThread();
							}
						} else if (line.equals("login >")) {
							serviceState = ServiceState.LOGIN;
							write(BluetoothCommands.LOGIN);
						} else if (isLastCommand(BluetoothCommands.AT_PARAM_LIST)) {
							// Log.d("SaveParamList", "Line: " + line);
							saveParamList(line);
						} else {
							// Log.d("SavePush", "Line: " + line);
						}
					}
				}
			} catch (IOException e) {
				Log.e("CONNECTION-SERVICE", "Fail in reading stream", e);
			}
		}

		// Checks if the last command is the given on
		private boolean isLastCommand(BluetoothCommands command) {
			if (command == lastCommand) {
				if (command != BluetoothCommands.AT_PARAM_LIST) {
					lastCommand = null;
				}
				return true;
			}
			return false;
		}

		// Write a command
		public void write(BluetoothCommands command) {
			if (command != BluetoothCommands.AT_0) {
				lastCommand = command;
			}
			Log.v("CONNECTION-SERVICE", "> " + command.toString());
			write((command.toString() + "\r").getBytes());
		}

		// Write to the connected OutputStream
		private void write(byte[] buffer) {
			try {
				outputStream.write(buffer);
			} catch (IOException e) {
				Log.e("CONNECTION-SERVICE", "Exception during writing", e);
			}
		}

		private void init() {
			inputStream = null;
			outputStream = null;
		}

		private void cancel() {
			// write(Command.AT_PUSH_N, "0");
			write(BluetoothCommands.AT_LOGOUT);

			// TODO TODO
			// try {
			// Thread.sleep(1000);
			// if (getConState() != STATE_NONE) {
			// // If logout not finished after 1 second do it
			// Log.d("BLA", "1000");
			// inputStream = null;
			// outputStream = null;
			//
			// connectedThread = null;
			// }
			// } catch (InterruptedException e) {
			// Log.e("CONNECTION-SERVICE", "Sleep during logout failed", e);
			// }
		}
	}

	// This thread holds the connection on sending attention simple signals
	private class HoldConnectionThread extends Thread {
		@Override
		public void run() {
			// Repeat message every 60 seconds
			while (true) {
				try {
					Thread.sleep(60000);
					write(BluetoothCommands.AT_0);
				} catch (InterruptedException e) {
					Log.d("CONNECTION-SERVICE", "Sleep was interrupted");
					break;
				}
			}
		}
	}

	// // Authenticate with controller
	// private void login() {
	// Log.d("CONNECTION-SERVICE", "login()");
	// if (serviceState == ServiceState.LOGGED_IN) {
	// return;
	// }
	// serviceState = ServiceState.LOGIN;
	// write(BluetoothCommands.LOGIN);
	// }

	private void write(BluetoothCommands command) {
		// // Create temporary object
		// ConnectedThread tmpThread;
		// // Synchronize a copy of the ConnectedThread
		// synchronized (this) { // TODO warum ist das so??
		if (serviceState != ServiceState.LOGGED_IN
				&& serviceState != ServiceState.LOGIN) {
			return;
		}
		// tmpThread = connectedThread;
		// }
		// // Perform the write unsynchronized
		// tmpThread.write(txt);
		if (connectedThread != null) {
			connectedThread.write(command);
		}
	}

	// This method is doing some stuff
	// private void doSomeStuff() {
	// try {
	// // Messwertausgabe
	// Thread.sleep(250);
	// // write(Command.AT_PUSHINT_N, "5");
	// // Thread.sleep(250);
	// // write(Command.AT_PUSH_N, "2");
	// // Thread.sleep(250);
	// } catch (InterruptedException e) {
	// Log.d("CONNECTION-SERVICE", "Sleep was interrupted");
	// Thread.currentThread().interrupt();
	// }
	// }

	// Request the parameter list
	public void getParameterList() {
		write(BluetoothCommands.AT_PARAM_LIST);
	}

	// Cancels the mHoldConnectionThread
	private synchronized void cancelHoldConnectionThread() {
		if (holdConnectionThread != null) {
			holdConnectionThread.interrupt();
			holdConnectionThread = null;
		}
	}

	// Cancels the mConnectThread
	private synchronized void cancelConnectThread() {
		if (connectThread != null) {
			connectThread.interrupt();
			connectThread.init();
			connectThread = null;
		}
	}

	// Cancels the mConnectedThread
	private synchronized void cancelConnectedThread() {
		if (connectedThread != null) {
			connectedThread.cancel();
			connectedThread = null;
		}
	}

	// Stops all threads
	public synchronized void stop() {
		Log.d("CONNECTION-SERVICE", "stop()");
		serviceState = ServiceState.LOGOUT;
		cancelHoldConnectionThread();
		cancelConnectedThread();

		// TODO TODO
		// try {
		// Thread.sleep(1500);
		// if (getConState() != STATE_NONE) {
		// // If logout not finished after 1,5 second do it
		// Log.d("BLA", "1500");
		// setConState(STATE_NONE);
		// cancelConnectThread();
		// }
		// } catch (InterruptedException e) {
		// Log.e("CONNECTION-SERVICE", "Sleep during logout failed", e);
		// }
	}

	private void saveParamList(String paramList) {
		// Prepare the commands
		HashMap<String, BluetoothCommands> bluetooth_commands = activityHandler
				.getBluetoothCommands();

		// Read the parameter list
		String command_text, value;
		String[] tmp;
		for (String line : paramList.split("\n")) {
			tmp = line.split("=");
			command_text = tmp[0];
			value = tmp[1];

			if (bluetooth_commands.containsKey(command_text)) {
				bluetooth_commands.get(command_text).setValue(value);
				// TODO prüfen ob wirklich gespeichert wird
				// System.out.println(bluetooth_commands);
			}
		}
	}

	// Sends the wanted command
	public void sendCommand(BluetoothCommands command) {
		write(command);
	}
}
