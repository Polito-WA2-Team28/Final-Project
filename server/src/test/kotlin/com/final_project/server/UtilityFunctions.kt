package com.final_project.server

import com.final_project.security.dto.UserCredentialsDTO
import com.final_project.server.model.Customer
import com.final_project.server.model.Expert
import com.final_project.server.model.Manager
import com.final_project.server.model.Product
import com.final_project.server.repository.CustomerRepository
import com.final_project.server.repository.ExpertRepository
import com.final_project.server.repository.ManagerRepository
import com.final_project.server.repository.ProductRepository
import com.final_project.ticketing.dto.MessageObject
import com.final_project.ticketing.dto.toDTO
import com.final_project.ticketing.model.Attachment
import com.final_project.ticketing.model.Message
import com.final_project.ticketing.model.Ticket
import com.final_project.ticketing.model.toModel
import com.final_project.ticketing.repository.AttachmentRepository
import com.final_project.ticketing.repository.MessageRepository
import com.final_project.ticketing.repository.TicketRepository
import com.final_project.ticketing.util.ExpertiseFieldEnum
import com.final_project.ticketing.util.TicketState
import org.apache.http.entity.ContentType
import org.json.JSONObject
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.context.annotation.Configuration
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.util.MimeType
import org.springframework.web.multipart.MultipartFile
import java.text.SimpleDateFormat
import java.util.*

@Configuration
class UtilityFunctions {

    @Autowired
    lateinit var restTemplate: TestRestTemplate
    @Autowired
    private lateinit var customerRepository: CustomerRepository
    @Autowired
    private lateinit var productRepository: ProductRepository
    @Autowired
    private lateinit var expertRepository: ExpertRepository
    @Autowired
    private lateinit var ticketRepository: TicketRepository
    @Autowired
    private lateinit var managerRepository: ManagerRepository
    @Autowired
    private lateinit var messageRepository: MessageRepository
    @Autowired
    private lateinit var attachmentRepository: AttachmentRepository

    private fun myDate(year: Int, month: Int, day: Int): Date {
        return Date(year - 1900, month - 1, day)
    }
    private fun Date.formatDate(): String {
        return SimpleDateFormat("yyyy-MM-dd").format(this)
    }

    private fun createFirstCustomer(): Customer {
        val customer = Customer(
            UUID.fromString("0ae24126-7590-4e62-9f05-199f61824ed6"),
            "Mario",
            "Rossi",
            "mariorossi",
            myDate(2022, 1, 1),
            myDate(1990, 1, 1),
            "mario.rossi@mail.com",
            "0123456789"
        )
        customerRepository.save(customer)
        return customer
    }

    private fun createSecondCustomer(): Customer {
        val customer = Customer(
            UUID.fromString("b73f15f3-31fb-49a8-9369-b0a2e334226c"),
            "John",
            "Doe",
            "johndoe",
            myDate(2022, 2, 2),
            myDate(1991, 2, 2),
            "johndoe@example.com",
            "9876543210"
        )
        customerRepository.save(customer)
        return customer
    }

    fun createTestExpert(): Expert {
        val uuid = UUID.fromString("6e2f3411-1f7b-4da4-9128-2bac562b3687")
        val expert: Expert =  Expert(uuid, "expert@ticketingservice.it", mutableSetOf(ExpertiseFieldEnum.APPLIANCES))
        expertRepository.save(expert)
        return expert
    }


    fun createTestCustomer(name: String, surname: String): Customer? {
        val key = "$name $surname"
        return when (key) {
            "Mario Rossi" -> createFirstCustomer()
            "John Doe" -> createSecondCustomer()
            else -> null
        }
    }

    fun createTestProduct(customer: Customer): Product {
        val product: Product = Product(0, UUID.randomUUID(),"Iphone", "15", true, customer)
        val productId = productRepository.save(product).id
        product.id = productId
        return product
    }

    fun createTestTicket(customer: Customer, product: Product, expert: Expert?, state: TicketState): Ticket {
        val ticket: Ticket = Ticket(
            state, customer, expert, "Description", product, mutableSetOf(),
            myDate(2020, 1, 1), myDate(2020, 1, 1)
        )
        ticketRepository.save(ticket)
        return ticket
    }

    fun createTestManager(): Manager {
        val manager: Manager = Manager(UUID.fromString("3eb963ee-1404-45e1-bef2-9583d4b6243f"),"manager@ticketingservice.it")
        managerRepository.save(manager)
        return manager
    }

    fun createAttachments(): MutableSet<Attachment> {
        val attachments: MutableSet<Attachment> = mutableSetOf<Attachment>().apply {
            add(Attachment("PoliTo", ContentType.WILDCARD.mimeType, "44d34d89-1bdd-4942-963b-6777090fc6be_attach2"))
        }
        return attachments
    }

    fun addMessage(ticket: Ticket, customer: Customer, text: String, files: MutableSet<Attachment>?): String? {
        val attachments: MutableSet<Attachment> = files ?: mutableSetOf()
        val message: Message = Message(text, myDate(2023, 1, 1), customer.username, attachments, ticket)
        val ticket = ticketRepository.findByIdOrNull(ticket.getId() ?: 0)
            ?: fail("Test failed because no ticket was created in the database (sending messages).")
        messageRepository.save(message)
        return files?.first()?.fileUniqueName
    }


    fun customerLogin():String{return login("customer-test-1", "test")}

    fun customer2Login(): String { return login("customer-test-2", "test") }

    fun expertLogin():String{return login("expert-1", "password1")}

    fun managerLogin():String{return login("manager-1","password")}

    fun login(username: String, password: String): String {

        /* crafting the request */
        val credentials = UserCredentialsDTO(username, password)
        val body = HttpEntity(credentials)

        /* login */
        val response = restTemplate.postForEntity<String>(
            "/api/auth/login",
            body,
            HttpMethod.POST
        )
        println("**********${response.statusCode}**********")

        /* retrieving the access token */
        return JSONObject(response.body)["accessToken"].toString()
    }
}