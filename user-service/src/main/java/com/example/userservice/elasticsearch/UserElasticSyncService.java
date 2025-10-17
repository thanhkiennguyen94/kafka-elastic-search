//package com.example.userservice.elasticsearch;
//
//import com.example.userservice.model.User;
//import com.example.userservice.repository.UserRepository;
//import jakarta.annotation.PostConstruct;
//import lombok.RequiredArgsConstructor;
//import org.modelmapper.ModelMapper;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class UserElasticSyncService {
//
//    private final UserRepository userRepository;
//    private final UserElasticRepository userElasticRepository;
//    private final ModelMapper modelMapper;
//
//    @PostConstruct
//    public void syncUsersToElasticsearch() {
//        List<User> users = userRepository.findAll();
//        users.forEach(user -> {
//            UserDocument doc = modelMapper.map(user, UserDocument.class);
//            userElasticRepository.save(doc);
//        });
//        System.out.println("✅ Synced " + users.size() + " users to Elasticsearch");
//    }
//}
