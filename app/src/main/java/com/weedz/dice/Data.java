package com.weedz.dice;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by WeeDz on 2016-08-07.
 */
public class Data extends Observable {
    private static final String TAG = "Data";

    private static Data ourInstance = new Data();
    public static Data getInstance() {
        return ourInstance;
    }

    // Flag values
    public static final int FLAG_DICESET_UPDATE = 0;
    public static final int FLAG_DICE_ROLL = 1;
    public static final int FLAG_THREAD_LOCK = 2;
    public static final int FLAG_INTERRUPTED = 3;
    // flags
    private Boolean[] flags = new Boolean[4];

    // Different sized dice
    private ConcurrentHashMap<Integer, ArrayList<Integer>> multiDice;

    private int mTotal = 0;

    private RollThread mRollThread;
    private WeakReference<Data> ref = new WeakReference<>(this);
    private RollHandler handler = new RollHandler(ref);

    private Data() {
        Arrays.fill(flags, false);

        multiDice = new ConcurrentHashMap<>();
        multiDice.put(6, new ArrayList<Integer>());
        multiDice.get(6).add(0);

    }

    private void setUpdate() {
        setChanged();
        notifyObservers();
    }

    public boolean getFlag(int flag) {
        return flags[flag];
    }
    public void setFlag(int flag, Boolean b) {
        flags[flag] = b;
    }

    // MultiDice
    public synchronized void addMultiDice(int nr, int sides) {
        if (mRollThread != null && mRollThread.isAlive()) {
            mRollThread.interrupt();
        }
        if (sides <= 0 || nr <= 0) {
            return;
        }
        if (multiDice.get(sides) == null) {
            multiDice.put(sides, new ArrayList<Integer>());
        }

        multiDice.get(sides).addAll(Arrays.asList(new Integer[nr]));

        setFlag(FLAG_DICESET_UPDATE, true);
        setUpdate();
    }
    public synchronized void removeMultiDice(int nr, int sides) {
        if (mRollThread != null && mRollThread.isAlive()) {
            mRollThread.interrupt();
        }
        if (sides <= 0 || nr <= 0) {
            return;
        }
        if (multiDice.get(sides) != null) {
            int count = 0;
            while(count < nr) {
                multiDice.get(sides).remove(0);
                if (multiDice.get(sides).size() == 0) {
                    multiDice.remove(sides);
                    break;
                }
                count++;
            }
        }
        setFlag(FLAG_DICESET_UPDATE, true);
        setUpdate();
    }
    public synchronized void setMultiDice(int nr, int sides) {
        if (mRollThread != null && mRollThread.isAlive()) {
            mRollThread.interrupt();
        }
        multiDice.clear();
        if (nr > 0 && sides > 0) {
            addMultiDice(nr, sides);
        } else {
            setFlag(FLAG_DICESET_UPDATE, true);
            setUpdate();
        }
    }
    public synchronized int getMultiDie(int sides, int i) {
        if (mRollThread != null && mRollThread.isAlive()) {
            mRollThread.interrupt();
        }
        if (sides > 0 && multiDice.get(sides) != null) {
            return multiDice.get(sides).get(i);
        }

        return 0;
    }
    public synchronized ConcurrentHashMap<Integer, ArrayList<Integer>> getMultiDice() {
        return multiDice;
    }
    public int getMultiNrOfDice() {
        int total = 0;

        for (Integer key :
                multiDice.keySet()) {
            if (Thread.interrupted()) {
                handler.sendEmptyMessage(1);
                return 0;
            }
            total += multiDice.get(key).size();
        }
        return total;
    }
    public int getMultiNrOfDice(int sides) {
        if (sides > 0 && multiDice.get(sides) != null) {
            return multiDice.get(sides).size();
        }
        return 0;
    }

    public int getTotal() {
        return mTotal;
    }

    public synchronized void roll() {
        if (mRollThread != null) {
            mRollThread.interrupt();
            mRollThread = null;
        } else {
            mRollThread = new RollThread();
            mRollThread.start();
        }
    }

    static class RollHandler extends Handler {
        WeakReference<Data> ref;
        public RollHandler(WeakReference<Data> ref){
            this.ref = ref;
        }

        @Override
        public void handleMessage(Message msg) {
            if(msg == null) {
                //.d(TAG, "RollHandler() msg == null");
                return;
            }
            if (!Thread.currentThread().isInterrupted()) {
                switch (msg.what) {
                    case 0:
                        ref.get().setFlag(FLAG_INTERRUPTED, true);
                        ref.get().setUpdate();
                        break;
                    case 1: // Roll finished
                        ref.get().setFlag(FLAG_DICE_ROLL, true);
                        ref.get().setUpdate();
                        break;
                }
            }
            super.handleMessage(msg);
        }
    }

    private class RollThread extends Thread {

        public void run() {
            int roll;
            mTotal = 0;

            for (Integer key :
                    multiDice.keySet()) {
                for (int j = 0; j < multiDice.get(key).size(); j++) {
                    roll = (int)(Math.random()*key+1);
                    mTotal += roll;
                    multiDice.get(key).set(j, roll);
                }
            }

            handler.sendEmptyMessage(1);
            synchronized (Data.this) {
                mRollThread = null;
            }
        }

    }
}
