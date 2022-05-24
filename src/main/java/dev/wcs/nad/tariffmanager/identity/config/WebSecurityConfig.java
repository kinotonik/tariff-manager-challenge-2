package dev.wcs.nad.tariffmanager.identity.config;

import dev.wcs.nad.tariffmanager.identity.user.SecurityUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private final SecurityUserService userService;

    private final PasswordEncoder passwordEncoder;

    public WebSecurityConfig(SecurityUserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // @formatter:off
                http.csrf().disable();

                // TODO: Only allow frames if using h2 as the database
                http.headers().frameOptions().disable();
                http.authorizeRequests()
                        .antMatchers("/public/private/**").authenticated()
                        .antMatchers("/public/**", "/webjars/**", "/", "/logout", "/api/**", "/v3/**", "/login", "/swagger-ui/**", "/swagger-ui.html", "/h2-console/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated()

                    .and()
                .formLogin()
                    .loginPage("/public/sign-in").permitAll()
                    .loginProcessingUrl("/public/do-sign-in")
//                    .defaultSuccessUrl("/")
                    .failureUrl("/public/sign-in?error=true")
                    .usernameParameter("username")
                    .passwordParameter("password")
                    //.failureHandler(userService)
                    //.successHandler(userService)
                    .and()
                .logout()
                    .logoutRequestMatcher(new AntPathRequestMatcher("/public/logout"))
                    .clearAuthentication(true)
                    .invalidateHttpSession(true)
                    .deleteCookies("JSESSIONID");
//                .and()
//                    .anonymous();
        // @formatter:on
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService)
                .passwordEncoder(passwordEncoder);
    }

    @Bean
    public AuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new DaoOverride();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

}