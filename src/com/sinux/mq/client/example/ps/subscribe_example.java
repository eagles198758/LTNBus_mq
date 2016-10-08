package com.sinux.mq.client.example.ps;

import com.sinux.mq.client.MqChannel;
import com.sinux.mq.client.MqConnectionFactory;
import com.sinux.mq.client.MqConsumer;
import com.sinux.mq.client.mod.MsgDes;

public class subscribe_example {
	private static final String host = "localhost";// "192.168.0.105"
	private static final String port = "5672";// "5673"
	private static final String userName = "test";// "sinux"
	private static final String passWord = "test";// "sinux123"

	private static final String topicName = "logs";
	private static final String queueName = "sub_log_queun";
	private static final boolean isPersist = false;
	private static final String filterKey = "log.*";

	private static void sub() {
		// 构造mq工厂类
		MqConnectionFactory factory = new MqConnectionFactory(host, port, userName, passWord);
		// 初始化工厂类，建立服务连接
		factory.initMq();
		// 打开并获取传输通道
		MqChannel channel = factory.openTopicChannel(topicName, isPersist, queueName);
		// 构造consumer对象，实现handleDeliver()，以获取消息
		MqConsumer consumer = new MqConsumer(12, channel) {
			@Override
			public void handleDeliver(byte[] body, byte[] msgDes) {
				System.out.println("body.length:" + body.length + "/" + "msgDes.length:" + msgDes.length);
				System.out.println("***************header***************");
				// 获取消息头
				MsgDes msgHeaderDes = MsgDes.unserialize(msgDes);
				System.out.println(msgHeaderDes.toString());
				System.out.println("****************body****************");
				// 获取消息体
				System.out.println("body:" + new String(body));
				System.out.println("====================================");
			}
		};
		//开始接受订阅数据
		factory.subscribe(consumer, filterKey);
	}

	public static void main(String[] args) {
		sub();
	}
}
