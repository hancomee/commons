package com.boosteel;

import com.boosteel.util.backup.FTPManager;
import com.boosteel.util.backup.FileClone;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FTPTest {

    @Test
    public void run() throws Exception {
        FTPManager client =
                new FTPManager("115.23.187.44", 61954, "nas1", "ko9984")
                        .setRoot("$bin/backup/sns/twitter");
        Path root = Paths.get("D:\\files\\gallery\\sns/twitter");

        List<FTPFile> list = FileClone.deleteList(root, client);
        for(FTPFile file : list)
            if(file.isDirectory())
                out(file.getName());
        out(list.size());
    }

    private void out(Object obj) {
        System.out.println(obj);
    }
}
