package com.sinux.mq.client.mod;

/**
 * @author jingwen.tong 2006-12-18 Copyright IBM 2005 记录一些传输进度信息
 */

public class TransInfo {

	private long totalSendedSize = 0;// 已经发送了多少字节
	private Object synObject1 = new Object();

	private long totalSendedTime = 0;// 发送花了多少时间 单位为毫秒
	private Object synObject2 = new Object();

	private long totalReceivedSize = 0;// 已经接收了多少字节
	private Object synObject3 = new Object();

	private long totalReceivedTime = 0;// 接收花了多少时间 单位为毫秒
	private Object synObject4 = new Object();

	private long totalFinishedSize = 0;// 已经传送了多少字节，这个数据大于等于totalSendedSize
	private Object synObject5 = new Object();

	public void setTotalFinishedSize(long finishedSize) {
		synchronized (synObject5) {
			totalFinishedSize += finishedSize;
		}
	}

	public long getTotalFinishedSize() {
		synchronized (synObject5) {
			return this.totalFinishedSize;
		}
	}

	public void setTotalReceivedTime(long receivedTime) {
		synchronized (synObject4) {
			totalReceivedTime += receivedTime;
		}
	}

	public long getTotalReceivedTime() {
		synchronized (synObject4) {
			return this.totalReceivedTime;
		}
	}

	public void setTotalReceivedSize(long receivedSize) {
		synchronized (synObject3) {
			totalReceivedSize += receivedSize;
		}
	}

	public long getTotalReceivedSize() {
		synchronized (synObject3) {
			return this.totalReceivedSize;
		}
	}

	public void setTotalSendedSize(long sendedSize) {
		synchronized (synObject1) {
			totalSendedSize += sendedSize;
		}
	}

	public long getTotalSendedSize() {
		synchronized (synObject1) {
			return this.totalSendedSize;
		}
	}

	public void setTotalSendedTime(long sendedTime) {
		synchronized (synObject2) {
			totalSendedTime += sendedTime;
		}
	}

	public long getTotalSendedTime() {
		synchronized (synObject2) {
			return this.totalSendedTime;
		}
	}
}
