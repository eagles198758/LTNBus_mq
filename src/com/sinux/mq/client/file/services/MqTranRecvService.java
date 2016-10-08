//package com.sinux.mq.client.file.services;
//
//import java.io.File;
//import java.io.RandomAccessFile;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//
//import com.sinux.mq.client.mod.TransInfo;
//
///*
// * File oFile = new File(GlobalVar.tempPath+"/recv");
//			if(oFile.exists())
//			{
//				File oFiles[] = oFile.listFiles();
//				for(int i=0;i<oFile.length();i++)
//				{
//					File contorlMsgPersistFile = oFiles[i];
//					if(contorlMsgPersistFile.isFile())
//					{
//						
//							RecvFileControl recvFileControl = new RecvFileControl();
//							
//							recvFileControl.absoluteFileName = "";
//							//���ļ����ڲ��Ҷ�ȡ���ļ�������
//							RandomAccessFile writeAccess2 = new RandomAccessFile(contorlMsgPersistFile,"r");
//							byte buffer[] = new byte[(int) contorlMsgPersistFile.length()];
//							writeAccess2.readFully(buffer);
//							writeAccess2.close();
//							writeAccess2 = null;
//							//���������ļ������ݵ���Ӧ���ֶ�
//							try
//							{
//								recvFileControl.unPackMsgData(buffer);
//							}
//							catch(Exception exc)
//							{
//								continue;
//							}
//							
//							ReceveFileData receiveFileData = new ReceveFileData();
//							receiveFileData.msgid = ByteBuffer.HexToByte(contorlMsgPersistFile.getName());
//							receiveFileData.chunkcount = Integer.parseInt(recvFileControl.chunkCount);
//							receiveFileData.tradeCode = recvFileControl.tradeCode;
//							receiveFileData.fileLength = recvFileControl.
//						
//					}
//				}
//			}
// */
///**
// * @author jingwen.tong 2006-12-19 Copyright IBM 2005 ����MQ��Ϣ��ɶϵ����ص�ʵ����
// */
//public class MqTranRecvService {
//
//	private TransInfo transInfo = null;
//	// private MQConnectionInfo connectionInfo = null;
//	private byte[] msgid = null;
//	private Map hRecvFileControl = new HashMap();
//	private Object synObjectTrans = null;
//
//	public MqTranRecvService() {
//
//	}
//
//	public MqTranRecvService(byte[] msgid, TransInfo transInfo, Map hRecvFileControl, Object synObjectTrans) {
//
//		this.transInfo = transInfo;
//		this.connectionInfo = GlobalVar.connectionInfo;
//		this.msgid = msgid;
//
//		this.hRecvFileControl = hRecvFileControl;
//		this.synObjectTrans = synObjectTrans;
//	}
//
//	// ɾ��recvFileControl��Ӧ�Ķ������õ���Ϣ
//	private int deleteInvalidControlInfoFromQueue(MQConnection connection, RecvFileControl recvFileControl) {
//		int iRetVal = 0;
//		MQQueue fileTransFinishedControlQueue = null, fileTransControlQueue = null;
//		MQConnectionInfo connectionInfo = connection.getConnectionInfo();
//		MQConnectionPool connectionPool = MQConnectionPool.getSingleInstance();
//		boolean connectFlag = connectionInfo.getConnectFlag();
//		MQGetMessageOptions gmo = new MQGetMessageOptions();
//		MQMessage message1 = new MQMessage();
//		MQMessage message2 = new MQMessage();
//		try {
//			fileTransFinishedControlQueue = connection.getConnection().accessQueue(
//					GlobalVar.fileTransControlFinishQueueName,
//					MQC.MQOO_INPUT_SHARED | MQC.MQOO_INQUIRE | MQC.MQOO_FAIL_IF_QUIESCING, "", "", "mqm");
//			fileTransControlQueue = connection.getConnection().accessQueue(GlobalVar.fileTransControlQueueName,
//					MQC.MQOO_INPUT_SHARED | MQC.MQOO_INQUIRE | MQC.MQOO_FAIL_IF_QUIESCING, "", "", "mqm");
//			gmo.options = MQC.MQGMO_FAIL_IF_QUIESCING | MQC.MQGMO_NO_WAIT;
//			gmo.waitInterval = 300;
//			gmo.matchOptions = MQC.MQMO_MATCH_MSG_ID;
//			if (recvFileControl.receiverName.compareToIgnoreCase("") != 0) {
//				message2.correlationId = recvFileControl.receiverName.getBytes();
//				gmo.matchOptions = MQC.MQMO_MATCH_MSG_ID | MQC.MQMO_MATCH_CORREL_ID;
//			} else
//				gmo.matchOptions = MQC.MQMO_MATCH_MSG_ID;
//			message2.messageId = msgid;
//			message2.messageType = MQC.MQMT_DATAGRAM;
//			message2.format = MQC.MQFMT_NONE;
//			do {
//				try {
//					fileTransFinishedControlQueue.get(message2, gmo);
//					message2.clearMessage();
//				} catch (MQException exc) {
//					if (exc.completionCode == MQException.MQCC_FAILED) {
//						if (exc.reasonCode == MQException.MQRC_NO_MSG_AVAILABLE)
//							break;
//						else
//							throw exc;
//					}
//				}
//			} while (true);
//			gmo.options = MQC.MQGMO_FAIL_IF_QUIESCING | MQC.MQGMO_NO_WAIT;
//			gmo.waitInterval = 300;
//			gmo.matchOptions = MQC.MQMO_MATCH_MSG_ID;
//			message1.messageType = MQC.MQMT_DATAGRAM;
//			message1.format = MQC.MQFMT_NONE;
//			message1.messageId = msgid;
//			do {
//				try {
//					fileTransControlQueue.get(message1, gmo);
//					message1.clearMessage();
//				} catch (MQException exc) {
//					if (exc.completionCode == MQException.MQCC_FAILED) {
//						if (exc.reasonCode == MQException.MQRC_NO_MSG_AVAILABLE)
//							break;
//						else
//							throw exc;
//					}
//				}
//			} while (true);
//		} catch (MQException exc) {
//
//			if (exc.completionCode == MQException.MQCC_FAILED) {
//
//				if (exc.reasonCode == MQException.MQRC_CONNECTION_BROKEN
//						|| exc.reasonCode == MQException.MQRC_CONNECTION_QUIESCING
//						|| exc.reasonCode == MQException.MQRC_CONNECTION_STOPPING) {
//					iRetVal = 2;
//					// �ð����ӳ��е�����ȫ���������
//					connectionPool.destoryAllConnection(connectionInfo);
//				} else {
//					iRetVal = -1;
//				}
//
//			}
//
//		} catch (Exception exc) {
//			iRetVal = -1;
//
//		} finally {
//			try {
//				if (fileTransFinishedControlQueue != null)
//					fileTransFinishedControlQueue.close();
//
//				if (fileTransControlQueue != null)
//					fileTransControlQueue.close();
//			} catch (Exception exc) {
//
//			}
//		}
//		return iRetVal;
//	}
//
//	// �ж�msgid��Ӧ����Ϣ�Ƿ�ȷʵ������Ҫ���յ��ļ�����
//	private boolean isExistFileData(MQConnection connection, byte[] msgid, String msgSegNum, String queueName)
//			throws MQException, Exception {
//		boolean bRetVal = false;
//
//		MQQueue fileTransControlQueue = null;
//		MQConnectionInfo connectionInfo = connection.getConnectionInfo();
//		// MQConnectionPool connectionPool =
//		// MQConnectionPool.getSingleInstance();
//		boolean connectFlag = connectionInfo.getConnectFlag();
//		MQGetMessageOptions gmo = new MQGetMessageOptions();
//		MQMessage message = new MQMessage();
//		try {
//			fileTransControlQueue = connection.getConnection().accessQueue(queueName,
//					MQC.MQOO_BROWSE | MQC.MQOO_FAIL_IF_QUIESCING, "", "", "mqm");
//			gmo.options = MQC.MQGMO_FAIL_IF_QUIESCING | MQC.MQGMO_NO_WAIT | MQC.MQGMO_BROWSE_NEXT;
//			gmo.waitInterval = 300;
//			gmo.matchOptions = MQC.MQMO_MATCH_MSG_ID | MQC.MQMO_MATCH_CORREL_ID;
//			message.messageId = msgid;
//			message.correlationId = msgSegNum.getBytes();
//			message.messageType = MQC.MQMT_DATAGRAM;
//			message.format = MQC.MQFMT_NONE;
//			try {
//				fileTransControlQueue.get(message, gmo);
//				bRetVal = true;
//			} catch (MQException exc) {
//				if (exc.completionCode == MQException.MQCC_FAILED) {
//					if (exc.reasonCode == MQException.MQRC_NO_MSG_AVAILABLE) {
//						bRetVal = false;
//					} else
//						throw exc;
//				}
//				//
//			}
//		} catch (MQException exc) {
//
//			/*
//			 * if(exc.completionCode == MQException.MQCC_FAILED) {
//			 * 
//			 * if(exc.reasonCode == MQException.MQRC_CONNECTION_BROKEN ||
//			 * exc.reasonCode ==MQException.MQRC_CONNECTION_QUIESCING
//			 * ||exc.reasonCode == MQException.MQRC_CONNECTION_STOPPING) {
//			 * //�ð����ӳ��е�����ȫ���������
//			 * connectionPool.destoryAllConnection(connectionInfo); } }
//			 */
//			throw exc;
//
//		} catch (Exception exc) {
//			throw exc;
//		} finally {
//			try {
//				if (fileTransControlQueue != null)
//					fileTransControlQueue.close();
//			} catch (Exception exc) {
//			}
//		}
//		return bRetVal;
//
//	}
//
//	private void delInvalidMsgInfo(MQConnection connection, String queueName) throws Exception {
//		MQGetMessageOptions gmo = new MQGetMessageOptions();
//		MQQueue fileTransDataQueue = null;
//
//		MQMessage message = new MQMessage();
//
//		gmo.options = MQC.MQGMO_FAIL_IF_QUIESCING | MQC.MQGMO_NO_WAIT | MQC.MQGMO_ACCEPT_TRUNCATED_MSG;
//		gmo.waitInterval = 300;
//		gmo.matchOptions = MQC.MQMO_MATCH_MSG_ID;
//
//		fileTransDataQueue = connection.getConnection().accessQueue(queueName,
//				MQC.MQOO_INPUT_SHARED | MQC.MQOO_INQUIRE | MQC.MQOO_FAIL_IF_QUIESCING, "", "", "mqm");
//
//		/*
//		 * long segNumber = recvFileControl.recvedSegNum; if(segNumber==0)
//		 * segNumber++;
//		 */
//		try {
//			while (true) {
//				message.messageType = MQC.MQMT_DATAGRAM;
//				message.format = MQC.MQFMT_NONE;
//				message.messageId = msgid;
//				message.resizeBuffer(0);
//
//				// ��ȡ������Ϣ
//				try {
//					fileTransDataQueue.get(message, gmo);
//				} catch (MQException exc) {
//
//					if (exc.completionCode == MQException.MQCC_FAILED) {
//						if (exc.reasonCode == MQException.MQRC_NO_MSG_AVAILABLE)// ,
//						{
//							break;
//						}
//						/*
//						 * if(exc.reasonCode ==
//						 * MQException.MQRC_TRUNCATED_MSG_ACCEPTED) { continue;
//						 * }
//						 */
//						else
//							throw exc;
//					}
//					//
//				}
//			}
//		} catch (Exception exc) {
//			throw exc;
//		} finally {
//			try {
//				if (fileTransDataQueue != null)
//					fileTransDataQueue.close();
//
//			} catch (Exception exc) {
//
//			}
//		}
//	}
//
//	/*
//	 * private int delControlInfoFromQueue(MQConnection connection,String
//	 * receiverName) { int iRetVal = 0; MQQueue fileTransFinishedControlQueue =
//	 * null,fileTransControlQueue = null; MQConnectionInfo connectionInfo =
//	 * connection.getConnectionInfo(); MQConnectionPool connectionPool =
//	 * MQConnectionPool.getSingleInstance(); boolean connectFlag =
//	 * connectionInfo.getConnectFlag(); MQGetMessageOptions gmo = new
//	 * MQGetMessageOptions(); MQMessage message1 = new MQMessage (); MQMessage
//	 * message2 = new MQMessage ();
//	 * 
//	 * try {
//	 * 
//	 * // �Ѷ�������Ӧ����Ϣ��ɾ�� fileTransFinishedControlQueue =
//	 * connection.getConnection().accessQueue
//	 * (GlobalVar.fileTransControlFinishQueueName, MQC.MQOO_INPUT_SHARED
//	 * |MQC.MQOO_INQUIRE|MQC.MQOO_FAIL_IF_QUIESCING, "", "", "mqm");
//	 * fileTransControlQueue = connection.getConnection().accessQueue
//	 * (GlobalVar.fileTransControlQueueName, MQC.MQOO_INPUT_SHARED
//	 * |MQC.MQOO_INQUIRE|MQC.MQOO_FAIL_IF_QUIESCING, "", "", "mqm"); gmo.options
//	 * = MQC.MQGMO_FAIL_IF_QUIESCING|MQC.MQGMO_NO_WAIT; gmo.waitInterval = 300;
//	 * gmo.matchOptions = MQC.MQMO_MATCH_MSG_ID;
//	 * if(receiverName.compareToIgnoreCase("")!=0) { message2.correlationId =
//	 * receiverName.getBytes(); gmo.matchOptions =
//	 * MQC.MQMO_MATCH_MSG_ID|MQC.MQMO_MATCH_CORREL_ID; } else gmo.matchOptions =
//	 * MQC.MQMO_MATCH_MSG_ID; message2.messageId = msgid; message2.messageType =
//	 * MQC.MQMT_DATAGRAM; message2.format = MQC.MQFMT_NONE ; do { try {
//	 * fileTransFinishedControlQueue.get(message2,gmo); message2.clearMessage();
//	 * } catch (MQException exc) { if(exc.completionCode
//	 * ==MQException.MQCC_FAILED) { if(exc.reasonCode ==
//	 * MQException.MQRC_NO_MSG_AVAILABLE) break; else throw exc; } // } }
//	 * while(true); gmo.options = MQC.MQGMO_FAIL_IF_QUIESCING|MQC.MQGMO_NO_WAIT;
//	 * gmo.waitInterval = 300; gmo.matchOptions = MQC.MQMO_MATCH_MSG_ID;
//	 * message1.messageType = MQC.MQMT_DATAGRAM; message1.format =
//	 * MQC.MQFMT_NONE ; message1.messageId = msgid; do { try {
//	 * fileTransControlQueue.get(message1,gmo); message1.clearMessage(); } catch
//	 * (MQException exc) { if(exc.completionCode ==MQException.MQCC_FAILED) {
//	 * if(exc.reasonCode == MQException.MQRC_NO_MSG_AVAILABLE) break; else throw
//	 * exc; } // } } while(true); } catch (MQException exc) {
//	 * 
//	 * 
//	 * if(!connectFlag)
//	 * logImpl.error("�Ӷ��й�����:"+connectionInfo.getQueueManagerName()+
//	 * "�����ļ�����ȡ����Ӧ�Ŀ�����Ϣʧ��!" ,exc); else
//	 * logImpl.error("�Ӷ��й�����:"+connectionInfo.getQueueManagerName()+"hostname:"
//	 * +connectionInfo.getHostName()+"�˿ں�:"+connectionInfo.getPort()+
//	 * "�����ļ�����ȡ����Ӧ�Ŀ�����Ϣʧ��!" ,exc); if(exc.completionCode ==
//	 * MQException.MQCC_FAILED) {
//	 * 
//	 * if(exc.reasonCode == MQException.MQRC_CONNECTION_BROKEN || exc.reasonCode
//	 * ==MQException.MQRC_CONNECTION_QUIESCING ||exc.reasonCode ==
//	 * MQException.MQRC_CONNECTION_STOPPING) { iRetVal = 2; //�ð����ӳ��е�����ȫ���������
//	 * connectionPool.destoryAllConnection(connectionInfo); } else { iRetVal =
//	 * -1; }
//	 * 
//	 * }
//	 * 
//	 * } catch(Exception exc) { if(!connectFlag)
//	 * logImpl.error("�Ӷ��й�����:"+connectionInfo.getQueueManagerName()+
//	 * "�����ļ�����ȡ����Ӧ�Ŀ�����Ϣʧ��!" ,exc); else
//	 * logImpl.error("�Ӷ��й�����:"+connectionInfo.getQueueManagerName()+"hostname:"
//	 * +connectionInfo.getHostName()+"�˿ں�:"+connectionInfo.getPort()+
//	 * "�����ļ�����ȡ����Ӧ�Ŀ�����Ϣʧ��!" ,exc); iRetVal = -1;
//	 * 
//	 * 
//	 * } finally { try { if(fileTransFinishedControlQueue!=null)
//	 * fileTransFinishedControlQueue.close();
//	 * 
//	 * if(fileTransControlQueue!=null) fileTransControlQueue.close(); }
//	 * catch(Exception exc) { if(!connectFlag)
//	 * logImpl.error("�����й�����:"+connectionInfo.getQueueManagerName()+"�ر�"+
//	 * GlobalVar.fileTransControlFinishQueueName+"/"+GlobalVar.
//	 * fileTransControlQueueName+"����ʧ��!",exc); else
//	 * logImpl.error("�����й�����:"+connectionInfo.getQueueManagerName()+"hostname:"
//	 * +connectionInfo.getHostName()+"�˿ں�:"+connectionInfo.getPort()+"�ر�"+
//	 * GlobalVar.fileTransControlFinishQueueName+"/"+GlobalVar.
//	 * fileTransControlQueueName+"����ʧ��!",exc);
//	 * 
//	 * } } return iRetVal; }
//	 */
//	/*
//	 * 0:�ɹ� 1:�޷�ȡ����MQ���й����������� 2:��MQ���й�������������Ч������������ʧЧ����
//	 * 3:�޷������ļ�����Ϊ����Ӧ�Ķ����в�������Ӧ�ؿ�����Ϣ
//	 */
//	// ��"+GlobalVar.fileTransControlFinishQueueName+"���к�"+GlobalVar.fileTransControlQueueName+"������ȡ����Ӧ����Ϣ
//	private int getControlInfoFromQueue(MQConnection connection, String receiverName, RecvFileControl recvFileControl) {
//		int iRetVal = 0;
//		MQQueue fileTransFinishedControlQueue = null, fileTransControlQueue = null;
//		MQConnectionInfo connectionInfo = connection.getConnectionInfo();
//		MQConnectionPool connectionPool = MQConnectionPool.getSingleInstance();
//		boolean connectFlag = connectionInfo.getConnectFlag();
//		MQGetMessageOptions gmo = new MQGetMessageOptions();
//		MQMessage message1 = new MQMessage();
//		MQMessage message2 = new MQMessage();
//
//		try {
//			FileTransControlMsg fileTransControlMsg = new FileTransControlMsg();
//			fileTransFinishedControlQueue = connection.getConnection().accessQueue(
//					GlobalVar.fileTransControlFinishQueueName, MQC.MQOO_BROWSE | MQC.MQOO_FAIL_IF_QUIESCING, "", "",
//					"mqm");
//			fileTransControlQueue = connection.getConnection().accessQueue(GlobalVar.fileTransControlQueueName,
//					MQC.MQOO_BROWSE | MQC.MQOO_FAIL_IF_QUIESCING, "", "", "mqm");
//			// �������Ӧ�ص���Ϣ
//			gmo.options = MQC.MQGMO_FAIL_IF_QUIESCING | MQC.MQGMO_NO_WAIT | MQC.MQGMO_BROWSE_NEXT;
//			gmo.waitInterval = 300;
//			gmo.matchOptions = MQC.MQMO_MATCH_MSG_ID;
//			message1.messageType = MQC.MQMT_DATAGRAM;
//			message1.format = MQC.MQFMT_NONE;
//			message1.messageId = msgid;
//			try {
//				fileTransControlQueue.get(message1, gmo);
//				message1.readFully(fileTransControlMsg.getBuffer());
//				fileTransControlMsg.unPackMsgData(fileTransControlMsg.getBuffer());
//				// recvFileControl.absoluteFileName
//				recvFileControl.chunkCount = fileTransControlMsg.chunkCount;
//				recvFileControl.chunkFileName = fileTransControlMsg.chunkFileName;
//				recvFileControl.chunkNum = fileTransControlMsg.chunkNum;
//				recvFileControl.dataSize = fileTransControlMsg.dataSize;
//				recvFileControl.dirName = fileTransControlMsg.dirName;
//				recvFileControl.fileName = fileTransControlMsg.fileName;
//				recvFileControl.hostName = fileTransControlMsg.hostName;
//				recvFileControl.ipAddress = fileTransControlMsg.ipAddress;
//				recvFileControl.offsetFile = fileTransControlMsg.offsetFile;
//				recvFileControl.queueName = fileTransControlMsg.queueName;
//				recvFileControl.receiverName = fileTransControlMsg.receiverName;
//				recvFileControl.segSize = fileTransControlMsg.segSize;
//				recvFileControl.tradeCode = fileTransControlMsg.tradeCode;
//				message1.clearMessage();
//
//			} catch (MQException exc) {
//				if (exc.completionCode == MQException.MQCC_FAILED) {
//					if (exc.reasonCode == MQException.MQRC_NO_MSG_AVAILABLE) {
//						iRetVal = 3;
//					} else
//						throw exc;
//				}
//				//
//			}
//			gmo.options = MQC.MQGMO_FAIL_IF_QUIESCING | MQC.MQGMO_NO_WAIT | MQC.MQGMO_BROWSE_NEXT;
//			gmo.waitInterval = 300;
//			if (receiverName.compareToIgnoreCase("") != 0) {
//				message2.correlationId = receiverName.getBytes();
//				gmo.matchOptions = MQC.MQMO_MATCH_MSG_ID | MQC.MQMO_MATCH_CORREL_ID;
//			} else
//				gmo.matchOptions = MQC.MQMO_MATCH_MSG_ID;
//			message2.messageId = msgid;
//			message2.messageType = MQC.MQMT_DATAGRAM;
//			message2.format = MQC.MQFMT_NONE;
//			try {
//				fileTransFinishedControlQueue.get(message2, gmo);
//				byte chunkCount[] = new byte[Constants.CHUNKCOUNT_LENGTH];
//				byte chunkNum[] = new byte[Constants.CHUNKNUM_LENGTH];
//				byte desc[] = new byte[Constants.DESC_LENGTH];
//				byte tradeCode[] = new byte[Constants.TRADECODE_LENGTH];
//				ByteBuffer.memset(tradeCode, (byte) ' ');
//				ByteBuffer.memset(chunkCount, (byte) ' ');
//				ByteBuffer.memset(chunkNum, (byte) ' ');
//				ByteBuffer.memset(desc, (byte) ' ');
//
//				message2.readFully(chunkCount);
//				message2.readFully(chunkNum);
//				message2.readFully(desc);
//				message2.readFully(tradeCode);
//				recvFileControl.desc = (new String(desc)).trim();
//			} catch (MQException exc) {
//				if (exc.completionCode == MQException.MQCC_FAILED) {
//					if (exc.reasonCode == MQException.MQRC_NO_MSG_AVAILABLE) {
//						iRetVal = 3;
//					} else
//						throw exc;
//				}
//				//
//			}
//			if (iRetVal == 0) {
//				hRecvFileControl.put(msgid, recvFileControl);
//				// ��ʼ�Ѵ���Ϣ���־û���
//				if (GlobalVar.isControlMsgPersist) {
//					File contorlMsgPersistFile = new File(GlobalVar.tempPath + "/recv/" + ByteBuffer.ByteToHex(msgid));
//					RandomAccessFile writeAccess1 = new RandomAccessFile(contorlMsgPersistFile, "rw");
//					writeAccess1.seek(0);
//					writeAccess1.write(recvFileControl.packMsgData());
//					writeAccess1.close();
//				}
//			}
//			/*
//			 * fileTransFinishedControlQueue.close();
//			 * fileTransControlQueue.close();
//			 */
//
//		} catch (MQException exc) {
//
//			if (exc.completionCode == MQException.MQCC_FAILED) {
//
//				if (exc.reasonCode == MQException.MQRC_CONNECTION_BROKEN
//						|| exc.reasonCode == MQException.MQRC_CONNECTION_QUIESCING
//						|| exc.reasonCode == MQException.MQRC_CONNECTION_STOPPING) {
//					iRetVal = 2;
//					// �ð����ӳ��е�����ȫ���������
//					connectionPool.destoryAllConnection(connectionInfo);
//				} else {
//					iRetVal = -1;
//				}
//
//			}
//
//		} catch (Exception exc) {
//			iRetVal = -1;
//		} finally {
//			try {
//				if (fileTransFinishedControlQueue != null)
//					fileTransFinishedControlQueue.close();
//
//				if (fileTransControlQueue != null)
//					fileTransControlQueue.close();
//			} catch (Exception exc) {
//			}
//		}
//		return iRetVal;
//	}
//
//	private List getReceiveFileList(MQConnection connection, String receiverName, int count) {
//		MQQueue fileTransFinishedControlQueue = null;
//		List receiveList = new LinkedList();
//		MQConnectionInfo connectionInfo = connection.getConnectionInfo();
//		MQConnectionPool connectionPool = MQConnectionPool.getSingleInstance();
//		boolean connectFlag = connectionInfo.getConnectFlag();
//		MQGetMessageOptions gmo = new MQGetMessageOptions();
//		MQMessage message = new MQMessage();
//		List receiveListTmp = null;
//		Map receiveMap = new HashMap();
//		int iNum = 0;
//		// List shaValue = new LinkedList();
//		try {
//			fileTransFinishedControlQueue = connection.getConnection().accessQueue(
//					GlobalVar.fileTransControlFinishQueueName, MQC.MQOO_BROWSE | MQC.MQOO_FAIL_IF_QUIESCING, "", "",
//					"mqm");
//			// ȡ�����е���Ϣ
//			gmo.options = MQC.MQGMO_FAIL_IF_QUIESCING | MQC.MQGMO_NO_WAIT | MQC.MQGMO_BROWSE_NEXT;
//			gmo.matchOptions = MQC.MQMO_NONE;
//			gmo.waitInterval = 300;
//			do {
//
//				if (receiverName != null) {
//					message.correlationId = receiverName.getBytes();
//					gmo.matchOptions = gmo.matchOptions | MQC.MQMO_MATCH_CORREL_ID;
//				}
//				message.messageType = MQC.MQMT_DATAGRAM;
//				message.format = MQC.MQFMT_NONE;
//
//				try {
//					fileTransFinishedControlQueue.get(message, gmo);
//
//				} catch (MQException exc) {
//					break;
//				}
//				// String msgid = (new String(message.messageId)).trim();
//				ReceveFileData receiveFileData = new ReceveFileData();
//				receiveFileData.msgid = message.messageId;
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
//				message.readFully(chunkCount);
//				message.readFully(chunkNum);
//				message.readFully(desc);
//				message.readFully(tradeCode);
//				message.readFully(fileLength1);
//				receiveFileData.chunkcount = Integer.parseInt((new String(chunkCount)).trim());
//				receiveFileData.desc = (new String(desc)).trim();
//				receiveFileData.tradeCode = (new String(tradeCode)).trim();
//				receiveFileData.fileLength = Long.parseLong((new String(fileLength1)).trim());
//				byte[] shaValue = new byte[22];
//
//				ByteBuffer.memcpy(shaValue, 0, receiveFileData.msgid, 0, 22);
//
//				String shaValue1 = ByteBuffer.ByteToHex(shaValue);
//				// shaValue =
//				// msgid.substring(0,msgid.length()-Constants.CHUNKNUM_LENGTH);
//				/*
//				 * for (Iterator i = receiveMap.keySet().iterator();
//				 * i.hasNext();) { String shaValue2 = (String)i.next();
//				 * if(shaValue2.compareTo(shaValue1)==0) { shaValue1 =
//				 * shaValue2; } }
//				 */
//				receiveListTmp = (List) receiveMap.get(shaValue1);
//				if (receiveListTmp == null) {
//					receiveListTmp = new LinkedList();
//					receiveListTmp.add(receiveFileData);
//					receiveMap.put(shaValue1, receiveListTmp);
//				} else
//					receiveListTmp.add(receiveFileData);
//				message.clearMessage();
//				// gmo.options =
//				// MQC.MQGMO_FAIL_IF_QUIESCING|MQC.MQGMO_NO_WAIT|MQC.MQGMO_BROWSE_NEXT;
//				//
//				iNum++;
//				if (iNum > count)
//					break;
//			} while (true);
//
//			//
//
//			// ���˳��ܹ��������ص��ļ��б�
//			for (Iterator i = receiveMap.keySet().iterator(); i.hasNext();) {
//				String index = (String) i.next();
//				receiveListTmp = (List) receiveMap.get(index);
//				ReceveFileData receiveFileData = (ReceveFileData) receiveListTmp.get(0);
//				if (receiveFileData.chunkcount == receiveListTmp.size())// ��Ҫ��Ӧ�Էֶ��ϴ������
//				{
//					receiveList.add(receiveFileData);
//				}
//
//			}
//
//			receiveMap = null;
//
//		} catch (MQException exc) {
//			if (exc.completionCode == MQException.MQCC_FAILED) {
//
//				if (exc.reasonCode == MQException.MQRC_CONNECTION_BROKEN
//						|| exc.reasonCode == MQException.MQRC_CONNECTION_QUIESCING
//						|| exc.reasonCode == MQException.MQRC_CONNECTION_STOPPING) {
//
//					// �ð����ӳ��е�����ȫ���������
//					connectionPool.destoryAllConnection(connectionInfo);
//				}
//			}
//
//		} catch (Exception exc) {
//
//		} finally {
//			try {
//				if (fileTransFinishedControlQueue != null)
//					fileTransFinishedControlQueue.close();
//			} catch (Exception exc) {
//			}
//		}
//		return receiveList;
//
//	}
//
//	/*
//	 * �õ��ܹ����յ��ļ������б�
//	 */
//	public List getReceiveFileList(String receiverName, int count) {
//		List receiveList = null;
//
//		MQConnection connection = null;
//		// 1.ȡ����MQ������
//		MQConnectionPool connectionPool = MQConnectionPool.getSingleInstance();
//
//		try {
//			connection = connectionPool.getConnection(GlobalVar.connectionInfo);
//
//			if (connection == null) {
//
//				return null;
//			}
//			receiveList = getReceiveFileList(connection, receiverName, count);
//		}
//
//		catch (Exception exc) {
//
//		} finally {
//			connectionPool.freeConnection(connection);
//		}
//		return receiveList;
//	}
//
//	/*
//	 * 0:�ɹ� 1:�޷�ȡ����MQ���й����������� 2:��MQ���й�������������Ч������������ʧЧ����
//	 * 3:�޷������ļ�����Ϊ����Ӧ�Ķ����в�������Ӧ�ؿ�����Ϣ 4:�޷������ļ�����Ϊ����Ӧ�����ݶ����в�������Ӧ���ļ�������Ϣ
//	 */
//	public int recvFile(String receiverName)
//	{
//		//String fileName = null;
//		MQConnection connection = null;
//		MQConnectionInfo connectionInfo = null;
//		MQConnectionPool connectionPool = MQConnectionPool.getSingleInstance();
//		RecvFileControl recvFileControl = null;//new SendFileControl()
//		RandomAccessFile writeAccess2  = null,writeAccess1 = null;
//		File contorlMsgPersistFile = null;
//		MQQueue			fileTransDataQueue = null;
//		int iRetVal = 0;
//		try
//		{	
//			//1.�ж���ǰ�����˶���
//			//�ж��ڴ����Ƿ��Ѿ���������Ӧ��recvFileControl,����ڴ��в����ڵĻ����ӳ־û����ļ�ϵͳ�е��ļ���ȡ��Ӧ������
//			boolean flag = false;
//			
////			2.ȡ����MQ������
//			connection = connectionPool.getConnection(GlobalVar.connectionInfo);
//			
//			if(connection==null)
//			{
//				iRetVal = 1;
//				return iRetVal;
//			}
//			connectionInfo = connection.getConnectionInfo();
//			if(GlobalVar.isControlMsgPersist)
//			{
//				contorlMsgPersistFile = new File(GlobalVar.tempPath+"/recv/"+ByteBuffer.ByteToHex(msgid));
//				if(contorlMsgPersistFile.exists())
//				{
//					recvFileControl = new RecvFileControl();
//					
//					recvFileControl.absoluteFileName = "";
//					//���ļ����ڲ��Ҷ�ȡ���ļ�������
//					writeAccess2 = new RandomAccessFile(contorlMsgPersistFile,"r");
//					byte buffer[] = new byte[(int) contorlMsgPersistFile.length()];
//					writeAccess2.readFully(buffer);
//					writeAccess2.close();
//					writeAccess2 = null;
//					//���������ļ������ݵ���Ӧ���ֶ�
//					recvFileControl.unPackMsgData(buffer);
//					synchronized(synObjectTrans)
//					{
//						hRecvFileControl.put(msgid,recvFileControl);
//					}
//					flag = true;
//				}
//				contorlMsgPersistFile = null;
//			}
//			if(!flag)
//			{
//				synchronized(synObjectTrans)
//				{
//					recvFileControl = (RecvFileControl)hRecvFileControl.get(msgid);
//					if(recvFileControl==null)
//					{
//						recvFileControl = new RecvFileControl();
//						recvFileControl.absoluteFileName = "";
//						//��Ҫ��MQ����Ӧ�Ķ�����ȡ����Щ��Ϣ
//						iRetVal = getControlInfoFromQueue(connection,receiverName,recvFileControl);
//						if(iRetVal!=0)
//							return iRetVal;
//						/*if(!isExistFileData(connection,msgid,"1",recvFileControl.queueName))
//						{
//							contorlMsgPersistFile = new File(GlobalVar.tempPath+"/recv/"+ByteBuffer.ByteToHex(msgid));
//							contorlMsgPersistFile.delete();
//							return 4;
//						}
//						*/
//					}
//				
//				}
//			}
//			transInfo.setTotalReceivedSize(recvFileControl.recvedDataSize);
//			
//			
//			if(iRetVal!=0)
//				return iRetVal;
//			
//			if(recvFileControl.isFinished)//�Ѿ����������.����Ҫ�ٽ�����
//			{
//				transInfo.setTotalReceivedSize(recvFileControl.recvedDataSize);
//				delInvalidMsgInfo(connection,recvFileControl.queueName);
//				iRetVal = deleteInvalidControlInfoFromQueue(connection,recvFileControl);
//				if(iRetVal==0)
//				{
//					//ɾ�����ļ�
//					contorlMsgPersistFile = new File(GlobalVar.tempPath+"/recv/"+ByteBuffer.ByteToHex(msgid));
//					contorlMsgPersistFile.delete();
//				}
//				return iRetVal;
//			}
//
//			
//			//�ж�fileTransControlMsg��Ӧ����Ϣ�Ƿ���"+GlobalVar.fileTransControlQueueName+"�����д���
//	
//			
//			int segSize = Integer.parseInt(recvFileControl.segSize);
//			long transFileDataSize = Long.parseLong(recvFileControl.dataSize);//Ҫ������ļ������ݴ�С
//			//ȡ���Ѿ�������ļ���С
//			
//			
//			int recvDataLength = 0;//ÿ����Ҫ���յ����ݳ���
//			/*
//			 * 
//			 */
//			//------- begin 
//			String newDirName = CommonFun.replace(recvFileControl.dirName,"\\","/");
//			/*byte[] dirNameBytes = newDirName.getBytes();
//			int length = dirNameBytes.length;
//			while(true)
//			{
//				
//				if(dirNameBytes[length-1]==(byte)'/')
//					length--;
//				else
//					break;
//			}
//			String newDirName1 = newDirName.substring(0,length);
//			*/
//			int offset = newDirName.indexOf('/');
//			String dirNameTmp = newDirName.substring(offset+1,newDirName.length());
//			//-------- end
//			
//			//String dirNameTmp = recvFileControl.dirName.substring(recvFileControl.dirName.lastIndexOf("/"),recvFileControl.dirName.length()-recvFileControl.dirName.lastIndexOf("/"));
//			
//			String hostName = recvFileControl.hostName;
//			String hostAddress = recvFileControl.ipAddress;
//			File fileTmp = new File(GlobalVar.tempPath+"/recv/"+hostName+"/"+hostAddress+"/"+dirNameTmp);
//			if(!fileTmp.exists())
//			{
//				fileTmp.mkdirs();
//			}
//			File recvFile = new File(fileTmp.getAbsolutePath()+"/"+recvFileControl.chunkFileName);
//			if(!recvFile.exists())
//			{
//				if(!recvFile.createNewFile())
//					throw new Exception("���ܴ������ļ�:"+recvFile.getAbsolutePath());
//			}
//			else
//			{
//				if(!recvFile.canWrite())
//				{
//					
//					throw new Exception("�ļ���"+recvFile.getAbsolutePath()+"����д��");
//				}
//			}
//			recvFileControl.absoluteFileName = recvFile.getAbsolutePath();
//			writeAccess2 = new RandomAccessFile(recvFile,"rw");	
//			if(GlobalVar.isControlMsgPersist)
//			{
//				contorlMsgPersistFile = new File(GlobalVar.tempPath+"/recv/"+ByteBuffer.ByteToHex(msgid));
//				writeAccess1 = new RandomAccessFile(contorlMsgPersistFile,"rw");	
//			}
//			byte recvBuffer[] = new byte[segSize];
//			
//			if(Integer.parseInt(recvFileControl.chunkCount)==1)
//			
//			//long seekOffset = recvFileControl.recvedDataSize;
//			//long startTime;
//			
//	    	MQMessage message = new MQMessage ();
//	    	MQGetMessageOptions gmo = new MQGetMessageOptions();
//			gmo.options = MQC.MQGMO_FAIL_IF_QUIESCING|MQC.MQGMO_NO_WAIT|MQC.MQGMO_SYNCPOINT;
//			gmo.waitInterval = 300;
//			gmo.matchOptions = MQC.MQMO_MATCH_MSG_ID|MQC.MQMO_MATCH_CORREL_ID;
//	    	
//	    	fileTransDataQueue = connection.getConnection().accessQueue (recvFileControl.queueName, 
//	    			MQC.MQOO_INPUT_SHARED |MQC.MQOO_INQUIRE|MQC.MQOO_FAIL_IF_QUIESCING, "", "", "mqm");
//	    	
//	    	/*long segNumber = recvFileControl.recvedSegNum;
//	    	if(segNumber==0)
//	    		segNumber++;
//	    	*/
//	    	byte msgDataSize[] = new byte[Constants.MSGDATASIZE_LENGTH];
//	    	byte fileOffset[] = new byte[Constants.OFFSET_LENGTH];
//	    	long startTime = 0;
//			while(recvFileControl.recvedDataSize!=transFileDataSize)//ѭ����������
//			{
//				message.messageType = MQC.MQMT_DATAGRAM;
//	    		message.format = MQC.MQFMT_NONE ;
//	    		message.messageId = msgid;
//	    		recvFileControl.recvedSegNum++;
//	    		message.correlationId = Long.toString(recvFileControl.recvedSegNum).getBytes();
//				//
//				ByteBuffer.memset(msgDataSize,(byte)' ');
//	    		ByteBuffer.memset(fileOffset,(byte)' ');
//	    		ByteBuffer.memset(recvBuffer,(byte)' ');
//				//��ȡ������Ϣ
//	    		try
//	    		{
//	    			startTime = System.currentTimeMillis();
//	    			fileTransDataQueue.get(message,gmo);
//	    		}
//	    		catch (MQException exc)
//        		{
//	    			recvFileControl.recvedSegNum--;
//    				if(exc.completionCode ==MQException.MQCC_FAILED)
//    				{
//    					if(exc.reasonCode == MQException.MQRC_NO_MSG_AVAILABLE)//����������ȱʧ,
//    					{
//    						recvFileControl.recvedSegNum++;
//    						//д���ļ�ϵͳ��
//    						LogService.Log("error",recvFileControl.desc+" ȱʧ��:"+recvFileControl.recvedSegNum+"������!");
//    						continue;
//    					}
//    					else
//    						throw exc;
//    				}
//        			//
//        		}
//	    		transInfo.setTotalReceivedSize(recvDataLength);
//				
//	    		transInfo.setTotalReceivedTime(System.currentTimeMillis()-startTime);
//				message.readFully(msgDataSize);
//				message.readFully(fileOffset);
//				recvDataLength = Integer.parseInt((new String(msgDataSize)).trim());
//				message.readFully(recvBuffer,0,recvDataLength);
//				message.clearMessage();
//				writeAccess2.seek(Long.parseLong((new String(fileOffset)).trim()));
//				writeAccess2.write(recvBuffer,0,recvDataLength);
//				
//				
//				
//				//���ͳɹ�
//				
//				//transInfo.setTotalSendedSize((long)sendDataLength);
//				//recvFileControl.recvedSegNum ++;
//				recvFileControl.recvedDataSize += recvDataLength;
//				if(GlobalVar.isControlMsgPersist)
//				{
//					//�Ѵ���Ϣ�־û����ļ�ϵͳ��
//					writeAccess1.seek(0);
//					writeAccess1.write(recvFileControl.packMsgData());
//				}				
//			}
//			fileTransDataQueue.close();
//			fileTransDataQueue = null;
//			recvFileControl.isFinished = true;
//			if(GlobalVar.isControlMsgPersist)
//			{
//				//�Ѵ���Ϣ�־û����ļ�ϵͳ��
//				writeAccess1.seek(0);
//				writeAccess1.write(recvFileControl.packMsgData());
//			}	
//			//ɾ�����ݶ��ж��ڵ���Ϣ
//			delInvalidMsgInfo(connection,recvFileControl.queueName);
////			ɾ��recvFileControl��Ӧ�Ķ������õ���Ϣ
//			iRetVal = deleteInvalidControlInfoFromQueue(connection,recvFileControl);
//			
//			
//			
//			
//		}
//		catch (MQException exc)
//		{
//			iRetVal =-1;
//			boolean connectFlag = connection.getConnectionInfo().getConnectFlag();
//    		if(exc.completionCode == MQException.MQCC_FAILED)
//    		{
//    			
//    			if(exc.reasonCode == MQException.MQRC_CONNECTION_BROKEN || exc.reasonCode ==MQException.MQRC_CONNECTION_QUIESCING 
//    					||exc.reasonCode == MQException.MQRC_CONNECTION_STOPPING)
//    			{
//    				iRetVal = 2;
//    				//�ð����ӳ��е�����ȫ���������
//    				connectionPool.destoryAllConnection(connectionInfo);
//    			}
//    		}
//    		
//		}
//		catch(Exception exc)
//		{
//			iRetVal =-1;
//			
//			if(GlobalVar.isControlMsgPersist)
//			{
//				if(writeAccess1!=null)
//				{
//					try
//					{
//					//�Ѵ���Ϣ�־û����ļ�ϵͳ��
//						writeAccess1.seek(0);
//						writeAccess1.write(recvFileControl.packMsgData());
//					}
//					catch(Exception exc1)
//					{
//						
//					}
//				}
//			}
//		}
//		finally
//		{
//			try
//			{
//				if(fileTransDataQueue!=null)
//				{
//					try
//					{
//						fileTransDataQueue.close();
//					}
//					catch(Exception exc)
//					{
//						
//					}
//				}
//				if(writeAccess2!=null)
//				{
//					try
//					{
//						writeAccess2.close();
//					}
//					catch(Exception exc)
//					{
//						
//					}
//				}
//				if(writeAccess1!=null)
//				{
//					try
//					{
//						
//						writeAccess1.close();
//					}
//					catch(Exception exc)
//					{
//						
//					}
//				}
//			}
//			catch(Exception exc)
//			{
//				logImpl.error(exc);
//			}
//			connectionPool.freeConnection(connection);
//		}
//		
//		return iRetVal;
//	}
//}
