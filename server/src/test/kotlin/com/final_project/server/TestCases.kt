package com.final_project.server

import com.final_project.security.dto.UserCredentialsDTO
import com.final_project.ticketing.util.TicketState
import org.json.JSONObject
import org.junit.Ignore
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.http.*
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.DefaultTransactionDefinition
import org.springframework.util.MultiValueMap

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
class TestCases : ApplicationTests(){

    //AUTHENTICATION TESTS
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


    //CUSTOMERS

    @Test /** GET /api/customers/tickets */
    fun `Customer retrieve all the tickets`() {
        /* adding data to database */
        val expert = utilityFunctions.createTestExpert()
        val expertId = expertRepository.save(expert).id

        val customer = utilityFunctions.createTestCustomer()
        val customerId = customerRepository.save(customer).id

        val product = utilityFunctions.createTestProduct(customer)
        println(productRepository.save(product).getId())


        val ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)
        val ticketId = ticketRepository.save(ticket).getId()

        /* customer login */
        val accessToken = utilityFunctions.customerLogin()

        /* crafting the request */
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* retrieving all the tickets */
        val response: ResponseEntity<String> = utilityFunctions.restTemplate.exchange(
            //"http://localhost:$port/api/customers/tickets",
            "/api/customers/tickets",
            HttpMethod.GET,
            HttpEntity(null, headers),
            String::class.java
        )

        val resTicket = JSONObject(response.body).getJSONArray("content").getJSONObject(0)
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
        Assertions.assertEquals("IN_PROGRESS", resTicket.getString("ticketState"))
        Assertions.assertEquals(product.serialNumber.toString(), resTicket.getString("serialNumber"))
        Assertions.assertEquals(expertId.toString(), resTicket.getString("expertId"))
        Assertions.assertEquals(customerId.toString(), resTicket.getString("customerId"))
        Assertions.assertEquals(ticket.description, resTicket.getString("description"))
        Assertions.assertEquals(formatter.format(ticket.lastModified), resTicket.getString("lastModified"))
        Assertions.assertEquals(formatter.format(ticket.creationDate), resTicket.getString("creationDate"))
        Assertions.assertEquals(ticketId!!.toLong(), resTicket.getLong("ticketId"))
    }


    @Test /** GET /api/customers/tickets */
    fun `Fail get all tickets without login`() {
        val url = "/api/customers/tickets"
        val response = utilityFunctions.restTemplate.getForEntity(url, String::class.java)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response?.statusCode)
    }


    //@Ignore
    @Test /** POST /api/customers/tickets POST*/
    fun `Successful creation of a new ticket`() {
        val customer = utilityFunctions.createTestCustomer()
        val customerId = customerRepository.save(customer).id

        val product = utilityFunctions.createTestProduct(customer)
        productRepository.save(product).getId()


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
            "http://localhost:$port/api/customers/tickets",
            HttpMethod.POST,
            HttpEntity(jsonRequest.toString(), headers),
            String::class.java
        )


        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.CREATED,response.statusCode)
        val body = JSONObject(response.body)
        Assertions.assertEquals("OPEN", body.getString("ticketState"))
        Assertions.assertEquals("myDescription",body.getString("description"))
        Assertions.assertEquals(product.serialNumber.toString(), body.getString("serialNumber"))
        Assertions.assertEquals(customerId.toString(), body.getString("customerId"))
        Assertions.assertEquals(0,body.optInt("expertId"))

    }


    @Test /** GET /api/customers/tickets/:ticketId */
    fun successGetASingleTicketsOfACustomer() {
        val customer = utilityFunctions.createTestCustomer()
        val customerId = customerRepository.save(customer).id

        val expert = utilityFunctions.createTestExpert()
        val expertId = expertRepository.save(expert).id

        val product = utilityFunctions.createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = utilityFunctions.createTestTicket(customer, product, null, TicketState.OPEN)
        val ticketId = ticketRepository.save(ticket).getId()

        /* customer login */
        val accessToken = utilityFunctions.customerLogin()

        /* crafting the request */
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        /* retrieving all the tickets */
        val response: ResponseEntity<String> = utilityFunctions.restTemplate.exchange(
            "http://localhost:$port/api/customers/tickets/${ticketId}",
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
        Assertions.assertEquals(expertId.toString(), resTicket.getString("expertId"))
        Assertions.assertEquals(customerId.toString(), resTicket.getString("customerId"))
        Assertions.assertEquals(ticket.description, resTicket.getString("description"))
        Assertions.assertEquals(formatter.format(ticket.lastModified), resTicket.getString("lastModified"))
        Assertions.assertEquals(formatter.format(ticket.creationDate), resTicket.getString("creationDate"))
        Assertions.assertEquals(ticketId!!.toLong(), resTicket.getLong("ticketId"))
    }

    @Test
    /** GET /api/customers/tickets/:ticketId */
    fun failGetASingleTicketOfANonExistentCustomer(){
        val ticketId = (1000..2000).random()
        val url = "/api/customers/tickets/${ticketId}"
        val response = utilityFunctions.restTemplate
            .getForEntity(url, String::class.java)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response?.statusCode)
    }

    @Test
    /** GET /api/customers/tickets/:ticketId */
    fun failGetANonExistentTicketOfACustomer(){
        val customer = utilityFunctions.createTestCustomer()
        val customerId = customerRepository.save(customer).id

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


    @Test /** PATCH /api/customers/tickets/:ticketId/reopen */
    fun successReopenClosedTicket(){
        val customer = utilityFunctions.createTestCustomer()
        val customerId = customerRepository.save(customer).id

        val expert = utilityFunctions.createTestExpert()
        val expertId = expertRepository.save(expert).id

        val product = utilityFunctions.createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = utilityFunctions.createTestTicket(customer,product, expert, TicketState.CLOSED)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = utilityFunctions.createTestManager()
        val managerId = managerRepository.save(manager).id

        //Where is the ticket being closed?

        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/${ticketId}/reopen",
            HttpMethod.PATCH,
            null,
            String::class.java
        )

        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)

        val actualTicket = ticketRepository.getReferenceById(ticketId!!)
        Assertions.assertEquals(TicketState.REOPENED, actualTicket.state)
    }

    @Test /** PATCH /api/customers/tickets/:ticketId/reopen */
    fun successReopenResolvedTicket(){
        val customer = utilityFunctions.createTestCustomer()
        val customerId = customerRepository.save(customer).id

        val expert = utilityFunctions.createTestExpert()
        val expertId = expertRepository.save(expert).id

        val product = utilityFunctions.createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = utilityFunctions.createTestTicket(customer,product, expert, TicketState.RESOLVED)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = utilityFunctions.createTestManager()
        val managerId = managerRepository.save(manager).id

        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/${ticketId}/reopen",
            HttpMethod.PATCH,
            null,
            String::class.java
        )

        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)

        val actualTicket = ticketRepository.getReferenceById(ticketId!!)
        Assertions.assertEquals(TicketState.REOPENED, actualTicket.state)
    }

    @Test /** PATCH /api/customers/tickets/:ticketId/reopen */
    fun failReopenAlreadyOpenTicket(){
        val customer = utilityFunctions.createTestCustomer()
        val customerId = customerRepository.save(customer).id

        val expert = utilityFunctions.createTestExpert()
        val expertId = expertRepository.save(expert).id

        val product = utilityFunctions.createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = utilityFunctions.createTestTicket(customer,product, null, TicketState.OPEN)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = utilityFunctions.createTestManager()
        val managerId = managerRepository.save(manager).id

        val url = "/api/managers/tickets/${ticketId}/relieveExpert"
        val response = utilityFunctions.restTemplate.exchange(
            url,
            HttpMethod.PATCH,
            null,
            String::class.java
        )
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.CONFLICT, response?.statusCode)

        val actualTicket = ticketRepository.getReferenceById(ticketId!!)
        Assertions.assertEquals(TicketState.OPEN, actualTicket.state)
    }

    //FIX
    @Test /** PATCH /api/customers/tickets/:ticketId/compileSurvey */
    fun successCompileSurvey(){
        val customer = utilityFunctions.createTestCustomer()
        val customerId = customerRepository.save(customer).id

        val expert = utilityFunctions.createTestExpert()
        val expertId = expertRepository.save(expert).id

        val product = utilityFunctions.createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = utilityFunctions.createTestTicket(customer,product, expert, TicketState.RESOLVED)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = utilityFunctions.createTestManager()
        val managerId = managerRepository.save(manager).id

        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/${ticketId}/compileSurvey",
            HttpMethod.PATCH,
            null,
            String::class.java
        )
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        val actualTicket = ticketRepository.getReferenceById(ticketId!!)
        Assertions.assertEquals(actualTicket.state, TicketState.CLOSED)
    }

    @Test /** PATCH /api/customers/tickets/:ticketId/compileSurvey */
    fun failCompileSurveyTicketAlreadyClosed() {
        val customer = utilityFunctions.createTestCustomer()
        val customerId = customerRepository.save(customer).id

        val expert = utilityFunctions.createTestExpert()
        val expertId = expertRepository.save(expert).id

        val product = utilityFunctions.createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = utilityFunctions.createTestTicket(customer,product, expert, TicketState.CLOSED)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = utilityFunctions.createTestManager()
        val managerId = managerRepository.save(manager).id

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val requestObject = JSONObject()
        requestObject.put("expertId", expert.id)

        val response = utilityFunctions.restTemplate.exchange(
            "/api/customers/tickets/${ticketId}/compileSurvey",
            HttpMethod.PATCH,
            HttpEntity(requestObject.toString(), headers),
            String::class.java
        )

        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.CONFLICT, response.statusCode)

        val actualTicket = ticketRepository.getReferenceById(ticketId!!)
        Assertions.assertEquals(TicketState.CLOSED, actualTicket.state)
    }




    //EXPERTS

    @Test /** GET /api/experts/tickets*/
    fun successGetAllTicketsOfAnExpert() {
        val customer = utilityFunctions.createTestCustomer()
        val customerId = customerRepository.save(customer).id

        val expert = utilityFunctions.createTestExpert()
        val expertId = expertRepository.save(expert).id

        val product = utilityFunctions.createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)
        val ticketId = ticketRepository.save(ticket).getId()

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
        Assertions.assertEquals("OPEN", resTicket.getString("ticketState"))
        Assertions.assertEquals(product.serialNumber.toString(), resTicket.getString("serialNumber"))
        Assertions.assertEquals(expertId.toString(), resTicket.getString("expertId"))
        Assertions.assertEquals(customerId.toString(), resTicket.getString("customerId"))
        Assertions.assertEquals(ticket.description, resTicket.getString("description"))
        Assertions.assertEquals(formatter.format(ticket.lastModified.toString()), resTicket.getString("lastModified"))
        Assertions.assertEquals(formatter.format(ticket.creationDate), resTicket.getString("creationDate"))
        Assertions.assertEquals(ticketId!!.toLong(), resTicket.getLong("ticketId"))
    }


    @Test /** GET /api/experts/tickets */
    fun failGetAllTicketsOfANonExistentExpert() {
        val url = "/api/experts/tickets"
        val response = utilityFunctions.restTemplate.getForEntity(url, String::class.java)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response?.statusCode)
    }


    @Ignore
    @Test /** GET /api/experts/tickets/:ticketId */
    fun successGetASingleTicketsOfAnExpert() {
        val customer = utilityFunctions.createTestCustomer()
        val customerId = customerRepository.save(customer).id

        val expert = utilityFunctions.createTestExpert()
        val expertId = expertRepository.save(expert).id

        val product = utilityFunctions.createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = utilityFunctions.createTestTicket(customer, product, expert, TicketState.IN_PROGRESS)
        val ticketId = ticketRepository.save(ticket).getId()


        /* customer login */
        val accessToken = utilityFunctions.customerLogin()

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
        val body = response.body
        val resTicket = JSONObject(body)
        Assertions.assertEquals("OPEN", resTicket.getString("ticketState"))
        Assertions.assertEquals(product.serialNumber.toString(), resTicket.getString("serialNumber"))
        Assertions.assertEquals(expertId.toString(), resTicket.getString("expertId"))
        Assertions.assertEquals(customerId.toString(), resTicket.getString("customerId"))
        Assertions.assertEquals(ticket.description, resTicket.getString("description"))
        Assertions.assertEquals(formatter.format(ticket.lastModified), resTicket.getString("lastModified"))
        Assertions.assertEquals(formatter.format(ticket.creationDate), resTicket.getString("creationDate"))
        Assertions.assertEquals(ticketId!!.toLong(), resTicket.getLong("ticketId"))

    }

    @Test /** GET /api/experts/tickets/:ticketId */
    fun failGetASingleTicketOfAnExpertWithoutLogin(){
        val ticketId = (1000..2000).random()
        val url = "/api/experts/tickets/${ticketId}"
        val response = utilityFunctions.restTemplate
            .getForEntity(url, String::class.java)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response?.statusCode)
    }

    @Test /** GET /api/experts/tickets/:ticketId */
    fun failGetANonExistentTicketOfAnExpert(){
        val expert = utilityFunctions.createTestExpert()
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



    @Test /** PATCH /api/experts/tickets/:ticketId/resolve */
    fun successResolveInProgressTicket(){
        val customer = utilityFunctions.createTestCustomer()
        val customerId = customerRepository.save(customer).id

        val transactionDefinition = DefaultTransactionDefinition()
        val transactionStatus: TransactionStatus = transactionManager.getTransaction(transactionDefinition)


        val expert = utilityFunctions.createTestExpert()

        try {
            expertRepository.save(expert)
            transactionManager.commit(transactionStatus)
        } catch (e: Exception) {
            transactionManager.rollback(transactionStatus)
            throw e
        }


        println("******EXPERTIDS**********")
        expertRepository.findAll().map { it -> println(it.id) }
        println("end")

        val product = utilityFunctions.createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = utilityFunctions.createTestTicket(customer,product, expert, TicketState.IN_PROGRESS)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = utilityFunctions.createTestManager()
        val managerId = managerRepository.save(manager).id

        /* expert login */
        val accessToken = utilityFunctions.expertLogin()

        /* crafting the request */
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        // 404 no expert found by UUID but it was actually inserted in the db
        val response = utilityFunctions.restTemplate.exchange(
            "/api/experts/tickets/${ticketId}/resolve",
            HttpMethod.PATCH,
            HttpEntity(null, headers),
            String::class.java
        )

        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)

        val actualTicket = ticketRepository.getReferenceById(ticketId!!)
        Assertions.assertEquals(TicketState.RESOLVED, actualTicket.state)
    }

    @Test /** PATCH /api/experts/tickets/:ticketId/resolve */
    fun failResolveClosedTicket(){
        val customer = utilityFunctions.createTestCustomer()
        val customerId = customerRepository.save(customer).id

        val expert = utilityFunctions.createTestExpert()
        val expertId = expertRepository.save(expert).id

        val product = utilityFunctions.createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = utilityFunctions.createTestTicket(customer,product, expert, TicketState.CLOSED)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = utilityFunctions.createTestManager()
        val managerId = managerRepository.save(manager).id

        val response = utilityFunctions.restTemplate.exchange(
            "/api/experts/tickets/${ticketId}/resolve",
            HttpMethod.PATCH,
            null,
            String::class.java
        )

        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.CONFLICT, response.statusCode)

        val actualTicket = ticketRepository.getReferenceById(ticketId!!)
        Assertions.assertEquals(TicketState.CLOSED, actualTicket.state)
    }


    @Test /** PATCH '/api/experts/tickets/:ticketId/close' */
    fun successCloseReopenedTicketByExpert(){
        val customer = utilityFunctions.createTestCustomer()
        val customerId = customerRepository.save(customer).id

        val expert = utilityFunctions.createTestExpert()
        val expertId = expertRepository.save(expert).id

        val product = utilityFunctions.createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = utilityFunctions.createTestTicket(customer,product, expert, TicketState.REOPENED)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = utilityFunctions.createTestManager()
        val managerId = managerRepository.save(manager).id

        val response = utilityFunctions.restTemplate.exchange(
            "/api/experts/tickets/${ticketId}/close",
            HttpMethod.PATCH,
            null,
            String::class.java
        )
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        val actualTicket = ticketRepository.getReferenceById(ticketId!!)
        Assertions.assertEquals(actualTicket.state, TicketState.CLOSED)
    }

    @Test /** PATCH '/api/experts/tickets/:ticketId/close' */
    fun failCloseAlreadyClosedTicketByExpert() {
        val customer = utilityFunctions.createTestCustomer()
        val customerId = customerRepository.save(customer).id

        val expert = utilityFunctions.createTestExpert()
        val expertId = expertRepository.save(expert).id

        val product = utilityFunctions.createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = utilityFunctions.createTestTicket(customer,product, expert, TicketState.CLOSED)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = utilityFunctions.createTestManager()
        val managerId = managerRepository.save(manager).id

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val requestObject = JSONObject()
        requestObject.put("expertId", expert.id)

        val response = utilityFunctions.restTemplate.exchange(
            "/api/experts/tickets/${ticketId}/close",
            HttpMethod.PATCH,
            HttpEntity(requestObject.toString(), headers),
            String::class.java
        )

        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.CONFLICT, response.statusCode)

        val actualTicket = ticketRepository.getReferenceById(ticketId!!)
        Assertions.assertEquals(TicketState.CLOSED, actualTicket.state)
    }


    //MANAGERS
}