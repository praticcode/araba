package com.kurum.personel.repository;

import com.kurum.personel.entity.GarajKaydi;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GarajKaydiRepository extends JpaRepository<GarajKaydi, Long> {

    List<GarajKaydi> findAllByOrderByGirisTarihiDescGirisSaatiDesc();

    boolean existsByAraba_IdAndCikisTarihiIsNull(Long arabaId);

    boolean existsByAraba_IdAndCikisTarihiIsNullAndIdNot(Long arabaId, Long id);

    boolean existsByAraba_Id(Long arabaId);

    boolean existsByPersonel_Id(Long personelId);
}
