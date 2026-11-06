package com.adarsha.qe.base;

import com.adarsha.qe.base.model.TestExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Instant;

/**
 * Verification test ensuring TestNG, SLF4J, Logback, and Lombok function seamlessly.
 */
@Slf4j
public class BaseTestVerificationTest extends BaseTest {

    @Test(description = "Verify TestNG, Lombok and BaseTest thread context integration")
    public void testLombokAndBaseTest() {
        log.info("Verifying Lombok builder and getter generation...");

        TestExecutionContext context = TestExecutionContext.builder()
                .testSuiteName("SmokeTest")
                .environment("staging")
                .browserOrPlatform("chrome")
                .executionTimestamp(Instant.now())
                .build();

        context.setAttribute("retryCount", 3);

        Assert.assertNotNull(context, "Context should not be null");
        Assert.assertEquals(context.getTestSuiteName(), "SmokeTest");
        Assert.assertEquals(context.getEnvironment(), "staging");
        Assert.assertEquals(context.getBrowserOrPlatform(), "chrome");
        Assert.assertEquals(context.getAttribute("retryCount"), 3);
        Assert.assertEquals(context.getAttribute("retryCount", Integer.class), Integer.valueOf(3));

        // Also verify the thread-isolated context auto-injected by BaseTest
        TestExecutionContext threadContext = getContext();
        Assert.assertNotNull(threadContext, "Injected thread context should not be null");
        Assert.assertEquals(threadContext.getTestMethodName(), "testLombokAndBaseTest");
        Assert.assertEquals(threadContext.getThreadId(), Thread.currentThread().getId());

        log.info("Lombok model and thread context verification successful: {}", context);
    }

    @Test(description = "Verify SLF4J and Logback logging levels")
    public void testLoggingLevels() {
        log.debug("DEBUG message - detailed test execution diagnostic");
        log.info("INFO message - standard test flow event");
        log.warn("WARN message - potential test anomaly notification");

        Assert.assertTrue(log.isInfoEnabled(), "SLF4J INFO level should be enabled");
        Assert.assertNotNull(getContext(), "Context should be available in test");
    }
}
