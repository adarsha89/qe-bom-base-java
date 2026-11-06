package com.adarsha.qe.base.listener;

import com.adarsha.qe.base.context.TestExecutionContextManager;
import com.adarsha.qe.base.model.TestExecutionContext;
import lombok.extern.slf4j.Slf4j;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * TestNG listener that integrates with SLF4J / Logback for standardized, thread-aware execution logging.
 */
@Slf4j
public class TestExecutionListener implements ITestListener {

    @Override
    public void onStart(ITestContext context) {
        log.info("========== Starting Test Suite: {} ==========", context.getName());
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("========== Completed Test Suite: {} [Passed: {}, Failed: {}, Skipped: {}] ==========",
                context.getName(),
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());
    }

    @Override
    public void onTestStart(ITestResult result) {
        log.info(">>> Running test: {}.{}() [Thread: {} (id: {})]",
                result.getTestClass().getName(),
                result.getMethod().getMethodName(),
                Thread.currentThread().getName(),
                Thread.currentThread().getId());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        long duration = result.getEndMillis() - result.getStartMillis();
        log.info("PASSED: {}.{}() [Duration: {} ms] [Thread: {}]",
                result.getTestClass().getName(),
                result.getMethod().getMethodName(),
                duration,
                Thread.currentThread().getName());
    }

    @Override
    public void onTestFailure(ITestResult result) {
        long duration = result.getEndMillis() - result.getStartMillis();
        log.error("FAILED: {}.{}() [Duration: {} ms] [Thread: {}] - Error: {}",
                result.getTestClass().getName(),
                result.getMethod().getMethodName(),
                duration,
                Thread.currentThread().getName(),
                result.getThrowable() != null ? result.getThrowable().getMessage() : "Unknown error",
                result.getThrowable());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log.warn("SKIPPED: {}.{}() [Thread: {}]",
                result.getTestClass().getName(),
                result.getMethod().getMethodName(),
                Thread.currentThread().getName());
        // Clean up thread-local in case skipped test was not routed through @AfterMethod
        TestExecutionContextManager.removeContext();
    }
}
