package com.adarsha.qe.base;

import com.adarsha.qe.base.model.TestExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Validates fully parallel execution behavior:
 * - Parallel test methods
 * - Parallel DataProviders
 * Verifies strict ThreadLocal isolation, thread safety, and context integrity.
 */
@Slf4j
public class ParallelExecutionVerificationTest extends BaseTest {

    // Thread-safe collector of threads used during parallel execution
    private static final Set<Long> METHOD_THREADS = ConcurrentHashMap.newKeySet();
    private static final Set<Long> DATAPROVIDER_THREADS = ConcurrentHashMap.newKeySet();

    // Parallel DataProvider supplying multiple test datasets concurrently
    @DataProvider(name = "parallelUserProvider", parallel = true)
    public Object[][] parallelUserProvider() {
        return new Object[][]{
                {"user_alpha", "admin", 101},
                {"user_beta", "editor", 102},
                {"user_gamma", "viewer", 103},
                {"user_delta", "tester", 104},
                {"user_epsilon", "manager", 105}
        };
    }

    @Test(dataProvider = "parallelUserProvider", description = "Verify parallel DataProvider context isolation")
    public void testParallelDataProviderExecution(String username, String role, int userId) throws InterruptedException {
        long currentThreadId = Thread.currentThread().getId();
        DATAPROVIDER_THREADS.add(currentThreadId);

        TestExecutionContext context = getContext();
        Assert.assertNotNull(context, "ThreadLocal context must not be null");
        Assert.assertEquals(context.getThreadId(), currentThreadId, "Context thread ID must match current thread");

        // Verify DataProvider parameters were captured in context
        Assert.assertNotNull(context.getDataProviderParameters(), "DataProvider params should not be null");
        Assert.assertTrue(context.getDataProviderParameters().contains(username), "Context should contain username");
        Assert.assertTrue(context.getDataProviderParameters().contains(role), "Context should contain role");
        Assert.assertTrue(context.getDataProviderParameters().contains(userId), "Context should contain userId");

        // Set an isolated attribute for this DataProvider iteration
        String attributeKey = "context_key_" + username;
        context.setAttribute(attributeKey, "payload_" + userId);

        // Sleep briefly to induce concurrent execution overlap
        Thread.sleep(100);

        // Assert that this thread's context preserved its attribute intact
        Assert.assertEquals(context.getAttribute(attributeKey), "payload_" + userId);
        Assert.assertEquals(context.getAttribute(attributeKey, String.class), "payload_" + userId);

        log.info("DataProvider execution verified for user '{}' on Thread: {} (Total DP threads: {})",
                username, currentThreadId, DATAPROVIDER_THREADS.size());
    }

    @Test(description = "Verify parallel method execution - Task 1")
    public void testParallelMethodAlpha() throws InterruptedException {
        executeParallelMethodAssertion("alpha_task", 1);
    }

    @Test(description = "Verify parallel method execution - Task 2")
    public void testParallelMethodBeta() throws InterruptedException {
        executeParallelMethodAssertion("beta_task", 2);
    }

    @Test(description = "Verify parallel method execution - Task 3")
    public void testParallelMethodGamma() throws InterruptedException {
        executeParallelMethodAssertion("gamma_task", 3);
    }

    private void executeParallelMethodAssertion(String taskName, int priority) throws InterruptedException {
        long currentThreadId = Thread.currentThread().getId();
        METHOD_THREADS.add(currentThreadId);

        TestExecutionContext context = getContext();
        Assert.assertNotNull(context, "Context should be initialized by @BeforeMethod");
        Assert.assertEquals(context.getThreadId(), currentThreadId);

        // Set thread-local attributes
        context.setAttribute("taskName", taskName);
        context.setAttribute("priority", priority);

        // Introduce simulated work to ensure concurrency overlap
        Thread.sleep(120);

        // Verify thread context integrity
        Assert.assertEquals(context.getAttribute("taskName", String.class), taskName);
        Assert.assertEquals(context.getAttribute("priority", Integer.class), Integer.valueOf(priority));
        Assert.assertTrue(context.hasAttribute("taskName"));

        log.info("Parallel method '{}' verified on thread {} (Total method threads: {})",
                taskName, currentThreadId, METHOD_THREADS.size());
    }
}
