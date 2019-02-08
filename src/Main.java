import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisPubSub;

import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

	private static String username = "-NICE";
	private static String host = "localhost";
	private static String channel = "channel";
	private static Jedis jedis;
	private static JedisPubSub jps;
	private static String messageReceived;
	private static JedisPoolConfig poolConfig;
	private static JedisPool jedisPool;
	private static Publisher pub;


	public static void main(String[] args) {
		//Connecting to Redis server on localhost
		jedis = new Jedis(host, 6379);
		poolConfig = new JedisPoolConfig();
		jedisPool = new JedisPool(poolConfig, host);

		//Calling methods
		username();
		//connect();
		subscribe("channel");
		publish();
	}

	//Get username
	private static void username() {
		Scanner in = new Scanner(System.in);
		username = in.nextLine();

		while(username.contains(" ") || username.contains("~") || username.contains("`") || username.contains(",") || username.contains("'")) {
			System.out.println("Username cannot have any spaces, '~', '`', ''', and ',' Please input a valid username");
			username = in.nextLine();
		}

		System.out.println("Successfully connected as " + username);

		pub = new Publisher();
		pub.setUsername(username.toLowerCase());
	}

	//Check if message received is message published (to ignore)
	private static void checkMessage() {
		pub = new Publisher();

		Pattern p = Pattern.compile("^mainchannel\\$(.*)~(.*)$");
		Matcher m2 = p.matcher(messageReceived);

		//Check for channel names
		if(m2.find()) {
			String[] parts = messageReceived.split("~");
			String[] chanNameParts = messageReceived.split("\\$");

			String theirUsername = parts[0];
			String myUsername = parts[1];

			String channelName = "$"+chanNameParts[1];
			//Only receive channel name if directed towards you
			if (myUsername.contains(username)) {
				pub = new Publisher();
				pub.setChannel(channelName);

				subscribe(channelName);
				System.out.println(channelName);
			}
		} else {
			if(messageReceived.contains("has opened a PM with you")) {
				String[] parts = messageReceived.split("has");

				String usernameSender="";
				try {
					usernameSender = parts[0];
				} catch (ArrayIndexOutOfBoundsException e) {}

				if (!(usernameSender.equals(username))) {
					return;
				}
			}

			System.out.println(messageReceived);
		}
		//If not in main channel send message
	}

	//Create new channel
	public void subscribeChannel(String channel) {
		subscribe(channel);
	}

	//Subscribe thread
	private static void subscribe(String channel) {
		new Thread(new Runnable() {
			public void run() {
				jedis = new Jedis();

				jps = new JedisPubSub() {
					@Override
					public void onMessage(String channel, String message) {
						messageReceived = message;
						checkMessage();
					}
				};

				//Set client ID sub as username
				jedis.clientSetname(username.toLowerCase()+"//~Sub");

				//Start sub
				jedis.subscribe(jps, channel);
			}
		}).start();
	}

	//Publish thread
	private static void publish() {
		new Thread(new Runnable() {
			public void run() {
				Jedis publisher = jedisPool.getResource();
				new Publisher(publisher, channel).start();
			}
		}).start();
	}
}