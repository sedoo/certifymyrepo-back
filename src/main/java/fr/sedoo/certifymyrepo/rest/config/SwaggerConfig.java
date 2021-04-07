package fr.sedoo.certifymyrepo.rest.config;

import javax.servlet.ServletContext;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.paths.RelativePathProvider;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

	
	@Bean
	@Profile(Profiles.DEV_PROFILE)
	public Docket testApi(ServletContext servletContext) { 
		return new Docket(DocumentationType.SWAGGER_2).ignoredParameterTypes(AuthenticationPrincipal.class)
				.pathProvider(new RelativePathProvider(servletContext) {
					@Override
					public String getApplicationBasePath() {
						return "/";
					}
				}).select().apis(RequestHandlerSelectors.basePackage("fr.sedoo")).paths(PathSelectors.any()).build();                                    
	}
	
	@Bean
	@Profile(Profiles.PRODUCTION_PROFILE)
	public Docket prodApi(ServletContext servletContext) { 
		return new Docket(DocumentationType.SWAGGER_2).ignoredParameterTypes(AuthenticationPrincipal.class)
				.pathMapping("/certifymyrepo").pathProvider(new RelativePathProvider(servletContext) {
					@Override
					public String getApplicationBasePath() {
						return "/";
					}
				}).select().apis(RequestHandlerSelectors.basePackage("fr.sedoo")).paths(PathSelectors.any()).build();                       
	}
	
	@Bean
	@Profile(Profiles.PRE_PRODUCTION_PROFILE)
	public Docket preprodApi(ServletContext servletContext) { 
		return new Docket(DocumentationType.SWAGGER_2).ignoredParameterTypes(AuthenticationPrincipal.class)
				.pathMapping("/crusoe-preprod").pathProvider(new RelativePathProvider(servletContext) {
					@Override
					public String getApplicationBasePath() {
						return "/";
					}
				}).select().apis(RequestHandlerSelectors.basePackage("fr.sedoo")).paths(PathSelectors.any()).build();                       
	}
	
	
}

