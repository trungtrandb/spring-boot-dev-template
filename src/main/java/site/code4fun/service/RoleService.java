package site.code4fun.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import site.code4fun.exception.ServiceException;
import site.code4fun.model.Role;
import site.code4fun.model.User;
import site.code4fun.repository.jpa.RoleRepository;
import site.code4fun.repository.jpa.UserRepository;
import site.code4fun.util.SecurityUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Lazy
@Getter(AccessLevel.PROTECTED)
public class RoleService extends AbstractBaseService<Role, Long> {

    private final RoleRepository repository;
    private final UserRepository userRepository;

    @Override
    public Role create(Role role) {
        Set<String> privileges = role.getPrivileges().stream()
                .map(String::trim)
                .collect(Collectors.toSet());

        role.setPrivileges(privileges);

        if (role.getId() == null) {
            role.setName("ROLE_" + System.currentTimeMillis());
        } else {
            getRepository().findById(role.getId()).ifPresent(existingRole -> {
                if (!"ROLE_ADMIN".equals(existingRole.getName())) {
                    role.setName("ROLE_" + System.currentTimeMillis());
                } else {
                    role.setName("ROLE_ADMIN");
                }
            });
        }

        return getRepository().save(role);
    }


    @Override
    public void deleteByIds(Collection<Long> ids) {
        User currentUser = SecurityUtils.getUser();
        List<Long> currentUserRoleIds = new ArrayList<>();
        if (currentUser != null){
            currentUserRoleIds.addAll(currentUser.getRoles().stream().map(Role::getId).toList());
        }
        ids.forEach(id->{
            if (currentUserRoleIds.contains(id)) throw new ServiceException("Can't delete your group");
            Role r = getById(id);
            userRepository.findAllByRoles_idIn(ids).forEach(user -> user.removeRole(r));
            getRepository().delete(r);
        });
    }
}
