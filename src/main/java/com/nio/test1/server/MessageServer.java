package com.nio.test1.server;

/**
 * @author fengchao
 * @data 2017年6月2日
 * @注释 基于jdk原生nio编写测试nio的用法
 */
public class MessageServer {

	public static void main(String[] args) {
		MultiplexerServer server=new MultiplexerServer(8888);
		new Thread(server).start();
	}
}
