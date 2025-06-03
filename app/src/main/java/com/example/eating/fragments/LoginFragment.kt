package com.example.eating.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.eating.MainActivity
import com.example.eating.R
import com.example.eating.fragments.RegisterFragment
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()

        val emailEditText = view.findViewById<EditText>(R.id.emailEditText)
        val passwordEditText = view.findViewById<EditText>(R.id.passwordEditText)
        val loginBtn = view.findViewById<Button>(R.id.loginButton)
        val anonBtn = view.findViewById<Button>(R.id.anonymousLoginButton)
        val toRegisterText = view.findViewById<TextView>(R.id.toRegister)

        loginBtn.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "Fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Login failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        anonBtn.setOnClickListener {
            auth.signInAnonymously()
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Logged in anonymously", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(requireContext(), MainActivity::class.java))
                    requireActivity().finish()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Anonymous login failed: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }

        toRegisterText.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_fragment_container, RegisterFragment())
                .addToBackStack(null)
                .commit()
        }
    }
}
