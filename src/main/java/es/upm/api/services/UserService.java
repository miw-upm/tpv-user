package es.upm.api.services;

import es.upm.api.data.daos.UserRepository;
import es.upm.api.data.entities.Role;
import es.upm.api.data.entities.User;
import es.upm.api.data.entities.UserFindCriteria;
import es.upm.api.services.exceptions.ConflictException;
import es.upm.api.services.exceptions.ForbiddenException;
import es.upm.api.services.exceptions.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void createUser(User user) {
        if (!authorizedScopes().contains(user.getRole())) {
            throw new ForbiddenException("Insufficient role to create this userDto: " + user);
        }
        this.assertNoExistByMobile(user.getMobile());
        this.assertNoExistByEmail(user.getEmail());
        this.assertNoExistByDni(user.getDni());
        user.setId(UUID.randomUUID());
        user.setPassword(this.passwordEncoder.encode(user.getPassword()));
        user.setRegistrationDate(LocalDateTime.now());
        this.userRepository.save(user);
    }

    private List<Role> authorizedScopes() {
        Role role = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .map(Role::of)
                .orElse(Role.ANONYMOUS);

        return switch (role) {
            case ADMIN -> List.of(Role.ADMIN, Role.MANAGER, Role.OPERATOR, Role.CUSTOMER);
            case MANAGER -> List.of(Role.MANAGER, Role.OPERATOR, Role.CUSTOMER);
            case OPERATOR, CUSTOMER, URL_TOKEN -> List.of(Role.CUSTOMER);
            default -> List.of();
        };
    }

    public User read(UUID id) {
        return this.userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The id don't exist: " + id));
    }

    public User readByMobile(String mobile) {
        return this.userRepository.findByMobile(mobile)
                .orElseThrow(() -> new NotFoundException("The mobile don't exists: " + mobile));
    }

    private void assertNoExistByEmail(String email) {
        if (email != null && this.userRepository.existsByEmail(email)) {
            throw new ConflictException("The email already exists: " + email);
        }
    }

    private void assertNoExistByMobile(String mobile) {
        if (this.userRepository.existsByMobile(mobile)) {
            throw new ConflictException("The mobile already exists: " + mobile);
        }
    }

    private void assertNoExistByDni(String dni) {
        if (dni != null && this.userRepository.existsByDni(dni)) {
            throw new ConflictException("The dni already exists: " + dni);
        }
    }

    public Stream<User> findNullSafe(UserFindCriteria criteria) {
        Stream<User> userDtos;
        if (criteria.all()) {
            userDtos = this.userRepository.findByRoleIn(authorizedScopes()).stream();
        } else {
            userDtos = this.userRepository.findByMobileAndFirstNameAndFamilyNameAndEmailAndDniContainingNullSafe(
                    criteria.getMobile(), criteria.getFirstName(), criteria.getFamilyName(), criteria.getEmail(), criteria.getDni(), this.authorizedScopes()
            ).stream();
        }
        if (SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals(Role.CUSTOMER.roleValue())
                )
        ) {
            userDtos = userDtos.filter(user -> user.getMobile().equals(SecurityContextHolder.getContext().getAuthentication().getName()));
        }
        return userDtos;

    }

}
