package com.example.demo.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo.command.CommandHandler;
import com.example.demo.command.CommandHolder;
import com.example.demo.config.NettyConfig;
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

import javax.validation.constraints.NotNull;
import java.text.ParseException;

/**
 * 接受/处理/响应客户端websocke请求的核心业务处理类
 */
public class WebSockeHandler extends SimpleChannelInboundHandler<Object> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private WebSocketServerHandshaker webSocketServerHandshaker;
    private static final String WEB_SOCKET_URL = "ws://localhost:8888/webSocket";

    //客户端与服务端创建链接的时候调用
    @Override
    public void channelActive (ChannelHandlerContext context)throws Exception{
        NettyConfig.group.add(context.channel());
        logger.warn("客户端与服务端连接开启");
    }
    //客户端与服务端断开连接的时候调用
    @Override
    public void channelInactive(ChannelHandlerContext context)throws Exception{
        NettyConfig.group.remove(context.channel());
        logger.warn("客户端与服务端连接断开");
    }
    //服务端接收客户端发送过来的数据结束之后调用
    @Override
    public void channelReadComplete(ChannelHandlerContext context)throws Exception{
        context.flush();
    }
    //工程出现异常的时候调用
    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable throwable)throws Exception{
        throwable.printStackTrace();
        context.close();
    }
    //服务端处理客户端websocke请求的核心方法
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        //处理客户端向服务端发起的http握手请求
        if (o instanceof FullHttpRequest){
            handHttpRequest(channelHandlerContext,(FullHttpRequest) o);
        }else if (o instanceof WebSocketFrame){//处理websocket链接业务
            handWebSocketFrame(channelHandlerContext,(WebSocketFrame) o);
        }
    }

    /**
     * 处理客户端与服务端之间的websocket业务
     * @param context
     * @param webSocketFrame
     */
    private void handWebSocketFrame(ChannelHandlerContext context,WebSocketFrame webSocketFrame)throws Exception{
        if (webSocketFrame instanceof CloseWebSocketFrame){//判断是否是关闭websocket的指令
            webSocketServerHandshaker.close(context.channel(),(CloseWebSocketFrame) webSocketFrame.retain());
        }
        if (webSocketFrame instanceof PingWebSocketFrame){//判断是否是ping消息
            context.channel().write(new PongWebSocketFrame(webSocketFrame.content().retain()));
            return;
        }
        if (!(webSocketFrame instanceof TextWebSocketFrame)){//判断是否是二进制消息
            logger.warn("不支持二进制消息");
            throw new RuntimeException(this.getClass().getName());
        }
        //返回应答消息
        //获取客户端向服务端发送的消息
        String reqParam = ((TextWebSocketFrame) webSocketFrame ).text();
        logger.warn("[websocket connection] receive message : " + reqParam);
        JSONObject recJson = vaildJson(reqParam);
        CommandHandler commandHandler = CommandHolder.getHandler(recJson.getString("cmd"));
        try {
            commandHandler.handle(recJson);
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(context.channel().id() + ":" + "你个笨比");
        //服务端向每个连接上来的客户端发送消息
        NettyConfig.group.writeAndFlush(textWebSocketFrame);
    }

    public JSONObject vaildJson(String recMsg) throws ParseException{
        JSONObject recJson = null;
        if(recMsg.startsWith("{")){
            return recJson = JSON.parseObject(recMsg);
        }
        throw new ParseException("Invaild JSON",0);
    }

    /**
     * 处理客户端向服务端发起http握手请求业务
     * @param context
     * @param fullHttpRequest
     */
    private void handHttpRequest(ChannelHandlerContext context,FullHttpRequest fullHttpRequest){
        if (!fullHttpRequest.getDecoderResult().isSuccess() ||!("websocket".equals(fullHttpRequest.headers().get("Upgrade")))){//判断是否http握手请求
            sendHttpResponse(context,fullHttpRequest, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }
        WebSocketServerHandshakerFactory webSocketServerHandshakerFactory = new WebSocketServerHandshakerFactory(WEB_SOCKET_URL,null,false);
        webSocketServerHandshaker = webSocketServerHandshakerFactory.newHandshaker(fullHttpRequest);
        if (webSocketServerHandshaker == null){
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(context.channel());
        }else{
            webSocketServerHandshaker.handshake(context.channel(),fullHttpRequest);
        }
    }

    /**
     * 服务端向客户端发送响应消息
     * @param context
     * @param fullHttpRequest
     * @param defaultFullHttpResponse
     */
    private void sendHttpResponse(ChannelHandlerContext context, FullHttpRequest fullHttpRequest, DefaultFullHttpResponse defaultFullHttpResponse){
        if (defaultFullHttpResponse.getStatus().code() != 200){
            ByteBuf buf = Unpooled.copiedBuffer(defaultFullHttpResponse.getStatus().toString(), CharsetUtil.UTF_8);
            defaultFullHttpResponse.content().writeBytes(buf);
            buf.release();
        }
        //服务端向客户端发送数据
        ChannelFuture future = context.channel().writeAndFlush(defaultFullHttpResponse);
        if (defaultFullHttpResponse.getStatus().code() !=200){
            future.addListener(ChannelFutureListener.CLOSE);
        }

    }

}
