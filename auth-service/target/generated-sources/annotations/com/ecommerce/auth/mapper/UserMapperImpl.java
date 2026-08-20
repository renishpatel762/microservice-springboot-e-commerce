package com.ecommerce.auth.mapper;

import com.ecommerce.auth.dto.SignupRequest;
import com.ecommerce.auth.dto.UserResponse;
import com.ecommerce.auth.entity.User;
import com.ecommerce.auth.enums.Role;
import java.time.OffsetDateTime;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-08-18T22:51:08+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.100.v20260624-0231, environment: Java 21.0.11 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl implements UserMapper {

    @Override
    public User toEntity(SignupRequest signupRequest) {
        if ( signupRequest == null ) {
            return null;
        }

        User.UserBuilder user = User.builder();

        user.email( signupRequest.email() );
        user.fullName( signupRequest.fullName() );
        user.role( signupRequest.role() );

        return user.build();
    }

    @Override
    public UserResponse toUserResponse(User user) {
        if ( user == null ) {
            return null;
        }

        Long id = null;
        String email = null;
        String fullName = null;
        Role role = null;
        OffsetDateTime createdAt = null;

        id = user.getId();
        email = user.getEmail();
        fullName = user.getFullName();
        role = user.getRole();
        createdAt = user.getCreatedAt();

        UserResponse userResponse = new UserResponse( id, email, fullName, role, createdAt );

        return userResponse;
    }
}
