/*
 * Copyright (c) 2023-2025 Open Shop Channel
 *
 * This program is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this
 * program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.oscwii.repositorymanager.config;

import org.oscwii.repositorymanager.config.repoman.SecurityConfig;
import org.oscwii.repositorymanager.security.RepoManAuthenticationProvider;
import org.oscwii.repositorymanager.security.RepoManAuthorizationManager;
import org.oscwii.repositorymanager.services.AuthService;
import org.springframework.aop.Advisor;
import org.springframework.aop.support.JdkRegexpMethodPointcut;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authorization.method.AuthorizationInterceptorsOrder;
import org.springframework.security.authorization.method.AuthorizationManagerBeforeMethodInterceptor;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity(prePostEnabled = false)
public class WebSecurityConfig
{
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityConfig config) throws Exception
    {
        http
                .authorizeHttpRequests(requests -> requests
                        // We will be using method security
                        .anyRequest().permitAll())
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
                .csrf(csrf -> csrf.
                        ignoringRequestMatchers("/shop/**")
                )
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
        source.registerCorsConfiguration("/hbb/**", config);
        source.registerCorsConfiguration("/api/**", config);
        source.registerCorsConfiguration("/shop/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public AuthenticationProvider authProvider(AuthService authService, PasswordEncoder encoder)
    {
        RepoManAuthenticationProvider authProvider = new RepoManAuthenticationProvider();
        authProvider.setUserDetailsService(authService);
        authProvider.setPasswordEncoder(encoder);
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public Advisor preAuthorize(RepoManAuthorizationManager manager)
    {
        JdkRegexpMethodPointcut pointcut = new JdkRegexpMethodPointcut();
        pointcut.setPattern("org.oscwii.repositorymanager.controllers.*");
        var interceptor = new AuthorizationManagerBeforeMethodInterceptor(pointcut, manager);
        interceptor.setOrder(AuthorizationInterceptorsOrder.PRE_AUTHORIZE.getOrder() - 1);
        return interceptor;
    }
}
