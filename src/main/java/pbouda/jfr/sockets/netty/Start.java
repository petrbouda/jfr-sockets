package pbouda.jfr.sockets.netty;

import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;
import pbouda.jfr.sockets.Jfr;
import pbouda.jfr.sockets.netty.client.Client;
import pbouda.jfr.sockets.netty.server.Server;

public class Start {

    private static final Lorem LOREM = LoremIpsum.getInstance();

    private static final String CONFIG =
            "<configuration version=\"2.0\">"+
            "    <event name=\"jdk.SocketRead\">\n" +
            "        <setting name=\"enabled\">true</setting>\n" +
            "        <setting name=\"stackTrace\">true</setting>\n" +
            "        <setting name=\"threshold\" control=\"socket-io-threshold\">0 s</setting>\n" +
            "    </event>\n" +
            "    <event name=\"jdk.SocketWrite\">\n" +
            "        <setting name=\"enabled\">true</setting>\n" +
            "        <setting name=\"stackTrace\">true</setting>\n" +
            "        <setting name=\"threshold\" control=\"socket-io-threshold\">0 s</setting>\n" +
            "    </event>" +
            "</configuration>";

    public static void main(String[] args) throws InterruptedException {
        Jfr.start(CONFIG, "jdk.SocketRead", "jdk.SocketWrite");

        // ------------------
        //  Websocket Server
        // ------------------
        var server = new Server();
        Runtime.getRuntime().addShutdownHook(new Thread(server::close));

        server.start()
                .closeFuture()
                .addListener(f -> System.out.println("Websocket Server closed"));

        // -----------------
        // Websocket Client
        // -----------------
        var client = new Client();
        Runtime.getRuntime().addShutdownHook(new Thread(client::close));

        client.connect()
                .closeFuture()
                .addListener(v -> System.out.println("Websocket Client closed"));

        // -----------------
        // Sending Message
        // -----------------
        ChannelGroup channelGroup = server.getChannelGroup();

        // Keep the content of the same size - 10 bytes
        String message = "Rita Hayes";

        int i = 0;
        while (true) {
            Thread.sleep(1000);

            ByteBuf buffer = PooledByteBufAllocator.DEFAULT.buffer();
            // Different size of messages
            // String message = LOREM.getName();
            buffer.writeCharSequence(message, CharsetUtil.UTF_8);

            channelGroup.write(new TextWebSocketFrame(buffer))
                    .addListener(future -> {
                        if (!future.isSuccess()) {
                            future.cause().printStackTrace();
                        }
                    });

//            // Flush a bulk of 5 messages
//            if (i == 4) {
//                channelGroup.flush();
//                i = 0;
//            } else {
//                i++;
//            }
        }
    }
}
