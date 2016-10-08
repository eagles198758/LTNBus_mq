package com.sinux.mq.client.example;

import com.sinux.mq.client.mod.MsgDes;

public class serialize_example {
	public static void main(String[] args) {
		split();
		serialize();
	}
	
	private static void serialize(){
		MsgDes msgDes = new MsgDes();
		System.out.println(msgDes.toString());
		byte[] b = msgDes.serialize();
		System.out.println(b.length);
		System.out.println(msgDes.getId());
		msgDes.setId("1233223412341");
		System.out.println(b.length);
		System.out.println(msgDes.getId());
		MsgDes msgDes1 = MsgDes.unserialize(b);
		System.out.println(msgDes1.getTimestamp() + "+" + msgDes1.getId());
	}
	
	private static void split(){
		byte[] msg = new String("hello world!").getBytes();
		int nLen = msg.length;
		MsgDes msgDes = new MsgDes();
		byte[] b = msgDes.serialize();
		int checkSum = b.length + nLen;
		byte[] data = new byte[checkSum];
		System.arraycopy(b, 0, data, 0, b.length);
		System.arraycopy(msg, 0, data, b.length, msg.length);
		System.out.println(data.length);
		byte[] split = new byte[nLen];
		byte[] obj = new byte[data.length - nLen];
		System.arraycopy(data, data.length - nLen, split, 0, nLen);
		System.arraycopy(data, 0, obj, 0, obj.length);
		System.out.println(new String(split));
		MsgDes m = MsgDes.unserialize(obj);
		System.out.println(m.getTimestamp());
	}
}
