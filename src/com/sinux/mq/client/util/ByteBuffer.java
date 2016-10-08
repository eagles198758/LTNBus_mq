package com.sinux.mq.client.util;

import java.io.Serializable;

/**
 * <p>
 * Title: 操作字节流的类
 * </p>
 * <p>
 * Description: 用于操作字节流
 * </p>
 * <p>
 * File： ByteBuffer.java
 * </p>
 * <p>
 * Copyright 2005 IBM， Ltd. All right reserved.
 * </p>
 * <p>
 * Date Author Changes
 * </p>
 * <p>
 * 2006/12/16 jingwen.tong Created
 * </p>
 * 
 * @version 1.0
 */

public class ByteBuffer implements Serializable {

	/**
		 * 
		 */
	private static final long serialVersionUID = 1L;
	// 属性定义
	// 私有变量：
	// private static final String copyright = "Copyright 2004 IBM Ltd. All
	// right reserved.";
	private byte m_byteBuffer[];
	// 保护变量：

	// 方法定义：

	// 公共方法：
	/**
	 * 构造函数
	 * 
	 * @param abyte0[]：传入的字节流
	 */
	public ByteBuffer(byte abyte0[]) {
		m_byteBuffer = null;
		m_byteBuffer = abyte0;
	}

	/**
	 * 把传入的字节流附加到原来的字节流上
	 * 
	 * @param abyte0[]：传入的字节流
	 */
	public void append(byte abyte0[]) throws Exception {

		int i = 0;
		try {
			if (m_byteBuffer != null)
				i = m_byteBuffer.length;
			byte abyte1[] = new byte[abyte0.length + i];
			if (i > 0)
				memcpy(abyte1, 0, m_byteBuffer, 0, i);
			memcpy(abyte1, i, abyte0, 0, abyte0.length);
			m_byteBuffer = abyte1;
		} catch (Exception exc) {
			throw exc;
		}
	}

	/**
	 * 把传入的字节流中前i字节附加到原来的字节流上
	 * 
	 * @param abyte0[]：传入的字节流
	 * @param i:那几个字节
	 */
	public void append(byte abyte0[], int i) throws Exception {
		int j = 0;
		try {
			if (m_byteBuffer != null)
				j = m_byteBuffer.length;
			byte abyte1[] = new byte[i + j];
			if (j > 0)
				memcpy(abyte1, 0, m_byteBuffer, 0, j);
			memcpy(abyte1, j, abyte0, 0, i);
			m_byteBuffer = abyte1;
		} catch (Exception exc) {
			throw exc;
		}
	}

	/**
	 * 返回字节流的长度
	 * 
	 * @return ： 长度
	 */
	public int length() {
		if (m_byteBuffer == null)
			return 0;
		return m_byteBuffer.length;
	}

	/**
	 * 把字节流的转换成字符串
	 * 
	 * @return ： 字符串
	 */
	public String toString() {
		if (m_byteBuffer == null)
			return null;
		return new String(m_byteBuffer);
	}

	/**
	 * 判断是否与输入的字节流是否相同
	 * 
	 * @param abyte0[]：要判断的2进制流
	 * @return ： true：相同 false：不同
	 */
	public boolean equals(byte abyte0[]) {
		return memcmp(m_byteBuffer, abyte0);
	}

	/**
	 * 判断是否与输入的ByteBuffer是否相同
	 * 
	 * @param bytebuffer：要判断的对象
	 * @return ： true：相同 false：不同
	 */
	public boolean equals(ByteBuffer bytebuffer) {
		try {
			return memcmp(m_byteBuffer, bytebuffer.byteValue());
		} catch (Exception exc) {
			return false;
		}
	}

	/**
	 * 判断是否与输入的Object是否相同
	 * 
	 * @param obj：要判断的对象
	 * @return ： true：相同 false：不同
	 */
	public boolean equals(Object obj) {
		try {
			ByteBuffer bytebuffer = (ByteBuffer) obj;
			return memcmp(m_byteBuffer, bytebuffer.byteValue());
		} catch (Exception exc) {
			return false;
		}
	}

	/**
	 * 得到字节流
	 * 
	 * @return ：字节流
	 */
	public byte[] byteValue() {
		return m_byteBuffer;
	}

	/**
	 * 得到对象中字节流的Hash值
	 * 
	 * @return ：Hash值
	 */
	public int hashCode() {
		int i = 0;
		try {
			for (int j = 0; j < m_byteBuffer.length; j++)
				i += m_byteBuffer[j];
		} catch (Exception exc) {
			return 0;
		}
		return i;
	}

	/**
	 * 把abyte0字节流中从第I位开始的4个字节顺序的转换成整数
	 * 
	 * @param abyte0[]：传入的字节流
	 * @param i:从那开始
	 * @return:返回的整数值
	 */
	public static int byte2Int(byte abyte0[], int i) throws Exception {
		try {
			int j = (abyte0[i] & 0xff) << 24;
			j |= (abyte0[i + 1] & 0xff) << 16;
			j |= (abyte0[i + 2] & 0xff) << 8;
			j |= abyte0[i + 3] & 0xff;
			return j;
		} catch (Exception exc) {
			throw exc;
		}
	}

	/**
	 * 把abyte0字节流中从第I位开始的4个字节倒序的转换成整数
	 * 
	 * @param abyte0[]：传入的字节流
	 * @param i:从那开始
	 * @return:返回的整数值
	 */
	public static int byte2IntLE(byte abyte0[], int i) throws Exception {
		try {
			int j = 0;
			j = (abyte0[i + 3] & 0xff) << 24;
			j |= (abyte0[i + 2] & 0xff) << 16;
			j |= (abyte0[i + 1] & 0xff) << 8;
			j |= abyte0[i] & 0xff;
			return j;
		} catch (Exception exc) {
			throw exc;
		}
	}

	/**
	 * 把abyte0字节流中从第j位开始的4个字节,按顺序的被i表示成的二进制流替换掉
	 * 
	 * @param abyte0[]：传入的字节流
	 * @param i:替换值
	 * @param j:从那开始
	 */
	public static void int2Byte(int i, byte abyte0[], int j) throws Exception {
		try {
			abyte0[j] = (byte) (i >> 24);
			abyte0[j + 1] = (byte) ((i & 0xff0000) >> 16);
			abyte0[j + 2] = (byte) ((i & 0xff00) >> 8);
			abyte0[j + 3] = (byte) (i & 0xff);
		} catch (Exception exc) {
			throw exc;
		}
	}

	/**
	 * 把abyte0字节流中从第j位开始的4个字节,按倒序的被i表示成的二进制流替换掉
	 * 
	 * @param abyte0[]：传入的字节流
	 * @param i:替换值
	 * @param j:从那开始
	 */
	public static void int2ByteLE(int i, byte abyte0[], int j) throws Exception {
		try {
			abyte0[j + 3] = (byte) ((i & 0xff000000) >> 24);
			abyte0[j + 2] = (byte) ((i & 0xff0000) >> 16);
			abyte0[j + 1] = (byte) ((i & 0xff00) >> 8);
			abyte0[j] = (byte) (i & 0xff);
		} catch (Exception exc) {
			throw exc;
		}
	}

	/**
	 * 把abyte0字节流中从第I位开始的4个字节顺序的转换成整数
	 * 
	 * @param abyte0[]：传入的字节流
	 * @param i:从那开始
	 * @return:返回的整数值
	 */
	public static short byte2Short(byte abyte0[], int i) throws Exception {
		try {
			short word0 = (short) ((abyte0[i] & 0xff) << 8);
			word0 |= (short) (abyte0[i + 1] & 0xff);
			return word0;
		} catch (Exception exc) {
			throw exc;
		}
	}

	/**
	 * 把abyte0字节流中从第I位开始的4个字节倒序的转换成整数
	 * 
	 * @param abyte0[]：传入的字节流
	 * @param i:从那开始
	 * @return:返回的整数值
	 */
	public static short byte2ShortLE(byte abyte0[], int i) throws Exception {
		try {
			short word0 = 0;
			word0 = (short) ((abyte0[i + 1] & 0xff) << 8);
			word0 |= (short) (abyte0[i] & 0xff);
			return word0;
		} catch (Exception exc) {
			throw exc;
		}
	}

	/**
	 * 把abyte0字节流中从第j位开始的4个字节,按顺序的被i表示成的二进制流替换掉
	 * 
	 * @param abyte0[]：传入的字节流
	 * @param i:替换值
	 * @param j:从那开始
	 */
	public static void short2Byte(short word0, byte abyte0[], int i) throws Exception {
		try {
			abyte0[i] = (byte) (word0 >> 8);
			abyte0[i + 1] = (byte) (word0 & 0xff);
		} catch (Exception exc) {
			throw exc;
		}
	}

	/**
	 * 把abyte0字节流中从第j位开始的4个字节,按倒序的被i表示成的二进制流替换掉
	 * 
	 * @param abyte0[]：传入的字节流
	 * @param i:替换值
	 * @param j:从那开始
	 */
	public static void short2ByteLE(short word0, byte abyte0[], int i) throws Exception {
		try {
			abyte0[i + 1] = (byte) ((word0 & 0xff00) >> 8);
			abyte0[i] = (byte) (word0 & 0xff);
		} catch (Exception exc) {
			throw exc;
		}
	}

	public static void int2ShortLE(int i, short aword0[], int j) throws Exception {
		try {
			byte abyte0[] = new byte[4];
			int2ByteLE(i, abyte0, 0);
			short word0 = 0;
			short word1 = 0;
			word0 = byte2ShortLE(abyte0, 0);
			word1 = byte2ShortLE(abyte0, 2);
			aword0[j] = word0;
			aword0[j + 1] = word1;
		} catch (Exception exc) {
			throw exc;
		}
	}

	public static int short2IntLE(short aword0[], int i) throws Exception {
		try {
			int j = 0;
			byte abyte0[] = new byte[4];
			short word0 = 0;
			short word1 = 0;
			word0 = aword0[i];
			word1 = aword0[i + 1];
			short2ByteLE(word0, abyte0, 0);
			short2ByteLE(word1, abyte0, 2);
			j = byte2IntLE(abyte0, 0);
			return j;
		} catch (Exception exc) {
			throw exc;
		}
	}

	/**
	 * 比较输入的两个字节流是否相同
	 * 
	 * @param abyte0[]：传入的字节流
	 * @param abyte1[]：传入的字节流
	 */
	public static boolean memcmp(byte abyte0[], byte abyte1[]) {
		int iFlag = 0;

		if (abyte0 == null)
			iFlag += 1;
		if (abyte1 == null)
			iFlag += 2;
		switch (iFlag) {
		case 3:
			return true;
		case 1:
			return false;
		case 2:
			return false;
		}

		if (abyte0.length != abyte1.length)
			return false;
		for (int i = 0; i < abyte0.length; i++)
			if (abyte0[i] != abyte1[i])
				return false;

		return true;
	}

	/**
	 * 比较输入的两个字节流分别从第i、j位开始的k位是否相同
	 * 
	 * @param abyte0[]：传入的字节流
	 * @param i:abyte0[]的起始位
	 * @param abyte1[]：传入的字节流
	 * @param j:abyte1[]的起始位
	 * @param k:要比较的位数
	 */
	public static boolean memcmp(byte abyte0[], int i, byte abyte1[], int j, int k) {
		int iFlag = 0;

		if (abyte0 == null)
			iFlag += 1;
		if (abyte1 == null)
			iFlag += 2;
		switch (iFlag) {
		case 3:
			return true;
		case 1:
			return false;
		case 2:
			return false;
		}
		try {
			for (int l = 0; l < k; l++)
				if (abyte0[l + i] != abyte1[l + j])
					return false;
		} catch (ArrayIndexOutOfBoundsException arrayindexoutofboundsexception) {
			return false;
		}
		return true;
	}

	/**
	 * 用byte0值初始化abyte0[]这个2进制流
	 * 
	 * @param abyte0[]：要被初始化的字节流。
	 * @param byte0:初始化的值。
	 */
	public static void memset(byte abyte0[], byte byte0) {
		try {
			int i = 0;
			if (abyte0 == null)
				return;
			while (true) {
				abyte0[i] = byte0;
				i++;
			}
		} catch (IndexOutOfBoundsException indexoutofboundsexception) {
			return;
		}
	}

	/**
	 * 作用同C运行库的同名函数
	 * 
	 * @param abyte0[]：传入的字节流
	 * @param i:abyte0[]的起始位
	 * @param abyte1[]：传入的字节流
	 * @param j:abyte1[]的起始位
	 * @param k:要拷贝的位数
	 */
	public static void memcpy(byte abyte0[], int i, byte abyte1[], int j, int k) throws Exception {
		try {
			for (int l = 0; l < k; l++)
				abyte0[l + i] = abyte1[l + j];
		} catch (Exception exc) {
			throw exc;
		}

	}

	/**
	 * 二进制字节转换成16字节
	 * 
	 * @param byte0:要转换的字节
	 * @return :转换后的16进制表示
	 */
	public static String byte2Hex(byte byte0) {
		char ac[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		String s = "";
		s = s + ac[byte0 >> 4 & 0xf];
		s = s + ac[byte0 & 0xf];
		return s;
	}

	/**
	 * 整数转换成16字节
	 * 
	 * @param i:要转换的整数
	 * @return :转换后的16进制表示
	 */
	public static String int2Hex(int i) {
		String s = "";
		byte byte0 = (byte) ((i & 0xff000000) >>> 24);
		s = s + byte2Hex(byte0);
		byte0 = (byte) ((i & 0xff0000) >>> 16);
		s = s + byte2Hex(byte0);
		byte0 = (byte) ((i & 0xff00) >>> 8);
		s = s + byte2Hex(byte0);
		byte0 = (byte) (i & 0xff);
		s = s + byte2Hex(byte0);
		return s;
	}

	/**
	 * 短整数转换成16字节
	 * 
	 * @param word0:要转换的短整数
	 * @return :转换后的16进制表示
	 */
	public static String short2Hex(short word0) {
		String s = "";
		byte byte0 = (byte) ((word0 & 0xff00) >>> 8);
		s = s + byte2Hex(byte0);
		byte0 = (byte) (word0 & 0xff);
		s = s + byte2Hex(byte0);
		return s;
	}

	/**
	 * 把字节流进行解密
	 * 
	 * @param abyte0:要进行解密的字节流
	 * @return :解密后的字节流 如果返回null则失败
	 */
	public static byte[] decipher(byte abyte0[]) {
		byte abyte1[] = null;
		try {
			int k = 0;
			int l = 0;
			int i1 = 0;
			i1 = 0;
			k = abyte0.length;
			abyte1 = new byte[k / 2];
			for (; abyte0[l] != 0; l += 2) {
				int j = abyte0[l] - 65 & 0xf;
				int i = abyte0[l + 1] - 65 & 0xf;
				abyte1[i1++] = (byte) (i + (j << 4));
			}
		} catch (Exception exc) {
			return abyte1;
		}
		return abyte1;
	}

	/**
	 * 把字节流进行简单的加密
	 * 
	 * @param abyte0:要进行加密的字节流
	 * @return :加密后的字节流 如果返回null则失败
	 */
	public static byte[] encipher(byte abyte0[]) {
		byte abyte1[] = null;
		try {
			abyte1 = new byte[abyte0.length * 2];

			int l = 0;
			l = 0;
			for (int k = 0; k < abyte0.length; k++) {
				int j = ((abyte0[k] & 0xf0) >> 4) + 65;
				abyte1[l++] = (byte) j;
				int i = (abyte0[k] & 0xf) + 65;
				abyte1[l++] = (byte) i;
			}
		} catch (Exception exc) {
			return null;
		}
		return abyte1;
	}

	/**
	 * 将字节流转换成16进制
	 * 
	 * @function ByteToHex
	 * @param b
	 * @return
	 */
	public static String ByteToHex(byte[] b) {// 二行制转字符串
		String sHexStr = "";
		String sTemp = "";
		for (int n = 0; n < b.length; n++) {
			sTemp = (java.lang.Integer.toHexString(b[n] & 0XFF));
			if (sTemp.length() == 1)
				sHexStr = sHexStr + "0" + sTemp;
			else
				sHexStr = sHexStr + sTemp;
		}
		return sHexStr.toUpperCase();
	}

	/**
	 * 将16进制字符串转换成字节流
	 * 
	 * @function HexToByte
	 * @param sHexString
	 * @return
	 */
	public static byte[] HexToByte(String sHexString) {
		byte[] ResultByte;
		Integer oInteger;
		String sTemp;

		ResultByte = new byte[sHexString.length() / 2];
		for (int i = 0; i < sHexString.length(); i = i + 2) {
			sTemp = sHexString.substring(i, i + 2);
			oInteger = new Integer((Integer.parseInt(sTemp, 16)));
			ResultByte[i / 2] = oInteger.byteValue();
		}
		return ResultByte;
	}
}