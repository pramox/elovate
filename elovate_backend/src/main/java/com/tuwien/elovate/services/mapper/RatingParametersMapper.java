package com.tuwien.elovate.services.mapper;

/*-
 * #%L
 * ELOvate
 * %%
 * Copyright (C) 2024 ELOvate GmbH.
 * %%
 * Copyright (C) 2024 ELOvate GmbH. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * #L%
 */

import com.tuwien.elovate.dtos.rating.Glicko2RatingParametersDto;
import com.tuwien.elovate.entities.rating.Glicko2RatingParameters;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Period;

@Mapper(componentModel = "spring")
public interface RatingParametersMapper {

    @Mapping(target = "ratingPeriod", source = "dto", qualifiedByName = "stringToPeriod")
    Glicko2RatingParameters mapDtoToEntity(Glicko2RatingParametersDto dto);

    @Named("stringToPeriod")
    default Period stringToPeriod(Glicko2RatingParametersDto dto) {
        return Period.ofDays(dto.getRatingPeriod());
    }

    @Mapping(target = "ratingPeriod", source = "ratingPeriod", qualifiedByName = "periodToString")
    Glicko2RatingParametersDto mapEntityToDto(Glicko2RatingParameters save);

    @Named("periodToString")
    default String periodToString(Period period) {
        return "" + period.getDays();
    }
}
