package com.xidian.netty.app.time.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.Date;

/**
 * 文件描述：时间服务器处理器，用于对网络事件进行读写操作
 * 创建作者：陈苗
 * 创建时间：2017/6/1 12:00
 */
public class TimeServerHandler extends ChannelHandlerAdapter {

    private int counter;

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String body = (String) msg;
        System.out.println("The time server receive order:" + body + "; the counter is : " + ++counter);
        String currentTime = "Query Time Order".equalsIgnoreCase(body) ? new Date(System.currentTimeMillis()).toString() : "Bad Order";
        currentTime += System.getProperty("line.separator");
        ByteBuf resp = Unpooled.copiedBuffer(currentTime.getBytes());
        ctx.writeAndFlush(resp);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();/*作用是将信息发送队列中的消息写入到SocketChannel中发送给对方*/
    }
}
