package com.sinux.mq.client.file.services;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.QueueingConsumer;
import com.sinux.mq.client.MqChannel;
import com.sinux.mq.client.MqConnectionFactory;
import com.sinux.mq.client.MqConsumer;
import com.sinux.mq.client.mod.Constants;
import com.sinux.mq.client.mod.FileTransControlMsg;
import com.sinux.mq.client.mod.MsgDes;
import com.sinux.mq.client.mod.RecvFileControl;
import com.sinux.mq.client.mod.SendFileControl;
import com.sinux.mq.client.mod.TransInfo;
import com.sinux.mq.client.util.ByteBuffer;

/**
 * @author jingwen.tong 2006-12-18 Copyright IBM 2005 利用MQ消息完成断点上传的实现类
 */
@SuppressWarnings({ "rawtypes", "unused" })
public class MqTranSendService {

	private FileTransControlMsg fileTransControlMsg = null;
	private TransInfo transInfo = null;
	private byte[] msgid = null;
	private String receiver = null;
	public Map hSendFileControl = null;
	public Object synObjectTrans = null;

	private MqConnectionFactory factory = null;
	private MqChannelPool channelPool = null;

	public MqTranSendService(byte[] msgid, FileTransControlMsg fileTransControlMsg, TransInfo transInfo,
			Map hSendFileControl, Object synObjectTrans, MqConnectionFactory factory) {
		this.fileTransControlMsg = fileTransControlMsg;
		this.transInfo = transInfo;
		this.msgid = msgid;
		this.receiver = fileTransControlMsg.receiverName;
		this.hSendFileControl = hSendFileControl;
		this.synObjectTrans = synObjectTrans;
		this.factory = factory;
		this.channelPool = MqChannelPool.getSingleInstance(factory);
	}

	/*
	 * 0:成功 2:网络出现问题
	 */
	private int sendFileTransControlMsg(MqChannel ctrlChannel) {
		int iRetVal = -1;
		ctrlChannel = channelPool.getMqChannel(ctrlChannel);
		BasicProperties.Builder builder = new BasicProperties.Builder();
		builder.messageId(new String(msgid));
		try {
			ctrlChannel.getChannel().basicPublish("", ctrlChannel.getQueueName(), builder.build(),
					fileTransControlMsg.packMsgData());
			iRetVal = 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return iRetVal;
	}

	// 判断fileTransControlMsg对应的消息是否在"+GlobalVar.fileTransControlQueueName+".FINISH队列中存在
	private boolean isExistFileTransControlFinishMsg(MqChannel feedbackChannel) {
		boolean bRetVal = false;
		feedbackChannel = channelPool.getMqChannel(feedbackChannel);
		try {
			GetResponse response = feedbackChannel.getChannel().basicGet(feedbackChannel.getQueueName(), true);
			if(response != null){
				RecvFileControl recvFileControl = new RecvFileControl();
				recvFileControl.unPackMsgData(response.getBody());
				System.out.println("成功收取文件" + recvFileControl.absoluteFileName + "中的" + recvFileControl.chunkFileName + "包");
				bRetVal = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bRetVal;
	}

	// 判断fileTransControlMsg对应的消息是否在"+GlobalVar.fileTransControlQueueName+"队列中存在
	private boolean isExistFileTransControlMsg(MqChannel ctrlChannel) {
		return channelPool.isExistMqChannel(ctrlChannel);
	}

	/*
	 * 开始发送fileTransControlMsg对应的文件数据信息
	 * 
	 * 返回值定义: -1:其它错误 0:成功 1:无法取得与MQ队列管理器的连接 2:与MQ队列管理器的连接无效，即出现网络失效错误
	 */
	@SuppressWarnings("unchecked")
	public int sendFile() {
		int iRetVal = 0;
		MqChannel dataChannel = new MqChannel();
		MqChannel ctrlChannel = new MqChannel();
		MqChannel feedbackChannel = new MqChannel();
		SendFileControl sendFileControl = null;
		RandomAccessFile readAccess1 = null, writeAccess1 = null;
		File contorlMsgPersistFile = null;
		long offsetFile = 0;
		long seekOffset = 0;
		int sendDataLength = 0;// 每次需要发送的数据长度
		long segNumber = 0;
		try {
			// 1.判断以前传送了多少
			// 判断内存中是否已经存在了相应地sendFileControl,如果内存中不存在的话将从持久化到文件系统中的文件读取相应的内容
			boolean flag = false;

			if (GlobalVar.isControlMsgPersist) {
				contorlMsgPersistFile = new File(GlobalVar.tempPath + "/send/" + ByteBuffer.ByteToHex(msgid));
				if (contorlMsgPersistFile.exists()) {
					sendFileControl = new SendFileControl();
					sendFileControl.chunkCount = fileTransControlMsg.chunkCount;
					sendFileControl.chunkFileName = fileTransControlMsg.chunkFileName;
					sendFileControl.chunkNum = fileTransControlMsg.chunkNum;
					sendFileControl.dataSize = fileTransControlMsg.dataSize;
					sendFileControl.offsetFile = fileTransControlMsg.offsetFile;
					sendFileControl.segSize = fileTransControlMsg.segSize;

					// 此文件存在并且读取此文件的内容
					readAccess1 = new RandomAccessFile(contorlMsgPersistFile, "r");
					byte buffer[] = new byte[(int) contorlMsgPersistFile.length()];
					readAccess1.readFully(buffer);
					readAccess1.close();
					readAccess1 = null;
					// 解析出此文件的内容到相应的字段
					sendFileControl.unPackMsgData(buffer);
					synchronized (synObjectTrans) {
						hSendFileControl.put(msgid, sendFileControl);
					}
					flag = true;
				}
				contorlMsgPersistFile = null;
			}
			if (!flag) {
				synchronized (synObjectTrans) {
					sendFileControl = (SendFileControl) hSendFileControl.get(msgid);
					if (sendFileControl == null) {
						sendFileControl = new SendFileControl();
						sendFileControl.chunkCount = fileTransControlMsg.chunkCount;
						sendFileControl.chunkFileName = fileTransControlMsg.chunkFileName;
						sendFileControl.chunkNum = fileTransControlMsg.chunkNum;
						sendFileControl.dataSize = fileTransControlMsg.dataSize;
						sendFileControl.offsetFile = fileTransControlMsg.offsetFile;
						sendFileControl.segSize = fileTransControlMsg.segSize;
						hSendFileControl.put(msgid, sendFileControl);
					}
				}
			}

			if (sendFileControl.isFinished) {// 已经传输完毕了.不需要再传输了
				transInfo.setTotalSendedSize(sendFileControl.sendedDataSize);
				transInfo.setTotalFinishedSize(sendFileControl.sendedDataSize);

				// 删除此文件
				contorlMsgPersistFile = new File(GlobalVar.tempPath + "/send/" + ByteBuffer.ByteToHex(msgid)); // String(msgid,"ISO-8859-1")));
				contorlMsgPersistFile.delete();
				return iRetVal;
			}
			fileTransControlMsg.segSize = sendFileControl.segSize;
			// 2.取得与MQ的连接
			dataChannel.setQueueName(fileTransControlMsg.chunkFileName);
			dataChannel = channelPool.getMqChannel(dataChannel);

			ctrlChannel.setQueueName(fileTransControlMsg.fileName + GlobalVar.fileTransControlQueueName);

			feedbackChannel.setQueueName(fileTransControlMsg.fileName + GlobalVar.fileTransControlFinishQueueName);

			// 判断fileTransControlMsg对应的消息是否在"+GlobalVar.fileTransControlQueueName+"队列中存在
			if (isExistFileTransControlMsg(ctrlChannel)) {
				iRetVal = sendFileTransControlMsg(ctrlChannel);
				if (iRetVal != 0) {
					return iRetVal;
				}
				// if (isExistFileTransControlFinishMsg(feedbackChannel)) {//
				// 存在的话，则表示文件已经传输完毕了
				// iRetVal = 0;
				// return iRetVal;
				// }
				if (Long.parseLong(fileTransControlMsg.dataSize) == sendFileControl.sendedDataSize) {
					transInfo.setTotalSendedSize(sendFileControl.sendedDataSize);
					transInfo.setTotalFinishedSize(sendFileControl.sendedDataSize);
				}
			} else {
				if (GlobalVar.isMsgPersist) {
					// 如果不存在的话
					if (Long.parseLong(fileTransControlMsg.dataSize) == sendFileControl.sendedDataSize) {
						// 这个只有在启用消息持久性才有效
						// 这个表明，出现了这样的一种情况就是当发送文件已经传输完毕的消息到MQ中已经成功的打入到队列中，但是返回给客户端的
						// 返回码由于网络中断问题，导致客户端认为失效，而接收程序把所有有关此文件的数据消息都接收处理掉了
						// 如果出现这样的情况，我们需要认为它已经成功
						transInfo.setTotalFinishedSize(sendFileControl.sendedDataSize);
						return iRetVal;
					}
				}
				sendFileControl.sendedDataSize = 0;
				sendFileControl.sendedSegNum = 0;

				// 发送文件传输控制消息到"+GlobalVar.fileTransControlQueueName+"队列中
				iRetVal = sendFileTransControlMsg(ctrlChannel);
				if (iRetVal != 0) {
					return iRetVal;
				}
			}
			transInfo.setTotalSendedSize(sendFileControl.sendedDataSize);
			// 开始需要从失败的那点开始进行传输了
			// 可能会使用原来的segSize大小，主要是避免用户可能会修改原来配置文件中的segSize的大小
			int segSize = Integer.parseInt(fileTransControlMsg.segSize);
			long transFileDataSize = Long.parseLong(fileTransControlMsg.dataSize);// 要传输的文件的数据大小
			// 取得已经传输的文件大小
			offsetFile = Long.parseLong(fileTransControlMsg.offsetFile);

			sendDataLength = 0;// 每次需要发送的数据长度
			File sendFile = new File(fileTransControlMsg.dirName + "/" + fileTransControlMsg.fileName);
			readAccess1 = new RandomAccessFile(sendFile, "r");
			if (GlobalVar.isControlMsgPersist) {
				contorlMsgPersistFile = new File(GlobalVar.tempPath + "/send/" + ByteBuffer.ByteToHex(msgid));
				writeAccess1 = new RandomAccessFile(contorlMsgPersistFile, "rw");
			}
			byte sendBuffer[] = new byte[segSize];

			seekOffset = offsetFile + sendFileControl.sendedDataSize;
			long startTime;

			segNumber = sendFileControl.sendedSegNum;
			byte msgDataSize[] = new byte[Constants.MSGDATASIZE_LENGTH];
			byte fileOffset[] = new byte[Constants.OFFSET_LENGTH];

			BasicProperties.Builder builder = new BasicProperties.Builder();
			Map<String, Object> headers = new HashMap<String, Object>();
			while (sendFileControl.sendedDataSize != transFileDataSize) {// 循环发送数据

				sendDataLength = (int) (transFileDataSize - sendFileControl.sendedDataSize);
				if (sendDataLength > segSize)
					sendDataLength = segSize;

				readAccess1.seek(seekOffset);

				if (readAccess1.read(sendBuffer, 0, sendDataLength) == -1) {// 没有读到数据，说明文件已经读完了
					break;
				}
				segNumber++;
				ByteBuffer.memset(msgDataSize, (byte) ' ');
				ByteBuffer.memset(fileOffset, (byte) ' ');
				int length = Long.toString(sendDataLength).getBytes().length;
				if (length > Constants.MSGDATASIZE_LENGTH) {
					length = Constants.MSGDATASIZE_LENGTH;
				}
				ByteBuffer.memcpy(msgDataSize, 0, Long.toString(sendDataLength).getBytes(), 0, length);

				length = Long.toString(seekOffset - offsetFile).getBytes().length;
				if (length > Constants.OFFSET_LENGTH) {
					length = Constants.OFFSET_LENGTH;
				}
				ByteBuffer.memcpy(fileOffset, 0, Long.toString(seekOffset - offsetFile).getBytes(), 0, length);
				seekOffset += sendDataLength;
				// 把这个数据当中MQ消息数据区中的数据发送出去
				startTime = System.currentTimeMillis();

				headers.put("fileOffset", fileOffset);
				headers.put("msgDataSize", msgDataSize);
				builder.headers(headers);
				dataChannel.getChannel().basicPublish("", dataChannel.getQueueName(), builder.build(), sendBuffer);

				transInfo.setTotalSendedTime(System.currentTimeMillis() - startTime);
				// 发送成功
				transInfo.setTotalSendedSize(sendDataLength);
				sendFileControl.sendedSegNum = (int) segNumber;
				sendFileControl.sendedDataSize += sendDataLength;

				if (GlobalVar.isControlMsgPersist) {
					// 把此信息持久化到文件系统中
					writeAccess1.seek(0);
					writeAccess1.write(sendFileControl.packMsgData());
				}
				headers.clear();
			}

			// 发送文件已经传输完毕的消息到MQ中成功
			if (iRetVal == 0) {
				sendFileControl.isFinished = true;
				if (GlobalVar.isControlMsgPersist) {
					// 把此信息持久化到文件系统中
					writeAccess1.seek(0);
					writeAccess1.write(sendFileControl.packMsgData());
				}
			}
			
			// 如果是与队列管理器连接的错误，则需要清理连接池中无效的连接
			if (iRetVal == 2) {
				channelPool.destoryAllChannel();
			}
			while(!isExistFileTransControlFinishMsg(feedbackChannel)){
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			iRetVal = -1;
			if (GlobalVar.isControlMsgPersist) {
				if (writeAccess1 != null) {
					try {
						// 把此信息持久化到文件系统中
						writeAccess1.seek(0);
						writeAccess1.write(sendFileControl.packMsgData());
					} catch (Exception exc1) {

					}
				}
			}
		} finally {
			try {
				if (readAccess1 != null) {
					try {
						readAccess1.close();
					} catch (Exception exc) {

					}
				}
				if (writeAccess1 != null) {
					try {

						writeAccess1.close();
					} catch (Exception exc) {

					}
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			channelPool.freeChannel(dataChannel);
			channelPool.freeChannel(ctrlChannel);
			channelPool.freeChannel(feedbackChannel);
		}
		return iRetVal;
	}
}
