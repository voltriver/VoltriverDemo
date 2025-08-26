package com.voltriver.demo.controller;

import java.sql.Timestamp;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.voltriver.demo.dto.MemberDTO;

@RestController
public class ApiController {

	
	@GetMapping("/test")
	public MemberDTO test() {
		MemberDTO member = new MemberDTO();
		member.setUserId("hgdong");
		member.setUserName("Hong Gil-Dong");
		member.setPassWord("welcme");
		member.setEmail("teest@naver.com");
		member.setLastLogin(new Timestamp(System.currentTimeMillis()));
		member.setCreateOn(new Timestamp(System.currentTimeMillis()));
		
		return member;		
	}
	
	@GetMapping("/test2")
	public String test2() {
		
		String result = "test2 response!";
		
		return result;		
	}
}
