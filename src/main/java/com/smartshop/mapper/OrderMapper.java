package com.smartshop.mapper;

import com.smartshop.dto.OrderDTO;
import com.smartshop.entity.Order;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {CouponMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "coupon", ignore = true)
    OrderDTO toDTO(Order order);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Order toEntity(OrderDTO orderDTO);

    List<OrderDTO> toDTOList(List<Order> orders);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntityFromDTO(OrderDTO orderDTO, @MappingTarget Order order);
}
