package com.sinux.mq.client.example.p2p;

import com.sinux.mq.client.MqChannel;
import com.sinux.mq.client.MqConnectionFactory;
import com.sinux.mq.client.mod.MsgDes;

public class send_example {
	public static final String host = "localhost";// "192.168.0.105"
	public static final String port = "5672";// "5673"
	public static final String userName = "test";// "sinux"
	public static final String passWord = "test";// "sinux123"
	public static final String queueName = "zxh";
	public static final String message = "Hello World!";

	public static void send() {
		// ����mq������
		MqConnectionFactory factory = new MqConnectionFactory(host, port, userName, passWord);
		// ��ʼ�������࣬������������
		factory.initMq();
		MsgDes msgDes = new MsgDes();
		// �򿪲���ȡ����ͨ��
		MqChannel channel = factory.openChannel(queueName, false, false);
		byte[] msg = message.getBytes();
		// �����������Ϣ
		for (int i = 0; i < 100; i++) {
			msgDes.setId(String.valueOf(i));
			factory.putData(msgDes, msg, channel);
			System.out.println("***************header***************");
			System.out.println(msgDes.toString());
			System.out.println("****************body****************");
			System.out.println("body:" + message);
			System.out.println("body.length:" + msg.length);
			System.out.println("====================================");
		}
		// �رմ���ͨ��
		factory.closeChannel(channel);
		// �رշ�������
		factory.closeMqConnection();
	}
	
	public static void main(String[] args) {
		send();
	}
}
