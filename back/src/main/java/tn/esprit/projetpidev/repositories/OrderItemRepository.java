package tn.esprit.projetpidev.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.projetpidev.domain.OrderItem;
import tn.esprit.projetpidev.domain.enums.OrderStatus;
import tn.esprit.projetpidev.domain.enums.TransactionType;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderId(Long orderId);
    List<OrderItem> findByEquipmentId(Long equipmentId);
    List<OrderItem> findByTransactionType(TransactionType transactionType);

    @Query("SELECT COUNT(oi) > 0 FROM OrderItem oi WHERE oi.equipment.id = :equipmentId AND oi.order.status <> :status")
    boolean existsByEquipmentIdAndOrderStatusNot(@Param("equipmentId") Long equipmentId,
                                                  @Param("status") OrderStatus status);
}

