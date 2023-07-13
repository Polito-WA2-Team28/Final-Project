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
import org.springframework.http.MediaType
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CustomerTests: ApplicationTests() {

    /*** --- Messages and attachments related tests --- ***/

    /* --- Customers --- */

    @Test
    fun `Customer wants to get all messages but does not exist in the database`() {

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
        utilityFunctions.createTestCustomer("Mario", "Rossi")
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
        val expert: Expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
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
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")

        val customer2: Customer = utilityFunctions.createTestCustomer("John", "Doe")
            ?: fail("Test failed because no customer was created in the database.")

        val product: Product = utilityFunctions.createTestProduct(customer)
        val expert: Expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
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

    @Test
    fun `Customer retrieves all the messages`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val expert: Expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val product: Product = utilityFunctions.createTestProduct(customer)
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)
        utilityFunctions.createMessage(ticket, customer, "Hello, I need help!")
        utilityFunctions.createMessage(ticket, customer, "Hi, how can I help you?")
        utilityFunctions.createMessage(ticket, customer, "Windows keeps freezing...")


        /* customer login */
        val accessToken: String = utilityFunctions.customerLogin()
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
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals(3, JSONObject(response.body).get("totalElements"))
    }



    @Test
    fun `Customer wants to send a message but does not exist in the database`() {

        /* customer login */
        val accessToken: String = utilityFunctions.customerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            contentType = MediaType.MULTIPART_FORM_DATA
        }


        /* sending the messages */
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("messageText", "Hello sir, I need help.")
            add("attachments", null)
        }
        val anyValueDoesntMatter: Int = 0
        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/$anyValueDoesntMatter/messages",
            HttpMethod.POST,
            HttpEntity(formData, headers),
            String::class.java
        )

        /* assertions */
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals("No customer profile found with this UUID.", JSONObject(response.body).get("error"))
    }

    @Test
    fun `Customer fails to send a message for a non-existing ticket`() {

        /* preparing database */
        utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")

        /* customer login */
        val accessToken: String = utilityFunctions.customerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            contentType = MediaType.MULTIPART_FORM_DATA
        }


        /* sending the messages */
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("messageText", "Hello sir, I need help.")
            add("attachments", null)
        }
        val nonExistingTicketId: Int = 0
        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/$nonExistingTicketId/messages",
            HttpMethod.POST,
            HttpEntity(formData, headers),
            String::class.java
        )

        /* assertions */
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals("Ticket not found.", JSONObject(response.body).get("error"))
    }

    @Test
    fun `Customer sending the message is unauthorized to access this set of tickets`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val product: Product = utilityFunctions.createTestProduct(customer)
        val expert: Expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)

        /* customer login */
        val accessToken: String = utilityFunctions.customerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            contentType = MediaType.MULTIPART_FORM_DATA
        }


        /* sending the messages */
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("messageText", "Hello sir, I need help.")
            add("attachments", null)
        }
        val response = utilityFunctions.restTemplate.exchange(
            "/api/experts/tickets/${ticket.getId()}/messages",
            HttpMethod.POST,
            HttpEntity(formData, headers),
            String::class.java
        )

        /* assertions */
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        Assertions.assertEquals("Forbidden", JSONObject(response.body).get("error"))
    }


    @Test
    fun `Customer sending a message is forbidden to access tickets he did not created`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")

        val customer2: Customer = utilityFunctions.createTestCustomer("John", "Doe")
            ?: fail("Test failed because no customer was created in the database.")

        val product: Product = utilityFunctions.createTestProduct(customer)
        val expert: Expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)

        /* customer login */
        val accessToken: String = utilityFunctions.customer2Login()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            contentType = MediaType.MULTIPART_FORM_DATA
        }

        /* sending the messages */
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("messageText", "Hello sir, I need help.")
            add("attachments", null)
        }
        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/${ticket.getId()}/messages",
            HttpMethod.POST,
            HttpEntity(formData, headers),
            String::class.java
        )

        /* assertions */
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
        Assertions.assertEquals("This ticket belongs to another customer.", JSONObject(response.body).get("error"))

    }

    @Test
    fun `Customer sending a message makes a request with wrong format`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("John", "Doe")
            ?: fail("Test failed because no customer was created in the database.")
        val product: Product = utilityFunctions.createTestProduct(customer)
        val expert: Expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)


        /* customer login */
        val accessToken: String = utilityFunctions.customerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            contentType = MediaType.MULTIPART_FORM_DATA
        }

        /* sending the messages */
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("wrongAttribute", "Hello sir, I need help.")
            add("attachments", null)
        }
        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/${ticket.getId()}/messages",
            HttpMethod.POST,
            HttpEntity(formData, headers),
            String::class.java
        )

        /* assertions */
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        Assertions.assertEquals("Bad Request", JSONObject(response.body).get("error"))

    }

    @Test
    fun `Customer creates a message for a ticket in an invalid state`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val expert: Expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val product: Product = utilityFunctions.createTestProduct(customer)
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, null, TicketState.CLOSED)

        /* customer login */
        val accessToken: String = utilityFunctions.customerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            contentType = MediaType.MULTIPART_FORM_DATA
        }


        /* sending the messages */
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("messageText", "Hello sir, I need help.")
            add("attachments", null)
        }
        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/${ticket.getId()}/messages",
            HttpMethod.POST,
            HttpEntity(formData, headers),
            String::class.java
        )

        /* assertions */
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.CONFLICT, response.statusCode)
        Assertions.assertEquals("Invalid ticket status for this operation.", JSONObject(response.body).get("error"))
    }

    @Test
    fun `Customer sends a message`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val expert: Expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val product: Product = utilityFunctions.createTestProduct(customer)
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)

        /* customer login */
        val accessToken: String = utilityFunctions.customerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            contentType = MediaType.MULTIPART_FORM_DATA
        }


        /* sending the messages */
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("messageText", "Hello sir, I need help.")
            add("attachments", null)
        }
        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/${ticket.getId()}/messages",
            HttpMethod.POST,
            HttpEntity(formData, headers),
            String::class.java
        )

        /* assertions */
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.CREATED, response.statusCode)
        Assertions.assertEquals("customer-test-1", JSONObject(response.body).get("sender"))
        Assertions.assertEquals("Hello sir, I need help.", JSONObject(response.body).get("messageText"))
    }

    @Test
    fun `Customer sends a message with an attachment`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val expert: Expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val product: Product = utilityFunctions.createTestProduct(customer)
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)

        /* customer login */
        val accessToken: String = utilityFunctions.customerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            contentType = MediaType.MULTIPART_FORM_DATA
        }

        /* sending the messages */
        val formData: MultiValueMap<String, Any> = LinkedMultiValueMap<String, Any>().apply {
            add("messageText", "Hello sir, I need help.")
            add("attachments", utilityFunctions.createTestAttachment().resource)
        }

        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/${ticket.getId()}/messages",
            HttpMethod.POST,
            HttpEntity(formData, headers),
            String::class.java
        )

        /* assertions */
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.CREATED, response.statusCode)
        Assertions.assertEquals("customer-test-1", JSONObject(response.body).get("sender"))
        Assertions.assertEquals("Hello sir, I need help.", JSONObject(response.body).get("messageText"))
    }

    @Test
    fun `Customer wants to retrieve an attachment but does not exist in the database`() {

        /* customer login */
        val accessToken: String = utilityFunctions.customerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }


        /* sending the messages */
        val anyValueDoesntMatter: Int = 0
        val anyNameDoesntMatter: String = "filename"
        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/$anyValueDoesntMatter/attachments/$anyNameDoesntMatter",
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
    fun `Customer fails to retrieve attachment for a non-existing ticket`() {

        /* preparing database */
        utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")

        /* customer login */
        val accessToken: String = utilityFunctions.customerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }


        /* sending the messages */
        val nonExistingTicketId: Int = 0
        val anyNameDoesntMatter: String = "filename"
        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/$nonExistingTicketId/attachments/$anyNameDoesntMatter",
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
    fun `Customer retrieving an attachment is unauthorized to access this set of tickets`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("John", "Doe")
            ?: fail("Test failed because no customer was created in the database.")
        val product: Product = utilityFunctions.createTestProduct(customer)
        val expert: Expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)
        utilityFunctions.createMessage(ticket, customer, "Hello, I need help!")
        utilityFunctions.createMessage(ticket, customer, "Hi, how can I help you?")
        val uniqueFileNameList: List<String> = utilityFunctions.createMessageWithAttachment(ticket, customer, "Windows keeps freezing...")


        /* customer login */
        val accessToken: String = utilityFunctions.customerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* sending the messages */
        val response = utilityFunctions.restTemplate.exchange(
            "/api/experts/tickets/${ticket.getId()}/attachments/${uniqueFileNameList.first()}",
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
    fun `Customer retrieving an attachment is forbidden to access tickets he did not created`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val customer2: Customer = utilityFunctions.createTestCustomer("John", "Doe")
            ?: fail("Test failed because no customer was created in the database.")
        val product: Product = utilityFunctions.createTestProduct(customer)
        val expert: Expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)
        utilityFunctions.createMessage(ticket, customer, "Hello, I need help!")
        utilityFunctions.createMessage(ticket, customer, "Hi, how can I help you?")
        val uniqueFileNameList: List<String> = utilityFunctions.createMessageWithAttachment(ticket, customer, "Windows keeps freezing...")

        /* customer login */
        val accessToken: String = utilityFunctions.customer2Login()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* sending the messages */
        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/${ticket.getId()}/attachments/${uniqueFileNameList.first()}",
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
    fun `Customer retrieving an attachment makes a request with wrong format`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val product: Product = utilityFunctions.createTestProduct(customer)
        val expert: Expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)
        utilityFunctions.createMessage(ticket, customer, "Hello, I need help!")
        utilityFunctions.createMessage(ticket, customer, "Hi, how can I help you?")
        val uniqueFileNameList: List<String> = utilityFunctions.createMessageWithAttachment(ticket, customer, "Windows keeps freezing...")


        /* customer login */
        val accessToken: String = utilityFunctions.customerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* sending the messages */
        val wrongTicketId = "BadRequest"
        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/$wrongTicketId/attachments/${uniqueFileNameList.first()}",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )

        /* assertions */
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        Assertions.assertEquals("Bad Request", JSONObject(response.body).get("error"))
    }

    @Test
    fun `Customer fails to retrieve attachment for a non-existing attachment`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val product: Product = utilityFunctions.createTestProduct(customer)
        val expert: Expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)

        /* customer login */
        val accessToken: String = utilityFunctions.customerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* sending the messages */
        val wrongFileName = "BadRequest"
        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/${ticket.getId()}/attachments/$wrongFileName",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )

        /* assertions */
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals("This attachment does not exist.", JSONObject(response.body).get("error"))
    }

    @Test
    fun `Customer retrieves the attachment`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val product: Product = utilityFunctions.createTestProduct(customer)
        val expert: Expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)
        utilityFunctions.createMessage(ticket, customer, "Hello, I need help!")
        utilityFunctions.createMessage(ticket, customer, "Hi, how can I help you?")
        val uniqueFileNameList: List<String> = utilityFunctions.createMessageWithAttachment(ticket, customer, "Windows keeps freezing...")


        /* customer login */
        val accessToken: String = utilityFunctions.customerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* sending the messages */
        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/${ticket.getId()}/attachments/${uniqueFileNameList.first()}",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )

        /* assertions */
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("application/octet-stream", response.headers.contentType.toString())
        Assertions.assertEquals("attachment; filename=\"${uniqueFileNameList.first()}\"", response.headers.getFirst("Content-Disposition"))
        response.body
            ?: fail("Response body is null")

    }

}