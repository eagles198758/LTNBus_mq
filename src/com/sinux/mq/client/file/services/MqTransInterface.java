package com.sinux.mq.client.file.services;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.sinux.mq.client.MqChannel;
import com.sinux.mq.client.MqConnectionFactory;
import com.sinux.mq.client.mod.Constants;
import com.sinux.mq.client.mod.FileTransControlMsg;
import com.sinux.mq.client.mod.RecvFileControl;
import com.sinux.mq.client.mod.SendFileControl;
import com.sinux.mq.client.mod.TransInfo;
import com.sinux.mq.client.util.ByteBuffer;
import com.sinux.mq.client.util.CommonFun;
import com.sinux.mq.client.util.ComplexEvent;

public class MqTransInterface {
	private MqConnectionFactory factory = null;

	public MqTransInterface(MqConnectionFactory factory) {
		this.factory = factory;
	}

	public static void initFromConfig(String configFileName) throws Exception {
		InitEnvironment.initFromConfigFile(configFileName);
	}

	/*
	 * -6:没有足够的可用线程来启动分块传输，请过段时间重试 -5:没有空闲的服务线程服务 -4:要发送的文件不能够读取，不能够继续
	 * -3:要发送的不是文件 -2:要发送的文件不存在 -1:失败 0:成功 1:无法取得与MQ队列管理器的连接
	 * 2:与MQ队列管理器的连接无效，即出现网络失效错误
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int sendFile(String absolutFileName, String receiverName, TransInfo transInfo) {
		int iRetVal = 0;
		List serviceThreadList = new LinkedList();
		try {
			// System.out.println("开始准备文件:"+absolutFileName+"发送的前期准备工作!");
			File oFile = new File(absolutFileName);
			if (!oFile.exists()) {
				return -2;
			}
			if (!oFile.isFile()) {
				return -3;
			}
			if (!oFile.canRead()) {
				return -4;
			}
			long fileLength = oFile.length();
			String fileName = oFile.getName();
			String dirName = oFile.getParentFile().getAbsolutePath();

			int serviceThreadNum = 1, idleServiceThreadNum = 0;
			if (GlobalVar.isStartChunk) {// 需要启动分块传输机制
				// 这个主要是为了避免不大的文件也要启动分块传输机制
				if (fileLength > (GlobalVar.filesizeChunk * 1024)) {// 文件大小大于要进行分块的大小
					serviceThreadNum = GlobalVar.chunknum;
				}
			}
			ThreadPool threadPool = ThreadPool.getSingleInstance();
			ComplexEvent complexEvent = null;
			synchronized (GlobalVar.synObject) {
				// 没有可用的线程进行服务
				idleServiceThreadNum = threadPool.getIdleThreadNum();
				if (idleServiceThreadNum <= 0) {
					return -5;
				}
				// 没有足够的可用线程来启动分块传输，请过段时间重试
				if (serviceThreadNum > idleServiceThreadNum) {
					return -6;
				}
				complexEvent = new ComplexEvent(serviceThreadNum);
				// 开始取出空闲的服务线程出来，以开始进行服务
				for (int i = 0; i < serviceThreadNum; i++) {
					ServiceThread serviceThread = threadPool.getIdleServiceThread();
					serviceThreadList.add(serviceThread);
				}
			}
			// System.out.println("开始计算文件:"+absolutFileName+"的SHA值!");
			InetAddress inetAddress = InetAddress.getLocalHost();
			String hostName = inetAddress.getHostName();
			String hostAddress = inetAddress.getHostAddress();
			byte[] shaValueTmp = null;
			synchronized (GlobalVar.synObject) {
				GlobalVar.messageDigest.update((absolutFileName + hostName + hostAddress).getBytes("utf-8"));
				shaValueTmp = GlobalVar.messageDigest.digest();
			}
			byte[] shaValueBytes = new byte[24];
			ByteBuffer.memset(shaValueBytes, (byte) ' ');
			ByteBuffer.memcpy(shaValueBytes, 0, "00".getBytes(), 0, Constants.CHUNKNUM_LENGTH);
			ByteBuffer.memcpy(shaValueBytes, Constants.CHUNKNUM_LENGTH, shaValueTmp, 0, Constants.DIGEST_LENGTH);// shaValueTmp.length);

			// 开始准备传输
			// System.out.println("开始准备文件:"+absolutFileName+"的传输!");
			List fileTransControlMsgList = new LinkedList();
			List msgidList = new LinkedList();
			long offsetFile = 0;
			if (serviceThreadNum > 1) {
				// 开始对文件进行分断
				long chunkSize = fileLength / serviceThreadNum;
				long mod = fileLength % serviceThreadNum;
				for (int i = 0; i < serviceThreadNum; i++) {
					FileTransControlMsg fileTransControlMsg = new FileTransControlMsg();
					fileTransControlMsg.chunkCount = Integer.toString(serviceThreadNum);
					fileTransControlMsg.chunkFileName = fileName + Integer.toString(i);
					fileTransControlMsg.chunkNum = Integer.toString(i + 1);
					fileTransControlMsg.dirName = dirName;
					fileTransControlMsg.segSize = Integer.toString(GlobalVar.msgSize);
					fileTransControlMsg.fileName = fileName;
					fileTransControlMsg.hostName = hostName;
					fileTransControlMsg.ipAddress = hostAddress;
					if (i == 0)
						offsetFile = 0;
					else
						offsetFile += chunkSize;

					fileTransControlMsg.offsetFile = Long.toString(offsetFile);
					if (i == (serviceThreadNum - 1)) {
						fileTransControlMsg.dataSize = Long.toString(chunkSize + mod);
					} else
						fileTransControlMsg.dataSize = Long.toString(chunkSize);

					if (receiverName == null) {
						fileTransControlMsg.receiverName = "";
					} else
						fileTransControlMsg.receiverName = receiverName;

					fileTransControlMsg.tradeCode = "";
					fileTransControlMsg.queueName = GlobalVar.queueName;
					fileTransControlMsgList.add(fileTransControlMsg);
				}
			} else {
				FileTransControlMsg fileTransControlMsg = new FileTransControlMsg();
				fileTransControlMsg.chunkCount = Integer.toString(serviceThreadNum);
				fileTransControlMsg.chunkFileName = fileName;
				fileTransControlMsg.chunkNum = Integer.toString(0);
				fileTransControlMsg.dirName = dirName;
				fileTransControlMsg.segSize = Integer.toString(GlobalVar.msgSize);
				fileTransControlMsg.fileName = fileName;
				fileTransControlMsg.hostName = hostName;
				fileTransControlMsg.ipAddress = hostAddress;
				fileTransControlMsg.offsetFile = Long.toString(offsetFile);
				fileTransControlMsg.dataSize = Long.toString(fileLength);
				if (receiverName == null) {
					fileTransControlMsg.receiverName = "";
				} else
					fileTransControlMsg.receiverName = receiverName;
				fileTransControlMsg.tradeCode = "";
				fileTransControlMsg.queueName = GlobalVar.queueName;
				fileTransControlMsgList.add(fileTransControlMsg);
			}
			// 开始激发线程
			// System.out.println("开始激发服务线程来传输文件:"+absolutFileName+"!");
			for (int i = 0; i < serviceThreadNum; i++) {
				// byte[] msgid = new byte[24];
				// ByteBuffer.memset(msgid, (byte) ' ');
				ServiceThread serviceThread = (ServiceThread) serviceThreadList.get(i);
				FileTransControlMsg fileTransControlMsg = (FileTransControlMsg) fileTransControlMsgList.get(i);
				if (fileTransControlMsg.chunkNum.length() < Constants.CHUNKNUM_LENGTH) {
					fileTransControlMsg.chunkNum = "0" + fileTransControlMsg.chunkNum;
				}
				byte[] msgid = fileTransControlMsg.chunkFileName.getBytes();
				msgidList.add(msgid);
				serviceThread.prepareSend(GlobalVar.hSendFileControl, GlobalVar.synObjectSend, fileTransControlMsg,
						transInfo, complexEvent, msgid, factory);
			}
			complexEvent.WaitForSingleObject(-1);
			// 开始判断返回的结果
			boolean successFlag = true;
			for (Iterator i = msgidList.iterator(); i.hasNext();) {
				byte[] msgid = (byte[]) i.next();
				synchronized (GlobalVar.synObjectSend) {
					SendFileControl sendFileControl = (SendFileControl) GlobalVar.hSendFileControl.get(msgid);
					if (sendFileControl.retVal != 0) {
						successFlag = false;
						break;
					}
				}
			}
			if (!successFlag) {
				if (GlobalVar.isControlMsgPersist) {
					synchronized (GlobalVar.synObjectSend) {
						for (Iterator i = msgidList.iterator(); i.hasNext();) {
							byte[] msgid = (byte[]) i.next();
							File contorlMsgPersistFile = new File(
									GlobalVar.tempPath + "/send/" + ByteBuffer.ByteToHex(msgid));// (new
							RandomAccessFile writeAccess1 = new RandomAccessFile(contorlMsgPersistFile, "rw");
							SendFileControl sendFileControl = (SendFileControl) GlobalVar.hSendFileControl.get(msgid);
							writeAccess1.seek(0);
							writeAccess1.write(sendFileControl.packMsgData());
							writeAccess1.close();
							writeAccess1 = null;
						}
					}
				}
				return -1;
			} else {
				// 否则全部删除
				synchronized (GlobalVar.synObjectSend) {
					for (Iterator i = msgidList.iterator(); i.hasNext();) {
						byte[] msgid = (byte[]) i.next();
						File contorlMsgPersistFile = new File(
								GlobalVar.tempPath + "/send/" + ByteBuffer.ByteToHex(msgid));// (new
						contorlMsgPersistFile.delete();
						GlobalVar.hSendFileControl.remove(msgid);
					}
				}
				return iRetVal;
			}
		} catch (Exception exc) {
			iRetVal = -1;
		}
		return iRetVal;
	}

	/*
	 * 接收此对象对应的文件数据,返回接收到的文件名
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String recvFile(String dirName, String receiverName, TransInfo transInfo, List errorList) {
		String fileName = null;
		ComplexEvent complexEvent = null;
		List serviceThreadList = new LinkedList();
		try {
			if (receiverName == null)
				receiverName = "";
			int serviceThreadNum = getRecvThreadNum(receiverName) == null ? 0
					: Integer.valueOf(getRecvThreadNum(receiverName)), idleServiceThreadNum = 0;
			ThreadPool threadPool = ThreadPool.getSingleInstance();
			synchronized (GlobalVar.synObject) {
				// 没有可用的线程进行服务
				idleServiceThreadNum = threadPool.getIdleThreadNum();
				if (idleServiceThreadNum <= 0) {
					System.out.println("接收文件:" + "出现错误,错误信息:没有可用的线程进行服务!");
					return null;
				}
				// 没有足够的可用线程来启动分块传输，请过段时间重试
				if (serviceThreadNum > idleServiceThreadNum) {
					System.out.println("接收文件:" + "出现错误,错误信息:没有足够的可用线程来启动分块传输，请过段时间重试!");
					return null;
				}
				complexEvent = new ComplexEvent(serviceThreadNum);
				// 开始取出空闲的服务线程出来，以开始进行服务
				for (int i = 0; i < serviceThreadNum; i++) {
					ServiceThread serviceThread = threadPool.getIdleServiceThread();
					serviceThreadList.add(serviceThread);
				}
			}
			List msgidList = new LinkedList();
			// 开始激发线程
			System.out.println("开始激发服务线程来接收文件:" + "!");
			for (int i = 0; i < serviceThreadNum; i++) {
				ServiceThread serviceThread = (ServiceThread) serviceThreadList.get(i);
				byte[] msgid = new byte[24];
				ByteBuffer.memset(msgid, (byte) ' ');

				int chunkNum = 0;
				if (serviceThreadNum == 1) {
					chunkNum = 0;
				} else {
					chunkNum = i + 1;
				}
				if (Integer.toString(chunkNum).length() < 2) {
					ByteBuffer.memcpy(msgid, Constants.CHUNKNUM_LENGTH + Constants.DIGEST_LENGTH,
							("0" + Integer.toString(chunkNum)).getBytes(), 0, 2);

				} else {
					ByteBuffer.memcpy(msgid, Constants.CHUNKNUM_LENGTH + Constants.DIGEST_LENGTH,
							(Integer.toString(chunkNum)).getBytes(), 0, 2);
				}
				msgidList.add(msgid);
				serviceThread.prepareRecv(GlobalVar.hRecvFileControl, GlobalVar.synObjectRecv, transInfo, complexEvent,
						msgid, receiverName, this.factory);
			}
			System.out.println("开始等待" + serviceThreadNum + "个服务线程接收文件:" + "返回!");
			complexEvent.WaitForSingleObject(-1);
			// 开始判断返回的结果
			System.out.println("开始分析" + serviceThreadNum + "个服务线程接收文件:" + "的返回结果!");
			boolean successFlag = true;
			for (Iterator i = msgidList.iterator(); i.hasNext();) {
				byte[] msgid = (byte[]) i.next();

				synchronized (GlobalVar.synObjectRecv) {
					RecvFileControl recvFileControl = (RecvFileControl) GlobalVar.hRecvFileControl.get(msgid);
					if (recvFileControl.retVal != 0) {
						successFlag = false;
						// 3:无法接收文件，因为在相应的队列中不存在相应地控制信息
						// * 4:无法接收文件，因为在相应的数据队列中不存在相应地文件数据信息
						if (recvFileControl.retVal == 3) {
							errorList.add("无法接收文件:" + "! 因为在相应的队列中不存在相应地控制信息.");
							File contorlMsgPersistFile = new File(
									GlobalVar.tempPath + "/recv/" + ByteBuffer.ByteToHex(msgid));
							contorlMsgPersistFile.delete();
							GlobalVar.hRecvFileControl.remove(msgid);
						}
						if (recvFileControl.retVal == 4) {
							errorList.add("无法接收文件:" + "!因为在相应的数据队列中不存在相应地文件数据信息.");
							File contorlMsgPersistFile = new File(
									GlobalVar.tempPath + "/recv/" + ByteBuffer.ByteToHex(msgid));
							contorlMsgPersistFile.delete();
							GlobalVar.hRecvFileControl.remove(msgid);
						}
					}
				}
			}
			if (!successFlag) {
				if (GlobalVar.isControlMsgPersist) {
					synchronized (GlobalVar.synObjectRecv) {
						for (Iterator i = msgidList.iterator(); i.hasNext();) {
							byte[] msgid = (byte[]) i.next();
							File contorlMsgPersistFile = new File(
									GlobalVar.tempPath + "/recv/" + ByteBuffer.ByteToHex(msgid));
							RandomAccessFile writeAccess1 = new RandomAccessFile(contorlMsgPersistFile, "rw");
							RecvFileControl recvFileControl = (RecvFileControl) GlobalVar.hRecvFileControl.get(msgid);

							writeAccess1.seek(0);
							writeAccess1.write(recvFileControl.packMsgData());
							writeAccess1.close();
							writeAccess1 = null;
						}
					}
				}
				return null;
			} else {
				// 开始合并文件
				synchronized (GlobalVar.synObjectSend) {
					File tempFile = new File(GlobalVar.tempFilePath);
					File[] tempFiles = tempFile.listFiles();
					for (int i = 0; i < tempFiles.length; i++) {
						if (i == 0) {
							fileName = dirName + "/" + receiverName;
							File deleteFile = new File(fileName);
							deleteFile.delete();
							CommonFun.FileCut(tempFiles[i].getAbsolutePath(), fileName);
						} else {
							// 文件追加合并
							CommonFun.FileAppend(tempFiles[i].getAbsolutePath(), fileName);
							File deleteFile = new File(tempFiles[i].getAbsolutePath());
							deleteFile.delete();
						}
					}
					tempFile.delete();
				}
				// 否则全部删除
				synchronized (GlobalVar.synObjectSend) {
					for (Iterator i = msgidList.iterator(); i.hasNext();) {
						byte[] msgid = (byte[]) i.next();
						File contorlMsgPersistFile = new File(
								GlobalVar.tempPath + "/recv/" + ByteBuffer.ByteToHex(msgid));
						contorlMsgPersistFile.delete();
						GlobalVar.hRecvFileControl.remove(msgid);
					}
				}
			}
		} catch (Exception exc) {
			fileName = null;
			System.out.println("接收文件:" + "出现异常！");
			exc.printStackTrace();
		}
		return fileName;
	}

	private String getRecvThreadNum(String receiverName) {
		MqChannel channel = factory.openChannel(receiverName + GlobalVar.fileTransControlQueueName, false, false);
		try {
			return String.valueOf(channel.getChannel().messageCount(channel.getQueueName()));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				channel.getChannel().close();
			} catch (IOException | TimeoutException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
