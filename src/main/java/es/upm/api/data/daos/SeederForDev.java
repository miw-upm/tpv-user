package es.upm.api.data.daos;

import es.upm.api.data.entities.Role;
import es.upm.api.data.entities.User;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

@Log4j2
@Repository
@Profile({"dev", "test"})
public class SeederForDev {
    private final String pass;
    private final DatabaseStarting databaseStarting;
    private final UserRepository userRepository;

    @Autowired
    public SeederForDev(UserRepository userRepository, DatabaseStarting databaseStarting, @Value("${miw.password}") String password, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.databaseStarting = databaseStarting;
        this.pass = passwordEncoder.encode(password);
        this.deleteAllAndInitializeAndSeedDataBase();
    }

    public void deleteAllAndInitializeAndSeedDataBase() {
        this.deleteAllAndInitialize();
        this.seedDataBase();
    }

    public void deleteAllAndInitialize() {
        this.userRepository.deleteAll();
        log.warn("------- Deleted All -----------");
        this.databaseStarting.initialize();
    }

    private void seedDataBase() {
        log.warn("------- Initial Load from JAVA -----------");
        User[] users = {
                User.builder().id(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000"))
                        .mobile("66").firstName("customer").password(pass).role(Role.CUSTOMER)
                        .registrationDate(LocalDateTime.now()).active(true).build(),
                User.builder().id(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0001"))
                        .mobile("666666000").firstName("adm").password(pass).dni(null).address("C/TPV, 0")
                        .email("adm@gmail.com").role(Role.ADMIN).registrationDate(LocalDateTime.now()).active(true)
                        .build(),
                User.builder().id(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0002"))
                        .mobile("666666001").firstName("man").password(pass).dni("66666601C").address("C/TPV, 1")
                        .email("man@gmail.com").role(Role.MANAGER).registrationDate(LocalDateTime.now()).active(true)
                        .build(),
                User.builder().id(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0003"))
                        .mobile("666666002").firstName("ope").password(pass).dni("66666602K").address("C/TPV, 2")
                        .email("ope@gmail.com").role(Role.OPERATOR).registrationDate(LocalDateTime.now()).active(true)
                        .build(),
                User.builder().id(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0004"))
                        .mobile("666666003").firstName("c1").familyName("ac1").password(pass).dni("66666603E")
                        .address("C/TPV, 3").email("c1@gmail.com").role(Role.CUSTOMER)
                        .registrationDate(LocalDateTime.now()).active(true).build(),
                User.builder().id(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0005"))
                        .mobile("666666004").firstName("c2").familyName("ac2").password(pass).dni("66666604T")
                        .address("C/TPV, 4").email("c2@gmail.com").role(Role.CUSTOMER)
                        .registrationDate(LocalDateTime.now()).active(true).build(),
                User.builder().id(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0006"))
                        .mobile("666666005").firstName("c3").password(pass).role(Role.CUSTOMER)
                        .registrationDate(LocalDateTime.now()).active(true).build()
        };
        this.userRepository.saveAll(Arrays.asList(users));
        log.warn("        ------- users");
    }

}
