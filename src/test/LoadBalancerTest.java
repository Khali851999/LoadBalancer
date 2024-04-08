import main.LoadBalancer;
import main.Provider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LoadBalancerTest {

    @Mock
    Provider provider;
    @Mock
    Provider anotherProvider;

    @Test
    public void registerProvider() {
        final LoadBalancer loadBalancer = new LoadBalancer(5);
        loadBalancer.registerProvider(provider);

        assertEquals(loadBalancer.getRoundRobinProviderID(), provider.getID());
    }

    @Test
    public void excludeProvider() {
        final LoadBalancer loadBalancer = new LoadBalancer(5);
        loadBalancer.registerProvider(provider);
        loadBalancer.excludeProvider(provider);

        assertEquals(0, loadBalancer.getProviders().size());
        assertTrue(loadBalancer.getExcludedProviders().contains(provider));
    }

    @Test
    public void includeProvider() {
        final LoadBalancer loadBalancer = new LoadBalancer(5);
        loadBalancer.excludeProvider(provider);
        loadBalancer.includeProvider(provider);

        assertEquals(1, loadBalancer.getProviders().size());
        assertTrue(loadBalancer.getProviders().contains(provider));
    }

    @Test
    public void getRandomProviderID() {
        final LoadBalancer loadBalancer = new LoadBalancer(5);
        loadBalancer.registerProvider(provider);
        when(provider.getID()).thenReturn("TestID");

        assertEquals("TestID", loadBalancer.getRandomProviderID());
    }

    @Test
    public void getRoundRobinProviderID() {
        final LoadBalancer loadBalancer = new LoadBalancer(5);
        loadBalancer.registerProvider(provider);
        loadBalancer.registerProvider(anotherProvider);
        when(provider.getID()).thenReturn("Provider1ID");
        when(anotherProvider.getID()).thenReturn("Provider2ID");

        assertEquals("Provider1ID", loadBalancer.getRoundRobinProviderID());
        assertEquals("Provider2ID", loadBalancer.getRoundRobinProviderID());
        assertEquals("Provider1ID", loadBalancer.getRoundRobinProviderID());
    }

    @Test
    public void heartbeatCheck() {
        final LoadBalancer loadBalancer = new LoadBalancer(5);
        Provider provider1 = mock(Provider.class);
        Provider provider2 = mock(Provider.class);
        loadBalancer.registerProvider(provider1);
        loadBalancer.registerProvider(provider2);
        when(provider1.check()).thenReturn(true);
        when(provider2.check()).thenReturn(false);

        loadBalancer.heartbeatCheck();

        assertFalse(loadBalancer.getProviders().contains(provider2));
    }

    @Test
    public void heartbeatCheckAllProviders() {
        final LoadBalancer loadBalancer = new LoadBalancer(5);
        loadBalancer.registerProvider(provider);
        loadBalancer.registerProvider(anotherProvider);
        when(provider.check()).thenReturn(true);
        when(anotherProvider.check()).thenReturn(false);

        loadBalancer.advanceHeartbeatCheck();

        assertTrue(loadBalancer.getProviders().contains(provider));
        assertFalse(loadBalancer.getProviders().contains(anotherProvider));
    }

    @Test
    public void handleRequest() {
        final LoadBalancer loadBalancer = new LoadBalancer(1);
        loadBalancer.registerProvider(provider);

        loadBalancer.handleRequest();

        verify(provider, times(1)).getID();
    }

    @Test
    public void handleRequestById() {
        final LoadBalancer loadBalancer = new LoadBalancer(1);
        loadBalancer.registerProvider(provider);
        when(provider.getID()).thenReturn("TestID");

        loadBalancer.handleRequest("TestID");

        verify(provider, times(1)).getID();
    }

    @Test
    public void canAcceptRequest() {
        final LoadBalancer loadBalancer = new LoadBalancer(1);
        loadBalancer.registerProvider(provider);
        loadBalancer.handleRequest();

        assertFalse(loadBalancer.canAcceptRequest());
    }
}
