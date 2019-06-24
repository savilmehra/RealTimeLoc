package com.easy_locater.realtimeloc

import com.google.firebase.database.IgnoreExtraProperties


@IgnoreExtraProperties
class LoggedUsers  {


    var id: String = ""

    // Default constructor required for calls to
    // DataSnapshot.getValue(UserNew.class)
    constructor() {}

    constructor( id: String) {

        this.id = id
    }
}