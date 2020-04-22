package com.saladglobal.cobb.ms.user.sevice;

import com.saladglobal.cobb.ms.user.dto.mapper.UserMapper;
import com.saladglobal.cobb.ms.user.dto.model.UserDto;
import com.saladglobal.cobb.ms.user.exception.EntityType;
import com.saladglobal.cobb.ms.user.exception.ExceptionType;
import com.saladglobal.cobb.ms.user.exception.ServiceException;
import com.saladglobal.cobb.ms.user.model.User;
import com.saladglobal.cobb.ms.user.repository.user.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import static com.saladglobal.cobb.ms.user.exception.EntityType.USER;
import static com.saladglobal.cobb.ms.user.exception.ExceptionType.DUPLICATE_ENTITY;
import static com.saladglobal.cobb.ms.user.exception.ExceptionType.ENTITY_NOT_FOUND;

@Component
public class UserServiceImpl implements UserService {

//    @Autowired
//    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public UserDto signup(UserDto userDto) {
        User user = userRepository.findByEmail(userDto.getEmail());
        if (user == null) {
            user = new User()
                    .setEmail(userDto.getEmail())
//                    .setPassword(bCryptPasswordEncoder.encode(userDto.getPassword()))
                    .setPassword(userDto.getPassword())
                    .setFirstName(userDto.getFirstName())
                    .setLastName(userDto.getLastName())
                    .setMobileNumber(userDto.getMobileNumber());
            return UserMapper.toUserDto(userRepository.save(user));
        }
        throw exception(USER, DUPLICATE_ENTITY, userDto.getEmail());
    }

    @Override
    public UserDto findUserByEmail(String email) {
        Optional<User> user = Optional.ofNullable(userRepository.findByEmail(email));
        if (user.isPresent()) {
            return modelMapper.map(user.get(), UserDto.class);
        }
        throw exception(USER, ENTITY_NOT_FOUND, email);
    }

    @Override
    public UserDto updateProfile(UserDto userDto) {
        return null;
    }

    @Override
    public UserDto changePassword(UserDto userDto, String newPassword) {
        return null;
    }

    private RuntimeException exception(EntityType entityType, ExceptionType exceptionType, String... args) {
        return ServiceException.throwException(entityType, exceptionType, args);
    }
}
