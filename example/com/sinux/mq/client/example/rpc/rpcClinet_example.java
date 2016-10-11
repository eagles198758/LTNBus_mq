package com.sinux.mq.client.example.rpc;

import java.io.UnsupportedEncodingException;

import com.sinux.mq.client.MqChannel;
import com.sinux.mq.client.MqConnectionFactory;
import com.sinux.mq.client.mod.MsgDes;

public class rpcClinet_example {
	private static final String host = "localhost";// "192.168.0.105"
	private static final String port = "5672";// "5673"
	private static final String userName = "test";// "sinux"
	private static final String passWord = "test";// "sinux123"
	private static final String queueName = "rpc_queue";
	
	private static void rpcClient() throws UnsupportedEncodingException {
		// 构造mq工厂类
		MqConnectionFactory factory = new MqConnectionFactory(host, port, userName, passWord);
		// 初始化工厂类，建立服务连接
		factory.initMq();
		// 打开并获取传输通道
		MqChannel channel = factory.openChannel(queueName, false, true);
		for(;;){
			MsgDes msgDes = new MsgDes();
			msgDes.setId("2342123");
			System.out.println(new String(factory.rpcCall(channel, msgDes.serialize())));
			System.out.println(new String(factory.rpcCall(channel, "hello world!".getBytes())));
		}
//		factory.closeChannel(channel);
//		factory.closeMqConnection();
	}
	
	public static void main(String[] args) throws UnsupportedEncodingException {
		rpcClient();
	}
}
