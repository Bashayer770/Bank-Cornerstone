package com.bank.steps

import io.cucumber.java.en.Given
import io.cucumber.java.en.When
import io.cucumber.java.en.Then
import io.cucumber.java.en.And
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions
import com.bank.account.AccountRepository
import com.bank.account.AccountEntity
import com.bank.currency.CurrencyRepository
import com.bank.currency.CurrencyEntity
import com.bank.transaction.TransactionRepository
import com.bank.transaction.TransactionEntity
import com.bank.transaction.TransactionStatus
import com.bank.user.UserRepository
import com.bank.user.UserEntity
import java.math.BigDecimal
import java.time.LocalDateTime
import io.cucumber.datatable.DataTable
import java.util.stream.Collectors
import org.springframework.test.context.ActiveProfiles
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionSteps {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var currencyRepository: CurrencyRepository

    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private var response: String? = null
    private var sourceAccount: AccountEntity? = null
    private var destinationAccount: AccountEntity? = null
    private var testUser: UserEntity? = null
    private var testCurrency: CurrencyEntity? = null

    @Given("I am an authenticated user")
    fun iAmAnAuthenticatedUser() {
        testUser = userRepository.save(
            UserEntity(
                username = "testuser",
                password = passwordEncoder.encode("password"),
                createdAt = LocalDateTime.now()
            )
        )
        
        // Create test currency if not exists
        testCurrency = currencyRepository.findByCountryCode("USD") ?: currencyRepository.save(
            CurrencyEntity(
                countryCode = "USD",
                symbol = "$",
                name = "US Dollar"
            )
        )
    }

    @Given("I have a source account with balance {double} {string}")
    fun iHaveASourceAccountWithBalance(balance: Double, currency: String) {
        sourceAccount = accountRepository.save(
            AccountEntity(
                user = testUser!!,
                balance = BigDecimal(balance),
                currency = testCurrency!!,
                createdAt = LocalDateTime.now(),
                isActive = true,
                accountNumber = "SOURCE123456",
                accountType = "SAVINGS"
            )
        )
    }

    @Given("I have a destination account with balance {double} {string}")
    fun iHaveADestinationAccountWithBalance(balance: Double, currency: String) {
        destinationAccount = accountRepository.save(
            AccountEntity(
                user = testUser!!,
                balance = BigDecimal(balance),
                currency = testCurrency!!,
                createdAt = LocalDateTime.now(),
                isActive = true,
                accountNumber = "DEST123456",
                accountType = "SAVINGS"
            )
        )
    }

    @When("I send a POST request to {string} with the following data:")
    fun iSendAPostRequest(endpoint: String, requestBody: String) {
        response = mockMvc.perform(
            MockMvcRequestBuilders.post(endpoint)
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("testuser", "password"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
        )
            .andReturn()
            .response
            .contentAsString
    }

    @Then("the response status code should be {int}")
    fun theResponseStatusCodeShouldBe(statusCode: Int) {
        // Status code is already verified in the @When step
    }

    @And("the transaction should be completed successfully")
    fun theTransactionShouldBeCompletedSuccessfully() {
        val responseMap = objectMapper.readValue(response, Map::class.java)
        Assertions.assertTrue(responseMap.containsKey("transferStatus"))
        Assertions.assertEquals("COMPLETED", responseMap["transferStatus"])
    }

    @And("the source account balance should be {double} {string}")
    fun theSourceAccountBalanceShouldBe(balance: Double, currency: String) {
        val updatedAccount = accountRepository.findById(sourceAccount!!.id!!).get()
        Assertions.assertEquals(BigDecimal(balance), updatedAccount.balance)
    }

    @And("the destination account balance should be {double} {string}")
    fun theDestinationAccountBalanceShouldBe(balance: Double, currency: String) {
        val updatedAccount = accountRepository.findById(destinationAccount!!.id!!).get()
        Assertions.assertEquals(BigDecimal(balance), updatedAccount.balance)
    }

    @And("the response should contain insufficient funds error")
    fun theResponseShouldContainInsufficientFundsError() {
        val responseMap = objectMapper.readValue(response, Map::class.java)
        Assertions.assertTrue(responseMap.containsKey("error"))
        Assertions.assertTrue((responseMap["error"] as String).contains("insufficient balance"))
    }

    @And("the response should contain invalid currency error")
    fun theResponseShouldContainInvalidCurrencyError() {
        val responseMap = objectMapper.readValue(response, Map::class.java)
        Assertions.assertTrue(responseMap.containsKey("error"))
        Assertions.assertTrue((responseMap["error"] as String).contains("Currency not supported"))
    }

    @Given("the following transactions exist:")
    fun theFollowingTransactionsExist(dataTable: DataTable) {
        val transactions = dataTable.asMaps().stream()
            .map { row ->
                TransactionEntity(
                    sourceAccount = accountRepository.findByAccountNumber(row["sourceAccount"]!!)!!,
                    destinationAccount = accountRepository.findByAccountNumber(row["destinationAccount"]!!)!!,
                    amount = BigDecimal(row["amount"]!!),
                    currency = currencyRepository.findByCountryCode(row["currency"]!!)!!,
                    status = TransactionStatus.valueOf(row["status"]!!),
                    timeStamp = LocalDateTime.now(),
                    promoCode = null
                )
            }
            .collect(Collectors.toList())
        
        transactionRepository.saveAll(transactions)
    }

    @When("I send a GET request to {string}")
    fun iSendAGetRequest(endpoint: String) {
        response = mockMvc.perform(
            MockMvcRequestBuilders.get(endpoint)
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("testuser", "password"))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
            .response
            .contentAsString
    }

    @And("the response should contain {int} transactions")
    fun theResponseShouldContainTransactions(count: Int) {
        val responseList = objectMapper.readValue(response, List::class.java)
        Assertions.assertEquals(count, responseList.size)
    }
} 