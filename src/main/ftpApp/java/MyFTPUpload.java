import java.io.IOException;
import java.util.Scanner;


import org.apache.commons.net.ftp.FTPClient;

public class MyFTPUpload {
    public static void main(String[] args) throws IOException {
        String server = args[1];
        int port = 21;
        String user = args[2];
        Scanner scanner=new Scanner(System.in);
        System.out.println("Please give the password:");
        String pass = scanner.next();
        FTPClient ftpClient = new FTPClient();
        ftpClient.connect(server, port);
        ftpClient.login(user, pass);
        ftpClient.enterLocalPassiveMode();
        String remoteDirPath = "/home/ftpadmin";
        String localDirPath = args[0];
        FTPUtil.uploadDirectory(ftpClient, remoteDirPath, localDirPath, "");
        ftpClient.logout();
        ftpClient.disconnect();
    }
}