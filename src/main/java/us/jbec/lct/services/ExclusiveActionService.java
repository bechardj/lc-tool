package us.jbec.lct.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Service
public class ExclusiveActionService {

    private final ConcurrentHashMap<String, String> actionMap;
    private final ConcurrentHashMap<String, List<CountDownLatch>> waitLatchMap;
    private final ReentrantReadWriteLock lock;

    Logger LOG = LoggerFactory.getLogger(ExclusiveActionService.class);

    public ExclusiveActionService() {
        actionMap = new ConcurrentHashMap<>();
        waitLatchMap = new ConcurrentHashMap<>();
        lock = new ReentrantReadWriteLock();
    }

    /**
     * Exclusively perform the action identified by the provided string for a given class
     *
     * @param cls calling class
     * @param action the string identifying the action to perform exclusively
     */
    public void acquireExclusiveActionLock(Class cls, String action) {
        lock.writeLock().lock();
        try {
            String key = cls.getName() + action;
            if (actionMap.containsKey(key)) {
                List<CountDownLatch> latches = waitLatchMap.get(key);
                CountDownLatch myLatch = new CountDownLatch(1);
                latches.add(myLatch);
                lock.writeLock().unlock();
                LOG.info("Class {} waiting on action {} with {} total waiting", cls.getName(), action, latches.size());
                myLatch.await();
                lock.writeLock().lock();
                latches.remove(myLatch);
                LOG.info("Class {} resuming on action {} with {} total waiting", cls.getName(), action, latches.size());
            } else {
                actionMap.put(key, key);
                waitLatchMap.put(key, new ArrayList<>());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Release the exclusive right to perform the action identified by the provided string for a given class
     *
     * @param cls calling class
     * @param action the string identifying the action to perform exclusively
     */
    public void releaseExclusiveActionLock(Class cls, String action) {
        lock.writeLock().lock();
        try {
            String key = cls.getName() + action;
            if (!waitLatchMap.get(key).isEmpty()) {
                waitLatchMap.get(key).stream().findFirst().ifPresent(CountDownLatch::countDown);
            } else {
                waitLatchMap.remove(key);
                actionMap.remove(key);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

}
