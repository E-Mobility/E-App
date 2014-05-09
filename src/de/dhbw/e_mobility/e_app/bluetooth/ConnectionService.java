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
import java.util.LinkedList;
import java.util.Queue;

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
    private HashMap<String, Timeout> timeouts;

    // Queue for commands
    private Queue<String> commandQueue;

    // Constructor
    public ConnectionService() {
        setupConnection = null;
        activeConnection = null;
        holdConnection = null;
        timeouts = new HashMap<String, Timeout>();
        commandQueue = new LinkedList<String>();
    }

    // Checks the if there is a connection currently
    public void checkConnectionAndLogin(BluetoothDevice device) {
        if (isLoggedIn()) {
            fireToHandler(BluetoothInfoState.LOGGED_IN);
            return;
        }
        connect(device);
    }

    // Returns true if hold connection thread is running
    private boolean isLoggedIn() {
        return (holdConnection != null);
    }

    // Returns true if active connection thread is running
    private boolean isConnected() {
        // return (activeConnection != null);
        return isLoggedIn();
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
            Log.d("PARAMLIST", line);
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

    // Saves the command in the queue
    public void sendCommand(Command command) {
        // Commands can only be sent when you are logged in
        if (isLoggedIn()) {
            String tmpPushVal = null;
            // If command is not push_n you have to pause it
            if (command != Command.AT_PUSH_N) {
                tmpPushVal = Command.AT_PUSH_N.getValue();
                Command.AT_PUSH_N.setValue("0");
                commandQueue.add(Command.AT_PUSH_N.toString());
            }
            commandQueue.add(command.toString());
            if (tmpPushVal != null && command != Command.AT_LOGOUT) {
                Command.AT_PUSH_N.setValue(tmpPushVal);
                commandQueue.add(Command.AT_PUSH_N.toString());
            }
            // Start sending commands
            if (activeConnection != null) {
                activeConnection.sendNextCommand();
            }
        }
    }

    // Fires a state update to the handler
    private void fireToHandler(BluetoothInfoState theState) {
        activityHandler.fireToHandler(IntentKeys.HANDLLER_DEVICE_PROVIDER.getValue(), theState);
    }

    // Logout from controller and close the socket
    public void logout() {
        if (activeConnection != null) {
            activeConnection.send(Command.AT_LOGOUT);
        }
        closeAllThreads();
    }

    // Cancels all threads
    public void closeAllThreads() {
        if (holdConnection != null) {
            holdConnection.interrupt();
            holdConnection = null;
        }
        for (Timeout timeout : timeouts.values()) {
            if (timeout != null) {
                timeout.interrupt();
            }
        }
        timeouts.clear();
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

        // Also clear command queue
        commandQueue.clear();
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
                Log.e("CONNECTION-SERVICE", "Make createRfcommSocket() failed [" + e.getClass().getSimpleName() + "]", e);
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
        private String previousCommand;
        private boolean currentlySending;

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

            currentlySending = false;
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
                        if (previousCommand != null) {
                            Log.d("CONNECTION-SERVICE", "previousCommand: " + previousCommand);
                        } else {
                            Log.d("CONNECTION-SERVICE", "previousCommand: NULL");
                        }
                        Log.v("CONNECTION-SERVICE", line);
                        if (line.startsWith("ATE0")) {
                            // Start timer for connection
                            if (!timeouts.containsKey(ConnectTimeout.class.getName())) {
                                Log.d("TIMEOUT-THREAD", "StartConnect");
                                ConnectTimeout connectTimeout = new ConnectTimeout();
                                connectTimeout.start();
                                timeouts.put(ConnectTimeout.class.getName(), connectTimeout);
                            }
                            // Send an attention command
                            send(Command.AT_0);

                            // line.startsWith("error")) { reaction of error unimportant
                            //  closeAllThreads();
                            //  fireToHandler(BluetoothInfoState.CONNECTION_FAILED);
                        } else if (line.startsWith("login >")) {
                            // Interrupt connect timer
                            if (timeouts.containsKey(ConnectTimeout.class.getName())) {
                                Log.d("TIMEOUT-THREAD", "InterruptConnect");
                                timeouts.get(ConnectTimeout.class.getName()).interrupt();
                                timeouts.remove(ConnectTimeout.class.getName());
                            }
                            // Start timer for connection
                            if (!timeouts.containsKey(LoginTimeout.class.getName())) {
                                Log.d("TIMEOUT-THREAD", "StartLogin");
                                LoginTimeout loginTimeout = new LoginTimeout();
                                loginTimeout.start();
                                timeouts.put(LoginTimeout.class.getName(), loginTimeout);
                            }
                            send(Command.LOGIN);
                        } else if (isPreviousCommand(Command.AT_PARAM_LIST)) {
                            if (line.startsWith(Command.AT_CALIBH_N.getCommand())) {
                                // Last element in parameter list
                                resetAndSendNextCommand();
                            }
                            // Saving the pushed parameter list
                            saveParamList(line);
                        } else if (line.startsWith("ok")) {
                            if (isPreviousCommand(Command.LOGIN)) {
                                // Interrupt login timer
                                if (timeouts.containsKey(LoginTimeout.class.getName())) {
                                    Log.d("TIMEOUT-THREAD", "InterruptLogin");
                                    timeouts.get(LoginTimeout.class.getName()).interrupt();
                                    timeouts.remove(LoginTimeout.class.getName());
                                }

                                // Start the HoldConnection
                                Log.d("TIMEOUT-THREAD", "StartHold");
                                holdConnection = new HoldConnection();
                                holdConnection.start();
                                fireToHandler(BluetoothInfoState.LOGGED_IN);
                            }
                            resetAndSendNextCommand();
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
            return (command.toString().equals(previousCommand));
        }

        // Sends a command
        private void send(Command command) {
            send(command.toString());
        }

        private void send(String command) {
            if (!command.equals(Command.AT_0.toString())) {
                // Save previous sent command
                previousCommand = command;
            }
            Log.v("CONNECTION-SERVICE", "> " + command);
            write((command + "\r").getBytes());
        }

        // Sends the next command from queue
        private void sendNextCommand() {
            // Sending just one command at the same time
            if (!currentlySending) {

                // Interrupt timer for command
                if (timeouts.containsKey(CommandTimeout.class.getName())) {
                    timeouts.get(CommandTimeout.class.getName()).interrupt();
                    timeouts.remove(CommandTimeout.class.getName());
                }

                String tmpCommand = commandQueue.poll();
                if (tmpCommand != null) {
                    send(tmpCommand);

                    // Start timer for command
                    if (!timeouts.containsKey(CommandTimeout.class.getName())) {
                        CommandTimeout commandTimeout = new CommandTimeout();
                        commandTimeout.start();
                        timeouts.put(CommandTimeout.class.getName(), commandTimeout);
                    }
                }
            }
        }

        // Resets the currently sending attribute
        public void resetAndSendNextCommand() {
            currentlySending = false;
            sendNextCommand();
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
                    if (activeConnection != null) {
                        activeConnection.send(Command.AT_0);
                    }
                } catch (InterruptedException e) {
                    Log.d("CONNECTION-SERVICE", "Sleep was interrupted");
                    break;
                }
            }
        }
    }

    // This thread is waiting for login success or interrupt it after a while
    private class LoginTimeout extends Timeout {

        @Override
        protected String getTopic() {
            return "Login";
        }

        @Override
        protected int getMs() {
            return 2000;
        }

        @Override
        protected boolean isJobDone() {
            return isLoggedIn();
        }

        @Override
        protected void fireTimeOutMessages() {
            fireToHandler(BluetoothInfoState.LOGIN_TIMEOUT);
            closeAllThreads();
            fireToHandler(BluetoothInfoState.CONNECTION_FAILED);
        }
    }

    // This thread is waiting for connection success or interrupt it after a while
    private class ConnectTimeout extends Timeout {

        @Override
        protected String getTopic() {
            return "Connect";
        }

        @Override
        protected int getMs() {
            return 2000;
        }

        @Override
        protected boolean isJobDone() {
            return isConnected();
        }

        @Override
        protected void fireTimeOutMessages() {
            closeAllThreads();
            fireToHandler(BluetoothInfoState.CONNECTION_FAILED);
        }
    }

    // This thread is waiting for sending the next command
    private class CommandTimeout extends Timeout {

        @Override
        protected String getTopic() {
            return "Command";
        }

        @Override
        protected int getMs() {
            return 1500;
        }

        @Override
        protected boolean isJobDone() {
            return false;
        }

        @Override
        protected void fireTimeOutMessages() {
            activeConnection.resetAndSendNextCommand();
//            activeConnection.sendNextCommand();
        }
    }

    // This is a abstract waiting thread
    private abstract class Timeout extends Thread {
        @Override
        public void run() {
            try {
                int ms = getMs();
                Thread.sleep(ms);
                if (!isJobDone()) {
                    Log.d("CONNECTION-SERVICE", "(" + getTopic() + ") Time out after " + String.valueOf(ms) + " ms");

                    fireTimeOutMessages();
                }
            } catch (InterruptedException e) {
                Log.d("CONNECTION-SERVICE", "(" + getTopic() + ") Sleep was interrupted");
            }
        }

        protected abstract String getTopic();

        protected abstract int getMs();

        protected abstract boolean isJobDone();

        protected abstract void fireTimeOutMessages();
    }
}
