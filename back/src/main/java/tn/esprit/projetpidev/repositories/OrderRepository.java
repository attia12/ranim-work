package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.Order;
import tn.esprit.projetpidev.domain.enums.OrderStatus;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCamperIdOrderByCreatedAtDesc(Long camperId);
    List<Order> findByStatus(OrderStatus status);
    boolean existsByOrderNumber(String orderNumber);
}

