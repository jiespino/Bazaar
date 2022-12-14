package com.example.bazaar.Storage

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.example.bazaar.Model.UserPost
import com.example.bazaar.ui.createPost.Category
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class DBHelper() {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val collectionRootBazaarPosts = "BazaarPosts"

    private fun elipsizeString(string: String) : String {
        if(string.length < 10)
            return string
        return string.substring(0..9) + "..."
    }

    fun fetchPostsForSearch(location: List<String>, chosenCategory: Category, searchCriteria: List<Any>, postsList: MutableLiveData<List<UserPost>>) {
        dbFetchPostsForSearch(location, chosenCategory, searchCriteria, postsList)
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
    private fun dbFetchPostsForSearch(location: List<String>, category: Category, searchCriteria: List<Any>, userPostList: MutableLiveData<List<UserPost>>) {
        db.collection(collectionRootBazaarPosts)
            .orderBy("timeStamp", Query.Direction.DESCENDING)
            .limit(10000)
            .get()
            .addOnSuccessListener { result ->
                Log.d(javaClass.simpleName, "all user posts  fetch ${result!!.documents.size}")
                // NB: This is done on a background thread

                val totalUserPosts = mutableListOf<UserPost>()

                result.documents.mapNotNull {
                    val userPost = it.toObject(UserPost::class.java)
                    if (meetsFilterCriteria(location, category, searchCriteria, userPost!!)) {
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
        db.collection(collectionRootBazaarPosts)
            .orderBy("timeStamp", Query.Direction.DESCENDING)
            .limit(10000)
            .get()
            .addOnSuccessListener { result ->
                Log.d(javaClass.simpleName, "all user posts  fetch ${result!!.documents.size}")
                // NB: This is done on a background thread
                val totalUserPosts = mutableListOf<UserPost>()
                result.documents.mapNotNull {
                    val userPost = it.toObject(UserPost::class.java)
                    if (userPost?.ownerUid == userUid) {
                        totalUserPosts.add(userPost)
                    }
                }
                userPostList.postValue(totalUserPosts)
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
        db.collection(collectionRootBazaarPosts)
            .document(userPost.firestoreID)
            .set(userPost)
            //EEE // XXX Writing a note
            .addOnSuccessListener {
                Log.d(
                    javaClass.simpleName,
                    "Note update \"${elipsizeString(userPost.title)}\" len ${pictureUUIDs.size} id: ${userPost.firestoreID}"
                )
                dbFetchUserPosts(userPost.ownerUid, userPostsList)
            }
            .addOnFailureListener { e ->
                Log.d(javaClass.simpleName, "Note update FAILED \"${elipsizeString(userPost.title)}\"")
                Log.w(javaClass.simpleName, "Error ", e)
            }
    }

    fun createUserPost(
        userPost: UserPost
    ) {
        // We can get ID locally
        // note.firestoreID = db.collection("allNotes").document().id
        db.collection(collectionRootBazaarPosts)
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
    fun deleteUserPost(
        userPost: UserPost,
        userPostsList: MutableLiveData<List<UserPost>>
    ) {
        db.collection(collectionRootBazaarPosts)
            .document(userPost.firestoreID)
            .delete()
            .addOnSuccessListener {
                Log.d(
                    javaClass.simpleName,
                    "Note delete \"${elipsizeString(userPost.title)}\" id: ${userPost.firestoreID}"
                )
                dbFetchUserPosts(userPost.ownerUid, userPostsList)
            }
            .addOnFailureListener { e ->
                Log.d(javaClass.simpleName, "Note deleting FAILED \"${elipsizeString(userPost.title)}\"")
                Log.w(javaClass.simpleName, "Error adding document", e)
            }
    }

    private fun meetsFilterCriteria(location: List<String>, category: Category, searchCriteria: List<Any>, userPost: UserPost): Boolean {

        val searchTerm = searchCriteria[0] as String

        if (!isSameLocation(location, userPost)) {
            return false
        }

        if (userPost.category != category.toString()) {
            return false
        }

        if (searchTerm.isNotEmpty()) {
            if (!userPost.title.contains(searchTerm, ignoreCase = true) && !userPost.description.contains(searchTerm, ignoreCase = true)) {
                return false
            }
        }

        val priceRange = searchCriteria[1] as List<Float>
        if (valueNotInRange(userPost.price, priceRange)) {
            return false
        }

        if (category == Category.APARTMENT) {
            val aptCriteria = searchCriteria[2] as List<*>

            val sqFtRange = aptCriteria[0] as List<Float>
            if (valueNotInRange(userPost.aptInfo?.squareFeet!!, sqFtRange)) {
                return false
            }

            val roomRange = aptCriteria[1] as List<Float>
            if (valueNotInRange(userPost.aptInfo?.rooms!!, roomRange)) {
                return false
            }

            val bathRange = aptCriteria[2] as List<Float>
            if (valueNotInRange(userPost.aptInfo?.baths!!, bathRange)) {
                return false
            }

        }
        return true
    }

    private fun valueNotInRange(value: Int, range: List<Float>): Boolean {
        val rangeMin = range[0]
        val rangeMax = range[1]

        if (value < rangeMin || value > rangeMax) {
            return true
        }

        return false
    }

    private fun isSameLocation(location: List<String>, userPost: UserPost): Boolean {

        val city = location[0]
        val state = location[1]
        val countryCode = location[2]

        if (userPost.city == city && userPost.state == state && userPost.countryCode == countryCode) {
            return true
        }

        return false

    }

}