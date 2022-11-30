package com.example.bazaar.Storage

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.bazaar.Model.UserPost
import com.example.bazaar.ui.createPost.Category
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class DBHelper() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val collectionRootAllPosts = "allPosts"
    private val collectionRootUserPosts = "userPosts"
    private val collectionRootSingleUserPosts = "SingleUserPosts"

    private fun elipsizeString(string: String) : String {
        if(string.length < 10)
            return string
        return string.substring(0..9) + "..."
    }

    fun fetchPostsForSearch(location: List<String>, chosenCategory: Category, postsList: MutableLiveData<List<UserPost>>) {
        dbFetchPostsForSearch(location, chosenCategory, postsList)
    }

    fun fetchInitialUserPosts(userUid: String, userPostsList: MutableLiveData<List<UserPost>>) {
        dbFetchUserPosts(userUid, userPostsList)
    }
    /////////////////////////////////////////////////////////////
    // Interact with Firestore db
    // https://firebase.google.com/docs/firestore/query-data/get-data
    //
    // If we want to listen for real time updates use this
    // .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
    // But be careful about how listener updates live data
    // and noteListener?.remove() in onCleared
    private fun dbFetchPostsForSearch(location: List<String>, category: Category, userPostList: MutableLiveData<List<UserPost>>) {
        db.collection(collectionRootAllPosts)
            .document(location.joinToString(","))
            .collection(category.toString())
            .orderBy("timeStamp", Query.Direction.DESCENDING)
            .limit(1000)
            .get()
            .addOnSuccessListener { result ->
                Log.d(javaClass.simpleName, "all user posts  fetch ${result!!.documents.size}")
                // NB: This is done on a background thread

                Log.e("XXX", result.documents.toString())
                val totalUserPosts = mutableListOf<UserPost>()

                result.documents.mapNotNull {
                    val userPost = it.toObject(UserPost::class.java)
                    if (meetsFilterCriteria(userPost!!)) {
                        totalUserPosts.add(userPost)
                    }
                }

                userPostList.postValue(totalUserPosts)
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "all user posts fetch FAILED ", it)
            }
    }

    private fun dbFetchUserPosts(userUid: String, userPostList: MutableLiveData<List<UserPost>>) {
        db.collection(collectionRootUserPosts)
            .document(userUid)
            .collection(collectionRootSingleUserPosts)
            .orderBy("timeStamp", Query.Direction.DESCENDING)
            .limit(1000)
            .get()
            .addOnSuccessListener { result ->
                Log.d(javaClass.simpleName, "all user posts  fetch ${result!!.documents.size}")
                // NB: This is done on a background thread
                userPostList.postValue(result.documents.mapNotNull {
                    it.toObject(UserPost::class.java)
                })
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "all user posts fetch FAILED ", it)
            }
    }

    // After we successfully modify the db, we refetch the contents to update our
    // live data.  Hence the dbFetchNotes() calls below.
    fun updateUserPost(
        userPost: UserPost,
        userPostsList: MutableLiveData<List<UserPost>>
    ) {
        val pictureUUIDs = userPost.pictureUUIDs
        //SSS
        db.collection(collectionRootAllPosts)
            .document(userPost.firestoreID)
            .set(userPost)
            //EEE // XXX Writing a note
            .addOnSuccessListener {
                Log.d(
                    javaClass.simpleName,
                    "Note update \"${elipsizeString(userPost.title)}\" len ${pictureUUIDs.size} id: ${userPost.firestoreID}"
                )
                //dbFetchUserPosts(userPostsList)
            }
            .addOnFailureListener { e ->
                Log.d(javaClass.simpleName, "Note update FAILED \"${elipsizeString(userPost.title)}\"")
                Log.w(javaClass.simpleName, "Error ", e)
            }
    }

    fun createUserPost(
        userPost: UserPost,
        location: List<String>,
        chosenCategory: Category
    ) {
        // We can get ID locally
        // note.firestoreID = db.collection("allNotes").document().id
        Log.d(
            "XXX",
            "Note CREETE TEST WHAT \"${elipsizeString(userPost.title)}\" id: ${userPost.firestoreID}"
        )
        db.collection(collectionRootAllPosts)
            .document(location.joinToString(","))
            .collection(chosenCategory.toString())
            .add(userPost)
            .addOnSuccessListener {
                Log.d(
                    "XXX",
                    "category post create \"${elipsizeString(userPost.title)}\" id: ${userPost.firestoreID}"
                )
            }
            .addOnFailureListener { e ->
                Log.d("XXX", "category post FAILED \"${elipsizeString(userPost.title)}\"")
                Log.d("XXX", "Error ", e)
            }

        db.collection(collectionRootUserPosts)
            .document(userPost.ownerUid)
            .collection(collectionRootSingleUserPosts)
            .add(userPost)
            .addOnSuccessListener {
                Log.d(
                    "XXX",
                    "Single post create \"${elipsizeString(userPost.title)}\" id: ${userPost.firestoreID}"
                )
            }
            .addOnFailureListener { e ->
                Log.d("XXX", "Single post create FAILED \"${elipsizeString(userPost.title)}\"")
                Log.d("XXX", "Error ", e)
            }
    }

    fun removeUserPost(
        userPost: UserPost,
        userPostsList: MutableLiveData<List<UserPost>>
    ) {
        db.collection(collectionRootAllPosts)
            .document(userPost.firestoreID)
            .delete()
            .addOnSuccessListener {
                Log.d(
                    javaClass.simpleName,
                    "Note delete \"${elipsizeString(userPost.title)}\" id: ${userPost.firestoreID}"
                )
                //dbFetchUserPosts(userPostsList)
            }
            .addOnFailureListener { e ->
                Log.d(javaClass.simpleName, "Note deleting FAILED \"${elipsizeString(userPost.title)}\"")
                Log.w(javaClass.simpleName, "Error adding document", e)
            }
    }

    private fun meetsFilterCriteria(userPost: UserPost): Boolean {
        return true
    }
}