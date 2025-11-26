package com.smartshop.mapper;

import com.smartshop.dto.UserDTO;
import com.smartshop.entity.User;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {

    UserDTO toDTO(User user);

    User toEntity(UserDTO userDTO);

    List<UserDTO> toDTOList(List<User> users);

    void updateEntityFromDTO(UserDTO userDTO, @MappingTarget User user);
}
