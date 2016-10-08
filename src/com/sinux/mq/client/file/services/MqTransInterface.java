package com.sinux.mq.client.file.services;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.sinux.mq.client.MqConnectionFactory;
import com.sinux.mq.client.mod.Constants;
import com.sinux.mq.client.mod.FileTransControlMsg;
import com.sinux.mq.client.mod.SendFileControl;
import com.sinux.mq.client.mod.TransInfo;
import com.sinux.mq.client.util.ByteBuffer;
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
	 * -6:û���㹻�Ŀ����߳��������ֿ鴫�䣬�����ʱ������ -5:û�п��еķ����̷߳��� -4:Ҫ���͵��ļ����ܹ���ȡ�����ܹ�����
	 * -3:Ҫ���͵Ĳ����ļ� -2:Ҫ���͵��ļ������� -1:ʧ�� 0:�ɹ� 1:�޷�ȡ����MQ���й�����������
	 * 2:��MQ���й�������������Ч������������ʧЧ����
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public int sendFile(String absolutFileName, String receiverName, TransInfo transInfo) {
		int iRetVal = 0;
		List serviceThreadList = new LinkedList();
		try {
			// logImpl.info("��ʼ׼���ļ�:"+absolutFileName+"���͵�ǰ��׼������!");
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
			if (GlobalVar.isStartChunk) {// ��Ҫ�����ֿ鴫�����
				// �����Ҫ��Ϊ�˱��ⲻ����ļ�ҲҪ�����ֿ鴫�����
				if (fileLength > (GlobalVar.filesizeChunk * 1024)) {// �ļ���С����Ҫ���зֿ�Ĵ�С
					serviceThreadNum = GlobalVar.chunknum;
				}
			}
			ThreadPool threadPool = ThreadPool.getSingleInstance();
			ComplexEvent complexEvent = null;
			synchronized (GlobalVar.synObject) {
				// û�п��õ��߳̽��з���
				idleServiceThreadNum = threadPool.getIdleThreadNum();
				if (idleServiceThreadNum <= 0) {
					return -5;
				}
				// û���㹻�Ŀ����߳��������ֿ鴫�䣬�����ʱ������
				if (serviceThreadNum > idleServiceThreadNum) {
					return -6;
				}
				complexEvent = new ComplexEvent(serviceThreadNum);
				// ��ʼȡ�����еķ����̳߳������Կ�ʼ���з���
				for (int i = 0; i < serviceThreadNum; i++) {
					ServiceThread serviceThread = threadPool.getIdleServiceThread();
					serviceThreadList.add(serviceThread);
				}
			}
			// logImpl.info("��ʼ�����ļ�:"+absolutFileName+"��SHAֵ!");
			InetAddress inetAddress = InetAddress.getLocalHost();
			String hostName = inetAddress.getHostName();
			String hostAddress = inetAddress.getHostAddress();
			byte[] shaValueTmp = null;
			synchronized (GlobalVar.synObject) {
				GlobalVar.messageDigest.update((absolutFileName + hostName + hostAddress).getBytes("ISO-8859-1"));
				shaValueTmp = GlobalVar.messageDigest.digest();
			}
			byte[] shaValueBytes = new byte[24];
			ByteBuffer.memset(shaValueBytes, (byte) ' ');
			ByteBuffer.memcpy(shaValueBytes, 0, "00".getBytes(), 0, Constants.CHUNKNUM_LENGTH);
			ByteBuffer.memcpy(shaValueBytes, Constants.CHUNKNUM_LENGTH, shaValueTmp, 0, Constants.DIGEST_LENGTH);// shaValueTmp.length);

			// ��ʼ׼������
			// logImpl.info("��ʼ׼���ļ�:"+absolutFileName+"�Ĵ���!");
			List fileTransControlMsgList = new LinkedList();
			List msgidList = new LinkedList();
			long offsetFile = 0;
			if (serviceThreadNum > 1) {
				// ��ʼ���ļ����зֶ�
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
			// ��ʼ�����߳�
			// logImpl.info("��ʼ���������߳��������ļ�:"+absolutFileName+"!");
			for (int i = 0; i < serviceThreadNum; i++) {
				byte[] msgid = new byte[24];
				ByteBuffer.memset(msgid, (byte) ' ');
				ServiceThread serviceThread = (ServiceThread) serviceThreadList.get(i);
				FileTransControlMsg fileTransControlMsg = (FileTransControlMsg) fileTransControlMsgList.get(i);
				if (fileTransControlMsg.chunkNum.length() < Constants.CHUNKNUM_LENGTH) {
					fileTransControlMsg.chunkNum = "0" + fileTransControlMsg.chunkNum;
				}
				ByteBuffer.memcpy(msgid, 0, shaValueBytes, 0, shaValueBytes.length);
				ByteBuffer.memcpy(msgid, Constants.CHUNKNUM_LENGTH + Constants.DIGEST_LENGTH,
						fileTransControlMsg.chunkNum.getBytes(), 0, Constants.CHUNKNUM_LENGTH);
				// byte[] msgid = shaValueBytes
				// ;//shaValue+fileTransControlMsg.chunkNum;
				msgidList.add(msgid);
				serviceThread.prepareSend(GlobalVar.hSendFileControl, GlobalVar.synObjectSend, fileTransControlMsg,
						transInfo, complexEvent, msgid, factory);
			}
			// logImpl.info("��ʼ�ȴ�"+serviceThreadNum+"�������̴߳����ļ�:"+absolutFileName+"����!");
//			int statusValue = complexEvent.WaitForSingleObject(-1);
			// ��ʼ�жϷ��صĽ��
			// logImpl.info("��ʼ����"+serviceThreadNum+"�������̴߳����ļ�:"+absolutFileName+"�ķ��ؽ��!");
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
				// ����ȫ��ɾ��
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
	 * �õ��ܹ����յ��ļ������б�
	 */
	@SuppressWarnings("rawtypes")
	public List getReceiveFileList(String receiverName, int count) {
//		 MqTranRecvService tranRecvService = new MqTranRecvService();
//		 return tranRecvService.getReceiveFileList(receiverName,count);
		return null;
	}
	/*
	 * ���մ˶����Ӧ���ļ�����,���ؽ��յ����ļ���
	 */
	// public String recvFile(ReceveFileData receveFileData,String
	// dirName,String receiverName,TransInfo transInfo,List errorList)
	// {
	// String fileName = null;
	// ComplexEvent complexEvent = null;
	// List serviceThreadList = new LinkedList();
	// try
	// {
	// if(receiverName==null)
	// receiverName="";
	// int serviceThreadNum = receveFileData.chunkcount,idleServiceThreadNum =
	// 0;
	// ThreadPool threadPool = ThreadPool.getSingleInstance();
	// synchronized(GlobalVar.synObject)
	// {
	// //û�п��õ��߳̽��з���
	// idleServiceThreadNum = threadPool.getIdleThreadNum();
	// if(idleServiceThreadNum<=0)
	// {
	// logImpl.error("�����ļ�:"+receveFileData.desc+"���ִ���,������Ϣ:û�п��õ��߳̽��з���!");
	// return null;
	// }
	// //û���㹻�Ŀ����߳��������ֿ鴫�䣬�����ʱ������
	// if (serviceThreadNum>idleServiceThreadNum)
	// {
	// logImpl.error("�����ļ�:"+receveFileData.desc+"���ִ���,������Ϣ:û���㹻�Ŀ����߳��������ֿ鴫�䣬�����ʱ������!");
	//
	// return null;
	// }
	// complexEvent = new ComplexEvent(serviceThreadNum);
	// //��ʼȡ�����еķ����̳߳������Կ�ʼ���з���
	// for(int i=0;i<serviceThreadNum;i++)
	// {
	// ServiceThread serviceThread = threadPool.getIdleServiceThread();
	// serviceThreadList.add(serviceThread);
	// }
	// }
	// //TransInfo transInfo = new TransInfo();
	// List msgidList = new LinkedList();
	// //��ʼ�����߳�
	// logImpl.info("��ʼ���������߳��������ļ�:"+receveFileData.desc+"!");
	// for(int i=0;i<serviceThreadNum;i++)
	// {
	// ServiceThread serviceThread = (ServiceThread)serviceThreadList.get(i);
	// byte[] msgid = new byte[24];
	// ByteBuffer.memset(msgid,(byte)' ');
	// ByteBuffer.memcpy(msgid,0,receveFileData.msgid,0,Constants.CHUNKNUM_LENGTH+Constants.DIGEST_LENGTH);
	// //String msgid =
	// receveFileData.msgid.substring(0,receveFileData.msgid.length()-2);
	// int chunkNum = 0;
	// if(serviceThreadNum==1)
	// chunkNum =0;
	// else
	// chunkNum = i+1;
	// if(Integer.toString(chunkNum).length()<2)
	// {
	// ByteBuffer.memcpy(msgid,Constants.CHUNKNUM_LENGTH+Constants.DIGEST_LENGTH,
	// ("0"+Integer.toString(chunkNum)).getBytes(),0,2);
	//
	// }
	// else
	// ByteBuffer.memcpy(msgid,Constants.CHUNKNUM_LENGTH+Constants.DIGEST_LENGTH,
	// (Integer.toString(chunkNum)).getBytes(),0,2);
	//
	//
	// msgidList.add(msgid);
	// serviceThread.prepareRecv(GlobalVar.hRecvFileControl,GlobalVar.synObjectRecv,
	// transInfo,complexEvent,msgid,receiverName);
	//
	// }
	// logImpl.info("��ʼ�ȴ�"+serviceThreadNum+"�������߳̽����ļ�:"+receveFileData.desc+"����!");
	// int statusValue = complexEvent.WaitForSingleObject(-1);
	// //��ʼ�жϷ��صĽ��
	// logImpl.info("��ʼ����"+serviceThreadNum+"�������߳̽����ļ�:"+receveFileData.desc+"�ķ��ؽ��!");
	// boolean successFlag =true;
	// boolean isThrowException = false;
	// for (Iterator i = msgidList.iterator(); i.hasNext();)
	// {
	// byte[] msgid = (byte[])i.next();
	//
	//
	// synchronized(GlobalVar.synObjectRecv)
	// {
	// RecvFileControl recvFileControl =
	// (RecvFileControl)GlobalVar.hRecvFileControl.get(msgid);
	// if(recvFileControl.retVal!=0)
	// {
	// successFlag = false;
	// // 3:�޷������ļ�����Ϊ����Ӧ�Ķ����в�������Ӧ�ؿ�����Ϣ
	// // * 4:�޷������ļ�����Ϊ����Ӧ�����ݶ����в�������Ӧ���ļ�������Ϣ
	// if(recvFileControl.retVal==3)
	// {
	// errorList.add("�޷������ļ�:"+receveFileData.desc+"! ��Ϊ����Ӧ�Ķ����в�������Ӧ�ؿ�����Ϣ.");
	// File contorlMsgPersistFile = new
	// File(GlobalVar.tempPath+"/recv/"+ByteBuffer.ByteToHex(msgid));//(new
	// String(msgid,"ISO-8859-1")));
	// contorlMsgPersistFile.delete();
	// GlobalVar.hRecvFileControl.remove(msgid);
	// }
	// if(recvFileControl.retVal==4)
	// {
	// errorList.add("�޷������ļ�:"+receveFileData.desc+"!
	// ��Ϊ����Ӧ�����ݶ����в�������Ӧ���ļ�������Ϣ.");
	// File contorlMsgPersistFile = new
	// File(GlobalVar.tempPath+"/recv/"+ByteBuffer.ByteToHex(msgid));//(new
	// String(msgid,"ISO-8859-1")));
	// contorlMsgPersistFile.delete();
	// GlobalVar.hRecvFileControl.remove(msgid);
	// }
	// }
	//
	// }
	// }
	// if(!successFlag)
	// {
	// if(GlobalVar.isControlMsgPersist)
	// {
	// synchronized(GlobalVar.synObjectRecv)
	// {
	// for (Iterator i = msgidList.iterator(); i.hasNext();)
	// {
	// byte[] msgid = (byte[])i.next();
	// File contorlMsgPersistFile = new
	// File(GlobalVar.tempPath+"/recv/"+ByteBuffer.ByteToHex(msgid));//(new
	// String(msgid,"ISO-8859-1")));
	// RandomAccessFile writeAccess1 = new
	// RandomAccessFile(contorlMsgPersistFile,"rw");
	// RecvFileControl recvFileControl =
	// (RecvFileControl)GlobalVar.hRecvFileControl.get(msgid);
	//
	// writeAccess1.seek(0);
	// writeAccess1.write(recvFileControl.packMsgData());
	// writeAccess1.close();
	// writeAccess1 = null;
	//
	// }
	// }
	// }
	// return null;
	// }
	// else
	// {
	//
	// //��ʼ�ϲ��ļ�
	// synchronized(GlobalVar.synObjectSend)
	// {
	//
	// for (int i=0;i<msgidList.size();i++)
	// {
	// byte[] msgid = (byte[])msgidList.get(i);
	// RecvFileControl recvFileControl =
	// (RecvFileControl)GlobalVar.hRecvFileControl.get(msgid);
	// if(i==0)
	// {
	// fileName = dirName+"/"+recvFileControl.fileName;
	// File deleteFile = new File(fileName);
	// deleteFile.delete();
	// CommonFun.FileCut(recvFileControl.absoluteFileName,fileName);
	// }
	// else
	// {
	// //�ļ�׷�Ӻϲ�
	// CommonFun.FileAppend(recvFileControl.absoluteFileName,fileName);
	// File deleteFile = new File(recvFileControl.absoluteFileName);
	// deleteFile.delete();
	// }
	// }
	// }
	// /*for(int i=0;i<serviceThreadNum;i++)
	// {
	// byte[] msgid = new byte[24];
	// ByteBuffer.memset(msgid,(byte)' ');
	// ByteBuffer.memcpy(msgid,0,receveFileData.msgid,0,Constants.CHUNKNUM_LENGTH+Constants.DIGEST_LENGTH);
	// //String msgid =
	// receveFileData.msgid.substring(0,receveFileData.msgid.length()-2);
	// int chunkNum = 0;
	// if(serviceThreadNum==1)
	// chunkNum =0;
	// else
	// chunkNum = i+1;
	// if(Integer.toString(chunkNum).length()<2)
	// {
	// ByteBuffer.memcpy(msgid,Constants.CHUNKNUM_LENGTH+Constants.DIGEST_LENGTH,("0"+Integer.toString(chunkNum)).getBytes(),0,2);
	//
	// }
	// else
	// ByteBuffer.memcpy(msgid,Constants.CHUNKNUM_LENGTH+Constants.DIGEST_LENGTH,(Integer.toString(chunkNum)).getBytes(),0,2);
	//
	//
	//
	//
	// }
	// */
	// //����ȫ��ɾ��
	// synchronized(GlobalVar.synObjectSend)
	// {
	// for (Iterator i = msgidList.iterator(); i.hasNext();)
	// {
	// byte[] msgid = (byte[])i.next();
	// File contorlMsgPersistFile = new
	// File(GlobalVar.tempPath+"/recv/"+ByteBuffer.ByteToHex(msgid));//(new
	// String(msgid,"ISO-8859-1")));
	// contorlMsgPersistFile.delete();
	// GlobalVar.hRecvFileControl.remove(msgid);
	// }
	// }
	// //return iRetVal;
	// }
	// }
	// catch(Exception exc)
	// {
	// fileName = null;
	// logImpl.error("�����ļ�:"+receveFileData.desc+"�����쳣��",exc);
	// }
	//
	// return fileName;
	// }
}
