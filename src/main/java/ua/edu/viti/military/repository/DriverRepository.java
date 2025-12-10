package ua.edu.viti.military.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.edu.viti.military.entity.Driver;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    Optional<Driver> findByMilitaryId(String militaryId);
    boolean existsByMilitaryId(String militaryId);
    Optional<Driver> findByLicenseNumber(String licenseNumber);
    List<Driver> findByIsActive(Boolean isActive);
    List<Driver> findByLicenseExpiryDateBefore(LocalDate date);
}
