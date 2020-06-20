/*
The MIT License

Copyright (c) 2016-2020 kong <congcoi123@gmail.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package com.tenio.common.middleware;

import java.io.IOException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;
import com.tenio.common.configuration.CommonConfiguration;
import com.tenio.common.logger.AbstractLogger;
import com.tenio.message.codec.MsgPackUtils;

/**
 * @author kong
 */
public class RabbitMessage extends AbstractLogger {

	private RabbitMessageApi messageApi = new RabbitMessageApi();
	private Connection __connection;
	private Channel __channel;
	private QueueingConsumer __consumer;

	public RabbitMessage(CommonConfiguration configuration) {
		try {

			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(configuration.getMiddleHost());
			factory.setUsername(configuration.getMiddleName());
			factory.setPassword(configuration.getMiddlePass());

			connection = factory.newConnection();
			channel = connection.createChannel();

			channel.queueDeclare(configuration.getMiddleQueue(), false, false, false, null);
			channel.basicQos(1);

			consumer = new QueueingConsumer(channel);
			channel.basicConsume(configuration.getMiddleQueue(), false, consumer);

		} catch (Exception e) {
			error("EXCEPTION MESSAGE", "system", e);
		}
	}

	public void listen() {
		Map<String, Object> response = new ConcurrentHashMap<String, Object>();
		while (true) {
			QueueingConsumer.Delivery delivery;
			try {
				delivery = consumer.nextDelivery();
				BasicProperties props = delivery.getProperties();
				BasicProperties replyProps = new BasicProperties.Builder().correlationId(props.getCorrelationId())
						.build();
				//
				try {
					// Convert to Map
					Map<String, Object> request = MsgPackUtils.unserialize(delivery.getBody());
					response = messageApi.get(request);
					if (response.isEmpty()) {
						trace("RABBIT", loggen("Can not process request: ", request.toString()));
					}
				} catch (Exception e) {
					error("EXCEPTION MESSAGE", "system", e);
				} finally {
					try {
						channel.basicPublish("", props.getReplyTo(), replyProps, MsgPackUtils.serialize(response));
						channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
					} catch (IOException e1) {
						error("EXCEPTION MESSAGE", "system", e1);
					}
					response.clear();
				}
			} catch (ShutdownSignalException | ConsumerCancelledException | InterruptedException e2) {
				error("EXCEPTION MESSAGE", "system", e2);
			}
		}
	}

	public void close() {
		if (connection != null) {
			try {
				connection.close();
			} catch (IOException e) {
				error("EXCEPTION MESSAGE", "system", e);
			}
		}
	}

}
