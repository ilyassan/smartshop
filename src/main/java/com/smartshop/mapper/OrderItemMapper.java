package com.smartshop.mapper;

import com.smartshop.dto.OrderItemDTO;
import com.smartshop.entity.OrderItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderItemMapper {

    OrderItemDTO toDTO(OrderItem orderItem);

    OrderItem toEntity(OrderItemDTO orderItemDTO);

    List<OrderItemDTO> toDTOList(List<OrderItem> orderItems);

    void updateEntityFromDTO(OrderItemDTO orderItemDTO, @MappingTarget OrderItem orderItem);
}
