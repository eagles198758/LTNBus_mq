package com.sinux.mq.client.util;

/**
 * �ֽ����鴦������
 * @author zhongkehexun
 *
 */
public class ByteArrayutil {
	
	/**
	 * �ϲ�������Ŀ������
	 * @param src1 ��һ������
	 * @param src2 �ڶ�������
	 * @param dest Ŀ������
	 * @return �ϲ�������
	 */
	public static void combine(byte[] src1, byte[] src2, byte[] dest){
		System.arraycopy(src1, 0, dest, 0, src1.length);
		System.arraycopy(src2, 0, dest, src1.length, src2.length);
	}
	
	/**
	 * ��Ŀ��������Ϊ��������
	 * @param src Ŀ������
	 * @param dest1 ��ֺ��һ������
	 * @param dest2 ��ֺ�ڶ�������
	 */
	public static void split(byte[] src, byte[] dest1, byte[] dest2){
		System.arraycopy(src, src.length - dest2.length, dest2, 0, dest2.length);
		System.arraycopy(src, 0, dest1, 0, dest1.length);
	}
}
