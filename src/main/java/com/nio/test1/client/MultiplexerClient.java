package com.nio.test1.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class MultiplexerClient implements Runnable {

	private String host;
	private int port;
	private Selector selector;
	private SocketChannel socketChannel;
	private volatile boolean stop;

	public MultiplexerClient(String host, int port) {
		super();
		this.host = host;
		this.port = port;
		try {
			// 创建选择器
			selector = Selector.open();
			socketChannel = SocketChannel.open(); // 打开通道
			socketChannel.configureBlocking(false); // 非阻塞
			// 启用SO_REUSEADDR套接字选项,SO_REUSEADDR是让端口释放后立即就可以被再次使用,server程序总是应该在调用bind()之前设置SO_REUSEADDR套接字选项
			socketChannel.socket().setReuseAddress(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		try {
			connect();
		} catch (IOException e) {
			e.printStackTrace();
		}
		while (!stop) { // 轮询select
			try {
				selector.select(1000);
				Set<SelectionKey> set = selector.selectedKeys();
				Iterator<SelectionKey> it = set.iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					it.remove();
					SocketChannel chl = (SocketChannel) key.channel();
					if(key.isConnectable()){
						if(socketChannel.finishConnect()){    //完成连接
							socketChannel.register(selector, SelectionKey.OP_READ);
							dowrite(chl);
						}
					}
					if(key.isReadable()){
						ByteBuffer buffer = ByteBuffer.allocate(1024);
						int readbytes = chl.read(buffer);
						buffer.flip();
						byte[] bytes = new byte[readbytes];
						buffer.get(bytes);
						String msg = new String(bytes, "utf-8");
						System.out.println("**version 1.0** server: " + msg);
					}
				}
			} catch (Throwable e) {
				e.printStackTrace();
			}

		}
		if (selector != null) {
			try {
				// 多路复用器关闭后，其上所有 注册的channel和资源都将自动关闭，不用手动关闭
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void connect() throws IOException {
		try {
			if (socketChannel.connect(new InetSocketAddress(host, port))) {
				socketChannel.register(selector, SelectionKey.OP_READ);
				dowrite(socketChannel);
			} else {
				socketChannel.register(selector, SelectionKey.OP_CONNECT);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	private void dowrite(SocketChannel channel) {
		String request = "request login in";
		ByteBuffer buffer = ByteBuffer.allocate(request.getBytes().length);
		buffer.put(request.getBytes());
		buffer.flip();
		try {
			channel.write(buffer);
			if (!buffer.hasRemaining()) { // 判断是否发送完成,具体参见jdk
				System.out.println("**version 1.0** send msg: " + request);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
