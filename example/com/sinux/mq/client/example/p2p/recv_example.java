package com.sinux.mq.client.example.p2p;

import com.sinux.mq.client.MqChannel;
import com.sinux.mq.client.MqConnectionFactory;
import com.sinux.mq.client.MqConsumer;
import com.sinux.mq.client.mod.MsgDes;

public class recv_example {
	public static final String host = "localhost";//"192.168.0.105"
	public static final String port = "5672";//"5673"
	public static final String userName = "test";//"sinux"
	public static final String passWord = "test";//"sinux123"
	public static final String queueName = "zxh";
	
	public static void recv(){
		//�������ӹ�����
		MqConnectionFactory factory = new MqConnectionFactory(host, port, userName, passWord);
		//��ʼ�������࣬��������
		factory.initMq();
		//��ȡͨ��
		MqChannel channel = factory.openChannel(queueName, false, false);
		//����consumer����ʵ��handleDeliver()���Ի�ȡ��Ϣ
		MqConsumer consumer = new MqConsumer(12, channel) {
			@Override
			public void handleDeliver(byte[] body, byte[] msgDes) {
				System.out.println("***************header***************");
				//��ȡ��Ϣͷ
				MsgDes msgHeaderDes = MsgDes.unserialize(msgDes);
				System.out.println(msgHeaderDes.toString());
				System.out.println("****************body****************");
				//��ȡ��Ϣ��
				System.out.println("body:" + new String(body));
				System.out.println("====================================");
			}
		};
		//��ʼ��������
		factory.getData(consumer);
	}
	
	public static void main(String[] args) {
		recv();
	}
}
