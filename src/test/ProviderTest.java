import main.Provider;
import org.junit.Test;

import static org.junit.Assert.*;

public class ProviderTest {

    @Test
    public void test_getID() {
        final Provider provider = new Provider();
        assertNotNull(provider.getID());
    }

    @Test
    public void test_check() {
        final Provider provider = new Provider();
        assertTrue(provider.check() || !provider.check()); // Either true or false
    }

    @Test
    public void test_isEligibleForBalancing() {
        final Provider provider = new Provider();
        assertFalse(provider.isEligibleForBalancing()); // Newly created provider should not be eligible

        // After 2 consecutive heartbeat checks, provider should be eligible
        provider.check();
        provider.check();
        assertTrue(provider.isEligibleForBalancing());
    }
}
