package site.code4fun.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import site.code4fun.repository.jpa.UserRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // JUnit 5
class UserRepositoryMockitoExtensionTest {

    @Mock
    UserRepository mockRepository;

    @Test
    void givenCountMethodMocked_WhenCountInvoked_ThenMockValueReturned() {
        when(mockRepository.count()).thenReturn(123L);

        long userCount = mockRepository.count();

        assertEquals(123L, userCount);
        verify(mockRepository).count();
    }

    @Test
    void givenCountMethodMocked_WhenCountInvoked_ThenMockedValueReturned() {
        UserRepository localMockRepository = mock(UserRepository.class);
        when(localMockRepository.count()).thenReturn(111L);

        long userCount = localMockRepository.count();

        assertEquals(111L, userCount);
        verify(localMockRepository).count();
    }
}


