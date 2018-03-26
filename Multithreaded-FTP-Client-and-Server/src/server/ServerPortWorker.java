package server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ServerPortWorker implements Runnable {

	public static HashMap<String, ReentrantReadWriteLock> readLockCheck = new HashMap<String, ReentrantReadWriteLock>();
	public static HashMap<Integer, String> commandMap = new HashMap<Integer, String>();
	public static Queue<Integer> writeLockQueue = new LinkedList<Integer>();
	private static final String TERMINATE = "terminate";
	private static final String LOCK = "lock";

	public synchronized boolean getLock(int commandId, String filePath) throws InterruptedException {

		if (readLockCheck.containsKey(filePath)) {
			if (readLockCheck.get(filePath).isWriteLocked()) {
				return false;
			} else if (readLockCheck.get(filePath).readLock().tryLock()) {
				commandMap.put(commandId, "LOCK");
				return true;
			} else {
				return false;
			}
		} else {
			ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
			readLockCheck.put(filePath, rwLock);
			readLockCheck.get(filePath).readLock().lock();
			commandMap.put(commandId, "LOCK");
			return true;
		}
	}

	public synchronized void getUnLock(String filePath, int cmdID) throws InterruptedException {

		readLockCheck.get(filePath).readLock().unlock();
		Thread.currentThread().sleep(50);
		if (readLockCheck.containsKey(filePath)) {
			if (!readLockCheck.get(filePath).isWriteLocked() && readLockCheck.get(filePath).getReadLockCount() == 0) {
				readLockCheck.remove(filePath);
			}
		}
		commandMap.remove(cmdID);
	}

	public synchronized boolean putLock(int commandId, String filePath) throws InterruptedException {

		if (writeLockQueue.peek() == commandId) {
			if (readLockCheck.containsKey(filePath)) {
				if (readLockCheck.get(filePath).getReadLockCount() > 0) {
					return false;
				} else if (readLockCheck.get(filePath).writeLock().tryLock()) {
					commandMap.replace(commandId, LOCK);
					return true;
				} else {
					return false;
				}
			} else {
				ReentrantReadWriteLock writeLock = new ReentrantReadWriteLock();
				readLockCheck.put(filePath, writeLock);
				readLockCheck.get(filePath).writeLock().lock();
				commandMap.replace(commandId, "LOCK");
				writeLockQueue.remove(commandId);
				return true;
			}
		}
		return false;
	}

	public synchronized void putUnlock(int cmdID, String filePath, Boolean check) throws InterruptedException {

		readLockCheck.get(filePath).writeLock().unlock();
		Thread.currentThread().sleep(50);
		if (readLockCheck.containsKey(filePath)) {
			if (!readLockCheck.get(filePath).isWriteLocked() && readLockCheck.get(filePath).getReadLockCount() == 0) {
				readLockCheck.remove(filePath);
			}
		}
		if (check) {
			commandMap.replace(cmdID, TERMINATE);
		} else {
			commandMap.remove(cmdID);
		}

	}

	public synchronized boolean deleteCheck(String filePath) {
		if (readLockCheck.containsKey(filePath)) {
			if (readLockCheck.get(filePath).isWriteLocked() || readLockCheck.get(filePath).getReadLockCount() > 0) {
				return false;
			} else {
				return true;
			}
		} else {
			return true;
		}
	}

	public static int generateCommandID() {
		Random random = new Random();
		int commandID = 0;
		commandID = random.nextInt(9) * 10;
		return commandID;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}
}
