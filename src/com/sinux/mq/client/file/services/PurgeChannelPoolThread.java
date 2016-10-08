//package com.sinux.mq.client.file.services;
//
//import com.sinux.mq.client.MqConnectionFactory;
//
///*
// * 清除连接池中的超时连接的线程
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
//					sleep(1000);// 休眠1000毫秒
//					if (!GlobalVar.isRun)// 要求此连接清除线程退出.....
//						return;
//				}
//				// 可以开始服务了
//				MqChannelPool connectionPool = MqChannelPool.getSingleInstance(factory);
//				connectionPool.destoryTimeOutChannel();// 清除所有超时的空闲线程.
//			}
//
//		} catch (Exception exc) {
//
//		}
//	}
//}
