package com.sinux.mq.client.file.services;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author jingwen.tong 2006-2-20 线程池的实现
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class ThreadPool {

	private List pool = new LinkedList();

	private static ThreadPool threadPool = null;

	private ThreadPool() {

	}

	private ThreadPool(int poolSize) {
		for (int i = 0; i < poolSize; i++) {
			ServiceThread serviceThread = new ServiceThread(this);
			// 这是线程的名字
			serviceThread.setName("serviceThread" + (i + 1));
			serviceThread.start();// 启动线程
			pool.add(serviceThread);
		}
	}

	// 通过此方法获得线程池实例，从而实现单根模型
	public synchronized static ThreadPool getSingleInstance() {
		if (threadPool == null) {
			threadPool = new ThreadPool(GlobalVar.threadPoolCount);
		}
		return threadPool;
	}

	// 获得空闲的服务线程，如果返回NULL则表示线程池已经满了
	public synchronized ServiceThread getIdleServiceThread() {
		ServiceThread serviceThread = null;
		for (Iterator i = pool.iterator(); i.hasNext();) {
			serviceThread = (ServiceThread) i.next();
			if (serviceThread.isIdle()) {
				serviceThread.setIdleStatus(false);
				return serviceThread;
			}
		}
		return null;
	}

	// 把服务线程做为空闲线程重新置入线程池中
	public synchronized void returnServiceThread(ServiceThread serviceThread) {
		for (Iterator i = pool.iterator(); i.hasNext();) {
			ServiceThread serviceThreadTmp = (ServiceThread) i.next();
			if (serviceThreadTmp == serviceThread) {
				serviceThread.setIdleStatus(true);
				return;
			}
		}
	}

	// 是否有空闲的线程进行服务
	public synchronized int getIdleThreadNum() {
		return pool.size();
	}
	
	public synchronized void shutdown() {
		for(Iterator i = pool.iterator(); i.hasNext();){
			ServiceThread serviceThread = (ServiceThread) i.next();
			serviceThread.interrupt();
		}
	}
}
