package com.sinux.mq.client.file.services;

import java.util.*;
import com.sinux.mq.client.MqChannel;
import com.sinux.mq.client.MqConnectionFactory;

/**
 * @author jingwen.tong 2006-12-15
 *  Copyright IBM 2005
 *  实现MQ的连接池功能，并且提供当网络失效的时候，清除所有相应的连接的功能。
 *  并且对于长时间空闲的连接将被清除掉
 */
public class MqChannelPool {

	private static MqChannelPool channelPool = null;
	@SuppressWarnings("rawtypes")
	private Map channelsPool = new HashMap();// 容纳与队列管理器连接的容器
	private Object synObject1 = new Object();// 同步对象1
	// private Object synObject2= new Object();//同步对象1
//	private SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm.ss.SSS");
	
	private static MqConnectionFactory factory = null;

	private MqChannelPool() {
		
	}

	private MqChannel addMqChannel(String queueName) {
		return factory.openChannel(queueName, true, false);
	}

	// 通过此方法获得队列池实例，从而实现单根模型
	public synchronized static MqChannelPool getSingleInstance(MqConnectionFactory connectionfactory) {
		if (channelPool == null) {
			channelPool = new MqChannelPool();
//			if (GlobalVar.isStartPurgeThread) {// 启动连接清除线程
//				PurgeChannelPoolThread purgePoolThread = PurgeChannelPoolThread.getSingleInstance(connectionfactory);
//				purgePoolThread.start();// 开始启动了
//			}
			try {
				GlobalVar.notifyPurgeThreadThreadEvent.SetEvent();
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			factory = connectionfactory;
		}
		return channelPool;
	}

	// 清除超时的空闲线程
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void destoryTimeOutChannel() {
		List purgeChannelPool = new LinkedList();
		long currentTime, timeOutTime;

		synchronized (synObject1) {
			if (channelsPool == null || channelsPool.size() == 0) {

			} else {
				for (Iterator i = channelsPool.entrySet().iterator(); i.hasNext();) {
					MqChannel channel = (MqChannel) i.next();
					if (channel == null)
						continue;
					if (channel.getChannel().isOpen())
						continue;
					else {
						currentTime = System.currentTimeMillis();
						timeOutTime = channel.getTimeOutTime();
						if (currentTime - channel.getFreeTime() >= timeOutTime) {// 说明这个空闲的连接超时了，需要清除
							// 断开与MQ的连接
							factory.closeChannel(channel);
							channel.setUse(false);
							purgeChannelPool.add(channel);
						}
					}
				}
				// 清除对象
				for (Iterator ii = purgeChannelPool.iterator(); ii.hasNext();) {
					MqChannel channel = (MqChannel) ii.next();
					channelsPool.remove(channel);
				}
				purgeChannelPool.removeAll(purgeChannelPool);
			}
		}
	}

	// 清除connectionInfo对应的子连接池中所有的连接
	@SuppressWarnings("rawtypes")
	public void destoryAllChannel() {
		synchronized (synObject1) {
			if (channelsPool == null || channelsPool.size() == 0) {
				// 不用干任何事情
			} else {
				for (Iterator i = channelsPool.entrySet().iterator(); i.hasNext();) {
					MqChannel mqChannel = (MqChannel) i.next();
					factory.closeChannel(mqChannel);
					channelsPool.remove(mqChannel);
				}
			}
		}
	}

	// 把连接放入连接池中
	public void freeChannel(MqChannel channel) {
		if (channel == null)
			return;
		synchronized (synObject1) {
			channel.setFreeTime(System.currentTimeMillis());
			channel.setUse(false);
		}
	}

	// 如果返回null表示无法从连接池中取得连接
	@SuppressWarnings({ "unchecked" })
	public MqChannel getMqChannel(MqChannel channel) {
		String queueName = channel.getQueueName();
		synchronized (synObject1) {
			/*
			 * 查看是否有可用的连接，取出要连接的队列管理器是否存在一个子连接池，并且这个子连接池 当中是否存在可用的连接
			 */
			channel = (MqChannel) channelsPool.get(queueName);
			boolean flag = true;// 标志需要创建一个新的连接
			if (channel == null) {
				
			} else {
				if (channelsPool.size() > 0) {
					// 从这个子池当中查找是否存在可用的连接
					if (channel.getChannel() != null) {
						if (channel.getChannel().isOpen()) {
							return channel;
						} else {
							if (!channel.getChannel().isOpen()) {
								channel.setUseTime(System.currentTimeMillis());
								channel.setUse(true);
								flag = false;
							} else {
								channel.setFreeTime(0);// 清除线程启动时候将会把这个给清除掉
								channel.setUse(false);
							}
						}
					}
				}
			}
			if (flag) {// 需要重新创建一个新的连接
				int channelPoolNum = GlobalVar.channelPoolNum;
				if (channelsPool.size() < channelPoolNum) {
					channel = addMqChannel(queueName);
					channelsPool.put(queueName, channel);
				} else {
					// 说明达到最大的连接数
					System.out.println("达到最大的连接数");
				}
			}
		}
		if (channel.getChannel() != null) {
			if (!channel.getChannel().isOpen()) {// 连接是无效的
				channel.setFreeTime(0);// 清除线程启动时候将会把这个给清除掉
				channel.setUse(false);
				return null;
			}
		}
		return channel;
	}
	
	public boolean isExistMqChannel(MqChannel channel){
		if(channelsPool.get(channel.getQueueName()) != null){
			return true;
		}
		return false;
	}
}
