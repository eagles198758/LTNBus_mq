package com.sinux.mq.client;

/**
 * Զ�̹��̽ӿ�
 * 
 * @author zhongkehexun
 *
 */
public interface Procedure {
	/**
	 * ִ��Զ�̹���
	 * 
	 * @param params
	 *            ����ִ�в����ֽ�����
	 * @return ִ�н���ֽ�����
	 */
	public byte[] process(byte[] params);
}
