package com.sinux.mq.client.example.rpc;

import com.sinux.mq.client.MqChannel;
import com.sinux.mq.client.MqConnectionFactory;
import com.sinux.mq.client.Procedure;
import com.sinux.mq.client.mod.MsgDes;

public class rpcServer_example {
	private static final String host = "localhost";// "192.168.0.105"
	private static final String port = "5672";// "5673"
	private static final String userName = "test";// "sinux"
	private static final String passWord = "test";// "sinux123"
	private static final String queueName = "rpc_queue";

	private static void rpcServer() {
		// 构造mq工厂类
		MqConnectionFactory factory = new MqConnectionFactory(host, port, userName, passWord);
		// 初始化工厂类，建立服务连接
		factory.initMq();
		// 打开并获取传输通道
		MqChannel channel = factory.openChannel(queueName, false, false);
		// 构造远程过程对象
		Procedure proc = new MyProcedure();
		// 开起远程过程服务
		factory.rpcServer(channel, proc);
	}

	public static void main(String[] args) {
		rpcServer();
	}
}

/**
 * 远程过程实现类
 * 
 * @author zhongkehexun
 *
 */
class MyProcedure implements Procedure {

	@Override
	public byte[] process(byte[] params) {
		MsgDes msg = MsgDes.unserialize(params);
		if (msg instanceof MsgDes) {
			System.out.println(msg.toString());
			return "job 1 done!".getBytes();
		} else {
			System.out.println(new String(params));
			return "job 2 done!".getBytes();
		}
//		try {
//			return "job done!".getBytes("UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
//		return null;
	}
}
