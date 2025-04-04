package es.upm.api.data.daos;

import es.upm.api.data.entities.Role;
import es.upm.api.data.entities.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Log4j2
@Repository
public class DatabaseStarting {

    private final String admin;
    private final String mobile;
    private final String password;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DatabaseStarting(
            UserRepository userRepository,
            @Value("${miw.admin}") String admin,
            @Value("${miw.mobile}") String mobile,
            @Value("${miw.password}") String password,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.admin = admin;
        this.mobile = mobile;
        this.password = password;
        this.passwordEncoder = passwordEncoder;
        this.initialize();
    }

    public void initialize() {
        if (this.userRepository.findByRoleIn(List.of(Role.ADMIN)).isEmpty()) {
            User user = User.builder().id(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff6666")).mobile(this.mobile).firstName(this.admin)
                    .password(this.passwordEncoder.encode(this.password))
                    .role(Role.ADMIN).registrationDate(LocalDateTime.now()).active(true).build();
            this.userRepository.save(user);
            log.warn("------- Created Admin -----------");
        }
    }

}
