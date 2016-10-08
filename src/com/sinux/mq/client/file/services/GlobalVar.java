package com.sinux.mq.client.file.services;

/**
 * @author jingwen.tong 2006-12-14 
 *  Copyright IBM 2005
 * 一些全局变量定义，需要从配置文件中读取
 */
import java.security.*;
import java.util.HashMap;
import java.util.Map;

import com.sinux.mq.client.util.Event;

public class GlobalVar {
	public static int threadPoolCount = 5;// 服务线程池的容量大小

	public static int serviceThreadpriority = 0;// 进行服务的线程优先级别

	public static MessageDigest messageDigest = null;

	public static boolean isStartPurgeThread = true;// 是否启动连接池清除线程
	public static boolean isRun = true;// 系统是否正在运行

	public static int purgeThreadRunInterval = 100;// 连接池清除线程每隔多少毫秒唤醒一次

	public static Event notifyPurgeThreadThreadEvent = new Event();// 激发连接池清除线程的事件对象

	public static boolean isMsgPersist = false;// 往MQ发送的消息是否持久化
	/*
	 * 程序在内存中的控制信息是否持久化，如果为了追求最高的传输效率的话不启用，但是出现程序突然宕机的话， 将不会从失败的那一点开始传输
	 */
	public static boolean isControlMsgPersist = false;//

	public static int msgSize = 4096;

	public static boolean isStartChunk = false;
	public static long filesizeChunk = 10240;
	public static int chunknum = 5;
	public static String queueName = null;
	public static String tempPath = null;
	public static int channelPoolNum = 100;//连接池的所允许的最大数目

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
