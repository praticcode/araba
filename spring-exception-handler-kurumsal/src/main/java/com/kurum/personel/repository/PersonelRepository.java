package com.kurum.personel.repository;

import com.kurum.personel.entity.Personel;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public interface PersonelRepository extends JpaRepository<Personel, Long>, JpaSpecificationExecutor<Personel> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    @EntityGraph(attributePaths = "arabalar")
    List<Personel> findAllWithArabalarBy();

    @Override
    @EntityGraph(attributePaths = "arabalar")
    List<Personel> findAll(Specification<Personel> specification);
}
