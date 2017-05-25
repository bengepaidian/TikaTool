package com.emc.main;

import com.sun.xml.internal.ws.api.addressing.WSEndpointReference;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Created by wangj121 on 2017/5/17.
 */
public class TikaInformations {
    Class Jarred;
    URLClassLoader cl;

    //在构造函数中，声明使用的Tika版本号
    public TikaInformations(String tikaVersion) throws MalformedURLException, ClassNotFoundException {
        String jarPath = new String(tikaVersion);
        URL myJarFile = null;
        myJarFile = new URL("file:///"+jarPath);                //通过Jar包的绝对路径动态加载Jar包
        cl = URLClassLoader.newInstance(new URL[]{myJarFile});
        Jarred = null;
        Jarred = cl.loadClass("org.apache.tika.Tika");         //加载Jar包中的Tika类
    }

    //获取文件的类型
    public String getType(File file) throws NoSuchMethodException, IllegalAccessException, InstantiationException, InvocationTargetException {
        Method detect = null;
        detect = Jarred.getMethod("detect",new Class[]{File.class});       //获取Tika类中的detect方法
        Object JarredObj = null ;
        JarredObj = Jarred.newInstance();
        Object response = detect.invoke(JarredObj,file);                        //触发detect方法获取返回的类型信息
        return response.toString();                                             //返回提取的file的type信息
    }

    //获取文件的内容
    public String getContent(File file) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        Method parseToString = null;
        parseToString = Jarred.getMethod("parseToString",new Class[]{File.class});    //获取Tika类中的parseToString方法
        Object JarredObj = null ;
        JarredObj = Jarred.newInstance();
        Object response = parseToString.invoke(JarredObj,file);                  //触发parseToString方法获取返回内容信息
        return response.toString();                                              //返回提取的内容信息
    }


    //获取文件的元数据
    public String getMetadata(File file) throws ClassNotFoundException, IllegalAccessException, InstantiationException, FileNotFoundException, NoSuchMethodException, InvocationTargetException {

        Class Metadata;
        InputStream input = new FileInputStream(file);
        Method parseToString = null;
        Metadata = cl.loadClass("org.apache.tika.metadata.Metadata");        //加载metadata类
        parseToString = Jarred.getMethod("parseToString",new Class[]{InputStream.class,Metadata});    //获取parseToString方法
        Object JarredObj = Jarred.newInstance();
        Object metadataObj = Metadata.newInstance();
        parseToString.invoke(JarredObj,input,metadataObj);              //出发parseToString方法,metadataObj为元数据信息
     //   System.out.println(metadata.toString());
        Method names = null, get = null;                               //metaData的names方法，获取元数据的类型,get方法获取对应元数据的值
        names = Metadata.getMethod("names",new Class[]{});
        get = Metadata.getMethod("get",new Class[]{String.class});
        Object namesRes=names.invoke(metadataObj);                          //触发getMethod方法
        String[] metadataNames= (String[]) namesRes;                        //将object强制转换成string数组
        String metadataResult="";
        for(int i=0;i<metadataNames.length;i++){
            Object valueRes = get.invoke(metadataObj,metadataNames[i]);         //获取key以及其对应的value
            String temp = metadataNames[i] + " : " + (String)valueRes + '\n';
            metadataResult +=temp;
        }
        return metadataResult;
    }
}
