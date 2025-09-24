package com.carcenter.domain.user.repository;

import com.carcenter.domain.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserRepository {

    void save(User user);
    
    Optional<User> findById(Long id);
    
    List<User> findAll();
    
    void update(User user);
    
    void deleteById(Long id);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByUsername(String username);
}
