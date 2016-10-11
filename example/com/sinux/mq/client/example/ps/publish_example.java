package com.sinux.mq.client.example.ps;

import java.io.UnsupportedEncodingException;

import com.sinux.mq.client.MqChannel;
import com.sinux.mq.client.MqConnectionFactory;
import com.sinux.mq.client.mod.MsgDes;

public class publish_example {
	private static final String host = "localhost";// "192.168.0.105"
	private static final String port = "5672";// "5673"
	private static final String userName = "test";// "sinux"
	private static final String passWord = "test";// "sinux123"
	
	private static final String message = "Hello World!";
	private static final String topicName = "logs";
	private static final String queueName = "pub_log_queue";
	private static final boolean isPersist = false;
	private static final String filterKey1 = "log.a";
	private static final String filterKey2 = "log.b";

	private static void pub() throws UnsupportedEncodingException {
		// 构造mq工厂类
		MqConnectionFactory factory = new MqConnectionFactory(host, port, userName, passWord);
		// 初始化工厂类，建立服务连接
		factory.initMq();
		MsgDes msgDes = new MsgDes();
		// 打开并获取传输通道
		MqChannel channel = factory.openTopicChannel(topicName, isPersist, queueName);
		byte[] msg = message.getBytes("UTF-8");
		for(;;){
			// 向服务发布消息
			msgDes.setId("1");
			factory.publish(msgDes, msg, channel, filterKey1);
			System.out.println("***************header***************");
			System.out.println(msgDes.toString());
			System.out.println("****************body****************");
			System.out.println("body:" + message);
			System.out.println("body.length:" + msg.length);
			System.out.println("====================================");
			
			// 向服务发布消息
			msgDes.setId("2");
			factory.publish(msgDes, msg, channel, filterKey2);
			System.out.println("***************header***************");
			System.out.println(msgDes.toString());
			System.out.println("****************body****************");
			System.out.println("body:" + message);
			System.out.println("body.length:" + msg.length);
			System.out.println("====================================");
		}
//		// 关闭传输通道
//		factory.closeChannel(channel);
//		// 关闭服务连接
//		factory.closeMqConnection();
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		pub();
	}
}
