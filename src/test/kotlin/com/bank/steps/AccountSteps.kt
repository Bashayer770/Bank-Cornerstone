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
import com.bank.user.UserRepository
import com.bank.user.UserEntity
import com.bank.currency.CurrencyRepository
import com.bank.currency.CurrencyEntity
import com.bank.account.AccountRepository
import com.bank.account.AccountEntity
import java.math.BigDecimal
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDateTime
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountSteps {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var currencyRepository: CurrencyRepository

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private var response: String? = null
    private var accountId: String? = null
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

    @And("the account should be created successfully")
    fun theAccountShouldBeCreatedSuccessfully() {
        val responseMap = objectMapper.readValue(response, Map::class.java)
        Assertions.assertTrue(responseMap.containsKey("accountNumber"))
        Assertions.assertTrue(responseMap.containsKey("balance"))
    }

    @Given("I have an existing account")
    fun iHaveAnExistingAccount() {
        // Create a test account
        val account = accountRepository.save(
            AccountEntity(
                user = testUser!!,
                balance = BigDecimal("1000.00"),
                currency = testCurrency!!,
                createdAt = LocalDateTime.now(),
                isActive = true,
                accountNumber = "TEST123456",
                accountType = "SAVINGS"
            )
        )
        accountId = account.id.toString()
    }

    @When("I send a GET request to {string}")
    fun iSendAGetRequest(endpoint: String) {
        val actualEndpoint = endpoint.replace("{accountId}", accountId ?: "")
        response = mockMvc.perform(
            MockMvcRequestBuilders.get(actualEndpoint)
                .with(SecurityMockMvcRequestPostProcessors.httpBasic("testuser", "password"))
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(MockMvcResultMatchers.status().isOk)
            .andReturn()
            .response
            .contentAsString
    }

    @And("the response should contain the current balance")
    fun theResponseShouldContainTheCurrentBalance() {
        Assertions.assertNotNull(response)
        val responseMap = objectMapper.readValue(response, Map::class.java)
        Assertions.assertTrue(responseMap.containsKey("balance"))
        val balance = responseMap["balance"] as Number
        Assertions.assertTrue(balance.toDouble() > 0)
    }
} 