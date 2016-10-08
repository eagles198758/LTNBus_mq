package com.sinux.mq.client.file.services;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.GetResponse;
import com.sinux.mq.client.MqChannel;
import com.sinux.mq.client.MqConnectionFactory;
import com.sinux.mq.client.mod.Constants;
import com.sinux.mq.client.mod.FileTransControlMsg;
import com.sinux.mq.client.mod.MsgDes;
import com.sinux.mq.client.mod.ReceveFileData;
import com.sinux.mq.client.mod.RecvFileControl;
import com.sinux.mq.client.mod.TransInfo;
import com.sinux.mq.client.util.ByteArrayutil;
import com.sinux.mq.client.util.ByteBuffer;
import com.sinux.mq.client.util.CommonFun;

/*
 * File oFile = new File(GlobalVar.tempPath+"/recv");
			if(oFile.exists())
			{
				File oFiles[] = oFile.listFiles();
				for(int i=0;i<oFile.length();i++)
				{
					File contorlMsgPersistFile = oFiles[i];
					if(contorlMsgPersistFile.isFile())
					{
						
							RecvFileControl recvFileControl = new RecvFileControl();
							
							recvFileControl.absoluteFileName = "";
							//此文件存在并且读取此文件的内容
							RandomAccessFile writeAccess2 = new RandomAccessFile(contorlMsgPersistFile,"r");
							byte buffer[] = new byte[(int) contorlMsgPersistFile.length()];
							writeAccess2.readFully(buffer);
							writeAccess2.close();
							writeAccess2 = null;
							//解析出此文件的内容到相应的字段
							try
							{
								recvFileControl.unPackMsgData(buffer);
							}
							catch(Exception exc)
							{
								continue;
							}
							
							ReceveFileData receiveFileData = new ReceveFileData();
							receiveFileData.msgid = ByteBuffer.HexToByte(contorlMsgPersistFile.getName());
							receiveFileData.chunkcount = Integer.parseInt(recvFileControl.chunkCount);
							receiveFileData.tradeCode = recvFileControl.tradeCode;
							receiveFileData.fileLength = recvFileControl.
						
					}
				}
			}
 */
/**
 * @author jingwen.tong 2006-12-19 Copyright IBM 2005 利用MQ消息完成断点下载的实现类
 */
public class MqTranRecvService {

	private TransInfo transInfo = null;
	private MqConnectionFactory factory = null;
	private byte[] msgid = null;
	private Map hRecvFileControl = new HashMap();
	private Object synObjectTrans = null;

	public MqTranRecvService(MqConnectionFactory factory) {
		this.factory = factory;
	}

	public MqTranRecvService(byte[] msgid, TransInfo transInfo, Map hRecvFileControl, Object synObjectTrans,
			MqConnectionFactory factory) {
		this.transInfo = transInfo;
		this.msgid = msgid;

		this.hRecvFileControl = hRecvFileControl;
		this.synObjectTrans = synObjectTrans;

		this.factory = factory;
	}

	// 删除recvFileControl对应的多余无用的消息
	private int deleteInvalidControlInfoFromQueue(MqChannel channel) {
		int iRetVal = 0;
		MqChannelPool channelPool = MqChannelPool.getSingleInstance(factory);
		channel = channelPool.getMqChannel(channel);
		GetResponse response = null;
		try {
			do {
				response = channel.getChannel().basicGet(channel.getQueueName(), true);
			} while (response.getBody().length == 0);
		} catch (Exception e) {
			iRetVal = -1;
			e.printStackTrace();
		} finally {
			if (channel.getChannel().isOpen()) {
				try {
					channel.getChannel().close();
				} catch (IOException | TimeoutException e) {
					e.printStackTrace();
				}
			}
		}
		return iRetVal;
	}

	// 判断msgid对应的信息是否确实存在需要接收的文件数据
	private boolean isExistFileData(MqChannel channel, byte[] msgid, String msgSegNum, String queueName) {
		boolean bRetVal = false;

		MqChannelPool channelPool = MqChannelPool.getSingleInstance(factory);
		channel = channelPool.getMqChannel(channel);
		GetResponse response = null;
		try {
			response = channel.getChannel().basicGet(channel.getQueueName(), true);
			if (response != null) {
				bRetVal = true;
			}
		} catch (Exception e) {
			bRetVal = false;
			e.printStackTrace();
		} finally {
			if (channel.getChannel().isOpen()) {
				try {
					channel.getChannel().close();
				} catch (IOException | TimeoutException e) {
					e.printStackTrace();
				}
			}
		}
		return bRetVal;
	}

	private void delInvalidMsgInfo(MqChannel channel, String queueName) throws Exception {
		MqChannelPool channelPool = MqChannelPool.getSingleInstance(factory);
		channel = channelPool.getMqChannel(channel);
		GetResponse response = null;
		try {
			response = channel.getChannel().basicGet(channel.getQueueName(), true);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (channel.getChannel().isOpen()) {
				try {
					channel.getChannel().close();
				} catch (IOException | TimeoutException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/*
	 * private int delControlInfoFromQueue(MQConnection connection,String
	 * receiverName) { int iRetVal = 0; MQQueue fileTransFinishedControlQueue =
	 * null,fileTransControlQueue = null; MQConnectionInfo connectionInfo =
	 * connection.getConnectionInfo(); MQConnectionPool connectionPool =
	 * MQConnectionPool.getSingleInstance(); boolean connectFlag =
	 * connectionInfo.getConnectFlag(); MQGetMessageOptions gmo = new
	 * MQGetMessageOptions(); MQMessage message1 = new MQMessage (); MQMessage
	 * message2 = new MQMessage ();
	 * 
	 * try {
	 * 
	 * // 把队列中相应地消息给删除 fileTransFinishedControlQueue =
	 * connection.getConnection().accessQueue
	 * (GlobalVar.fileTransControlFinishQueueName, MQC.MQOO_INPUT_SHARED
	 * |MQC.MQOO_INQUIRE|MQC.MQOO_FAIL_IF_QUIESCING, "", "", "mqm");
	 * fileTransControlQueue = connection.getConnection().accessQueue
	 * (GlobalVar.fileTransControlQueueName, MQC.MQOO_INPUT_SHARED
	 * |MQC.MQOO_INQUIRE|MQC.MQOO_FAIL_IF_QUIESCING, "", "", "mqm"); gmo.options
	 * = MQC.MQGMO_FAIL_IF_QUIESCING|MQC.MQGMO_NO_WAIT; gmo.waitInterval = 300;
	 * gmo.matchOptions = MQC.MQMO_MATCH_MSG_ID;
	 * if(receiverName.compareToIgnoreCase("")!=0) { message2.correlationId =
	 * receiverName.getBytes(); gmo.matchOptions =
	 * MQC.MQMO_MATCH_MSG_ID|MQC.MQMO_MATCH_CORREL_ID; } else gmo.matchOptions =
	 * MQC.MQMO_MATCH_MSG_ID; message2.messageId = msgid; message2.messageType =
	 * MQC.MQMT_DATAGRAM; message2.format = MQC.MQFMT_NONE ; do { try {
	 * fileTransFinishedControlQueue.get(message2,gmo); message2.clearMessage();
	 * } catch (MQException exc) { if(exc.completionCode
	 * ==MQException.MQCC_FAILED) { if(exc.reasonCode ==
	 * MQException.MQRC_NO_MSG_AVAILABLE) break; else throw exc; } // } }
	 * while(true); gmo.options = MQC.MQGMO_FAIL_IF_QUIESCING|MQC.MQGMO_NO_WAIT;
	 * gmo.waitInterval = 300; gmo.matchOptions = MQC.MQMO_MATCH_MSG_ID;
	 * message1.messageType = MQC.MQMT_DATAGRAM; message1.format =
	 * MQC.MQFMT_NONE ; message1.messageId = msgid; do { try {
	 * fileTransControlQueue.get(message1,gmo); message1.clearMessage(); } catch
	 * (MQException exc) { if(exc.completionCode ==MQException.MQCC_FAILED) {
	 * if(exc.reasonCode == MQException.MQRC_NO_MSG_AVAILABLE) break; else throw
	 * exc; } // } } while(true); } catch (MQException exc) {
	 * 
	 * 
	 * if(!connectFlag)
	 * logImpl.error("从队列管理器:"+connectionInfo.getQueueManagerName()+
	 * "进行文件下载取得相应的控制信息失败!" ,exc); else
	 * logImpl.error("从队列管理器:"+connectionInfo.getQueueManagerName()+"hostname:"
	 * +connectionInfo.getHostName()+"端口号:"+connectionInfo.getPort()+
	 * "进行文件下载取得相应的控制信息失败!" ,exc); if(exc.completionCode ==
	 * MQException.MQCC_FAILED) {
	 * 
	 * if(exc.reasonCode == MQException.MQRC_CONNECTION_BROKEN || exc.reasonCode
	 * ==MQException.MQRC_CONNECTION_QUIESCING ||exc.reasonCode ==
	 * MQException.MQRC_CONNECTION_STOPPING) { iRetVal = 2; //该把连接池中的连接全部清除掉了
	 * connectionPool.destoryAllConnection(connectionInfo); } else { iRetVal =
	 * -1; }
	 * 
	 * }
	 * 
	 * } catch(Exception exc) { if(!connectFlag)
	 * logImpl.error("从队列管理器:"+connectionInfo.getQueueManagerName()+
	 * "进行文件下载取得相应的控制信息失败!" ,exc); else
	 * logImpl.error("从队列管理器:"+connectionInfo.getQueueManagerName()+"hostname:"
	 * +connectionInfo.getHostName()+"端口号:"+connectionInfo.getPort()+
	 * "进行文件下载取得相应的控制信息失败!" ,exc); iRetVal = -1;
	 * 
	 * 
	 * } finally { try { if(fileTransFinishedControlQueue!=null)
	 * fileTransFinishedControlQueue.close();
	 * 
	 * if(fileTransControlQueue!=null) fileTransControlQueue.close(); }
	 * catch(Exception exc) { if(!connectFlag)
	 * logImpl.error("往队列管理器:"+connectionInfo.getQueueManagerName()+"关闭"+
	 * GlobalVar.fileTransControlFinishQueueName+"/"+GlobalVar.
	 * fileTransControlQueueName+"队列失败!",exc); else
	 * logImpl.error("往队列管理器:"+connectionInfo.getQueueManagerName()+"hostname:"
	 * +connectionInfo.getHostName()+"端口号:"+connectionInfo.getPort()+"关闭"+
	 * GlobalVar.fileTransControlFinishQueueName+"/"+GlobalVar.
	 * fileTransControlQueueName+"队列失败!",exc);
	 * 
	 * } } return iRetVal; }
	 */
	/*
	 * 0:成功 1:无法取得与MQ队列管理器的连接 2:与MQ队列管理器的连接无效，即出现网络失效错误
	 * 3:无法接收文件，因为在相应的队列中不存在相应地控制信息
	 */
	// 从"+GlobalVar.fileTransControlFinishQueueName+"队列和"+GlobalVar.fileTransControlQueueName+"队列中取得相应地信息
	private int getControlInfoFromQueue(MqChannel channel, String receiverName, RecvFileControl recvFileControl) {
		int iRetVal = 0;
		MqChannelPool channelPool = MqChannelPool.getSingleInstance(factory);
		channel = channelPool.getMqChannel(channel);
		GetResponse response = null;
		try {
			FileTransControlMsg fileTransControlMsg = new FileTransControlMsg();
			response = channel.getChannel().basicGet(channel.getQueueName(), true);
			// message1.readFully(fileTransControlMsg.getBuffer());
			fileTransControlMsg.unPackMsgData(response.getBody());
			// recvFileControl.absoluteFileName
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
			// message1.clearMessage();

			byte chunkCount[] = new byte[Constants.CHUNKCOUNT_LENGTH];
			byte chunkNum[] = new byte[Constants.CHUNKNUM_LENGTH];
			byte desc[] = new byte[Constants.DESC_LENGTH];
			byte tradeCode[] = new byte[Constants.TRADECODE_LENGTH];
			ByteBuffer.memset(tradeCode, (byte) ' ');
			ByteBuffer.memset(chunkCount, (byte) ' ');
			ByteBuffer.memset(chunkNum, (byte) ' ');
			ByteBuffer.memset(desc, (byte) ' ');

			// message2.readFully(chunkCount);
			// message2.readFully(chunkNum);
			// message2.readFully(desc);
			// message2.readFully(tradeCode);
			recvFileControl.desc = (new String(desc)).trim();

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
		} finally {
			if (channel.getChannel().isOpen()) {
				try {
					channel.getChannel().close();
				} catch (IOException | TimeoutException e) {
					e.printStackTrace();
				}
			}
		}
		return iRetVal;
	}

	private List getReceiveFileList(MqChannel channel, String receiverName, int count) {
		MqChannelPool channelPool = MqChannelPool.getSingleInstance(factory);
		channel = channelPool.getMqChannel(channel);
		GetResponse response = null;
		List receiveListTmp = null;
		List receiveList = new LinkedList();
		Map receiveMap = new HashMap();
		int iNum = 0;
		try {
			FileTransControlMsg fileTransControlMsg = new FileTransControlMsg();
			ReceveFileData receiveFileData = new ReceveFileData();
			while (true) {
				response = channel.getChannel().basicGet(channel.getQueueName(), true);
				if(response == null){
					break;
				}
				if(response.getBody().length == Constants.DESC_LENGTH){
//					receiveFileData.msgid = response.getBody();
				}else{
					fileTransControlMsg.unPackMsgData(response.getBody());
				}
				// receiveFileData.msgid = message.messageId;
//				byte chunkCount[] = new byte[Constants.CHUNKCOUNT_LENGTH];
//				byte chunkNum[] = new byte[Constants.CHUNKNUM_LENGTH];
//				byte desc[] = new byte[Constants.DESC_LENGTH];
//				byte tradeCode[] = new byte[Constants.TRADECODE_LENGTH];
//				byte fileLength1[] = new byte[Constants.SENDEDDATASIZE_LENGTH];
//				ByteBuffer.memset(tradeCode, (byte) ' ');
//				ByteBuffer.memset(chunkCount, (byte) ' ');
//				ByteBuffer.memset(chunkNum, (byte) ' ');
//				ByteBuffer.memset(desc, (byte) ' ');
//				ByteBuffer.memset(fileLength1, (byte) ' ');
				// message.readFully(chunkCount);
				// message.readFully(chunkNum);
				// message.readFully(desc);
				// message.readFully(tradeCode);
				// message.readFully(fileLength1);
				receiveFileData.chunkcount = Integer.parseInt(fileTransControlMsg.chunkCount);
//				receiveFileData.desc = (new String(desc)).trim();
				receiveFileData.tradeCode = fileTransControlMsg.tradeCode;
				receiveFileData.fileLength = Long.parseLong(fileTransControlMsg.dataSize);
				byte[] shaValue = new byte[22];

//				ByteBuffer.memcpy(shaValue, 0, receiveFileData.msgid, 0, 22);

//				String shaValue1 = ByteBuffer.ByteToHex(shaValue);
				// shaValue =
				// msgid.substring(0,msgid.length()-Constants.CHUNKNUM_LENGTH);
				/*
				 * for (Iterator i = receiveMap.keySet().iterator();
				 * i.hasNext();) { String shaValue2 = (String)i.next();
				 * if(shaValue2.compareTo(shaValue1)==0) { shaValue1 =
				 * shaValue2; } }
				 */
//				receiveListTmp = (List) receiveMap.get(shaValue1);
				
				if (receiveListTmp == null) {
					receiveListTmp = new LinkedList();
					receiveListTmp.add(receiveFileData);
					receiveMap.put(String.valueOf(iNum), receiveListTmp);
				} else
					receiveListTmp.add(receiveFileData);
				iNum++;
			};

			for (Iterator i = receiveMap.keySet().iterator(); i.hasNext();) {
				String index = String.valueOf(i.next());
				receiveListTmp = (List) receiveMap.get(index);
				ReceveFileData receiveFileData1 = (ReceveFileData) receiveListTmp.get(0);
				if (receiveFileData1.chunkcount * 2 == receiveListTmp.size())// 主要是应对分段上传的情况
				{
					receiveList.add(receiveFileData1);
				}
			}
			receiveMap = null;
		} catch (Exception e) {
			channelPool.destoryAllChannel();
			e.printStackTrace();
		} finally {
			if (channel.getChannel().isOpen()) {
				try {
					channel.getChannel().close();
				} catch (IOException | TimeoutException e) {
					e.printStackTrace();
				}
			}
		}
		return receiveList;
	}

	/*
	 * 得到能够接收的文件数据列表
	 */
	public List getReceiveFileList(String receiverName, int count) {
		List receiveList = null;

		MqChannelPool channelPool = MqChannelPool.getSingleInstance(factory);
		MqChannel channel = new MqChannel();
		channel.setQueueName(receiverName);
		channel = channelPool.getMqChannel(channel);
		try {
			if (channel != null) {
				receiveList = getReceiveFileList(channel, receiverName, count);
			}
		} catch (Exception exc) {

		} finally {
			channelPool.freeChannel(channel);
		}
		return receiveList;
	}

	/*
	 * 0:成功 1:无法取得与MQ队列管理器的连接 2:与MQ队列管理器的连接无效，即出现网络失效错误
	 * 3:无法接收文件，因为在相应的队列中不存在相应地控制信息 4:无法接收文件，因为在相应的数据队列中不存在相应地文件数据信息
	 */
	public int recvFile(String receiverName) {
		MqChannelPool channelPool = MqChannelPool.getSingleInstance(factory);
		MqChannel channel = new MqChannel();
		channel.setQueueName(receiverName);
		channel = channelPool.getMqChannel(channel);
		// String fileName = null;
		// MQConnection connection = null;
		// MQConnectionInfo connectionInfo = null;
		// MQConnectionPool connectionPool =
		// MQConnectionPool.getSingleInstance();
		RecvFileControl recvFileControl = null;// new SendFileControl()
		RandomAccessFile writeAccess2 = null, writeAccess1 = null;
		File contorlMsgPersistFile = null;
		// MQQueue fileTransDataQueue = null;
		int iRetVal = 0;
		try {
			// 1.判断以前接收了多少
			// 判断内存中是否已经存在了相应地recvFileControl,如果内存中不存在的话将从持久化到文件系统中的文件读取相应的内容
			boolean flag = false;

			// 2.取得与MQ的连接
			// connection =
			// connectionPool.getConnection(GlobalVar.connectionInfo);

			if (channel == null) {
				iRetVal = 1;
				return iRetVal;
			}
			// connectionInfo = connection.getConnectionInfo();
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
						iRetVal = getControlInfoFromQueue(channel, receiverName, recvFileControl);
						if (iRetVal != 0)
							return iRetVal;
						/*
						 * if(!isExistFileData(connection,msgid,"1",
						 * recvFileControl.queueName)) { contorlMsgPersistFile =
						 * new
						 * File(GlobalVar.tempPath+"/recv/"+ByteBuffer.ByteToHex
						 * (msgid)); contorlMsgPersistFile.delete(); return 4; }
						 */
					}
				}
			}
			transInfo.setTotalReceivedSize(recvFileControl.recvedDataSize);

			if (iRetVal != 0)
				return iRetVal;

			if (recvFileControl.isFinished)	{// 已经接收完毕了.不需要再接收了
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

			// 判断fileTransControlMsg对应的消息是否在"+GlobalVar.fileTransControlQueueName+"队列中存在

			int segSize = Integer.parseInt(recvFileControl.segSize);
			long transFileDataSize = Long.parseLong(recvFileControl.dataSize);// 要传输的文件的数据大小
			// 取得已经传输的文件大小

			int recvDataLength = 0;// 每次需要接收的数据长度
			/*
			 * 
			 */
			// ------- begin
			String newDirName = CommonFun.replace(recvFileControl.dirName, "\\", "/");
			/*
			 * byte[] dirNameBytes = newDirName.getBytes(); int length =
			 * dirNameBytes.length; while(true) {
			 * 
			 * if(dirNameBytes[length-1]==(byte)'/') length--; else break; }
			 * String newDirName1 = newDirName.substring(0,length);
			 */
			int offset = newDirName.indexOf('/');
			String dirNameTmp = newDirName.substring(offset + 1, newDirName.length());
			// -------- end

			// String dirNameTmp =
			// recvFileControl.dirName.substring(recvFileControl.dirName.lastIndexOf("/"),recvFileControl.dirName.length()-recvFileControl.dirName.lastIndexOf("/"));

			String hostName = recvFileControl.hostName;
			String hostAddress = recvFileControl.ipAddress;
			File fileTmp = new File(GlobalVar.tempPath + "/recv/" + hostName + "/" + hostAddress + "/" + dirNameTmp);
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
			byte recvBuffer[] = new byte[segSize];

			// if(Integer.parseInt(recvFileControl.chunkCount)==1){

			// long seekOffset = recvFileControl.recvedDataSize;
			// long startTime;

			// MQMessage message = new MQMessage ();
			// MQGetMessageOptions gmo = new MQGetMessageOptions();
			// gmo.options =
			// MQC.MQGMO_FAIL_IF_QUIESCING|MQC.MQGMO_NO_WAIT|MQC.MQGMO_SYNCPOINT;
			// gmo.waitInterval = 300;
			// gmo.matchOptions =
			// MQC.MQMO_MATCH_MSG_ID|MQC.MQMO_MATCH_CORREL_ID;
			//
			// fileTransDataQueue = connection.getConnection().accessQueue
			// (recvFileControl.queueName,
			// MQC.MQOO_INPUT_SHARED
			// |MQC.MQOO_INQUIRE|MQC.MQOO_FAIL_IF_QUIESCING, "", "", "mqm");
			//
			/*
			 * long segNumber = recvFileControl.recvedSegNum; if(segNumber==0)
			 * segNumber++;
			 */
			byte msgDataSize[] = new byte[Constants.MSGDATASIZE_LENGTH];
			byte fileOffset[] = new byte[Constants.OFFSET_LENGTH];
			long startTime = 0;
			while (recvFileControl.recvedDataSize != transFileDataSize)// 循环接收数据
			{
				// message.messageType = MQC.MQMT_DATAGRAM;
				// message.format = MQC.MQFMT_NONE ;
				// message.messageId = msgid;
				recvFileControl.recvedSegNum++;
				// message.correlationId =
				// Long.toString(recvFileControl.recvedSegNum).getBytes();
				//
				ByteBuffer.memset(msgDataSize, (byte) ' ');
				ByteBuffer.memset(fileOffset, (byte) ' ');
				ByteBuffer.memset(recvBuffer, (byte) ' ');
				// 获取数据信息
				try {
					startTime = System.currentTimeMillis();
					// fileTransDataQueue.get(message,gmo);
					channel.getChannel().basicGet(channel.getQueueName(), true);
				} catch (Exception exc) {
					recvFileControl.recvedSegNum--;
					// if(exc.completionCode ==MQException.MQCC_FAILED)
					// {
					// if(exc.reasonCode ==
					// MQException.MQRC_NO_MSG_AVAILABLE)//出现有数据缺失,
					// {
					// recvFileControl.recvedSegNum++;
					// //写入文件系统中
					// LogService.Log("error",recvFileControl.desc+"
					// 缺失第:"+recvFileControl.recvedSegNum+"段数据!");
					// continue;
					// }
					// else
					// throw exc;
					// }
					exc.printStackTrace();
				}
				transInfo.setTotalReceivedSize(recvDataLength);

				transInfo.setTotalReceivedTime(System.currentTimeMillis() - startTime);
				// message.readFully(msgDataSize);
				// message.readFully(fileOffset);
				recvDataLength = Integer.parseInt((new String(msgDataSize)).trim());
				// message.readFully(recvBuffer,0,recvDataLength);
				// message.clearMessage();
				writeAccess2.seek(Long.parseLong((new String(fileOffset)).trim()));
				writeAccess2.write(recvBuffer, 0, recvDataLength);

				// 发送成功

				// transInfo.setTotalSendedSize((long)sendDataLength);
				// recvFileControl.recvedSegNum ++;
				recvFileControl.recvedDataSize += recvDataLength;
				if (GlobalVar.isControlMsgPersist) {
					// 把此信息持久化到文件系统中
					writeAccess1.seek(0);
					writeAccess1.write(recvFileControl.packMsgData());
				}
			}
			// fileTransDataQueue.close();
			// fileTransDataQueue = null;
			recvFileControl.isFinished = true;
			if (GlobalVar.isControlMsgPersist) {
				// 把此信息持久化到文件系统中
				writeAccess1.seek(0);
				writeAccess1.write(recvFileControl.packMsgData());
			}
			// 删除数据队列多于的信息
			delInvalidMsgInfo(channel, recvFileControl.queueName);
			// 删除recvFileControl对应的多余无用的消息
			iRetVal = deleteInvalidControlInfoFromQueue(channel);
			// }
			// catch (Exception exc)
			// {
			// iRetVal =-1;
			// boolean connectFlag =
			// connection.getConnectionInfo().getConnectFlag();
			// if(exc.completionCode == MQException.MQCC_FAILED)
			// {
			//
			// if(exc.reasonCode == MQException.MQRC_CONNECTION_BROKEN ||
			// exc.reasonCode ==MQException.MQRC_CONNECTION_QUIESCING
			// ||exc.reasonCode == MQException.MQRC_CONNECTION_STOPPING)
			// {
			// iRetVal = 2;
			// //该把连接池中的连接全部清除掉了
			// connectionPool.destoryAllConnection(connectionInfo);
			// }
			// }
			// exc.printStackTrace();
		} catch (Exception e) {
			iRetVal = -1;

			if (GlobalVar.isControlMsgPersist) {
				if (writeAccess1 != null) {
					try {
						// 把此信息持久化到文件系统中
						writeAccess1.seek(0);
						writeAccess1.write(recvFileControl.packMsgData());
					} catch (Exception exc1) {

					}
				}
			}
		} finally {
			try {
				if (channel.getChannel() != null) {
					try {
						if (channel.getChannel().isOpen()) {
							channel.getChannel().close();
						}
					} catch (Exception exc) {
					}
				}
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
		}
		return iRetVal;
	}
}
