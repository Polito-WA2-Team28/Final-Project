package com.final_project.server

import com.final_project.security.dto.UserCredentialsDTO
import com.final_project.server.model.Customer
import com.final_project.server.model.Expert
import com.final_project.server.model.Product
import com.final_project.ticketing.model.Ticket
import com.final_project.ticketing.util.TicketState
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Ignore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.junit.runner.RunWith
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.*
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.util.*

@RunWith(SpringRunner::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["user.dir=/opt"]
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TestCases : ApplicationTests() {

    //AUTHENTICATION TESTS
    @Test
            /** POST /api/auth/login Success */
    fun `Successful Customer Login`() {

        /* crafting the request  */
        val credentials = UserCredentialsDTO("customer-test-1", "customer-test-1")
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


    @Test
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


    //CUSTOMERS


    @Test
            /** GET /api/customers/tickets */
    fun `Customer retrieve all the tickets`() {
        /* adding data to database */
        val expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")

        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")

        var product = utilityFunctions.createTestProduct(customer)
        val ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)

        /* customer login */
        val accessToken = utilityFunctions.customerLogin()

        /* crafting the request */
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* retrieving all the tickets */
        val response: ResponseEntity<String> = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )

        val resTicket = JSONObject(response.body).getJSONArray("content").getJSONObject(0)
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("IN_PROGRESS", resTicket.getString("ticketState"))
        Assertions.assertEquals(product.serialNumber.toString(), resTicket.getString("serialNumber"))
        Assertions.assertEquals(expert.id.toString(), resTicket.getString("expertId"))
        Assertions.assertEquals(customer.id.toString(), resTicket.getString("customerId"))
        Assertions.assertEquals(ticket.description, resTicket.getString("description"))
        Assertions.assertEquals(formatter.format(ticket.lastModified), resTicket.getString("lastModified"))
        Assertions.assertEquals(formatter.format(ticket.creationDate), resTicket.getString("creationDate"))
        Assertions.assertEquals(ticket.getId()!!.toLong(), resTicket.getLong("ticketId"))
    }


    @Test
            /** GET /api/customers/tickets */
    fun `Fail get all tickets without login`() {
        val url = "/api/customers/tickets"
        val response = utilityFunctions.restTemplate.getForEntity(url, String::class.java)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response?.statusCode)
    }


    @Test
            /** POST /api/customers/tickets POST*/
    fun `Successful creation of a new ticket`() {
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")

        var product = utilityFunctions.createTestProduct(customer)

        val jsonRequest = JSONObject()
        jsonRequest.put("description", "myDescription")
        jsonRequest.put("serialNumber", product.serialNumber)

        /* customer login */
        val accessToken = utilityFunctions.customerLogin()

        /* crafting the request */
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            add("content-type", "application/json")
        }

        /* retrieving all the tickets */
        val response: ResponseEntity<String> = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets",
            HttpMethod.POST,
            HttpEntity(jsonRequest.toString(), headers),
            String::class.java
        )


        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.CREATED, response.statusCode)
        val body = JSONObject(response.body)
        Assertions.assertEquals("OPEN", body.getString("ticketState"))
        Assertions.assertEquals("myDescription", body.getString("description"))
        Assertions.assertEquals(product.serialNumber.toString(), body.getString("serialNumber"))
        Assertions.assertEquals(customer.id.toString(), body.getString("customerId"))
        Assertions.assertEquals(0, body.optInt("expertId"))

    }


    @Test
            /** GET /api/customers/tickets/:ticketId */
    fun successGetASingleTicketsOfACustomer() {
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")

        utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        var product = utilityFunctions.createTestProduct(customer)
        val ticket = utilityFunctions.createTestTicket(customer, product, null, TicketState.OPEN)

        /* customer login */
        val accessToken = utilityFunctions.customerLogin()

        /* crafting the request */
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* retrieving all the tickets */
        val response: ResponseEntity<String> = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/${ticket.getId()}",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )

        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body
        val resTicket = JSONObject(body)
        Assertions.assertEquals("OPEN", resTicket.getString("ticketState"))
        Assertions.assertEquals(product.serialNumber.toString(), resTicket.getString("serialNumber"))
        Assertions.assertEquals(customer.id.toString(), resTicket.getString("customerId"))
        Assertions.assertEquals(ticket.description, resTicket.getString("description"))
        Assertions.assertEquals(formatter.format(ticket.lastModified), resTicket.getString("lastModified"))
        Assertions.assertEquals(formatter.format(ticket.creationDate), resTicket.getString("creationDate"))
        Assertions.assertEquals(ticket.getId()!!.toLong(), resTicket.getLong("ticketId"))
    }

    @Test
            /** GET /api/customers/tickets/:ticketId */
    fun failGetASingleTicketOfANonExistentCustomer() {
        val ticketId = (1000..2000).random()
        val url = "/api/customers/tickets/${ticketId}"
        val response = utilityFunctions.restTemplate
            .getForEntity(url, String::class.java)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response?.statusCode)
    }

    @Test
            /** GET /api/customers/tickets/:ticketId */
    fun failGetANonExistentTicketOfACustomer() {
        utilityFunctions.createTestCustomer("John", "Doe")
            ?: fail("Test failed because no customer was created in the database.")

        /* customer login */
        val accessToken = utilityFunctions.customerLogin()

        /* crafting the request */
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        val ticketId = (1000..2000).random()
        /* retrieving all the tickets */
        val response: ResponseEntity<String> = utilityFunctions.restTemplate.exchange(
            "http://localhost:$port/api/customers/tickets/${ticketId}",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }


    @Test
            /** PATCH /api/customers/tickets/:ticketId/reopen */
    fun successReopenClosedTicket() {
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")

        val expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        var product = utilityFunctions.createTestProduct(customer)
        val ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.CLOSED)
        utilityFunctions.createTestManager()

        val accessToken = utilityFunctions.customerLogin()

        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }


        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/${ticket.getId()}/reopen",
            HttpMethod.PATCH,
            HttpEntity(null, headers),
            String::class.java
        )

        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)

        val actualTicket = ticketRepository.getReferenceById(ticket.getId()!!)
        Assertions.assertEquals(TicketState.REOPENED, actualTicket.state)
    }

    @Test
            /** PATCH /api/customers/tickets/:ticketId/reopen */
    fun successReopenResolvedTicket() {
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")

        val expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        var product = utilityFunctions.createTestProduct(customer)
        val ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.RESOLVED)
        utilityFunctions.createTestManager()

        val accessToken = utilityFunctions.customerLogin()

        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/${ticket.getId()}/reopen",
            HttpMethod.PATCH,
            HttpEntity(null, headers),
            String::class.java
        )

        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)

        val actualTicket = ticketRepository.getReferenceById(ticket.getId()!!)
        Assertions.assertEquals(TicketState.REOPENED, actualTicket.state)
    }

    @Test
            /** PATCH /api/customers/tickets/:ticketId/reopen */
    fun failReopenAlreadyOpenTicket() {
        val customer: Customer = utilityFunctions.createTestCustomer("John", "Doe")
            ?: fail("Test failed because no customer was created in the database.")

        utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        var product = utilityFunctions.createTestProduct(customer)
        val ticket = utilityFunctions.createTestTicket(customer, product, null, TicketState.OPEN)
        utilityFunctions.createTestManager()

        val accessToken = utilityFunctions.managerLogin()

        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        val url = "/api/managers/tickets/${ticket.getId()}/relieveExpert"
        val response = utilityFunctions.restTemplate.exchange(
            url,
            HttpMethod.PATCH,
            HttpEntity(null, headers),
            String::class.java
        )
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.CONFLICT, response?.statusCode)

        val actualTicket = ticketRepository.getReferenceById(ticket.getId()!!)
        Assertions.assertEquals(TicketState.OPEN, actualTicket.state)
    }


    @Test
            /** PATCH /api/customers/tickets/:ticketId/compileSurvey */
    fun successCompileSurvey() {
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")

        val expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        var product = utilityFunctions.createTestProduct(customer)
        val ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.RESOLVED)
        utilityFunctions.createTestManager()

        val accessToken = utilityFunctions.customerLogin()

        val jsonRequest = JSONObject()
        jsonRequest.put("survey", "survey for the ticket")

        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            add("content-type", "application/json")
        }

        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/${ticket.getId()}/compileSurvey",
            HttpMethod.PATCH,
            HttpEntity(jsonRequest.toString(), headers),
            String::class.java
        )
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        val actualTicket = ticketRepository.getReferenceById(ticket.getId()!!)
        Assertions.assertEquals(actualTicket.state, TicketState.CLOSED)
    }

    @Test
            /** PATCH /api/customers/tickets/:ticketId/compileSurvey */
    fun failCompileSurveyTicketAlreadyClosed() {
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")

        val expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        var product = utilityFunctions.createTestProduct(customer)
        val ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.CLOSED)
        utilityFunctions.createTestManager()



        val jsonRequest = JSONObject()
        jsonRequest.put("survey", "survey for the ticket")

        val accessToken = utilityFunctions.customerLogin()

        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            add("content-type", "application/json")
        }


        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/${ticket.getId()}/compileSurvey",
            HttpMethod.PATCH,
            HttpEntity(jsonRequest.toString(), headers),
            String::class.java
        )

        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.CONFLICT, response.statusCode)

        val actualTicket = ticketRepository.getReferenceById(ticket.getId()!!)
        Assertions.assertEquals(TicketState.CLOSED, actualTicket.state)
    }



    @Test
            /** PATCH /api/customers/products/registerProduct */
    fun successRegisterProduct() {
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")

        val randUUID = UUID.randomUUID()
        val product = utilityFunctions.createUnregisteredTestProduct(randUUID)
        println(product)

        val jsonRequest = JSONObject()
        jsonRequest.put("productId", product.id)
        jsonRequest.put("serialNumber", product.serialNumber)

        println("id: ${product.id}, sn: ${product.serialNumber}")

        val accessToken = utilityFunctions.customerLogin()

        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            add("content-type", "application/json")
        }

        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/products/registerProduct",
            HttpMethod.PATCH,
            HttpEntity(jsonRequest.toString(), headers),
            String::class.java
        )

        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        val updatedProduct = productRepository.findByIdOrNull(product.id)
        println("${updatedProduct?.id} ${updatedProduct?.registered} ${updatedProduct?.owner?.id}")
        Assertions.assertEquals(true, updatedProduct?.registered)
        Assertions.assertEquals(customer.id, updatedProduct?.owner?.id)

    }


    @Test
            /** PATCH /api/customers/products/registerProduct */
    fun failRegisterNonExistingProduct() {
        val customer: Customer = utilityFunctions.createTestCustomer("John", "Doe")
            ?: fail("Test failed because no customer was created in the database.")

        val randomSerialNumber = UUID.randomUUID()


        val jsonRequest = JSONObject()
        jsonRequest.put("productId", 1)
        jsonRequest.put("serialNumber", randomSerialNumber)

        val accessToken = utilityFunctions.customerLogin()

        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            add("content-type", "application/json")
        }

        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/products/registerProduct",
            HttpMethod.PATCH,
            HttpEntity(jsonRequest.toString(), headers),
            String::class.java
        )

        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }


    @Test
            /** PATCH /api/customers/products/registerProduct */
    fun failRegisterProductInValidation() {
        val customer: Customer = utilityFunctions.createTestCustomer("John", "Doe")
            ?: fail("Test failed because no customer was created in the database.")

        val serialNumber = UUID.randomUUID()
        val product: Product = utilityFunctions.createUnregisteredTestProduct(serialNumber)



        val jsonRequest = JSONObject()
        jsonRequest.put("productId", "invalid")
        jsonRequest.put("serialNumber", serialNumber)

        val accessToken = utilityFunctions.customerLogin()

        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            add("content-type", "application/json")
        }

        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/products/registerProduct",
            HttpMethod.PATCH,
            HttpEntity(jsonRequest.toString(), headers),
            String::class.java
        )

        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
    }





    //EXPERTS





    @Test
            /** GET /api/experts/tickets*/
    fun successGetAllTicketsOfAnExpert() {
        val customer: Customer = utilityFunctions.createTestCustomer("John", "Doe")
            ?: fail("Test failed because no customer was created in the database.")

        val expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        var product = utilityFunctions.createTestProduct(customer)
        val ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)

        /* expert login */
        val accessToken = utilityFunctions.expertLogin()

        /* crafting the request */
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* retrieving all the tickets */
        val response: ResponseEntity<String> = utilityFunctions.restTemplate.exchange(
            "http://localhost:$port/api/experts/tickets",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )


        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body
        val resTicket = JSONObject(body).getJSONArray("content").getJSONObject(0)
        Assertions.assertEquals("IN_PROGRESS", resTicket.getString("ticketState"))
        Assertions.assertEquals(product.serialNumber.toString(), resTicket.getString("serialNumber"))
        Assertions.assertEquals(expert.id.toString(), resTicket.getString("expertId"))
        Assertions.assertEquals(customer.id.toString(), resTicket.getString("customerId"))
        Assertions.assertEquals(ticket.description, resTicket.getString("description"))
        Assertions.assertEquals(formatter.format(ticket.lastModified), resTicket.getString("lastModified"))
        Assertions.assertEquals(formatter.format(ticket.creationDate), resTicket.getString("creationDate"))
        Assertions.assertEquals(ticket.getId()!!.toLong(), resTicket.getLong("ticketId"))
    }


    @Test
            /** GET /api/experts/tickets */
    fun failGetAllTicketsOfANonExistentExpert() {
        val url = "/api/experts/tickets"
        val response = utilityFunctions.restTemplate.getForEntity(url, String::class.java)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response?.statusCode)
    }


    @Ignore
    @Test
            /** GET /api/experts/tickets/:ticketId */
    fun successGetASingleTicketsOfAnExpert() {
        val customer: Customer = utilityFunctions.createTestCustomer("John", "Doe")
            ?: fail("Test failed because no customer was created in the database.")

        val expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        var product = utilityFunctions.createTestProduct(customer)
        val ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)

        /* customer login */
        val accessToken = utilityFunctions.expertLogin()

        /* crafting the request */
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* retrieving all the tickets */
        val response: ResponseEntity<String> = utilityFunctions.restTemplate.exchange(
            "/api/experts/tickets/${ticket.getId()}",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )
        val body = response.body
        val resTicket = JSONObject(body)

        Assertions.assertEquals("IN_PROGRESS", resTicket.getString("ticketState"))
        Assertions.assertEquals(product.serialNumber.toString(), resTicket.getString("serialNumber"))
        Assertions.assertEquals(expert.id.toString(), resTicket.getString("expertId"))
        Assertions.assertEquals(customer.id.toString(), resTicket.getString("customerId"))
        Assertions.assertEquals(ticket.description, resTicket.getString("description"))
        Assertions.assertEquals(formatter.format(ticket.lastModified), resTicket.getString("lastModified"))
        Assertions.assertEquals(formatter.format(ticket.creationDate), resTicket.getString("creationDate"))
        Assertions.assertEquals(ticket.getId()!!.toLong(), resTicket.getLong("ticketId"))

    }

    @Test
            /** GET /api/experts/tickets/:ticketId */
    fun failGetASingleTicketOfAnExpertWithoutLogin() {
        val ticketId = (1000..2000).random()
        val url = "/api/experts/tickets/${ticketId}"
        val response = utilityFunctions.restTemplate
            .getForEntity(url, String::class.java)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response?.statusCode)
    }

    @Test
            /** GET /api/experts/tickets/:ticketId */
    fun failGetANonExistentTicketOfAnExpert() {
        val expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        expertRepository.save(expert).id
        val ticketId = (1000..2000).random()

        /* expert login */
        val accessToken = utilityFunctions.expertLogin()

        /* crafting the request */
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* retrieving all the tickets */
        val response: ResponseEntity<String> = utilityFunctions.restTemplate.exchange(
            "http://localhost:$port/api/experts/tickets/${ticketId}",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )

        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
    }


    @Test
            /** PATCH /api/experts/tickets/:ticketId/resolve */
    fun successResolveInProgressTicket() {
        val customer: Customer = utilityFunctions.createTestCustomer("John", "Doe")
            ?: fail("Test failed because no customer was created in the database.")

        val transactionDefinition = DefaultTransactionDefinition()
        val transactionStatus: TransactionStatus = transactionManager.getTransaction(transactionDefinition)


        val expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")

        try {
            println("[!] saving into repository")
            println(expertRepository.save(expert).id)
            transactionManager.commit(transactionStatus)
        } catch (e: Exception) {
            transactionManager.rollback(transactionStatus)
            throw e
        }

        var product = utilityFunctions.createTestProduct(customer)
        val ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)
        val manager = utilityFunctions.createTestManager()

        /* expert login */
        val accessToken = utilityFunctions.expertLogin()

        /* crafting the request */
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        // 404 no expert found by UUID but it was actually inserted in the db
        val response = utilityFunctions.restTemplate.exchange(
            "/api/experts/tickets/${ticket.getId()}/resolve",
            HttpMethod.PATCH,
            HttpEntity(null, headers),
            String::class.java
        )

        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)

        val actualTicket = ticketRepository.getReferenceById(ticket.getId()!!)
        Assertions.assertEquals(TicketState.RESOLVED, actualTicket.state)
    }

    @Test
            /** PATCH /api/experts/tickets/:ticketId/resolve */
    fun failResolveClosedTicket() {
        val customer: Customer = utilityFunctions.createTestCustomer("John", "Doe")
            ?: fail("Test failed because no customer was created in the database.")

        val expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        var product = utilityFunctions.createTestProduct(customer)
        val ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.CLOSED)
        val manager = utilityFunctions.createTestManager()

        val accessToken = utilityFunctions.expertLogin()

        /* crafting the request */
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        val response = utilityFunctions.restTemplate.exchange(
            "/api/experts/tickets/${ticket.getId()}/resolve",
            HttpMethod.PATCH,
            HttpEntity(null, headers),
            String::class.java
        )

        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.CONFLICT, response.statusCode)

        val actualTicket = ticketRepository.getReferenceById(ticket.getId()!!)
        Assertions.assertEquals(TicketState.CLOSED, actualTicket.state)
    }


    @Test
            /** PATCH '/api/experts/tickets/:ticketId/close' */
    fun successCloseReopenedTicketByExpert() {
        val customer: Customer = utilityFunctions.createTestCustomer("John", "Doe")
            ?: fail("Test failed because no customer was created in the database.")

        val expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        var product = utilityFunctions.createTestProduct(customer)
        val ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.REOPENED)
        val manager = utilityFunctions.createTestManager()

        val accessToken = utilityFunctions.expertLogin()

        /* crafting the request */
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        val response = utilityFunctions.restTemplate.exchange(
            "/api/experts/tickets/${ticket.getId()}/close",
            HttpMethod.PATCH,
            HttpEntity(null, headers),
            String::class.java
        )
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        val actualTicket = ticketRepository.getReferenceById(ticket.getId()!!)
        Assertions.assertEquals(actualTicket.state, TicketState.CLOSED)
    }

    @Test
            /** PATCH '/api/experts/tickets/:ticketId/close' */
    fun failCloseAlreadyClosedTicketByExpert() {
        val customer: Customer = utilityFunctions.createTestCustomer("John", "Doe")
            ?: fail("Test failed because no customer was created in the database.")

        val expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        var product = utilityFunctions.createTestProduct(customer)
        val ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.CLOSED)
        val manager = utilityFunctions.createTestManager()

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val requestObject = JSONObject()
        requestObject.put("expertId", expert.id)

        val accessToken = utilityFunctions.expertLogin()
        headers.add("Authorization", "Bearer $accessToken")

        val response = utilityFunctions.restTemplate.exchange(
            "/api/experts/tickets/${ticket.getId()}/close",
            HttpMethod.PATCH,
            HttpEntity(requestObject.toString(), headers),
            String::class.java
        )

        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.CONFLICT, response.statusCode)

        val actualTicket = ticketRepository.getReferenceById(ticket.getId()!!)
        Assertions.assertEquals(TicketState.CLOSED, actualTicket.state)
    }


    //MANAGERS

    @Test
            /** GET /api/managers/tickets*/

    fun successGetAllTicketsOfAManager() {
        val customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val customerId = customer.id


        val expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val expertId = expertRepository.save(expert).id

        val product = utilityFunctions.createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = utilityFunctions.createTestManager()
        managerRepository.save(manager).id

        /* manager login */
        val accessToken = utilityFunctions.managerLogin()

        /* crafting the request */
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* retrieving all the tickets */
        val response: ResponseEntity<String> = utilityFunctions.restTemplate.exchange(
            "/api/managers/tickets",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )

        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        val body = response.body
        val resTicket = JSONObject(body).getJSONArray("content").getJSONObject(0)
        Assertions.assertEquals("IN_PROGRESS", resTicket.getString("ticketState"))
        Assertions.assertEquals(product.serialNumber.toString(), resTicket.getString("serialNumber"))
        Assertions.assertEquals(expertId.toString(), resTicket.getString("expertId"))
        Assertions.assertEquals(customerId.toString(), resTicket.getString("customerId"))
        Assertions.assertEquals(ticket.description, resTicket.getString("description"))
        Assertions.assertEquals(formatter.format(ticket.lastModified), resTicket.getString("lastModified"))
        Assertions.assertEquals(formatter.format(ticket.creationDate), resTicket.getString("creationDate"))
        Assertions.assertEquals(ticketId!!.toLong(), resTicket.getLong("ticketId"))
    }


    @Test
            /** GET /api/managers/tickets*/
    fun failGetAllTicketsOfANonExistentManager() {
        val url = "/api/managers/tickets"
        val response = utilityFunctions.restTemplate
            .getForEntity(url, String::class.java)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response?.statusCode)
    }


    @Test
            /** PATCH /api/managers/tickets/:ticketId/assign */
    fun successAssignmentOfATicket() {

        val customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val customerId = customerRepository.save(customer).id

        val expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val expertId = expertRepository.save(expert).id

        val product = utilityFunctions.createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = utilityFunctions.createTestTicket(customer, product, null, TicketState.OPEN)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = utilityFunctions.createTestManager()
        val managerId = managerRepository.save(manager).id

        /* manager login */
        val accessToken = utilityFunctions.managerLogin()

        /* crafting the request */
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            add("content-type", "application/json")
        }

        val requestObject = JSONObject()
        requestObject.put("expertId", expertId.toString())

        val response = utilityFunctions.restTemplate.exchange(
            "/api/managers/tickets/${ticketId}/assign",
            HttpMethod.PATCH,
            HttpEntity(requestObject.toString(), headers),
            String::class.java
        )
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)

        val actualTicket = ticketRepository.getReferenceById(ticketId!!)
        Assertions.assertNotNull(actualTicket.expert)
        Assertions.assertEquals(actualTicket.expert!!.id, expertId)
        Assertions.assertEquals(actualTicket.state, TicketState.IN_PROGRESS)

    }


    @Test
            /** PATCH /api/managers/tickets/:ticketId/relieveExpert */
    fun successRelieveExpert() {
        val customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val customerId = customerRepository.save(customer).id

        val expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val expertId = expertRepository.save(expert).id

        val product = utilityFunctions.createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = utilityFunctions.createTestManager()
        val managerId = managerRepository.save(manager).id

        /* manager login */
        val accessToken = utilityFunctions.managerLogin()

        /* crafting the request */
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            add("content-type", "application/json")
        }

        val requestObject = JSONObject()
        requestObject.put("expertId", expertId.toString())

        val response = utilityFunctions.restTemplate.exchange(
            "/api/managers/tickets/${ticketId}/relieveExpert",
            HttpMethod.PATCH,
            HttpEntity(requestObject.toString(), headers),
            String::class.java
        )

        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        val actualTicket = ticketRepository.getReferenceById(ticketId!!)
        Assertions.assertNull(actualTicket.expert)
        Assertions.assertEquals(actualTicket.state, TicketState.OPEN)
    }


    @Test
            /** PATCH /api/managers/tickets/:ticketId/relieveExpert */
    fun failRelieveExpertWithNonExistentIds() {
        val manager = utilityFunctions.createTestManager()
        val managerId = managerRepository.save(manager).id

        /* manager login */
        val accessToken = utilityFunctions.managerLogin()

        /* crafting the request */
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        val ticketId = (10000..20000).random()
        val url = "/api/managers/tickets/${ticketId}/relieveExpert"
        val response = utilityFunctions.restTemplate.exchange(
            url,
            HttpMethod.PATCH,
            HttpEntity(null, headers),
            String::class.java
        )
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response?.statusCode)
    }


    @Test
            /** PATCH /api/managers/tickets/:ticketId/close */
    fun successCloseResolvedTicketByManager() {
        val customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val customerId = customerRepository.save(customer).id

        val expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val expertId = expertRepository.save(expert).id

        val product = utilityFunctions.createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.RESOLVED)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = utilityFunctions.createTestManager()
        val managerId = managerRepository.save(manager).id

        /* manager login */
        val accessToken = utilityFunctions.managerLogin()

        /* crafting the request */
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        val response = utilityFunctions.restTemplate.exchange(
            "/api/managers/tickets/${ticketId}/close",
            HttpMethod.PATCH,
            HttpEntity(null, headers),
            String::class.java
        )
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        val actualTicket = ticketRepository.getReferenceById(ticketId!!)
        Assertions.assertEquals(actualTicket.state, TicketState.CLOSED)
    }


    @Test
            /** PATCH /api/managers/tickets/:ticketId/close */
    fun failCloseAlreadyClosedTicketByManager() {
        val customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val customerId = customerRepository.save(customer).id

        val expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val expertId = expertRepository.save(expert).id

        val product = utilityFunctions.createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.CLOSED)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = utilityFunctions.createTestManager()
        val managerId = managerRepository.save(manager).id

        /* manager login */
        val accessToken = utilityFunctions.managerLogin()

        /* crafting the request */
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        val response = utilityFunctions.restTemplate.exchange(
            "/api/managers/tickets/${ticketId}/close",
            HttpMethod.PATCH,
            HttpEntity(null, headers),
            String::class.java
        )

        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.CONFLICT, response.statusCode)

        val actualTicket = ticketRepository.getReferenceById(ticketId!!)
        Assertions.assertEquals(TicketState.CLOSED, actualTicket.state)
    }

    @Test
            /** PATCH /api/managers/tickets/:ticketId/resumeProgress */
    fun succeedResumeProgress() {
        val customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val customerId = customerRepository.save(customer).id

        val expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val expertId = expertRepository.save(expert).id

        val product = utilityFunctions.createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.REOPENED)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = utilityFunctions.createTestManager()
        val managerId = managerRepository.save(manager).id

        /* manager login */
        val accessToken = utilityFunctions.managerLogin()

        /* crafting the request */
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            add("content-type", "application/json")
        }

        val requestObject = JSONObject()
        requestObject.put("expertId", expert.id)

        val response = utilityFunctions.restTemplate.exchange(
            "/api/managers/tickets/${ticketId}/resumeProgress",
            HttpMethod.PATCH,
            HttpEntity(requestObject.toString(), headers),
            String::class.java
        )
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)

        val actualTicket = ticketRepository.getReferenceById(ticketId!!)
        Assertions.assertEquals(TicketState.IN_PROGRESS, actualTicket.state)

    }

    @Test
            /** PATCH /api/managers/tickets/:ticketId/resumeProgress */
    fun failResumeProgressAlreadyClosedTicket() {
        val customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val customerId = customerRepository.save(customer).id

        val expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val expertId = expertRepository.save(expert).id

        val product = utilityFunctions.createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.CLOSED)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = utilityFunctions.createTestManager()
        val managerId = managerRepository.save(manager).id

        /* manager login */
        val accessToken = utilityFunctions.managerLogin()

        /* crafting the request */
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            add("content-type", "application/json")
        }

        val requestObject = JSONObject()
        requestObject.put("expertId", expert.id)

        val response = utilityFunctions.restTemplate.exchange(
            "/api/managers/tickets/${ticketId}/resumeProgress",
            HttpMethod.PATCH,
            HttpEntity(requestObject.toString(), headers),
            String::class.java
        )

        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.CONFLICT, response.statusCode)

        val actualTicket = ticketRepository.getReferenceById(ticketId!!)
        Assertions.assertEquals(TicketState.CLOSED, actualTicket.state)
    }


    @Test
            /** PATCH /api/managers/tickets/:ticketId/remove*/
    fun successRemoveTicket() {
        val customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        customerRepository.save(customer).id

        val expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val expertId = expertRepository.save(expert).id

        val product = utilityFunctions.createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.CLOSED)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = utilityFunctions.createTestManager()
        val managerId = managerRepository.save(manager).id

        /* manager login */
        val accessToken = utilityFunctions.managerLogin()

        /* crafting the request */
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        val response = utilityFunctions.restTemplate.exchange(
            "/api/managers/tickets/${ticketId}/remove",
            HttpMethod.DELETE,
            HttpEntity(null, headers),
            String::class.java
        )
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
    }


// MESSAGING AND ATTACHMENTS TEST CASES

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


    @Test
    fun `Expert wants to send a message but does not exist in the database`() {

        /* expert login */
        val accessToken: String = utilityFunctions.expertLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            contentType = MediaType.MULTIPART_FORM_DATA
        }


        /* sending the messages */
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("messageText", "Hi. How can I help you?")
            add("attachments", null)
        }
        val anyValueDoesntMatter: Int = 0
        val response = utilityFunctions.restTemplate.exchange(
            "/api/experts/tickets/$anyValueDoesntMatter/messages",
            HttpMethod.POST,
            HttpEntity(formData, headers),
            String::class.java
        )

        /* assertions */
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals("No expert profile found with this UUID.", JSONObject(response.body).get("error"))
    }

    @Test
    fun `Expert fails to send a message for a non-existing ticket`() {

        /* preparing database */
        utilityFunctions.createTestExpert("expert-1")

        /* expert login */
        val accessToken: String = utilityFunctions.expertLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            contentType = MediaType.MULTIPART_FORM_DATA
        }


        /* sending the messages */
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("messageText", "Hi. How can I help you?")
            add("attachments", null)
        }
        val nonExistingTicketId: Int = 0
        val response = utilityFunctions.restTemplate.exchange(
            "/api/experts/tickets/$nonExistingTicketId/messages",
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
    fun `Expert sending the message is unauthorized to access this set of tickets`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val product: Product = utilityFunctions.createTestProduct(customer)
        val expert: Expert = utilityFunctions.createTestExpert("expert-2")
            ?: fail("Test failed because no expert was created in the database.")
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)

        /* expert login */
        val accessToken: String = utilityFunctions.expertLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            contentType = MediaType.MULTIPART_FORM_DATA
        }


        /* sending the messages */
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("messageText", "Hi. How can I help you?")
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
        Assertions.assertEquals("Forbidden", JSONObject(response.body).get("error"))

    }

    @Test
    fun `Expert sending a message is forbidden to access tickets he is not assigned to`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val product: Product = utilityFunctions.createTestProduct(customer)
        utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val expert: Expert = utilityFunctions.createTestExpert("expert-2")
            ?: fail("Test failed because no expert was created in the database.")
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)

        /* expert login */
        val accessToken: String = utilityFunctions.expertLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            contentType = MediaType.MULTIPART_FORM_DATA
        }

        /* sending the messages */
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("messageText", "Hi. How can I help you?")
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
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals("Expert not assigned to this ticket.", JSONObject(response.body).get("error"))

    }

    @Test
    fun `Expert sending a message makes a request with wrong format`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("John", "Doe")
            ?: fail("Test failed because no customer was created in the database.")
        val product: Product = utilityFunctions.createTestProduct(customer)
        val expert: Expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)


        /* expert login */
        val accessToken: String = utilityFunctions.expertLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            contentType = MediaType.MULTIPART_FORM_DATA
        }

        /* sending the messages */
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("wrongAttribute", "Hi. How can I help you?")
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
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        Assertions.assertEquals("Bad Request", JSONObject(response.body).get("error"))

    }

    @Test
    fun `Expert creates a message for a ticket in an invalid state`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val expert: Expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val product: Product = utilityFunctions.createTestProduct(customer)
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.CLOSED)

        /* expert login */
        val accessToken: String = utilityFunctions.expertLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            contentType = MediaType.MULTIPART_FORM_DATA
        }


        /* sending the messages */
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("messageText", "Hi. How can I help you?")
            add("attachments", null)
        }
        val response = utilityFunctions.restTemplate.exchange(
            "/api/experts/tickets/${ticket.getId()}/messages",
            HttpMethod.POST,
            HttpEntity(formData, headers),
            String::class.java
        )

        println(response.body)

        /* assertions */
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.CONFLICT, response.statusCode)
        Assertions.assertEquals("Invalid ticket status for this operation.", JSONObject(response.body).get("error"))
    }

    @Test
    fun `Expert sends a message`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val expert: Expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val product: Product = utilityFunctions.createTestProduct(customer)
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)

        /* expert login */
        val accessToken: String = utilityFunctions.expertLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            contentType = MediaType.MULTIPART_FORM_DATA
        }


        /* sending the messages */
        val formData: MultiValueMap<String, String> = LinkedMultiValueMap<String, String>().apply {
            add("messageText", "Hi. How can I help you?")
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
        Assertions.assertEquals(HttpStatus.CREATED, response.statusCode)
        Assertions.assertEquals("expert-1", JSONObject(response.body).get("sender"))
        Assertions.assertEquals("Hi. How can I help you?", JSONObject(response.body).get("messageText"))
    }

    @Test
    fun `Expert sends a message with an attachment`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val expert: Expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val product: Product = utilityFunctions.createTestProduct(customer)
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)

        /* expert login */
        val accessToken: String = utilityFunctions.expertLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
            contentType = MediaType.MULTIPART_FORM_DATA
        }


        /* sending the messages */
        val formData: MultiValueMap<String, Any> = LinkedMultiValueMap<String, Any>().apply {
            add("messageText", "Hi. How can I help you?")
            add("attachments", utilityFunctions.createTestAttachment().resource)
        }
        val response = utilityFunctions.restTemplate.exchange(
            "/api/experts/tickets/${ticket.getId()}/messages",
            HttpMethod.POST,
            HttpEntity(formData, headers),
            String::class.java
        )

        /* assertions */
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.CREATED, response.statusCode)
        Assertions.assertEquals("expert-1", JSONObject(response.body).get("sender"))
        Assertions.assertEquals("Hi. How can I help you?", JSONObject(response.body).get("messageText"))
    }


    @Test
    fun `Expert wants to get all messages but does not exist in the database`() {

        /* customer login */
        val accessToken: String = utilityFunctions.expertLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* retrieving the messages */
        val anyValueDoesntMatter: Int = 0
        val response = utilityFunctions.restTemplate.exchange(
            "/api/experts/tickets/${anyValueDoesntMatter}/messages",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )

        /* assertions */
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals("No expert profile found with this UUID.", JSONObject(response.body).get("error"))
    }

    @Test
    fun `Expert fails to retrieve messages for a non-existing ticket`() {

        /* preparing database */
        utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")

        /* customer login */
        val accessToken: String = utilityFunctions.expertLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* retrieving the messages */
        val nonExistingTicketId: Int = 0
        val response = utilityFunctions.restTemplate.exchange(
            "/api/experts/tickets/${nonExistingTicketId}/messages",
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
    fun `Expert wants to get all messages but is unauthorized to access this set of tickets`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("John", "Doe")
            ?: fail("Test failed because no customer was created in the database.")
        val product: Product = utilityFunctions.createTestProduct(customer)
        val expert: Expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)

        /* customer login */
        val accessToken: String = utilityFunctions.expertLogin()
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
        Assertions.assertEquals("Forbidden", JSONObject(response.body).get("error"))

    }

    @Test
    fun `Expert wants to get all messages but is forbidden to access tickets he is not assigned to`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val product: Product = utilityFunctions.createTestProduct(customer)
        val expert: Expert = utilityFunctions.createTestExpert("expert-2")
            ?: fail("Test failed because no expert was created in the database.")
        utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)

        /* customer login */
        val accessToken: String = utilityFunctions.expertLogin()
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

        println(response.body)

        /* assertions */
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals("Expert not assigned to this ticket.", JSONObject(response.body).get("error"))

    }

    @Test
    fun `Expert retrieving the messages makes a request with wrong format`() {

        /* preparing database */
        utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")

        /* customer login */
        val accessToken: String = utilityFunctions.expertLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* retrieving the messages */
        val wrongTicketId = "BadRequest"
        val response = utilityFunctions.restTemplate.exchange(
            "/api/experts/tickets/${wrongTicketId}/messages",
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
    fun `Expert retrieves all the messages`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val expert: Expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val product: Product = utilityFunctions.createTestProduct(customer)
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)
        utilityFunctions.createMessage(ticket, customer, "Hello, I need help!")
        utilityFunctions.createMessage(ticket, expert, "Hi, how can I help you?")
        utilityFunctions.createMessage(ticket, customer, "Windows keeps freezing...")


        /* customer login */
        val accessToken: String = utilityFunctions.expertLogin()
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
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals(3, JSONObject(response.body).get("totalElements"))
    }


    @Test
    fun `Expert wants to retrieve an attachment but does not exist in the database`() {

        /* expert login */
        val accessToken: String = utilityFunctions.expertLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }


        /* sending the messages */
        val anyValueDoesntMatter: Int = 0
        val anyNameDoesntMatter: String = "filename"
        val response = utilityFunctions.restTemplate.exchange(
            "/api/experts/tickets/$anyValueDoesntMatter/attachments/$anyNameDoesntMatter",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )

        /* assertions */
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals("No expert profile found with this UUID.", JSONObject(response.body).get("error"))
    }

    @Test
    fun `Expert fails to retrieve attachment for a non-existing ticket`() {

        /* preparing database */
        utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")

        /* expert login */
        val accessToken: String = utilityFunctions.expertLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }


        /* sending the messages */
        val nonExistingTicketId: Int = 0
        val anyNameDoesntMatter: String = "filename"
        val response = utilityFunctions.restTemplate.exchange(
            "/api/experts/tickets/$nonExistingTicketId/attachments/$anyNameDoesntMatter",
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
    fun `Expert retrieving an attachment is unauthorized to access this set of tickets`() {

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


        /* expert login */
        val accessToken: String = utilityFunctions.expertLogin()
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
        Assertions.assertEquals("Forbidden", JSONObject(response.body).get("error"))
    }


    @Test
    fun `Expert retrieving an attachment is forbidden to access tickets he is not assigned to`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val product: Product = utilityFunctions.createTestProduct(customer)
        val expert: Expert = utilityFunctions.createTestExpert("expert-2")
            ?: fail("Test failed because no expert was created in the database.")
        utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)
        utilityFunctions.createMessage(ticket, customer, "Hello, I need help!")
        utilityFunctions.createMessage(ticket, customer, "Hi, how can I help you?")
        val uniqueFileNameList: List<String> = utilityFunctions.createMessageWithAttachment(ticket, customer, "Windows keeps freezing...")

        /* expert login */
        val accessToken: String = utilityFunctions.expertLogin()
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
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals("Expert not assigned to this ticket.", JSONObject(response.body).get("error"))

    }

    @Test
    fun `Expert retrieving an attachment makes a request with wrong format`() {

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


        /* expert login */
        val accessToken: String = utilityFunctions.expertLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* sending the messages */
        val wrongTicketId = "BadRequest"
        val response = utilityFunctions.restTemplate.exchange(
            "/api/experts/tickets/$wrongTicketId/attachments/${uniqueFileNameList.first()}",
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
    fun `Expert fails to retrieve attachment for a non-existing attachment`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val product: Product = utilityFunctions.createTestProduct(customer)
        val expert: Expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)

        /* customer login */
        val accessToken: String = utilityFunctions.expertLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* sending the messages */
        val wrongFileName = "BadRequest"
        val response = utilityFunctions.restTemplate.exchange(
            "/api/experts/tickets/${ticket.getId()}/attachments/$wrongFileName",
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
    fun `Expert retrieves the attachment`() {

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


        /* expert login */
        val accessToken: String = utilityFunctions.expertLogin()
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
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("application/octet-stream", response.headers.contentType.toString())
        Assertions.assertEquals("attachment; filename=\"${uniqueFileNameList.first()}\"", response.headers.getFirst("Content-Disposition"))
        response.body
            ?: fail("Response body is null")

    }

    @Test
    fun `Manager wants to get all messages but does not exist in the database`() {

        /* customer login */
        val accessToken: String = utilityFunctions.managerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* retrieving the messages */
        val anyValueDoesntMatter: Int = 0
        val response = utilityFunctions.restTemplate.exchange(
            "/api/managers/tickets/${anyValueDoesntMatter}/messages",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )

        /* assertions */
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals("No manager profile found with this UUID.", JSONObject(response.body).get("error"))

    }

    @Test
    fun `Manager fails to retrieve messages for a non-existing ticket`() {

        /* preparing database */
        utilityFunctions.createTestManager()

        /* customer login */
        val accessToken: String = utilityFunctions.managerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* retrieving the messages */
        val nonExistingTicketId: Int = 0
        val response = utilityFunctions.restTemplate.exchange(
            "/api/managers/tickets/${nonExistingTicketId}/messages",
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
    fun `Manager makes a request with wrong format`() {

        /* preparing database */
        utilityFunctions.createTestManager()


        /* customer login */
        val accessToken: String = utilityFunctions.managerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* retrieving the messages */
        val wrongTicketId = "BadRequest"
        val response = utilityFunctions.restTemplate.exchange(
            "/api/managers/tickets/${wrongTicketId}/messages",
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
    fun `Managers retrieves all the messages`() {

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
        utilityFunctions.createTestManager()


        /* customer login */
        val accessToken: String = utilityFunctions.managerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* retrieving the messages */
        val response = utilityFunctions.restTemplate.exchange(
            "/api/managers/tickets/${ticket.getId()}/messages",
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
    fun `Manager wants to retrieve an attachment but does not exist in the database`() {

        /* customer login */
        val accessToken: String = utilityFunctions.managerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }


        /* sending the messages */
        val anyValueDoesntMatter: Int = 0
        val anyNameDoesntMatter: String = "filename"
        val response = utilityFunctions.restTemplate.exchange(
            "/api/managers/tickets/$anyValueDoesntMatter/attachments/$anyNameDoesntMatter",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )

        /* assertions */
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        Assertions.assertEquals("No manager profile found with this UUID.", JSONObject(response.body).get("error"))

    }


    @Test
    fun `Manager fails to retrieve attachment for a non-existing ticket`() {

        /* preparing database */
        utilityFunctions.createTestManager()

        /* customer login */
        val accessToken: String = utilityFunctions.managerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }


        /* sending the messages */
        val nonExistingTicketId: Int = 0
        val anyNameDoesntMatter: String = "filename"
        val response = utilityFunctions.restTemplate.exchange(
            "/api/managers/tickets/$nonExistingTicketId/attachments/$anyNameDoesntMatter",
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
    fun `Manager retrieving an attachment makes a request with wrong format`() {

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
        utilityFunctions.createTestManager()

        /* customer login */
        val accessToken: String = utilityFunctions.managerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* sending the messages */
        val wrongTicketId = "BadRequest"
        val response = utilityFunctions.restTemplate.exchange(
            "/api/managers/tickets/$wrongTicketId/attachments/${uniqueFileNameList.first()}",
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
    fun `Manager fails to retrieve attachment for a non-existing attachment`() {

        /* preparing database */
        val customer: Customer = utilityFunctions.createTestCustomer("Mario", "Rossi")
            ?: fail("Test failed because no customer was created in the database.")
        val product: Product = utilityFunctions.createTestProduct(customer)
        val expert: Expert = utilityFunctions.createTestExpert("expert-1")
            ?: fail("Test failed because no expert was created in the database.")
        val ticket: Ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)
        utilityFunctions.createTestManager()

        /* customer login */
        val accessToken: String = utilityFunctions.managerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* sending the messages */
        val wrongFileName = "BadRequest"
        val response = utilityFunctions.restTemplate.exchange(
            "/api/managers/tickets/${ticket.getId()}/attachments/$wrongFileName",
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
    fun `Manager retrieves the attachment`() {

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
        utilityFunctions.createTestManager()

        /* customer login */
        val accessToken: String = utilityFunctions.managerLogin()
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* sending the messages */
        val response = utilityFunctions.restTemplate.exchange(
            "/api/managers/tickets/${ticket.getId()}/attachments/${uniqueFileNameList.first()}",
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