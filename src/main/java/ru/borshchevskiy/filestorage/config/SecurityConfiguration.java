package ru.borshchevskiy.filestorage.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import ru.borshchevskiy.filestorage.config.handlers.LoginSuccessHandler;

/**
 * Security configuration.
 *
 * @see LoginSuccessHandler
 * @see PasswordEncoderConfiguration
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final LoginSuccessHandler successHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .sessionManagement((configurer) -> configurer.maximumSessions(1))
                .authorizeHttpRequests((configurer) -> configurer.requestMatchers(
                                "/",
                                "/login",
                                "/registration",
                                "/images/**",
                                "/favicon.ico").permitAll()
                        .anyRequest().authenticated())
                .formLogin(login ->
                        login.loginPage("/login")
                                .usernameParameter("email")
                                .failureUrl("/login?error=true")
                                .successHandler(successHandler)
                )
                .logout(logout ->
                        logout.logoutUrl("/logout")
                                .logoutSuccessUrl("/login"));

        return httpSecurity.build();
    }
}
