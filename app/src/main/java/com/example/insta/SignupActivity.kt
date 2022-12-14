package com.example.insta

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_signup.*

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        signin_link_btn.setOnClickListener {
            startActivity(Intent(this, SigninActivity::class.java))
        }
        signup_btn.setOnClickListener {
        CreateAccount()
    }
    }

    private fun CreateAccount() {
        val fullName = fullname_signup.text.toString()
        val userName = username_signup.text.toString()
        val email = email_signup.text.toString()
        val password = password_signup.text.toString()
        when{
            TextUtils.isEmpty(fullName) -> Toast.makeText(this,"Fullname is required",Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(userName) -> Toast.makeText(this,"Username is required",Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(email) -> Toast.makeText(this,"Email is required",Toast.LENGTH_LONG).show()
            TextUtils.isEmpty(password) -> Toast.makeText(this,"Password is required",Toast.LENGTH_LONG).show()

            else -> {
                val progressDialog = ProgressDialog(this@SignupActivity)
                progressDialog.setTitle("SignUp")
                progressDialog.setMessage("Please wait this may take a while..")
                progressDialog.setCanceledOnTouchOutside(false)
                progressDialog.show()
                val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener {task ->
                        if (task.isSuccessful)
                        {
                            saveUserInfo(fullName,userName,email,progressDialog)
                        }
                        else
                        {
                            val message = task.exception!!.toString()
                            Toast.makeText(this,"Error $message",Toast.LENGTH_LONG).show()
                            mAuth.signOut()
                            progressDialog.dismiss()
                        }
                    }
            }
        }
    }

    private fun saveUserInfo(fullName: String, userName: String, email: String,progressDialog: ProgressDialog) {
        val currentUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val usersRef: DatabaseReference = FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference.child("Users")

        //?????????? ???????????? ?????????????? ????????????????
        val userMap = HashMap<String,Any>()
        userMap["uid"] = currentUserID
        userMap["fullname"] = fullName
        userMap["username"] = userName
        userMap["email"] = email
        userMap["bio"] = "Insta"
        userMap["image"] = "gs://instagram-clone-app-6fdef.appspot.com/Default images/profile.png"
        //?????????? ???????????????? ?????? ???? ???????????????? ?????? ?????????? ????????????????
        usersRef.child(currentUserID).setValue(userMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                {
                    progressDialog.dismiss()
                    Toast.makeText(this,"Account has been created successfully",Toast.LENGTH_LONG).show()

                    //?????? ?????????? ???????????? ???????? ???????? ?????????? ???????? ???????? ?????????????? ????????
                    FirebaseDatabase.getInstance("https://salah-59d6e-default-rtdb.firebaseio.com/").reference.child("Follow").child(currentUserID)
                        .child("Following").child(currentUserID).setValue(true)

                    val intent = Intent(this@SignupActivity,MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                    finish()
                }
                else
                {
                    val message = task.exception!!.toString()
                    Toast.makeText(this,"Error $message",Toast.LENGTH_LONG).show()
                    FirebaseAuth.getInstance().signOut()
                    progressDialog.dismiss()
                }
            }
    }
}

