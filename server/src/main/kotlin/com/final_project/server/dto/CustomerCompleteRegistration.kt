package com.final_project.server.dto



import java.util.*

class CustomerCompleteRegistration(
    var id:UUID,
    var name:String,
    val surname:String,
    val username:String,
    val registrationDate: Date,
    val birthDate: Date,
    val email:String,
    val phoneNumber:String
) {

}


fun CustomerFormRegistration.toCompleteCustomer(id: UUID, profile:CustomerFormRegistration): CustomerCompleteRegistration {
    val registrationDate = Date()

    return CustomerCompleteRegistration(id, profile.name, profile.surname, profile.username, registrationDate,
                                        profile.birthDate, profile.email, profile.phoneNumber)
}
