package com.mycompany.webapp.security;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration // 설정과 관련된 객체 그러기 위해선 반드시 상속을 받아야 / 일부만 재정의하고 싶음녀 Adapter만 상속하고 일부분만 재정의할 수 있도록 그것을 어댑터 클래스라함
@EnableWebSecurity 
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	private static final Logger logger = LoggerFactory.getLogger(WebSecurityConfig.class);
	
	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private UserDetailsService userDetailsService;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		//폼 인증을 비활성화 - 시큐리티.xml 상단에 있는 부분
		http.httpBasic().disable(); // 이것은 비활성화
		//서버 세션 비활성화
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS); // 세션을 이용해서 상태정보를 저장하지 않겠다..
		//csrf : post 방식으로 전송할 때 key를 폼태그에 실을 때; 
		//사이트간 요청 위조 방지를 위해 비활성화 - 어차피 form tag를 쓰는 것도 아니고 다양한 방식을 쓸것이므로
		http.csrf().disable();
		//CORS(교차 출처 리소스 공유) 설정(다른 도메인에서 요청을 허가) a 서버에서 받은 html에서 b에 데이터를 요청할 경우 허가를 받아야.. 기본적으론 요청을 못함
		http.cors(); //cors 활성화 + 설정도 하셔야합니다.
		//JWT 인증 필터 추가**
		//2번째 필터 이전에 1번째 먼저 처리하고, 2번째 필터는 인증절차를 생략하고 
		http.addFilterBefore(new JwtAuthenticationFilter(userDetailsService), UsernamePasswordAuthenticationFilter.class);
		//마지막으로 뭘 해야하나면: 요청 경로 권한 설정
		http.authorizeRequests()
			// 권한 계층 정보 설정
			.expressionHandler(securityExpressionHandler()) 
			// 요청 경로 권한 설정
			//.antMatchers(HttpMethod.POST, "/boards").hasAuthority("ROLE_USER")
			
			.antMatchers(HttpMethod.POST, "/boards").hasAnyRole("USER")
			.antMatchers(HttpMethod.PUT, "/boards").hasAnyRole("USER")
			.antMatchers(HttpMethod.DELETE, "/boards/*").hasAnyRole("USER")
	
			.antMatchers(HttpMethod.POST, "/products").hasAnyRole("ADMIN")
			.antMatchers(HttpMethod.PUT, "/products").hasAnyRole("ADMIN")
			.antMatchers(HttpMethod.DELETE, "/products/*").hasAnyRole("ADMIN")

			//.antMatchers("/boards/**").hasAnyRole("USER") // 이거 요청하려면 USER 권한이 있어야합니다.
			
			//그 이외의 모든 경로 허가
			.antMatchers("/**").permitAll();
	}
	
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.jdbcAuthentication()
        .dataSource(dataSource)
        .usersByUsernameQuery("select userid as username, userpassword as password, userenabled as enabled from users where userid=?")
        .authoritiesByUsernameQuery("select userid as username, userauthority as authority from users where userid=?")
        .passwordEncoder(new BCryptPasswordEncoder());
	}
	
	//위에 껄 이용해서 유저디테일서비스
	//사용자의 상세 정보를 가져오는 서비스 객체를 Spring 관리 객체로 등록
	//JwtAuthenticationFilter에서 사용
	//부트에선 자동적으로 실행 리턴되는 것을 관리객체로 등록
	//위에서 사용했으
	@Bean
	@Override
	public UserDetailsService userDetailsServiceBean() throws Exception {
		return super.userDetailsServiceBean();
		//얘가 리턴간 객체를 관리객체로 만들어주면 끝. 간단합니다. 권한을 갖고와서 해당 uid의 권한을 갖고 얘가 알아서 사용자의 정보를 가지고 이용을할 수 있도록 
	}
	
	//인증된 정보를 관리하는 객체를 Spring 관리 객체로 등록
	//AuthController에서 사용
	//인증을 관리하는 자: 이제 사용할 예정. 미리 등록을 해놓음.
	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
	
	//권한 계층 객체 생성
	//method 하나 만들어요.
	public RoleHierarchyImpl roleHierarchyImpl() {
		RoleHierarchyImpl roleHierarchyImpl = new RoleHierarchyImpl(); //객체 만들기.
		roleHierarchyImpl.setHierarchy("ROLE_ADMIN > ROLE_MANAGER > ROLE_USER");
		return roleHierarchyImpl;
		
	}
	//권한 계층 객체를 이용한 웹 시큐리티 처리 객체 생성
	public SecurityExpressionHandler<FilterInvocation> securityExpressionHandler() {
		DefaultWebSecurityExpressionHandler defaultWebSecurityExpressionHandler = new DefaultWebSecurityExpressionHandler();
        defaultWebSecurityExpressionHandler.setRoleHierarchy(roleHierarchyImpl());
        return defaultWebSecurityExpressionHandler;
	}
	
	// 다른 도메인의 접근을 허용하는 메소드를 자동 실행해서 리턴된 객체를 Spring 관리 객체로 등록
	@Bean 
	//corsConfigurationSource <- 메소드 이름으로 
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		//모든 요청 사이트 허용
		configuration.addAllowedOrigin("*"); // *(모든 html)에서 받은 html만 B서버에서 접근을 허용해주겠다. 
		//모든 요청 방식 허용(GET, POST, PUT, DELETE)
		configuration.addAllowedMethod("*"); // 모든 method 다 허용
		//모든 요청 헤더 허용
		configuration.addAllowedHeader("*");
		//요청 헤더의 Authorization을 이용해서 사용자 인증(로그인 처리)하지 않음. http.httpBasic().disable();와 관련
		configuration.setAllowCredentials(false);
		//모든 URL 요청에 내용에 대해서 위 내용(configuration x 4; 상단의 코드4줄)을 적용하겠다.
		UrlBasedCorsConfigurationSource ccs = new UrlBasedCorsConfigurationSource();
		ccs.registerCorsConfiguration("/**", configuration); // 모든 요청에 대해 
		return ccs;
	}
	

}
