package org.example;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class FileReceiver {
    private ExecutorService executorService;
    LinkedBlockingQueue<Socket> socketQueue = new LinkedBlockingQueue<>();
    private ServerSocket serverSocket;

    public FileReceiver() {
        executorService = Executors.newCachedThreadPool();
    }

    public void close() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void waitReceive() {
        try {
            serverSocket = new ServerSocket(5000);
            while (true) {
                Socket client = serverSocket.accept();
                executorService.execute(new Runnable() {
                    @Override
                    public void run() {
                        receiveFile(client);
                    }
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void receiveFile(Socket client) {
        System.out.println("receiver: " + Thread.currentThread().getName());
        try {
            while (true) {
                DataInputStream dataInputStream = new DataInputStream(client.getInputStream());
                String fileName = dataInputStream.readUTF();
                FileUtils.copyInputStreamToFile(dataInputStream, new File(fileName));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void copyFile(String srcFilePath, OutputStream outputStream, Consumer<Integer> onProgressChange) {
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(srcFilePath));
            byte[] buffer = new byte[1024];
            int lengthRead;
            int hasWrite = 0;
            while ((lengthRead = in.read(buffer)) > 0) {
                outputStream.write(buffer, 0, lengthRead);
                hasWrite += lengthRead;
                onProgressChange.accept(hasWrite * 100 / in.available());
                outputStream.flush();
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
