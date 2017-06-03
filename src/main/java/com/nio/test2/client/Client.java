package com.nio.test2.client;

public class Client {

	public static void main(String[] args) {
		new Thread(new MultiplexerClient("127.0.0.1",8888)).start();
	}
}
