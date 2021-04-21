package com.mycompany.webapp.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.GenericFilterBean;

public class JwtAuthenticationFilter extends GenericFilterBean { // 요 앞이 추상 클래스; 그 말은 추상 메소드가 있다는 말, 에러가 나니깐 재정의를 해야
	private UserDetailsService userDetailsService; // 사용자의 상세 정보를 가지고 오는 서비스 
	
	public JwtAuthenticationFilter(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		//JWT 토근이 요청 헤더로 전송된 경우 //  
		HttpServletRequest httpRequest = (HttpServletRequest) request; // ServletRequest는 메서드가 적으므로 HttpServletRequest로 바꿔줘야
		String jwt = httpRequest.getHeader("authToken"); // 이건 우리가 정해야한다. authToken으로 정하도록
		if(jwt == null) {
			// JWT가 요청 파라미터로 전달된 경우 ex) <img src="download?bno=3"/> + authToken="xxxx"를 넘겨줘야
			jwt = request.getParameter("authToken");
		} 
		if(jwt != null) { // 넘어오긴 넘어옴
			if(JwtUtil.validateToken(jwt)) { // 넘어와도 만료기간이 넘으면 안됨
				//uid를 뽑아내는 작업을 해야
				//JWT에서 uid 얻기
				String uid = JwtUtil.getUid(jwt); // 유효성 패스할 경우 uid 얻어와서 
				
				// DB에서 uid에 해당하는 정보를 가져오기(이름, 권한들)
				UserDetails userDetails = userDetailsService.loadUserByUsername(uid);
				//최종 인증 객체를 만들어야
				//인증 성공 객체, 인증이 성공되면 authentication 객체가 만들어짐, 인증 객체를 만들고나서 
				Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
				// 스프링 시큐리티에 등록을 하는겁니다..이 사람이 로그인됐다. 
				SecurityContextHolder.getContext().setAuthentication(authentication); // 자동적으로 인정이 됩니다... 스프링시큐리티가 인증객체를 자동적으로 관리한다는 말.
				//jwt 안에는 uid가 들어있지? jwt를 조사해보니깐 유효해.. 그게 무슨말이냐면 
				// 복호화해보니깐 날짜가 남아있고 그 안에 uid가 있어.
				// 그러면 스프링시큐리티는 그 자체로 인정을 해줘야한다는거야. 그걸 받게되면 당연히
				// 자동로그인 해줘야죠.  
				
			}
		}
		chain.doFilter(httpRequest, response);
	} 
	
}





