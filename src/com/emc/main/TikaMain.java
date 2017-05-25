package com.emc.main;


import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by wangj121 on 2017/5/16.
 */
public class TikaMain {

    //返回filePath所有的jar版本号（想要比较的Tika版本）
    private static ArrayList<String> readTextFile(String filePath){
        ArrayList<String> list =  new ArrayList<String>();
        try {
            String encoding = "GBK";
            File file = new File(filePath);
            if (file.isFile() && file.exists()) {
                InputStreamReader read = new InputStreamReader(new FileInputStream(file), encoding);
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    list.add(lineTxt);
                    System.out.println(lineTxt);
                }
                bufferedReader.close();
                read.close();
            } else {
                System.out.println("找不到指定的文件");
            }
        }catch(Exception e){
            System.out.println("读取文件内容出错！");
            e.printStackTrace();
        }
        return list;
    }


    public static void main(String[] args) throws IOException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {


        XMLWriter writer = null;                                              // 声明写XML的对象
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("GBK");                                            // 设置XML文件的编码格式
        String filePath = "C:\\Users\\wangj121\\Desktop\\result.xml";         // 设置结果写入到的文件URL
        File _file = new File(filePath);
        Document document = null;
        document = DocumentHelper.createDocument();
        ArrayList<File> files;                                                //存储文件的List
        ArrayList<String> tikaVersions = readTextFile("C:\\Users\\wangj121\\Desktop\\tikaVersions.txt");  //获取所有的tika版本号
        FileChoose chooser = new FileChoose();
        System.out.println("please input the URL: ");                        //输入想要抽取信息的文件夹绝对路径
        Scanner scanner = new Scanner(System.in);
        String fileUrl = scanner.nextLine();
        files =chooser.getFilesURL(fileUrl);                                 //获取文件夹下所有的文件，存储在files列表中

        ArrayList<ArrayList<FileInformation>> informations = new ArrayList<>();    //存储不同版本不同文件的信息

        for(int i=0;i<tikaVersions.size();i++){                                    //遍历每一个版本
            String tikaVersion = tikaVersions.get(i);
            TikaInformations infs = new TikaInformations(tikaVersion);
            ArrayList<FileInformation> temp = new ArrayList<>();
            for(int j=0 ; j<files.size();j++){                                     //遍历每一个文件
                File file = files.get(j);
                FileInformation  fileinf = new FileInformation();
                fileinf.setType(infs.getType(file));                               //获取该文件在当前版本下的信息
                fileinf.setContent(infs.getContent(file));
                fileinf.setMetadata(infs.getMetadata(file));
                temp.add(fileinf);
            }
            informations.add(temp);
        }

        Element root = document.addElement("Baseline Version: "+tikaVersions.get(0));    //设置结果XML的根节点

        //默认tikaVersions.txt中第一个Tika版本为baseline
        for(int i=1;i<tikaVersions.size();i++) {
            boolean isVersionSame=true;                                          //判断当前版本是否和baseline提取的内容完全一样
            int errorNumber=0;                                                   //表示与baseline相比不同的file数量
            Element current = root.addElement("Tika");                         //添加子节点（当前的Tika版本）
            current.addAttribute("Version",tikaVersions.get(i));
            for(int j=0;j<files.size();j++) {
                Element curFile = current.addElement("File");                  //添加当前的文件名
                curFile.addAttribute("Name",files.get(j).getName());
                StringDiff diff = new StringDiff();
                                                                                 //获取该文件在baseline version 的信息
                String baseType = informations.get(0).get(j).getType();
                String baseContent = informations.get(0).get(j).getContent();
                String baseMetadata = informations.get(0).get(j).getMetadata();
                                                                                 //获取该文件在current version 的信息
                String tempType = informations.get(i).get(j).getType();
                String tempContent = informations.get(i).get(j).getContent();
                String tempMetadata = informations.get(i).get(j).getMetadata();
                boolean isFileSame = true;
                                                                                 //比较对应信息的差异结果
                ArrayList<String> typeDiff=diff.computeDiff(baseType,tempType);  //比较Type信息
                Element fileType = curFile.addElement("Type");
                fileType.addAttribute("result",typeDiff.get(0));
                if(!typeDiff.get(0).equals("equal")){                            //如果Type不相同
                    Element formerType = fileType.addElement("baseline only");
                    formerType.setText(typeDiff.get(1));                         //设置只有baseline拥有的信息
                    Element latterType = fileType.addElement("current only");
                    latterType.setText(typeDiff.get(2));                         //设置只有current拥有的信息
                    isFileSame=false;                                            //当前文件标志为false，与baseline抽取的结果不同
                    isVersionSame=false;                                         //当前版本标志位false，与baseline结果不完全一样
                }
                ArrayList<String> contentDiff = diff.computeDiff(baseContent,tempContent);
                Element fileContent = curFile.addElement("Content");           //比较content信息，同type
                fileContent.addAttribute("result",contentDiff.get(0));
                if(!contentDiff.get(0).equals("equal")){
                    Element formerContent = fileContent.addElement("baseline only");
                    formerContent.setText(contentDiff.get(1));
                    Element latterContent = fileContent.addElement("current only");
                    latterContent.setText(contentDiff.get(2));
                    isFileSame=false;
                    isVersionSame=false;
                }
                ArrayList<String> metadataDiff = diff.computeDiff(baseMetadata,tempMetadata);
                Element fileMetadata = curFile.addElement("Metadata");          //比较metadata信息，同type
                fileMetadata.addAttribute("result",metadataDiff.get(0));
                if(!metadataDiff.get(0).equals("equal")){
                    Element formerMetadata = fileMetadata.addElement("baseline only");
                    formerMetadata.setText(metadataDiff.get(1));
                    Element latterMetadata = fileMetadata.addElement("current only");
                    latterMetadata.setText(metadataDiff.get(2));
                    isFileSame=false;
                    isVersionSame=false;
                }
                if(isFileSame)                                                  //如果文件的三种信息都相同，设置same为true
                    curFile.addAttribute("Same","True");
                else {
                    curFile.addAttribute("Same", "False");
                    errorNumber++;
                }
            }
            if(isVersionSame)                                                   //如果当前version和baseline完全相同，设置当前版本same为true
                current.addAttribute("Same","True");
            else {
                current.addAttribute("Same", "False");
                current.addAttribute("ErrorNumber",Integer.toString(errorNumber));  //若不同，设置不同文件的数量
            }
        }

        writer = new XMLWriter(new FileWriter(_file),format);
        writer.write(document);
        writer.close();
    }
}
