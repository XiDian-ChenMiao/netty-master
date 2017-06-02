package com.xidian.netty.app.time.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 文件描述：授时客户端
 * 创建作者：陈苗
 * 创建时间：2017/6/1 12:14
 */
public class TimeClient {

    private final static Logger logger = LoggerFactory.getLogger(TimeClient.class);

    public void connect(int port, String host) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).handler(
                    new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new LineBasedFrameDecoder(1024));
                            socketChannel.pipeline().addLast(new StringDecoder());
                            socketChannel.pipeline().addLast(new TimeClientHandler());
                        }
                    }
            );
            ChannelFuture f = b.connect(host, port).sync();/*发起异步连接操作*/
            f.channel().closeFuture().sync();/*等待客户端链路关闭*/
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        } finally {
            group.shutdownGracefully();/*优雅退出，释放NIO线程组*/
        }
    }

    /**
     * 主函数调用
     * @param args
     */
    public static void main(String[] args) {
        int port = 9000;
        if (args != null && args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                logger.error(e.getMessage(), e);
            }
        }
        new TimeClient().connect(port, "127.0.0.1");
    }
}
