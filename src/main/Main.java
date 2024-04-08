package main;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class Main {
    public static void main(String[] args) {

        final LoadBalancer loadBalancer = new LoadBalancer(5); // Assuming max parallel requests per provider is 5

        // Register some providers
        IntStream.range(0, 100)
                .forEach(index -> loadBalancer.registerProvider(new Provider()));

        // Random invocation
        final String randomProviderID = loadBalancer.getRandomProviderID();
        System.out.println("Random provider ID: " + randomProviderID);

        // Round-robin invocation
        final String roundRobinProviderID = loadBalancer.getRoundRobinProviderID();
        System.out.println("Round-robin provider ID: " + roundRobinProviderID);

        // Heartbeat check every 2 seconds
        final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(loadBalancer::heartbeatCheck, 0, 2, TimeUnit.SECONDS);

        // Handling incoming requests
        loadBalancer.handleRequest();

        executor.shutdown();
    }
}
