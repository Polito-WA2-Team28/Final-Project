package com.final_project.server

import com.final_project.server.config.GlobalConfig
import com.final_project.server.repository.*
import com.final_project.ticketing.repository.TicketRepository
import com.final_project.ticketing.repository.TicketStateEvolutionRepository
import dasniko.testcontainers.keycloak.KeycloakContainer
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.*
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.*
import org.springframework.http.*
import org.springframework.test.context.*
import org.testcontainers.containers.PostgreSQLContainer
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.data.jpa.repository.Modifying
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import org.testcontainers.junit.jupiter.*
import java.text.SimpleDateFormat


@Testcontainers
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = ["spring.config.name=application-test"]
)
@RunWith(Suite::class)
@Suite.SuiteClasses(
        //CustomerProductTest::class,
        TestCases::class
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
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
    @Autowired
    lateinit var ticketStateEvolutionRepository: TicketStateEvolutionRepository
    @Value("\${keycloakMappedPort}")
    lateinit var keycloakMappedPort:String
    @Value("\${keycloakHost}")
    lateinit var keycloakHost:String
    var formatter = SimpleDateFormat("yyyy-MM-dd")
    @Autowired
    lateinit var entityManager: EntityManager
    @Autowired
    lateinit var transactionManager: PlatformTransactionManager

    @BeforeEach
    @Modifying
    fun setUp() {
        ticketStateEvolutionRepository.deleteAll()
        ticketRepository.deleteAll()
        productRepository.deleteAll()
        customerRepository.deleteAll()
        managerRepository.deleteAll()
        expertRepository.deleteAll()

        globalConfig.keycloakPort = keycloakMappedPort
        globalConfig.keycloakURL = keycloakHost
    }




    // Resolve Open ticket (manager)
}