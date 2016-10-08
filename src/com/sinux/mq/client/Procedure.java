package com.sinux.mq.client;

/**
 * 远程过程接口
 * 
 * @author zhongkehexun
 *
 */
public interface Procedure {
	/**
	 * 执行远程过程
	 * 
	 * @param params
	 *            过程执行参数字节数组
	 * @return 执行结果字节数组
	 */
	public byte[] process(byte[] params);
}
