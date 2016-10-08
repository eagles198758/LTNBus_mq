package com.sinux.mq.client.mod;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.sinux.mq.client.util.E_Msg_Type;

/**
 * ��Ϣͷ����Ϣ������
 * @author zhongkehexun
 *
 */
public class MsgDes implements Serializable {
	private static final long serialVersionUID = -4157373026358438323L;
	//���ݶ�����Ϣ�����������hashֵ����Ӧ�ڷ������������еĿͻ��˶���������64�ֽڴ���
	private String id = "0";
	//8�ֽ�ʱ�䣬����Ϊms��Ĭ��Ϊ��ǰʱ�䡣
	private long timestamp = System.currentTimeMillis();
	//ϵͳ���������˽⵱ǰ����Ϊ�������ݣ�ϵͳ֧�ֵ����Ͱ�����1�ֽڣ�Ĭ��Ϊ0��binary����
	private E_Msg_Type eMsgType = E_Msg_Type.E_BINARY;
	//��Ϣ����ǰ����ָ����Ϣ�����ݵ��ܳ��ȡ�����Ϣͷ�ֽڳ���+��Ϣ���ֽڳ��ȣ�
	private int msgSize;
	//��������
	private int tTL;
	//��ʶ��Ϣ����Ӧҵ���ʹ�ü���1�ֽڣ�ȡֵ0 - 9��Ĭ��ֵΪ0��
	private int priority = 0;
	//������ϵͳ�����ȴ����ҵ��id��4�ֽڣ������ֶΣ�Ĭ��Ϊ0��
	private int serviceId = 0;
	//�Ƿ���Ҫ�����ݽ��м��ܣ��Լ����ܵĸ�ʽ��1�ֽڡ���0bit�����Ƿ����checksum����1bit�����Ƿ���aes128���ܣ���2bit�����Ƿ���base64���룬Ĭ��Ϊnull��
	private byte[] encrypt = null;
	//�Ƿ�ѹ����false��ѹ����trueѹ����Ĭ��Ϊfalse
	private boolean compress = false;
	//�����õ�һЩSocket Option��1�ֽڣ�ȡֵ0 - 255��Ĭ��Ϊ0��
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
	 * ���л���Ϣͷ
	 * @return �ֽ�����
	 */
	public byte[] serialize() {
		ObjectOutputStream oos = null;
		ByteArrayOutputStream baos = null;
		try {
			// ���л�
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
	 * �����л���Ϣͷ
	 * @return ��Ϣͷ����
	 */
	public static MsgDes unserialize(byte[] bytes) {
        ByteArrayInputStream bais = null;
        ObjectInputStream ois = null;
        try {
            // �����л�
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
