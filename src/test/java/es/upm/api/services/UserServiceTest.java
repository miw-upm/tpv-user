package es.upm.api.services;

import es.upm.api.data.entities.Scope;
import es.upm.api.data.entities.User;
import es.upm.api.services.exceptions.ForbiddenException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void testCreateUser() {
        User userDto = User.builder().id(UUID.randomUUID()).mobile("000000001").firstName("k").scope(Scope.ADMIN).build();
        assertThrows(ForbiddenException.class, () -> this.userService.createUser(userDto, Scope.MANAGER));
    }

    @Test
    void testCreateUserForbidden() {
        User userDto = User.builder().id(UUID.randomUUID()).mobile("666000666").firstName("k").scope(Scope.ADMIN).build();
        assertThrows(ForbiddenException.class, () -> this.userService.createUser(userDto, Scope.MANAGER));
    }

    @Test
    void testCreateUserForbiddenByEmail() {
        User userDto = User.builder().id(UUID.randomUUID()).mobile("000000002").firstName("k").email("adm@gmail.com").scope(Scope.ADMIN).build();
        assertThrows(ForbiddenException.class, () -> this.userService.createUser(userDto, Scope.MANAGER));
    }

    @Test
    void testCreateUserForbiddenByDni() {
        User userDto = User.builder().id(UUID.randomUUID()).mobile("000000003").firstName("k").dni("66666601C").scope(Scope.ADMIN).build();
        assertThrows(ForbiddenException.class, () -> this.userService.createUser(userDto, Scope.MANAGER));
    }

}
