package com.example.playgound

import javax.inject.Inject

class AddClient @Inject constructor(
    private val firestore: FirestoreDatasource
) {

    suspend fun execute(client: Client){
        firestore.addClient(client)
    }

}