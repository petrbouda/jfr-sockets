package pbouda.jfr.sockets.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.oio.OioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.util.concurrent.Future;
import pbouda.jfr.sockets.NamedThreadFactory;

import java.net.URI;
import java.net.URISyntaxException;

public class Client implements AutoCloseable {

    private final EventLoopGroup group;

    private static final URI URI;

    static {
        try {
            URI = new URI("ws://127.0.0.1:8080/ws");
        } catch (URISyntaxException e) {
            throw new RuntimeException("Cannot parse WS Address", e);
        }
    }

    public Client() {
        // this.group = new NioEventLoopGroup(0, new NamedThreadFactory("client-oioEventLoopGroup"));
        this.group = new OioEventLoopGroup(0, new NamedThreadFactory("client-oioEventLoopGroup"));
    }

    public Channel connect() throws InterruptedException {
        var handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                URI, WebSocketVersion.V13, null, true, new DefaultHttpHeaders());

        var wsHandshakeHandler = new WebSocketClientHandler(handshaker);

        Bootstrap bootstrap = new Bootstrap()
                .group(group)
                // .channel(EpollSocketChannel.class)
                // .channel(NioSocketChannel.class)
                .channel(OioSocketChannel.class)
                .handler(new CustomClientInitializer(wsHandshakeHandler));

        ChannelFuture channelFuture = bootstrap.connect(URI.getHost(), URI.getPort()).sync()
                .addListener(f -> System.out.println("Client connected"))
                .syncUninterruptibly();

        wsHandshakeHandler.handshakeFuture().syncUninterruptibly();

        return channelFuture.channel();
    }

    @Override
    public void close() {
        Future<?> boss = group.shutdownGracefully();
        boss.syncUninterruptibly();
    }

    private static class CustomClientInitializer extends ChannelInitializer<SocketChannel> {

        private final ChannelHandler handler;

        private CustomClientInitializer(ChannelHandler handler) {
            this.handler = handler;
        }

        @Override
        protected void initChannel(SocketChannel channel) {
            channel.pipeline()
                    .addLast(new HttpClientCodec())
                    .addLast(new HttpObjectAggregator(8192))
                    .addLast(WebSocketClientCompressionHandler.INSTANCE)
                    .addLast(handler);
        }
    }
}
