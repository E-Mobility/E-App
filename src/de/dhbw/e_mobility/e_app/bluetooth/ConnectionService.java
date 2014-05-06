package de.dhbw.e_mobility.e_app.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;

import de.dhbw.e_mobility.e_app.R;
import de.dhbw.e_mobility.e_app.common.ActivityHandler;
import de.dhbw.e_mobility.e_app.common.IntentKeys;
import de.dhbw.e_mobility.e_app.speedo.SpeedoValues;

public class ConnectionService {

    // Get ActivityHandler object
    private ActivityHandler activityHandler = ActivityHandler.getInstance();

    // Threads
    private SetupConnection setupConnection;
    private ActiveConnection activeConnection;
    private HoldConnection holdConnection;
    private LoginTimeout loginTimeout;

    // Constructor
    public ConnectionService() {
        setupConnection = null;
        activeConnection = null;
        holdConnection = null;
        loginTimeout = null;
    }

    // Checks the if there is a connection currently
    public void checkConnectionAndLogin(BluetoothDevice device) {
        if (isLoggedIn()) {
            fireToHandler(BluetoothInfoState.LOGGED_IN);
            return;
        }
        connect(device);
    }

    // Returns true if active connection thread is running
    private boolean isLoggedIn() {
        return (activeConnection != null);
    }

    // Starts the SetupConnection thread
    private void connect(BluetoothDevice theDevice) {
        // First close all threads if they are running
        closeAllThreads();

        // Start the thread to connect with the given device
        setupConnection = new SetupConnection(theDevice);
        setupConnection.start();
    }

    // Starts the ActiveConnection
    private void connected(BluetoothSocket theSocket) {
        // Start the thread to handle the data transfer with the controller
        activeConnection = new ActiveConnection(theSocket);
        activeConnection.start();
    }

    // Saves the pushed values
    private void savePushValues(String pushValues) {
        String typ = Command.AT_PUSH_N.getValue();
        if (typ != null) {
            if (typ.equals("1")) {
                String tmp[];
                for (String line : pushValues.split("\n")) {
                    tmp = line.split("[\t\\s]+");
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
        HashMap<String, Command> bluetooth_commands = activityHandler
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

    // Sends a command to the controller
    public void sendCommand(Command command) {
        if (isLoggedIn()) {
            activeConnection.send(command);
        }
    }

    // Fires a state update to the handler
    private void fireToHandler(BluetoothInfoState theState) {
        activityHandler.fireToHandler(IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(), theState);
    }

    // Logout from controller and close the socket
    public void logout() {
        sendCommand(Command.AT_LOGOUT);
        closeAllThreads();
    }

    // Cancels all threads
    public void closeAllThreads() {
        if (holdConnection != null) {
            holdConnection.interrupt();
            holdConnection = null;
        }
        if (loginTimeout != null) {
            loginTimeout.interrupt();
            loginTimeout = null;
        }
        if (activeConnection != null) {
            activeConnection.interrupt();
            activeConnection.closeThread();
            activeConnection = null;
        }
        if (setupConnection != null) {
            setupConnection.interrupt();
            setupConnection.closeThread();
            setupConnection = null;
        }
    }

    // This thread builds the socket and tries to connect it with the controller
    private class SetupConnection extends Thread {
        private BluetoothSocket bluetoothSocket;

        public SetupConnection(BluetoothDevice theDevice) {
            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            BluetoothSocket tmpSocket = null;
            try {
                Method m = theDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                tmpSocket = (BluetoothSocket) m.invoke(theDevice, 1);
            } catch (Exception e) {
                Log.e("CONNECTION-SERVICE", "Make createRfcommSocket() failed [" + e.getClass().getSimpleName().toString() + "]", e);
            }
            bluetoothSocket = tmpSocket;
        }

        @Override
        public void run() {
            // Make a connection to the BluetoothSocket
            try {
                bluetoothSocket.connect();
                Log.d("CONNECTION-SERVICE", "Connection successful!");
            } catch (IOException e) {
                Log.d("CONNECTION-SERVICE", "Connection failed!");
                fireToHandler(BluetoothInfoState.CONNECTION_FAILED);
                closeAllThreads();
                return;
            }
            // Start the connected thread
            connected(bluetoothSocket);
        }

        // Reset all components thread components
        private void closeThread() {
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

    // Handles the active connection (reading and writing the socket)
    private class ActiveConnection extends Thread {
        // Stream attributes
        private InputStream inputStream;
        private OutputStream outputStream;

        // Attributes to save the things before
        private Command previousCommand;
        private String previousPush;

        public ActiveConnection(BluetoothSocket theSocket) {
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
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream));

            // Keep listening to the InputStream while connected
            try {
                String line;
                while (!isInterrupted()) {
                    line = reader.readLine();
                    if (!line.equals("")) {
                        if (previousCommand != null) { // TODO delete this if/else statement
                            Log.d("CONNECTION-SERVICE", "LASTCOM: " + previousCommand.toString());
                        } else {
                            Log.d("CONNECTION-SERVICE", "LASTCOM: NULL");
                        }
                        Log.v("CONNECTION-SERVICE", line);
                        if (line.startsWith("error")) {
                            // TODO reaction of error (unimportant)
                        } else if (line.startsWith("ok")) {
                            if (isPreviousCommand(Command.LOGIN)) {
                                // Interrupt the timeout
                                loginTimeout.interrupt();

                                // Start the HoldConnection
                                holdConnection = new HoldConnection();
                                holdConnection.start();
                                fireToHandler(BluetoothInfoState.LOGGED_IN);
                            }
                            restartPush();
                        } else if (line.equals("login >")) {
                            if (loginTimeout == null) {
                                // Start timer for login
                                loginTimeout = new LoginTimeout();
                                loginTimeout.start();
                            }
                            send(Command.LOGIN);
                        } else if (isPreviousCommand(Command.AT_PARAM_LIST)) {
                            // Saving the pushed parameter list
                            saveParamList(line);
                            if (line.startsWith(Command.AT_CALIBH_N.getCommand())) {
                                // Last element in parameter list
                                restartPush();
                            }
                        } else {
                            // Saving the pushed values
                            savePushValues(line);
                        }
                    }
                }
            } catch (IOException e) {
                Log.d("CONNECTION-SERVICE", "Fail in reading stream");
            }
            try {
                // Close the BufferedReader
                reader.close();
            } catch (IOException e) {
                Log.e("CONNECTION-SERVICE", "Fail in closing the BufferedReader");
            }
            closeAllThreads();
        }

        // Checks if the last command is the given on
        private boolean isPreviousCommand(Command command) {
            if (command == previousCommand) {
                return true;
            }
            return false;
        }

        // Restart pushing data
        private void restartPush() {
            Log.d("PUSH-RESTART", ":" + previousPush);
            if (previousCommand != Command.AT_PUSH_N) {
                if (previousPush != null) {
                    Command.AT_PUSH_N.setValue(previousPush);
                    send(Command.AT_PUSH_N);
                }
            }
        }

        // Stop pushing data
        private void stopPush() {
            previousPush = Command.AT_PUSH_N.getValue();
            Log.d("PUSH-STOP", ":" + previousPush);
            Command.AT_PUSH_N.setValue("0");
            send(Command.AT_PUSH_N);
        }

        // Sends a command
        private void send(Command command) {
            if (command != Command.AT_0) {
                if (command != Command.LOGIN && command != Command.AT_PUSH_N) {
                    stopPush();
                }
                // Save previous sent command
                previousCommand = command;
            }
            Log.v("CONNECTION-SERVICE", "> " + command.toString());
            write((command.toString() + "\r").getBytes());
        }

        // Write to the connected OutputStream
        private void write(byte[] buffer) {
            try {
                outputStream.write(buffer);
            } catch (IOException e) {
                Log.d("CONNECTION-SERVICE", "Exception during writing");
            }
        }

        // Reset all components thread components
        private void closeThread() {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    Log.e("CONNECTION-SERVICE", "Exception while closing the inputStream");
                }
                inputStream = null;
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    Log.e("CONNECTION-SERVICE", "Exception while closing the outputStream");
                }
                outputStream = null;
            }
            previousCommand = null;
            previousPush = null;
        }
    }

    // This thread holds the connection on sending attention simple signals
    private class HoldConnection extends Thread {
        @Override
        public void run() {
            // Repeat message every 60 seconds
            while (!isInterrupted()) {
                try {
                    Thread.sleep(60000);
                    sendCommand(Command.AT_0);
                } catch (InterruptedException e) {
                    Log.d("CONNECTION-SERVICE", "Sleep was interrupted");
                    break;
                }
            }
        }
    }

    // This thread is waiting for login success or interrupt it after a while
    private class LoginTimeout extends Thread {
        @Override
        public void run() {
            try {
                int ms = 2000;
                Thread.sleep(ms);
                if (isLoggedIn()) {
                    Log.d("CONNECTION-SERVICE", "Login timed out after " + String.valueOf(ms) + " ms");
                    fireToHandler(BluetoothInfoState.LOGIN_TIMEOUT);
                    closeAllThreads();
                    fireToHandler(BluetoothInfoState.CONNECTION_FAILED);
                }
            } catch (InterruptedException e) {
                Log.d("CONNECTION-SERVICE", "Sleep was interrupted");
            }
        }
    }
}
