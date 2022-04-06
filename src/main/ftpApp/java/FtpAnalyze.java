import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import java.util.*;
import java.util.regex.*;

public class FtpAnalyze {

    private static String readFiles(FTPClient ftpClient,String file) throws IOException {
        file=file.replaceAll("/home/ftpadmin/","");
        InputStream inputStream = ftpClient.retrieveFileStream(file);
        StringWriter writer = new StringWriter();
        Charset encoding = StandardCharsets.US_ASCII;
        IOUtils.copy(inputStream, writer, encoding);
        String contentString = writer.toString();
        ftpClient.completePendingCommand();
        return contentString;
    }

    static void listDirectory(FTPClient ftpClient, String parentDir, String currentDir, int level,boolean url) throws IOException {
        String dirToList = parentDir;
        if (!currentDir.equals("")) {
            dirToList += "/" + currentDir;
        }
        FTPFile[] subFiles = ftpClient.listFiles(dirToList);
        if (subFiles != null && subFiles.length > 0) {
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".")
                        || currentFileName.equals("..")) {

                    continue;
                }


                if (aFile.isDirectory()) {
                    //System.out.println("[" + currentFileName + "]");
                    listDirectory(ftpClient, dirToList, currentFileName, level + 1,url);
                } else {
                    String file=(dirToList+"/"+ currentFileName);
                    String x=readFiles(ftpClient,file);
                    String []temp=x.split("\n");
                    for (String line:temp){
                        extractURL(line);
                    }
                }
            }
        }
    }


    static void listDirectory(FTPClient ftpClient, String parentDir, String currentDir, int level) throws IOException {
        String dirToList = parentDir;
        if (!currentDir.equals("")) {
            dirToList += "/" + currentDir;
        }
        FTPFile[] subFiles = ftpClient.listFiles(dirToList);
        if (subFiles != null && subFiles.length > 0) {
            for (FTPFile aFile : subFiles) {
                String currentFileName = aFile.getName();
                if (currentFileName.equals(".")
                        || currentFileName.equals("..")) {

                    continue;
                }
                for (int i = 0; i < level; i++) {
                    System.out.print("\t");
                }
                if (aFile.isDirectory()) {
                    System.out.println("[" + currentFileName + "]");
                    listDirectory(ftpClient, dirToList, currentFileName, level + 1);
                } else {
                    System.out.println(currentFileName);
                }
            }
        }
    }

    // Function to extract all the URL
    // from the string
    public static void extractURL(
            String str)
    {
        List<String> list
                = new ArrayList<>();


        String regex
                = "\\b((?:https?|ftp|file):"
                + "//[-a-zA-Z0-9+&@#/%?="
                + "~_|!:, .;]*[-a-zA-Z0-9+"
                + "&@#/%=~_|])";

        Pattern p = Pattern.compile(
                regex,
                Pattern.CASE_INSENSITIVE);

        Matcher m = p.matcher(str);

        while (m.find()) {

            list.add(str.substring(
                    m.start(0), m.end(0)));
        }

        if (list.size() == 0) {
            return;
        }

        for (String url : list) {
            System.out.println(url);
        }
    }


    public static void main(String[] args) {
        String server = "localhost";
        int port = 21;
        String user = "ftpadmin";
        String pass = "csdeptucy";
        String option=args[0];
        FTPClient ftpClient = new FTPClient();

        try {
            // connect and login to the server
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);

            // use local passive mode to pass firewall
            ftpClient.enterLocalPassiveMode();
            if(option.equals("show-dir")){
                for (FTPFile file:ftpClient.listFiles())
                    System.out.println(file);
            }

            if(option.equals("show-file")){
                String file=args[1];
                String x= readFiles(ftpClient,file);
                System.out.println(x);
            }
            if(option.equals("find-string")){
                String stringToFind=args[2];
                String file=args[1];
                String content = readFiles(ftpClient,file);
                String []temp=content.split("\n");
                for (String x:temp){
                    if (x.contains(stringToFind))
                        System.out.println(x);
                }
            }
            if(option.equals("show-dir-R")){


                String dirToList = args[1];

                if(dirToList.equals(""))
                    dirToList="/home/ftpadmin";
                listDirectory(ftpClient, dirToList, "", 0);
            }
            if(option.equals("show-urls")){

                String dirToList="/home/ftpadmin";
                listDirectory(ftpClient, dirToList, "", 0,true);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

}
