package com.sinux.mq.client.file.services;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.sinux.mq.client.MqChannel;
import com.sinux.mq.client.MqConnectionFactory;
import com.sinux.mq.client.MqConsumer;
import com.sinux.mq.client.mod.Constants;
import com.sinux.mq.client.mod.FileTransControlMsg;
import com.sinux.mq.client.mod.MsgDes;
import com.sinux.mq.client.mod.SendFileControl;
import com.sinux.mq.client.mod.TransInfo;
import com.sinux.mq.client.util.ByteBuffer;

/**
 * @author jingwen.tong 2006-12-18 Copyright IBM 2005 ����MQ��Ϣ��ɶϵ��ϴ���ʵ����
 */
@SuppressWarnings({"rawtypes","unused"})
public class MqTranSendService {

	private FileTransControlMsg fileTransControlMsg = null;
	private TransInfo transInfo = null;
	private byte[] msgid = null;
	private String receiver = null;
	public Map hSendFileControl = null;
	public Object synObjectTrans = null;

	private MqConnectionFactory factory = null;

	public MqTranSendService(byte[] msgid, FileTransControlMsg fileTransControlMsg, TransInfo transInfo,
			Map hSendFileControl, Object synObjectTrans, MqConnectionFactory factory) {
		this.fileTransControlMsg = fileTransControlMsg;
		this.transInfo = transInfo;
		this.msgid = msgid;
		this.receiver = fileTransControlMsg.receiverName;
		this.hSendFileControl = hSendFileControl;
		this.synObjectTrans = synObjectTrans;
		this.factory = factory;
	}

	/*
	 * 0:�ɹ� 2:�����������
	 */
	private int sendFileTransControlMsg(MqChannel ctrlChannel) {
		int iRetVal = -1;
		MqChannelPool channelPool = MqChannelPool.getSingleInstance(factory);
		ctrlChannel = channelPool.getMqChannel(ctrlChannel);
		try {
			factory.putData(new MsgDes(), fileTransControlMsg.packMsgData(), ctrlChannel);
			iRetVal = 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return iRetVal;
	}

	// �ж�fileTransControlMsg��Ӧ����Ϣ�Ƿ���"+GlobalVar.fileTransControlQueueName+".FINISH�����д���
	private boolean isExistFileTransControlFinishMsg(MqChannel feedbackChannel) {
		boolean bRetVal = false;
		MqChannelPool channelPool = MqChannelPool.getSingleInstance(factory);
		if(!channelPool.isExistMqChannel(feedbackChannel)){
			feedbackChannel = channelPool.getMqChannel(feedbackChannel);
		}
		MqConsumer consumer = new MqConsumer(12, feedbackChannel) {
			
			@Override
			public void handleDeliver(byte[] body, byte[] msgDes) {
				String bodyStr = new String(body);
				if(body.equals("OK")){
					System.out.println(bodyStr);
					return;
				}
			}
		};
		factory.getData(consumer);
		return bRetVal;
	}

	// �ж�fileTransControlMsg��Ӧ����Ϣ�Ƿ���"+GlobalVar.fileTransControlQueueName+"�����д���
	private boolean isExistFileTransControlMsg(MqChannel ctrlChannel) {
		MqChannelPool channelPool = MqChannelPool.getSingleInstance(factory);
		return channelPool.isExistMqChannel(ctrlChannel);
	}

	// �����ļ��Ѿ�������ϵ���Ϣ��MQ�гɹ����Դ������սӿڴ����Կ�ʼ�����ļ�
	private int sendFileTransFinishedMsg(MqChannel ctrlChannel) {
		int iRetVal = -1;
		MqChannelPool channelPool = MqChannelPool.getSingleInstance(factory);
		try {
			File sendFile = new File(fileTransControlMsg.dirName + "/" + fileTransControlMsg.fileName);
			long fileLength = 0;
			if (sendFile.exists()) {
				fileLength = sendFile.length();
			}
			// ��Ϣ�����һЩ������Ϣ
			byte chunkCount[] = new byte[Constants.CHUNKCOUNT_LENGTH];
			byte chunkNum[] = new byte[Constants.CHUNKNUM_LENGTH];
			byte tradeCode[] = new byte[Constants.TRADECODE_LENGTH];
			byte fileLength1[] = new byte[Constants.SENDEDDATASIZE_LENGTH];
			ByteBuffer.memset(tradeCode, (byte) ' ');
			ByteBuffer.memset(chunkCount, (byte) ' ');
			ByteBuffer.memset(chunkNum, (byte) ' ');
			ByteBuffer.memset(fileLength1, (byte) ' ');
			ByteBuffer.memcpy(chunkCount, 0, fileTransControlMsg.chunkCount.getBytes(), 0,
					fileTransControlMsg.chunkCount.getBytes().length);
			ByteBuffer.memcpy(chunkNum, 0, fileTransControlMsg.chunkNum.getBytes(), 0,
					fileTransControlMsg.chunkNum.getBytes().length);
			ByteBuffer.memcpy(tradeCode, 0, fileTransControlMsg.tradeCode.getBytes(), 0,
					fileTransControlMsg.tradeCode.getBytes().length);
			ByteBuffer.memcpy(fileLength1, 0, Long.toString(fileLength).getBytes(), 0,
					Long.toString(fileLength).getBytes().length);

			byte desc[] = new byte[Constants.DESC_LENGTH];
			ByteBuffer.memset(desc, (byte) ' ');
			String desc1 = fileTransControlMsg.hostName + ":" + fileTransControlMsg.ipAddress + ":"
					+ fileTransControlMsg.fileName;
			ByteBuffer.memcpy(desc, 0, desc1.getBytes(), 0, desc1.getBytes().length);
			
			ctrlChannel = channelPool.getMqChannel(ctrlChannel);
			factory.putData(new MsgDes(), desc, ctrlChannel);
			iRetVal = 0;
		} catch (Exception exc) {
			iRetVal = 2;
			channelPool.destoryAllChannel();
		}
		return iRetVal;
	}

	/*
	 * ��ʼ����fileTransControlMsg��Ӧ���ļ�������Ϣ
	 * 
	 * ����ֵ����: -1:�������� 0:�ɹ� 1:�޷�ȡ����MQ���й����������� 2:��MQ���й�������������Ч������������ʧЧ����
	 */
	@SuppressWarnings("unchecked")
	public int sendFile() {
		int iRetVal = 0;
		MqChannelPool channelPool = MqChannelPool.getSingleInstance(factory);
		MqChannel dataChannel = new MqChannel();
		MqChannel ctrlChannel = new MqChannel();
		MqChannel feedbackChannel = new MqChannel();
		SendFileControl sendFileControl = null;
		RandomAccessFile readAccess1 = null, writeAccess1 = null;
		File contorlMsgPersistFile = null;
		long offsetFile = 0;
		long seekOffset = 0;
		int sendDataLength = 0;// ÿ����Ҫ���͵����ݳ���
		long segNumber = 0;
		try {
			// 1.�ж���ǰ�����˶���
			// �ж��ڴ����Ƿ��Ѿ���������Ӧ��sendFileControl,����ڴ��в����ڵĻ����ӳ־û����ļ�ϵͳ�е��ļ���ȡ��Ӧ������
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

					// ���ļ����ڲ��Ҷ�ȡ���ļ�������
					readAccess1 = new RandomAccessFile(contorlMsgPersistFile, "r");
					byte buffer[] = new byte[(int) contorlMsgPersistFile.length()];
					readAccess1.readFully(buffer);
					readAccess1.close();
					readAccess1 = null;
					// ���������ļ������ݵ���Ӧ���ֶ�
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

			if (sendFileControl.isFinished) {// �Ѿ����������.����Ҫ�ٴ�����
				transInfo.setTotalSendedSize(sendFileControl.sendedDataSize);
				transInfo.setTotalFinishedSize(sendFileControl.sendedDataSize);

				// ɾ�����ļ�
				contorlMsgPersistFile = new File(GlobalVar.tempPath + "/send/" + ByteBuffer.ByteToHex(msgid));					// String(msgid,"ISO-8859-1")));
				contorlMsgPersistFile.delete();
				return iRetVal;
			}
			fileTransControlMsg.segSize = sendFileControl.segSize;
			// 2.ȡ����MQ������
			dataChannel.setQueueName(fileTransControlMsg.fileName);
			dataChannel = channelPool.getMqChannel(dataChannel);
			
			ctrlChannel.setQueueName(fileTransControlMsg.fileName + GlobalVar.fileTransControlQueueName);
			
			feedbackChannel.setQueueName(fileTransControlMsg.fileName + GlobalVar.fileTransControlFinishQueueName);
			
			if (dataChannel == null) {
				iRetVal = 1;
				return iRetVal;
			}
			// �ж�fileTransControlMsg��Ӧ����Ϣ�Ƿ���"+GlobalVar.fileTransControlQueueName+"�����д���
			if (isExistFileTransControlMsg(ctrlChannel)) {
//				iRetVal = sendFileTransControlMsg(ctrlChannel);
//				if (iRetVal != 0) {
//					return iRetVal;
//				}
				if (isExistFileTransControlFinishMsg(feedbackChannel)) {// ���ڵĻ������ʾ�ļ��Ѿ���������� 
					iRetVal = 0;
					return iRetVal;
				}
				if (Long.parseLong(fileTransControlMsg.dataSize) == sendFileControl.sendedDataSize) {
					transInfo.setTotalSendedSize(sendFileControl.sendedDataSize);
					transInfo.setTotalFinishedSize(sendFileControl.sendedDataSize);
					// ������������Ѿ������꣬��Ҫ����һ���ļ��Ѿ�������ϵ���Ϣ���������սӿڽ��н���
					// ���÷����ļ��Ѿ�������ϵ���Ϣ��MQ��
					// �����ļ��Ѿ�������ϵ���Ϣ��MQ�гɹ�
					int transStatus = sendFileTransFinishedMsg(ctrlChannel);
					if (transStatus == 0) {
						sendFileControl.retVal = 0;
						return iRetVal;
					} else {
						sendFileControl.retVal = transStatus;
						// ���������й��������ӵĴ�������Ҫ�������ӳ�����Ч������
						if (transStatus == 2) {
							channelPool.destoryAllChannel();
						}
						return transStatus;
					}
				}
			} else {
				if (GlobalVar.isMsgPersist) {
					// ��������ڵĻ�
					if (Long.parseLong(fileTransControlMsg.dataSize) == sendFileControl.sendedDataSize) {
						// ���ֻ����������Ϣ�־��Բ���Ч
						// ���������������������һ��������ǵ������ļ��Ѿ�������ϵ���Ϣ��MQ���Ѿ��ɹ��Ĵ��뵽�����У����Ƿ��ظ��ͻ��˵�
						// ���������������ж����⣬���¿ͻ�����ΪʧЧ�������ճ���������йش��ļ���������Ϣ�����մ������
						// ������������������������Ҫ��Ϊ���Ѿ��ɹ�
						transInfo.setTotalFinishedSize(sendFileControl.sendedDataSize);
						return iRetVal;
					}
				}
				sendFileControl.sendedDataSize = 0;
				sendFileControl.sendedSegNum = 0;

				// �����ļ����������Ϣ��"+GlobalVar.fileTransControlQueueName+"������
				iRetVal = sendFileTransControlMsg(ctrlChannel);
				if (iRetVal != 0)
					return iRetVal;
			}
			transInfo.setTotalSendedSize(sendFileControl.sendedDataSize);
			// ��ʼ��Ҫ��ʧ�ܵ��ǵ㿪ʼ���д�����
			// ���ܻ�ʹ��ԭ����segSize��С����Ҫ�Ǳ����û����ܻ��޸�ԭ�������ļ��е�segSize�Ĵ�С
			int segSize = Integer.parseInt(fileTransControlMsg.segSize);
			long transFileDataSize = Long.parseLong(fileTransControlMsg.dataSize);// Ҫ������ļ������ݴ�С
			// ȡ���Ѿ�������ļ���С
			offsetFile = Long.parseLong(fileTransControlMsg.offsetFile);

			sendDataLength = 0;// ÿ����Ҫ���͵����ݳ���
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

			while (sendFileControl.sendedDataSize != transFileDataSize) {// ѭ����������
																			// if
																			// (GlobalVar.isMsgPersist)
																			// message.persistence
																			// =
																			// MQC.MQPER_PERSISTENT;
																			// else
																			// message.persistence
																			// =
																			// MQC.MQPER_NOT_PERSISTENT;
																			// message.messageType
																			// =
																			// MQC.MQMT_DATAGRAM;
																			// message.format
																			// =
																			// MQC.MQFMT_NONE;
																			// message.messageId
																			// =
																			// msgid;

				sendDataLength = (int) (transFileDataSize - sendFileControl.sendedDataSize);
				if (sendDataLength > segSize)
					sendDataLength = segSize;

				readAccess1.seek(seekOffset);

				if (readAccess1.read(sendBuffer, 0, sendDataLength) == -1) {// û�ж������ݣ�˵���ļ��Ѿ�������
					break;
				}
				segNumber++;
				ByteBuffer.memset(msgDataSize, (byte) ' ');
				ByteBuffer.memset(fileOffset, (byte) ' ');
				int length = Long.toString(sendDataLength).getBytes().length;
				if (length > Constants.MSGDATASIZE_LENGTH)
					length = Constants.MSGDATASIZE_LENGTH;
				ByteBuffer.memcpy(msgDataSize, 0, Long.toString(sendDataLength).getBytes(), 0, length);
				length = Long.toString(seekOffset - offsetFile).getBytes().length;
				if (length > Constants.OFFSET_LENGTH)
					length = Constants.OFFSET_LENGTH;
				ByteBuffer.memcpy(fileOffset, 0, Long.toString(seekOffset - offsetFile).getBytes(), 0, length);
				seekOffset += sendDataLength;
				// ��������ݵ���MQ��Ϣ�������е����ݷ��ͳ�ȥ
				startTime = System.currentTimeMillis();
				factory.putData(new MsgDes(), sendBuffer, dataChannel);
				transInfo.setTotalSendedTime(System.currentTimeMillis() - startTime);
				// ���ͳɹ�
				transInfo.setTotalSendedSize(sendDataLength);
				sendFileControl.sendedSegNum = (int) segNumber;
				sendFileControl.sendedDataSize += sendDataLength;

				if (GlobalVar.isControlMsgPersist) {
					// �Ѵ���Ϣ�־û����ļ�ϵͳ��
					writeAccess1.seek(0);
					writeAccess1.write(sendFileControl.packMsgData());
				}
			}

			// �����ļ��Ѿ�������ϵ���Ϣ��MQ�гɹ�
			iRetVal = sendFileTransFinishedMsg(ctrlChannel);
			if (iRetVal == 0) {
				sendFileControl.isFinished = true;
				if (GlobalVar.isControlMsgPersist) {
					// �Ѵ���Ϣ�־û����ļ�ϵͳ��
					writeAccess1.seek(0);
					writeAccess1.write(sendFileControl.packMsgData());
				}
			}
			// ���������й��������ӵĴ�������Ҫ�������ӳ�����Ч������
			if (iRetVal == 2) {
				channelPool.destoryAllChannel();
			}
		} catch (Exception exc) {
			exc.printStackTrace();
			iRetVal = -1;
			if (GlobalVar.isControlMsgPersist) {
				if (writeAccess1 != null) {
					try {
						// �Ѵ���Ϣ�־û����ļ�ϵͳ��
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
			channelPool.freeConnection(dataChannel);
			channelPool.freeConnection(ctrlChannel);
			channelPool.freeConnection(feedbackChannel);
		}
		return iRetVal;
	}
}
