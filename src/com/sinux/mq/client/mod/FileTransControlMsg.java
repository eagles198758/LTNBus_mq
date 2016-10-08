package com.sinux.mq.client.mod;

import com.sinux.mq.client.util.ByteBuffer;

/**
 * @author jingwen.tong 2006-12-16 Copyright IBM 2005 文件传输控制消息定义
 */
//
public class FileTransControlMsg {
	/*
	 * public static int SEGSIZE_LENGTH = 4; public static int DIRNAME_LENGTH =
	 * 512; public static int FILENAME_LENGTH = 256; public static int
	 * CHUNKCOUNT_LENGTH = 4; public static int CHUNKNUM_LENGTH = 2; public
	 * static int DATASIZE_LENGTH = 32; public static int OFFSET_LENGTH = 32;
	 * public static int HOSTNAME_LENGTH = 32; public static int
	 * IPADDRESS_LENGTH = 32; public static int RECEIVER_LENGTH = 32; public
	 * static int QUEUENAME_LENGTH = 64;
	 * 
	 */
	/*
	 * private byte[] segSize = new byte[Constants.SEGSIZE_LENGTH]; private
	 * byte[] dirName = new byte[Constants.DIRNAME_LENGTH]; private byte[]
	 * fileName = new byte[Constants.FILENAME_LENGTH]; private byte[]
	 * chunkFileName = new byte[Constants.FILENAME_LENGTH]; private byte[]
	 * chunkCount = new byte[Constants.CHUNKCOUNT_LENGTH]; private byte[]
	 * chunkNum = new byte[Constants.CHUNKNUM_LENGTH]; private byte[] dataSize =
	 * new byte[Constants.DATASIZE_LENGTH]; private byte[] offset = new
	 * byte[Constants.OFFSET_LENGTH]; private byte[] hostName = new
	 * byte[Constants.HOSTNAME_LENGTH]; private byte[] ipAddress = new
	 * byte[Constants.IPADDRESS_LENGTH]; private byte[] receiverName = new
	 * byte[Constants.RECEIVER_LENGTH]; private byte[] queueName = new
	 * byte[Constants.QUEUENAME_LENGTH];
	 */
	private byte buffer[] = null;// 容纳数据的缓冲区
	private int msgDataLen = Constants.SEGSIZE_LENGTH + Constants.DIRNAME_LENGTH + Constants.FILENAME_LENGTH
			+ Constants.FILENAME_LENGTH + Constants.CHUNKCOUNT_LENGTH + Constants.CHUNKNUM_LENGTH
			+ Constants.FILEDATASIZE_LENGTH + Constants.OFFSET_LENGTH + Constants.HOSTNAME_LENGTH
			+ Constants.IPADDRESS_LENGTH + Constants.RECEIVER_LENGTH + Constants.QUEUENAME_LENGTH
			+ Constants.TRADECODE_LENGTH;

	/*
	 * 为何不采用set/get方法来设置成员值而采用把成员设置为PUBLIC，这个因为方法调用需要压栈\出栈， 损耗效率
	 */
	public String segSize = null;
	public String dirName = null;
	public String fileName = null;
	public String chunkFileName = null;
	public String chunkCount = null;
	public String chunkNum = null;
	public String dataSize = null;
	public String offsetFile = null;
	public String hostName = null;
	public String ipAddress = null;
	public String receiverName = null;
	public String queueName = null;
	public String tradeCode = null;

	public FileTransControlMsg() {
		buffer = new byte[msgDataLen];// 预先分配缓冲区
	}

	public byte[] getBuffer() {
		return this.buffer;
	}

	public void free() {
		segSize = null;
		dirName = null;
		fileName = null;
		chunkFileName = null;
		chunkCount = null;
		chunkNum = null;
		dataSize = null;
		offsetFile = null;
		hostName = null;
		ipAddress = null;
		receiverName = null;
		queueName = null;
		buffer = null;
		tradeCode = null;
	}

	/*
	 * 根据成员变量组织消息数据包
	 */
	public byte[] packMsgData() throws Exception {

		int offset = 0;
		ByteBuffer.memset(buffer, (byte) ' ');
		//
		int length = 0;

		length = segSize.getBytes().length;
		if (length > Constants.SEGSIZE_LENGTH)
			length = Constants.SEGSIZE_LENGTH;
		ByteBuffer.memcpy(buffer, offset, segSize.getBytes(), 0, length);
		offset += Constants.SEGSIZE_LENGTH;

		length = dirName.getBytes().length;
		if (length > Constants.DIRNAME_LENGTH)
			length = Constants.DIRNAME_LENGTH;
		ByteBuffer.memcpy(buffer, offset, dirName.getBytes(), 0, length);
		offset += Constants.DIRNAME_LENGTH;

		length = fileName.getBytes().length;
		if (length > Constants.FILENAME_LENGTH)
			length = Constants.FILENAME_LENGTH;
		ByteBuffer.memcpy(buffer, offset, fileName.getBytes(), 0, length);
		offset += Constants.FILENAME_LENGTH;

		length = chunkFileName.getBytes().length;
		if (length > Constants.FILENAME_LENGTH)
			length = Constants.FILENAME_LENGTH;
		ByteBuffer.memcpy(buffer, offset, chunkFileName.getBytes(), 0, length);
		offset += Constants.FILENAME_LENGTH;

		length = chunkCount.getBytes().length;
		if (length > Constants.CHUNKCOUNT_LENGTH)
			length = Constants.CHUNKCOUNT_LENGTH;
		ByteBuffer.memcpy(buffer, offset, chunkCount.getBytes(), 0, length);
		offset += Constants.CHUNKCOUNT_LENGTH;

		length = chunkNum.getBytes().length;
		if (length > Constants.CHUNKNUM_LENGTH)
			length = Constants.CHUNKNUM_LENGTH;
		ByteBuffer.memcpy(buffer, offset, chunkNum.getBytes(), 0, length);
		offset += Constants.CHUNKNUM_LENGTH;

		length = dataSize.getBytes().length;
		if (length > Constants.FILEDATASIZE_LENGTH)
			length = Constants.FILEDATASIZE_LENGTH;
		ByteBuffer.memcpy(buffer, offset, dataSize.getBytes(), 0, length);
		offset += Constants.FILEDATASIZE_LENGTH;

		length = offsetFile.getBytes().length;
		if (length > Constants.OFFSET_LENGTH)
			length = Constants.OFFSET_LENGTH;
		ByteBuffer.memcpy(buffer, offset, offsetFile.getBytes(), 0, length);
		offset += Constants.OFFSET_LENGTH;

		length = hostName.getBytes().length;
		if (length > Constants.HOSTNAME_LENGTH)
			length = Constants.HOSTNAME_LENGTH;
		ByteBuffer.memcpy(buffer, offset, hostName.getBytes(), 0, length);
		offset += Constants.HOSTNAME_LENGTH;

		length = ipAddress.getBytes().length;
		if (length > Constants.IPADDRESS_LENGTH)
			length = Constants.IPADDRESS_LENGTH;
		ByteBuffer.memcpy(buffer, offset, ipAddress.getBytes(), 0, length);
		offset += Constants.IPADDRESS_LENGTH;

		length = receiverName.getBytes().length;
		if (length > Constants.RECEIVER_LENGTH)
			length = Constants.RECEIVER_LENGTH;
		ByteBuffer.memcpy(buffer, offset, receiverName.getBytes(), 0, length);
		offset += Constants.RECEIVER_LENGTH;

		length = queueName.getBytes().length;
		if (length > Constants.QUEUENAME_LENGTH)
			length = Constants.QUEUENAME_LENGTH;
		ByteBuffer.memcpy(buffer, offset, queueName.getBytes(), 0, length);
		offset += Constants.QUEUENAME_LENGTH;

		length = tradeCode.getBytes().length;
		if (length > Constants.TRADECODE_LENGTH)
			length = Constants.TRADECODE_LENGTH;
		ByteBuffer.memcpy(buffer, offset, tradeCode.getBytes(), 0, length);
		offset += Constants.TRADECODE_LENGTH;

		return buffer;
	}

	/*
	 * 把传入的消息数据流 解析出来放入相应地成员变量中
	 */
	public void unPackMsgData(byte[] msgData) throws Exception {
		if (msgData.length != msgDataLen) {
			throw new Exception("长度不为:" + msgDataLen + ",消息数据格式不对！");
		}

		int offset = 0;
		segSize = new String(msgData, offset, Constants.SEGSIZE_LENGTH).trim();
		offset += Constants.SEGSIZE_LENGTH;

		dirName = new String(msgData, offset, Constants.DIRNAME_LENGTH).trim();
		offset += Constants.DIRNAME_LENGTH;

		fileName = new String(msgData, offset, Constants.FILENAME_LENGTH).trim();
		offset += Constants.FILENAME_LENGTH;

		chunkFileName = new String(msgData, offset, Constants.FILENAME_LENGTH).trim();
		offset += Constants.FILENAME_LENGTH;

		chunkCount = new String(msgData, offset, Constants.CHUNKCOUNT_LENGTH).trim();
		offset += Constants.CHUNKCOUNT_LENGTH;

		chunkNum = new String(msgData, offset, Constants.CHUNKNUM_LENGTH).trim();
		offset += Constants.CHUNKNUM_LENGTH;

		dataSize = new String(msgData, offset, Constants.FILEDATASIZE_LENGTH).trim();
		offset += Constants.FILEDATASIZE_LENGTH;

		offsetFile = new String(msgData, offset, Constants.OFFSET_LENGTH).trim();
		offset += Constants.OFFSET_LENGTH;

		hostName = new String(msgData, offset, Constants.HOSTNAME_LENGTH).trim();
		offset += Constants.HOSTNAME_LENGTH;

		ipAddress = new String(msgData, offset, Constants.IPADDRESS_LENGTH).trim();
		offset += Constants.IPADDRESS_LENGTH;

		receiverName = new String(msgData, offset, Constants.RECEIVER_LENGTH).trim();
		offset += Constants.RECEIVER_LENGTH;

		queueName = new String(msgData, offset, Constants.QUEUENAME_LENGTH).trim();
		offset += Constants.QUEUENAME_LENGTH;

		tradeCode = new String(msgData, offset, Constants.TRADECODE_LENGTH).trim();
		offset += Constants.TRADECODE_LENGTH;

	}
}
