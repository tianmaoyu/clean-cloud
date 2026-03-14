package org.clean;

import com.alibaba.ttl.threadpool.TtlExecutors;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.*;


@Configuration
public class TaskExecutorConfig {
   @Bean("taskExecutor")
   public ThreadPoolTaskExecutor taskExecutor() {
      //获取当前系统的CPU核数
      int processors = Runtime.getRuntime().availableProcessors();
      ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
      executor.setCorePoolSize(processors);
      executor.setMaxPoolSize(processors*2);
      executor.setQueueCapacity(200);
      executor.setTaskDecorator(new ContextCopyingTaskDecorator());
      executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
      executor.setThreadNamePrefix("clean-task-");
      executor.initialize();
      return executor;
   }


    @Bean("taskLogExecutor")
    public ThreadPoolTaskExecutor taskLogExecutor() {
        //获取当前系统的CPU核数
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(200);
        executor.setTaskDecorator(new ContextCopyingTaskDecorator());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadNamePrefix("clean-log-task-");
        executor.initialize();
        return executor;
    }


//   //实现一个ttl 版本
//   @Bean("executorService")
//    public ExecutorService threadPoolExecutor() {
//
//       int processors = Runtime.getRuntime().availableProcessors();
//       ThreadPoolExecutor executor = new ThreadPoolExecutor(
//               processors,
//               processors*2,
//               60L,
//               TimeUnit.SECONDS,
//               new LinkedBlockingQueue<>(500),
//               Executors.defaultThreadFactory(),
//               new ThreadPoolExecutor.CallerRunsPolicy()
//       );
//
//       return TtlExecutors.getTtlExecutorService(executor);
//   }


//@Bean("taskTllExecutor")
//public ThreadPoolTaskExecutor taskTllExecutor() {
//    //获取当前系统的CPU核数
//    int processors = Runtime.getRuntime().availableProcessors();
//    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//    executor.setCorePoolSize(processors);
//    executor.setMaxPoolSize(processors*2);
//    executor.setQueueCapacity(200);
//    executor.setTaskDecorator(new TtlTaskDecorator());
//    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
//    executor.setThreadNamePrefix("clean-task-");
//    executor.initialize();
//    return executor;
//}


}

