package de.dhbw.e_mobility.e_app.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import de.dhbw.e_mobility.e_app.common.ActivityHandler;
import de.dhbw.e_mobility.e_app.common.IntentKeys;
import de.dhbw.e_mobility.e_app.speedo.SpeedoValues;

public class ConnectionService {

    // Current ServiceState
    private ServiceState serviceState;

    // Attributes to save the things before
    private Commands lastCommand;
    private String lastPush;

    // Get ActivityHandler object
    private ActivityHandler activityHandler = ActivityHandler.getInstance();

    // Threads
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private HoldConnectionThread holdConnectionThread;

    // Constructor
    public ConnectionService() {
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
                    IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
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
        cancelConnectThread();

        // Cancel any thread currently running a connection
        cancelConnectedThread();

        // Start the thread to connect with the given device
        connectThread = new ConnectThread(theDevice);
        serviceState = ServiceState.CONNECTING;
        connectThread.start();
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

    // Sends a command to the controller
    private void write(Commands command) {
        if (serviceState != ServiceState.LOGGED_IN
                && serviceState != ServiceState.LOGIN) {
            return;
        }
        if (connectedThread != null) {
            connectedThread.write(command);
        }
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

    // Saves the pushed values
    private void savePushValues(String pushValues) {
        String typ = Commands.AT_PUSH_N.getValue();
        if (typ != null) {
            if (typ.equals("1")) {
                String tmp[];
                for (String line : pushValues.split("\n")) {
                    tmp = line.split("[\\s]+");
                    if (tmp.length > 6) {
                        SpeedoValues.U.setValue(Float.valueOf(tmp[0]) / 10); // from 1/10 V to V
                        SpeedoValues.I.setValue(Float.valueOf(tmp[1]) / 1000); // from mA to A
                        SpeedoValues.V.setValue(Float.valueOf(tmp[2]) / 10); // from 1/10 km/h to km/h
                        SpeedoValues.C.setValue(Float.valueOf(tmp[3]) / 1000 / 60 / 60); // from mAs to Ah
                        SpeedoValues.M.setValue(Float.valueOf(tmp[4]) / 1000); // from mA to A
                        SpeedoValues.D.setValue(Float.valueOf(tmp[5]));
                        SpeedoValues.P.setValue(Float.valueOf(tmp[6]));
                    }
                }
            } else if (typ.equals("2")) {
                // Prepare the commands
                HashMap<String, SpeedoValues> speedo_values = activityHandler
                        .getSpeedoValues();

                String command_text;
                String value;
                String[] tmp;
                for (String line : pushValues.split("\n")) {
                    tmp = line.split("[:\\s]+");
                    if (tmp.length > 2) {
                        command_text = tmp[1];
                        value = tmp[2].replace(",", ".");

                        if (speedo_values.containsKey(command_text)) {
                            speedo_values.get(command_text).setValue(Float.valueOf(value));
                        }
                    }
                }
            }
        }
    }

    // Saves the received parameter list
    private void saveParamList(String paramList) {
        // Prepare the commands
        HashMap<String, Commands> bluetooth_commands = activityHandler
                .getBluetoothCommands();

        // Read the parameter list
        String command_text;
        String value;
        String[] tmp;
        for (String line : paramList.split("\n")) {
            tmp = line.split("=");
            if (tmp.length > 1) {
                command_text = tmp[0];
                value = tmp[1];

                if (bluetooth_commands.containsKey(command_text)) {
                    bluetooth_commands.get(command_text).setValue(value);
                }
            }
        }
    }

    // Sends the wanted command
    public void sendCommand(Commands command) {
        write(command);
    }

    // Stops all threads
    public synchronized void stop() {
        Log.d("CONNECTION-SERVICE", "stop()");
        serviceState = ServiceState.LOGOUT;
        cancelHoldConnectionThread();
        cancelConnectedThread();
    }

    // Different states of the controller connection
    private enum ServiceState {
        INIT, LOGOUT, CONNECTING, CONNECTED, LOGIN, LOGGED_IN
    }

    /**
     * This thread runs while attempting to make an outgoing connection with a
     * device. It runs straight through; the connection either succeeds or
     * fails.
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket bluetoothSocket;

        public ConnectThread(BluetoothDevice theDevice) {
            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            BluetoothSocket tmpSocket = null;
            try {
                Method m = theDevice.getClass().getMethod("createRfcommSocket",
                        new Class[]{int.class});
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

                // activityHandler.fireToast("Keine Verbindung moeglich!");
                // activityHandler.fireToHandler(
                // ActivityHandler.HANDLLER_DEVICE_PROVIDER,
                // BluetoothInfoState.DISCONNECTED);
                // TODO Fire a toast message

                serviceState = ServiceState.INIT;
                bluetoothSocket = null;
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
                Log.e("CONNECTION-SERVICE", "Couldn't get streams from socket", e);
            }
            inputStream = tmpInStream;
            outputStream = tmpOutStream;
        }

        @Override
        public void run() {
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream));

            // Keep listening to the InputStream while connected
            try {
                String line;
                while (true) {
                    if (serviceState == ServiceState.INIT) {
                        break;
                    }
                    line = reader.readLine();
                    if (!line.equals("")) {
                        if (lastCommand != null) { // TODO delete this if/else statement
                            Log.d("CONNECTION-SERVICE", "LASTCOM: " + lastCommand.toString());
                        } else {
                            Log.d("CONNECTION-SERVICE", "LASTCOM: NULL");
                        }
                        Log.v("CONNECTION-SERVICE", line);
                        if (line.startsWith("error")) {
                            // TODO reaction of error (unimportant)
                        } else if (line.startsWith("ok")) {
                            if (isLastCommand(Commands.LOGIN)) {
                                serviceState = ServiceState.LOGGED_IN;

                                // Start the HoldConnectionThread
                                holdConnectionThread = new HoldConnectionThread();
                                holdConnectionThread.start();
                                activityHandler
                                        .fireToHandler(
                                                IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                                                BluetoothInfoState.LOGGED_IN);

                                // Asking for parameter list
                                write(Commands.AT_PARAM_LIST);
                            } else if (isLastCommand(Commands.AT_LOGOUT)) {
                                serviceState = ServiceState.INIT;
                                init();
                                activityHandler
                                        .fireToHandler(
                                                IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(),
                                                BluetoothInfoState.NONE);
                                cancelConnectThread();
                            }
                        } else if (line.equals("login >")) {
                            serviceState = ServiceState.LOGIN;
                            write(Commands.LOGIN);
                        } else if (isLastCommand(Commands.AT_PARAM_LIST)) {
                            if (line.startsWith(Commands.AT_CALIBH_N.getCommand())) {
                                // Last element in parameter list
                                lastCommand = null;
                                Log.d("CONNECTION-SERVICE", "End of Param-List");
                            }
                            saveParamList(line);
                        } else {
                            savePushValues(line);
                        }

                        // Start push after last command is finished
                        if (lastCommand != Commands.AT_PUSH_N) {
                            // TODO pruefen ob das korrekt ist
                            Commands.AT_PUSH_N.setValue(lastPush);
                            write(Commands.AT_PUSH_N);
                        }
                    }
                }
            } catch (IOException e) {
                Log.e("CONNECTION-SERVICE", "Fail in reading stream", e);
            }
        }

        // Checks if the last command is the given on
        private boolean isLastCommand(Commands command) {
            if (command == lastCommand) {
                if (command != Commands.AT_PARAM_LIST) {
                    lastCommand = null;
                }
                return true;
            }
            return false;
        }

        // Write a command
        private void write(Commands command) {
            if (command != Commands.AT_0) {
                // Save as last send command
                lastCommand = command;

                // Hold pushing data
                if (command != Commands.AT_PUSH_N) {
                    // TODO pruefen ob das korrekt ist
                    lastPush = Commands.AT_PUSH_N.getValue();
                    Commands.AT_PUSH_N.setValue("0");
                    write(Commands.AT_PUSH_N);

                }
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
            write(Commands.AT_LOGOUT);
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
                    write(Commands.AT_0);
                } catch (InterruptedException e) {
                    Log.d("CONNECTION-SERVICE", "Sleep was interrupted");
                    break;
                }
            }
        }
    }
}
