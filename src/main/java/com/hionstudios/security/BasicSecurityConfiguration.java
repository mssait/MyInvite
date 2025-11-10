package com.hionstudios.security;

import static org.springframework.security.config.Customizer.withDefaults;

import java.util.Arrays;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;

import com.hionstudios.db.Handler;
import com.hionstudios.iam.JwtAuthenticationEntryPoint;
import com.hionstudios.iam.JwtRequestFilter;
import com.hionstudios.iam.JwtTokenUtil;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class BasicSecurityConfiguration extends WebSecurityConfigurerAdapter {
    private final UserDetailsService userDetailsService;
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private UserDetailsService jwtUserDetailsService;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(jwtUserDetailsService).passwordEncoder(NoPasswordEncoder.getInstance());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoPasswordEncoder.getInstance();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    BasicSecurityConfiguration(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public AuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(NoPasswordEncoder.getInstance());
        return provider;
    }

    @Bean
    @Override
    protected UserDetailsService userDetailsService() {
        return super.userDetailsService();
    }

    private static void successHandler(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Handler.write(new JSONObject().put("page", "/"), response);
    }

    private static CorsConfiguration corsConfigurationSource(HttpServletRequest request) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:8080","https://products.znifa.com"));
        cors.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        cors.setAllowCredentials(true);
        cors.addAllowedHeader("*");
        return cors;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(cors -> cors.configurationSource(BasicSecurityConfiguration::corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .authorizeRequests(withDefaults())
                .exceptionHandling(handling -> handling.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(login -> login
                        .loginProcessingUrl("/login")
                        .successHandler(BasicSecurityConfiguration::successHandler)
                        .loginPage("/login").failureForwardUrl("/").permitAll())
                .logout(logout -> logout
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies(JwtTokenUtil.TOKEN_NAME, "JSESSIONID")
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessUrl("/"));
    }
}
