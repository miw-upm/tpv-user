package es.upm.api.data.daos;

import es.upm.api.data.entities.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static es.upm.api.data.entities.Role.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class UserDtoRepositoryIT {

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindByMobile() {
        assertThat(this.userRepository.findByMobile("6")).isPresent();
    }

    @Test
    void testFindByScopeIn() {
        List<Role> roles = List.of(ADMIN, MANAGER);
        assertThat(this.userRepository.findByRoleIn(roles))
                .isNotEmpty()
                .allMatch(user -> roles.contains(user.getRole()));
    }

    @Test
    void testFindByMobileAndFirstNameAndFamilyNameAndEmailAndDniNullSafeWithMobile() {
        assertThat(this.userRepository.findByMobileAndFirstNameAndFamilyNameAndEmailAndDniContainingNullSafe(
                "1", null, null, ".com", null, List.of(MANAGER)))
                .anyMatch(user -> "666666001".equals(user.getMobile()));
    }

    @Test
    void testFindByMobileAndFirstNameAndFamilyNameAndEmailAndDniNullSafeWithDni() {
        assertThat(this.userRepository.findByMobileAndFirstNameAndFamilyNameAndEmailAndDniContainingNullSafe(
                null, null, null, null, "kk", List.of(ADMIN, MANAGER, OPERATOR, CUSTOMER)))
                .isEmpty();
    }
}
