//package com.sinux.mq.client.file.services;
//
//import com.sinux.mq.client.MqConnectionFactory;
//
///*
// * ������ӳ��еĳ�ʱ���ӵ��߳�
// */
//public class PurgeChannelPoolThread extends Thread {
//	private static PurgeChannelPoolThread purgeChannelPool = null;
//	private static MqConnectionFactory factory = null;
//	private PurgeChannelPoolThread() {
//
//	}
//
//	public synchronized static PurgeChannelPoolThread getSingleInstance(MqConnectionFactory connectionfactory) {
//		if (purgeChannelPool == null) {
//			purgeChannelPool = new PurgeChannelPoolThread();
//		}
//		factory = connectionfactory;
//		return purgeChannelPool;
//	}
//
//	public synchronized void run() {
//		try {
//			while (true) {
//				int retval = GlobalVar.notifyPurgeThreadThreadEvent.WaitForSingleObject(-1);
//				if (retval != 0)
//					continue;
//				else
//					break;
//			}
//			while (GlobalVar.isRun) {
//				int count = GlobalVar.purgeThreadRunInterval / 1000;
//				for (int i = 0; i < count; i++) {
//					sleep(1000);// ����1000����
//					if (!GlobalVar.isRun)// Ҫ�����������߳��˳�.....
//						return;
//				}
//				// ���Կ�ʼ������
//				MqChannelPool connectionPool = MqChannelPool.getSingleInstance(factory);
//				connectionPool.destoryTimeOutChannel();// ������г�ʱ�Ŀ����߳�.
//			}
//
//		} catch (Exception exc) {
//
//		}
//	}
//}
