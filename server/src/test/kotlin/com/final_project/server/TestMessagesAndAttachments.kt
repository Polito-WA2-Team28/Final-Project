package com.final_project.server

import com.final_project.server.model.Customer
import com.final_project.server.model.Expert
import com.final_project.server.model.Product
import com.final_project.ticketing.model.Ticket
import com.final_project.ticketing.util.TicketState
import org.json.JSONObject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.runner.RunWith
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.util.MultiValueMap

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TestMessagesAndAttachments: ApplicationTests() {

    /*** --- Messages and attachments related tests --- ***/

    /* --- Customers --- */

    @Test
    fun `Customer does not exist in the database`() {

        /* customer login */
        val accessToken: String = utilityFunctions.customerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* retrieving the messages */
        val anyValueDoesntMatter: Int = 0
        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/${anyValueDoesntMatter}/messages",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )

        /* assertions */
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals("No customer profile found with this UUID.", JSONObject(response.body).get("error"))
    }

    @Test
    fun `Customer fails to retrieve messages for a non-existing ticket`() {

        /* preparing database */
        utilityFunctions.createTestCustomer("John", "Doe")
            ?: fail("Test failed because no customer was created in the database.")

        /* customer login */
        val accessToken: String = utilityFunctions.customerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* retrieving the messages */
        val nonExistingTicketId: Int = 0
        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/${nonExistingTicketId}/messages",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )

        /* assertions */
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals("Ticket not found.", JSONObject(response.body).get("error"))
    }

    @Test
    fun `Customer is unauthorized to access this set of tickets`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("John", "Doe")
            ?: fail("Test failed because no customer was created in the database.")
        val product: Product = utilityFunctions.createTestProduct(customer)
        val expert: Expert = utilityFunctions.createTestExpert()
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)

        /* customer login */
        val accessToken: String = utilityFunctions.customerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* retrieving the messages */
        val response = utilityFunctions.restTemplate.exchange(
            "/api/experts/tickets/${ticket.getId()}/messages",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )

        /* assertions */
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        Assertions.assertEquals("Forbidden", JSONObject(response.body).get("error"))

    }


    @Test
    fun `Customer is forbidden to access tickets he did not created`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("John", "Doe")
            ?: fail("Test failed because no customer was created in the database.")

        val customer2: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")

        val product: Product = utilityFunctions.createTestProduct(customer)
        val expert: Expert = utilityFunctions.createTestExpert()
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)

        /* customer login */
        val accessToken: String = utilityFunctions.customer2Login()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* retrieving the messages */
        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/${ticket.getId()}/messages",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )

        /* assertions */
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        Assertions.assertEquals("This ticket belongs to another customer.", JSONObject(response.body).get("error"))

    }

    @Test
    fun `Customer makes a request with wrong format`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("John", "Doe")
            ?: fail("Test failed because no customer was created in the database.")


        /* customer login */
        val accessToken: String = utilityFunctions.customerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* retrieving the messages */
        val wrongTicketId = "BadRequest"
        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/${wrongTicketId}/messages",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )

        /* assertions */
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        Assertions.assertEquals("Bad Request", JSONObject(response.body).get("error"))

    }
}