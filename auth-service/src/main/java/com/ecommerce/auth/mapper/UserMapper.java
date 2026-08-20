package com.ecommerce.auth.mapper;

import com.ecommerce.auth.dto.SignupRequest;
import com.ecommerce.auth.dto.UserResponse;
import com.ecommerce.auth.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "password", ignore = true) // Hashed manually in service
    User toEntity(SignupRequest signupRequest);

    UserResponse toUserResponse(User user);
}
