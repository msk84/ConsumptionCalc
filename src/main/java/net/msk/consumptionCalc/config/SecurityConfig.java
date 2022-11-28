package net.msk.consumptionCalc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests().requestMatchers("/**", "/css/**","/images/**", "/js/**", "/fonts/**", "/actuator/**", "/webjars/**").permitAll()
				.and().csrf().ignoringRequestMatchers("/api/**");
		return http.build();
	}
}
