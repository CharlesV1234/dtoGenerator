package com.ttj.dtogen.dto;

import java.util.List;
import java.lang.Double;
import java.util.Map;
import java.lang.String;

public class DepartmentEntityDto{

	private String deptName;
	private Double specialNumber;
	private Map<String,List<String>> empList;

	public String getDeptName(){
		return deptName;
	}
	public void setDeptName(String deptName){
		this.deptName = deptName;
	}
	public void setSpecialNumber(Double specialNumber){
		this.specialNumber = specialNumber;
	}
	public Map<String,List<String>> getEmpList(){
		return empList;
	}
	public void setEmpList(Map<String,List<String>> empList){
		this.empList = empList;
	}

}