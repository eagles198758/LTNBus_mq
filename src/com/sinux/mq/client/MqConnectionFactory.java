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
 * mq���ӹ�����
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
	 * mq�����๹�췽��
	 * 
	 * @param host
	 *            ��������ַ
	 * @param port
	 *            �˿ں�
	 * @param userName
	 *            �û���
	 * @param passWord
	 *            ����
	 */
	@SuppressWarnings("static-access")
	public MqConnectionFactory(String host, String port, String userName, String passWord) {
		this.host = host;
		this.port = port;
		this.userName = userName;
		this.passWord = passWord;
	}

	/**
	 * ��ʼ��mq������mq�������Ӷ���
	 * 
	 * @return �ɹ��������ӷ���0��ʧ�ܷ���-1
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
	 * ��ȡ�������������
	 * 
	 * @return ���Ӷ���
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
	 * ������Ϣ����ͨ��
	 * 
	 * @param queueName
	 *            ����������ƣ�����Ϊ��
	 * @param isPersist
	 *            �Ƿ�־û����У�����Ϊnull
	 * @param isRPCClient
	 *            �Ƿ���RPC Clientͨ��������Ϊnull
	 * @return ͨ������ɹ�����MqChannel����ʧ�ܷ���null
	 */
	public MqChannel openChannel(String queueName, boolean isPersist, boolean isRPCClient) {
		MqChannel mqChannel = new MqChannel();
		if (connection != null && connection.isOpen()) {
			openNomalChannel(mqChannel, queueName, isPersist, isRPCClient);
		}
		return mqChannel;
	}

	/**
	 * ������Ϣ�������Ĵ���ͨ��
	 * 
	 * @param topicName
	 *            topic���ƣ������������Ϊ�ա�
	 * @param isPersist
	 *            �Ƿ�־û����У�����Ϊnull
	 * @param queueName
	 *            ����������ƣ�����Ϊ��
	 * @return ͨ������ɹ�����MqChannel����ʧ�ܷ���null
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
	 * ����topicͨ��
	 * 
	 * @param mqChannel
	 *            MqChannelͨ������
	 * @param topicName
	 *            topic���ƣ������������Ϊ�ա�
	 * @param exchangeType
	 *            exchange��ʽ
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
	 * ������ͨͨ��
	 * 
	 * @param mqChannel
	 *            MqChannelͨ������
	 * @param queueName
	 *            ��������
	 * @param isPersist
	 *            �Ƿ�־û�����
	 * @param isRPCClient
	 *            �Ƿ���RPC Clientͨ��������Ϊnull
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
	 * ������Ϣ
	 * 
	 * @param msgDes
	 *            ��Ϣͷ���󣬴����Ϣ�������
	 * @param pBuf
	 *            ��Ϣ�壬�ֽ�����
	 * @param mqChannel
	 *            MqChannelͨ������
	 * @return ���ͳɹ�����0��ʧ�ܷ���-1
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
	 * ��ȡ��Ϣ
	 * 
	 * @param consumer
	 *            MqConsumer�����߶������ڽ��ܴ�����Ϣ
	 * @return �ɹ���ȡ��Ϣ����0��ʧ�ܷ���-1
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
	 * ������Ϣ
	 * 
	 * @param msgDes
	 *            ��Ϣͷ���󣬴����Ϣ�������
	 * @param pBuf
	 *            ��Ϣ�壬�ֽ�����
	 * @param mqChannel
	 *            MqChannelͨ������
	 * @param filterKey
	 *            ���˹ؼ���
	 * @return ��Ϣ�����ɹ�����0��ʧ�ܷ���-1
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
	 * ���ܶ�����Ϣ
	 * 
	 * @param consumer
	 *            MqConsumer�����߶������ڽ��ܴ�����Ϣ
	 * @param filterKey
	 *            ���˹ؼ���
	 * @return ������Ϣʧ�ܷ���-1
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
	 * ����Զ�̹��̵��÷�������
	 * 
	 * @param mqChannel
	 *            mqͨ������
	 * @param procedure
	 *            Զ�̹���ʵ��
	 * @return Զ�̹�������ʧ�ܷ���-1
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
	 * ִ��Զ�̹���
	 * 
	 * @param mqChannel
	 *            mqͨ������
	 * @param params
	 *            Զ�̹��̲���
	 * @return Զ�̹���ִ�н������
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
	 * �رմ���ͨ��
	 * 
	 * @param mqChannel
	 *            MqChannelͨ������
	 * @return �ɹ��ر�ͨ������0��ʧ�ܷ���-1
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
	 * �رշ�������
	 * 
	 * @return �رճɹ�����0��ʧ�ܷ���-1
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
	 * ���ݴ���������ͷ�����ݽ������
	 * 
	 * @param msgDes
	 *            ��Ϣͷ����
	 * @param pBuf
	 *            ��Ϣ��
	 * @param nLen
	 *            ��Ϣ�峤��
	 * @return ���ݰ�
	 */
	private byte[] parserData(MsgDes msgDes, byte[] pBuf) {
		byte[] msgDesBytes = msgDes.serialize();
		int checkSum = msgDesBytes.length + pBuf.length;
		byte[] data = new byte[checkSum];
		ByteArrayutil.combine(msgDesBytes, pBuf, data);
		return data;
	}
}