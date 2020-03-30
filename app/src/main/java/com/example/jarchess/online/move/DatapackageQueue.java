package com.example.jarchess.online.move;

import android.util.Log;

import com.example.jarchess.online.datapackage.Datapackage;

import java.util.concurrent.LinkedBlockingQueue;

public class DatapackageQueue {
    private final String TAG = "DatapackageQueue";
    private LinkedBlockingQueue<Datapackage> localDatapackageQueue;
    private LinkedBlockingQueue<Datapackage> networkDatapackageQueue;
    private String gameId;

    public DatapackageQueue() {
        this.localDatapackageQueue = new LinkedBlockingQueue<Datapackage>();
        this.networkDatapackageQueue = new LinkedBlockingQueue<Datapackage>();
    }

    public Datapackage getClientBoundDatapackage() {
        try {
            Datapackage datapackage = localDatapackageQueue.take();
            Log.i(TAG, "getLocalDatapackage: " + datapackage);
            return datapackage;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Datapackage getServerBoundDatapackageQueue() {
        try {
            Datapackage datapackage = networkDatapackageQueue.take();
            Log.i(TAG, "getNetworkDatapackageQueue: " + datapackage);
            return datapackage;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insertClientBoundDatapackageQueue(Datapackage datapackage) {
        try {
            Log.i(TAG, "insertLocalDatapackageQueue: " + datapackage);
            localDatapackageQueue.put(datapackage);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void insertServerBoundDatapackageQueue(Datapackage datapackage) {
        try {
            Log.i(TAG, "insertNetworkDatapackageQueue: " + datapackage);
            networkDatapackageQueue.put(datapackage);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
