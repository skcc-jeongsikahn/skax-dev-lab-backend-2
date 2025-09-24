package com.carcenter.domain.user.service;

import com.carcenter.domain.user.dto.UserCreateRequest;
import com.carcenter.domain.user.dto.UserResponse;
import com.carcenter.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        // TODO: 사용자 생성 로직 구현
        return null;
    }

    public UserResponse getUser(Long id) {
        // TODO: 사용자 조회 로직 구현
        return null;
    }

    public List<UserResponse> getUsers() {
        // TODO: 사용자 목록 조회 로직 구현
        return List.of();
    }
}
