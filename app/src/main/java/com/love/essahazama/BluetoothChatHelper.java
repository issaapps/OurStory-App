package com.love.essahazama;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothChatHelper {
    private static final String TAG = "BluetoothChatHelper";
    private static final String NAME = "BluetoothChat";
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    public BluetoothChatHelper(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }

    private synchronized void setState(int state) {
        mState = state;
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized void start() {
        if (mConnectThread != null) { mConnectThread.cancel(); mConnectThread = null; }
        if (mConnectedThread != null) { mConnectedThread.cancel(); mConnectedThread = null; }
        
        // التأكد من أن البلوتوث مفعل قبل البدء
        if (mAdapter == null || !mAdapter.isEnabled()) {
            Log.e(TAG, "Bluetooth is not enabled or not available");
            setState(STATE_NONE);
            return;
        }

        setState(STATE_LISTEN);
        if (mAcceptThread == null) {
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    public synchronized void connect(BluetoothDevice device) {
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) { mConnectThread.cancel(); mConnectThread = null; }
        }
        if (mConnectedThread != null) { mConnectedThread.cancel(); mConnectedThread = null; }
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (mConnectThread != null) { mConnectThread.cancel(); mConnectThread = null; }
        if (mConnectedThread != null) { mConnectedThread.cancel(); mConnectedThread = null; }
        if (mAcceptThread != null) { mAcceptThread.cancel(); mAcceptThread = null; }

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        if (device != null) {
            bundle.putString(Constants.DEVICE_NAME, device.getName());
        }
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    public synchronized void stop() {
        if (mConnectThread != null) { mConnectThread.cancel(); mConnectThread = null; }
        if (mConnectedThread != null) { mConnectedThread.cancel(); mConnectedThread = null; }
        if (mAcceptThread != null) { mAcceptThread.cancel(); mAcceptThread = null; }
        setState(STATE_NONE);
    }

    public void write(byte[] out) {
        ConnectedThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        if (r != null) r.write(out);
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        public AcceptThread() {
            BluetoothServerSocket tmp = null;
            try {
                if (mAdapter != null) {
                    tmp = mAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
                }
            } catch (IOException e) { Log.e(TAG, "listen() failed", e); }
            mmServerSocket = tmp;
        }
        public void run() {
            BluetoothSocket socket = null;
            while (mState != STATE_CONNECTED) {
                try {
                    if (mmServerSocket != null) {
                        socket = mmServerSocket.accept();
                    } else {
                        break;
                    }
                } catch (IOException e) { break; }
                
                if (socket != null) {
                    synchronized (BluetoothChatHelper.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                try { socket.close(); } catch (IOException e) {}
                                break;
                        }
                    }
                }
            }
        }
        public void cancel() {
            try { if (mmServerSocket != null) mmServerSocket.close(); } catch (IOException e) {}
        }
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {}
            mmSocket = tmp;
        }
        public void run() {
            if (mAdapter != null) mAdapter.cancelDiscovery();
            try {
                if (mmSocket != null) {
                    mmSocket.connect();
                } else {
                    setState(STATE_LISTEN);
                    return;
                }
            } catch (IOException e) {
                try { if (mmSocket != null) mmSocket.close(); } catch (IOException e2) {}
                setState(STATE_LISTEN);
                return;
            }
            synchronized (BluetoothChatHelper.this) { mConnectThread = null; }
            connected(mmSocket, mmDevice);
        }
        public void cancel() {
            try { if (mmSocket != null) mmSocket.close(); } catch (IOException e) {}
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {}
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    if (mmInStream != null) {
                        bytes = mmInStream.read(buffer);
                        mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    } else {
                        break;
                    }
                } catch (IOException e) {
                    setState(STATE_LISTEN);
                    break;
                }
            }
        }
        public void write(byte[] buffer) {
            try {
                if (mmOutStream != null) {
                    mmOutStream.write(buffer);
                    mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
                }
            } catch (IOException e) {}
        }
        public void cancel() {
            try { if (mmSocket != null) mmSocket.close(); } catch (IOException e) {}
        }
    }
}
