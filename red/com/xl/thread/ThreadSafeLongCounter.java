/*
 * RED: RNA Editing Detector
 *     Copyright (C) <2014>  <Xing Li>
 *
 *     RED is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     RED is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.xl.thread;

/**
 * This class provides a thread safe implementation of a counter where the increment and decrement methods can be called from any number of threads with no
 * concern that the values will clash or updates be lost.
 *
 * @author andrewss
 */
public class ThreadSafeLongCounter {

    private long value = 0;

    public synchronized void increment() {
        value++;
    }

    public synchronized void decrement() {
        value--;
    }

    public synchronized void incrementBy(long amount) {
        value += amount;
    }

    public synchronized void decrementBy(long amount) {
        value -= amount;
    }

    public long value() {
        return value;
    }


}
