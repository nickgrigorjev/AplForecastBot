package com.example.aplforecastbot.repository;

import com.example.aplforecastbot.entities.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User,Long> {

}
