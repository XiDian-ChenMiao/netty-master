package com.xidian.netty.app.time.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 授时服务器
 *
 */
public class TimeServer
{
    private final static Logger logger = LoggerFactory.getLogger(TimeServer.class);

    public void bind(int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();/*创建Reactor线程组，专门用于网络事件的处理*/
        try {
            ServerBootstrap sb = new ServerBootstrap();/*Netty用于启动NIO服务端的辅助启动类，目的是降低服务端的开发复杂度*/
            sb.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChildChannelHandler());/*绑定IO事件的处理类，主要用于处理网路IO事件，例如记录日志，对消息进行编解码等*/
            ChannelFuture cf = sb.bind(port).sync();/*绑定端口，同步等待成功*/
            cf.channel().closeFuture().sync();/*等待服务端监听端口关闭*/
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        } finally {
            bossGroup.shutdownGracefully();/*优雅退出，释放相关联的资源*/
            workerGroup.shutdownGracefully();
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
        new TimeServer().bind(port);
    }
}
