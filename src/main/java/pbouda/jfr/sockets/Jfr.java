package pbouda.jfr.sockets;

import jdk.jfr.Configuration;
import jdk.jfr.consumer.EventStream;
import jdk.jfr.consumer.RecordedStackTrace;
import jdk.jfr.consumer.RecordedThread;
import jdk.jfr.consumer.RecordingStream;

import java.io.IOException;
import java.nio.file.Path;
import java.text.ParseException;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class Jfr {

    public static void start(String... events) {
        Configuration config;
        try {
            config = Configuration.create(Path.of("custom-profile.xml"));
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }

        ExecutorService executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("jfr"));
        executor.submit(() -> {
            try (EventStream es = new RecordingStream(config)) {
                for (String event : events) {
                    es.onEvent(event, System.out::println);
                }

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    try {
                        es.awaitTermination();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }));

                es.start();
            }
        });
    }
}
