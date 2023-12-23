package com.example.knockitriderapp.Model

class UserModel(
    var uid: String,
    var name : String,
    var email: String,
    var profile: String,
    var wishlistSize: String,
    var cartSize: String,
    var number: String,
    var city: String,
    var state: String,
    var country: String,
    var pincode: String,
    var address: String,
    var latitude: Float,
    var longitude: Float,
    var productListSize: String,
    var notificationSize: String
) {
    constructor() : this("","","","","","","","","","","","", 1F,1F,"","")
}