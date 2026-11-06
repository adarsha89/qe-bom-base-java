package com.adarsha.qe.base.context;

import com.adarsha.qe.base.model.TestExecutionContext;

/**
 * ThreadLocal manager for TestExecutionContext.
 * Guarantees strict thread isolation for parallel test execution (parallel methods and parallel DataProviders).
 */
public final class TestExecutionContextManager {

    private static final ThreadLocal<TestExecutionContext> CONTEXT_HOLDER = new ThreadLocal<>();

    private TestExecutionContextManager() {
        // Prevent instantiation of utility class
    }

    /**
     * Returns the TestExecutionContext bound to the current executing thread.
     *
     * @return current thread's TestExecutionContext, or null if uninitialized.
     */
    public static TestExecutionContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * Binds a TestExecutionContext to the current executing thread.
     *
     * @param context the context to associate with the current thread.
     */
    public static void setContext(TestExecutionContext context) {
        CONTEXT_HOLDER.set(context);
    }

    /**
     * Purges the TestExecutionContext from the current executing thread.
     * Essential for preventing memory leaks and context bleeding across reused worker threads.
     */
    public static void removeContext() {
        CONTEXT_HOLDER.remove();
    }
}
