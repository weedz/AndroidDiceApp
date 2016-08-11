package com.weedz.dice;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
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

    private volatile ArrayList<Integer> dice;
    // Different sized dice
    private ConcurrentHashMap<Integer, ArrayList<Integer>> multiDice;

    private int total = 0;
    private int largestDie = 6;

    private RollThread mRollThread;
    WeakReference<Data> ref = new WeakReference<>(this);
    private RollHandler handler = new RollHandler(ref);

    private Data() {
        Arrays.fill(flags, false);
        dice = new ArrayList<>();
        dice.add(0);

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

    public synchronized void addDice(int nr, int sides) {
        if (flags[FLAG_THREAD_LOCK]) {
            Log.d(TAG,"addDice(): ThreadLock");
            return;
        }
        if (mRollThread != null && mRollThread.isAlive()) {
            mRollThread.interrupt();
        }
        /*if (nr > 10000-dice.size()) {
            nr = 10000-dice.size();
        }*/
        dice.addAll(Arrays.asList(new Integer[nr]));
        setFlag(FLAG_DICESET_UPDATE, true);
        setUpdate();
    }
    public synchronized void addMultiDice(int nr, int sides) {
        if (mRollThread != null && mRollThread.isAlive()) {
            mRollThread.interrupt();
        }
        if (multiDice.get(sides) == null) {
            multiDice.put(sides, new ArrayList<Integer>());
        }
        multiDice.get(sides).addAll(Arrays.asList(new Integer[nr]));
        setFlag(FLAG_DICESET_UPDATE, true);
        setUpdate();
    }

    public synchronized void removeDie(int nr, int sides) {
        if (flags[FLAG_THREAD_LOCK]) {
            Log.d(TAG,"removeDie(): ThreadLock");
            return;
        }
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
        setFlag(FLAG_DICESET_UPDATE, true);
        setUpdate();
    }
    public synchronized void removeMultiDice(int nr, int sides) {
        if (mRollThread != null && mRollThread.isAlive()) {
            mRollThread.interrupt();
        }
        if (multiDice.get(sides) != null) {
            Iterator<Integer> iterator = multiDice.keySet().iterator();
            int count = 0;
            while(iterator.hasNext()) {

                iterator.next();

                iterator.remove();
                count++;
                if(count >= nr) {
                    break;
                }
            }
        }
    }

    public synchronized void setDice(int nr, int sides) {
        /*if (sides > 500) {
            sides = 500;
        }*/
        largestDie = sides;
        if (flags[FLAG_THREAD_LOCK]) {
            Log.d(TAG,"setDice(): ThreadLock");
            return;
        }
        if (mRollThread != null && mRollThread.isAlive()) {
            mRollThread.interrupt();
        }
        dice.clear();
        addDice(nr, sides);
    }
    public synchronized void setMultiDice(int nr, int sides) {
        largestDie = sides;
        if (flags[FLAG_THREAD_LOCK]) {
            Log.d(TAG,"setDice(): ThreadLock");
            return;
        }
        if (mRollThread != null && mRollThread.isAlive()) {
            mRollThread.interrupt();
        }
        multiDice.clear();
        addMultiDice(nr, sides);
    }

    public int getTotal() {
        return total;
    }

    public synchronized int getDie(int i) {
        if (mRollThread != null && mRollThread.isAlive()) {
            //Log.d(TAG, "getDie(): interrupt");
            mRollThread.interrupt();
        }
        if (dice.size() < i || dice.get(i) == null) {
            //Log.d(TAG, "getDie(" + i + "): OutOfBounds");
            return 0;
        }
        return dice.get(i);
    }

    public int getNrOfDie() {
        return dice.size();
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

    public int getLargestDie() {
        return largestDie;
    }

    static class RollHandler extends Handler {
        WeakReference<Data> ref;
        public RollHandler(WeakReference<Data> ref){
            this.ref = ref;
        }

        @Override
        public void handleMessage(Message msg) {
            if(msg == null) {
                Log.d(TAG, "RollHandler() msg == null");
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
            total = 0;

            /*Iterator<Integer> iterator = multiDice.keySet().iterator();
            while(iterator.hasNext()) {
                if (Thread.interrupted()) {
                    handler.sendEmptyMessage(1);
                    return;
                }
                Integer key = iterator.next();
                for (int j = 0; j < multiDice.get(key).size(); j++) {
                    roll = (int)(Math.random()*key+1);
                    total += roll;
                    multiDice.get(key).set(j, roll);
                }
            }*/

            // Java 8, Android API 24
            /*multiDice.forEach((k,v)->{
                for (int i = 0; i < v.size(); i++) {
                    v.set(i, (int)(Math.random()*k+1));
                }
                //System.out.println(k + " - " + v.toString());
            });*/
            // Single size dice
            total = 0;
            for(int i = 0; i < dice.size(); i++) {
                if (Thread.interrupted()) {
                    handler.sendEmptyMessage(0);
                    return;
                }
                roll = (int) (Math.random() * Data.getInstance().getLargestDie() + 1);
                if (dice.size() >= i) {
                    dice.set(i, roll);
                } else {
                    handler.sendEmptyMessage(0);
                    return;
                }
                total += roll;
            }

            handler.sendEmptyMessage(1);
            synchronized (Data.this) {
                mRollThread = null;
            }
        }

    }
}
