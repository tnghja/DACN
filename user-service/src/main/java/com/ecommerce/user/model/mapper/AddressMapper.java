package com.ecommerce.user.model.mapper;

import com.ecommerce.user.model.entity.Address;
import com.ecommerce.user.model.request.AddressCreateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public abstract class AddressMapper {



    public abstract Address toEntity(AddressCreateRequest request);
}
