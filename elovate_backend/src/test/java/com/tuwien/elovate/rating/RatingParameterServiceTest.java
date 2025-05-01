package com.tuwien.elovate.rating;

import com.tuwien.elovate.dtos.rating.Glicko2RatingParametersDto;
import com.tuwien.elovate.entities.rating.Glicko2RatingParameters;
import com.tuwien.elovate.services.mapper.RatingParametersMapper;
import com.tuwien.elovate.services.rating.RatingParameterService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class RatingParameterServiceTest {

    @Autowired
    private RatingParameterService ratingParameterService;

    @Autowired
    private RatingParametersMapper ratingParametersMapper;

    @Test
    void saveShouldWorkWithValidRatingParameters() {
        Glicko2RatingParameters parameters = Glicko2RatingParameters.createNewRatingParameters();
        Glicko2RatingParametersDto parametersDto = ratingParametersMapper.mapEntityToDto(parameters);
        Glicko2RatingParametersDto savedValue = ratingParameterService.save(parametersDto);

        // set id to be able to compare
        savedValue.setId(null);

        assertEquals(parametersDto, savedValue, "Saved value should be equal to requested value");
    }

}
