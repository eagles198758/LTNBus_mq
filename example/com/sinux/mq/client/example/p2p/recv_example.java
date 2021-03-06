package com.sinux.mq.client.example.p2p;

import java.io.IOException;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import com.sinux.mq.client.MqChannel;
import com.sinux.mq.client.MqConnectionFactory;
import com.sinux.mq.client.MqConsumer;
import com.sinux.mq.client.mod.MsgDes;
import com.sinux.mq.client.util.ByteArrayutil;

public class recv_example {
	public static final String host = "localhost";//"192.168.0.105"
	public static final String port = "5672";//"5673"
	public static final String userName = "test";//"sinux"
	public static final String passWord = "test";//"sinux123"
	public static final String queueName = "zxh";
	
	public static void recv(){
		//构造连接工厂类
		MqConnectionFactory factory = new MqConnectionFactory(host, port, userName, passWord);
		//初始化工厂类，创建连接
		factory.initMq();
		//获取通道
		MqChannel channel = factory.openChannel(queueName, false, false);
		//构造consumer对象，实现handleDeliver()，以获取消息
		MqConsumer consumer = new MqConsumer(12, channel) {
			@Override
			public void handleDeliver(byte[] body, byte[] msgDes) {
				System.out.println("***************header***************");
				//获取消息头
				MsgDes msgHeaderDes = MsgDes.unserialize(msgDes);
				System.out.println(msgHeaderDes.toString());
				System.out.println("****************body****************");
				//获取消息体
				System.out.println("body:" + new String(body));
				System.out.println("====================================");
			}
		};
		//开始接受数据
		factory.getData(consumer);
	}
	
	public static void recv1(){
		//构造连接工厂类
		MqConnectionFactory factory = new MqConnectionFactory(host, port, userName, passWord);
		//初始化工厂类，创建连接
		factory.initMq();
		//获取通道
		MqChannel channel = factory.openChannel(queueName, false, false);
		//构造consumer对象，实现handleDeliver()，以获取消息
		QueueingConsumer consumer = new QueueingConsumer(channel.getChannel());
		try {
			channel.getChannel().basicConsume(queueName, true, consumer);
			while(true){
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				byte[] body = delivery.getBody();
				byte[] msg = new byte[12];
				byte[] msgDes = new byte[body.length - 12];
				ByteArrayutil.split(body, msgDes, msg);
				System.out.println(new String(msg));
			}
		} catch (IOException | ShutdownSignalException | ConsumerCancelledException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		recv1();
	}
}
