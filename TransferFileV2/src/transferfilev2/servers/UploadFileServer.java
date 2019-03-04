/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package transferfilev2.servers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.thrift.TException;
import thrift.services.FileInfo;
import thrift.services.FileUpload;

/**
 *
 * @author cpu11418
 */
public class UploadFileServer implements FileUpload.Iface {

    private final File BASE_DIRECTORY = new File("storage");

    public UploadFileServer() {
        if (!BASE_DIRECTORY.exists()) {
            BASE_DIRECTORY.mkdir();
        }
    }

    @Override
    public boolean uploadFile(FileInfo info, long recordSize) throws TException {
        boolean resultFlag = false;

        String pathStorage = BASE_DIRECTORY.getAbsolutePath();
        String realFileName = pathStorage + "/" + getRealFileName(info.fileName, info.ip);


        try (RandomAccessFile out = new RandomAccessFile(realFileName, "rw")) {
            byte[] data = new byte[info.chunk.remaining()];
            info.chunk.get(data, 0, data.length);
            
            out.seek(recordSize * (info.stt));   
            out.write(data);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UploadFileServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UploadFileServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return resultFlag;
    }
    private String getRealFileName(String fileName, String ip) {
        // SachCuaDung_192_78_90_09.pdf
        StringBuilder builder = new StringBuilder();
        builder.append(fileName.substring(fileName.lastIndexOf('/') + 1, fileName.lastIndexOf('.')));
        builder.append("_");
        builder.append(ip.replace('.', '_'));
        builder.append(fileName.substring(fileName.lastIndexOf('.')));

        return builder.toString();
    }
}
