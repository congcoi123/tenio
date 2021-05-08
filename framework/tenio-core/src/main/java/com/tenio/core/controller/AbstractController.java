package com.tenio.core.controller;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

import com.tenio.core.exception.RequestQueueFullException;
import com.tenio.core.network.entity.protocol.Request;

public abstract class AbstractController implements Controller, Runnable {
	protected Object id;
	protected String name;
	protected BlockingQueue<Request> requestQueue;
	protected ExecutorService threadPool;
	protected int threadPoolSize = -1;
	protected volatile int maxQueueSize = -1;
	protected volatile boolean isActive = false;
	private volatile int threadId = 1;

	public void enqueueRequest(Request request) throws RequestQueueFullException {
		if (this.requestQueue.size() >= this.maxQueueSize) {
			throw new RequestQueueFullException();
		} else {
			this.requestQueue.add(request);
		}
	}

	public void init(Object o) {
		if (this.isActive) {
			throw new IllegalArgumentException("Object is already initialized. Destroy it first!");
		} else if (this.threadPoolSize < 1) {
			throw new IllegalArgumentException("Illegal value for a thread pool size: " + this.threadPoolSize);
		} else if (this.maxQueueSize < 1) {
			throw new IllegalArgumentException("Illegal value for max queue size: " + this.maxQueueSize);
		} else {
			Comparator<Request> requestComparator = new RequestComparator();
			this.requestQueue = new PriorityBlockingQueue<Request>(50, requestComparator);
			this.threadPool = Executors.newFixedThreadPool(this.threadPoolSize);
			this.isActive = true;
			this.initThreadPool();
			// this.bootLogger.info(String.format("Controller started: %s -- Queue: %s/%s",
			// this.getClass().getName(), this.getQueueSize(), this.getMaxQueueSize()));
		}
	}

	public void destroy(Object o) {
		this.isActive = false;
		List leftOvers = this.threadPool.shutdownNow();
//          this.logger.info("Controller stopping: " + this.getClass().getName() + ", Unprocessed tasks: " + leftOvers.size());
	}

	public void handleMessage(Object obj) {
	}

	public void run() {
		Thread.currentThread().setName(this.getClass().getName() + "-" + this.threadId++);

		while (this.isActive) {
			try {
				Request request = (Request) this.requestQueue.take();
				this.processRequest(request);
			} catch (InterruptedException var2) {
				this.isActive = false;
//                    this.logger.warn("Controller main loop was interrupted");
//                    Logging.logStackTrace(this.logger, (Throwable)var2);
			} catch (Throwable var3) {
//                    Logging.logStackTrace(this.logger, var3);
			}
		}

//          this.bootLogger.info("Controller worker threads stopped: " + this.getClass().getName());
	}

	public abstract void processRequest(Request var1) throws Exception;

	public Object getId() {
		return this.id;
	}

	public void setId(Object id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getThreadPoolSize() {
		return this.threadPoolSize;
	}

	public void setThreadPoolSize(int threadPoolSize) {
		if (this.threadPoolSize < 1) {
			this.threadPoolSize = threadPoolSize;
		}

	}

	public int getQueueSize() {
		return this.requestQueue.size();
	}

	public int getMaxQueueSize() {
		return this.maxQueueSize;
	}

	public void setMaxQueueSize(int maxQueueSize) {
		this.maxQueueSize = maxQueueSize;
	}

	protected void initThreadPool() {
		for (int j = 0; j < this.threadPoolSize; ++j) {
			this.threadPool.execute(this);
		}

	}

}
