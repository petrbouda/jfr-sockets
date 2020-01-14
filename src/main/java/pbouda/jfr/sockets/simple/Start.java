package pbouda.jfr.sockets.simple;

import jdk.jfr.Configuration;
import jdk.jfr.consumer.EventStream;
import jdk.jfr.consumer.RecordingStream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Start {

    public static void main(String[] args) throws IOException, InterruptedException, ParseException {
        Configuration cfg = Configuration.create(Path.of("custom-profile.xml"));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(() -> {
            try (EventStream es = new RecordingStream(cfg)) {
                es.onEvent("jdk.SocketRead", System.out::println);
                es.onEvent("jdk.SocketWrite", System.out::println);
                es.start();
            }
        });

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
