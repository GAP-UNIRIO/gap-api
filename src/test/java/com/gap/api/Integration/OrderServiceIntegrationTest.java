package com.gap.api.Integration;

import com.gap.api.Model.DTO.AnnexRequest;
import com.gap.api.Model.DTO.OrderRequest;
import com.gap.api.Model.Entity.Course;
import com.gap.api.Model.Entity.Order;
import com.gap.api.Model.Entity.User;
import com.gap.api.Repository.CourseRepository;
import com.gap.api.Repository.OrderRepository;
import com.gap.api.Repository.UserRepository;
import com.gap.api.Service.Interface.IOrderService;
import com.gap.api.Service.Interface.IStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceIntegrationTest {

    @Autowired
    private IOrderService orderService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private OrderRepository orderRepository;

    @MockitoBean
    private IStorageService storageService;

    @Test
    void create_workflowFailureRollsBackOrderAndAnnexesAndCleansFinalObjects() {
        String suffix = UUID.randomUUID().toString();
        Course course = new Course();
        course.setName("Order create rollback " + suffix);
        course = courseRepository.saveAndFlush(course);

        User user = new User();
        user.setName("Order rollback test");
        user.setEmail("order-rollback-" + suffix + "@example.test");
        user.setCourse(course);
        user = userRepository.saveAndFlush(user);

        String temporaryKey = "temporary/" + suffix;
        String finalKey = "orders/1/" + suffix + "/proof.pdf";
        when(storageService.isTemporaryKeyIssued(temporaryKey)).thenReturn(true);
        when(storageService.moveToOrder(eq(temporaryKey), anyLong(), eq("proof.pdf"))).thenReturn(finalKey);

        OrderRequest request = new OrderRequest(user.getId(), Order.OrderType.INCLUSAO_DISCIPLINA,
                Map.of("reason", "rollback integration test"),
                List.of(new AnnexRequest("proof.pdf", "application/pdf", temporaryKey)));

        assertThrows(RuntimeException.class, () -> orderService.create(request));

        assertEquals(0, orderRepository.count(), "order insert must roll back when no workflow template exists");
        verify(storageService).deleteObject(finalKey);

        userRepository.delete(user);
        courseRepository.delete(course);
    }
}

