package com.final_project.server.service

import com.final_project.server.dto.ProductDTO
import com.final_project.server.model.Customer
import com.final_project.server.model.Product
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import java.util.UUID

interface ProductService {
    fun getManagerProductsWithPaging(pageable: Pageable): Page<ProductDTO>

    // fun getExpertProductsWithPaging(expertId: UUID, pageable: Pageable): Page<ProductDTO>

    fun getCustomerProductsWithPaging(customerId: UUID, pageable: Pageable): Page<ProductDTO>


    fun managerGetProductById(productId:Long) : ProductDTO?

    fun expertGetProductById(expertId:UUID, productId:Long) : ProductDTO?

    fun customerGetProductById(customerId: UUID, productId: Long): ProductDTO?

    fun customerGetProductBySerialNumber(customerId:UUID, serialNumber:UUID): ProductDTO?

    fun registerProduct(customerId:UUID, productId:Long, serialNumber: UUID)
}