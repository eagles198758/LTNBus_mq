package com.sinux.mq.client.file.services;

import java.util.Map;

import com.sinux.mq.client.MqConnectionFactory;
import com.sinux.mq.client.mod.FileTransControlMsg;
import com.sinux.mq.client.mod.RecvFileControl;
import com.sinux.mq.client.mod.SendFileControl;
import com.sinux.mq.client.mod.TransInfo;
import com.sinux.mq.client.util.ComplexEvent;

/**
 * @author jingwen.tong 2006-12-14 Copyright IBM 2005 �����̵߳�ʵ��,����ϴ������صȹ���
 */
@SuppressWarnings("rawtypes")
public class ServiceThread extends Thread {
	private ThreadPool pool;

	private ComplexEvent complexSendEvent = null;

	private Map hSendFileControl = null;
	private Object synObjectSend = null;
	private Map hRecvFileControl = null;
	private Object synObjectRecv = null;
	private FileTransControlMsg fileTransControlMsg = null;
	private TransInfo transInfo = null;
	private Object synObject = new Object();
	private int tradeCode = 0;// 1:������ ��2.�������
	private byte[] msgid = null;
	private boolean isIdle = true;
	private String receiverName = null;

	private MqConnectionFactory factory = null;

	public synchronized void setIdleStatus(boolean isIdle) {
		this.isIdle = isIdle;
	}

	public synchronized boolean isIdle() {
		return this.isIdle;
	}

	public ServiceThread(ThreadPool pool) {
		this.pool = pool;
	}

	// ���������߳̿�ʼ����
	public void prepareRecv(Map hRecvFileControl, Object synObjectRecv, TransInfo transInfo, ComplexEvent complexEvent,
			byte[] msgid, String receiverName, MqConnectionFactory factory) {
		synchronized (synObject) {
			this.complexSendEvent = complexEvent;
			this.hRecvFileControl = hRecvFileControl;
			this.synObjectRecv = synObjectRecv;
			this.transInfo = transInfo;
			this.tradeCode = 2;
			this.msgid = msgid;
			this.receiverName = receiverName;
			this.factory = factory;
			synObject.notify();
		}
	}

	// ���������߳̿�ʼ����
	public void prepareSend(Map hSendFileControl, Object synObjectSend, FileTransControlMsg fileTransControlMsg,
			TransInfo transInfo, ComplexEvent complexEvent, byte[] msgid, MqConnectionFactory factory) {
		synchronized (synObject) {
			this.complexSendEvent = complexEvent;
			this.hSendFileControl = hSendFileControl;
			this.synObjectSend = synObjectSend;
			this.fileTransControlMsg = fileTransControlMsg;
			this.transInfo = transInfo;
			this.tradeCode = 1;
			this.msgid = msgid;
			this.factory = factory;
			synObject.notify();
		}
	}

	public void run() {
		while (GlobalVar.isRun) {
			try {
				synchronized (synObject) {
					// �ȴ�������
					synObject.wait();
				}
			} catch (InterruptedException exc) {
				continue;
			}
			try {
				// ��������Ҫ����
				switch (tradeCode) {
				case 1: {// ����
					MqTranSendService tranSendService = new MqTranSendService(msgid, fileTransControlMsg, transInfo,
							hSendFileControl, synObjectSend, factory);
					int iRetVal = tranSendService.sendFile();
					synchronized (synObjectSend) {
						SendFileControl sendFileControl = (SendFileControl) hSendFileControl.get(msgid);
						if (sendFileControl != null) {
							sendFileControl.retVal = iRetVal;
						}
					}
					try {
						complexSendEvent.SetEvent();
					} catch (Exception exc) {
						exc.printStackTrace();
					}
				}
					break;
				case 2: {// ����
					System.out.println(Thread.currentThread().getName() + "�ѻ��ѣ�");
					MqTranRecvService tranRecvService = new MqTranRecvService(msgid, transInfo, hRecvFileControl,
							synObjectRecv,factory);
					int iRetVal = tranRecvService.recvFile(receiverName);
					synchronized (synObjectRecv) {
						RecvFileControl recvFileControl = (RecvFileControl) hRecvFileControl.get(msgid);
						if (recvFileControl != null) {
							recvFileControl.retVal = iRetVal;
						}
					}
					try {
						complexSendEvent.SetEvent();
					} catch (Exception exc) {
						System.out.println("�߳�:" + this.getName() + " occur exception in notify event,exception info:");
						exc.printStackTrace();
					}
				}
					break;
				default:
					break;
				}
			} catch (Exception exc) {
				exc.printStackTrace();
			}
			pool.returnServiceThread(this);
		}
	}
}
