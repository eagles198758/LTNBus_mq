package com.sinux.mq.client.mod;
/**
 * @author jingwen.tong 2006-12-19
 *  Copyright IBM 2005
 *  �����ܹ����յ��ļ��б����ݶ���
 */
public class ReceveFileData{
	public byte[] msgid = null;
	public int   chunkcount=0;
	public long  fileLength = 0;
	public String desc = "";
	public String tradeCode = "";
}
