package com.example.playgound

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import javax.inject.Inject

class FirestoreDatasource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    fun addClient(client: Client) {
        firestore.collection("CLIENTS")
            .document(client.id)
            .set(client, SetOptions.merge())
    }

}