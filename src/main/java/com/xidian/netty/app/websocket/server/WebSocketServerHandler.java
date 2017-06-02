package com.xidian.netty.app.websocket.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

import static io.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static io.netty.handler.codec.http.HttpHeaders.setContentLength;

/**
 * 文件描述：WebSocket处理器
 * 创建作者：陈苗
 * 创建时间：2017/6/1 22:03
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<Object> {
    
    private final static Logger logger = LoggerFactory.getLogger(WebSocketServerHandler.class);
    
    private WebSocketServerHandshaker handshaker;
    
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        if (o instanceof FullHttpRequest) 
            handleHttpRequest(channelHandlerContext, (FullHttpRequest) o);
        else if (o instanceof WebSocketFrame)
            handleWebSocketFrame(channelHandlerContext, (WebSocketFrame) o);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 处理WebSocket请求
     * @param ctx
     * @param req
     */
    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame req) {
        /*判断是否是关闭链路的指令*/
        if (req instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) req.retain());
            return;
        }
        /*如果是Ping消息*/
        if (req instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(req.content().retain()));
            return;
        }
        if (!(req instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", req.getClass().getName()));
        }
        String request = ((TextWebSocketFrame) req).text();
        if (logger.isInfoEnabled())
            logger.info(String.format("%s received %s", ctx.channel(), req));
        ctx.channel().write(new TextWebSocketFrame(request + ", 欢迎使用Netty WebSocket服务，现在时刻：" + new Date().toString()));
    }

    /**
     * 处理一般HTTP请求
     * @param ctx
     * @param req
     */
    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        if (!req.getDecoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        WebSocketServerHandshakerFactory wsf = new WebSocketServerHandshakerFactory("ws://localhost:9000/websocket", null, false);
        handshaker = wsf.newHandshaker(req);
        if (handshaker == null)
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        else
            handshaker.handshake(ctx.channel(), req);
    }

    /**
     * 返回响应给客户端
     * @param ctx
     * @param req
     * @param res
     */
    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res) {
        if (res.getStatus().code() != 200) {/*返回响应给客户端*/
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            setContentLength(res, res.content().readableBytes());
        }
        ChannelFuture cf = ctx.channel().writeAndFlush(res);
        if (!isKeepAlive(req) || res.getStatus().code() != 200)/*如果是非Keep-Alive，则关闭连接*/
            cf.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
