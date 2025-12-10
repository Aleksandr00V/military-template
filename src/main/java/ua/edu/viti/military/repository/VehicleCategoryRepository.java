package ua.edu.viti.military.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.edu.viti.military.entity.VehicleCategory;

import java.util.Optional;

@Repository
public interface VehicleCategoryRepository extends JpaRepository<VehicleCategory, Long> {

    
    Optional<VehicleCategory> findByName(String name);

    // чи існує імя
    boolean existsByName(String name);

    // vehicle category пошук по полю код
    Optional<VehicleCategory> findByCode(String code);

    // перевірка чи він існує
    boolean existsByCode(String code);
}
