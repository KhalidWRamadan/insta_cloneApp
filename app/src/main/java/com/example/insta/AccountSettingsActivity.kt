package com.example.insta

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.insta.Model.User
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.activity_account_settings.*
import kotlinx.android.synthetic.main.activity_account_settings.bio_profile_frag
import kotlinx.android.synthetic.main.activity_account_settings.full_name_profile_frag

class AccountSettingsActivity : AppCompatActivity() {


    private lateinit var firebaseUser: FirebaseUser
    private var checker = ""
    private var myUrl = ""
    private var imageUri: Uri? = null
    private var storageProfilePicRef: StorageReference? = null


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_account_settings)




        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }else{
            window.statusBarColor = Color.WHITE
        }

        firebaseUser = FirebaseAuth.getInstance().currentUser!!
        storageProfilePicRef = FirebaseStorage.getInstance().reference.child("Profile Pictures")


        logout_btn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this@AccountSettingsActivity, SigninActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            finish()
        }


        change_image_text_btn.setOnClickListener {
            checker = "clicked"
            CropImage.activity().setAspectRatio(1, 1)
                .start(this@AccountSettingsActivity)
        }


        save_infor_profile_btn.setOnClickListener {
            if (checker == "clicked") {
                uploadImageAndUpdateInfo()
            } else {
                updateUserInfoOnly()
            }
        }
        userInfo()


    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {

            val result = CropImage.getActivityResult(data)

            imageUri = result.uri
            profile_image_view_profile_frag.setImageURI(imageUri)
        }
    }

    private fun updateUserInfoOnly() {
        when {
            full_name_profile_frag.text.toString() == "" -> {
                Toast.makeText(
                    this,
                    R.string.Please_write_full_name_first,
                    Toast.LENGTH_LONG
                ).show()
            }
            Username_profile_frag.text.toString() == "" -> {
                Toast.makeText(
                    this,
                    "Please write user name first",
                    Toast.LENGTH_LONG
                ).show()
            }
            bio_profile_frag.text.toString() == "" -> {
                Toast.makeText(
                    this,
                    "Please write Bio first",
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {
                val userRef =
                    FirebaseDatabase.getInstance().reference.child("Users")

                val userMap = HashMap<String, Any>()
                userMap["fullname"] = full_name_profile_frag.text.toString().toLowerCase()
                userMap["username"] = Username_profile_frag.text.toString().toLowerCase()
                userMap["bio"] = bio_profile_frag.text.toString().toLowerCase()

                userRef.child(firebaseUser.uid).updateChildren(userMap)

                Toast.makeText(
                    this,
                    "Account Information has been updated successfully",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(this@AccountSettingsActivity, MainActivity::class.java)
                startActivity(intent)
                finish()

            }
        }


    }

    private fun userInfo() {
        val userRef =
            FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").getReference().child("Users").child(firebaseUser.uid)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {

                if (p0.exists()) {
                    val user = p0.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile)
                        .into(profile_image_view_profile_frag)
                    Username_profile_frag.setText(user!!.getUsername())
                    full_name_profile_frag.setText(user!!.getFullname())
                    bio_profile_frag.setText(user!!.getBio())
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    private fun uploadImageAndUpdateInfo() {

        when {
            imageUri == null -> Toast.makeText(
                this,
                "Please select image first",
                Toast.LENGTH_LONG
            ).show()

            TextUtils.isEmpty(full_name_profile_frag.text.toString()) ->
                Toast.makeText(
                    this,
                    "Please write full name first",
                    Toast.LENGTH_LONG
                ).show()

            Username_profile_frag.text.toString() == "" -> {
                Toast.makeText(
                    this,
                    "Please write user name first",
                    Toast.LENGTH_LONG
                ).show()
            }
            bio_profile_frag.text.toString() == "" -> {
                Toast.makeText(
                    this,
                    "Please write Bio first",
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Account Settings")
                progressDialog.setMessage("Please wait , we are updating your profile....")
                progressDialog.show()

                val fileref = storageProfilePicRef!!.child(firebaseUser!!.uid + ".jpg")
                var uploadeTask: StorageTask<*>
                uploadeTask = fileref.putFile(imageUri!!)
                uploadeTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                            progressDialog.dismiss()
                        }
                    }

                    return@Continuation fileref.downloadUrl
                }).addOnCompleteListener(OnCompleteListener<Uri> { task ->
                    if (task.isSuccessful) {
                        val downloadUrl = task.result
                        myUrl = downloadUrl.toString()

                        val ref = FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference.child("Users")

                        val userMap = HashMap<String, Any>()
                        userMap["fullname"] =
                            full_name_profile_frag.text.toString().toLowerCase()
                        userMap["username"] =
                            Username_profile_frag.text.toString().toLowerCase()
                        userMap["bio"] = bio_profile_frag.text.toString().toLowerCase()
                        userMap["image"] = myUrl

                        ref.child(firebaseUser.uid).updateChildren(userMap)
                        Toast.makeText(
                            this,
                            "Account Information has been updated successfully",
                            Toast.LENGTH_LONG
                        ).show()
                        val intent =
                            Intent(this@AccountSettingsActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                        progressDialog.dismiss()

                    } else {
                        progressDialog.dismiss()
                    }

                })

            }

        }
    }
}