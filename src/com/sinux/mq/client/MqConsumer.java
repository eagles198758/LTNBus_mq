package com.sinux.mq.client;

import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.sinux.mq.client.util.ByteArrayutil;

/**
 * mq消费者类(数据接受处理类)
 * 
 * @author zhongxinghang
 *
 */
public abstract class MqConsumer {
	private DefaultConsumer consumer;

	private MqChannel mqChannel;

	public DefaultConsumer getConsumer() {
		return consumer;
	}

	public void setConsumer(DefaultConsumer consumer) {
		this.consumer = consumer;
	}

	public MqChannel getMqChannel() {
		return mqChannel;
	}

	public void setMqChannel(MqChannel mqChannel) {
		this.mqChannel = mqChannel;
	}

	/**
	 * 构造消费者
	 * 
	 * @param nLen
	 *            消息体长度
	 * @param mqChannel
	 *            通道对象
	 */
	public MqConsumer(final int nLen, MqChannel mqChannel) {
		this.mqChannel = mqChannel;
		consumer = new DefaultConsumer(mqChannel.getChannel()) {
			@Override
			public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
					byte[] body) throws IOException {
				byte[] msg = new byte[nLen];
				byte[] msgDes = new byte[body.length - nLen];
				ByteArrayutil.split(body, msgDes, msg);
				handleDeliver(msg, msgDes);
			}
		};
	}

	/**
	 * 
	 * 接受处理接受的消息
	 * 
	 * @param body
	 *            消息体
	 * @param msgDes
	 *            消息头
	 */
	public abstract void handleDeliver(byte[] body, byte[] msgDes);
}
