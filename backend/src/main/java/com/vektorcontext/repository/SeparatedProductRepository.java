package com.vektorcontext.repository;

import com.vektorcontext.models.SeparatedProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeparatedProductRepository extends JpaRepository<SeparatedProduct, Integer> {

    Optional<SeparatedProduct> findTopByProductCodeAndStoreCodeOrderByDateDesc(
            Integer productCode, Integer storeCode);
}