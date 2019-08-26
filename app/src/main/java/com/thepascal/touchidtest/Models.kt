package com.thepascal.touchidtest

data class User(var email:String, var password:String){
    var firstName:String = ""
    var lastName:String = ""
}

fun getUser(email: String, password: String):User{
    return User(email, password)
}

fun main(){
    var user = User("something@gmail.com", "1234")
    user.lastName = "Arvee"
    user.firstName = "Pascal"

    println(user.firstName)
}