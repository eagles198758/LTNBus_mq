package com.sinux.mq.client;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.sinux.mq.client.mod.MsgDes;
import com.sinux.mq.client.util.ByteArrayutil;
import com.sinux.mq.client.util.ExchangeType;

/**
 * mq连接工厂类
 * 
 * @author zhongxinghang
 *
 */
public class MqConnectionFactory {
	private static String host = null;
	private static String userName = null;
	private static String passWord = null;
	private static String port = null;
	private static Connection connection;
	private static ConnectionFactory factory;
	private static boolean autoAck = true;
	// private final String propFilePath = "";

	/**
	 * mq工厂类构造方法
	 * 
	 * @param host
	 *            服务器地址
	 * @param port
	 *            端口号
	 * @param userName
	 *            用户名
	 * @param passWord
	 *            密码
	 */
	@SuppressWarnings("static-access")
	public MqConnectionFactory(String host, String port, String userName, String passWord) {
		this.host = host;
		this.port = port;
		this.userName = userName;
		this.passWord = passWord;
	}

	/**
	 * 初始化mq，建立mq服务连接对象
	 * 
	 * @return 成功建立连接返回0，失败返回-1
	 */
	public synchronized int initMq() {
		try {
			if(host != null && port != null &&  userName != null &&  passWord != null){
				getConnection();
			}
		} catch (IOException | TimeoutException e) {
			e.getStackTrace();
		}
		if (connection != null && connection.isOpen()) {
			return 0;
		}
		return -1;
	}
	
	/**
	 * 获取与服务器的连接
	 * 
	 * @return 连接对象
	 * @throws IOException
	 * @throws TimeoutException
	 */
	private static Connection getConnection() throws IOException, TimeoutException {
		if (factory == null) {
			factory = new ConnectionFactory();
			factory.setHost(host);
			factory.setPort(Integer.valueOf(port));
			factory.setUsername(userName);
			factory.setPassword(passWord);
		}
		if (connection == null) {
			connection = factory.newConnection();
		}
		return connection;
	}

	/**
	 * 开起消息传输通道
	 * 
	 * @param queueName
	 *            传输队列名称，不可为空
	 * @param isPersist
	 *            是否持久化队列，不可为null
	 * @param isRPCClient
	 *            是否开起RPC Client通道，不可为null
	 * @return 通道开起成果返回MqChannel对象，失败返回null
	 */
	public MqChannel openChannel(String queueName, boolean isPersist, boolean isRPCClient) {
		MqChannel mqChannel = new MqChannel();
		if (connection != null && connection.isOpen()) {
			openNomalChannel(mqChannel, queueName, isPersist, isRPCClient);
		}
		return mqChannel;
	}

	/**
	 * 开起消息发布订阅传输通道
	 * 
	 * @param topicName
	 *            topic名称，该项参数不能为空。
	 * @param isPersist
	 *            是否持久化队列，不可为null
	 * @param queueName
	 *            传输队列名称，不可为空
	 * @return 通道开起成果返回MqChannel对象，失败返回null
	 */
	public MqChannel openTopicChannel(String topicName, boolean isPersist, String queueName) {
		MqChannel mqChannel = new MqChannel();
		mqChannel.setPersist(isPersist);
		mqChannel.setQueueName(queueName);
		if (connection != null && connection.isOpen()) {
			openExchangeChannel(mqChannel, topicName, ExchangeType.topic);
		}
		return mqChannel;
	}

	/**
	 * 开起topic通道
	 * 
	 * @param mqChannel
	 *            MqChannel通道对象
	 * @param topicName
	 *            topic名称，该项参数不能为空。
	 * @param exchangeType
	 *            exchange方式
	 */
	private void openExchangeChannel(MqChannel mqChannel, String topicName, ExchangeType exchangeType) {
		Channel channel = null;
		try {
			channel = connection.createChannel();
			channel.exchangeDeclare(topicName, exchangeType.name(), mqChannel.isPersist(), true, false, null);
			mqChannel.setChannel(channel);
			mqChannel.setExchangeName(topicName);
			mqChannel.setExchangeType(exchangeType);
			if (!mqChannel.isPersist()) {
				mqChannel.setQueueName(channel.queueDeclare().getQueue());
			} else {
				channel.queueDeclare(mqChannel.getQueueName(), mqChannel.isPersist(), true, true, null);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 开起普通通道
	 * 
	 * @param mqChannel
	 *            MqChannel通道对象
	 * @param queueName
	 *            队列名称
	 * @param isPersist
	 *            是否持久化队列
	 * @param isRPCClient
	 *            是否开起RPC Client通道，不可为null
	 */
	private void openNomalChannel(MqChannel mqChannel, String queueName, boolean isPersist, boolean isRPCClient) {
		Channel channel = null;
		try {
			channel = connection.createChannel();
			if(isRPCClient){
				String replyQueueName = channel.queueDeclare().getQueue();
				QueueingConsumer consumer = new QueueingConsumer(channel);
				mqChannel.setReplyQueueName(replyQueueName);
				mqChannel.setConsumer(consumer);
				channel.basicConsume(replyQueueName, autoAck, consumer);
			} else {
				channel.queueDeclare(queueName, isPersist, false, true, null);
			}
			mqChannel.setChannel(channel);
			mqChannel.setQueueName(queueName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 推送消息
	 * 
	 * @param msgDes
	 *            消息头对象，存放消息相关描述
	 * @param pBuf
	 *            消息体，字节数组
	 * @param mqChannel
	 *            MqChannel通道对象
	 * @return 推送成果返回0，失败返回-1
	 */
	public int putData(MsgDes msgDes, byte[] pBuf, MqChannel mqChannel) {
		try {
			mqChannel.getChannel().basicPublish("", mqChannel.getQueueName(), null, parserData(msgDes, pBuf));
			return 0;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * 获取消息
	 * 
	 * @param consumer
	 *            MqConsumer消费者对象，用于接受处理消息
	 * @return 成功获取消息返回0，失败返回-1
	 */
	public int getData(MqConsumer consumer) {
		try {
			consumer.getMqChannel().getChannel().basicConsume(consumer.getMqChannel().getQueueName(), autoAck,
					consumer.getConsumer());
			return 0;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * 发布消息
	 * 
	 * @param msgDes
	 *            消息头对象，存放消息相关描述
	 * @param pBuf
	 *            消息体，字节数组
	 * @param mqChannel
	 *            MqChannel通道对象
	 * @param filterKey
	 *            过滤关键字
	 * @return 消息发布成功返回0，失败返回-1
	 */
	public int publish(MsgDes msgDes, byte[] pBuf, MqChannel mqChannel, String filterKey) {
		try {
			mqChannel.getChannel().basicPublish(mqChannel.getExchangeName(), filterKey, null,
					parserData(msgDes, pBuf));
			return 0;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * 接受订阅消息
	 * 
	 * @param consumer
	 *            MqConsumer消费者对象，用于接受处理消息
	 * @param filterKey
	 *            过滤关键字
	 * @return 接受消息失败返回-1
	 */
	public int subscribe(MqConsumer consumer, String filterKey) {
		Channel channel = consumer.getMqChannel().getChannel();
		try {
			channel.queueBind(consumer.getMqChannel().getQueueName(), consumer.getMqChannel().getExchangeName(), filterKey);
			channel.basicConsume(consumer.getMqChannel().getQueueName(), autoAck, consumer.getConsumer());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * 启用远程过程调用服务器端
	 * 
	 * @param mqChannel
	 *            mq通道对象
	 * @param procedure
	 *            远程过程实现
	 * @return 远程过程启用失败返回-1
	 */
	public int rpcServer(MqChannel mqChannel, Procedure procedure) {
		Channel ch = mqChannel.getChannel();
		try {
			ch.basicQos(1);
			QueueingConsumer consumer = new QueueingConsumer(ch);
			ch.basicConsume(mqChannel.getQueueName(), false, consumer);
			while (true) {
				byte[] response = null;
				QueueingConsumer.Delivery delivery = null;
				try {
					delivery = consumer.nextDelivery();
				} catch (ShutdownSignalException | ConsumerCancelledException | InterruptedException e1) {
					e1.printStackTrace();
				}
				BasicProperties props = delivery.getProperties();
				BasicProperties replyProps = new BasicProperties.Builder().correlationId(props.getCorrelationId())
						.build();
				try {
					byte[] params = delivery.getBody();
					response = procedure.process(params);
				} catch (Exception e) {
					e.printStackTrace();
					response = null;
				} finally {
					ch.basicPublish("", props.getReplyTo(), replyProps, response);
					ch.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * 执行远程过程
	 * 
	 * @param mqChannel
	 *            mq通道对象
	 * @param params
	 *            远程过程参数
	 * @return 远程过程执行结果数组
	 */
	public byte[] rpcCall(MqChannel mqChannel, byte[] params) {
		Channel ch = mqChannel.getChannel();
		String corrId = UUID.randomUUID().toString();
		BasicProperties props = new BasicProperties.Builder().correlationId(corrId)
				.replyTo(mqChannel.getReplyQueueName()).build();
		try {
			ch.basicPublish("", mqChannel.getQueueName(), props, params);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		while (true) {
			QueueingConsumer.Delivery delivery = null;
			try {
				delivery = mqChannel.getConsumer().nextDelivery();
			} catch (ShutdownSignalException | ConsumerCancelledException | InterruptedException e) {
				e.printStackTrace();
			}
			if (delivery.getProperties().getCorrelationId().equals(corrId)) {
				return delivery.getBody();
			}
		}
	}

	public int putFile(String inputFilePath, MqChannel mqChannel, String queueName) {
		return -1;
	}

	public int getFile(String outputFilePath, MqChannel mqChannel, String queueName) {
		return -1;
	}

	/**
	 * 关闭传输通道
	 * 
	 * @param mqChannel
	 *            MqChannel通道对象
	 * @return 成功关闭通道返回0，失败返回-1
	 */
	public int closeChannel(MqChannel mqChannel) {
		Channel channel = mqChannel.getChannel();
		try {
			if(channel != null){
				if (channel.isOpen()) {
					channel.close();
				}
			}
			return 0;
		} catch (IOException | TimeoutException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * 关闭服务连接
	 * 
	 * @return 关闭成功返回0，失败返回-1
	 */
	public int closeMqConnection() {
		try {
			if (connection != null && connection.isOpen()) {
				connection.close();
			}
			return 0;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * 数据处理，将数据头和数据进行组合
	 * 
	 * @param msgDes
	 *            消息头对象
	 * @param pBuf
	 *            消息体
	 * @param nLen
	 *            消息体长度
	 * @return 数据包
	 */
	private byte[] parserData(MsgDes msgDes, byte[] pBuf) {
		byte[] msgDesBytes = msgDes.serialize();
		int checkSum = msgDesBytes.length + pBuf.length;
		byte[] data = new byte[checkSum];
		ByteArrayutil.combine(msgDesBytes, pBuf, data);
		return data;
	}
}