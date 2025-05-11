package site.code4fun.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.springframework.security.crypto.password.PasswordEncoder;
import site.code4fun.model.User;
import site.code4fun.repository.jpa.RoleRepository;
import site.code4fun.repository.jpa.UserRepository;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) // JUnit 5
@SuppressWarnings("unused")
class UserServiceTest {

    @Mock
    UserRepository repository;
    @Mock
    PasswordEncoder passwordEncoder;
    @Mock
    RoleRepository roleRepository;
    @InjectMocks
    UserService service;

    User user;

    @Test
    void whenGetAll_shouldReturnList() {
        // 1. create mock data
        List<User> expect = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            User user1 = new User();
            user1.setId((long)i);
            user1.setUsername("trungtq");
            expect.add(user1);
        }

        // 2. define behavior of Repository
        when(repository.findAll()).thenReturn(expect);

        // 3. call service method
        List<User> actual = service.getAll();

        // 4. assert the result
        assertThat(actual).hasSameSizeAs(expect);

        // 4.1 ensure repository is called
        verify(repository).findAll();
    }

    @Test
    void givenValidUser_whenSaveUser_thenSucceed() {
        // Given
        User user = new User();
        user.setUsername("Jery");
        user.setPassword("123456");
        when(repository.save(any(User.class))).then(new Answer<User>() {
            Long sequence = 1L;

            @Override
            public User answer(InvocationOnMock invocation) {
                User user = invocation.getArgument(0);
                user.setId(sequence++);
                return user;
            }
        });

        User insertedUser = service.create(user);
        verify(repository).save(user);
        assertNotNull(insertedUser.getId());
    }
}
