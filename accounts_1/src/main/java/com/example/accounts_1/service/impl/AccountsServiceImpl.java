package com.example.accounts_1.service.impl;

import java.util.Optional;
import java.util.Random;

import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.accounts_1.constants.AccountsConstants;
import com.example.accounts_1.dto.AccountsDto;
import com.example.accounts_1.dto.CustomerDto;
import com.example.accounts_1.entity.Accounts;
import com.example.accounts_1.entity.Customer;
import com.example.accounts_1.exception.CustomerAlreadyExistsException;
import com.example.accounts_1.exception.ResourceNotFoundException;
import com.example.accounts_1.mapper.AccountsMapper;
import com.example.accounts_1.mapper.CustomerMapper;
import com.example.accounts_1.repository.AccountsRepository;
import com.example.accounts_1.repository.CustomerRepository;
import com.example.accounts_1.service.IAccountsService;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class AccountsServiceImpl implements IAccountsService {

	private static final Logger logger = LoggerFactory.getLogger(AccountsServiceImpl.class);

	private AccountsRepository accountsRepository;
	private CustomerRepository customerRepository;
    private MeterRegistry meterRegistry;
  /*  is the core Micrometer interface in Spring Boot used to create, store, and publish custom application
        metrics (like counters, timers, and gauges) to monitoring tools like Prometheus, Grafana, or OpenTelemetry.*/


	@Override
	public void createAccount(CustomerDto customerDto) {
		Customer customer = CustomerMapper.mapToCustomer(customerDto, new Customer());

        Optional<Customer> optionalCustomer = customerRepository.findByMobileNumber(customerDto.getMobileNumber());

		if (optionalCustomer.isPresent()) {
            logger.warn("Customer already registered with mobileNumber: {}", customerDto.getMobileNumber());
			throw new CustomerAlreadyExistsException("Customer already registered with given mobileNumber " + customerDto.getMobileNumber());
		}

        Customer savedCustomer = customerRepository.save(customer);
		Accounts newAccount = accountsRepository.save(createNewAccount(savedCustomer));

        logger.debug("Created new account with accountNumber: {} for customerId: {}",  newAccount.getAccountNumber(), savedCustomer.getCustomerId());

        //will go to grafana
        meterRegistry.counter("accounts.operations", "operation", "created").increment();

        System.out.println();
		//sendCommunication(savedAccount, savedCustomer);
	}

	private Accounts createNewAccount(Customer customer) {
		Accounts newAccount = new Accounts();
		newAccount.setCustomerId(customer.getCustomerId());
		long randomAccNumber = 1000000000L + new Random().nextInt(900000000);

		newAccount.setAccountNumber(randomAccNumber);
		newAccount.setAccountType(AccountsConstants.SAVINGS);
		newAccount.setBranchAddress(AccountsConstants.ADDRESS);
		return newAccount;
	}

	@Override
	public CustomerDto fetchAccount(String mobileNumber) {
		Customer customer = customerRepository.findByMobileNumber(mobileNumber)
				.orElseThrow(() -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber));
		
		Accounts accounts = accountsRepository.findByCustomerId(customer.getCustomerId())
				.orElseThrow(
				() -> new ResourceNotFoundException("Account", "customerId", customer.getCustomerId().toString()));
		
		CustomerDto customerDto = CustomerMapper.mapToCustomerDto(customer, new CustomerDto());
		customerDto.setAccountsDto(AccountsMapper.mapToAccountsDto(accounts, new AccountsDto()));
		return customerDto;
	}

	/**
	 * @param customerDto - CustomerDto Object
	 * @return boolean indicating if the update of Account details is successful or not
	 */
	@Override
	public boolean updateAccount(CustomerDto customerDto) {
		boolean isUpdated = false;
		AccountsDto accountsDto = customerDto.getAccountsDto();
		if (accountsDto != null) {
			Accounts accounts = accountsRepository.findById(accountsDto.getAccountNumber())
					.orElseThrow(() -> new ResourceNotFoundException("Account", "AccountNumber",
							accountsDto.getAccountNumber().toString()));
			
			AccountsMapper.mapToAccounts(accountsDto, accounts);
			accounts = accountsRepository.save(accounts);

			Long customerId = accounts.getCustomerId();
			Customer customer = customerRepository.findById(customerId)
					.orElseThrow(() -> new ResourceNotFoundException("Customer", "CustomerID", customerId.toString()));
			
			CustomerMapper.mapToCustomer(customerDto, customer);
			customerRepository.save(customer);
			isUpdated = true;
		}

        meterRegistry.counter("accounts.operations", "operation", "updated").increment();

		return isUpdated;
	}

	/**
	 * @param mobileNumber - Input Mobile Number
	 * @return boolean indicating if the delete of Account details is successful or not
	 */
	@Override
	public boolean deleteAccount(String mobileNumber) {
		Customer customer = customerRepository.findByMobileNumber(mobileNumber)
				.orElseThrow(() -> new ResourceNotFoundException("Customer", "mobileNumber", mobileNumber));
		
		accountsRepository.deleteByCustomerId(customer.getCustomerId());
		customerRepository.deleteById(customer.getCustomerId());
        logger.debug("Deleted account and customer records for customerId: {}", customer.getCustomerId());

        meterRegistry.counter("accounts.operations", "operation", "deleted").increment();

        return true;
	}

	/**
	 * @param accountNumber - Long
	 * @return boolean indicating if the update of communication status is successful or not
	 */
	@Override
	public boolean updateCommunicationStatus(Long accountNumber) {
		boolean isUpdated = false;
		
		if (accountNumber != null) {
			Accounts accounts = accountsRepository.findById(accountNumber).orElseThrow(
					() -> new ResourceNotFoundException("Account", "AccountNumber", accountNumber.toString()));
			
			accounts.setCommunicationSw(true);
			accountsRepository.save(accounts);
			isUpdated = true;
		}
		return isUpdated;
	}

}
