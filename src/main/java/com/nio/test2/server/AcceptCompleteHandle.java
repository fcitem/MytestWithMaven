package com.nio.test2.server;

import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;

/**
 * @author fengchao
 * @data 2017年6月3日
 * @注释 前面的任务完成后将通过回调接口进入此类中的相关方法
 */
public class AcceptCompleteHandle implements CompletionHandler<AsynchronousSocketChannel,AsyncAioServer> {

	 /* 成功接收到客户端的链接
	 * attachment 通知回调的时候作为入参使用 
	 */
	@Override
	public void completed(AsynchronousSocketChannel result, AsyncAioServer attachment) {
		//异步循环接收客户端新连接
		attachment.serverSocketChannel.accept(attachment,this);
		ByteBuffer buffer=ByteBuffer.allocate(1024);
		result.read(buffer,buffer,new ReadComplete(result));   //异步读取
	}

	@Override
	public void failed(Throwable exc, AsyncAioServer attachment) {
		exc.printStackTrace();
	}


}
