package com.final_project.server

import com.final_project.server.config.GlobalConfig
import com.final_project.server.repository.*
import com.final_project.ticketing.repository.TicketRepository
import dasniko.testcontainers.keycloak.KeycloakContainer
import org.junit.jupiter.api.*
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.*
import org.springframework.http.*
import org.springframework.test.context.*
import org.testcontainers.containers.PostgreSQLContainer
import org.springframework.boot.test.web.server.LocalServerPort
import org.testcontainers.junit.jupiter.*
import java.text.SimpleDateFormat


@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(Suite::class)
@Suite.SuiteClasses(
        //CustomerProductTest::class,
        TestCases::class
)
class ApplicationTests {
    @Autowired
    lateinit var utilityFunctions: UtilityFunctions

    // CONFIGURATION

    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:latest")

        @Container
        val keycloak = KeycloakContainer("quay.io/keycloak/keycloak:latest")
            .withRealmImportFile("keycloak/realm_v2.json")


        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.jpa.hibernate.ddl-auto") { "create-drop" }
            registry.add("POSTGRES_URL", postgres::getJdbcUrl)
            println(postgres::getJdbcUrl)


            /* keycloak container */
            val keycloakBaseUrl = keycloak.authServerUrl
            registry.add("keycloakBaseUrl") { keycloakBaseUrl }
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri") { keycloakBaseUrl + "realms/TicketingServiceRealm" }
            registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri") { keycloakBaseUrl + "realms/TicketingServiceRealm/protocol/openid-connect/certs" }
            registry.add("keycloakMappedPort") { (keycloak.getMappedPort(8080)).toString() }
            registry.add("keycloakHost") { keycloak.host }
        }
    }

    @Autowired
    private lateinit var globalConfig: GlobalConfig
    @LocalServerPort
    protected var port: Int = 8080
    @Autowired
    lateinit var ticketRepository: TicketRepository
    @Autowired
    lateinit var customerRepository: CustomerRepository
    @Autowired
    lateinit var productRepository: ProductRepository
    @Autowired
    lateinit var expertRepository: ExpertRepository
    @Autowired
    lateinit var managerRepository: ManagerRepository
    @Value("\${keycloakMappedPort}")
    lateinit var keycloakMappedPort:String
    @Value("\${keycloakHost}")
    lateinit var keycloakHost:String
    var formatter = SimpleDateFormat("yyyy-MM-dd")

    @BeforeEach
    fun setUp() {
        ticketRepository.deleteAll()
        productRepository.deleteAll()
        customerRepository.deleteAll()
        managerRepository.deleteAll()
        expertRepository.deleteAll()

        globalConfig.keycloakPort = keycloakMappedPort
        globalConfig.keycloakURL = keycloakHost
    }



    /*

    @Test *//** GET /api/managers/tickets*//*

    fun successGetAllTicketsOfAManager() {
        val customer = createTestCustomer()
        val customerId = customerRepository.save(customer).id

        val expert = createTestExpert()
        val expertId = expertRepository.save(expert).id

        val product = createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket =createTestTicket(customer, product, expert)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = createTestManager()
        managerRepository.save(manager).id

        *//* manager login *//*
        val accessToken = managerLogin()

        *//* crafting the request *//*
        val headers: MultiValueMap<String, String> = HttpHeaders().apply {
            add("Authorization", "Bearer $accessToken")
        }

        *//* retrieving all the tickets *//*
        val response: ResponseEntity<String> = restTemplate.exchange(
            "http://localhost:$port/api/managers/tickets",
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
        Assertions.assertEquals(ticket.lastModified.formatDate(), resTicket.getString("lastModified"))
        Assertions.assertEquals(ticket.creationDate.formatDate(), resTicket.getString("creationDate"))
        Assertions.assertEquals(ticketId!!.toLong(), resTicket.getLong("ticketId"))
    }

    @Test *//** GET /api/managers/tickets*//*
    fun failGetAllTicketsOfANonExistentManager() {

        val url = "/api/managers/tickets"
        val response = restTemplate
            .getForEntity(url, String::class.java)
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, response?.statusCode)
    }




    @Test
    *//** GET /api/managers/:managerId/tickets/:ticketId *//*
    fun successGetASingleTicketsOfAManager() {
        val customer = createTestCustomer()
        val customerId = customerRepository.save(customer).id

        val expert = createTestExpert()
        val expertId = expertRepository.save(expert).id

        val product = createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = createTestTicket(customer,product, expert)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = createTestManager()
        val managerId = managerRepository.save(manager).id

        val url = "/api/managers/${managerId}/tickets/${ticketId}"
        val response = restTemplate
            .getForEntity(url, String::class.java)

        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.OK, response?.statusCode)
        val body = response.body
        val resTicket = JSONObject(body)
        Assertions.assertEquals("OPEN", resTicket.getString("ticketState"))
        Assertions.assertEquals(product.serialNumber.toString(), resTicket.getString("serialNumber"))
        Assertions.assertEquals(expertId.toString(), resTicket.getString("expertId"))
        Assertions.assertEquals(customerId.toString(), resTicket.getString("customerId"))
        Assertions.assertEquals(ticket.description, resTicket.getString("description"))
        Assertions.assertEquals(ticket.lastModified.formatDate(), resTicket.getString("lastModified"))
        Assertions.assertEquals(ticket.creationDate.formatDate(), resTicket.getString("creationDate"))
        Assertions.assertEquals(ticketId!!.toLong(), resTicket.getLong("ticketId"))
    }

	@Test
    *//** PATCH /api/managers/:managerId/tickets/:ticketId/assign *//*
    fun successAssignmentOfATicket() {

        val customer = createTestCustomer()
        val customerId = customerRepository.save(customer).id

        val expert = createTestExpert()
        val expertId = expertRepository.save(expert).id

        val product = createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = createTestTicket(customer,product, expert)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = createTestManager()
        val managerId = managerRepository.save(manager).id

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val requestObject = JSONObject()
        requestObject.put("expertId", expertId.toString())

        val response = restTemplate.exchange(
            "/api/managers/${managerId}/tickets/${ticketId}/assign",
            HttpMethod.PATCH,
            HttpEntity(requestObject.toString(), headers),
            String::class.java
        )
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)

        val actualTicket = ticketRepository.getReferenceById(ticketId!!)
        Assertions.assertNotNull(actualTicket.expert)
        Assertions.assertEquals(actualTicket.expert!!.id, expertId )
        Assertions.assertEquals(actualTicket.state, TicketState.IN_PROGRESS)


    }

    @Test
    *//** PATCH /api/managers/:managerId/tickets/:ticketId/relieveExpert *//*
    fun successRelieveExpert(){
        val customer = createTestCustomer()
        val customerId = customerRepository.save(customer).id

        val expert = createTestExpert()
        val expertId = expertRepository.save(expert).id

        val product = createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = createTestTicket(customer,product, expert)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = createTestManager()
        val managerId = managerRepository.save(manager).id

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val requestObject = JSONObject()
        requestObject.put("expertId", expertId.toString())

        val response = restTemplate.exchange(
            "/api/managers/${managerId}/tickets/${ticketId}/relieveExpert",
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
    *//** PATCH /api/managers/:managerId/tickets/:ticketId/relieveExpert *//*
    fun failRelieveExpertWithNonExistentIds(){
        val manager = createTestManager()
        val managerId = managerRepository.save(manager).id
        val ticketId = (0..100).random()
        val url = "/api/managers/${managerId}/tickets/${ticketId}/relieveExpert"
        val response = restTemplate.exchange(
            "/api/managers/${managerId}/tickets/${ticketId}/relieveExpert",
            HttpMethod.PATCH,
            null,
            String::class.java
        )
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NOT_FOUND, response?.statusCode)
    }





    @Test *//** PATCH /api/managers/:managerId/tickets/:ticketId/close *//*
    fun successCloseOpenedTicketByManager(){
        val customer = createTestCustomer()
        val customerId = customerRepository.save(customer).id

        val expert = createTestExpert()
        val expertId = expertRepository.save(expert).id

        val product = createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = createTestTicket(customer,product, expert)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = createTestManager()
        val managerId = managerRepository.save(manager).id

        val response = restTemplate.exchange(
            "/api/managers/${managerId}/tickets/${ticketId}/close",
            HttpMethod.PATCH,
            null,
            String::class.java
        )
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
        val actualTicket = ticketRepository.getReferenceById(ticketId!!)
        Assertions.assertEquals(actualTicket.state, TicketState.CLOSED)
    }

    @Test *//** PATCH /api/managers/:managerId/tickets/:ticketId/close *//*
    fun failCloseAlreadyClosedTicketByManager() {
        val customer = createTestCustomer()
        val customerId = customerRepository.save(customer).id

        val expert = createTestExpert()
        val expertId = expertRepository.save(expert).id

        val product = createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = createTestTicket(customer,product, expert)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = createTestManager()
        val managerId = managerRepository.save(manager).id

        val response = restTemplate.exchange(
            "/api/managers/${managerId}/tickets/${ticketId}/close",
            HttpMethod.PATCH,
            null,
            String::class.java
        )

        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.CONFLICT, response.statusCode)

        val actualTicket = ticketRepository.getReferenceById(ticketId!!)
        Assertions.assertEquals(TicketState.CLOSED, actualTicket.state)
    }

    @Test *//** PATCH /api/managers/:managerId/tickets/:ticketId/resumeProgress *//*
    fun succeedResumeProgress() {
        val customer = createTestCustomer()
        val customerId = customerRepository.save(customer).id

        val expert = createTestExpert()
        val expertId = expertRepository.save(expert).id

        val product = createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = createTestTicket(customer,product, expert)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = createTestManager()
        val managerId = managerRepository.save(manager).id


        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val requestObject = JSONObject()
        requestObject.put("expertId", expert.id)

        val response = restTemplate.exchange(
            "/api/managers/${managerId}/tickets/${ticketId}/resumeProgress",
            HttpMethod.PATCH,
            HttpEntity(requestObject.toString(), headers),
            String::class.java
        )
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.NO_CONTENT, response.statusCode)

        val actualTicket = ticketRepository.getReferenceById(ticketId!!)
        Assertions.assertEquals(TicketState.IN_PROGRESS, actualTicket.state)

    }

    @Test *//** PATCH /api/managers/:managerId/tickets/:ticketId/resumeProgress *//*
    fun failResumeProgressAlreadyClosedTicket() {
        val customer = createTestCustomer()
        val customerId = customerRepository.save(customer).id

        val expert = createTestExpert()
        val expertId = expertRepository.save(expert).id

        val product = createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = createTestTicket(customer,product, expert)
        val ticketId = ticketRepository.save(ticket).getId()

        val manager = createTestManager()
        val managerId = managerRepository.save(manager).id

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        val requestObject = JSONObject()
        requestObject.put("expertId", expert.id)

        val response = restTemplate.exchange(
            "/api/managers/${managerId}/tickets/${ticketId}/resumeProgress",
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
    *//** PATCH /api/managers/tickets/:ticketId/remove*//*
    fun successRemoveTicket(){
        val customer = createTestCustomer()
        customerRepository.save(customer).id

        val expert = createTestExpert()
        val expertId = expertRepository.save(expert).id

        val product = createTestProduct(customer)
        productRepository.save(product).getId()

        val ticket = Ticket(
            TicketState.RESOLVED, customer, expert, "Description", product, mutableSetOf(),
            myDate(2020, 1, 1), myDate(2020, 1, 1)
        )
        val ticketId = ticketRepository.save(ticket).getId()!!

        val manager = createTestManager()
        val managerId = managerRepository.save(manager).id

        val response = restTemplate.exchange(
            "/api/managers/${managerId}/tickets/${ticketId}/remove",
            HttpMethod.DELETE,
            null,
            String::class.java
        )
        Assertions.assertNotNull(response)
        Assertions.assertEquals(HttpStatus.OK, response.statusCode)
    }*/


    // Resolve Open ticket (manager)
}