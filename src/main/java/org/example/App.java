package org.example;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Hello world!
 *
 */
public class App 
{
    public static final int PORT = 5000;
    public static void main( String[] args ) throws URISyntaxException {
        startServer();
        startClient();
    }

    public static void startServer() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                FileReceiver fileReceiver = new FileReceiver();
                fileReceiver.waitReceive();
            }
        });

        thread.setDaemon(false);
        thread.start();
    }
    public static void startClient() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                FileSender fileSender = new FileSender();
                fileSender.sendFile("C:\\Users\\GMJ\\Desktop\\Screenshot_20231011_234741.png");
                try {
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                fileSender.sendFile("C:\\Users\\GMJ\\Downloads\\1292623144-1-192.mp4");
//                fileSender.sendFile("D:\\gradle-6.5-bin.zip");
            }
        });

        thread.setDaemon(false);
        thread.start();
    }
}
