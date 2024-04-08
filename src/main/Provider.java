package main;

import java.util.UUID;

public class Provider {
    private final String id;
    private int consecutiveHeartbeatChecks;

    public Provider() {
        this.id = UUID.randomUUID().toString();
        this.consecutiveHeartbeatChecks = 0;
    }

    public String getID() {
        return id;
    }

    // Method to check if the provider is alive
    public boolean check() {
        System.out.println("main.Provider " + id + " is checked for health status.");
        if (isAlive()) {
            consecutiveHeartbeatChecks++;
            return true;
        } else {
            consecutiveHeartbeatChecks = 0;
            return false;
        }
    }

    public void resetHeartbeatChecks() {
        consecutiveHeartbeatChecks = 0;
    }

    private boolean isAlive() {
        // Simulated method to check provider's health status
        return Math.random() < 0.8; // 80% chance of being alive
    }

    public boolean isEligibleForBalancing() {
        return consecutiveHeartbeatChecks >= 2;
    }
}
