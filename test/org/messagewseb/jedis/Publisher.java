package org.messagewseb.jedis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import redis.clients.jedis.Jedis;

public class Publisher {

	private static final Logger logger = Logger.getLogger(Publisher.class);

	private final Jedis publisherJedis;

	private final String channel;

	Jedis subscriberJedis;

	Subscriber sub;

	public Publisher(Jedis publisherJedis, String channel, Jedis subscriberJedis, Subscriber sub) {
		this.publisherJedis = publisherJedis;
		this.channel = channel;
		this.subscriberJedis = subscriberJedis;
		this.sub = sub;
	}

	public void start() {
		logger.info("Type your message (quit for terminate)");
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

			while (true) {
				String line = reader.readLine();

				if (!"quit".equals(line)) {
					if (line.startsWith("sub ")) {
						String[] parts = line.split(" ");
						System.out.println("subscribing to " + parts[1]);
						sub.subscribe(parts[1]);
					} else if (line.startsWith("unsub ")) {
						String[] parts = line.split(" ");
						System.out.println("um-subscribing to " + parts[1]);
						sub.unsubscribe(parts[1]);
					} else if (line.startsWith("pub ")) {
						String[] parts = line.split(" ");
						publisherJedis.publish(parts[1], parts[2]);
					} else
						publisherJedis.publish(channel, line);
				} else {
					break;
				}
			}

		} catch (IOException e) {
			logger.error("IO failure while reading input, e");
		}
	}
}