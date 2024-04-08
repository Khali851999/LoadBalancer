package main;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class LoadBalancer {
    private final List<Provider> providers;
    private final List<Provider> excludedProviders;
    private int currentIndex;
    private final int maxRequestsPerProvider;
    private final Map<Provider, Integer> activeRequests;
    private static final int MAX_PROVIDERS = 10;

    public LoadBalancer(final int maxRequestsPerProvider) {
        this.providers = new ArrayList<>();
        this.excludedProviders = new ArrayList<>();
        this.currentIndex = 0;
        this.maxRequestsPerProvider = maxRequestsPerProvider;
        this.activeRequests = new HashMap<>();
    }

    public List<Provider> getProviders() {
        return providers;
    }

    public List<Provider> getExcludedProviders() {
        return excludedProviders;
    }

    public void registerProvider(Provider provider) {
        if (providers.size() < MAX_PROVIDERS) {
            provider.resetHeartbeatChecks();
            providers.add(provider);
        } else {
            throw new IllegalArgumentException("Maximum number of providers reached.");
        }
    }

    public void excludeProvider(final Provider provider) {
        if (providers.contains(provider)) {
            provider.resetHeartbeatChecks();
            providers.remove(provider);
            excludedProviders.add(provider);
        }
    }

    public void includeProvider(final Provider provider) {
        if (excludedProviders.contains(provider)) {
            provider.resetHeartbeatChecks();
            excludedProviders.remove(provider);
            providers.add(provider);
        }
    }

    public synchronized String getRandomProviderID() {
        if (providers.isEmpty()) {
            return null;
        }

        final int randomIndex = ThreadLocalRandom.current().nextInt(providers.size());
        final Provider provider = providers.get(randomIndex);
        return provider.getID();
    }

    public synchronized String getRoundRobinProviderID() {
        if (providers.isEmpty()) {
            return null;
        }

        final Provider provider = providers.get(currentIndex);
        currentIndex = (currentIndex + 1) % providers.size();
        return provider.getID();
    }

    public void heartbeatCheck() {
        providers.forEach(provider -> {
            if (!provider.check()) {
                excludeProvider(provider);
            }
        });
    }

    public synchronized void advanceHeartbeatCheck() {
        for (Iterator<Provider> iterator = excludedProviders.iterator(); iterator.hasNext();) {
            final Provider provider = iterator.next();
            provider.check();
            if (provider.isEligibleForBalancing()) {
                includeProvider(provider);
                System.out.println("Provider " + provider.getID() + " re-included in load balancing.");
                iterator.remove();
            }
        }

        heartbeatCheck();
    }

    public synchronized void handleRequest() {
        if (!canAcceptRequest()) {
            System.out.println("Load balancer cannot handle request due to max capacity reached");
            return;
        }

        final int randomIndex = ThreadLocalRandom.current().nextInt(providers.size());
        final Provider provider = providers.get(randomIndex);

        final int activeRequestsCount = activeRequests.getOrDefault(provider, 0);
        activeRequests.put(provider, activeRequestsCount + 1);
        System.out.println("Request handled by provider: " + provider.getID());

        // Deregister the request
        requestHandled(provider);
    }

    public synchronized void handleRequest(final String providerId) {
        if (!canAcceptRequest()) {
            System.out.println("Load balancer cannot handle request due to max capacity reached");
            return;
        }

        final Optional<Provider> maybeProvider = providers.stream()
                .filter(provider -> Objects.equals(provider.getID(), providerId))
                .findFirst();

        maybeProvider.ifPresent(provider -> {
            final int activeRequestsCount = activeRequests.getOrDefault(provider, 0);
            activeRequests.put(provider, activeRequestsCount + 1);
            System.out.println("Request handled by provider: " + provider.getID());

            // Deregister the request
            requestHandled(provider);
        });
    }

     public synchronized boolean canAcceptRequest() {
        final int totalActiveRequests = activeRequests.values().stream().mapToInt(Integer::intValue).sum();
        final int maxAllowedRequests = maxRequestsPerProvider * providers.size();
        return totalActiveRequests < maxAllowedRequests;
    }

    // Method to be called before returning the response of the request
    private synchronized void requestHandled(final Provider provider) {
        final int activeRequestsCount = activeRequests.getOrDefault(provider, 0);
        activeRequests.put(provider, activeRequestsCount - 1);
    }
}
