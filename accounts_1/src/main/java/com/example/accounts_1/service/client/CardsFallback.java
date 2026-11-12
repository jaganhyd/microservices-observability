package com.example.accounts_1.service.client;

import org.springframework.cloud.client.circuitbreaker.httpservice.HttpServiceFallback;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.example.accounts_1.dto.CardsDto;

@Component
@HttpServiceFallback(value = CardsFallback.class, group = "cards-1")
public class CardsFallback implements CardsFeignClient {

	@Override
	public ResponseEntity<CardsDto> fetchCardDetails(String mobileNumber) {
		return null;
	}

}
