package com.sinux.mq.client.example.file;

import com.sinux.mq.client.MqConnectionFactory;

public class recvF_example {
	public static String host = "localhost";// "192.168.0.105"
	public static String port = "5672";// "5673"
	public static String userName = "test";// "sinux"
	public static String passWord = "test";// "sinux123"

	public static String outputFilePath = "F:/trans";
	public static String recvFileName = "git.rar";

	public static void recv() {
		MqConnectionFactory factory = new MqConnectionFactory(host, port, userName, passWord);
		factory.initMq();
		if (factory.getFile(outputFilePath, recvFileName) == 0) {
			System.out.println("���ճɹ���");
			System.out.println("�ļ�·����" + outputFilePath);
		} else {
			System.out.println("����ʧ�ܣ�");
		}
		if (factory.closeMqConnection() == 0) {
			System.out.println("���ӹر�...");
		}
	}

	public static void main(String[] args) {
		recv();
	}
}
