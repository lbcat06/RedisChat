import redis.clients.jedis.Client;
import redis.clients.jedis.Jedis;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class Publisher {
	private static Main main;
	private static Jedis jedisPub;
	private static String strKey;
	private static String channel;
	private static String messageSent;
	private static String s="";
	private static String message;
	private static ArrayList<String> usersOnline;
	private static String username;
	private static ArrayList<String> clientList;

	public Publisher() {
	}

	public Publisher(Jedis jedisPub, String channel) {
		this.jedisPub = jedisPub;
		this.channel = channel;
	}

	public void start() {
		setName();
		while(true) {

			Scanner in = new Scanner(System.in);
			message = in.nextLine();
			messageSent = channel + " | " + username + ": " + message;

			//Check for PM and to who
			if(message.toLowerCase().contains("SUB".toLowerCase())) {
				setName();
				if (message.split("\\w+").length == 2) {
					String[] pm = message.split(" ");
					String keyword = pm[0];
					String chanName = pm[1];
					//Is first word 'SUB'?
					if(keyword.equalsIgnoreCase("SUB") && !chanName.toLowerCase().equals(username.toLowerCase())) {
						//Create channel
						String channelName = "$"+chanName.toLowerCase();

						//Subscribe to channel
						main = new Main();
						main.subscribeChannel(channelName);

						//Publish to channel
						System.out.println("You have successfully opened a new channel " + channelName);
					} else {
						System.out.println("Cannot create a channel with yourself");
					}
				} else {
					System.out.println("message sent through pm else");
					jedisPub.publish(channel, messageSent);
				}
			}

			//Check for talk and to who
			else if(message.toLowerCase().contains("PUB".toLowerCase())) {
				setName();
				if (message.split("\\w+").length == 2) {
					String[] talk = message.split(" ");
					String keyword = talk[0];
					String chanName = talk[1];
					//Is first word 'PUB'?
					if(keyword.equalsIgnoreCase("PUB") && !chanName.toLowerCase().equals(username.toLowerCase())) {
						//If string matches a client ID(username/person) will PM
						//Create channel
						String channelName = "$"+chanName.toLowerCase();

						//Publish to channel
						channel = channelName;
						System.out.println("Publishing to channel " + channelName);
					} else {
						System.out.println("Cannot publish to yourself");
					}
				} else {
					//If message is not correct syntax will send message anyway
					System.out.println("message sent through pub else");
					jedisPub.publish(channel, messageSent);
				}
			}

			//PM People
			else if(message.toLowerCase().contains("PM".toLowerCase())) {
				setName();
				if (message.split("\\w+").length == 2) {
					String[] talk = message.split(" ");
					String keyword = talk[0];
					String person = talk[1];
					//Is first word 'pm'?
					if(keyword.equalsIgnoreCase("PM") && !person.toLowerCase().equals(username.toLowerCase())) {
						//If string matches a client ID(username/person) will PM
						if(usersOnline.contains(person.toLowerCase())) {
							//Create channel
							String channelName = "$" + username+"~"+person.toLowerCase();

							//Subscribe to channel
							main = new Main();
							main.subscribeChannel(channelName);

							//Publish to channel
							channel = channelName;
							System.out.println("Publishing and Subscribing to channel " + channelName);
							jedisPub.publish("channel", "mainchannel" + channelName);
							jedisPub.publish("channel", username + " has opened a PM with you");

						} else {
							System.out.println("No user exists by the name " + person);
						}
					} else {
						System.out.println("Cannot PM yourself");
					}
				} else {
					//If message is not correct syntax will send message anyway
					System.out.println("message sent through pm else");
					jedisPub.publish(channel, messageSent);
				}
			}

			//If user types qqq, quits
			else if (message.equalsIgnoreCase("qqq")) {
				System.out.println("Successfully quit");
				jedisPub.close();
				System.exit(1);
				break;
			}

			//Message is blank or message is more than 2000 characters
			else if (message.isBlank() || message.length()>2000) {
				System.out.print("Nice try");
			}

			//If message isn't blank publish message
			else if (!message.isBlank()) {
				//Publish message to channel
				jedisPub.publish(channel, messageSent);
			}
		}
	}

	private void setName() {
		jedisPub.clientSetname(username+"//~Client");
		String clientList = jedisPub.clientList();

		BufferedReader reader = new BufferedReader(new StringReader(clientList));
		usersOnline = new ArrayList<>();
		usersOnline.clear();

		try {
			//Go to 1st line
			String line = reader.readLine();

			while (line!=null) {
				//Get name=*
				String name = line.substring(line.indexOf("name="), line.indexOf("age") - 1);

				if(name.contains("//~Sub")) {
					//Go to next line
					line = reader.readLine();
				} else {

					//Remove "name="
					String[] parts = name.split("=");
					String splitName = parts[1];
					//Remove "//~Client"
					parts = splitName.split("//~");
					String finalName = parts[0];

					//Add all names to array to check
					usersOnline.add(finalName.toLowerCase());
					System.out.println(finalName);

					//Go to next line
					line = reader.readLine();
				}
			}

			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getMessage() {
		return messageSent;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

}
