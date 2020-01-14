package pbouda.jfr.sockets.simple;

import pbouda.jfr.sockets.Jfr;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Start {

    private static final String CONFIG =
            "<configuration version=\"2.0\">" +
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

    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
        Jfr.start(CONFIG, "jdk.SocketRead", "jdk.SocketWrite");

        ExecutorService serverExecutor = Executors.newSingleThreadExecutor();
        serverExecutor.submit(() -> {
            try (ServerSocket ss = new ServerSocket(5000)) {
                while (true) {
                    Socket socket = ss.accept();
                    System.out.println("A new client is connected : " + socket);
                    new ClientHandler(socket).start();
                }
            }
        });

        Thread.sleep(2000);

        try (var soc = new Socket("localhost", 5000);
             var dis = new DataInputStream(soc.getInputStream())) {

            while (true) {
                String received = dis.readUTF();
                System.out.println(received);
            }
        }
    }

    private static class ClientHandler extends Thread {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (DataOutputStream dos = new DataOutputStream(socket.getOutputStream()); socket) {
                while (true) {
                    Thread.sleep(1000);
                    dos.writeUTF("my-message");
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
