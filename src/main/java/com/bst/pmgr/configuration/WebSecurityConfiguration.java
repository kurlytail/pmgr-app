package com.bst.pmgr.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bst.user.authentication.components.UserService;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Value("${pmgr.app.configuration.security:enabled}")
	private String securityState;

	@Autowired
	private UserService userService;

	public WebSecurityConfiguration() {
		super(false);
	}

	@Bean
	public DaoAuthenticationProvider authProvider() {
		final DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
		authProvider.setUserDetailsService(this.userService);
		authProvider.setPasswordEncoder(this.passwordEncoder);
		return authProvider;
	}

	@Override
	protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(this.authProvider());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry matchers = http
				.antMatcher("/**").authorizeRequests();
		if (this.securityState.equals("enabled")) {
			matchers = matchers.antMatchers("/assets/**").permitAll();
			matchers = matchers.antMatchers("/auth/**").permitAll();
			matchers = matchers.antMatchers("/*").permitAll();
			matchers = matchers.anyRequest().authenticated();

			http = matchers.and();
			http = http.logout().logoutSuccessUrl("/index").logoutUrl("/auth/signout").and();
			http = http.formLogin().failureUrl("/auth/signin?error").loginPage("/auth/signin")
					.loginProcessingUrl("/auth/signin").usernameParameter("email").passwordParameter("password").and();
		} else {
			matchers = matchers.antMatchers("/**/*").permitAll();
		}

		http.csrf().disable();
	}

	@Override
	public void configure(final WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/resources/**");
	}

	@Bean
	public AuthenticationManager customAuthenticationManager() throws Exception {
		return this.authenticationManager();
	}
}
