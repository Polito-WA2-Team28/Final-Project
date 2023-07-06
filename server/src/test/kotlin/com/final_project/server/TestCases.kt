package com.final_project.server

import com.final_project.security.dto.UserCredentialsDTO
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TestCases : ApplicationTests(){

    @Test /** POST /api/auth/login Success */
    fun `Successful Customer Login`() {

        /* crafting the request  */
        val credentials = UserCredentialsDTO("customer-test-1", "test")
        val body = HttpEntity(credentials)
        //* login *//*
        val response = utilityFunctions.restTemplate.postForEntity<String>(
            "/api/auth/login",
            body,
            HttpMethod.POST
        )

        println(response.statusCode)

        /* Assertions */
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
    }



    fun `Unauthorized Customer Login`() {

        /* crafting the request  */
        val credentials = UserCredentialsDTO("customer-test-10", "test")
        val body = HttpEntity(credentials)
        //* login *//*
        val response = utilityFunctions.restTemplate.postForEntity<String>(
            "/api/auth/login",
            body,
            HttpMethod.POST
        )


        /* Assertions */
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response.statusCode)
    }
}