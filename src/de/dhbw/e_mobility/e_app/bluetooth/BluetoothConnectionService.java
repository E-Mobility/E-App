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
import android.util.Log;
import de.dhbw.e_mobility.e_app.ActivityHandler;

public class BluetoothConnectionService {
	// Debugging
	private static final String TAG = "CONNECTION-SERVICE";

	// Member fields
	// private final Handler mHandler;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	private HoldConnectionThread mHoldConnectionThread;
	private int mState;
	private String mPassword;

	// Constants that indicate the current connection state
	public static final int STATE_NONE = 0; // we're doing nothing
	public static final int STATE_CONNECTING = 1; // now initiating an outgoing
													// connection
	public static final int STATE_CONNECTED = 2; // now connected to a remote
	// device
	public static final int STATE_LOGIN = 3; // now loging in to a remote
	public static final int STATE_LOGEDIN = 4; // now loged in to a remote

	private ActivityHandler activityHandler = ActivityHandler.getInstance();

	/**
	 * Constructor. Prepares a new BluetoothConnection session.
	 * 
	 * @param context
	 *            The UI Activity Context
	 * @param theHandler
	 *            A Handler to send messages back to the UI Activity
	 * @param mBluetoothAdapter
	 */
	// public BluetoothConnectionService(Handler theHandler, String thePassword) {
	public BluetoothConnectionService(String thePassword) {
		Log.d(TAG, "BluetoothConnectionService()");

		mState = STATE_NONE;
		// mHandler = theHandler;
		mPassword = thePassword;
	}

	/**
	 * Set the current state of the connection
	 * 
	 * @param theState
	 *            An integer defining the current connection state
	 */
	private synchronized void setState(int theState) {
		Log.d(TAG, "setState() " + mState + " -> " + theState);
		mState = theState;
	}

	/**
	 * Return the current connection state.
	 */
	private synchronized int getState() {
		// Log.d(TAG, "getState() " + mState);
		return mState;
	}

	/**
	 * Start the ConnectThread to initiate a connection to a remote device.
	 * 
	 * @param theDevice
	 *            The BluetoothDevice to connect
	 * @param secure
	 *            Socket Security type - Secure (true) , Insecure (false)
	 */
	public synchronized void connect(BluetoothDevice theDevice) {
		Log.d(TAG, "connect() to " + theDevice);

		// Cancel connect thread for reconnecting
		if (mState == STATE_CONNECTING) {
			cancelConnectThread();
		}
		// Cancel any thread currently running a connection
		cancelConnectedThread();

		// Start the thread to connect with the given device
		mConnectThread = new ConnectThread(theDevice);
		setState(STATE_CONNECTING);
		mConnectThread.start();
	}

	/**
	 * Start the ConnectedThread to begin managing a Bluetooth connection
	 * 
	 * @param theSocket
	 *            The BluetoothSocket on which the connection was made
	 * @param device
	 *            The BluetoothDevice that has been connected
	 */
	private synchronized void connected(BluetoothSocket theSocket) {
		Log.d(TAG, "connected()");

		// Cancel the thread that completed the connection
		// cancelConnectThread(); // NEIN, weil sonst wird der socket
		// geschlossen!
		// try {
		// mConnectThread.join();
		// } catch (InterruptedException e) {
		// Log.e(TAG, "Fail in join() mConnectThread", e);
		// }

		// Cancel any thread currently running a connection
		cancelConnectedThread();

		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread(theSocket);
		setState(STATE_CONNECTED);
		mConnectedThread.start();
	}

	// Login
	private void login() { // TODO synchronized???
		Log.d(TAG, "login()");
		if (getState() == STATE_LOGEDIN) {
			return;
		}
		setState(STATE_LOGIN);
		write(mPassword);
	}

	/**
	 * Write to the ConnectedThread in an unsynchronized manner
	 * 
	 * @param out
	 *            The bytes to write
	 * @see ConnectedThread#write(byte[])
	 */
	public void write(String out) {
		Log.d(TAG, "write()");
		Log.v(TAG, "> " + out);
		out += "\r";

		// Create temporary object
		ConnectedThread tmpThread;
		// Synchronize a copy of the ConnectedThread
		synchronized (this) { // TODO warum ist das so??
			if (mState != STATE_LOGEDIN && mState != STATE_LOGIN) {
				return;
			}
			tmpThread = mConnectedThread;
		}
		// Perform the write unsynchronized
		tmpThread.write(out.getBytes());
	}

	// This methode is doing some stuff
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
			Log.e(TAG, "Fail sleep() during doSomeStuff()", e);
		}
	}

	/**
	 * Stops all threads
	 */
	public synchronized void stop() {
		Log.d(TAG, "stop()");

		setState(STATE_NONE);
		cancelConnectedThread();
		cancelConnectThread();
	}

	// Cancels the mHoldConnectionThread
	private synchronized void cancelHoldConnectionThread() {
		if (mHoldConnectionThread != null) {
			// try { // TODO
			// holdConnectionThread.join();
			// } catch (InterruptedException e) {
			// Log.e(TAG, "Cancel mHoldConnectionThread failed", e);
			// }
			mHoldConnectionThread = null;
		}
	}

	// Cancels the mConnectThread
	private synchronized void cancelConnectThread() {
		if (mConnectThread != null) {
			mConnectThread.cancel();
			// try { // TODO
			// mConnectThread.join();
			// } catch (InterruptedException e) {
			// Log.e(TAG, "Cancel mConnectThread failed", e);
			// }
			mConnectThread = null;
		}
	}

	// Cancels the mConnectedThread
	private synchronized void cancelConnectedThread() {
		// Cancel thread for holding connection
		cancelHoldConnectionThread();

		if (mConnectedThread != null) {
			// try {
			// mConnectedThread.join(); // TODO // TODO
			// } catch (InterruptedException e) {
			// Log.e(TAG, "Fail during join() mConnectedThread", e);
			// }
			mConnectedThread.cancel();
			mConnectedThread = null;
		}
	}

	/**
	 * This thread runs while attempting to make an outgoing connection with a
	 * device. It runs straight through; the connection either succeeds or
	 * fails.
	 */
	private class ConnectThread extends Thread {
		private BluetoothSocket mConnectSocket;

		public ConnectThread(BluetoothDevice theDevice) {
			Log.d(TAG, "ConnectThread() with " + theDevice);

			// Get a BluetoothSocket for a connection with the
			// given BluetoothDevice
			BluetoothSocket tmpSocket = null;
			try {
				Method m = theDevice.getClass().getMethod("createRfcommSocket",
						new Class[] { int.class });
				tmpSocket = (BluetoothSocket) m.invoke(theDevice, 1);
			} catch (SecurityException e) {
				Log.e(TAG,
						"Make createRfcommSocket() failed [SecurityException]",
						e);
			} catch (NoSuchMethodException e) {
				Log.e(TAG,
						"Make createRfcommSocket() failed [NoSuchMethodException]",
						e);
			} catch (IllegalArgumentException e) {
				Log.e(TAG,
						"Run createRfcommSocket() failed [IllegalArgumentException]",
						e);
			} catch (IllegalAccessException e) {
				Log.e(TAG,
						"Run createRfcommSocket() failed [IllegalAccessException]",
						e);
			} catch (InvocationTargetException e) {
				Log.e(TAG,
						"Run createRfcommSocket() failed [InvocationTargetException]",
						e);
			}
			mConnectSocket = tmpSocket;
		}

		public void run() {
			Log.d(TAG, "ConnectThread-run()");

			// Make a connection to the BluetoothSocket
			try {
				// This is a blocking call and will only return on a
				// successful connection or an exception
				mConnectSocket.connect();
				Log.d(TAG, "Connection sucessfull!");
			} catch (IOException e) {
				Log.e(TAG, "Connection failed!", e);
				try {
					mConnectSocket.close();
					mConnectSocket = null;
				} catch (IOException e1) {
					Log.e(TAG, "Closing socket failed", e1);
				}
				return;
			}

			// Start the connected thread
			connected(mConnectSocket);
		}

		private void cancel() {
			Log.d(TAG, "ConnectThread-cancel()");

			try {
				mConnectSocket.close();
				mConnectSocket = null;
			} catch (IOException e) {
				Log.e(TAG, "Closing socket failed", e);
			}
		}
	}

	/**
	 * This thread runs during a connection with a remote device. It handles all
	 * incoming and outgoing transmissions.
	 */
	private class ConnectedThread extends Thread {
		private BluetoothDevice mConnectedDevice;
		private InputStream mConnectedInStream;
		private OutputStream mConnectedOutStream;

		public ConnectedThread(BluetoothSocket theSocket) {
			Log.d(TAG, "ConnectedThread()");

			mConnectedDevice = theSocket.getRemoteDevice();

			// Get the BluetoothSocket input and output streams
			InputStream tmpInStream = null;
			OutputStream tmpOutStream = null;
			try {
				tmpInStream = theSocket.getInputStream();
				tmpOutStream = theSocket.getOutputStream();
			} catch (IOException e) {
				Log.e(TAG, "Couldn't get streams from socket", e);
			}
			mConnectedInStream = tmpInStream;
			mConnectedOutStream = tmpOutStream;
		}

		public void run() {
			Log.d(TAG, "ConnectedThread-run()");

			final BufferedReader reader = new BufferedReader(
					new InputStreamReader(mConnectedInStream));

			// Keep listening to the InputStream while connected
			String line = "";
			try {
				while (line != null) {
					if (BluetoothConnectionService.this.getState() == STATE_NONE) {
						break;
					}
					line = reader.readLine();
					Log.v(TAG, line);
					if (line.equals("error")) {
					} else if (line.equals("ok")) {
						if (BluetoothConnectionService.this.getState() == STATE_LOGIN) {
							setState(STATE_LOGEDIN);

							// Start this thread to hold the connection
							mHoldConnectionThread = new HoldConnectionThread();
							mHoldConnectionThread.start();

							activityHandler
									.fireToHandler(ActivityHandler.UPDATE_BLUETOOTHINFO);

							doSomeStuff();
						}
					} else if (line.equals("login >")) {
						login();
					}
					// StringTokenizer t = new StringTokenizer(line, "\t");
					// if (t.countTokens() >= 5) {
					//
					// }
				}
			} catch (IOException e) {
				Log.e(TAG, "Fail in reading stream", e);
			}
		}

		/**
		 * Write to the connected OutStream.
		 * 
		 * @param buffer
		 *            The bytes to write
		 */
		private void write(byte[] buffer) {
			Log.d(TAG, "ConnectedThread-write()");
			try {
				mConnectedOutStream.write(buffer);
			} catch (IOException e) {
				Log.e(TAG, "Exception during write", e);
			}
		}

		private void cancel() {
			Log.d(TAG, "ConnectedThread-cancel()");

			try {
				// mmOutStream.write("at-push=0".getBytes());
				mConnectedOutStream.write("at-logout".getBytes());

				mConnectedDevice = null;
				mConnectedInStream = null;
				mConnectedOutStream = null;
			} catch (IOException e) {
				Log.e(TAG, "cancel() of connected thread failed", e);
			}
		}

		// Returns connected remote device
		private BluetoothDevice getRemoteDevice() {
			return mConnectedDevice;
		}
	}

	// This thread holds the connection on sending a attention signals without
	// response
	private class HoldConnectionThread extends Thread {

		public HoldConnectionThread() {
		}

		public void run() {
			Log.d(TAG, "HoldConnectionThread-run()");

			// Repeat message every 60 seconds
			while (true) {
				try {
					Thread.sleep(60000);
					write("at-0");
				} catch (InterruptedException e) {
					Log.e(TAG, "Sleep during holding connection failed", e);
				}
			}
		}
	}

	// Returns the currently connected bluetooth device
	public BluetoothDevice getRemoteDevice() {
		if (getState() == STATE_LOGEDIN) {
			if (mConnectedThread != null) {
				return mConnectedThread.getRemoteDevice();
			}
		}
		return null;
	}
}
