package com.example.userservice.elasticsearch;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "user_index") // tên index trong Elasticsearch
public class UserDocument {
    @Id
    private Long id;
    private String username;
    private String email;
}
