package pbouda.jfr.sockets;

import jdk.jfr.Configuration;
import jdk.jfr.consumer.EventStream;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedObject;
import jdk.jfr.consumer.RecordingStream;
import jdk.jfr.internal.tool.PrettyWriter;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Jfr {

    public static void start(String configuration, String... events) {
        try {
            Path config = Files.createTempFile(null, ".xml");
            Files.write(config, configuration.getBytes());
            Configuration configFile = Configuration.create(config);

            configFile.getSettings().forEach((key, value) -> System.out.println(key + ": " + value));

            ExecutorService executor = Executors.newSingleThreadExecutor(new NamedThreadFactory("jfr"));
            executor.submit(() -> {
                try (EventStream es = new RecordingStream(configFile)) {
                    for (String event : events) {
                        es.onEvent(event, e -> {
                            // String formatted = toString(e);
                            System.out.println(e);
                        });
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
        } catch (IOException | ParseException e) {
            throw new RuntimeException("Cannot create a configuration file", e);
        }
    }

//    public static String toString(RecordedObject event) {
//        StringWriter s = new StringWriter();
//        PrintWriter writer = new PrintWriter(s);
//        PrettyWriter p = new PrettyWriter(writer);
//        p.setStackDepth(64);
//        if (event instanceof RecordedEvent) {
//            p.print((RecordedEvent) event);
//        } else {
//            p.print(event, "");
//        }
//        p.flush(true);
//        return s.toString();
//    }
}
