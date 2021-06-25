package fr.sedoo.certifymyrepo.rest.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import fr.sedoo.certifymyrepo.rest.dao.AdminDao;
import fr.sedoo.certifymyrepo.rest.filter.jwt.JwtConfig;
import fr.sedoo.certifymyrepo.rest.filter.jwt.JwtUtil;
import fr.sedoo.certifymyrepo.rest.habilitation.ApplicationUser;
import fr.sedoo.certifymyrepo.rest.habilitation.LoginUtils;
import fr.sedoo.certifymyrepo.rest.habilitation.Roles;
import fr.sedoo.certifymyrepo.rest.service.exception.UserNotLoggedException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;


@Component
public class JwtAuthenticationFilter extends GenericFilterBean {

	private static final Logger LOG = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
	
	public final static String UUID_KEY = "uuid";

	@Autowired
	AdminDao adminDao;
	
	@Autowired
	JwtConfig jwtConfig;

	public JwtAuthenticationFilter() {
	}
	
	private String generateToken(String name, String uuid) throws Exception{
		Map<String, String> infos = new HashMap<>();
		infos.put(UUID_KEY, uuid);
				
		String token = JwtUtil.generateToken(name, jwtConfig.getSigningKey(), jwtConfig.getTokenValidity(), null, infos);
		return token;
	}
	
	private void extendTokenValidity(String token, HttpServletResponse response) {
        response.addHeader(JwtUtil.AUTH_HEADER, token);
    }

	/**
	 * 
	 * @param request
	 * @return null user if no correct information is available
	 */
	public ApplicationUser getUserFromAuthHeader(HttpServletRequest request, HttpServletResponse response) {
		try{
			ApplicationUser loggedUser = null; 
			try {
				loggedUser = LoginUtils.getLoggedUser();
				if (loggedUser != null) {
					return loggedUser;
				}
			} catch (UserNotLoggedException e) {
				//We don't do anything...
			}
			String token;
			try {
				token = JwtUtil.getTokenFromAuthHeader(request);
			}
			catch (JwtException e) {
				return null;
			}
			Claims claims = JwtUtil.getClaims(token, jwtConfig.getSigningKey());
			String userUuid = claims.get(UUID_KEY, String.class);
			String name = claims.getSubject();
			if (userUuid != null){
				
				extendTokenValidity(generateToken(name, userUuid), response);
				
				List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
				authorities.add(new SimpleGrantedAuthority(Roles.AUTHORITY_USER));
				if (adminDao.isAdmin(userUuid)) {
					authorities.add(new SimpleGrantedAuthority(Roles.AUTHORITY_ADMIN));
				} else if (adminDao.isSuperAdmin(userUuid)) {
					authorities.add(new SimpleGrantedAuthority(Roles.AUTHORITY_ADMIN));
					authorities.add(new SimpleGrantedAuthority(Roles.AUTHORITY_SUPER_ADMIN));
				}
				
				return new ApplicationUser(userUuid, name, authorities);
			}
		}catch(Exception e){
			LOG.error("error reading token. Cause: " + e.getMessage(), e);
		}	
		return null;
	}

	@Override
	public void doFilter(ServletRequest request,
			ServletResponse response,
			FilterChain filterChain)
					throws IOException, ServletException {

		LOG.debug("doFilter(");
		
		ApplicationUser user = getUserFromAuthHeader((HttpServletRequest)request, (HttpServletResponse)response);
				
		Authentication authentication = null;
		if (user != null){
			authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
		}

		SecurityContextHolder.getContext().setAuthentication(authentication);
		filterChain.doFilter(request,response);
	}
}