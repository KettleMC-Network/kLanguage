package net.kettlemc.klanguage.common.data;

import java.util.concurrent.*;

/**
 * A thread handler for data operations containing a queue.
 */
public class DataThreadHandler {

    private BlockingQueue<Runnable> taskQueue;
    private ExecutorService executorService;

    private Thread taskProcessingThread;

    public void init() {
        if (taskQueue != null || executorService != null) {
            throw new IllegalStateException("DataThreadHandler already initialized!");
        }
        this.taskQueue = new LinkedBlockingQueue<>();
        this.executorService = Executors.newSingleThreadExecutor();

        this.taskProcessingThread = new Thread(() -> {
            while (true) {
                try {
                    Runnable task = taskQueue.take();
                    executorService.execute(task);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        taskProcessingThread.start();

    }

    public void queue(Runnable runnable) {
        if (taskQueue == null || executorService == null) {
            throw new IllegalStateException("DataThreadHandler not initialized!");
        }
        taskQueue.add(runnable);
    }

    public void shutdown() {
        if (taskQueue == null && executorService == null) {
            throw new IllegalStateException("DataThreadHandler not initialized!");
        }
        try {
            taskProcessingThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        executorService.shutdown();
    }


}
