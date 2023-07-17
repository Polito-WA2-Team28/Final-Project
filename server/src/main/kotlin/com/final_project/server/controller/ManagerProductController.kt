package com.final_project.server.controller

import com.final_project.security.config.SecurityConfig
import com.final_project.server.dto.ProductDTO
import com.final_project.server.exception.Exception
import com.final_project.server.service.ManagerServiceImpl
import com.final_project.server.service.ProductServiceImpl
import com.final_project.ticketing.dto.PageResponseDTO
import com.final_project.ticketing.dto.PageResponseDTO.Companion.toDTO
import com.final_project.ticketing.dto.computePageSize
import com.final_project.ticketing.util.Nexus
import io.micrometer.observation.annotation.Observed
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@Observed
class ManagerProductController @Autowired constructor(
    val productService: ProductServiceImpl,
    val managerService: ManagerServiceImpl,
    val securityConfig: SecurityConfig
) {

    val logger: Logger = LoggerFactory.getLogger(CustomerProductController::class.java)

    @GetMapping("/api/managers/products")
    @ResponseStatus(HttpStatus.OK)
    fun getProducts(
        @RequestParam("pageNo", defaultValue = "1") pageNo: Int
    ): PageResponseDTO<ProductDTO> {

        val nexus: Nexus = Nexus(managerService)


        /* running checks... */
        val managerId = UUID.fromString(securityConfig.retrieveUserClaim(SecurityConfig.ClaimType.SUB))
        nexus
            .setEndpointForLogger("/api/managers/products")
            .assertManagerExists(managerId)

        /* crafting pageable request */
        var result: PageResponseDTO<ProductDTO> = PageResponseDTO()
        val page: Pageable = PageRequest.of(pageNo-1, result.computePageSize())

        /* return result to client */
        result = productService.getManagerProductsWithPaging(page).toDTO()
        return result
    }

    @GetMapping("/api/managers/products/{productId}")
    @ResponseStatus(HttpStatus.OK)
    fun getProductById(@PathVariable("productId") productId: Long): ProductDTO? {
        val products = productService.managerGetProductById(productId)

        if (products == null) {
            logger.error("Endpoint: /api/managers/products/$productId Error: No product matched the requested Id")
            throw Exception.ProductNotFoundException("No product matched the requested Id")
        }
        return products
    }
}