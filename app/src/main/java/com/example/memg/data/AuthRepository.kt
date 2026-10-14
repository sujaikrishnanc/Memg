package com.example.memg.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await

enum class UserRole {
    ADMIN, MEMBER
}

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val isPasswordChanged: Flow<Boolean> = flow {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val mobile = currentUser.email?.substringBefore("@")
            if (mobile != null) {
                try {
                    var userDoc = db.collection("users").document(mobile).get().await()
                    if (!userDoc.exists() && mobile.startsWith("+")) {
                        userDoc = db.collection("users").document(mobile.removePrefix("+")).get().await()
                    }
                    emit(userDoc.getBoolean("isPasswordChanged") ?: false)
                } catch (_: Exception) {
                    emit(false)
                }
            } else {
                emit(false)
            }
        } else {
            emit(false)
        }
    }

    suspend fun getCurrentUserStatus(): UserStatus? {
        val currentUser = auth.currentUser ?: return null
        val mobile = currentUser.email?.substringBefore("@") ?: return null
        return try {
            var userDoc = db.collection("users").document(mobile).get().await()
            if (!userDoc.exists() && mobile.startsWith("+")) {
                userDoc = db.collection("users").document(mobile.removePrefix("+")).get().await()
            }
            val role = userDoc.getString("role")?.let { UserRole.valueOf(it) } ?: UserRole.MEMBER
            val isPasswordChanged = userDoc.getBoolean("isPasswordChanged") ?: false
            UserStatus(mobile, role, isPasswordChanged)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun addMember(mobile: String) {
        val normalizedMobile = if (mobile.startsWith("+")) mobile else "+$mobile"
        val email = "$normalizedMobile@sndpmsnattika.org"
        try {
            auth.createUserWithEmailAndPassword(email, "sndpmsnattika2026").await()
        } catch (_: Exception) {
            // Ignore if user already exists
        }
        db.collection("users").document(normalizedMobile).set(
            mapOf(
                "role" to UserRole.MEMBER.name,
                "isPasswordChanged" to false
            )
        ).await()
    }

    suspend fun login(mobile: String, password: String): LoginResult {
        val email = "$mobile@sndpmsnattika.org"
        return try {
            val userDoc = db.collection("users").document(mobile).get().await()
            if (!userDoc.exists()) { // Checks if user exists in Firestore because admin is set not to.
                //This if block handles first time admin login and setup.
                val adminDoc = db.collection("admins").document(mobile).get().await()
                if(adminDoc.exists()) {
                    val isLoggingFirstTime=adminDoc.getBoolean("isLoggingFirstTime")
                    val defaultPassword = adminDoc.getString("defaultPassword")
                    if (isLoggingFirstTime == true) {
                        if(defaultPassword != null && password == defaultPassword) {
                            // Use normalized email for Auth
                            try {
                                auth.createUserWithEmailAndPassword(email, defaultPassword).await()
                            }
                            catch (_: Exception) {
                                //Handle failures associated with creating admin authentication
                                return LoginResult.Failure
                            }

                            db.collection("admins").document(mobile).update(
                                mapOf("isLoggingFirstTime" to false)
                            ).await() //Perform this instruction after successful initial password change

                            db.collection("admins").document(mobile).update(
                                mapOf("defaultPassword" to FieldValue.delete())
                            ).await() //Perform this instruction after successful initial password change

                            db.collection("users").document(mobile).set(
                                mapOf(
                                    "role" to UserRole.ADMIN.name,
                                    "isPasswordChanged" to true
                                )
                            ).await() //Perform this instruction after successful initial password change
                        }
                        return LoginResult.Success(forcePasswordChange = true, role = UserRole.ADMIN)
                    }
                }
                return LoginResult.Failure
            } else {
                //This block executes if user is present in 'users' collection
                val isPasswordChanged = userDoc.getBoolean("isPasswordChanged") ?: false
                val roleStr = userDoc.getString("role")
                val role = if (roleStr != null) UserRole.valueOf(roleStr) else UserRole.MEMBER
                
                try {
                    // Try to sign in. We need to be careful about which email was used during creation.
                    // Most recent logic creates with normalized email (with +).
                    // If old users exist without +, we might need a fallback.
                    try {
                        auth.signInWithEmailAndPassword(email, password).await()
                    } catch (_: Exception) {
                        val fallbackEmail = "$mobile@sndpmsnattika.org"
                        auth.signInWithEmailAndPassword(fallbackEmail, password).await()
                    }
                    
                    LoginResult.Success(forcePasswordChange = !isPasswordChanged, role = role)
                } catch (_: Exception) {
                    LoginResult.Failure
                }
            }
        } catch (_: Exception) {
            LoginResult.Failure
        }
    }

    suspend fun changePassword(newPassword: String): Boolean {
        return try {
            val currentUser = auth.currentUser ?: return false
            currentUser.updatePassword(newPassword).await()
            
            val mobile = currentUser.email?.substringBefore("@") ?: return false
            db.collection("users").document(mobile).update("isPasswordChanged", true).await()
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun requestOtp(mobile: String): Boolean {
        return try {
            val userDoc = db.collection("users").document(mobile).get().await()
            if (userDoc.exists() && userDoc.getBoolean("isPasswordChanged") == true) {
                // Simulation: In real Firebase we might send a password reset email
                // to the synthetic email, but here we just return true to simulate OTP sent.
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun verifyOtp(mobile: String, otp: String): Boolean {
        // Mocked OTP verification
        return otp == "123456"
    }

    suspend fun resetPassword(mobile: String, otp: String, newPassword: String): Boolean {
        return try {
            if (otp == "123456") {
                // In this synthetic mapping, we can't easily update password without old password
                // unless we use admin SDK or have a reset token.
                // For this simulation, we mark the password as changed in Firestore.
                db.collection("users").document(mobile).update("isPasswordChanged", true).await()
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    suspend fun logout() {
        auth.signOut()
    }

    data class UserStatus(val mobileNumber: String, val role: UserRole, val isPasswordChanged: Boolean)

    sealed class LoginResult {
        data class Success(val forcePasswordChange: Boolean, val role: UserRole) : LoginResult()
        data object Failure : LoginResult()
    }
}
