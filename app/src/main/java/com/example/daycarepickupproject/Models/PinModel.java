package com.example.daycarepickupproject.Models;

/**
 * Model for the staff PIN being typed on the lock screen.
 * Holds no Android view code, so it can be tested on its own.
 */
public class PinModel {

    public static final int PIN_LENGTH = 4;

    // Placeholder PIN until staff accounts are stored (e.g. in a database).
    private static final String STAFF_PIN = "1234";

    private final StringBuilder enteredPin = new StringBuilder();

    /** Adds a digit. Returns false if the PIN is already full. */
    public boolean addDigit(String digit) {
        if (enteredPin.length() >= PIN_LENGTH) {
            return false;
        }
        enteredPin.append(digit);
        return true;
    }

    public void removeLastDigit() {
        if (enteredPin.length() > 0) {
            enteredPin.deleteCharAt(enteredPin.length() - 1);
        }
    }

    public void clear() {
        enteredPin.setLength(0);
    }

    public int getLength() {
        return enteredPin.length();
    }

    public boolean isComplete() {
        return enteredPin.length() == PIN_LENGTH;
    }

    public boolean isCorrect() {
        return STAFF_PIN.contentEquals(enteredPin);
    }
}
