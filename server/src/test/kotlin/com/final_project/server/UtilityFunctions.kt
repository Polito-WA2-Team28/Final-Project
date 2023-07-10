package com.final_project.server

import com.final_project.security.dto.UserCredentialsDTO
import com.final_project.server.model.Customer
import com.final_project.server.model.Expert
import com.final_project.server.model.Manager
import com.final_project.server.model.Product
import com.final_project.ticketing.model.Ticket
import com.final_project.ticketing.util.ExpertiseFieldEnum
import com.final_project.ticketing.util.TicketState
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import java.text.SimpleDateFormat
import java.util.*

@Configuration
class UtilityFunctions {
    @Autowired
    lateinit var restTemplate: TestRestTemplate

    fun myDate(year: Int, month: Int, day: Int): Date {
        return Date(year - 1900, month - 1, day)
    }
    fun Date.formatDate(): String {
        return SimpleDateFormat("yyyy-MM-dd").format(this)
    }
    fun createTestCustomer(): Customer {
        return Customer(
            UUID.fromString("0ae24126-7590-4e62-9f05-199f61824ed6"),
            "Mario", "Rossi", "mariorossi",
            myDate(2022, 1, 1), myDate(1990, 1, 1),
            "mario.rossi@mail.com", "0123456789"
        )
    }
    fun createTestExpert(): Expert {
        val uuid = UUID.fromString("6e2f3411-1f7b-4da4-9128-2bac562b3687")
        return Expert(uuid, "expert@ticketingservice.it", mutableSetOf(ExpertiseFieldEnum.APPLIANCES))
    }
    fun createTestProduct(customer: Customer): Product {
        return Product(0, UUID.randomUUID(),"Iphone", "15", true, customer)
    }

    fun updateTestProduct(product: Product, id: Long): Product {
        product.id = id
        return product
    }

    fun createTestTicket(customer: Customer, product: Product, expert: Expert?, state: TicketState): Ticket {
        return Ticket(
            state, customer, expert, "Description", product, mutableSetOf(),
            myDate(2020, 1, 1), myDate(2020, 1, 1)
        )
    }



    fun createTestManager(): Manager {
        return Manager(UUID.fromString("3eb963ee-1404-45e1-bef2-9583d4b6243f"),"manager@ticketingservice.it")
    }
    fun customerLogin():String{return login("customer-test-1", "test")}

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