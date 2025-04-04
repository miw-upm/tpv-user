package es.upm.api.configurations;

import es.upm.api.data.daos.UserRepository;
import es.upm.api.data.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UserAuthConfig { // Authentication with user:password

    private final UserRepository userRepository;

    @Autowired
    public UserAuthConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return mobile -> {
            User user = userRepository.findByMobile(mobile)
                    .orElseThrow(() -> new UsernameNotFoundException("UserDto not found: " + mobile));
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getMobile())
                    .password(user.getPassword())
                    .roles(user.getRole().value())
                    .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder(); // BCrypt by default
    }
}
