package com.boosteel.util.backup;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.junit.Test;

public class FTPManager {

    private FTPClient ftp;
    private String ip = "localhost";
    private int port = 21;
    private String user;
    private String password;
    private String root;

    @Test
    public void set() throws Exception {
        ip = "115.23.187.44";
        port = 61954;
    }

    public FTPManager(String ip, int port, String user, String password) {
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    public FTPManager setRoot(String root) {
        this.root = root;
        return this;
    }

    public FTPManager connect() throws Exception {
        ftp = new FTPClient();
        ftp.setControlEncoding("utf-8");
        //ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        ftp.setDefaultPort(port);
        ftp.connect(ip);//호스트 연결

        if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
            ftp.disconnect();
            System.out.println("FTP server refused connection.");
        } else {
            System.out.println("\n--------------------------------------------------------\n" +
                    "------------------ Connect successful ------------------" +
                    "\n--------------------------------------------------------\n\n");
        }
        ftp.login(user, password);//로그인
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        ftp.enterLocalPassiveMode();

        return this;
    }

    public FTPManager disconnect() throws Exception {
        ftp.logout();
        ftp.disconnect();
        return this;
    }

    public FTPClient getClient() throws Exception {
        if (ftp == null || !ftp.isConnected())
            connect();
        else
            ftp.changeWorkingDirectory("/");


        if (root != null) ftp.changeWorkingDirectory(root);
        return ftp;
    }

    public FTPManager run(Handler handler) throws Exception {
        handler.accept(getClient());
        return this;
    }

    public static final FTPClient connet(String ip, int port, String user, String pass) throws Exception {
        FTPClient ftp = new FTPClient();
        ftp.setControlEncoding("utf-8");
        //ftp.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        ftp.setDefaultPort(port);
        ftp.connect(ip);//호스트 연결

        if (!FTPReply.isPositiveCompletion(ftp.getReplyCode())) {
            ftp.disconnect();
            System.out.println("FTP server refused connection.");
        } else {
            System.out.println("\n--------------------------------------------------------\n" +
                    "------------------ Connect successful ------------------" +
                    "\n--------------------------------------------------------\n\n");
        }
        ftp.login(user, pass);//로그인
        ftp.setFileType(FTP.BINARY_FILE_TYPE);
        ftp.enterLocalPassiveMode();

        return ftp;
    }

    public interface Handler {
        void accept(FTPClient ftp) throws Exception;
    }

    private void out(Object obj) {
        System.out.println(obj);
    }
}
