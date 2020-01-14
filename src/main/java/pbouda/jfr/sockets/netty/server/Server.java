package pbouda.jfr.sockets.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.oio.OioServerSocketChannel;
import io.netty.util.concurrent.ImmediateEventExecutor;
import pbouda.jfr.sockets.NamedThreadFactory;

import java.lang.management.ManagementFactory;

public class Server implements AutoCloseable {

    private static final String WS_PATH = "/ws";
    private static final int PORT = 8080;

    private final ServerBootstrap bootstrap;
    private final ChannelGroup channelGroup;
    private final EventLoopGroup workerEventLoopGroup;

    public Server() {
        this.channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
        // this.workerEventLoopGroup = new EpollEventLoopGroup();
         this.workerEventLoopGroup = new NioEventLoopGroup(0, new NamedThreadFactory("server-nioEventLoopGroup"));
//        this.workerEventLoopGroup = new OioEventLoopGroup(0, new NamedThreadFactory("server-oioEventLoopGroup"));

        this.bootstrap = new ServerBootstrap()
                // .channel(EpollServerSocketChannel.class)
                 .channel(NioServerSocketChannel.class)
//                .channel(OioServerSocketChannel.class)
                .group(workerEventLoopGroup)
                .localAddress(8080)
                // .handler(new LoggingHandler(LogLevel.INFO))
                // .childOption(ChannelOption.SO_SNDBUF, 1024 * 1024)
                // .childOption(ChannelOption.SO_RCVBUF, 32 * 1024)
                .childHandler(new RouterChannelInitializer(channelGroup));

        /*
         * The maximum queue length for incoming connection indications
         * (a request to connect) is set to the backlog parameter. If
         * a connection indication arrives when the queue is full,
         * the connection is refused.
         */
        // bootstrap.option(ChannelOption.SO_BACKLOG, 100);
        // bootstrap.handler(new LoggingHandler(LogLevel.INFO));


        // Receive and Send Buffer - always be able to fill in an entire entity.
        // bootstrap.childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark.DEFAULT);
    }

    public Channel start() {
        ChannelFuture serverBindFuture = bootstrap.bind();
        serverBindFuture.addListener(f ->
                System.out.printf("PID %s - Broadcaster started on port '%s' and path '%s'",
                        ManagementFactory.getRuntimeMXBean().getPid(), PORT, WS_PATH));

        // Wait for the binding is completed
        serverBindFuture.syncUninterruptibly();
        return serverBindFuture.channel();
    }

    @Override
    public void close() {
        workerEventLoopGroup.shutdownGracefully()
                .syncUninterruptibly();
    }

    public ChannelGroup getChannelGroup() {
        return channelGroup;
    }
}
