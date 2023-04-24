package com.ruyue.domain;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;


@Data
@ExcelIgnoreUnannotated
public class ExcelData {

    @ExcelProperty("URL")

    String excelDurl;

    @ExcelProperty("API")
    String excelDapi;

    @ExcelProperty("敏感信息")
    String excelDSensitiveInfo;

    @Override
    public String toString() {
        return "\033[32;4m"+"ExcelData{" + "excelDurl='" + excelDurl + '\'' + ", excelDapi='" + excelDapi + '\'' + ", " +
                "excelDSensitiveInfo='" + excelDSensitiveInfo + '\'' + '}' +"\033";
    }
}
