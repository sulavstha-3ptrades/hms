package com.group4;

import org.mindrot.jbcrypt.BCrypt;
import com.group4.models.User;
import com.group4.utils.FileConstants;
import com.group4.utils.FileHandler;

import java.io.IOException;
import java.util.List;

public class TestLogin {
    public static void main(String[] args) {
        try {
            // Read users from file
            List<String> lines = FileHandler.readLines(FileConstants.getUsersFilePath());
            System.out.println("Read " + lines.size() + " lines from users file");

            // Test password
            String password = "password123";

            for (String line : lines) {
                System.out.println("Processing line: " + line);
                User user = User.fromDelimitedString(line);

                if (user != null) {
                    System.out.println("User parsed: " + user.getEmail() + ", status: " + user.getStatus());
                    System.out.println("Stored password hash: " + user.getPassword());

                    // Test BCrypt verification
                    try {
                        boolean passwordMatch = BCrypt.checkpw(password, user.getPassword());
                        System.out.println("Password check result for " + user.getEmail() + ": " + passwordMatch);
                    } catch (Exception e) {
                        System.err.println("Error checking password: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Could not parse user from line: " + line);
                }

                System.out.println("-------------------------");
            }

        } catch (IOException e) {
            System.err.println("Error reading users file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}