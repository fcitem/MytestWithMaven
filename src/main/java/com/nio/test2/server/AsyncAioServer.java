package com.nio.test2.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;

/**
 * @author fengchao
 * @data 2017年6月3日
 * @注释 异步服务端通讯任务
 */
public class AsyncAioServer implements Runnable{

	private int port;
	AsynchronousServerSocketChannel serverSocketChannel;   //jdk1.7之后引入的异步服务器通道
	public AsyncAioServer(int port) {
		this.port=port;
		try {
			serverSocketChannel=AsynchronousServerSocketChannel.open();
			serverSocketChannel.bind(new InetSocketAddress(port));
			System.out.println("**version-1.0** The server is start in port:"+port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		serverSocketChannel.accept(this,new AcceptCompleteHandle());
		try {
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}

}
