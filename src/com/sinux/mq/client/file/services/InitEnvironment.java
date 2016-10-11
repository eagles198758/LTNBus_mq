/**
 * 
 */
package com.sinux.mq.client.file.services;

import java.io.File;
import java.util.List;
import org.jdom.*;
import org.jdom.input.*;

/**
 * @author jingwen.tong 2006-12-18 Copyright IBM 2005 读取配置文件初始化相应的值
 */
public class InitEnvironment {
	// private static final Log logImpl =
	// LogFactory.getLog(InitEnvironment.class);

	@SuppressWarnings({ "unused", "rawtypes" })
	public static void initFromConfigFile(String configFileName) throws Exception {
		try {
			// 1.读取配置文件信息
			File file = new File(configFileName);
			if (!file.exists())
				throw new Exception("配置文件:" + configFileName + "不存在,不能完成初始化！");
			Element oChild = null;
			SAXBuilder oBuild = new SAXBuilder();
			Document oDoc = oBuild.build(file);
			Element oRoot = oDoc.getRootElement();
			List mqparamsetList = oRoot.getChildren("mqparamset");
			if (mqparamsetList == null || mqparamsetList.size() != 1) {
				throw new Exception("配置文件:" + configFileName + "节点mqparamset不能存在多个或者不存在！");
			}

			List mqconinfoList = (List) ((Element) mqparamsetList.get(0)).getChildren("mqconinfo");
			if (mqconinfoList == null || mqparamsetList.size() != 1) {
				throw new Exception("配置文件:" + configFileName + "节点mqparamset下的mqconinfo节点不能存在多个或者不存在！");
			}

			Element mqconinfoElement = (Element) mqconinfoList.get(0);
			if (mqconinfoElement.getAttributeValue("connectmethod").trim().compareToIgnoreCase("1") == 0) {
				// GlobalVar.connectionInfo.setConnectFlag(true);
			} else
			// GlobalVar.connectionInfo.setConnectFlag(false);
			// GlobalVar.connectionInfo.setQueueManagerName(mqconinfoElement.getAttributeValue("queuemanager"));
			// GlobalVar.connectionInfo.setHostName(mqconinfoElement.getAttributeValue("hostname"));
			// GlobalVar.connectionInfo.setPort(Integer.parseInt(mqconinfoElement.getAttributeValue("port")));
			// GlobalVar.connectionInfo.setCcsid(Integer.parseInt(mqconinfoElement.getAttributeValue("ccsid")));
			// GlobalVar.connectionInfo.setChannelName(mqconinfoElement.getAttributeValue("channelname"));
			// GlobalVar.connectionInfo.setUserId(mqconinfoElement.getAttributeValue("userid"));
			// GlobalVar.connectionInfo.setPassword(mqconinfoElement.getAttributeValue("password"));
			// GlobalVar.connectionInfo.setConnectionPoolNum(Integer.parseInt(mqconinfoElement.getAttributeValue("connectionpollnum")));
			// GlobalVar.connectionInfo.setTimeOutTime(Integer.parseInt(mqconinfoElement.getAttributeValue("timeout"))*60*1000);

			if (((Element) mqparamsetList.get(0)).getChild("startpurgethread").getTextTrim()
					.compareToIgnoreCase("1") == 0) {
				GlobalVar.isStartPurgeThread = true;
			} else
				GlobalVar.isStartPurgeThread = false;
			GlobalVar.purgeThreadRunInterval = Integer.parseInt(
					((Element) mqparamsetList.get(0)).getChild("purgethreadruninterval").getTextTrim()) * 60 * 1000;

			GlobalVar.threadPoolCount = Integer.parseInt(oRoot.getChild("threadpoolnum").getTextTrim());
			List transparamsetList = oRoot.getChildren("transparamset");
			if (transparamsetList == null || transparamsetList.size() != 1) {
				throw new Exception("配置文件:" + configFileName + "节点transparamset不能存在多个或者不存在！");
			}
			Element transparamElement = (Element) transparamsetList.get(0);

			if (transparamElement.getChild("msgpersist").getTextTrim().compareToIgnoreCase("1") == 0)
				GlobalVar.isMsgPersist = true;
			else
				GlobalVar.isMsgPersist = false;

			if (transparamElement.getChild("controlmsgpersist").getTextTrim().compareToIgnoreCase("1") == 0)
				GlobalVar.isControlMsgPersist = true;
			else
				GlobalVar.isControlMsgPersist = false;

			GlobalVar.msgSize = Integer.parseInt(transparamElement.getChild("msgsize").getTextTrim()) * 1024;
			List chunktransparamsetList = transparamElement.getChildren("chunktransparamset");
			if (chunktransparamsetList == null || chunktransparamsetList.size() != 1) {
				throw new Exception(
						"配置文件:" + configFileName + "节点transparamset下的字节点chunktransparamsetList不能存在多个或者不存在！");

			}
			GlobalVar.queueName = transparamElement.getChild("queuename").getTextTrim();
			GlobalVar.fileTransControlQueueName = transparamElement.getChild("filetranscontorlqueuename").getTextTrim();
			GlobalVar.fileTransControlFinishQueueName = transparamElement.getChild("filetranscontorlfinishqueuename")
					.getTextTrim();
			Element chunktransparamsetElement = (Element) chunktransparamsetList.get(0);

			GlobalVar.chunknum = Integer.parseInt(chunktransparamsetElement.getAttributeValue("chunknum"));

			GlobalVar.filesizeChunk = Integer.parseInt(chunktransparamsetElement.getAttributeValue("filesize"));

			if (chunktransparamsetElement.getAttributeValue("isstart").trim().compareToIgnoreCase("1") == 0)
				GlobalVar.isStartChunk = true;
			else
				GlobalVar.isStartChunk = false;
			if (GlobalVar.chunknum > GlobalVar.threadPoolCount)
				GlobalVar.chunknum = GlobalVar.threadPoolCount;
			GlobalVar.tempPath = oRoot.getChild("temppath").getTextTrim() + "/trans";
			File tempFile = new File(GlobalVar.tempPath);
			if (!tempFile.exists()) {
				if (!tempFile.mkdirs())
					throw new Exception("无法创建系统所需要的目录:" + GlobalVar.tempPath + "系统将无法进行传输!");
			}
			tempFile = new File(GlobalVar.tempPath + "/send");
			if (!tempFile.exists()) {
				tempFile.mkdirs();
			}

			tempFile = new File(GlobalVar.tempPath + "/recv");
			if (!tempFile.exists()) {
				tempFile.mkdirs();
			}
			// 2.初始化SHA引擎
			GlobalVar.messageDigest = java.security.MessageDigest.getInstance("SHA-1");

			// 3.开始创建服务线程池
			ThreadPool threadPool = ThreadPool.getSingleInstance();
			// LogService logService = new LogService();
			// logService.InitService();
			// logService.start();
			// MQEnvironment.disableTracing();
		} catch (Exception exc) {
			// logImpl.error("初始化失败!",exc);
			throw exc;
		}
	}
}
