package com.example.accounts_1.service.client;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.example.accounts_1.dto.CardsDto;

@Component
public class CardsFallback implements CardsFeignClient {

	@Override
	public ResponseEntity<CardsDto> fetchCardDetails(String mobileNumber) {
		return null;
	}

}
