package com.example.bezpiecznynotatnik.fragments

import com.example.bezpiecznynotatnik.R
import com.example.bezpiecznynotatnik.SecureNotesApp
import com.example.bezpiecznynotatnik.UserState
import com.example.bezpiecznynotatnik.data.GoogleDriveBackupManager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch

class AccountFragment : Fragment(R.layout.fragment_account) {

    private lateinit var btnSignOut: Button
    private lateinit var btnExport: Button
    private lateinit var btnImport: Button
    private lateinit var googleDriveManager: GoogleDriveBackupManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        googleDriveManager = (requireActivity().applicationContext as SecureNotesApp).googleDriveManager
        googleDriveManager.initializeGoogleSignIn(requireActivity())
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnSignOut = view.findViewById(R.id.btnSignOut)
        btnExport = view.findViewById(R.id.btnExport)
        btnImport = view.findViewById(R.id.btnImport)

        setupUI()

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    findNavController().navigate(R.id.nav_settings) }
            })
    }

    private fun setupUI() {
        // Set up Sign-In button
        btnSignOut.setOnClickListener {
            signOutUser()
        }

        // Set up Export button
        btnExport.setOnClickListener {
            lifecycleScope.launch {
                if (!googleDriveManager.isDriveServiceInitialized()) {
                    Toast.makeText(requireContext(), "Please sign in first", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                try {
                    googleDriveManager.uploadDatabase(requireActivity().applicationContext as SecureNotesApp)
                    Toast.makeText(requireContext(), "Database uploaded successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Set up Import button
        btnImport.setOnClickListener {
            lifecycleScope.launch {
                if (!googleDriveManager.isDriveServiceInitialized()) {
                    Toast.makeText(requireContext(), "Please sign in first", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                try {
                    googleDriveManager.downloadDatabase(requireActivity().applicationContext as SecureNotesApp)
                    Toast.makeText(requireContext(), "Database restored successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Restore failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun signOutUser() {
        googleDriveManager.signOut { success ->
            if (success) {
                UserState.isUserSignedIn = false
                Toast.makeText(requireContext(), "Signed out successfully!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.nav_settings)
            } else {
                Toast.makeText(requireContext(), "Failed to sign out.", Toast.LENGTH_SHORT).show()
                UserState.isUserSignedIn = true
            }
        }
    }
}
