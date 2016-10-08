package com.sinux.mq.client.util;

/**
 * 字节数组处理工具类
 * @author zhongkehexun
 *
 */
public class ByteArrayutil {
	
	/**
	 * 合并两个到目标数组
	 * @param src1 第一个数组
	 * @param src2 第二个数组
	 * @param dest 目标数组
	 * @return 合并后数组
	 */
	public static void combine(byte[] src1, byte[] src2, byte[] dest){
		System.arraycopy(src1, 0, dest, 0, src1.length);
		System.arraycopy(src2, 0, dest, src1.length, src2.length);
	}
	
	/**
	 * 将目标数组拆分为两个数组
	 * @param src 目标数组
	 * @param dest1 拆分后第一个数组
	 * @param dest2 拆分后第二个数组
	 */
	public static void split(byte[] src, byte[] dest1, byte[] dest2){
		System.arraycopy(src, src.length - dest2.length, dest2, 0, dest2.length);
		System.arraycopy(src, 0, dest1, 0, dest1.length);
	}
}
