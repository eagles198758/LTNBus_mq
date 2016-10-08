package com.sinux.mq.client.test;

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

	@SuppressWarnings("static-access")
	@Test
	public void testConnection() throws Exception {
		// 构造mq工厂类
		MqConnectionFactory factory = new MqConnectionFactory(host, port, userName, passWord);
		// 初始化工厂类，建立服务连接
		factory.initMq();
		
		MqTransInterface tranInterface = new MqTransInterface(factory);
		TransInfo transInfo = new TransInfo();
		tranInterface.initFromConfig("./sysparamconfig.xml");
		tranInterface.sendFile("J:\\Installer Package\\FINAL.FANTASY\\3DMGAME-FINAL.FANTASY.TYPE-0.HD.Repack-3DM\\3DMGAME-FINAL.FANTASY.TYPE-0.HD.Repack-3DM.part26.rar", null, transInfo);
		
	}
}
