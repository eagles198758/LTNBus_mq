package com.sinux.mq.client.example.file;

import com.sinux.mq.client.MqConnectionFactory;

public class sendF_example {
	public static String host = "localhost";//"192.168.0.105"
	public static String port = "5672";//"5673"
	public static String userName = "test";//"sinux"
	public static String passWord = "test";//"sinux123"
	
	public static String inputFilePath = "J:/Downloads/git.rar";
	
	public static void send(){
		MqConnectionFactory factory = new MqConnectionFactory(host, port, userName, passWord);
		factory.initMq();
		if(factory.putFile(inputFilePath) == 0){
			System.out.println("发送成功！");
		} else {
			System.out.println("发送失败！");
		}
		if(factory.closeMqConnection() == 0){
			System.out.println("连接关闭...");
		}
	}
	
	public static void main(String[] args) {
		send();
	}
}
