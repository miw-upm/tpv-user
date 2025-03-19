package es.upm.api.data.daos;

import es.upm.api.data.entities.Scope;
import es.upm.api.data.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByMobile(String mobile);
    List<User> findByScopeIn(Collection<Scope> scopes);
    boolean existsByMobile(String mobile);
    boolean existsByEmail(String email);
    boolean existsByDni(String dni);

    @Query("select u from User u where " +
            "(coalesce(?1, '') = '' or u.mobile like concat('%',?1,'%')) and " +
            "(coalesce(?2, '') = '' or lower(u.firstName) like lower(concat('%',?2,'%'))) and" +
            "(coalesce(?3, '') = '' or lower(u.familyName) like lower(concat('%',?3,'%'))) and" +
            "(coalesce(?4, '') = '' or lower(u.email) like lower(concat('%',?4,'%'))) and" +
            "(coalesce(?5, '') = '' or lower(u.dni) like lower(concat('%',?5,'%'))) and" +
            "(u.scope in ?6)")
    List<User> findByMobileAndFirstNameAndFamilyNameAndEmailAndDniContainingNullSafe(
            String mobile, String firstName, String familyName, String email, String dni, Collection<Scope> scopes);
}
