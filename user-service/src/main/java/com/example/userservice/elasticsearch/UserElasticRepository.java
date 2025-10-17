package com.example.userservice.elasticsearch;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserElasticRepository extends ElasticsearchRepository<UserDocument, Long> {
    Page<UserDocument> findByUsernameContainingOrEmailContaining(String username, String email, Pageable pageable);
}