package com.example.wellnesstracker.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.wellnesstracker.R
import com.example.wellnesstracker.data.User
import com.example.wellnesstracker.utils.PreferencesManager

class RegisterFragment : Fragment() {

    private lateinit var prefs: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        prefs = PreferencesManager(requireContext())

        val etUsername = view.findViewById<EditText>(R.id.etUsernameRegister)
        val etPassword = view.findViewById<EditText>(R.id.etPasswordRegister)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val tvLoginPrompt = view.findViewById<TextView>(R.id.tvLoginPrompt)

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.toast_fill_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val users = prefs.loadUsers()
            if (users.any { it.username == username }) {
                Toast.makeText(requireContext(), getString(R.string.toast_username_taken), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newUser = User(username = username, passwordHash = password)
            users.add(newUser)
            prefs.saveUsers(users)
            prefs.saveCurrentUser(newUser.id)

            Toast.makeText(requireContext(), getString(R.string.toast_registration_successful), Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
        }

        tvLoginPrompt.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        return view
    }
}