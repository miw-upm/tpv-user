package es.upm.api.services;

import es.upm.api.data.daos.UserRepository;
import es.upm.api.data.entities.Scope;
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
        if (!authorizedScopes().contains(user.getScope())) {
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

    private List<Scope> authorizedScopes() {
        Scope scope = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .map(Scope::of)
                .orElse(Scope.NONE);

        return switch (scope) {
            case ADMIN -> List.of(Scope.ADMIN, Scope.MANAGER, Scope.OPERATOR, Scope.CUSTOMER);
            case MANAGER -> List.of(Scope.MANAGER, Scope.OPERATOR, Scope.CUSTOMER);
            case OPERATOR, CUSTOMER -> List.of(Scope.CUSTOMER);
            default -> List.of();
        };
    }

    private void assertNoExistByMobile(String mobile) {
        if (this.userRepository.existsByMobile(mobile)) {
            throw new ConflictException("The mobile already exists: " + mobile);
        }
    }

    private void assertNoExistByEmail(String email) {
        if (email != null && this.userRepository.existsByEmail(email)) {
            throw new ConflictException("The email already exists: " + email);
        }
    }

    private void assertNoExistByDni(String dni) {
        if (dni != null && this.userRepository.existsByDni(dni)) {
            throw new ConflictException("The dni already exists: " + dni);
        }
    }

    public Stream<User> findNullSafe(UserFindCriteria criteria) {
        if (criteria.all()) {
            return this.userRepository.findByScopeIn(authorizedScopes()).stream();
        }

        if (criteria.isProjection()) {
            User user = this.userRepository.findByMobile(criteria.getMobile())
                    .orElseThrow(() -> new NotFoundException("The mobile don't exist: " + criteria.getMobile()));
            if (!SecurityContextHolder.getContext().getAuthentication().getName().contains(criteria.getMobile())) {
                throw new ForbiddenException("Forbidden access to mobile: " + criteria.getMobile());
            }
            return Stream.of(user);
        }

        return this.userRepository.findByMobileAndFirstNameAndFamilyNameAndEmailAndDniContainingNullSafe(
                criteria.getMobile(), criteria.getFirstName(), criteria.getFamilyName(), criteria.getEmail(), criteria.getDni(), this.authorizedScopes()
        ).stream();

    }

    public User read(UUID id) {
        return this.userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The id don't exist: " + id));
    }

}
