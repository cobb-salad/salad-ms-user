package com.saladglobal.cobb.ms.user.dto.mapper;

import com.saladglobal.cobb.ms.user.dto.model.UserDto;
import com.saladglobal.cobb.ms.user.model.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public static UserDto toUserDto(User user) {
        return new UserDto()
                .setEmail(user.getEmail())
                .setFirstName(user.getFirstName())
                .setLastName(user.getLastName())
                .setMobileNumber(user.getMobileNumber());
    }
}
