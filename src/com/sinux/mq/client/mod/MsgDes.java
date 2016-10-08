package com.sinux.mq.client.mod;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.sinux.mq.client.util.E_Msg_Type;

/**
 * 消息头，消息描述类
 * @author zhongkehexun
 *
 */
public class MsgDes implements Serializable {
	private static final long serialVersionUID = -4157373026358438323L;
	//根据订阅消息描述计算出的hash值，对应于反向代理服务器中的客户端队列索引，64字节串。
	private String id = "0";
	//8字节时间，精度为ms，默认为当前时间。
	private long timestamp = System.currentTimeMillis();
	//系统必须清晰了解当前数据为何种数据，系统支持的类型包括，1字节，默认为0（binary）。
	private E_Msg_Type eMsgType = E_Msg_Type.E_BINARY;
	//消息发送前必须指明消息体数据的总长度。（消息头字节长度+消息体字节长度）
	private int msgSize;
	//生命周期
	private int tTL;
	//标识消息所对应业务的使用级别。1字节，取值0 - 9，默认值为0。
	private int priority = 0;
	//可能在系统中优先处理的业务id，4字节，保留字段，默认为0。
	private int serviceId = 0;
	//是否需要对数据进行加密，以及加密的格式，1字节。第0bit代表是否计算checksum，第1bit代表是否以aes128加密，第2bit代表是否以base64编码，默认为null。
	private byte[] encrypt = null;
	//是否压缩，false不压缩，true压缩，默认为false
	private boolean compress = false;
	//可设置的一些Socket Option。1字节，取值0 - 255，默认为0。
	private byte option = 0; 
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public E_Msg_Type geteMsgType() {
		return eMsgType;
	}
	public void seteMsgType(E_Msg_Type eMsgType) {
		this.eMsgType = eMsgType;
	}
	public int getMsgSize() {
		return msgSize;
	}
	public void setMsgSize(int msgSize) {
		this.msgSize = msgSize;
	}
	public int gettTL() {
		return tTL;
	}
	public void settTL(int tTL) {
		this.tTL = tTL;
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public int getServiceId() {
		return serviceId;
	}
	public void setServiceId(int serviceId) {
		this.serviceId = serviceId;
	}
	public byte[] getEncrypt() {
		return encrypt;
	}
	public void setEncrypt(byte[] encrypt) {
		this.encrypt = encrypt;
	}
	public boolean isCompress() {
		return compress;
	}
	public void setCompress(boolean compress) {
		this.compress = compress;
	}
	public byte getOption() {
		return option;
	}
	public void setOption(byte option) {
		this.option = option;
	}
	
	/**
	 * 序列化消息头
	 * @return 字节数组
	 */
	public byte[] serialize() {
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;
		try {
			// 序列化
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			return baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				if(baos != null){
					baos.flush();
					baos.close();
				}
				if(oos != null){
					oos.flush();
					oos.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * 反序列化消息头
	 * @return 消息头对象
	 */
	public static MsgDes unserialize(byte[] bytes) {
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try {
            // 反序列化
            bais = new ByteArrayInputStream(bytes);
            ois = new ObjectInputStream(bais);
            return (MsgDes) ois.readObject();
        } catch (Exception e) {

        } finally{
			try {
				if(bais != null){
					bais.close();
				}
				if(ois != null){
					ois.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        return null;
    }
	
	@Override
	public String toString(){
		return "id:" + id + 
				"\ntimestamp:" + timestamp + 
				"\neMsgType:" + eMsgType + 
				"\nmsgSize:" + msgSize + 
				"\ntTL:" + tTL + 
				"\npriority:" + priority + 
				"\nserviceId:" + serviceId + 
				"\nencrypt:" + encrypt + 
				"\ncompress:" + compress + 
				"\noption:" + option;
	}
}
