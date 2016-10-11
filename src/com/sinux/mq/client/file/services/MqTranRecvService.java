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

	// 删除recvFileControl对应的多余无用的消息
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
	 * 0:成功 1:无法取得与MQ队列管理器的连接 2:与MQ队列管理器的连接无效，即出现网络失效错误
	 * 3:无法接收文件，因为在相应的队列中不存在相应地控制信息
	 */
	// 从"+GlobalVar.fileTransControlFinishQueueName+"队列和"+GlobalVar.fileTransControlQueueName+"队列中取得相应地信息
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
				// 开始把此消息给持久化掉
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
	 * 0:成功 1:无法取得与MQ队列管理器的连接 2:与MQ队列管理器的连接无效，即出现网络失效错误
	 * 3:无法接收文件，因为在相应的队列中不存在相应地控制信息 4:无法接收文件，因为在相应的数据队列中不存在相应地文件数据信息
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
			// 1.判断以前接收了多少
			// 判断内存中是否已经存在了相应地recvFileControl,如果内存中不存在的话将从持久化到文件系统中的文件读取相应的内容
			boolean flag = false;

			// 2.取得与MQ的连接
			if (GlobalVar.isControlMsgPersist) {
				contorlMsgPersistFile = new File(GlobalVar.tempPath + "/recv/" + ByteBuffer.ByteToHex(msgid));
				if (contorlMsgPersistFile.exists()) {
					recvFileControl = new RecvFileControl();

					recvFileControl.absoluteFileName = "";
					// 此文件存在并且读取此文件的内容
					writeAccess2 = new RandomAccessFile(contorlMsgPersistFile, "r");
					byte buffer[] = new byte[(int) contorlMsgPersistFile.length()];
					writeAccess2.readFully(buffer);
					writeAccess2.close();
					writeAccess2 = null;
					// 解析出此文件的内容到相应的字段
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
						// 需要从MQ中相应的队列中取得这些信息
						iRetVal = getControlInfoFromQueue(receiverName, recvFileControl);
						System.out.println(Thread.currentThread().getName() + "正确获取文件传输控制信息！");
						if (iRetVal != 0) {
							return iRetVal;
						}
					}
				}
			}
			transInfo.setTotalReceivedSize(recvFileControl.recvedDataSize);

			if (iRetVal != 0)
				return iRetVal;

			if (recvFileControl.isFinished) {// 已经接收完毕了.不需要再接收了
				transInfo.setTotalReceivedSize(recvFileControl.recvedDataSize);
				delInvalidMsgInfo(channel, recvFileControl.queueName);
				iRetVal = deleteInvalidControlInfoFromQueue(channel);
				if (iRetVal == 0) {
					// 删除此文件
					contorlMsgPersistFile = new File(GlobalVar.tempPath + "/recv/" + ByteBuffer.ByteToHex(msgid));
					contorlMsgPersistFile.delete();
				}
				return iRetVal;
			}

			long transFileDataSize = Long.parseLong(recvFileControl.dataSize);// 要传输的文件的数据大小
			// 取得已经传输的文件大小
			int recvDataLength = 0;// 每次需要接收的数据长度

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
					throw new Exception("不能创建新文件:" + recvFile.getAbsolutePath());
			} else {
				if (!recvFile.canWrite()) {
					throw new Exception("文件：" + recvFile.getAbsolutePath() + "不能写！");
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

			System.out.println(Thread.currentThread().getName() + "开始获取文件到临时文件！");
			while (recvFileControl.recvedDataSize != transFileDataSize) {// 循环接收数据
				recvFileControl.recvedSegNum++;
				ByteBuffer.memset(msgDataSize, (byte) ' ');
				ByteBuffer.memset(fileOffset, (byte) ' ');
				GetResponse response = null;
				// 获取数据信息
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

				// 发送成功
				recvFileControl.recvedDataSize += recvDataLength;
				if (GlobalVar.isControlMsgPersist) {
					// 把此信息持久化到文件系统中
					writeAccess1.seek(0);
					writeAccess1.write(recvFileControl.packMsgData());
				}
			}
			System.out.println(Thread.currentThread().getName() + "临时文件写入成功！");

			recvFileControl.isFinished = true;

			iRetVal = sendFileRecvFinishedMsg(finishedChannel, recvFileControl);

			if (GlobalVar.isControlMsgPersist) {
				// 把此信息持久化到文件系统中
				writeAccess1.seek(0);
				writeAccess1.write(recvFileControl.packMsgData());
			}
			// 删除数据队列多于的信息
			delInvalidMsgInfo(dataChannel, recvFileControl.queueName);
			// 删除recvFileControl对应的多余无用的消息
			iRetVal = deleteInvalidControlInfoFromQueue(channel);

			GlobalVar.tempFilePath = fileTmp.getPath();
		} catch (Exception e) {
			iRetVal = -1;
			if (GlobalVar.isControlMsgPersist) {
				if (writeAccess1 != null) {
					try {
						// 把此信息持久化到文件系统中
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
