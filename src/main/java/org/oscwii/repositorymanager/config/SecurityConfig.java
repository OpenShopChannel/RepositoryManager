package org.oscwii.repositorymanager.config;

import org.oscwii.repositorymanager.config.repoman.RepoManSecurityConfig;
import org.oscwii.repositorymanager.services.AuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig
{
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, RepoManSecurityConfig config) throws Exception
    {
        http
                .authorizeHttpRequests(requests -> requests
                        .requestMatchers("/", "/static/**", "/api/**", "/hbb/**").permitAll()
                        .requestMatchers("/admin/register", "/admin/reset-password").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/admin/login")
                        .defaultSuccessUrl("/admin")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/admin/login?logout")
                        .permitAll())
                .rememberMe(rememberMe -> rememberMe
                        .key(config.secretKey())
                        .rememberMeParameter("remember-me")
                        .rememberMeCookieName("remember-me")
                        .tokenValiditySeconds(365 * 24 * 60 * 60)
                )
                // Enable CSRF
                .csrf(Customizer.withDefaults())
                // Enable CORS
                .cors(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public CorsFilter corsFilter()
    {
        var source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/hbb/", config);
        source.registerCorsConfiguration("/api/v3/", config);
        return new CorsFilter(source);
    }

    @Bean
    public AuthenticationProvider authProvider(AuthService authService, PasswordEncoder encoder)
    {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(authService);
        authProvider.setPasswordEncoder(encoder);
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }
}
