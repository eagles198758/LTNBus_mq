package com.sinux.mq.client.file.services;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author jingwen.tong 2006-2-20 �̳߳ص�ʵ��
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
			// �����̵߳�����
			serviceThread.setName("serviceThread" + (i + 1));
			serviceThread.start();// �����߳�
			pool.add(serviceThread);
		}
	}

	// ͨ���˷�������̳߳�ʵ�����Ӷ�ʵ�ֵ���ģ��
	public synchronized static ThreadPool getSingleInstance() {
		if (threadPool == null) {
			threadPool = new ThreadPool(GlobalVar.threadPoolCount);
		}
		return threadPool;
	}

	// ��ÿ��еķ����̣߳��������NULL���ʾ�̳߳��Ѿ�����
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

	// �ѷ����߳���Ϊ�����߳����������̳߳���
	public synchronized void returnServiceThread(ServiceThread serviceThread) {
		for (Iterator i = pool.iterator(); i.hasNext();) {
			ServiceThread serviceThreadTmp = (ServiceThread) i.next();
			if (serviceThreadTmp == serviceThread) {
				serviceThread.setIdleStatus(true);
				return;
			}
		}
	}

	// �Ƿ��п��е��߳̽��з���
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
