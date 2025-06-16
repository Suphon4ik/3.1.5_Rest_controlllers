package ru.kata.spring.boot_security.demo.demo.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.ConstraintViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import ru.kata.spring.boot_security.demo.demo.model.Role;
import ru.kata.spring.boot_security.demo.demo.model.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final RoleRepository roleRepository;

    public UserRepositoryImpl(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public User getUserById(Long id) {
        return entityManager.find(User.class, id);
    }

    @Override
    public User findByUsername(String name) {
        try {
            return entityManager.createQuery("SELECT u FROM User u join fetch u.roles " +
                                    "WHERE u.username=:name",
                    User.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<User> getAllUsers() {
        return entityManager.createQuery("SELECT u FROM User u", User.class).getResultList();
    }

    @Override
    public void saveUser(User user) {
        if (user.getId() == null) {
            entityManager.persist(user);
        } else {
            entityManager.merge(user);
        }
    }

    @Override
    public void deleteUser(Long id) {
        User user = entityManager.find(User.class, id);
        if (user != null) {
            entityManager.remove(user);
        }
    }

    @Override
    public void updateUser(Long id, User user, BindingResult bindingResult, List<Long> roleIds) {
        //проверка валидности данных
        if (bindingResult.hasErrors()) {
            return;
        }

        //проверка на уникальность юсера
        User existingUser = findByUsername(user.getUsername());
        if (existingUser != null || !existingUser.getId().equals(user.getId())) {
            bindingResult.addError(new ObjectError("user",
                    "Username already exists"));
            return;
        }

        user.setId(id);
        entityManager.merge(user);

    }

    @Override
    public Map<String, Object> getUserWithRolesForEdit(long id) {
        User user = entityManager.find(User.class, id);
        if (user == null) {
            throw new IllegalStateException("User with id " + id + " not found");
        }
        List<Role> roles = roleRepository.getAllRoles();

        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        response.put("roles", roles);

        return response;
    }
}
