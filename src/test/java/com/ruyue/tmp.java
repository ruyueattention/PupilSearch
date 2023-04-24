package com.ruyue;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.ruyue.domain.ExcelData;
import com.ruyue.domain.TrackURL;
import com.ruyue.domain.TreeNodeData;
import com.ruyue.util.TreeNode;
import com.ruyue.util.UniqueLinkedBlockingQueue;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class tmp
{
    public TrackURL trackURL = new TrackURL("https://www.leve.com");

    public tmp() throws MalformedURLException {
    }

    public boolean comp(TrackURL trackURL){

        return  false;
    }


    public static void main(String[] args) throws MalformedURLException, InterruptedException {


        TrackURL aa = new TrackURL("https://jp.leve.com");
        String pattern = "[^.]*\\.[a-z]*$";
        Pattern r = Pattern.compile(pattern);
        System.out.println(aa.getUrl().getHost());
        Matcher m = r.matcher(aa.getUrl().getHost());
        while (m.find()) {
                System.out.println(m.group());
        }

    }



}
