package pbouda.jfr.sockets.netty.server;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

public class WebsocketEventHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final ChannelGroup channelGroup;

    WebsocketEventHandler(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext context, Object event) throws Exception {
        if (event instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            channelGroup.add(context.channel());
            System.out.println("Client connected: " + context.channel().remoteAddress());
//        } else if (event instanceof IdleStateEvent) {
//            /*
//             * Automatic PING - PONG mechanism in WebSocket?
//             */
//            IdleStateEvent e = (IdleStateEvent) event;
//            if (e.state() == IdleState.READER_IDLE) {
//                context.close();
//            } else if (e.state() == IdleState.WRITER_IDLE) {
//                context.writeAndFlush(new PingMessage());
//            }

        } else if (event == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_TIMEOUT) {
            System.out.println("WS HandshakeTimeout occurred: " + context.channel().remoteAddress());

        } else {
            super.userEventTriggered(context, event);
        }
    }

    @Override
    public void channelWritabilityChanged(ChannelHandlerContext context) throws Exception {
        if (!context.channel().isWritable()) {
            System.out.printf("Channel '%s' became not writable (probably slower consumer)\n", context.channel().remoteAddress());
        } else {
            System.out.printf("Channel '%s' became writable again\n", context.channel().remoteAddress());
        }

        super.channelWritabilityChanged(context);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext context, TextWebSocketFrame msg) {
        context.fireChannelRead(msg.retain());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext context, Throwable cause) {
        System.out.printf("An exception occurred, closing client " + context.channel().remoteAddress(), cause);
        context.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext context) {
        System.out.println("Closing WS Client: " + context.channel().remoteAddress());
    }
}
