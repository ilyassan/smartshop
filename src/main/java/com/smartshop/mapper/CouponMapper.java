package com.smartshop.mapper;

import com.smartshop.dto.CouponDTO;
import com.smartshop.entity.Coupon;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CouponMapper {

    CouponDTO toDTO(Coupon coupon);

    Coupon toEntity(CouponDTO couponDTO);

    List<CouponDTO> toDTOList(List<Coupon> coupons);

    void updateEntityFromDTO(CouponDTO couponDTO, @MappingTarget Coupon coupon);
}
