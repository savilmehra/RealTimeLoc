package com.easy_locater.realtimeloc

import com.google.firebase.database.IgnoreExtraProperties


@IgnoreExtraProperties
class UserNew {

    var lat: String = ""
    var long: String = ""

    // Default constructor required for calls to
    // DataSnapshot.getValue(UserNew.class)
    constructor() {}

    constructor(lat: String, long: String) {
        this.lat = lat
        this.long = long
    }
}
