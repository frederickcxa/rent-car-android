package com.icode.rentcar.ui

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.icode.rentcar.R
import com.google.firebase.auth.FirebaseAuth
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import android.util.Log
import com.google.firebase.auth.GoogleAuthProvider
import com.icode.rentcar.showView
import com.icode.rentcar.toast
import kotlinx.android.synthetic.main.activity_login.*

private const val TAG = "LoginActivity"
private const val RC_SIGN_IN = 9001

class LoginActivity : AppCompatActivity() {
  private lateinit var mAuth: FirebaseAuth
  private lateinit var mGoogleSignInClient: GoogleSignInClient

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(getString(R.string.default_web_client_id))
        .requestEmail()
        .build()

    mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

    mAuth = FirebaseAuth.getInstance()

    loginButton.setOnClickListener { signIn() }
  }

  private fun goHome() {
    startActivity(Intent(this, HomeActivity::class.java))
    finish()
  }

  override fun onStart() {
    super.onStart()
    mAuth.currentUser?.let { goHome() }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == RC_SIGN_IN) {
      val task = GoogleSignIn.getSignedInAccountFromIntent(data)

      try {
        val account = task.getResult(ApiException::class.java)

        firebaseAuthWithGoogle(account)
      } catch (e: ApiException) {
        Log.w(TAG, "Google sign in failed", e)
        toast("No se pudo obtener la cuenta de Google")
      }
    }
  }

  private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
    showView(mainContainer, false)
    showView(progressBar)

    val credential = GoogleAuthProvider.getCredential(acct.idToken, null)

    mAuth.signInWithCredential(credential)
        .addOnCompleteListener(this) { task ->
          if (task.isSuccessful) {
            goHome()
          } else {
            toast("No se pudo logear con su cuenta de Google")
          }

          showView(progressBar, false)
          showView(mainContainer)
        }
  }

  private fun signIn() {
    val signInIntent = mGoogleSignInClient.signInIntent
    startActivityForResult(signInIntent, RC_SIGN_IN)
  }
}
