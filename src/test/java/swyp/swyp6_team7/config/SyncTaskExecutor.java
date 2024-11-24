package swyp.swyp6_team7.config;

import org.springframework.core.task.TaskExecutor;

public class SyncTaskExecutor implements TaskExecutor {

    @Override
    public void execute(Runnable task) {
        task.run();
    }
}
