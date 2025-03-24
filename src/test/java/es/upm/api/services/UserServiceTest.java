package es.upm.api.services;

import es.upm.api.data.entities.Scope;
import es.upm.api.data.entities.User;
import es.upm.api.data.entities.UserFindCriteria;
import es.upm.api.services.exceptions.ForbiddenException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    @WithMockUser(username = "666666003", authorities = {"SCOPE_manager"})
    void testCreateUser() {
        User userDto = User.builder().id(UUID.randomUUID()).mobile("000000001").firstName("k").scope(Scope.ADMIN).build();
        assertThrows(ForbiddenException.class, () -> this.userService.createUser(userDto));
    }

    @Test
    @WithMockUser(username = "666666003", authorities = {"SCOPE_manager"})
    void testCreateUserForbidden() {
        User userDto = User.builder().id(UUID.randomUUID()).mobile("666000666").firstName("k").scope(Scope.ADMIN).build();
        assertThrows(ForbiddenException.class, () -> this.userService.createUser(userDto));
    }

    @Test
    @WithMockUser(username = "666666003", authorities = {"SCOPE_manager"})
    void testCreateUserForbiddenByEmail() {
        User userDto = User.builder().id(UUID.randomUUID()).mobile("000000002").firstName("k").email("adm@gmail.com").scope(Scope.ADMIN).build();
        assertThrows(ForbiddenException.class, () -> this.userService.createUser(userDto));
    }

    @Test
    @WithMockUser(username = "666666003", authorities = {"SCOPE_manager"})
    void testCreateUserForbiddenByDni() {
        User userDto = User.builder().id(UUID.randomUUID()).mobile("000000003").firstName("k").dni("66666601C").scope(Scope.ADMIN).build();
        assertThrows(ForbiddenException.class, () -> this.userService.createUser(userDto));
    }

    @Test
    @WithMockUser(username = "666666003", authorities = {"SCOPE_manager"})
    void testReadOwnerUser() {
        UserFindCriteria criteria = new UserFindCriteria();
        criteria.setMobile("666666003");
        criteria.setProjection(true);
        List<User> users = this.userService.findNullSafe(criteria).toList();
        assertThat(users)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1)
                .allMatch(user -> user.getMobile().equals("666666003"));
    }

    @Test
    @WithMockUser(username = "666666003", authorities = {"SCOPE_manager"})
    void testReadOtherUser() {
        UserFindCriteria criteria = new UserFindCriteria();
        criteria.setMobile("666666004");
        criteria.setProjection(true);
        assertThrows(ForbiddenException.class, () -> this.userService.findNullSafe(criteria));
    }

}
