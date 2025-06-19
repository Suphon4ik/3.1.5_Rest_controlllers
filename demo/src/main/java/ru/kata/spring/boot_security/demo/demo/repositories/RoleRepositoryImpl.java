package ru.kata.spring.boot_security.demo.demo.repositories;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.kata.spring.boot_security.demo.demo.model.Role;

import java.util.List;
import java.util.Optional;

@Repository
public class RoleRepositoryImpl implements RoleRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Optional<Role> findByName(String roleName) {

        try {
            Role role = entityManager.createQuery(
                    "SELECT r FROM Role r WHERE r.name=:name", Role.class)
                    .setParameter("name", roleName)
                    .getSingleResult();
            return Optional.ofNullable(role);
        } catch (Exception e) {
            return Optional.empty();
        }

    }

    @Override
    public Role save(Role role) {
        if (role.getId() == null) {
            entityManager.persist(role);
        } else {
            entityManager.merge(role);
        }
        return role;
    }

    @Override
    public List<Role> getAllRoles() {
        return entityManager.createQuery("SELECT r FROM Role r", Role.class).getResultList();
    }

    @Override
    public List<Role> findRolesByIds(List<Long> roleIds) {
        return entityManager.createQuery("SELECT r FROM Role r WHERE r.id IN :ids", Role.class)
                .setParameter("ids", roleIds)
                .getResultList();
    }
}
