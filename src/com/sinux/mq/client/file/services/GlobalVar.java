package com.sinux.mq.client.file.services;

/**
 * @author jingwen.tong 2006-12-14 
 *  Copyright IBM 2005
 * һЩȫ�ֱ������壬��Ҫ�������ļ��ж�ȡ
 */
import java.security.*;
import java.util.HashMap;
import java.util.Map;

import com.sinux.mq.client.util.Event;

public class GlobalVar {
	public static int threadPoolCount = 5;// �����̳߳ص�������С

	public static int serviceThreadpriority = 0;// ���з�����߳����ȼ���

	public static MessageDigest messageDigest = null;

	public static boolean isStartPurgeThread = true;// �Ƿ��������ӳ�����߳�
	public static boolean isRun = true;// ϵͳ�Ƿ���������

	public static int purgeThreadRunInterval = 100;// ���ӳ�����߳�ÿ�����ٺ��뻽��һ��

	public static Event notifyPurgeThreadThreadEvent = new Event();// �������ӳ�����̵߳��¼�����

	public static boolean isMsgPersist = false;// ��MQ���͵���Ϣ�Ƿ�־û�
	/*
	 * �������ڴ��еĿ�����Ϣ�Ƿ�־û������Ϊ��׷����ߵĴ���Ч�ʵĻ������ã����ǳ��ֳ���ͻȻ崻��Ļ��� �������ʧ�ܵ���һ�㿪ʼ����
	 */
	public static boolean isControlMsgPersist = false;//

	public static int msgSize = 4096;

	public static boolean isStartChunk = false;
	public static long filesizeChunk = 10240;
	public static int chunknum = 5;
	public static String queueName = null;
	public static String tempPath = null;
	public static int channelPoolNum = 100;//���ӳص�������������Ŀ

	@SuppressWarnings("rawtypes")
	public static Map hSendFileControl = new HashMap();
	public static Object synObjectSend = new Object();
	public static Object synObject = new Object();

	@SuppressWarnings("rawtypes")
	public static Map hRecvFileControl = new HashMap();
	public static Object synObjectRecv = new Object();

	public static String fileTransControlQueueName = ".FILETRANS.CONTROL";

	public static String fileTransControlFinishQueueName = ".FILETRANS.CONTROL.FINISH";

}
