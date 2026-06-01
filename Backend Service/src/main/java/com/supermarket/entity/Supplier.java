package com.supermarket.entity;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDataTime;

@Data
public class Supplier {
    private Integer id;

    @NotBlank(message = "供应商名称不能为空")
    @Size(max = 100, message = "名称长度不能超过100")
    private String name;
    
    @Size(max = 50, message = "简称长度不能超过50")
    private String shortName;
    
    @Size(max = 200, message = "地址长度不能超过200")
    private String address;
    
    @NotBlank(message = "电话不能为空")
    @Pattern(regexp = "^[0-9\\-]{7,20}$", message = "电话格式不正确")
    private String phone;
    
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", 
             message = "邮箱格式不正确")
    private String email;
    
    @Size(max = 50, message = "联系人长度不能超过50")
    private String contactPerson;
    
    @Pattern(regexp = "^[0-9\\-]{7,20}$", message = "联系人电话格式不正确")
    private String contactPhone;
    
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
    
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

