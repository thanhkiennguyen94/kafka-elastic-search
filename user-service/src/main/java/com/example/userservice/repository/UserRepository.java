package com.example.userservice.repository;

import com.example.userservice.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    @Query(value = "SELECT u FROM User u " +
            "WHERE u.email LIKE %:search% " +
            "OR u.username LIKE %:search% "
    )
    Page<User> findAll(Pageable pageable, String search);

}
