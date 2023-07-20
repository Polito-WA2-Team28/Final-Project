package com.final_project.server.service

import com.final_project.server.dto.ProductDTO
import com.final_project.server.dto.toDTO
import com.final_project.server.model.Product
import org.springframework.stereotype.Service
import com.final_project.server.repository.ProductRepository
import org.springframework.transaction.annotation.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import java.util.*


@Service
class ProductServiceImpl @Autowired constructor(
    private val productRepository: ProductRepository
): ProductService {

    override fun getManagerProductsWithPaging(pageable: Pageable): Page<ProductDTO> {
        return productRepository.findAll(pageable).map { it.toDTO() }
    }

    /*override fun getExpertProductsWithPaging(expertId: UUID, pageable: Pageable): Page<ProductDTO> {
        return productRepository.findAllByExpertId(expertId, pageable).map { it.toDTO() }
    }*/

    override fun getCustomerProductsWithPaging(customerId: UUID, pageable: Pageable): Page<ProductDTO> {
        return productRepository.findAllByOwnerId(customerId, pageable).map { it.toDTO() }
    }

    @Transactional
    override fun registerProduct(customerId:UUID, productId: Long, serialNumber: UUID) {
        return productRepository.registerProduct(customerId, productId, serialNumber)
    }

    override fun customerGetProductBySerialNumber(customerId:UUID, serialNumber: UUID): ProductDTO? {
        return productRepository.customerFindProductBySerialNumber(customerId, serialNumber)?.toDTO()
    }

    override fun customerGetProductById(customerId: UUID, productId: Long): ProductDTO? {
        return productRepository.customerFindProductById(customerId, productId)?.toDTO()
    }

    override fun getProductBySerialNumber(serialNumber: UUID): ProductDTO? {
        return productRepository.findBySerialNumber(serialNumber)?.toDTO()
    }

    override fun getProductBySerialNumberAndId(serialNumber: UUID, productId: Long): ProductDTO? {
        return productRepository.findProductBySerialNumberAndId(serialNumber, productId)?.toDTO()
    }


    override fun managerGetProductById(productId: Long): ProductDTO? {
        return productRepository.findByIdOrNull(productId)?.toDTO()
    }

    override fun expertGetProductById(expertId: UUID, productId: Long): ProductDTO? {
        return productRepository.expertFindProductById(expertId, productId)?.toDTO()
    }
}