package com.restaurant.management.repository;

import com.restaurant.management.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
     Optional<Customer> findByPhone(String phone);

     @Query("SELECT c FROM Customer c WHERE " +
             "LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
             "OR c.phone LIKE CONCAT('%', :keyword, '%')")
     List<Customer> searchByKeyword(String keyword);

     boolean existsByPhone(String phone);


     @Query("""
    SELECT c.id, c.fullName, c.phone, c.address, c.createdAt,
           COUNT(o.id) AS completedOrders
    FROM Customer c
    LEFT JOIN Order o ON o.customer.id = c.id AND o.status = 'COMPLETED'
    GROUP BY c.id, c.fullName, c.phone, c.address, c.createdAt
    ORDER BY completedOrders DESC
""")
     List<Object[]> findAllWithCompletedOrders();


     // Lấy 1 khách (an toàn, không bị NonUniqueResultException)
     Optional<Customer> findFirstByPhone(String phone);

     // Các method khác của bạn giữ nguyên


     // Nếu cần tìm tất cả (dùng cho quản lý)
     List<Customer> findAllByPhone(String phone);

}
