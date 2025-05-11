package site.code4fun.repository.jpa;

import site.code4fun.model.Role;

import java.util.Optional;

public interface RoleRepository extends BaseRepository<Role, Long> {
    Optional<Role> findByNameContains(String name);
}
