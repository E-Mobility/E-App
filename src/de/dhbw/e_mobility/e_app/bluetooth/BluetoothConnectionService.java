package de.dhbw.e_mobility.e_app.bluetooth;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;
import de.dhbw.e_mobility.e_app.ActivityHandler;

public class BluetoothConnectionService {

	// State of connection with device
	public static final int STATE_NONE = 10;
	public static final int STATE_LOGOUT = 11;
	public static final int STATE_LOGEDOUT = 12;
	public static final int STATE_CONNECTING = 13;
	public static final int STATE_CONNECTED = 14;
	public static final int STATE_LOGIN = 15;
	public static final int STATE_LOGEDIN = 16;

	// Get ActivityHandler object
	private ActivityHandler activityHandler = ActivityHandler.getInstance();

	// Handler of other objects
	private Handler deviceProviderHandler;

	// Private objects
	private ConnectThread connectThread;
	private ConnectedThread connectedThread;
	private HoldConnectionThread holdConnectionThread;
	private int state;
	private String password;

	/**
	 * Constructor.
	 */
	public BluetoothConnectionService(Handler theHandler, String thePassword) {
		Log.d("CONNECTION-SERVICE", "BluetoothConnectionService()");
		setConState(STATE_NONE);
		deviceProviderHandler = theHandler;
		password = thePassword;
	}

	// Sets the given state
	private synchronized void setConState(int theState) {
		Log.d("CONNECTION-SERVICE", "setState() " + state + " -> " + theState);
		state = theState;
	}

	// Returns the current state
	private synchronized int getConState() {
		return state;
	}

	// Checks the if there is a connection currently
	public void checkConnectionAndLogin(BluetoothDevice device) {
		if (getConState() == STATE_LOGEDIN) {
			activityHandler.fireToHandler(
					ActivityHandler.HANDLLER_DEVICE_PROVIDER, STATE_LOGEDIN);
			// activityHandler.fireToHandler(deviceProviderHandler,
			// STATE_LOGEDIN);
		} else {
			connect(device);
		}
	}

	// Starts the ConnectThread
	private synchronized void connect(BluetoothDevice theDevice) {
		Log.d("CONNECTION-SERVICE", "connect() to " + theDevice);

		// Cancel connect thread for reconnecting
		if (state == STATE_CONNECTING) {
			cancelConnectThread();
		}
		// Cancel any thread currently running a connection
		cancelConnectedThread();

		// Start the thread to connect with the given device
		connectThread = new ConnectThread(theDevice);
		setConState(STATE_CONNECTING);
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
			Log.d("CONNECTION-SERVICE", "ConnectThread() with " + theDevice);

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

		public void run() {
			Log.d("CONNECTION-SERVICE", "ConnectThread-run()");

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				bluetoothSocket.connect();
				Log.d("CONNECTION-SERVICE", "Connection sucessfull!");
			} catch (IOException e) {
				Log.e("CONNECTION-SERVICE", "Connection failed!", e);
				try {
					bluetoothSocket.close();
					bluetoothSocket = null;
				} catch (IOException e1) {
					Log.e("CONNECTION-SERVICE", "Closing socket failed", e1);
				}
				return;
			}

			// Start the connected thread
			connected(bluetoothSocket);
		}

		private void cancel() {
			Log.d("CONNECTION-SERVICE", "ConnectThread-cancel()");

			try {
				bluetoothSocket.close();
				bluetoothSocket = null;
			} catch (IOException e) {
				Log.e("CONNECTION-SERVICE", "Closing socket failed", e);
			}
		}
	}

	// Starts the ConnectedThread
	private synchronized void connected(BluetoothSocket theSocket) {
		Log.d("CONNECTION-SERVICE", "connected()");

		// Cancel any thread currently running a connection
		cancelConnectedThread();

		// Start the thread to manage the connection and perform transmissions
		connectedThread = new ConnectedThread(theSocket);
		setConState(STATE_CONNECTED);
		connectedThread.start();
		// activityHandler.fireToHandler(deviceProviderHandler,
		// STATE_CONNECTED);
	}

	/**
	 * This thread runs during a connection with a remote device. It handles all
	 * incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread {
		private BluetoothDevice bluetoothDevice;
		private InputStream inputStream;
		private OutputStream outputStream;

		public ConnectedThread(BluetoothSocket theSocket) {
			Log.d("CONNECTION-SERVICE", "ConnectedThread()");

			bluetoothDevice = theSocket.getRemoteDevice();

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

		public void run() {
			Log.d("CONNECTION-SERVICE", "ConnectedThread-run()");

			final BufferedReader reader = new BufferedReader(
					new InputStreamReader(inputStream));

			// Keep listening to the InputStream while connected
			String line = "";
			try {
				while (line != null) {
					if (getConState() == STATE_NONE) {
						break;
					}
					line = reader.readLine();
					Log.v("CONNECTION-SERVICE", line);
					if (line.equals("error")) {
						// TODO reaction of error (unimportant)
					} else if (line.equals("ok")) {
						if (getConState() == STATE_LOGIN) {
							setConState(STATE_LOGEDIN);

							// Start the HoldConnectionThread
							holdConnectionThread = new HoldConnectionThread();
							holdConnectionThread.start();
							activityHandler.fireToHandler(ActivityHandler.HANDLLER_DEVICE_PROVIDER, STATE_LOGEDIN);
							// activityHandler.fireToHandler(
							// deviceProviderHandler, STATE_LOGEDIN);

							doSomeStuff();
						} else if (getConState() == STATE_LOGOUT) {
							setConState(STATE_LOGEDOUT);
							activityHandler.fireToHandler(ActivityHandler.HANDLLER_DEVICE_PROVIDER, STATE_LOGEDOUT);
							// activityHandler.fireToHandler(
							// deviceProviderHandler, STATE_LOGEDOUT);
						}
					} else if (line.equals("login >")) {
						login();
					}
				}
			} catch (IOException e) {
				Log.e("CONNECTION-SERVICE", "Fail in reading stream", e);
			}
		}

		// Write a String
		public void write(String txt) {
			Log.d("CONNECTION-SERVICE", "write()");
			Log.v("CONNECTION-SERVICE", "> " + txt);
			txt += "\r";

			write(txt.getBytes());
		}

		// Write to the connected OutputStream
		private void write(byte[] buffer) {
			Log.d("CONNECTION-SERVICE", "ConnectedThread-write()");
			try {
				outputStream.write(buffer);
			} catch (IOException e) {
				Log.e("CONNECTION-SERVICE", "Exception during writing", e);
			}
		}

		private void cancel() {
			Log.d("CONNECTION-SERVICE", "ConnectedThread-cancel()");

			// write("at-push=0");
			write("at-logout");

			bluetoothDevice = null;
			inputStream = null;
			outputStream = null;
		}

		// Returns connected remote device
		private BluetoothDevice getRemoteDevice() {
			return bluetoothDevice;
		}
	}

	// This thread holds the connection on sending attention simple signals
	private class HoldConnectionThread extends Thread {

		public void run() {
			Log.d("CONNECTION-SERVICE", "HoldConnectionThread-run()");

			// Repeat message every 60 seconds
			while (true) {
				try {
					Thread.sleep(60000);
					write("at-0");
				} catch (InterruptedException e) {
					Log.e("CONNECTION-SERVICE",
							"Sleep during holding connection failed", e);
				}
			}
		}
	}

	// Authenticate with controller
	private void login() {
		Log.d("CONNECTION-SERVICE", "login()");
		if (getConState() == STATE_LOGEDIN) {
			return;
		}
		setConState(STATE_LOGIN);
		write(password);
	}

	// Write to controller
	private void write(String txt) {
		// Create temporary object
		ConnectedThread tmpThread;
		// Synchronize a copy of the ConnectedThread
		synchronized (this) { // TODO warum ist das so??
			if (state != STATE_LOGEDIN && state != STATE_LOGIN) {
				return;
			}
			tmpThread = connectedThread;
		}
		// Perform the write unsynchronized
		tmpThread.write(txt);
	}

	// This method is doing some stuff
	private void doSomeStuff() {
		try {
			// Messwertausgabe
			Thread.sleep(250);
			// write("at-pushint=5");
			// Thread.sleep(250);
			// write("at-push=2");
			// Thread.sleep(250);
			write("at-?");
		} catch (InterruptedException e) {
			Log.e("CONNECTION-SERVICE", "Fail sleep() during doSomeStuff()", e);
		}
	}

	// Cancels the mHoldConnectionThread
	private synchronized void cancelHoldConnectionThread() {
		if (holdConnectionThread != null) {
			// try { // TODO
			// holdConnectionThread.join();
			// } catch (InterruptedException e) {
			// Log.e("CONNECTION-SERVICE",
			// "Cancel mHoldConnectionThread failed", e);
			// }
			holdConnectionThread = null;
		}
	}

	// Cancels the mConnectThread
	private synchronized void cancelConnectThread() {
		if (connectThread != null) {
			connectThread.cancel();
			// try { // TODO
			// mConnectThread.join();
			// } catch (InterruptedException e) {
			// Log.e("CONNECTION-SERVICE", "Cancel mConnectThread failed", e);
			// }
			connectThread = null;
		}
	}

	// Cancels the mConnectedThread
	private synchronized void cancelConnectedThread() {
		// Cancel thread for holding connection
		cancelHoldConnectionThread();

		if (connectedThread != null) {
			// try {
			// mConnectedThread.join(); // TODO // TODO
			// } catch (InterruptedException e) {
			// Log.e("CONNECTION-SERVICE",
			// "Fail during join() mConnectedThread", e);
			// }
			connectedThread.cancel();
			connectedThread = null;
		}
	}

	// Stops all threads
	public synchronized void stop() {
		Log.d("CONNECTION-SERVICE", "stop()");

		setConState(STATE_NONE);
		cancelConnectedThread();
		cancelConnectThread();
	}

	// TODO (wird ein logout benötigt?? --> einfach "stop()")
	// public void logout() {
	// if (getConState() == STATE_LOGEDOUT) {
	// activityHandler
	// .fireToHandler(deviceProviderHandler, STATE_LOGEDOUT);
	// return;
	// }
	// setConState(STATE_LOGOUT);
	// write("at-logout");
	// }

	public boolean checkConnection() {
		// TODO Auto-generated method stub
		return false;
	}

}
