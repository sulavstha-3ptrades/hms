package com.group4;

import org.mindrot.jbcrypt.BCrypt;

public class TestBCrypt {
    public static void main(String[] args) {
        String password = "password123";
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());

        System.out.println("Original password: " + password);
        System.out.println("Hashed password: " + hashed);
        System.out.println("Verification result: " + BCrypt.checkpw(password, hashed));
    }
}