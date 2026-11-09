package com.kurum.personel.repository;

import com.kurum.personel.entity.Personel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;

public interface PersonelRepository extends JpaRepository<Personel, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    @EntityGraph(attributePaths = "arabalar")
    List<Personel> findAllWithArabalarBy();
}
