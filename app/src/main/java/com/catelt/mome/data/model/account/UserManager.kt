package com.catelt.mome.data.model.account

import android.accounts.Account
import android.accounts.AccountManager
import android.content.Context
import android.os.Bundle

class UserManager(context: Context) {
    private val accountManager: AccountManager = AccountManager.get(context)

    fun addAccount(
        user: User
    ) {
        val data = Bundle()
            .apply {
                this.putString(NAME,user.name)
                this.putString(EMAIL, user.email)
                this.putString(PASSWORD, user.password)
                this.putString(SESSION, user.sessionId)
                this.putString(AVATAR, user.avatar)
            }
        val account = Account(user.email, ACCOUNT_TYPE)
        accountManager.addAccountExplicitly(account, user.sessionId, data)
        accountManager.setAuthToken(account, AUTH_TOKEN_TYPE, user.sessionId)
    }

    private fun getAccount(): Account? {
        return if (accountManager.getAccountsByType(ACCOUNT_TYPE).isNotEmpty()) {
            accountManager.getAccountsByType(ACCOUNT_TYPE)[0]
        } else {
            null
        }
    }

    fun getUser(): User {
        return User(
            getName(),
            getEmail(),
            getPassword(),
            getSessionId(),
            getAvatar(),
        )
    }

    fun isLogged(): Boolean {
        val accounts = accountManager.getAccountsByType(ACCOUNT_TYPE)
        if (accounts.isNotEmpty()) {
            return true
        }
        return false
    }

    fun logOut() {
        if (getAccount() != null) {
            accountManager.removeAccountExplicitly(getAccount())
        }
    }

    fun getSessionId(): String {
        return accountManager.getUserData(getAccount(), SESSION) ?: ""
    }

    fun getEmail(): String {
        return accountManager.getUserData(getAccount(), EMAIL)
    }

    fun getName(): String {
        return accountManager.getUserData(getAccount(), NAME)
    }

    fun getPassword(): String {
        return accountManager.getUserData(getAccount(), PASSWORD)
    }

    fun getAvatar(): String {
        return accountManager.getUserData(getAccount(), AVATAR) ?: ""
    }

    fun setName(name: String) {
        accountManager.setUserData(getAccount(), NAME, name)
    }

    fun setPassword(password: String) {
        accountManager.setUserData(getAccount(), PASSWORD, password)
    }

    fun setAvatar(avatar: String) {
        accountManager.setUserData(getAccount(), AVATAR, avatar)
    }


    companion object {
        const val AUTH_TOKEN_TYPE = "com.catelt.mome"
        const val ACCOUNT_TYPE = "com.catelt.mome"
        const val SESSION = "session_id"
        const val NAME = "name"
        const val EMAIL = "email"
        const val PASSWORD = "password"
        const val AVATAR = "avatar"

        @Volatile
        private var instance: UserManager? = null

        fun getInstance(context: Context): UserManager {
            return instance ?: synchronized(this) {
                instance ?: UserManager(context)
            }
        }
    }
}