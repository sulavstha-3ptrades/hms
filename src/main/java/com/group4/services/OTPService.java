package com.group4.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

/**
 * Service for generating and validating OTPs (One-Time Passwords)
 */
public class OTPService {
    private static final Logger LOGGER = Logger.getLogger(OTPService.class.getName());
    // OTP will be valid for 5 minutes
    private static final long OTP_VALIDITY_DURATION = 5 * 60 * 1000;
    
    private static final Map<String, OTPData> otpStore = new HashMap<>();
    private static final Random random = new Random();
    
    private static class OTPData {
        private static final Logger OTP_LOGGER = Logger.getLogger(OTPData.class.getName());
        final String otp;
        final long expiryTime;
        final String email; // Used for logging and verification
        final long creationTime;
        
        OTPData(String otp, String email) {
            this.otp = otp;
            this.email = email;
            this.creationTime = System.currentTimeMillis();
            this.expiryTime = this.creationTime + OTP_VALIDITY_DURATION;
            
            OTP_LOGGER.fine(String.format("Created OTP for %s (expires in %d minutes)", 
                email, OTP_VALIDITY_DURATION / 60000));
        }
        
        boolean isExpired() {
            boolean expired = System.currentTimeMillis() > expiryTime;
            if (expired) {
                OTP_LOGGER.fine(String.format("OTP for %s has expired", email));
            }
            return expired;
        }
    }
    
    /**
     * Generates a new OTP for the given email
     * @param email The email to generate OTP for
     * @return The generated OTP
     */
    public static String generateOTP(String email) {
        // Generate a random 6-digit number
        int otpNumber = 100000 + random.nextInt(900000);
        String otp = String.valueOf(otpNumber);
        
        // Store the OTP with its expiry time
        OTPData otpData = new OTPData(otp, email);
        otpStore.put(email, otpData);
        
        // Schedule cleanup after expiry
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                OTPData data = otpStore.get(email);
                if (data != null && data.isExpired()) {
                    LOGGER.info("OTP expired for " + email);
                    otpStore.remove(email);
                }
            }
        }, OTP_VALIDITY_DURATION);
        
        LOGGER.info("Generated OTP for " + email + ": " + otp + " (valid for " + 
                  (OTP_VALIDITY_DURATION / 60000) + " minutes)");
        return otp;
    }
    
    /**
     * Validates the provided OTP for the given email
     * @param email The email to validate OTP for
     * @param otp The OTP to validate
     * @return true if OTP is valid, false otherwise
     */
    public static boolean validateOTP(String email, String otp) {
        OTPData data = otpStore.get(email);
        if (data == null) {
            LOGGER.warning("No OTP found for email: " + email);
            return false;
        }
        
        if (data.isExpired()) {
            LOGGER.warning("OTP expired for email: " + email);
            otpStore.remove(email);
            return false;
        }
        
        boolean isValid = data.otp.equals(otp);
        if (isValid) {
            LOGGER.info("OTP validation successful for " + email);
            // Remove the OTP after successful validation
            otpStore.remove(email);
        } else {
            LOGGER.warning("Invalid OTP provided for " + email);
        }
        
        return isValid;
    }
}
