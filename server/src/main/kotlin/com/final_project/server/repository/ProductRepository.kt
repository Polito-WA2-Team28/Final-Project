package com.final_project.server.repository

import com.final_project.server.model.Product
import com.final_project.ticketing.model.Ticket
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProductRepository : CrudRepository<Product, Long>, JpaRepository<Product, Long> {

    fun findAllByOwnerId(ownerId: UUID, pageable: Pageable): Page<Product>

    fun findBySerialNumber(serialNumber: UUID): Product?


    @Query("SELECT p FROM Product p WHERE p.owner.id = :customerId AND p.id = :productId")
    fun customerFindProductById(customerId: UUID, productId:Long):Product?


    @Query("SELECT p FROM Product p WHERE p.owner.id = :customerId AND p.serialNumber = :serialNumber")
    fun customerFindProductBySerialNumber(customerId: UUID, serialNumber:UUID):Product?


    @Query("SELECT p FROM Product p, Ticket t WHERE t.expert.id = :expertId AND t.product.id = p.id")
    fun expertFindProducts(expertId:UUID):List<Product>


    @Query("SELECT p FROM Product p, Ticket t WHERE t.expert.id = :expertId AND t.product.id = p.id AND p.id = :productId")
    fun expertFindProductById(expertId:UUID, productId:Long):Product?

    @Modifying
    @Query("UPDATE Product p SET p.registered = true, p.owner.id = :ownerId WHERE p.id = :productId AND p.serialNumber = :serialNumber")
    fun registerProduct(ownerId:UUID, productId:Long, serialNumber: UUID)
}