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
    private final File TMP_DICRECTORY = new File("tmps");
    private final static int BUFFER_SIZE = 1024 * 10;

    public UploadFileServer() {
        if (!TMP_DICRECTORY.exists()) {
            TMP_DICRECTORY.mkdir();
        }
        if (!BASE_DIRECTORY.exists()) {
            BASE_DIRECTORY.mkdir();
        }
    }

    @Override
    public boolean uploadFile(FileInfo info) throws TException {
        boolean resultFlag = false;

        createCurTmpDir(info);

        System.out.println("File: " + getRealFileName(info.fileName, info.ip) + " ----- " + info.stt + " ---- is uploading");

        try (BufferedOutputStream out = new BufferedOutputStream(
                new FileOutputStream(
                        getCurTmpDir(info.fileName, info.ip) + "/Chunk" + info.stt + "_" + getRealFileName(info.fileName, info.ip)))) {

                    byte[] data = new byte[info.chunk.remaining()];
                    info.chunk.get(data, 0, data.length);

                    out.write(data);
                    resultFlag = true;
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(UploadFileServer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(UploadFileServer.class.getName()).log(Level.SEVERE, null, ex);
                }
                return resultFlag;
    }

    @Override
    public boolean uploadSuccess(boolean bln, String fileName, String ip) throws TException {
        boolean resultFlag = false;

        String pathStorage = BASE_DIRECTORY.getAbsolutePath();
        String realFileName = pathStorage + "/" + getRealFileName(fileName, ip);

        int length = new File(getCurTmpDir(fileName, ip)).listFiles().length;

        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(realFileName))) {
            for (int i = 0; i < length; i++) {
                String currentChunk = getCurTmpDir(fileName, ip) + "/Chunk" + i + "_" + getRealFileName(fileName, ip);
                System.out.println(currentChunk);

                joinFile(currentChunk, out);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UploadFileServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UploadFileServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Remove folder current tmp
        removeFolder(getCurTmpDir(fileName, ip));
        
        return resultFlag;
    }
    
    private void removeFolder(String pathCurTmp) {
       File currentTmpDir = new File(pathCurTmp);
       
       if (currentTmpDir.exists()) {
           File[] children = currentTmpDir.listFiles();
           
           for (File file : children) {
               file.delete();
           }
           currentTmpDir.delete();
       }
    }

    private byte[] joinFile(String fileName, BufferedOutputStream out) {
        byte[] data = new byte[BUFFER_SIZE];
        int byteRead = 0;
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(fileName))) {
            while ((byteRead = in.read(data)) != -1) {
                if (byteRead != BUFFER_SIZE) {
                    byte[] dataFinal = Arrays.copyOf(data, byteRead);
                    out.write(dataFinal);
                } else {
                    out.write(data);
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(UploadFileServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(UploadFileServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void createCurTmpDir(FileInfo info) {
        String folderName = getCurTmpDir(info.fileName, info.ip);
        File curTmpDir = new File(folderName);
        if (!curTmpDir.exists()) {
            curTmpDir.mkdirs();
        }
    }

    private String getCurTmpDir(String fileName, String ip) {
        String fileInServer = getRealFileName(fileName, ip);
        return TMP_DICRECTORY.getAbsolutePath() + "/" + fileInServer.substring(0, fileInServer.lastIndexOf('.'));
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
