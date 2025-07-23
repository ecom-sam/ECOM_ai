package com.ecom.ai.ecomassistant.model.dto.mapper;

import com.ecom.ai.ecomassistant.db.model.Dataset;
import com.ecom.ai.ecomassistant.model.dto.request.DatasetCreateRequest;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DatasetMapper {

    DatasetMapper INSTANCE = Mappers.getMapper(DatasetMapper.class);
    
    Dataset toEntity(DatasetCreateRequest request);
}