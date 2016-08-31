package com.weedz.dice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;
import java.util.concurrent.ConcurrentHashMap;

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
    private Boolean[] flags = new Boolean[4];

    // Different sized dice
    private volatile ConcurrentHashMap<Integer, ArrayList<Integer>> multiDice;

    private int mTotal = 0;
    private long seed;

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
        multiDice.clear();
        if (nr > 0 && sides > 0) {
            addMultiDice(nr, sides);
        } else {
            setFlag(FLAG_DICESET_UPDATE, true);
            setUpdate();
        }
    }
    public synchronized int getMultiDie(int sides, int i) {
        if (sides > 0 && multiDice.get(sides) != null) {
            if (multiDice.get(sides).get(i) != null) {
                return multiDice.get(sides).get(i);
            }
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

    public synchronized int getTotal(int sides, int nr) {
        int total = 0;
        for (int i = 0; i < multiDice.get(sides).size(); i++) {
            if (multiDice.get(sides).get(i) == nr) {
                total++;
            }
        }
        return total;
    }
    public void setTotal(int total) {
        mTotal = total;
    }

    public long getSeed() {
        return seed;
    }
    public void setSeed(long seed) {
        this.seed = seed;
    }
}
