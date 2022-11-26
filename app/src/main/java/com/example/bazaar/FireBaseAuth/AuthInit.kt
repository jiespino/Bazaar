package com.example.bazaar.FireBaseAuth

import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

// https://firebase.google.com/docs/auth/android/firebaseui
class AuthInit(firebaseAuthLiveData: FirestoreAuthLiveData, signInLauncher: ActivityResultLauncher<Intent>) {
    companion object {
        private const val TAG = "AuthInit"
        fun setDisplayName(displayName : String, firebaseAuthLiveData: FirestoreAuthLiveData) {
            Log.d(TAG, "XXX profile change request")
            //SSS
            val user = FirebaseAuth.getInstance().currentUser ?: return
            val profileUpdates = UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build()

            user.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        firebaseAuthLiveData.updateUser()
                    } else {
                        Log.d(TAG,
                            "XXX profile update failed ${task.exception?.toString()}"
                        )
                    }
                }
            //EEE // XXX Write me. User is attempting to update display name. Get the profile updates (see android doc)
            // UserProfileChangeRequest.Builder()
        }
    }

    init {
        val user = FirebaseAuth.getInstance().currentUser
        if(user == null) {
            Log.d(TAG, "XXX user null")
            // Choose authentication providers
            val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build())

            // Create and launch sign-in intent
            //SSS
            val signInIntent = AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build()
            signInLauncher.launch(signInIntent)
            //EEE // XXX Write me. Set authentication providers and start sign-in for user
            // setIsSmartLockEnabled(false) solves some problems
        } else {
            Log.d(TAG, "XXX user ${user.displayName} email ${user.email}")
            firebaseAuthLiveData.updateUser()
        }
    }
}