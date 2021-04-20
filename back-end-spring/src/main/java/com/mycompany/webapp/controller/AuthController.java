package com.mycompany.webapp.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.webapp.security.JwtUtil;



@RestController
@RequestMapping("/auth")
public class AuthController {
	private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
	//로그인경우 포스트방식으로
	@Autowired
	private AuthenticationManager authenticationManager; 
	
	@PostMapping("/login") // json이 {"uid":"user1", "upassword":"12345"} 이런식으로 body에 넘어왔을때
	public Map<String,String> login(@RequestBody Map<String, String> user) { // 이런 식으로 받으려면 맵으로 받아도 됩니다. 유저 객체로 받아도 okok
		String uid = user.get("uid");
		String upassword = user.get("upassword");
		logger.info(uid);
		logger.info(upassword);
		
		//사용자 인증
		UsernamePasswordAuthenticationToken upat = new UsernamePasswordAuthenticationToken(uid, upassword);
		//spring security에 인증 객체 등록
		Authentication authentication= authenticationManager.authenticate(upat); //인증이 성공하면 authentication라는 객체를 리턴합니다.
		SecurityContextHolder.getContext().setAuthentication(authentication);
		//jwt 생성
		String jwt = JwtUtil.createToken(uid);
		//JSON 응답 보내기 
		Map<String,String> map = new HashMap<>();
		map.put("uid", uid);
		map.put("authToken", jwt);
		return map;
		
		//{ 이렇게 넘어오는 것을 알 수 있다.
	    //"uid": "user1",
	    //"authToken": "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE2MTg5MjYzMTMsInVpZCI6InVzZXIxIn0.jxlgTbYPlv3WUyaHEOwLcQ5whPnZ51VnMWxw_5nzYBg"
	    //}
	}
}
