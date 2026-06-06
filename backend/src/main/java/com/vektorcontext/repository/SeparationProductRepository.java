package com.vektorcontext.repository;

import com.vektorcontext.models.SeparationProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeparationProductRepository extends JpaRepository<SeparationProduct, Long> {
}