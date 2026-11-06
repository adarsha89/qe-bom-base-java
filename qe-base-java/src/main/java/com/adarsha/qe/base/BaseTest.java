package com.adarsha.qe.base;

import com.adarsha.qe.base.context.TestExecutionContextManager;
import com.adarsha.qe.base.listener.TestExecutionListener;
import com.adarsha.qe.base.model.TestExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Base test class for QE test suites with full parallel execution support.
 * Manages thread-isolated TestExecutionContext instances for parallel methods and DataProviders.
 */
@Slf4j
@Listeners(TestExecutionListener.class)
public abstract class BaseTest {

    @BeforeSuite(alwaysRun = true)
    public void globalSetup() {
        log.info("Initializing QE Test Automation Suite...");
    }

    /**
     * Initializes a thread-isolated TestExecutionContext for each parallel method or DataProvider execution.
     * TestNG injects the current Method, DataProvider arguments (if any), and ITestContext.
     *
     * @param method      currently executing test method
     * @param testData    arguments supplied by DataProvider, if present
     * @param testContext test context of the suite/test tag
     */
    @BeforeMethod(alwaysRun = true)
    public void setupTest(Method method, Object[] testData, ITestContext testContext) {
        List<Object> dataParams = (testData != null && testData.length > 0)
                ? Collections.synchronizedList(new ArrayList<>(Arrays.asList(testData)))
                : Collections.synchronizedList(new ArrayList<>());

        TestExecutionContext context = TestExecutionContext.builder()
                .testSuiteName(testContext != null ? testContext.getName() : "DefaultSuite")
                .testClassName(getClass().getSimpleName())
                .testMethodName(method.getName())
                .threadId(Thread.currentThread().getId())
                .threadName(Thread.currentThread().getName())
                .dataProviderParameters(dataParams)
                .environment(System.getProperty("env", "qa"))
                .executionTimestamp(Instant.now())
                .build();

        TestExecutionContextManager.setContext(context);

        log.info("Initialized context for test: {}.{}() [Thread: {} (id: {})] [DataProvider params count: {}]",
                getClass().getSimpleName(),
                method.getName(),
                Thread.currentThread().getName(),
                Thread.currentThread().getId(),
                dataParams.size());
    }

    /**
     * Teardown hook that logs completion and purges ThreadLocal context to prevent memory leaks in thread pools.
     *
     * @param method currently executed method
     * @param result TestNG execution result
     */
    @AfterMethod(alwaysRun = true)
    public void teardownTest(Method method, ITestResult result) {
        try {
            log.info("Tearing down context for test: {}.{}() (status: {}) [Thread: {} (id: {})]",
                    getClass().getSimpleName(),
                    method.getName(),
                    result.getStatus(),
                    Thread.currentThread().getName(),
                    Thread.currentThread().getId());
        } finally {
            // Clean up ThreadLocal to prevent worker thread pollution
            TestExecutionContextManager.removeContext();
        }
    }

    @AfterSuite(alwaysRun = true)
    public void globalTeardown() {
        log.info("QE Test Automation Suite teardown completed.");
    }

    /**
     * Retrieves the thread-isolated TestExecutionContext for the current test or DataProvider iteration.
     *
     * @return current thread's TestExecutionContext
     */
    protected TestExecutionContext getContext() {
        return TestExecutionContextManager.getContext();
    }

    /**
     * Alias for getContext() for backward compatibility.
     *
     * @return current thread's TestExecutionContext
     */
    protected TestExecutionContext getExecutionContext() {
        return getContext();
    }
}
