package com.example.insta.Adapter


import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.example.insta.Model.Story
import com.example.insta.Model.User
import com.example.insta.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import jp.shts.android.storiesprogressview.StoriesProgressView
import kotlinx.android.synthetic.main.activity_story.*

class StoryActivity : AppCompatActivity(), StoriesProgressView.StoriesListener {
    var currentUserId: String = ""
    var userId: String = ""


    var imagesList: List<String>? = null
    var storyIdsList: List<String>? = null

    var storiesProgreeView: StoriesProgressView? = null
    var counter = 0

    var pressTime = 0L
    var limit = 500L

    @SuppressLint("ClickableViewAccessibility")
    private val onTouchListener = View.OnTouchListener { v, event ->
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

                pressTime = System.currentTimeMillis()
                storiesProgreeView!!.pause()
                return@OnTouchListener false

            }

            MotionEvent.ACTION_UP -> {

                var now = System.currentTimeMillis()
                storiesProgreeView!!.resume()
                return@OnTouchListener limit < now - pressTime

            }
        }

        false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story)

        currentUserId = FirebaseAuth.getInstance().currentUser!!.uid
        userId = intent.getStringExtra("userId").toString()
        storiesProgreeView = findViewById(R.id.stories_progress)




        layout_seen.visibility = View.GONE
        story_delete.visibility = View.GONE

        if (userId == currentUserId) {
            layout_seen.visibility = View.VISIBLE
            story_delete.visibility = View.VISIBLE

        }

        getStories(userId!!)
        userInfo(userId!!)

        val reverse: View = findViewById(R.id.reverse)
        reverse.setOnClickListener {
            storiesProgreeView!!.reverse()
        }
        reverse.setOnTouchListener(onTouchListener)


        val skip: View = findViewById(R.id.skip)
        skip.setOnClickListener {
            storiesProgreeView!!.skip()
        }
        skip.setOnTouchListener(onTouchListener)

        seen_number.setOnClickListener {
            val intent = Intent(this@StoryActivity, ShowUsersActivity::class.java)
            intent.putExtra("id", userId)
            intent.putExtra("storyid", storyIdsList!![counter])
            intent.putExtra("title", "views")
            startActivity(intent)


        }

        story_delete.setOnClickListener {

            val ref =
                FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference.child("Story").child(userId!!).child(storyIdsList!![counter])
            ref.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful){
                    Toast.makeText(this@StoryActivity, "Deleted...",Toast.LENGTH_SHORT).show()
                }
            }

        }


    }


    private fun getStories(userId: String) {

        imagesList = ArrayList()
        storyIdsList = ArrayList()

        val ref =
            FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference.child("Story").child(userId!!)

        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                (imagesList as ArrayList<String>).clear()
                (storyIdsList as ArrayList<String>).clear()
                for (snapshot in snapshot.children) {
                    val story: Story? = snapshot.getValue<Story>(Story::class.java)
                    val timeCurrent = System.currentTimeMillis()
                    if (timeCurrent > story!!.getTimeStart() && timeCurrent < story.getTimeEnd()) {

                        (imagesList as ArrayList<String>).add(story.getImageUrl())
                        (storyIdsList as ArrayList<String>).add(story.getStoryId())

                    }
                }

                storiesProgreeView!!.setStoriesCount((imagesList as ArrayList<String>).size)
                storiesProgreeView!!.setStoryDuration(6000L)
                storiesProgreeView!!.setStoriesListener(this@StoryActivity)
                storiesProgreeView!!.startStories(counter)
                Picasso.get().load(imagesList!!.get(counter)).placeholder(R.drawable.profile)
                    .into(image_story)
                addViewToStory(storyIdsList!!.get(counter))
                seenNumber(storyIdsList!!.get(counter))


            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

    }


    private fun userInfo(userId: String) {
        val userRef = FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference.child("Users").child(userId)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {
                    val user = p0.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(story_profile_image)
                    story_username.text = user.getUsername()

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun addViewToStory(storyId: String) {

        FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference.child("Story").child(userId!!).child(storyId)
            .child("views").child(currentUserId).setValue(true)


    }

    private fun seenNumber(storyId: String) {

        val ref =
            FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference.child("Story").child(userId!!).child(storyId)
                .child("views")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {

                seen_number.text = "" + dataSnapshot.childrenCount

            }

            override fun onCancelled(error: DatabaseError) {

            }
        })


    }

    override fun onComplete() {

        finish()


    }

    override fun onPrev() {

        if (counter - 1 < 0 ) return

        Picasso.get().load(imagesList!![--counter]).placeholder(R.drawable.profile)
            .into(image_story)
        seenNumber(storyIdsList!![counter])

    }

    override fun onNext() {

        Picasso.get().load(imagesList!![++counter]).placeholder(R.drawable.profile)
            .into(image_story)
        addViewToStory(storyIdsList!![counter])
        seenNumber(storyIdsList!![counter])

    }

    override fun onDestroy() {
        super.onDestroy()
        storiesProgreeView!!.destroy()

    }

    override fun onRestart() {
        super.onRestart()

        storiesProgreeView!!.resume()


    }

    override fun onPause() {
        super.onPause()
        storiesProgreeView!!.pause()
    }
}