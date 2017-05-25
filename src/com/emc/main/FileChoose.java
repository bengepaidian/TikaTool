package com.emc.main;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by wangj121 on 2017/5/16.
 */
public class FileChoose {
    public static ArrayList<File> getFilesURL(Object path) {    //path为文件夹的绝对路径
        File directory = null;
        if(path instanceof File){
            directory = (File) path;
        }
        else{
            directory = new File(path.toString());
        }
        ArrayList<File> files=new ArrayList<>();
        if(directory.isFile()){                                 //如果是文件，则加入到list之中
            files.add(directory);
            System.out.println(directory.toString());
            return files;
        }
        else if(directory.isDirectory()){                       //如果是文件夹，则递归找到其中的文件并加入到list之中
            File[] fileArr=directory.listFiles();
            for(int i=0;i<fileArr.length;i++){
                File oneFile=fileArr[i];
                files.addAll(getFilesURL(oneFile));
            }

        }
        return files;
    }
}
