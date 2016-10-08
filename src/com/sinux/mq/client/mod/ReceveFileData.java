package com.sinux.mq.client.mod;
/**
 * @author jingwen.tong 2006-12-19
 *  Copyright IBM 2005
 *  表明能够接收的文件列表数据对象
 */
public class ReceveFileData{
	public byte[] msgid = null;
	public int   chunkcount=0;
	public long  fileLength = 0;
	public String desc = "";
	public String tradeCode = "";
}
