package com.emc.main;

/**
 * Created by wangj121 on 2017/5/18.
 */


import java.util.ArrayList;
import java.util.LinkedList;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Diff;
import name.fraser.neil.plaintext.diff_match_patch.Operation;
import sun.awt.image.ImageWatched;


public class StringDiff {
    //比较两个文本的差异，并将差异返回(使用google code：google-diff-match-path.jar)
    //jar：https://github.com/GerHobbelt/google-diff-match-patch
    ArrayList<String> computeDiff(String a,String b){
        diff_match_patch difference = new diff_match_patch();    //创建diff_match_patch 的object
        difference.Diff_Timeout= 0 ;                             //不设置时限，直至完成
        LinkedList<Diff> deltas = difference.diff_main(a,b);
        String text1 = "",text2="";                              //text1表示只有a拥有的文本内容，text2表示只有b拥有的文本内容
        for(Diff d:deltas){
            if(d.operation==Operation.DELETE)                    //delete表示a需要删除什么来达到b,即只有a拥有的内容
                text1 = text1 + d.text + '\n';
            else if(d.operation == Operation.INSERT)             //insert表示a需要添加什么来达到b,即只有b拥有的内容
                text2 = text2 + d.text + '\n';
            else{                                                //相等的部分,不记录
            }
        }
        ArrayList<String> result = new ArrayList<>();            //存储是否相等，若不相等，各自拥有的内容
        int count1=0,count2=0;                                   //表示text1,text2中换行符的个数
        for(int k=0;k<text1.length();k++){
            if(text1.charAt(k)=='\n')
                count1++;
        }
        for(int q=0;q<text2.length();q++){
            if(text2.charAt(q)=='\n')
                count2++;
        }                                                         //如果两者相等或者两者都只包含换行符
        if((count1==text1.length()&&count2==text2.length())||(text1==""&&text2=="")){                                //如果相等，输出equal
            result.add("equal");
            return result;
        } else{                                                  //如果不相等，输出not equal和对应信息
            result.add("not equal");
            result.add(text1);
            result.add(text2);
            return result;
        }
    }
}

