package com.sinux.mq.client.mod;

import com.sinux.mq.client.util.ByteBuffer;

public class SendFileControl {

	private byte buffer[] = null;// 容纳数据的缓冲区
	private int msgDataLen = Constants.SEGSIZE_LENGTH + Constants.FILENAME_LENGTH + Constants.CHUNKCOUNT_LENGTH
			+ Constants.CHUNKNUM_LENGTH + Constants.FILEDATASIZE_LENGTH + Constants.OFFSET_LENGTH
			+ Constants.SENDEDDATASIZE_LENGTH + Constants.SENDEDSEGNUM_LENGTH + Constants.FINISHED_LENGTH
			+ Constants.INTEGER_LENGTH;

	/*
	 * 为何不采用set/get方法来设置成员值而采用把成员设置为PUBLIC，这个因为方法调用需要压栈\出栈， 损耗效率
	 */
	public String segSize = null;

	public String chunkFileName = null;
	public String chunkCount = null;
	public String chunkNum = null;
	public String dataSize = null;
	public String offsetFile = null;
	public long sendedDataSize = 0;
	public long sendedSegNum = 0;
	public boolean isFinished = false;
	public int retVal = -1;// 返回值

	public SendFileControl() {
		buffer = new byte[msgDataLen];// 预先分配缓冲区

	}

	public void free() {
		segSize = null;

		chunkFileName = null;
		chunkCount = null;
		chunkNum = null;
		dataSize = null;
		offsetFile = null;
		sendedSegNum = 0;
		buffer = null;
		sendedDataSize = 0;
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

		length = Long.toString(sendedDataSize).getBytes().length;
		if (length > Constants.SENDEDDATASIZE_LENGTH)
			length = Constants.SENDEDDATASIZE_LENGTH;
		ByteBuffer.memcpy(buffer, offset, Long.toString(sendedDataSize).getBytes(), 0, length);
		offset += Constants.SENDEDDATASIZE_LENGTH;

		length = Long.toString(sendedSegNum).getBytes().length;
		if (length > Constants.SENDEDSEGNUM_LENGTH)
			length = Constants.SENDEDSEGNUM_LENGTH;
		ByteBuffer.memcpy(buffer, offset, Long.toString(sendedSegNum).getBytes(), 0, length);
		offset += Constants.SENDEDSEGNUM_LENGTH;

		String tempFinish = null;
		if (isFinished)
			tempFinish = "1";
		else
			tempFinish = "0";

		length = tempFinish.getBytes().length;
		if (length > Constants.FINISHED_LENGTH)
			length = Constants.FINISHED_LENGTH;
		ByteBuffer.memcpy(buffer, offset, tempFinish.getBytes(), 0, length);
		offset += Constants.FINISHED_LENGTH;

		length = Integer.toString(retVal).getBytes().length;
		if (length > Constants.INTEGER_LENGTH)
			length = Constants.INTEGER_LENGTH;
		ByteBuffer.memcpy(buffer, offset, Integer.toString(retVal).getBytes(), 0, length);
		offset += Constants.INTEGER_LENGTH;
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

		sendedDataSize = Long.parseLong(new String(msgData, offset, Constants.SENDEDDATASIZE_LENGTH).trim());
		offset += Constants.SENDEDDATASIZE_LENGTH;

		sendedSegNum = Integer.parseInt(new String(msgData, offset, Constants.SENDEDSEGNUM_LENGTH).trim());
		offset += Constants.SENDEDSEGNUM_LENGTH;

		String tempFinish = new String(msgData, offset, Constants.FINISHED_LENGTH).trim();
		if (tempFinish.compareToIgnoreCase("1") == 0)
			isFinished = true;
		else
			isFinished = false;
		offset += Constants.FINISHED_LENGTH;

		retVal = Integer.parseInt(new String(msgData, offset, Constants.INTEGER_LENGTH).trim());
		offset += Constants.INTEGER_LENGTH;
	}
}
