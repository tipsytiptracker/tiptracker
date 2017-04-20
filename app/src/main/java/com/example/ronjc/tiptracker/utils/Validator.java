package com.example.ronjc.tiptracker.utils;

/**
 * Validator.java
 *
 * Validator class that validates user input for registration and login
 *
 * @author Ronald Mangiliman
 * Created on 4/4/2017.
 */

public class Validator {

    public boolean validateEmail(String email) {
        return email.contains("@");
    }

    public boolean validatePassword(String password) {
        String n = ".*[0-9].*"; //regex for containing numbers
        String a = ".*[A-Z]+.*"; //regex for containing uppercase letters

        // Check if password passes all validations:
        // Password is at least 8 characters, password contains at least one number, and
        // at least one uppercase letter
        return password.length() > 7 && password.matches(n) && password.matches(a);
    }
}
