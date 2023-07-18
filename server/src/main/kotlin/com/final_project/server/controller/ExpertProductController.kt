package com.final_project.server.controller

import com.final_project.security.config.SecurityConfig
import com.final_project.server.dto.ProductDTO
import com.final_project.server.exception.Exception
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
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Observed
class ExpertProductController @Autowired constructor(
    val productService: ProductServiceImpl,
    val expertService: ProductServiceImpl,
    val securityConfig: SecurityConfig
) {

    val logger: Logger = LoggerFactory.getLogger(CustomerProductController::class.java)

    @GetMapping("/api/experts/products")
    @ResponseStatus(HttpStatus.OK)
    fun getProducts(
        @RequestParam("pageNo", defaultValue = "1") pageNo: Int
    ): PageResponseDTO<ProductDTO>{

        val nexus: Nexus = Nexus(expertService, productService)

        /* running checks... */
        val expertId = UUID.fromString(securityConfig.retrieveUserClaim(SecurityConfig.ClaimType.SUB))
        nexus
            .setEndpointForLogger("/api/experts/products")
            .assertExpertExists(expertId)

        /* crafting pageable request */
        var result: PageResponseDTO<ProductDTO> = PageResponseDTO()
        val page: Pageable = PageRequest.of(pageNo-1, result.computePageSize())

        /* return result to client */
        result = productService.getManagerProductsWithPaging(page).toDTO()
        /* TODO: eventually remove this, expert does not need to retrieve the products */

        return result
    }

    @GetMapping("/api/experts/products/{productId}")
    @ResponseStatus(HttpStatus.OK)
    fun getProductById(@PathVariable("productId") productId: Long): ProductDTO? {
        val expertId = securityConfig.retrieveUserClaim(SecurityConfig.ClaimType.SUB)
        val products = productService.expertGetProductById(UUID.fromString(expertId), productId)

        if (products == null) {
            logger.error("Endpoint: /api/experts/products/$productId Error: No product matched the requested Id")
            throw Exception.ProductNotFoundException("No product matched the requested Id")
        }
        return products
    }
}