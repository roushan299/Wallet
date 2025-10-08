package com.example.wallet.service;

import java.util.List;
import com.example.wallet.entity.User;


public interface UserService {

    List<User> getAllUsers();

    User getUserById(Long id);

    User createUser(User user);

    User updateUser(Long id, User user);

    void deleteUser(Long id);

    User findByUsername(String username);
    
}


