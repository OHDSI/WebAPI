package org.ohdsi.webapi.util;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

@ManagedResource
public class ManagedThreadPoolTaskExecutor extends ThreadPoolTaskExecutor {

  @ManagedAttribute
  @Override
  public int getCorePoolSize() {
    return super.getCorePoolSize();
  }

  @ManagedAttribute
  @Override
  public int getMaxPoolSize() {
    return super.getMaxPoolSize();
  }

  @ManagedAttribute
  @Override
  public int getKeepAliveSeconds() {
    return super.getKeepAliveSeconds();
  }

  @ManagedAttribute
  @Override
  public int getPoolSize() {
    return super.getPoolSize();
  }

  @ManagedAttribute
  @Override
  public int getActiveCount() {
    return super.getActiveCount();
  }

  @ManagedAttribute
  public long getCompletedTaskCount() {

    Optional<ThreadPoolExecutor> executor = Optional.ofNullable(getThreadPoolExecutor());
    return executor.map(ThreadPoolExecutor::getCompletedTaskCount).orElse(0L);
  }

  @ManagedAttribute
  public int getLargestPoolSize() {

    Optional<ThreadPoolExecutor> executor = Optional.ofNullable(getThreadPoolExecutor());
    return executor.map(ThreadPoolExecutor::getLargestPoolSize).orElse(0);
  }

  @ManagedAttribute
  public long getTaskCount() {

    Optional<ThreadPoolExecutor> executor = Optional.ofNullable(getThreadPoolExecutor());
    return executor.map(ThreadPoolExecutor::getTaskCount).orElse(0L);
  }

  @ManagedAttribute
  public int getQueuedTaskCount() {

    ThreadPoolExecutor executor = getThreadPoolExecutor();
    BlockingQueue<Runnable> queue = Objects.nonNull(executor) ? executor.getQueue() : null;
    return Objects.nonNull(queue) ? queue.size() : 0;
  }
}
