package com.group4.utils;

import javafx.concurrent.Task;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Utility class for working with Tasks.
 */
public class TaskUtils {

    private static final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Executes a task synchronously and returns the result.
     * 
     * @param <T>  The result type of the task
     * @param task The task to execute
     * @return The result of the task
     * @throws Exception If an error occurs during execution
     */
    public static <T> T executeTask(Task<T> task) throws Exception {
        executor.execute(task);

        try {
            return task.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new Exception("Task execution failed", e);
        }
    }

    /**
     * Executes a task asynchronously with progress reporting and callbacks.
     * 
     * @param <T>         The result type of the task
     * @param task        The task to execute
     * @param onSucceeded Callback to run when the task succeeds
     * @param onFailed    Callback to run when the task fails
     */
    public static <T> void executeTaskWithProgress(Task<T> task, Consumer<T> onSucceeded,
            Consumer<Throwable> onFailed) {
        task.setOnSucceeded(event -> {
            T result = task.getValue();
            onSucceeded.accept(result);
        });

        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            onFailed.accept(exception);
        });

        executor.execute(task);
    }

    /**
     * Shuts down the executor service.
     * Call this method when the application is closing.
     */
    public static void shutdown() {
        executor.shutdown();
    }
}