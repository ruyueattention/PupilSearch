package com.ruyue.module;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.builder.ExcelWriterBuilder;
import com.alibaba.excel.write.builder.ExcelWriterSheetBuilder;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.ruyue.domain.ExcelData;
import com.ruyue.domain.TreeNodeData;
import com.ruyue.util.TreeNode;
import com.ruyue.util.UniqueLinkedBlockingQueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class ReportTask {
    public boolean resultIsNull(TreeNodeData treeNodeData){
        int sensitiveInfoDictSize = 0;
        if(null != treeNodeData.sensitiveInfoDict){
            sensitiveInfoDictSize = treeNodeData.sensitiveInfoDict.size();
        }
        if(treeNodeData.urlQuee.size()+treeNodeData.apiQuee.size()+sensitiveInfoDictSize>0){
            return false;
        }
        return true;
    }
    //从敏感字典中把空的数据删掉
    public void delNullfromSensitive(TreeNodeData treeNodeData){

        if(null != treeNodeData.sensitiveInfoDict){
            treeNodeData.sensitiveInfoDict.entrySet().removeIf(entry -> entry.getValue().size()==0);
        }
    }

    //别问为什么分两个excel，tmd，搞不明白easyExcel什么原理，尝试换sheet写，就会覆盖写。
    public void writeExcel(TreeNode<TreeNodeData> rootNode,String fileRoad) throws InterruptedException {

        ExcelWriterBuilder writeWorkBook = EasyExcel.write("详细结果_"+fileRoad, TreeNodeData.class);

        // 2.获取工作表对象，默认是第一个工作表
        ExcelWriterSheetBuilder sheet = writeWorkBook.sheet(0,"详细结果");
        ArrayList<TreeNodeData> resultList = new ArrayList<TreeNodeData>();
        for (TreeNode<TreeNodeData> node : rootNode.elementsIndex) {
            //有内容才输出，没内容输出个J8
            if(!resultIsNull(node.nodeData)){
                delNullfromSensitive(node.nodeData);
                node.nodeData.setExcelData();
                resultList.add(node.nodeData);
            }
        }

        // 4.将数据写入工作表
        sheet.doWrite(resultList);
        UniqueLinkedBlockingQueue tmp1 = new UniqueLinkedBlockingQueue();
        UniqueLinkedBlockingQueue tmp2 = new UniqueLinkedBlockingQueue();
        UniqueLinkedBlockingQueue tmp3 = new UniqueLinkedBlockingQueue();
        writeWorkBook =  EasyExcel.write("聚合结果_"+fileRoad, ExcelData.class);
        // 2.获取工作表对象
        ExcelWriterSheetBuilder sheet2 = writeWorkBook.sheet(1,"聚合结果");
        for (TreeNodeData treeNodeData: resultList){
            tmp1.addAll(treeNodeData.apiQuee);
            tmp2.addAll(treeNodeData.urlQuee);
            if(null!=treeNodeData.sensitiveInfoDict){
                Iterator<HashMap.Entry<String,UniqueLinkedBlockingQueue>> iterator =
                        treeNodeData.sensitiveInfoDict.entrySet().iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String,UniqueLinkedBlockingQueue> entry = iterator.next();
                    tmp3.addAll(entry.getValue());
                }
            }
        }
        ArrayList<ExcelData> resultList2 = new ArrayList<ExcelData>();
        int max = ((max=(tmp1.size()> tmp2.size())?tmp1.size():tmp2.size())>tmp3.size()?max:tmp3.size());
        for(int i=0;i<max;i++){
            ExcelData tmp = new ExcelData();
            tmp.setExcelDSensitiveInfo(tmp3.isEmpty()? "" :(String) tmp3.take());
            tmp.setExcelDurl(tmp2.isEmpty()? "":(String) tmp2.take() );
            tmp.setExcelDapi(tmp1.isEmpty()? "":(String) tmp1.take());
            resultList2.add(tmp);
            System.out.println(tmp);
        }
        sheet2.doWrite(resultList2);








    }



}