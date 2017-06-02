package com.nio.test1.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class MultiplexerServer implements Runnable{

	private Selector selector;
	//ServerSocketChannel负责监听传入的连接和创建新的SocketChannel对象，它本身从不传输数据
	private ServerSocketChannel serverChannel;
	private volatile boolean stop;
	//创建分配缓冲区,在java堆中分配,不是直接内存分配(allocateDirect将划分直接内存,创建的时候比较慢,传输较快)
	ByteBuffer buffer=ByteBuffer.allocate(1024);  
	public MultiplexerServer(int port) {
		try {
			selector=Selector.open();    //创建选择器
			serverChannel=ServerSocketChannel.open();    //打开服务器套接字通道
			serverChannel.configureBlocking(false);     //设置非阻塞方式
			//获取与此通道关联的服务器套接字并绑定到端口号
			serverChannel.socket().bind(new InetSocketAddress(port),1024);  
			//向selector注册感兴趣的监听事件
			serverChannel.register(selector,SelectionKey.OP_ACCEPT);   
			System.out.println("**version-1.0** The server is start in port:"+port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void run() {
		while (! stop) {
			try {
				//返回一组处于就绪状态的selectkey集合,参数为超时时间
				selector.select(1000);
				//返回此选择器的已选择键集,也就是已经准备就绪的键集
				Set<SelectionKey> set=selector.selectedKeys();
				Iterator<SelectionKey> it=set.iterator();
				SelectionKey key=null;
				while (it.hasNext()) {
					key=it.next();
					it.remove();            //移除
					handleMsg(key);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (selector!=null) {
			try {
				//多路复用器关闭后，其上所有 注册的channel和资源都将自动关闭，不用手动关闭
				selector.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	/**
	 * @author fengchao
	 * @data 2017年6月2日
	 * @注释 消息处理函数
	 */
	public void handleMsg(SelectionKey key){
		buffer.clear();
		//键是否有效.键在创建时是有效的，并在被取消、其通道已关闭或者其选择器已关闭之前保持有效
		if(key.isValid()){
			//测试此键的通道是否已准备好接受新的套接字连接
			if(key.isAcceptable()){
				ServerSocketChannel ssc=(ServerSocketChannel) key.channel();   //获取此键的通道
				try {
					SocketChannel channel=ssc.accept();  //接受到此通道套接字的连接
					//读取消息到缓冲区,返回读取的字节数，可能为零，如果该通道已到达流的末尾，则返回 -1 
					int readbytes=channel.read(buffer);
					StringBuilder body=new StringBuilder();
					while (readbytes>0) {  //未读到末尾
				        //将position设置为0,limit设置为当前位置,然后处理的数据就是从position到limit的数据,也就是有效数据
						buffer.flip(); 
						byte[] bytes=new byte[buffer.remaining()]; //返回position到limit之间的元素数
						buffer.get(bytes);    //将缓冲区的元素复制到byte数组
						body.append(new String(bytes,"utf-8"));
						buffer.clear();
						readbytes=channel.read(buffer);
					}
					System.out.println("The Server received a msg:"+body);
					dowrite("Welcome you online",channel);   //写响应消息
					//取消此键的通道到其选择器的注册
					key.cancel();
					ssc.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public void dowrite(String responseMsg,SocketChannel channel){
		if(responseMsg!=null&&responseMsg.length()>0){
			byte[] bytes=responseMsg.getBytes();
			ByteBuffer buf=ByteBuffer.allocate(bytes.length);
			buf.put(bytes);
			System.out.println("position: "+buf.position());
			buf.flip();
			System.out.println("flip后position:"+buf.position());
			try {
				channel.write(buf);      //写入响应消息
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public boolean isStop() {
		return stop;
	}
	public void setStop(boolean stop) {
		this.stop = stop;
	}
	public void stop(){
		this.setStop(true);
	}

}
