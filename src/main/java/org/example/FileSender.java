package org.example;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class FileSender {
    private ExecutorService executorService;
    LinkedBlockingQueue<Socket> socketQueue = new LinkedBlockingQueue<>();
    public FileSender() {
        executorService = Executors.newCachedThreadPool();
    }

    public void close() {
        while (!socketQueue.isEmpty()) {
            Socket socket = socketQueue.poll();
            try {
                socket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public void sendFile(String filePath) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println("sender: " + Thread.currentThread().getName());
                try {
                    Socket socket = null;
                    if (socketQueue.isEmpty()) {
                        socket = new Socket("127.0.0.1", 5000);
                    } else {
                        socket = socketQueue.poll();
                    }
                    DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                    String fileName = FilenameUtils.getName(filePath);
                    dataOutputStream.writeUTF(fileName);
                    copyFile(filePath, dataOutputStream, progress -> {
                        System.out.println(fileName + ": " + progress + "%");
                    });
                    dataOutputStream.flush();
                    socketQueue.put(socket);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static void copyFile(String srcFilePath, OutputStream outputStream, Consumer<Integer> onProgressChange) {
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(srcFilePath));
            long available = in.available();
            byte[] buffer = new byte[4096];
            int lengthRead;
            long hasWrite = 0;
            while ((lengthRead = in.read(buffer)) > 0) {
                outputStream.write(buffer, 0, lengthRead);
                hasWrite += lengthRead;
                int progress = (int) (hasWrite * 100 / available);
                if (progress < 0) {
                    System.out.println("error");
                }
                onProgressChange.accept(progress);
                outputStream.flush();
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
