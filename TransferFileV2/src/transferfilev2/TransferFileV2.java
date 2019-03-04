/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transferfilev2;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.thrift.protocol.TCompactProtocol;
import org.apache.thrift.server.THsHaServer;
import org.apache.thrift.transport.TNonblockingServerSocket;
import org.apache.thrift.transport.TNonblockingServerTransport;
import org.apache.thrift.transport.TTransportException;
import thrift.services.FileUpload;
import transferfilev2.servers.UploadFileServer;

/**
 *
 * @author cpu11418
 */
public class TransferFileV2 {

    /**
     * @param args the command line arguments
     */
        
    public static void main(String[] args) {
        final int uploadPort = Integer.getInteger("upload", 10400);

        new Thread(() -> {
            try {
                TNonblockingServerTransport transport = new TNonblockingServerSocket(uploadPort);
                THsHaServer.Args serverAgs = new THsHaServer.Args(transport);
                serverAgs.protocolFactory(new TCompactProtocol.Factory());
                
                UploadFileServer fileUploadServer = new UploadFileServer();
                FileUpload.Processor<FileUpload.Iface> processor = new FileUpload.Processor<>(fileUploadServer);

                serverAgs.processor(processor);
                THsHaServer server = new THsHaServer((serverAgs));

                System.out.println("Start upload service.");
                server.serve();

            } catch (TTransportException ex) {
                Logger.getLogger(TransferFileV2.class.getName()).log(Level.SEVERE, null, ex);
            }

        }).start();
    }
    
}
