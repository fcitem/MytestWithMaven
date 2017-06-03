package com.nio.test2.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * @author fengchao
 * @data 2017年6月3日
 * @注释 接收通知回调的业务handle
 */
public class ReadComplete implements CompletionHandler<Integer,ByteBuffer>{

	AsynchronousSocketChannel socketChanel;
	public ReadComplete(AsynchronousSocketChannel result) {
		super();
		if (this.socketChanel==null) {
			this.socketChanel=result;
		}
	}

	@Override
	public void completed(Integer result, ByteBuffer attachment) {

		attachment.flip();
		byte[] body=new byte[attachment.remaining()];   //reamaining 元素个数
		attachment.get(body);
		try {
			String request=new String(body,"utf-8");
			System.out.println("**version-2.0** The Aio Server receive a msg:"+request);
			dowrite();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private void dowrite() {

		String response="welcome you online Success！";
		ByteBuffer buffer=ByteBuffer.allocate(response.getBytes().length);
		buffer.put(response.getBytes());
		buffer.flip();
		socketChanel.write(buffer,buffer,new CompletionHandler<Integer, ByteBuffer>() {

			@Override
			public void completed(Integer result, ByteBuffer attachment) {
				//若果没有发送完成,继续发送
				if (attachment.hasRemaining()) {
					socketChanel.write(attachment,attachment,this);
				}
			}

			@Override
			public void failed(Throwable exc, ByteBuffer attachment) {
				try {
					socketChanel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		});
	}

	@Override
	public void failed(Throwable exc, ByteBuffer attachment) {
		try {
			this.socketChanel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
