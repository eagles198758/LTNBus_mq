package com.sinux.mq.client.test;

import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import com.sinux.mq.client.MqConnectionFactory;
import com.sinux.mq.client.file.services.MqTransInterface;
import com.sinux.mq.client.mod.ReceveFileData;
import com.sinux.mq.client.mod.TransInfo;

public class MqConnectionFactory_test {

	public static final String host = "localhost";
	public static final String port = "5672";
	public static final String userName = "test";
	public static final String passWord = "test";
	public static final String queueName = "eclipse-jee-neon-R-win32.zip";

	@Test
	public void testSendFile() throws Exception {
		// 构造mq工厂类
		MqConnectionFactory factory = new MqConnectionFactory(host, port, userName, passWord);
		// 初始化工厂类，建立服务连接
		factory.initMq();
		
		MqTransInterface tranInterface = new MqTransInterface(factory);
		TransInfo transInfo = new TransInfo();
		tranInterface.initFromConfig("./sysparamconfig.xml");
		tranInterface.sendFile("E:/install packages/eclipse-jee-neon-R-win32.zip", null, transInfo);
	}
	
	@SuppressWarnings("rawtypes")
	@Test
	public void testReceiveFile() throws Exception {
		// 构造mq工厂类
		MqConnectionFactory factory = new MqConnectionFactory(host, port, userName, passWord);
		// 初始化工厂类，建立服务连接
		factory.initMq();

		MqTransInterface tranInterface = new MqTransInterface(factory);
		TransInfo transInfo = new TransInfo();
		ReceveFileData receveFileData = (ReceveFileData)tranInterface.getReceiveFileList("eclipse-jee-neon-R-win32.zip",1000).get(0);
		tranInterface.recvFile(receveFileData, "F:\temp\trans\recv", "eclipse-jee-neon-R-win32.zip", transInfo, new LinkedList());
	}
	
	@Test
	public void test() throws Exception {
		// 构造mq工厂类
		MqConnectionFactory factory = new MqConnectionFactory(host, port, userName, passWord);
		// 初始化工厂类，建立服务连接
		factory.initMq();

		System.out.println(factory.openChannel(queueName, true, false) == null);
	}
}
