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
		// ����mq������
		MqConnectionFactory factory = new MqConnectionFactory(host, port, userName, passWord);
		// ��ʼ�������࣬������������
		factory.initMq();
		// �򿪲���ȡ����ͨ��
		MqChannel channel = factory.openChannel(queueName, false, false);
		// ����Զ�̹��̶���
		Procedure proc = new MyProcedure();
		// ����Զ�̹��̷���
		factory.rpcServer(channel, proc);
	}

	public static void main(String[] args) {
		rpcServer();
	}
}

/**
 * Զ�̹���ʵ����
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
