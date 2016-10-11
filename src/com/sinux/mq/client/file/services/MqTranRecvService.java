package com.sinux.mq.client.file.services;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.GetResponse;
import com.sinux.mq.client.MqChannel;
import com.sinux.mq.client.MqConnectionFactory;
import com.sinux.mq.client.mod.Constants;
import com.sinux.mq.client.mod.FileTransControlMsg;
import com.sinux.mq.client.mod.RecvFileControl;
import com.sinux.mq.client.mod.TransInfo;
import com.sinux.mq.client.util.ByteBuffer;

public class MqTranRecvService {

	private TransInfo transInfo = null;
	private byte[] msgid = null;
	@SuppressWarnings("rawtypes")
	private Map hRecvFileControl = new HashMap();
	private Object synObjectTrans = null;

	private MqChannelPool channelPool = null;

	public MqTranRecvService(MqConnectionFactory factory) {
		this.channelPool = MqChannelPool.getSingleInstance(factory);
	}

	@SuppressWarnings("rawtypes")
	public MqTranRecvService(byte[] msgid, TransInfo transInfo, Map hRecvFileControl, Object synObjectTrans,
			MqConnectionFactory factory) {
		this.transInfo = transInfo;
		this.msgid = msgid;
		this.hRecvFileControl = hRecvFileControl;
		this.synObjectTrans = synObjectTrans;
		this.channelPool = MqChannelPool.getSingleInstance(factory);
	}

	// ɾ��recvFileControl��Ӧ�Ķ������õ���Ϣ
	private int deleteInvalidControlInfoFromQueue(MqChannel channel) {
		int iRetVal = 0;
		MqChannel ctrlChannel = new MqChannel();
		ctrlChannel.setQueueName(channel.getQueueName() + GlobalVar.fileTransControlQueueName);
		ctrlChannel = channelPool.getMqChannel(ctrlChannel);
		try {
			if (ctrlChannel.getChannel().messageCount(ctrlChannel.getQueueName()) > 0) {
				ctrlChannel.getChannel().queuePurge(ctrlChannel.getQueueName());
			}
		} catch (Exception e) {
			iRetVal = -1;
			e.printStackTrace();
		}
		return iRetVal;
	}

	private void delInvalidMsgInfo(MqChannel channel, String queueName) throws Exception {
		channel = channelPool.getMqChannel(channel);
		try {
			if (channel.getChannel().messageCount(channel.getQueueName()) > 0) {
				channel.getChannel().queuePurge(channel.getQueueName());
			}
			channel.getChannel().queueDelete(channel.getQueueName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * 0:�ɹ� 1:�޷�ȡ����MQ���й����������� 2:��MQ���й�������������Ч������������ʧЧ����
	 * 3:�޷������ļ�����Ϊ����Ӧ�Ķ����в�������Ӧ�ؿ�����Ϣ
	 */
	// ��"+GlobalVar.fileTransControlFinishQueueName+"���к�"+GlobalVar.fileTransControlQueueName+"������ȡ����Ӧ����Ϣ
	@SuppressWarnings("unchecked")
	private int getControlInfoFromQueue(String receiverName, RecvFileControl recvFileControl) {
		int iRetVal = 0;
		MqChannel ctrlChannel = new MqChannel();
		ctrlChannel.setQueueName(receiverName + GlobalVar.fileTransControlQueueName);
		ctrlChannel = channelPool.getMqChannel(ctrlChannel);
		GetResponse response = null;
		try {
			FileTransControlMsg fileTransControlMsg = new FileTransControlMsg();
			response = ctrlChannel.getChannel().basicGet(ctrlChannel.getQueueName(), true);
			if (response != null) {
				fileTransControlMsg.unPackMsgData(response.getBody());
				recvFileControl.chunkCount = fileTransControlMsg.chunkCount;
				recvFileControl.chunkFileName = fileTransControlMsg.chunkFileName;
				recvFileControl.chunkNum = fileTransControlMsg.chunkNum;
				recvFileControl.dataSize = fileTransControlMsg.dataSize;
				recvFileControl.dirName = fileTransControlMsg.dirName;
				recvFileControl.fileName = fileTransControlMsg.fileName;
				recvFileControl.hostName = fileTransControlMsg.hostName;
				recvFileControl.ipAddress = fileTransControlMsg.ipAddress;
				recvFileControl.offsetFile = fileTransControlMsg.offsetFile;
				recvFileControl.queueName = fileTransControlMsg.queueName;
				recvFileControl.receiverName = fileTransControlMsg.receiverName;
				recvFileControl.segSize = fileTransControlMsg.segSize;
				recvFileControl.tradeCode = fileTransControlMsg.tradeCode;
				byte desc[] = new byte[Constants.DESC_LENGTH];
				recvFileControl.desc = (new String(desc)).trim();
			} else {
				iRetVal = -1;
			}

			if (iRetVal == 0) {
				hRecvFileControl.put(msgid, recvFileControl);
				// ��ʼ�Ѵ���Ϣ���־û���
				if (GlobalVar.isControlMsgPersist) {
					File contorlMsgPersistFile = new File(GlobalVar.tempPath + "/recv/" + ByteBuffer.ByteToHex(msgid));
					RandomAccessFile writeAccess1 = new RandomAccessFile(contorlMsgPersistFile, "rw");
					writeAccess1.seek(0);
					writeAccess1.write(recvFileControl.packMsgData());
					writeAccess1.close();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return iRetVal;
	}

	/*
	 * 0:�ɹ� 1:�޷�ȡ����MQ���й����������� 2:��MQ���й�������������Ч������������ʧЧ����
	 * 3:�޷������ļ�����Ϊ����Ӧ�Ķ����в�������Ӧ�ؿ�����Ϣ 4:�޷������ļ�����Ϊ����Ӧ�����ݶ����в�������Ӧ���ļ�������Ϣ
	 */
	@SuppressWarnings("unchecked")
	public int recvFile(String receiverName) {
		MqChannel channel = new MqChannel();
		channel.setQueueName(receiverName);
		MqChannel dataChannel = new MqChannel();
		MqChannel finishedChannel = new MqChannel();
		finishedChannel.setQueueName(channel.getQueueName() + GlobalVar.fileTransControlFinishQueueName);
		RecvFileControl recvFileControl = null;// new SendFileControl()
		RandomAccessFile writeAccess2 = null, writeAccess1 = null;
		File contorlMsgPersistFile = null;
		int iRetVal = 0;
		try {
			// 1.�ж���ǰ�����˶���
			// �ж��ڴ����Ƿ��Ѿ���������Ӧ��recvFileControl,����ڴ��в����ڵĻ����ӳ־û����ļ�ϵͳ�е��ļ���ȡ��Ӧ������
			boolean flag = false;

			// 2.ȡ����MQ������
			if (GlobalVar.isControlMsgPersist) {
				contorlMsgPersistFile = new File(GlobalVar.tempPath + "/recv/" + ByteBuffer.ByteToHex(msgid));
				if (contorlMsgPersistFile.exists()) {
					recvFileControl = new RecvFileControl();

					recvFileControl.absoluteFileName = "";
					// ���ļ����ڲ��Ҷ�ȡ���ļ�������
					writeAccess2 = new RandomAccessFile(contorlMsgPersistFile, "r");
					byte buffer[] = new byte[(int) contorlMsgPersistFile.length()];
					writeAccess2.readFully(buffer);
					writeAccess2.close();
					writeAccess2 = null;
					// ���������ļ������ݵ���Ӧ���ֶ�
					recvFileControl.unPackMsgData(buffer);
					synchronized (synObjectTrans) {
						hRecvFileControl.put(msgid, recvFileControl);
					}
					flag = true;
				}
				contorlMsgPersistFile = null;
			}
			if (!flag) {
				synchronized (synObjectTrans) {
					recvFileControl = (RecvFileControl) hRecvFileControl.get(msgid);
					if (recvFileControl == null) {
						recvFileControl = new RecvFileControl();
						recvFileControl.absoluteFileName = "";
						// ��Ҫ��MQ����Ӧ�Ķ�����ȡ����Щ��Ϣ
						iRetVal = getControlInfoFromQueue(receiverName, recvFileControl);
						System.out.println(Thread.currentThread().getName() + "��ȷ��ȡ�ļ����������Ϣ��");
						if (iRetVal != 0) {
							return iRetVal;
						}
					}
				}
			}
			transInfo.setTotalReceivedSize(recvFileControl.recvedDataSize);

			if (iRetVal != 0)
				return iRetVal;

			if (recvFileControl.isFinished) {// �Ѿ����������.����Ҫ�ٽ�����
				transInfo.setTotalReceivedSize(recvFileControl.recvedDataSize);
				delInvalidMsgInfo(channel, recvFileControl.queueName);
				iRetVal = deleteInvalidControlInfoFromQueue(channel);
				if (iRetVal == 0) {
					// ɾ�����ļ�
					contorlMsgPersistFile = new File(GlobalVar.tempPath + "/recv/" + ByteBuffer.ByteToHex(msgid));
					contorlMsgPersistFile.delete();
				}
				return iRetVal;
			}

			long transFileDataSize = Long.parseLong(recvFileControl.dataSize);// Ҫ������ļ������ݴ�С
			// ȡ���Ѿ�������ļ���С
			int recvDataLength = 0;// ÿ����Ҫ���յ����ݳ���

			String hostName = recvFileControl.hostName;
			String hostAddress = recvFileControl.ipAddress;
			File fileTmp = new File(GlobalVar.tempPath + "/recv/" + hostName + "/" + hostAddress + "/" + "/"
					+ recvFileControl.fileName);
			if (!fileTmp.exists()) {
				fileTmp.mkdirs();
			}
			File recvFile = new File(fileTmp.getAbsolutePath() + "/" + recvFileControl.chunkFileName);
			if (!recvFile.exists()) {
				if (!recvFile.createNewFile())
					throw new Exception("���ܴ������ļ�:" + recvFile.getAbsolutePath());
			} else {
				if (!recvFile.canWrite()) {
					throw new Exception("�ļ���" + recvFile.getAbsolutePath() + "����д��");
				}
			}
			recvFileControl.absoluteFileName = recvFile.getAbsolutePath();
			writeAccess2 = new RandomAccessFile(recvFile, "rw");
			if (GlobalVar.isControlMsgPersist) {
				contorlMsgPersistFile = new File(GlobalVar.tempPath + "/recv/" + ByteBuffer.ByteToHex(msgid));
				writeAccess1 = new RandomAccessFile(contorlMsgPersistFile, "rw");
			}

			byte msgDataSize[] = new byte[Constants.MSGDATASIZE_LENGTH];
			byte fileOffset[] = new byte[Constants.OFFSET_LENGTH];
			long startTime = 0;

			Map<String, Object> headers = new HashMap<String, Object>();
			dataChannel.setQueueName(recvFileControl.chunkFileName);
			dataChannel = channelPool.getMqChannel(dataChannel);

			System.out.println(Thread.currentThread().getName() + "��ʼ��ȡ�ļ�����ʱ�ļ���");
			while (recvFileControl.recvedDataSize != transFileDataSize) {// ѭ����������
				recvFileControl.recvedSegNum++;
				ByteBuffer.memset(msgDataSize, (byte) ' ');
				ByteBuffer.memset(fileOffset, (byte) ' ');
				GetResponse response = null;
				// ��ȡ������Ϣ
				try {
					startTime = System.currentTimeMillis();
					response = dataChannel.getChannel().basicGet(dataChannel.getQueueName(), true);
					headers = response.getProps().getHeaders();
					msgDataSize = (byte[]) headers.get("msgDataSize");
					fileOffset = (byte[]) headers.get("fileOffset");
				} catch (Exception exc) {
					recvFileControl.recvedSegNum--;
					exc.printStackTrace();
				}
				transInfo.setTotalReceivedSize(recvDataLength);
				transInfo.setTotalReceivedTime(System.currentTimeMillis() - startTime);

				recvDataLength = Integer.parseInt((new String(msgDataSize)).trim());

				writeAccess2.seek(Long.parseLong((new String(fileOffset)).trim()));
				writeAccess2.write(response.getBody(), 0, recvDataLength);

				// ���ͳɹ�
				recvFileControl.recvedDataSize += recvDataLength;
				if (GlobalVar.isControlMsgPersist) {
					// �Ѵ���Ϣ�־û����ļ�ϵͳ��
					writeAccess1.seek(0);
					writeAccess1.write(recvFileControl.packMsgData());
				}
			}
			System.out.println(Thread.currentThread().getName() + "��ʱ�ļ�д��ɹ���");

			recvFileControl.isFinished = true;

			iRetVal = sendFileRecvFinishedMsg(finishedChannel, recvFileControl);

			if (GlobalVar.isControlMsgPersist) {
				// �Ѵ���Ϣ�־û����ļ�ϵͳ��
				writeAccess1.seek(0);
				writeAccess1.write(recvFileControl.packMsgData());
			}
			// ɾ�����ݶ��ж��ڵ���Ϣ
			delInvalidMsgInfo(dataChannel, recvFileControl.queueName);
			// ɾ��recvFileControl��Ӧ�Ķ������õ���Ϣ
			iRetVal = deleteInvalidControlInfoFromQueue(channel);

			GlobalVar.tempFilePath = fileTmp.getPath();
		} catch (Exception e) {
			iRetVal = -1;
			if (GlobalVar.isControlMsgPersist) {
				if (writeAccess1 != null) {
					try {
						// �Ѵ���Ϣ�־û����ļ�ϵͳ��
						writeAccess1.seek(0);
						writeAccess1.write(recvFileControl.packMsgData());
					} catch (Exception exc1) {
						exc1.printStackTrace();
					}
				}
			}
			e.printStackTrace();
		} finally {
			try {
				if (writeAccess2 != null) {
					try {
						writeAccess2.close();
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
			channelPool.freeChannel(channel);
			channelPool.freeChannel(dataChannel);
		}
		return iRetVal;
	}

	private int sendFileRecvFinishedMsg(MqChannel channel, RecvFileControl recvFileControl) {
		int iRetVal = -1;
		channel = channelPool.getMqChannel(channel);
		try {
			channel.getChannel().basicPublish("", channel.getQueueName(), null, recvFileControl.packMsgData());
			iRetVal = 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return iRetVal;
	}
}
