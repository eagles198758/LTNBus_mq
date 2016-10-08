/**
 * 
 */
package com.sinux.mq.client.mod;

import com.sinux.mq.client.util.ByteBuffer;

/**
 * @author jingwen.tong
 *
 */
public class RecvFileControl {
	private byte buffer[] = null;//容纳数据的缓冲区
	private int msgDataLen  = Constants.SEGSIZE_LENGTH+Constants.DIRNAME_LENGTH+Constants.FILENAME_LENGTH
							+Constants.FILENAME_LENGTH+Constants.CHUNKCOUNT_LENGTH+Constants.CHUNKNUM_LENGTH
							+Constants.FILEDATASIZE_LENGTH+Constants.OFFSET_LENGTH+Constants.HOSTNAME_LENGTH
							+Constants.IPADDRESS_LENGTH+Constants.RECEIVER_LENGTH+Constants.QUEUENAME_LENGTH
							+Constants.TRADECODE_LENGTH+Constants.DESC_LENGTH+Constants.SENDEDDATASIZE_LENGTH+Constants.SENDEDSEGNUM_LENGTH+Constants.FINISHED_LENGTH
							+Constants.INTEGER_LENGTH+Constants.DIRNAME_LENGTH;
	
	/*
	 * 为何不采用set/get方法来设置成员值而采用把成员设置为PUBLIC，这个因为方法调用需要压栈\出栈，
	 * 损耗效率
	 */
	public String segSize =null;
	public String dirName = null;
	public String fileName = null;
	public String chunkFileName = null;
	public String chunkCount = null;
	public String chunkNum =null;
	public String dataSize = null;
	public String offsetFile = null;
	public String hostName =null;
	public String ipAddress = null;
	public String receiverName = null;
	public String queueName = null;
	public String tradeCode = null;
	public String desc = null;
	public String absoluteFileName = null;
	public long recvedDataSize =0;
	public long recvedSegNum = 0;
	public boolean isFinished = false;
	public int retVal =-1;//返回值
	public RecvFileControl()
	{
		buffer = new byte[msgDataLen];//预先分配缓冲区
		
	}

	
	
	public void free()
	{
		segSize =null;
		dirName = null;
		fileName = null;
		chunkFileName = null;
		chunkCount = null;
		chunkNum =null;
		dataSize = null;
	    offsetFile = null;
		hostName =null;
		ipAddress = null;
		receiverName = null;
		queueName = null;
		buffer = null;
		tradeCode = null;
		desc = null;
		recvedDataSize = 0;
		recvedSegNum = 0;
		isFinished = false;
		retVal =-1;
	}
	/*
	 * 根据成员变量组织消息数据包
	 */
	public byte[] packMsgData()throws Exception
	{
		
		int offset = 0;
		ByteBuffer.memset(buffer,(byte)' ');
		//
		int length = 0;
		
		length = segSize.getBytes().length;
		if(length>Constants.SEGSIZE_LENGTH)
			length = Constants.SEGSIZE_LENGTH;
		ByteBuffer.memcpy(buffer,offset,segSize.getBytes(),0,length);
		offset += Constants.SEGSIZE_LENGTH;
		
		length = dirName.getBytes().length;
		if(length>Constants.DIRNAME_LENGTH)
			length = Constants.DIRNAME_LENGTH;
		ByteBuffer.memcpy(buffer,offset,dirName.getBytes(),0,length);
		offset += Constants.DIRNAME_LENGTH;
		
		length = fileName.getBytes().length;
		if(length>Constants.FILENAME_LENGTH)
			length = Constants.FILENAME_LENGTH;
		ByteBuffer.memcpy(buffer,offset,fileName.getBytes(),0,length);
		offset += Constants.FILENAME_LENGTH;
		
		length = chunkFileName.getBytes().length;
		if(length>Constants.FILENAME_LENGTH)
			length = Constants.FILENAME_LENGTH;
		ByteBuffer.memcpy(buffer,offset,chunkFileName.getBytes(),0,length);
		offset += Constants.FILENAME_LENGTH;
		
		length = chunkCount.getBytes().length;
		if(length>Constants.CHUNKCOUNT_LENGTH)
			length = Constants.CHUNKCOUNT_LENGTH;
		ByteBuffer.memcpy(buffer,offset,chunkCount.getBytes(),0,length);
		offset += Constants.CHUNKCOUNT_LENGTH;
		
		length = chunkNum.getBytes().length;
		if(length>Constants.CHUNKNUM_LENGTH)
			length = Constants.CHUNKNUM_LENGTH;
		ByteBuffer.memcpy(buffer,offset,chunkNum.getBytes(),0,length);
		offset += Constants.CHUNKNUM_LENGTH;
		
		length = dataSize.getBytes().length;
		if(length>Constants.FILEDATASIZE_LENGTH)
			length = Constants.FILEDATASIZE_LENGTH;
		ByteBuffer.memcpy(buffer,offset,dataSize.getBytes(),0,length);
		offset += Constants.FILEDATASIZE_LENGTH;
		
		length = offsetFile.getBytes().length;
		if(length>Constants.OFFSET_LENGTH)
			length = Constants.OFFSET_LENGTH;
		ByteBuffer.memcpy(buffer,offset,offsetFile.getBytes(),0,length);
		offset += Constants.OFFSET_LENGTH;
		
		length = hostName.getBytes().length;
		if(length>Constants.HOSTNAME_LENGTH)
			length = Constants.HOSTNAME_LENGTH;
		ByteBuffer.memcpy(buffer,offset,hostName.getBytes(),0,length);
		offset += Constants.HOSTNAME_LENGTH;
		
		length = ipAddress.getBytes().length;
		if(length>Constants.IPADDRESS_LENGTH)
			length = Constants.IPADDRESS_LENGTH;
		ByteBuffer.memcpy(buffer,offset,ipAddress.getBytes(),0,length);
		offset += Constants.IPADDRESS_LENGTH;
		
		length = receiverName.getBytes().length;
		if(length>Constants.RECEIVER_LENGTH)
			length = Constants.RECEIVER_LENGTH;
		ByteBuffer.memcpy(buffer,offset,receiverName.getBytes(),0,length);
		offset += Constants.RECEIVER_LENGTH;
		
		length = queueName.getBytes().length;
		if(length>Constants.QUEUENAME_LENGTH)
			length = Constants.QUEUENAME_LENGTH;
		ByteBuffer.memcpy(buffer,offset,queueName.getBytes(),0,length);
		offset += Constants.QUEUENAME_LENGTH;
		

		length = tradeCode.getBytes().length;
		if(length>Constants.TRADECODE_LENGTH)
			length = Constants.TRADECODE_LENGTH;
		ByteBuffer.memcpy(buffer,offset,tradeCode.getBytes(),0,length);
		offset += Constants.TRADECODE_LENGTH;
		
		

		length = desc.getBytes().length;
		if(length>Constants.DESC_LENGTH)
			length = Constants.DESC_LENGTH;
		ByteBuffer.memcpy(buffer,offset,desc.getBytes(),0,length);
		offset += Constants.DESC_LENGTH;
		
		length = Long.toString(recvedDataSize).getBytes().length;
		if(length>Constants.SENDEDDATASIZE_LENGTH)
			length = Constants.SENDEDDATASIZE_LENGTH;
		ByteBuffer.memcpy(buffer,offset,Long.toString(recvedDataSize).getBytes(),0,length);
		offset += Constants.SENDEDDATASIZE_LENGTH;
		
		length = Long.toString(recvedSegNum).getBytes().length;
		if(length>Constants.SENDEDSEGNUM_LENGTH)
			length = Constants.SENDEDSEGNUM_LENGTH;
		ByteBuffer.memcpy(buffer,offset,Long.toString(recvedSegNum).getBytes(),0,length);
		offset += Constants.SENDEDSEGNUM_LENGTH;
		
		String tempFinish = null;
		if(isFinished)
			tempFinish = "1";
		else
			tempFinish = "0";

		length = tempFinish.getBytes().length;
		if(length>Constants.FINISHED_LENGTH)
			length = Constants.FINISHED_LENGTH;
		ByteBuffer.memcpy(buffer,offset,tempFinish.getBytes(),0,length);
		offset += Constants.FINISHED_LENGTH;
		
		length = Integer.toString(retVal).getBytes().length;
		if(length>Constants.INTEGER_LENGTH)
			length = Constants.INTEGER_LENGTH;
		ByteBuffer.memcpy(buffer,offset,Integer.toString(retVal).getBytes(),0,length);
		offset += Constants.INTEGER_LENGTH;
		

		length = absoluteFileName.getBytes().length;
		if(length>Constants.DIRNAME_LENGTH)
			length = Constants.DIRNAME_LENGTH;
		ByteBuffer.memcpy(buffer,offset,absoluteFileName.getBytes(),0,length);
		offset += Constants.DIRNAME_LENGTH;
		
		return buffer;
	}
	/*
	 * 把传入的消息数据流 解析出来放入相应地成员变量中
	 */
	public void unPackMsgData(byte[] msgData)throws Exception
	{
		if(msgData.length!=msgDataLen)
		{
			throw new Exception("长度不为:"+msgDataLen+",消息数据格式不对！");
		}
		
		int offset = 0;
		segSize = new String(msgData,offset,Constants.SEGSIZE_LENGTH).trim();
		offset += Constants.SEGSIZE_LENGTH;
		
		dirName = new String(msgData,offset,Constants.DIRNAME_LENGTH).trim();
		offset +=Constants.DIRNAME_LENGTH;
		
		fileName = new String(msgData,offset,Constants.FILENAME_LENGTH).trim();
		offset +=Constants.FILENAME_LENGTH;
		
		chunkFileName = new String(msgData,offset,Constants.FILENAME_LENGTH).trim();
		offset +=Constants.FILENAME_LENGTH;
		
		chunkCount = new String(msgData,offset,Constants.CHUNKCOUNT_LENGTH).trim();
		offset +=Constants.CHUNKCOUNT_LENGTH;
		
		chunkNum = new String(msgData,offset,Constants.CHUNKNUM_LENGTH).trim();
		offset +=Constants.CHUNKNUM_LENGTH;
		

		
		dataSize = new String(msgData,offset,Constants.FILEDATASIZE_LENGTH).trim();
		offset +=Constants.FILEDATASIZE_LENGTH;
		
		offsetFile = new String(msgData,offset,Constants.OFFSET_LENGTH).trim();
		offset +=Constants.OFFSET_LENGTH;
		
		hostName = new String(msgData,offset,Constants.HOSTNAME_LENGTH).trim();
		offset +=Constants.HOSTNAME_LENGTH;
		
		ipAddress = new String(msgData,offset,Constants.IPADDRESS_LENGTH).trim();
		offset +=Constants.IPADDRESS_LENGTH;
		
		receiverName = new String(msgData,offset,Constants.RECEIVER_LENGTH).trim();
		offset +=Constants.RECEIVER_LENGTH;
		
		queueName = new String(msgData,offset,Constants.QUEUENAME_LENGTH).trim();
		offset +=Constants.QUEUENAME_LENGTH;
		
		tradeCode = new String(msgData,offset,Constants.TRADECODE_LENGTH).trim();
		offset +=Constants.TRADECODE_LENGTH;
		
		desc = new String(msgData,offset,Constants.DESC_LENGTH).trim();
		offset +=Constants.DESC_LENGTH;
		

		recvedDataSize = Long.parseLong(new String(msgData,offset,Constants.SENDEDDATASIZE_LENGTH).trim());
		offset +=Constants.SENDEDDATASIZE_LENGTH;
		
		recvedSegNum = Integer.parseInt(new String(msgData,offset,Constants.SENDEDSEGNUM_LENGTH).trim());
		offset +=Constants.SENDEDSEGNUM_LENGTH;
		
		String  tempFinish= new String(msgData,offset,Constants.FINISHED_LENGTH).trim();
		if(tempFinish.compareToIgnoreCase("1")==0)
			isFinished = true;
		else
			isFinished = false;
		offset +=Constants.FINISHED_LENGTH;
		
		retVal = Integer.parseInt(new String(msgData,offset,Constants.INTEGER_LENGTH).trim());
		offset +=Constants.INTEGER_LENGTH;
		
		absoluteFileName =(new String(msgData,offset,Constants.DIRNAME_LENGTH).trim());
		offset +=Constants.DIRNAME_LENGTH;
		
	}
}
