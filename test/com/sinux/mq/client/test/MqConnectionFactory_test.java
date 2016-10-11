package com.sinux.mq.client.test;

import java.util.LinkedList;

import org.junit.Test;

import com.sinux.mq.client.MqConnectionFactory;
import com.sinux.mq.client.file.services.MqTransInterface;
import com.sinux.mq.client.mod.TransInfo;

public class MqConnectionFactory_test {

	public static final String host = "localhost";
	public static final String port = "5672";
	public static final String userName = "test";
	public static final String passWord = "test";
	public static final String queueName = "zxh";

	@Test
	public void testSendFile() throws Exception {
		// 构造mq工厂类
		MqConnectionFactory factory = new MqConnectionFactory(host, port, userName, passWord);
		// 初始化工厂类，建立服务连接
		factory.initMq();
		
		MqTransInterface tranInterface = new MqTransInterface(factory);
		TransInfo transInfo = new TransInfo();
		MqTransInterface.initFromConfig("./sysparamconfig.xml");
		tranInterface.sendFile("E:/install packages/eclipse-jee-neon-R-win32.zip", "eclipse-jee-neon-R-win32.zip", transInfo);
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
		MqTransInterface.initFromConfig("./sysparamconfig.xml");
		System.out.println(tranInterface.recvFile("E://recv", "eclipse-jee-neon-R-win32.zip", transInfo, new LinkedList()));
	}
}
