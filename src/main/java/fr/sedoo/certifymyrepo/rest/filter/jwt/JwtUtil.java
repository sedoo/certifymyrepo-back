package fr.sedoo.certifymyrepo.rest.filter.jwt;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JwtUtil {

	private static final Logger LOG = LoggerFactory.getLogger(JwtUtil.class);

	public static final String XSRF_TOKEN = "xsrfToken";
	public static final String DEFAULT_COOKIE_NAME = "JWT-TOKEN";
	
	public static final String ROLES_KEY = "roles";
	
	
	public static final int DEFAULT_VALIDITY = 1800;//30 minutes
	
	public static final String ISSUER = "Sedoo";
	public static final String AUTH_HEADER = "Authorization";
	
	public static String generateToken(String subject, String signingKey) throws Exception {
		return generateToken(subject, signingKey, DEFAULT_VALIDITY);
	}
	
	public static String generateToken(String subject, String signingKey, int validity) throws Exception {
		return generateToken(subject, signingKey, validity, Collections.emptySet(), Collections.emptyMap());
	}
	public static String generateToken(String subject, String signingKey, int validity, Set<String> roles, Map<String, String> addPayload) throws Exception {
		return generateToken(subject, signingKey, validity, roles, addPayload, null);
	}
	public static String generateToken(String subject, String signingKey, int validity, Set<String> roles, Map<String, String> addPayload, String xsrfToken) throws Exception {
		long nowMillis = System.currentTimeMillis();
		Date iat = new Date(nowMillis);

		long expMillis = nowMillis + (validity * 1000);
		Date exp = new Date(expMillis);

		String key = Base64Utils.encode(signingKey);

		JwtBuilder builder = Jwts.builder()
				.setSubject(subject)
				.setIssuer(ISSUER)
				.setIssuedAt(iat)
				.setExpiration(exp)
				.claim(XSRF_TOKEN, xsrfToken)
				.signWith(SignatureAlgorithm.HS256, key);

		for (String name: addPayload.keySet()){
			builder.claim(name, addPayload.get(name));
		}
		
		if (roles != null){
			builder.claim(ROLES_KEY, roles);
		}
		
		return builder.compact();
	}

	public static String getSubjectFromAuthHeader(String authHeader, String signingKey) throws Exception{
		return getSubjectFromAuthHeader(authHeader, signingKey, null);
	}
	public static String getSubjectFromAuthHeader(String authHeader, String signingKey, String xsrfToken) throws Exception{
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new JwtException("Missing or invalid Authorization header");
		}
		final String token = authHeader.substring(7).trim();
		return getSubject(token, signingKey, xsrfToken);
	}
	
	private static String getSubject(String token, String signingKey, String xsrfToken) throws Exception{
		LOG.debug("getSubject");
		/*String key = Base64Utils.encode(signingKey);
		LOG.debug("token: " + token);
		if(token == null) return null;
						
		final Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();*/
		final Claims claims = getClaims(token, signingKey);
		String xsrfClaim = claims.get(xsrfToken, String.class);
		if (xsrfClaim != null){
			LOG.debug("xsrfClaim: " + xsrfClaim);
			if ( ! xsrfClaim.equals(xsrfToken) ){
				throw new JwtException("Invalid xsrf");	
			}
		}else{
			if (xsrfToken != null){
				throw new JwtException("xsrf claim is null");
			}
		}
		
		
		LOG.debug("subject: " + claims.getSubject());

		return claims.getSubject();
	}
	
	public static Claims getClaims(String token, String signingKey) throws Exception{
		LOG.debug("getClaims");
		String key = Base64Utils.encode(signingKey);
		LOG.debug("token: " + token);
		if(token == null) return null;
						
		return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody();
	}
	
	public static String getSubjectFromCookie(HttpServletRequest request, String signingKey) throws Exception{
		return getSubjectFromCookie(request, signingKey, DEFAULT_COOKIE_NAME, null);
	}
	public static String getSubjectFromCookie(HttpServletRequest request, String signingKey, String xsrfToken) throws Exception{
		return getSubjectFromCookie(request, signingKey, DEFAULT_COOKIE_NAME, xsrfToken);
	}
	public static String getSubjectFromCookie(HttpServletRequest request, String signingKey, String jwtCookieName, String xsrfToken) throws Exception{
		String token = CookieUtil.getValue(request, jwtCookieName);
		return getSubject(token, signingKey, xsrfToken);
	}
	public static String getTokenFromAuthHeader(HttpServletRequest request) throws JwtException{
		String authHeader = request.getHeader(AUTH_HEADER);
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			throw new JwtException("Missing or invalid Authorization header");
		}
		return authHeader.substring(7).trim();
	}
	public static Authentication getAuthentication(HttpServletRequest request, String signingKey) {
	    String authHeader = request.getHeader(AUTH_HEADER);
	    try{
	    	String user = getSubjectFromAuthHeader(authHeader, signingKey);
	    	return user != null ?
	  	          new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList()) :
	  	          null;
	    }catch(Exception e){
	    	LOG.error("error in getAuthentication()", e);
	    	return null;
	    }
	  }
	

}

