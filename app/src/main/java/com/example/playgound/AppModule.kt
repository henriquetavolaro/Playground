package com.example.playgound

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object AppModule {


    @Singleton
    @Provides
    fun provideFirebaseAuth() : FirebaseAuth {
        return Firebase.auth
    }

    @Singleton
    @Provides
    fun provideAuthenticationClass() : Authentication {
        return Authentication()
    }

}