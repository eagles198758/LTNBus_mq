package com.sinux.mq.client;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;
import com.sinux.mq.client.util.ExchangeType;

/**
 * mq通道类
 * @author zhongkehexun
 *
 */
public class MqChannel {
	private Channel channel;
	private String queueName;
	private String exchangeName;
	private ExchangeType exchangeType;
	private boolean isPersist;
	private String replyQueueName;
	private QueueingConsumer consumer;
	
	private long timeOutTime = 0;;//连接超时时间计数
	private int hashCode = 0;
	
	private boolean isUse = false;// false:空闲 true:正在使用
	private long freeTime = System.currentTimeMillis(); // 本连接开始闲置的时间,如
														// 从1970年逝去的毫秒数目
	private long useTime = System.currentTimeMillis(); // 本连接开始使用的时间,即:状态
														// 空闲->使用中
	private long createTime = System.currentTimeMillis();// 连接的创建时间

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public String getExchangeName() {
		return exchangeName;
	}

	public void setExchangeName(String exchangeName) {
		this.exchangeName = exchangeName;
	}

	public ExchangeType getExchangeType() {
		return exchangeType;
	}

	public void setExchangeType(ExchangeType exchangeType) {
		this.exchangeType = exchangeType;
	}

	public boolean isPersist() {
		return isPersist;
	}

	public void setPersist(boolean isPersist) {
		this.isPersist = isPersist;
	}

	public String getReplyQueueName() {
		return replyQueueName;
	}

	public void setReplyQueueName(String replyQueueName) {
		this.replyQueueName = replyQueueName;
	}

	public QueueingConsumer getConsumer() {
		return consumer;
	}

	public void setConsumer(QueueingConsumer consumer) {
		this.consumer = consumer;
	}

	public long getTimeOutTime() {
		return timeOutTime;
	}

	public void setTimeOutTime(long timeOutTime) {
		this.timeOutTime = timeOutTime;
	}

	public int getHashCode() {
		return hashCode;
	}

	public void setHashCode(int hashCode) {
		this.hashCode = hashCode;
	}

	public boolean isUse() {
		return isUse;
	}

	public void setUse(boolean isUse) {
		this.isUse = isUse;
	}

	public long getFreeTime() {
		return freeTime;
	}

	public void setFreeTime(long freeTime) {
		this.freeTime = freeTime;
	}

	public long getUseTime() {
		return useTime;
	}

	public void setUseTime(long useTime) {
		this.useTime = useTime;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}
}
