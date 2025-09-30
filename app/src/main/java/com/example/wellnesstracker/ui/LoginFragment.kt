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

class LoginFragment : Fragment() {

    private lateinit var prefs: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        prefs = PreferencesManager(requireContext())

        val etUsername = view.findViewById<EditText>(R.id.etUsernameLogin)
        val etPassword = view.findViewById<EditText>(R.id.etPasswordLogin)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvRegisterPrompt = view.findViewById<TextView>(R.id.tvRegisterPrompt)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), getString(R.string.toast_fill_fields), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val users = prefs.loadUsers()
            val user = users.find { it.username == username && it.passwordHash == password }

            if (user != null) {
                prefs.saveCurrentUser(user.id)
                Toast.makeText(requireContext(), getString(R.string.toast_login_successful), Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            } else {
                Toast.makeText(requireContext(), getString(R.string.toast_invalid_credentials), Toast.LENGTH_SHORT).show()
            }
        }

        tvRegisterPrompt.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        return view
    }
}