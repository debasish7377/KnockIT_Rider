package com.example.knockitriderapp.Model

class NotificationModel(
    var id: String,
    var title : String,
    var description: String,
    var timeStamp: Long,
    var read: String,
    var payment: String
) {
    constructor() : this("","","",1,"", "")
}