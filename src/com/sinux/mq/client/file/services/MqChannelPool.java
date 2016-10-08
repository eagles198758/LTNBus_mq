package com.sinux.mq.client.file.services;

import java.util.*;
import com.sinux.mq.client.MqChannel;
import com.sinux.mq.client.MqConnectionFactory;

/**
 * @author jingwen.tong 2006-12-15
 *  Copyright IBM 2005
 *  ʵ��MQ�����ӳع��ܣ������ṩ������ʧЧ��ʱ�����������Ӧ�����ӵĹ��ܡ�
 *  ���Ҷ��ڳ�ʱ����е����ӽ��������
 */
public class MqChannelPool {

	private static MqChannelPool channelPool = null;
	@SuppressWarnings("rawtypes")
	private Map channelsPool = new HashMap();// ��������й��������ӵ�����
	private Object synObject1 = new Object();// ͬ������1
	// private Object synObject2= new Object();//ͬ������1
//	private SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm.ss.SSS");
	
	private static MqConnectionFactory factory = null;

	private MqChannelPool() {
		
	}

	private MqChannel addMqChannel(String queueName) {
		return factory.openChannel(queueName, true, false);
	}

	// ͨ���˷�����ö��г�ʵ�����Ӷ�ʵ�ֵ���ģ��
	public synchronized static MqChannelPool getSingleInstance(MqConnectionFactory connectionfactory) {
		if (channelPool == null) {
			channelPool = new MqChannelPool();
//			if (GlobalVar.isStartPurgeThread) {// ������������߳�
//				PurgeChannelPoolThread purgePoolThread = PurgeChannelPoolThread.getSingleInstance(connectionfactory);
//				purgePoolThread.start();// ��ʼ������
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

	// �����ʱ�Ŀ����߳�
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
						if (currentTime - channel.getFreeTime() >= timeOutTime) {// ˵��������е����ӳ�ʱ�ˣ���Ҫ���
							// �Ͽ���MQ������
							factory.closeChannel(channel);
							channel.setUse(false);
							purgeChannelPool.add(channel);
						}
					}
				}
				// �������
				for (Iterator ii = purgeChannelPool.iterator(); ii.hasNext();) {
					MqChannel channel = (MqChannel) ii.next();
					channelsPool.remove(channel);
				}
				purgeChannelPool.removeAll(purgeChannelPool);
			}
		}
	}

	// ���connectionInfo��Ӧ�������ӳ������е�����
	@SuppressWarnings("rawtypes")
	public void destoryAllChannel() {
		synchronized (synObject1) {
			if (channelsPool == null || channelsPool.size() == 0) {
				// ���ø��κ�����
			} else {
				for (Iterator i = channelsPool.entrySet().iterator(); i.hasNext();) {
					MqChannel mqChannel = (MqChannel) i.next();
					factory.closeChannel(mqChannel);
					channelsPool.remove(mqChannel);
				}
			}
		}
	}

	// �����ӷ������ӳ���
	public void freeChannel(MqChannel channel) {
		if (channel == null)
			return;
		synchronized (synObject1) {
			channel.setFreeTime(System.currentTimeMillis());
			channel.setUse(false);
		}
	}

	// �������null��ʾ�޷������ӳ���ȡ������
	@SuppressWarnings({ "unchecked" })
	public MqChannel getMqChannel(MqChannel channel) {
		String queueName = channel.getQueueName();
		synchronized (synObject1) {
			/*
			 * �鿴�Ƿ��п��õ����ӣ�ȡ��Ҫ���ӵĶ��й������Ƿ����һ�������ӳأ�������������ӳ� �����Ƿ���ڿ��õ�����
			 */
			channel = (MqChannel) channelsPool.get(queueName);
			boolean flag = true;// ��־��Ҫ����һ���µ�����
			if (channel == null) {
				
			} else {
				if (channelsPool.size() > 0) {
					// ������ӳص��в����Ƿ���ڿ��õ�����
					if (channel.getChannel() != null) {
						if (channel.getChannel().isOpen()) {
							return channel;
						} else {
							if (!channel.getChannel().isOpen()) {
								channel.setUseTime(System.currentTimeMillis());
								channel.setUse(true);
								flag = false;
							} else {
								channel.setFreeTime(0);// ����߳�����ʱ�򽫻������������
								channel.setUse(false);
							}
						}
					}
				}
			}
			if (flag) {// ��Ҫ���´���һ���µ�����
				int channelPoolNum = GlobalVar.channelPoolNum;
				if (channelsPool.size() < channelPoolNum) {
					channel = addMqChannel(queueName);
					channelsPool.put(queueName, channel);
				} else {
					// ˵���ﵽ����������
					System.out.println("�ﵽ����������");
				}
			}
		}
		if (channel.getChannel() != null) {
			if (!channel.getChannel().isOpen()) {// ��������Ч��
				channel.setFreeTime(0);// ����߳�����ʱ�򽫻������������
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
