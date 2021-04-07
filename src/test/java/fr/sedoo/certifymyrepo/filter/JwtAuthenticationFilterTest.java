package fr.sedoo.certifymyrepo.filter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import fr.sedoo.certifymyrepo.rest.dao.AdminDao;
import fr.sedoo.certifymyrepo.rest.filter.JwtAuthenticationFilter;
import fr.sedoo.certifymyrepo.rest.filter.jwt.JwtConfig;
import fr.sedoo.certifymyrepo.rest.filter.jwt.JwtUtil;
import fr.sedoo.certifymyrepo.rest.habilitation.ApplicationUser;
import io.jsonwebtoken.Claims;

@RunWith(SpringJUnit4ClassRunner.class)
public class JwtAuthenticationFilterTest {
	
	private static final String TOKEN_0000_0000_0000_1234 = "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE2MTI1MzQyMTgsImV4cCI6MTk1OTYwMzAxOCwiYXVkIjoid3d3LnNlZG9vLmNvbSIsInN1YiI6InRvbXRvbS5uYW5hQG9icy1taXAuZnIiLCJ1dWlkIjoiMDAwMC0wMDAwLTAwMDAtMTIzNCJ9.0AsmdkgKNufbKJxFPWamP0icCJX7o1LnqDp0Rv_TJNw";
	
	@Mock
	private MockHttpServletRequest request;
	
	@Mock
	private MockHttpServletResponse response;
	
	@Mock
	private AdminDao adminDao;
	
	@Mock
	private JwtConfig jwtConfig;
	
	@InjectMocks
	private JwtAuthenticationFilter filter;
	
	@Test
	public void testgetUserFromAuthHeader() {
		
		when(request.getHeader(JwtUtil.AUTH_HEADER)).thenReturn(TOKEN_0000_0000_0000_1234);
		when(adminDao.isAdmin(any())).thenReturn(true);
		when(jwtConfig.getSigningKey()).thenReturn("secretkey");
		when(jwtConfig.getTokenValidity()).thenReturn(10000);

		ApplicationUser user = filter.getUserFromAuthHeader(request, response);
		assertEquals("tomtom.nana@obs-mip.fr", user.getName());
		assertEquals("0000-0000-0000-1234", user.getUserId());
		assertTrue(user.getRoles().contains("ROLE_USER"));
		assertTrue(user.getRoles().contains("ROLE_ADMIN"));
	}
	
	@Test
	public void testJwtUtilGetSubjectFromAuthHeader() {
		try {
			assertEquals("tomtom.nana@obs-mip.fr", JwtUtil.getSubjectFromAuthHeader(TOKEN_0000_0000_0000_1234, "secretkey"));
		} catch(Exception e) {
			assertFalse("No exception has to be thrown", true);
		}
	}
	
	@Test
	public void testJwtUtilGetClaims() {
		try {
			when(request.getHeader(JwtUtil.AUTH_HEADER)).thenReturn(TOKEN_0000_0000_0000_1234);
			Claims claims = JwtUtil.getClaims(JwtUtil.getTokenFromAuthHeader(request), "secretkey");
			assertEquals("Online JWT Builder",claims.getIssuer());
			assertEquals("www.sedoo.com",claims.getAudience());
			assertEquals("tomtom.nana@obs-mip.fr",claims.getSubject());
		} catch(Exception e) {
			assertFalse("No exception has to be thrown", true);
		}
	}

}
