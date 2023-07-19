package com.final_project.ticketing.util

import com.final_project.security.service.KeycloakService
import com.final_project.server.config.GlobalConfig
import com.final_project.server.dto.CustomerDTO
import com.final_project.server.dto.ExpertDTO
import com.final_project.server.dto.ManagerDTO
import com.final_project.server.dto.ProductDTO
import com.final_project.server.exception.Exception
import com.final_project.server.repository.ExpertRepository
import com.final_project.server.repository.ProductRepository
import java.util.UUID
import com.final_project.server.service.*
import com.final_project.ticketing.dto.AttachmentDTO
import com.final_project.ticketing.dto.TicketDTO
import com.final_project.ticketing.dto.TicketStateEvolutionDTO
import com.final_project.ticketing.dto.TicketSurveyDTO
import com.final_project.ticketing.exception.TicketException
import com.final_project.ticketing.service.TicketService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import java.io.File

@Configuration
class Nexus (vararg services: Any) {

    /* services */
    private lateinit var customerService: CustomerService
    private lateinit var expertService: ExpertService
    private lateinit var managerService: ManagerService
    private lateinit var ticketService: TicketService
    private lateinit var productService: ProductService
    private lateinit var fileStorageService: FileStorageService
    private lateinit var keycloakService: KeycloakService

    init {
        for (service in services) {
            when (service) {
                is CustomerService -> customerService = service
                is ExpertService -> expertService = service
                is ManagerService -> managerService = service
                is TicketService -> ticketService = service
                is ProductService -> productService = service
                is FileStorageService -> fileStorageService = service
                is KeycloakService -> keycloakService = service
            }
        }
    }


    /* logging */
    private val endpointHolder: ThreadLocal<String> = ThreadLocal()
    private val logger: Logger = LoggerFactory.getLogger(Nexus::class.java)

    /* assertions */
    var customer: CustomerDTO? = null
    var expert: ExpertDTO? = null
    var manager: ManagerDTO? = null
    var ticket: TicketDTO? = null
    var product: ProductDTO? = null
    var attachment: ResponseEntity<ByteArray>? = null
    var ticketStatusLifecycle: List<TicketStateEvolutionDTO> = emptyList()
    var validationErrors: List<FieldError> = emptyList()

    fun setEndpointForLogger(endpoint: String): Nexus {
        endpointHolder.set(endpoint)
        return this
    }

    fun assertCustomerExists(customerId: UUID): Nexus {
        println(customerService.toString())
        this.customer = customerService.getCustomerById(customerId) ?: run {
            logger.error("Endpoint: ${endpointHolder.get()} Error: No customer profile found with this UUID.")
            throw Exception.CustomerNotFoundException("No customer profile found with this UUID.")
        }
        return this
    }


    fun assertExpertExists(expertId: UUID): Nexus {
        this.expert = expertService.getExpertById(expertId) ?: run {
            logger.error("Endpoint: ${endpointHolder.get()} Error: No expert profile found with this UUID.")
            throw Exception.ExpertNotFoundException("No expert profile found with this UUID.")
        }
        return this
    }

    fun assertManagerExists(managerId: UUID): Nexus {
        this.manager = managerService.getManagerById(managerId) ?: run {
            logger.error("Endpoint: ${endpointHolder.get()} Error: No manager profile found with this UUID.")
            throw Exception.ManagerNotFoundException("No manager profile found with this UUID.")
        }
        return this
    }

    fun assertTicketNonNull(ticket: TicketDTO?): Nexus {
        ticket ?: run {
            logger.error("Endpoint: ${endpointHolder.get()} Error: Ticket creation error.")
            throw TicketException.TicketCreationException("Ticket creation error.")
        }
        return this
    }

    fun assertTicketExists(ticketId: Long): Nexus {
        this.ticket = ticketService.getTicketDTOById(ticketId) ?: run {
            logger.error("Endpoint: ${endpointHolder.get()} Error: Ticket not found.")
            throw TicketException.TicketNotFoundException("Ticket not found.")
        }
        return this
    }

    fun assertTicketOwnership(): Nexus {
        if (this.ticket!!.customerId != this.customer!!.id) {
            logger.error("Endpoint: ${endpointHolder.get()} Error: This ticket belongs to another customer.")
            throw TicketException.TicketForbiddenException("This ticket belongs to another customer.")
        }
        return this
    }

    fun assertTicketAssignment(): Nexus {
        if (this.ticket!!.expertId == null || this.ticket!!.expertId != this.expert!!.id) {
            logger.error("Endpoint: ${endpointHolder.get()} Error: Expert not assigned to this ticket.")
            throw Exception.ExpertNotFoundException("Expert not assigned to this ticket.")
        }
        return this
    }

    fun assertTicketStatus(allowedStates: Set<TicketState>): Nexus {
        if (!allowedStates.contains(this.ticket!!.ticketState)) {
            logger.error("Endpoint: ${endpointHolder.get()} Error: Invalid ticket status for this operation..")
            throw TicketException.TicketInvalidOperationException("Invalid ticket status for this operation.")
        }
        return this
    }

    fun assertProductExists(serialNumber: UUID): Nexus {
        this.product = productService.customerGetProductBySerialNumber(this.customer!!.id, serialNumber) ?: run {
            logger.error("Endpoint: ${endpointHolder.get()} Error: Not Found.")
            throw Exception.ProductNotFoundException("Not Found.")
        }
        return this
    }

    fun assertCustomerlessProductExists(serialNumber: UUID): Nexus {
        this.product = productService.getProductBySerialNumber(serialNumber) ?: run {
            logger.error("Endpoint: ${endpointHolder.get()} Error: Not Found.")
            throw Exception.ProductNotFoundException("Not Found.")
        }
        return this
    }

    fun assertProductOwnership(): Nexus {
        if (this.product!!.owner != this.customer!!.id) {
            logger.error("Endpoint: ${endpointHolder.get()} Error: Customer is not the owner of this product.")
            throw Exception.CustomerNotOwnerException("Customer is not the owner of this product.")
        }
        return this
    }

    fun assertFileExists(filename: String): Nexus {
        try {
            this.attachment = fileStorageService.getAttachmentFile(filename)
        } catch (e: Exception) {
            logger.error("Endpoint: ${endpointHolder.get()} Error: This attachment does not exist.")
            throw Exception.FileNotExistException("This attachment does not exist.")
        }
        return this
    }

    fun assertValidationResult(endpoint:String, br: BindingResult): Nexus {
        println("${br.hasErrors()} ${br.fieldErrors}")
        if(br.hasErrors()){
            println("error")
            val invalidFields = br.fieldErrors.map { it.field }
            throw Exception.ValidationException("Endpoint: $endpoint, Error: Invalid fields:", invalidFields)
        }

        return this
    }

    /* operations */
    fun assignTicketToExpert(ticketId: Long, expertId: UUID): Nexus {
        this.ticket!!.assignExpert(expertId)
        this.ticket!!.changeState(TicketState.IN_PROGRESS)
        ticketService.assignTicketToExpert(ticketId, expertId)
        ticketService.changeTicketStatus(ticketId, TicketState.IN_PROGRESS)
        return this
    }

    fun relieveExpertFromTicket(ticketId: Long): Nexus {
        this.ticket!!.relieveExpert()
        this.ticket!!.changeState(TicketState.OPEN)
        ticketService.relieveExpertFromTicket(ticketId)
        ticketService.changeTicketStatus(ticketId, TicketState.OPEN)
        return this
    }

    fun closeTicket(ticketId: Long): Nexus {
        this.ticket!!.changeState(TicketState.CLOSED)
        ticketService.changeTicketStatus(ticketId, TicketState.CLOSED)
        return this
    }

    fun updateTicketSurvey(ticketId: Long, ticketSurvey:TicketSurveyDTO): Nexus {
        this.ticket!!.updateSurvey(ticketSurvey.survey)
        ticketService.updateTicketSurvey(ticketId, ticketSurvey)
        return this
    }

    fun resolveTicket(ticketId: Long): Nexus {
        this.ticket!!.changeState(TicketState.RESOLVED)
        ticketService.changeTicketStatus(ticketId, TicketState.RESOLVED)
        return this
    }

    fun reopenTicket(ticketId: Long): Nexus {
        this.ticket!!.changeState(TicketState.REOPENED)
        ticketService.changeTicketStatus(ticketId, TicketState.REOPENED)
        return this
    }

    fun retrieveTicketStateLifecycle(ticketId: Long): Nexus {
        this.ticketStatusLifecycle = ticketService.retrieveTicketStateLifecycle(ticketId)
        return this
    }

}