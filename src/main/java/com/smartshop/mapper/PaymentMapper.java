package com.smartshop.mapper;

import com.smartshop.dto.PaymentDTO;
import com.smartshop.entity.Payment;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PaymentMapper {

    PaymentDTO toDTO(Payment payment);

    @Mapping(target = "createdAt", ignore = true)
    Payment toEntity(PaymentDTO paymentDTO);

    List<PaymentDTO> toDTOList(List<Payment> payments);

    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDTO(PaymentDTO paymentDTO, @MappingTarget Payment payment);
}
