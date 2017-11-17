package orient.util.thread;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ThreadLifeCycle {
  private final int __FAILED = -1, __STOPPED = 0, __STARTING = 1, __STARTED = 2, __STOPPING = 3;
  private volatile int state = __STOPPED;
  private int idleTimeout = 60000;
  private Set<Thread> threads = ConcurrentHashMap.newKeySet();
  private int priority = Thread.NORM_PRIORITY;
  private String threadName = "qtp" + hashCode();
  ThreadGroup tGroup;
  BlockingQueue<Runnable> queue;

  private Runnable runnable = () -> {
    try {
      Runnable task = queue.poll();

      while(isRunning()) {
        task.run();

        if(Thread.interrupted()) {
          break;
        }

        task = queue.poll();
      }

      while(isRunning() && task == null) {
        if(idleTimeout <= 0) {
          task = queue.take();
        } else {
          task = queue.poll(idleTimeout, TimeUnit.MILLISECONDS);
        }
      }
    } catch (InterruptedException ie) {

    } finally {
      threads.remove(Thread.currentThread());
    }
  };

  public void startThreads() {
    try {
      Thread thread = new Thread(tGroup, runnable);
      thread.setDaemon(false);
      thread.setPriority(priority);
      thread.setName(threadName + "-" + thread.getId());
      threads.add(thread);

      thread.start();
    } finally {

    }
  }

  public boolean isRunning() {
    final int _state = state;

    return _state == __STARTED || _state == __STARTING;
  }
}
