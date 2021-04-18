package us.jbec.lct.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration {

    @Order(2)
    @Configuration
    public static class RestConfiguration extends WebSecurityConfigurerAdapter {

        AuthenticationProvider provider;

        public RestConfiguration(final AuthenticationProvider authenticationProvider) {
            super();
            this.provider = authenticationProvider;
        }

        // The URLs to protect
        private RequestMatcher requestMatcher() {
            List<RequestMatcher> matchers = new ArrayList<>();
            matchers.add(new AntPathRequestMatcher("/login**"));
            matchers.add(new AntPathRequestMatcher("/api/**"));
            return new OrRequestMatcher(matchers);
        }

        @Override
        protected void configure(final AuthenticationManagerBuilder auth) {
            auth.authenticationProvider(provider);
        }

        @Override
        public void configure(final WebSecurity webSecurity) {
//            webSecurity.ignoring().antMatchers("/dev/**");
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .and()
                    .exceptionHandling()
                    .and()
                    .authenticationProvider(provider)
                    .addFilterBefore(authenticationFilter(), AnonymousAuthenticationFilter.class)
                    .authorizeRequests()
                    .requestMatchers(requestMatcher()).authenticated()
                    .and()
                    .csrf().disable()
                    .formLogin().disable()
                    .httpBasic().disable()
                    .logout().disable();
        }

        @Bean
        us.jbec.lct.security.AuthenticationFilter authenticationFilter() throws Exception {
            final var filter = new us.jbec.lct.security.AuthenticationFilter(requestMatcher());
            filter.setAuthenticationManager(authenticationManager());
            return filter;
        }

        @Bean
        AuthenticationEntryPoint forbiddenEntryPoint() {
            return new HttpStatusEntryPoint(HttpStatus.FORBIDDEN);
        }
    }

    @Configuration
    @Order(1)
    public static class WebConfiguration extends WebSecurityConfigurerAdapter {

        AuthenticationProvider provider;

        public WebConfiguration(final AuthenticationProvider authenticationProvider) {
            super();
            this.provider = authenticationProvider;
        }

        // The URLs to protect
        private RequestMatcher requestMatcher() {
            List<RequestMatcher> matchers = new ArrayList<>();
            matchers.add(new AntPathRequestMatcher("/dev/**"));
            matchers.add(new AntPathRequestMatcher("/dev**"));
            return new OrRequestMatcher(matchers);
        }

        @Override
        protected void configure(final AuthenticationManagerBuilder auth) {
            auth.authenticationProvider(provider);
        }

        @Override
        public void configure(final WebSecurity webSecurity) {
//            webSecurity.ignoring().antMatchers("/api/**");
//            webSecurity.ignoring().antMatchers("/login**");
        }

        @Override
        public void configure(HttpSecurity http) throws Exception {
            http.antMatcher("/dev/**")
                    .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
//                    .and()
//                    .authorizeRequests()
//                    .anyRequest().hasRole("ADMIN")
                    .and()
                    .csrf().disable();
        }

        us.jbec.lct.security.AuthenticationFilter authenticationFilter() throws Exception {
            final var filter = new us.jbec.lct.security.AuthenticationFilter(requestMatcher());
            filter.setAuthenticationManager(authenticationManager());
            return filter;
        }
    }
}
