package com.example.ryte.Logic

data class Captain(
    val category:String
    , val email:String,
     val OnCity:String,
    val licence:String, val model:String, val name:String, val nationalId:String, val phone:String, val zone:String,val Status:String,val image:String
) {
    constructor():this("","","","","","","","","","","")


}