package kr.co.classact.rancher.login.repository;

import kr.co.classact.rancher.login.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final EntityManager em;

    public void save(User user) {
        em.persist(user);
    }

    public Optional<User> findByEmail(String email) {
        List<User> users = em.createQuery(
                "SELECT u " +
                        "  FROM User u " +
                        " WHERE u.email = :email "
                , User.class
                )
                .setParameter("email", email)
                .getResultList();

        users.add(null);
        return Optional.ofNullable(users.get(0));
    }
}
