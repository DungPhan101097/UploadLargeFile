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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TCompactProtocol;
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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws TTransportException, IOException, FileNotFoundException, InterruptedException, TException {
        if (args.length < 1) {
            System.out.println("Enter file name!");
        } else {
            for (int i = 0; i < args.length; i++) {
                uploadFile(args[i], new TFramedTransport(new TSocket(address, uploadPort)));
            } 
        }
    }

    private static void uploadFile(String fileName, TTransport socket) throws FileNotFoundException, TTransportException, IOException, InterruptedException, TException {

        File file = new File(fileName);

        if (!file.exists()) {
            System.out.println("File not found " + file.getAbsolutePath());
        }

        FileInfo fileInfo = new FileInfo();
        fileInfo.fileName = fileName;
        fileInfo.length = file.length();
        fileInfo.ip = getIp();
        
        socket.open();
        TCompactProtocol protocol = new TCompactProtocol(socket);
        FileUpload.Client client = new FileUpload.Client(protocol);

        final byte[] data = new byte[BUFFER_SIZE];
   
        Thread t1 = new Thread(() -> {
            try (BufferedInputStream buffIn = new BufferedInputStream(new FileInputStream(file))) {
                int byteRead = 0;
                int stt = 0;
                while ((byteRead = buffIn.read(data)) != -1) {
                    fileInfo.stt = stt++;
                    if (byteRead != BUFFER_SIZE) {
                        byte[] dataFinal = Arrays.copyOf(data, byteRead);
                        fileInfo.chunk = ByteBuffer.wrap(dataFinal);
                        
                    } else {
                        fileInfo.chunk = ByteBuffer.wrap(data);
                    }
                    client.uploadFile(fileInfo, BUFFER_SIZE);
                }
            } catch (FileNotFoundException ex) {
                Logger.getLogger(TransferFileClient.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException | TException ex) {
                Logger.getLogger(TransferFileClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        t1.start();
    }
    
    private static String getIp() {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostAddress();
        } catch (UnknownHostException ex) {
            Logger.getLogger(TransferFileClient.class.getName()).log(Level.SEVERE, null, ex);
        }
      
       return "";
    }
}
