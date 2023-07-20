package com.final_project.server.controller

import com.final_project.security.config.SecurityConfig
import com.final_project.server.dto.ProductDTO
import com.final_project.server.service.ProductServiceImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import com.final_project.server.exception.Exception
import com.final_project.server.dto.RegisterProductDTO
import com.final_project.server.service.CustomerServiceImpl
import com.final_project.ticketing.dto.PageResponseDTO
import com.final_project.ticketing.dto.PageResponseDTO.Companion.toDTO
import com.final_project.ticketing.dto.TicketDTO
import com.final_project.ticketing.dto.computePageSize
import com.final_project.ticketing.util.Nexus
import io.micrometer.observation.annotation.Observed
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.validation.BindingResult
import org.springframework.web.bind.annotation.ResponseStatus
import java.util.*

@RestController
@Observed
class CustomerProductController @Autowired constructor (
    val productService: ProductServiceImpl,
    val customerService: CustomerServiceImpl,
    val securityConfig: SecurityConfig
) {

    val logger: Logger = LoggerFactory.getLogger(CustomerProductController::class.java)

    @GetMapping("/api/customers/products")
    @ResponseStatus(HttpStatus.OK)
    fun getProducts(
        @RequestParam("pageNo", defaultValue = "1") pageNo: Int
    ): PageResponseDTO<ProductDTO> {

        val nexus: Nexus = Nexus(customerService, productService)

        /* running checks... */
        val customerId = UUID.fromString(securityConfig.retrieveUserClaim(SecurityConfig.ClaimType.SUB))
        nexus
            .setEndpointForLogger("/api/customers/products")
            .assertCustomerExists(customerId)

        /* crafting pageable request */
        var result: PageResponseDTO<ProductDTO> = PageResponseDTO()
        val page: Pageable = PageRequest.of(pageNo-1, result.computePageSize())

        /* return result to client */
        result = productService.getCustomerProductsWithPaging(customerId, page).toDTO()
        return result
    }

    @GetMapping("/api/customers/products/{productId}")
    @ResponseStatus(HttpStatus.OK)
    fun getProductById(@PathVariable("productId") productId: Long): ProductDTO? {
        val customerId = securityConfig.retrieveUserClaim(SecurityConfig.ClaimType.SUB)
        val products = productService.customerGetProductById(UUID.fromString(customerId), productId)

        if (products == null) {
            logger.error("Endpoint: /api/customers/products/$productId Error: No product matched the requested Id")
            throw Exception.ProductNotFoundException("No product matched the requested Id")
        }
        return products
    }

    @PatchMapping("/api/customers/products/registerProduct")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun registerProduct(@RequestBody @Valid productIds: RegisterProductDTO,
                        br: BindingResult
    ){
        val nexus: Nexus = Nexus(productService, customerService)
        val customerId = securityConfig.retrieveUserClaim(SecurityConfig.ClaimType.SUB)
        /* Checking errors */
        nexus
            .setEndpointForLogger("/api/customers/products/registerProduct")
            .assertValidationResult("/api/customers/products/registerProduct", br)
            .assertCustomerExists(UUID.fromString(customerId))
            .assertCustomerlessProductExists(productIds.serialNumber, productIds.productId)



        return productService.registerProduct(UUID.fromString(customerId), productIds.productId, productIds.serialNumber)
    }
}