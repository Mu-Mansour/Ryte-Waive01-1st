package com.example.ryte.Logic

data class PendingRideModule(
    var CancelableWithFees:String?,
    var Cstiswithme:String?,
    var Customer:String?,
    var Captain:String?,
    var Uid:String?,
    var StartingTime:String?,
    var CaptainArrived:String?,
    var Distance:String?,
    var WaitingTime:String?,
    var Price:String?,
    var vat:String?,
) {

    constructor():this(null,null,null,null,null,null,null,null,null,null,null)
}
