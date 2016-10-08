package com.sinux.mq.client.example.file;

import com.sinux.mq.client.MqChannel;
import com.sinux.mq.client.MqConnectionFactory;

public class sendF_example {
	public static String host = "localhost";//"192.168.0.105"
	public static String port = "5672";//"5673"
	public static String userName = "test";//"sinux"
	public static String passWord = "test";//"sinux123"
	
	public static String inputFilePath = "E:/docs/mq/TLQ�������.docx";
	public static String queueName = "file";
	
	public static void send(){
		MqConnectionFactory factory = new MqConnectionFactory(host, port, userName, passWord);
		factory.initMq();
		MqChannel channel = factory.openChannel(queueName, false, false);
		if(factory.putFile(inputFilePath, channel, queueName) == 0){
			System.out.println("���ͳɹ���");
		} else {
			System.out.println("����ʧ�ܣ�");
		}
		if(factory.closeChannel(channel) == 0){
			System.out.println("ͨ���ر�...");
		}
		if(factory.closeMqConnection() == 0){
			System.out.println("���ӹر�...");
		}
	}
	
	public static void main(String[] args) {
		send();
	}
}
