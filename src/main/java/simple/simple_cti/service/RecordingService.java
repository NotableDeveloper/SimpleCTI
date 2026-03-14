package simple.simple_cti.service;

import com.jcraft.jsch.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.Properties;

@Slf4j
@Service
public class RecordingService {

    @Value("${sftp.host}")
    private String host;

    @Value("${sftp.port:22}")
    private int port;

    @Value("${sftp.username}")
    private String username;

    @Value("${sftp.password}")
    private String password;
    
    @Value("${recording.base-path}")
    private String basePath;

    public void downloadFile(String filename, OutputStream outputStream) throws Exception {
        Session session = null;
        ChannelSftp sftpChannel = null;
        
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, port);
            session.setPassword(password);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();

            Channel channel = session.openChannel("sftp");
            channel.connect();
            sftpChannel = (ChannelSftp) channel;

            String path = basePath;
            if (path != null && !path.endsWith("/")) {
                path += "/";
            } else if (path == null) {
                path = "";
            }
            
            String fullPath = path + filename;
            log.info("Downloading recording file: {}", filename);
            log.debug("Resolved SFTP path: {}", fullPath);
            sftpChannel.get(fullPath, outputStream);

        } catch (Exception e) {
            log.error("SFTP download failed for file {}", filename, e);
            throw e;
        } finally {
            closeChannel(sftpChannel, session);
        }
    }

    public java.util.List<String> listRecordings() throws Exception {
        Session session = null;
        ChannelSftp sftpChannel = null;
        java.util.List<String> fileList = new java.util.ArrayList<>();

        try {
            session = createSession();
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            String path = basePath;
            if (path != null && !path.endsWith("/")) {
                path += "/";
            } else if (path == null) {
                path = "";
            }

            log.debug("Listing recordings from SFTP path: {}", path);
            java.util.Vector<ChannelSftp.LsEntry> entries = sftpChannel.ls(path + "*.wav");
            for (ChannelSftp.LsEntry entry : entries) {
                if (!entry.getAttrs().isDir()) {
                    fileList.add(entry.getFilename());
                }
            }
            log.info("Listed {} recording(s) from {}", fileList.size(), path);
        } catch (Exception e) {
            log.error("SFTP list failed for path {}", basePath, e);
            // Don't throw, just return empty list or handle gracefully? 
            // Better to throw so controller knows.
            throw e;
        } finally {
            closeChannel(sftpChannel, session);
        }
        return fileList;
    }

    private Session createSession() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(username, host, port);
        session.setPassword(password);
        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.connect();
        return session;
    }

    private void closeChannel(ChannelSftp channel, Session session) {
        if (channel != null) {
            channel.disconnect();
        }
        if (session != null) {
            session.disconnect();
        }
    }
}
