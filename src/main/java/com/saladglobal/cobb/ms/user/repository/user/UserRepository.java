package com.saladglobal.cobb.ms.user.repository.user;

import com.saladglobal.cobb.ms.user.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
    User findByEmail(String email);
}
