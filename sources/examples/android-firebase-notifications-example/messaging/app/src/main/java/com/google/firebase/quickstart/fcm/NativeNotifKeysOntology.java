package com.google.firebase.quickstart.fcm;

import com.google.gson.annotations.SerializedName;

public class NativeNotifKeysOntology {
    public NativeNotifKeysOntology(NativeNotifKeys nativeNotifKeys) {
        this.nativeNotifKeys = nativeNotifKeys;
    }
    @SerializedName("NativeNotifKeys")
    NativeNotifKeys nativeNotifKeys;
}
