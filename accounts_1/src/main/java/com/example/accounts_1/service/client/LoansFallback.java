package com.example.accounts_1.service.client;

import org.springframework.cloud.client.circuitbreaker.httpservice.HttpServiceFallback;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.example.accounts_1.dto.LoansDto;

@Component
@HttpServiceFallback(value = LoansFallback.class, group = "loans-1")
public class LoansFallback implements LoansFeignClient {

	@Override
	public ResponseEntity<LoansDto> fetchLoanDetails(String mobileNumber) {
		return null;
	}

}
