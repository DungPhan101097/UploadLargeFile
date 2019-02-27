/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clients;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import thrift.services.FileInfo;
import thrift.services.FileUpload;

/**
 *
 * @author cpu11418
 */
public class TransferFileClient {

    private static final int BUFFER_SIZE = 1024 * 1024;
    private static final int uploadPort = Integer.getInteger("port.upload", 10400);
    private static final String address = System.getProperty("address", "localhost");
    private static final int chunk = 0;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws TTransportException, IOException, FileNotFoundException, InterruptedException, TException {
        
       uploadFile("data/SachCuaDung.pdf", new TFramedTransport(new TSocket(address, uploadPort)));
        
    }

    private static void uploadFile(String fileName, TTransport socket) throws FileNotFoundException, TTransportException, IOException, InterruptedException, TException {

        File file = new File(fileName);

        if (!file.exists()) {
            System.out.println("File not found " + file.getAbsolutePath());
        }

        FileInfo fileInfo = new FileInfo();
        fileInfo.fileName = fileName;
        fileInfo.length = file.length();
        fileInfo.ip = "192.78.90.09";
        

        socket.open();
        TProtocol protocol = new TBinaryProtocol(socket);
        FileUpload.Client client = new FileUpload.Client(protocol);
        byte[] data = new byte[BUFFER_SIZE];
        int byteRead = 0;
        int stt = 0;
        
        ArrayList<ByteBuffer> bbs = new ArrayList<>();
        
        try (BufferedInputStream buffIn = new BufferedInputStream(new FileInputStream(file))) {
            while ((byteRead = buffIn.read(data)) != -1) {
                fileInfo.stt = stt++;
                if (byteRead != BUFFER_SIZE) {
                    byte[] dataFinal = Arrays.copyOf(data, byteRead);
//                    bbs.add(ByteBuffer.wrap(dataFinal));
                    fileInfo.chunk = ByteBuffer.wrap(dataFinal);
                      client.uploadFile(fileInfo);
                } else {
//                    bbs.add(ByteBuffer.wrap(data));
                     
                    fileInfo.chunk = ByteBuffer.wrap(data);
                    client.uploadFile(fileInfo);
                }
            }
        } 
        
//        Thread t1 = new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                System.out.println(bbs.size());
//                for (int i = 0; i < bbs.size(); i++) {
//                    fileInfo.chunk = bbs.get(i);
//                    fileInfo.stt = i;
//                    try {
//                        client.uploadFile(fileInfo);
//                    } catch (TException ex) {
//                        Logger.getLogger(TransferFileClient.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//            }
//        });
        
//        Thread t2 = new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                for (int i = 0; i < bbs.size() / 2; i++) {
//                    fileInfo.chunk = bbs.get(i);
//                    fileInfo.stt = i;
//                    try {
//                        client.uploadFile(fileInfo);
//                    } catch (TException ex) {
//                        Logger.getLogger(TransferFileClient.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                }
//            }
//        });
        
//        t1.start();
//        t2.start();
//        
//        t1.join();
//        t2.join();
        
        client.uploadSuccess(true, fileName, fileInfo.ip);
        
        
        

        System.out.println("Success to upload " + fileInfo.fileName);

    }
    
    private static String getIp() {
       return "";
    }
}
