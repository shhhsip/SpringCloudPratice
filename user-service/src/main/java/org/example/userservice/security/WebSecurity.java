package org.example.userservice.security;

import lombok.RequiredArgsConstructor;
import org.example.userservice.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.servlet.Filter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurity extends WebSecurityConfigurerAdapter {
    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final Environment env;


    // 권한관련
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        /*http.csrf().disable();*/
        /*http.authorizeRequests().antMatchers("/users/**").permitAll();*/
        http.authorizeRequests().antMatchers("/actuator/**").permitAll();
        http.authorizeRequests().antMatchers("/users/**")
                .hasIpAddress("61.254.97.89") // IP 제한
                .and()
                .addFilter(getAuthenticationFilter())
                .csrf().disable();

        http.headers().frameOptions().disable();//h2 console이 프레임으로 나눠져있기때문에 깨지기때문에 꺼줌
    }

    private AuthenticationFilter getAuthenticationFilter() throws Exception {
        AuthenticationFilter authenticationFilter
                = new AuthenticationFilter(authenticationManager(), userService, env);

        return authenticationFilter;
    }

    //인증 관련
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
    }
}
