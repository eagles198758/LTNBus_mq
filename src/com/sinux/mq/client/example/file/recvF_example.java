package com.sinux.mq.client.example.file;

import com.sinux.mq.client.MqChannel;
import com.sinux.mq.client.MqConnectionFactory;

public class recvF_example {
	public static String host = "localhost";//"192.168.0.105"
	public static String port = "5672";//"5673"
	public static String userName = "test";//"sinux"
	public static String passWord = "test";//"sinux123"
	
	public static String outputFilePath = "E:/docs/mq/TLQ²âÊÔÇé¿ö1.docx";
	public static String queueName = "file";
	
	public static void recv(){
		MqConnectionFactory factory = new MqConnectionFactory(host, port, userName, passWord);
		factory.initMq();
		MqChannel channel = factory.openChannel(queueName, false, false);
		factory.getFile(outputFilePath, channel, queueName);
	}
	
	public static void main(String[] args) {
		recv();
	}
}
