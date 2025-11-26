package com.smartshop.mapper;

import com.smartshop.dto.ProductDTO;
import com.smartshop.entity.Product;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

    ProductDTO toDTO(Product product);

    Product toEntity(ProductDTO productDTO);

    List<ProductDTO> toDTOList(List<Product> products);

    void updateEntityFromDTO(ProductDTO productDTO, @MappingTarget Product product);
}
