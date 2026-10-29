package com.example.accounts_1.service.impl;

import com.example.accounts_1.dto.AccountsDto;
import com.example.accounts_1.dto.CardsDto;
import com.example.accounts_1.dto.CustomerDetailsDto;
import com.example.accounts_1.dto.LoansDto;
import com.example.accounts_1.entity.Accounts;
import com.example.accounts_1.entity.Customer;
import com.example.accounts_1.exception.ResourceNotFoundException;
import com.example.accounts_1.mapper.AccountsMapper;
import com.example.accounts_1.mapper.CustomerMapper;
import com.example.accounts_1.repository.AccountsRepository;
import com.example.accounts_1.repository.CustomerRepository;
import com.example.accounts_1.service.ICustomersService;
import com.example.accounts_1.service.client.CardsFeignClient;
import com.example.accounts_1.service.client.LoansFeignClient;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor  //lombok will generate all argsConstr and autowire will happen automatically
public class CustomersServiceImpl implements ICustomersService {

    private static final Logger logger = LoggerFactory.getLogger(CustomersServiceImpl.class);
	private AccountsRepository accountsRepository;
	private CustomerRepository customerRepository;
	private CardsFeignClient cardsFeignClient;
	private LoansFeignClient loansFeignClient;

	/**
	 * @param mobileNumber  - Input Mobile Number
	 * @param correlationId - Correlation ID value generated at Edge server
	 * @return Customer Details based on a given mobileNumber
	 */
	@Override
	public CustomerDetailsDto fetchCustomerDetails(String mobileNumber, String correlationId) {

        logger.debug("Looking up customer by mobileNumber: {}", mobileNumber);
		CustomerDetailsDto customerDetailsDto = null;
		//try {
        Customer customer = customerRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber));

        Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString()));

        customerDetailsDto = CustomerMapper.mapToCustomerDetailsDto(customer, new CustomerDetailsDto());
        customerDetailsDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));


        //LOANS
        logger.debug("Calling loans service for mobileNumber: {}", mobileNumber);
        //Using feignClient
        ResponseEntity<LoansDto> loansDtoResponseEntity = loansFeignClient.fetchLoanDetails(correlationId, mobileNumber);
        if(null != loansDtoResponseEntity) {
            customerDetailsDto.setLoansDto(loansDtoResponseEntity.getBody());
        } else {
            logger.warn("Loans service call failed or fell back for mobileNumber: {}", mobileNumber);
        }


        //CARDS
        logger.debug("Calling cards service for mobileNumber: {}", mobileNumber);
        ResponseEntity<CardsDto> cardsDtoResponseEntity = cardsFeignClient.fetchCardDetails(correlationId, mobileNumber);
        if(null != cardsDtoResponseEntity) {
            customerDetailsDto.setCardsDto(cardsDtoResponseEntity.getBody());
        } else {
            logger.warn("Cards service call failed or fell back for mobileNumber: {}", mobileNumber);
        }
		/*} catch (Exception e) {
			e.printStackTrace();
		}*/
		return customerDetailsDto;

	}
}
