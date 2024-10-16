package org.opencdmp.commons.lock;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class LockByKeyManager {

	private static class LockWrapper {
		private final ReentrantLock lock = new ReentrantLock();
		private final AtomicInteger numberOfThreadsInQueue = new AtomicInteger(1);

		private LockWrapper addThreadInQueue() {
			numberOfThreadsInQueue.incrementAndGet();
			return this;
		}

		private int removeThreadFromQueue() {
			return numberOfThreadsInQueue.decrementAndGet();
		}

	}

	private static ConcurrentHashMap<String, LockWrapper> locks = new ConcurrentHashMap<String, LockWrapper>();

	public void lock(String key) {
		LockWrapper lockWrapper = locks.compute(key, (k, v) -> v == null ? new LockWrapper() : v.addThreadInQueue());
		lockWrapper.lock.lock();
	}

	public boolean tryLock(String key, long timeout, TimeUnit unit) throws InterruptedException {
		LockWrapper lockWrapper = null;
		try {
			lockWrapper = locks.compute(key, (k, v) -> v == null ? new LockWrapper() : v.addThreadInQueue());
			return lockWrapper.lock.tryLock(timeout, unit);
		} catch (Exception ex){
			if (lockWrapper != null && lockWrapper.removeThreadFromQueue() == 0) {
				// NB : We pass in the specific value to remove to handle the case where another thread would queue right before the removal
				locks.remove(key, lockWrapper);
			}
			throw ex;
		}
	}

	public void unlock(String key) {
		LockWrapper lockWrapper = locks.get(key);
		lockWrapper.lock.unlock();
		if (lockWrapper.removeThreadFromQueue() == 0) {
			// NB : We pass in the specific value to remove to handle the case where another thread would queue right before the removal
			locks.remove(key, lockWrapper);
		}
	}

}
