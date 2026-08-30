package com.shopkeeper.app.repository;

import com.shopkeeper.app.entity.KhataEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface KhataEntryRepository extends JpaRepository<KhataEntry, Long> {
    List<KhataEntry> findByShopkeeperId(Long shopkeeperId);
    List<KhataEntry> findByCustomerId(Long customerId);
    List<KhataEntry> findByStatusNotAndDueDateLessThanEqualAndReminderSentFalse(
            KhataEntry.KhataStatus status, LocalDate date);
    List<KhataEntry> findByShopkeeperIdAndStatusNot(Long shopkeeperId, KhataEntry.KhataStatus status);
}
