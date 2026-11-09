package com.kurum.personel.repository;

import com.kurum.personel.entity.Personelarabasi;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArabaRepository extends JpaRepository<Personelarabasi, Long> {
}
