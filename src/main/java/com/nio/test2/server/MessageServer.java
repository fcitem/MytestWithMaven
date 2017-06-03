package com.nio.test2.server;

/**
 * @author fengchao
 * @data 2017年6月2日
 * @注释 基于jdk7原生nio编写测试Aio的用法
 */
public class MessageServer {

	public static void main(String[] args) {
		AsyncAioServer server=new AsyncAioServer(8888);
		new Thread(server).start();
	}
}
