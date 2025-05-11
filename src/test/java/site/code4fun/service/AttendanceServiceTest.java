package site.code4fun.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import site.code4fun.model.AttendanceEntity;
import site.code4fun.model.ShiftEntity;
import site.code4fun.model.User;
import site.code4fun.repository.jpa.AttendanceRepository;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {  // Assuming this method is in AttendanceService

  @Mock
  private AttendanceRepository attendanceRepository;

  @Mock
  private UserService userService;

  @InjectMocks
  private AttendanceService attendanceService;  // Adjust class name as needed

  private User testUser;

  @BeforeEach
  void setUp() {
    ShiftEntity shiftEntity = new ShiftEntity();
    shiftEntity.setId(1L);
    shiftEntity.setStartTime(LocalTime.of(9, 0));
    shiftEntity.setEndTime(LocalTime.of(18 , 0));
    shiftEntity.setBreakStartTime(LocalTime.of(12, 0));
    shiftEntity.setBreakEndTime(LocalTime.of(13, 0));
    testUser = new User();
    testUser.setId(1L);
    testUser.setShift(shiftEntity);
  }

//  @Test
//  void testCreateNewAttendance_CheckIn() {
//    // Arrange
//    LocalDate today = LocalDate.now();
//    LocalDateTime startOfDay = today.atStartOfDay();
//    LocalDateTime endOfDay = today.atTime(23, 59, 59);
//
//    when(attendanceRepository.findByUser_IdAndCreatedBetween(1L, startOfDay, endOfDay))
//            .thenReturn(Collections.emptyList());
//    when(userService.getCurrentUser()).thenReturn(testUser);
//    when(attendanceRepository.save(any(AttendanceEntity.class)))
//            .thenAnswer(invocation -> invocation.getArgument(0));
//
//    // Act
//    AttendanceEntity result = attendanceService.create(null);
//
//    // Assert
//    assertNotNull(result);
//    assertEquals(testUser, result.getUser());
//    assertNotNull(result.getCheckIn());
//    assertNull(result.getCheckOut());
//    assertEquals(AttendanceEntity.Status.PRESENT, result.getStatus());
//
//    verify(attendanceRepository).findByUser_IdAndCreatedBetween(1L, startOfDay, endOfDay);
//    verify(attendanceRepository).save(any(AttendanceEntity.class));
//  }

//  @Test
//  void testCreateExistingAttendance_CheckOut() {
//    // Arrange
//    LocalDate today = LocalDate.now();
//    LocalDateTime startOfDay = today.atStartOfDay();
//    LocalDateTime endOfDay = today.atTime(23, 59, 59);
//    LocalDateTime checkInTime = LocalDateTime.of(today, LocalTime.of(8, 30));
//
//    AttendanceEntity existingAttendance = new AttendanceEntity();
//    existingAttendance.setUser(testUser);
//    existingAttendance.setCheckIn(checkInTime);
//
//    when(userService.getCurrentUser()).thenReturn(testUser);
//    when(attendanceRepository.findByUser_IdAndCreatedBetween(1L, startOfDay, endOfDay))
//            .thenReturn(List.of(existingAttendance));
//    when(attendanceRepository.save(any(AttendanceEntity.class)))
//            .thenAnswer(invocation -> invocation.getArgument(0));
//
//    // Act
//    AttendanceEntity result = attendanceService.create(null);
//
//    // Assert
//    assertNotNull(result);
//    assertEquals(testUser, result.getUser());
//    assertEquals(checkInTime, result.getCheckIn());
//    assertNotNull(result.getCheckOut());
//    assertEquals(AttendanceEntity.Status.PRESENT, result.getStatus());
//    assertEquals(60,  result.getBreakTime());
////    assertTrue(result.getOverTime() > 0); // this test depend on current time so take care when use
//
//    verify(attendanceRepository).findByUser_IdAndCreatedBetween(1L, startOfDay, endOfDay);
//    verify(attendanceRepository).save(any(AttendanceEntity.class));
//  }

  @Test
  void testGetPaging() {
    AttendanceEntity entity = new AttendanceEntity();
    entity.setId(1L);
    List<AttendanceEntity> products = new ArrayList<>();
    products.add(entity);
    Page<AttendanceEntity> productPage = new PageImpl<>(products);
    when(attendanceRepository.findAll(any(PageRequest.class))).thenReturn(productPage);

    Map<String, String> requestParams = Map.of("page", "0", "size", "10");

    // Call the service method
    Page<AttendanceEntity> result = attendanceService.getPaging(requestParams);

    // Assert the result
    assertEquals(1, result.getTotalElements());
    assertEquals(0, result.getNumber());
    assertEquals(1, result.getContent().size());

    // Verify that the ProductRepository findAll method was called with the appropriate PageRequest
    verify(attendanceRepository, Mockito.times(1)).findAll(any(PageRequest.class));
  }
}