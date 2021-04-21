package com.mycompany.webapp.security;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtUtil {
   private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
   //비밀키(노출되면 안됨)
   private static final String secretKey = "secret";
   
   //JWT 생성
   public static String createToken(String uid) {
      String token = null;
      try {
         token = Jwts.builder()
                     .setHeaderParam("typ", "JWT") //토큰의 종류
                     .setHeaderParam("alg", "HS256") //암호화 알고리즘 종류
                     .setExpiration(new Date(new Date().getTime() + (1000*60*60*12))) //토큰의 유효기간
                     .claim("uid", uid) //토큰에 저장되는 데이터
                     .signWith(SignatureAlgorithm.HS256, secretKey.getBytes("UTF-8")) //비밀키
                     .compact(); //모든 내용을 묶기
      } catch(Exception e) {
         e.printStackTrace();
      }
      return token;
   }
   
   //JWT를 파싱해서 uid 얻기
   public static String getUid(String token) {
      String uid = null;
      try {
         JwtParser parser = Jwts.parser();
         parser.setSigningKey(secretKey.getBytes("UTF-8"));
         Jws<Claims> jws = parser.parseClaimsJws(token);
         Claims claims = jws.getBody();
         uid = claims.get("uid", String.class);
      } catch(Exception e) {
         e.printStackTrace();
      }
      return uid;
   }
   //JWT 유효성 검사: 유효기간 확인 29분에 토큰 보냈어. 1분 남았어. 그럼 어떻게 해줘? 다시 토큰 발행해줘야지. 
   //그건 나중에 플젝할때 적용해보시길
   public static boolean validateToken(String token) {
	   boolean validate = false;
	   try {
	         JwtParser parser = Jwts.parser();
	         parser.setSigningKey(secretKey.getBytes("UTF-8"));
	         Jws<Claims> jws = parser.parseClaimsJws(token);
	         Claims claims = jws.getBody();
	         //validate = !claims.getExpiration().before(new Date()); 아래가 같은 뜻.
	         validate = claims.getExpiration().after(new Date()); // 만료가 아직 안되었다. 만료시간이 현재시간 1분 후이다.
//	         if(validate) {
//	        	 long remainTime = claims.getExpiration().getTime() - new Date().getTime();
//	        	 logger.info("" + remainTime/1000 + "초 남았습니다.");
//	         } 나중에 활용해보기
	   } catch(Exception e) {
	         e.printStackTrace();
	      }
	   return validate;
   }
   
   
   //테스트 - 사용안할때는 주석처리하기. main으로 되어있으므로
//   public static void main(String[] args) {
//      //토큰 생성
//	  String jwt = createToken("user1");
//	  System.out.println(jwt);
//      logger.info(jwt);
//      
//      // 5초 딜레이
//      try { Thread.sleep(5000); } catch(Exception e) {};
//      
//      //토크 정보 얻기
//      if(validateToken(jwt)) {
//    	  String uid = getUid(jwt);
//    	  logger.info(uid);
//      }
//   }
}
