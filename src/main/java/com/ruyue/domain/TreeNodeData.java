package com.ruyue.domain;


import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.ruyue.util.UniqueLinkedBlockingQueue;
import lombok.Data;

import java.util.HashMap;

//节点的内容
@Data
@ExcelIgnoreUnannotated
public class TreeNodeData {
    public TrackURL trackURL;
    //SensitiveInfo
    public HashMap<String, UniqueLinkedBlockingQueue> sensitiveInfoDict;
    //api
    public UniqueLinkedBlockingQueue apiQuee;
    //URL
    public UniqueLinkedBlockingQueue urlQuee;

    public TreeNodeData(TrackURL trackURL) {
        this.trackURL = trackURL;
        this.sensitiveInfoDict = new HashMap();
        this.apiQuee = new UniqueLinkedBlockingQueue();
        this.urlQuee = new UniqueLinkedBlockingQueue();
    }


    public void setExcelData() {
        this.excelApiQuee = this.apiQuee.toString();
        this.excelUrlQuee = this.urlQuee.toString();
        this.exceltrackURL = this.trackURL.getUrl().toString();
        if(this.sensitiveInfoDict != null){
            this.excelSensitiveInfoDict = this.sensitiveInfoDict.toString();
        }else {
            this.excelSensitiveInfoDict = "";
        }
    }
    @ExcelProperty(value = "目标URL",index = 0)
    public String exceltrackURL;

    @ExcelProperty("URL")
    public String excelUrlQuee;
    @ExcelProperty("api接口")
    public String excelApiQuee;
    @ExcelProperty("敏感数据")
    public String excelSensitiveInfoDict;




}
