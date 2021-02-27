package com.example.ryte.Logic

data class Customer(val Credit:String?,val Image:String ,val Name:String,val  Phone:String,val uid:String,val Status:String) {
    constructor():this(null,"","","","","")
}