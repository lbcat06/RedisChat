# Redis Chat Proof of Concept

Redis chat is a chat implementation of jedis: a library to communicate with a redis server through a java client. It is utilizing a JedisPool which is a collection of channels using PUB/SUB functionality of redis which allows for efficient and safe transfer of data.

## Notes
- All channels are named as follows: $channel;
- Due to restrictions in redis, retreiving all users online must be done by splitting the output of `jedis.clientList();`. This is done periodically and whenever a user enters a command.


## Commands
SUB: Subscribe to a channel, Allows user to receive messages from channel.
	SYNTAX : SUB <channel>

PUB: Publish to a channel, All messages sent after execution will be sent through this channel 
	SYNTAX: PUB <channel>

PM: Private message a user, the channel will be named using the following formula: '$user1~user2', both users will be subscribed to the channel automatically, and the user which executed the command will automatically start publishing to this channel.
	SYNTAX: PM <user>

## Features: 
- Chatroom
- Direct private messages 

## How does it work?
The message will be sent through the redis server and sent to everyone through the main channel. Everyone is automatically subscribed to this channel and commands are sent through this channel, they are all filtered and understood be each client individually.
