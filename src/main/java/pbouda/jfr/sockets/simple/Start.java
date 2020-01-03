package pbouda.jfr.sockets.simple;

import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;
import pbouda.jfr.sockets.Jfr;
import pbouda.jfr.sockets.NamedThreadFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Start {

    private static final Lorem LOREM = LoremIpsum.getInstance();

    public static void main(String[] args) throws IOException, InterruptedException {
        Jfr.start("jdk.SocketRead", "jdk.SocketWrite");

        ExecutorService serverExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("server-handler"));
        serverExecutor.submit(() -> {
            try (ServerSocket ss = new ServerSocket(5056)) {
                while (true) {
                    Socket socket = ss.accept();
                    System.out.println("A new client is connected : " + socket);
                    new ClientHandler(socket).start();
                }
            }
        });

        Thread.sleep(3000);

        try (var soc = new Socket("localhost", 5056);
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
                    String message = LOREM.getName();
                    dos.writeUTF(message);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
