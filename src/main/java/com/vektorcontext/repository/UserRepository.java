package com.vektorcontext.repository;

import com.vektorcontext.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByCodeUser(String codeUser);

    boolean existsByCodeUser(String codeUser);
}