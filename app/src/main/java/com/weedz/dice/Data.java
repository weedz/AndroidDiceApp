package com.weedz.dice;

import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Observable;

/**
 * Created by WeeDz on 2016-08-07.
 */
public class Data extends Observable {
    private static final String TAG = "Data";

    private static Data ourInstance = new Data();
    public static Data getInstance() {
        return ourInstance;
    }

    private ArrayList<Integer> dice;

    private RollThread mRollThread;
    private RollHandler handler = new RollHandler();

    private Data() {
        dice = new ArrayList<>();
        dice.add(0);
    }

    public void setUpdate() {
        setChanged();
        notifyObservers();
    }

    public void addDie(int nr) {
        /*for (int i = 0; i < nr; i++) {
            dice.add(0);
        }*/
        if (mRollThread != null && mRollThread.isAlive()) {
            mRollThread.interrupt();
        }
        if (nr > 1000-dice.size()) {
            nr = 1000-dice.size();
        }
        dice.addAll(Arrays.asList(new Integer[nr]));
        setUpdate();
    }
    public void removeDie(int nr) {
        if (mRollThread != null && mRollThread.isAlive()) {
            mRollThread.interrupt();
        }
        for (int i = 0; i < nr; i++) {
            if (dice.size() > 0) {
                dice.remove(0);
            } else {
                break;
            }
        }
        setUpdate();
    }
    public void setDice(int nr) {
        if (mRollThread != null && mRollThread.isAlive()) {
            mRollThread.interrupt();
        }
        dice.clear();
        addDie(nr);
        setUpdate();
    }

    // TODO: run in a separate thread?
    public void roll() {
        /*if (mRollThread != null && !mRollThread.isAlive()) {
            mRollThread.interrupt();
            mRollThread = null;
        }
        if (mRollThread == null) {
            mRollThread = new RollThread();
            mRollThread.start();
        }*/
        for(int i=0; i < dice.size(); i++) {
            dice.set(i, (int) (Math.random()*6+1));
        }
        setUpdate();
    }
    public int getTotal() {
        if (mRollThread != null && mRollThread.isAlive()) {
            return 0;
        }
        int total = 0;
        for(int i=0; i < dice.size(); i++) {
            total += dice.get(i);
        }
        return total;
    }
    public int getDie(int i) {
        if (mRollThread != null && mRollThread.isAlive()) {
            return 0;
        }
        if (dice.size() >= i) {
            return dice.get(i);
        }
        return 0;
    }

    public int getNrOfDie() {
        return dice.size();
    }

    static class RollHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            if (!Thread.currentThread().isInterrupted()) {
                //Log.print(msg.what);
                switch (msg.what) {
                    case 1: // Roll finished
                        Data.getInstance().setUpdate();
                        break;
                }
            }
            super.handleMessage(msg);
        }
    }

    private class RollThread extends Thread {
        public void run() {
            for(int i=0; i < dice.size(); i++) {
                dice.set(i, (int) (Math.random()*6+1));
            }
            Log.print(Data.getInstance().dice.get(0));
            handler.obtainMessage(1).sendToTarget();
        }
    }
}
