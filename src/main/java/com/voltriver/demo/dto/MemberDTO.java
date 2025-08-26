package com.voltriver.demo.dto;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class MemberDTO {
	
	private String userId;
	private String userName;
	private String passWord;
	private String email;
	private Timestamp createOn;
	private Timestamp lastLogin;

}
