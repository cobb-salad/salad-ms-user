package com.saladglobal.cobb.ms.user.sevice;

import com.saladglobal.cobb.ms.user.dto.model.UserDto;

public interface UserService {

    UserDto signup(UserDto userDto);

    UserDto findUserByEmail(String email);

    UserDto updateProfile(UserDto userDto);

    UserDto changePassword(UserDto userDto, String newPassword);
}
