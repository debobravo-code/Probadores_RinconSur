package com.example.probadores_rinconsur.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.probadores_rinconsur.repository.models.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // READ
    @Query("SELECT * FROM users ORDER BY name ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getUserById(id: Int): User?

    // LOGIN
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    // CREATE (ABORT: el email es único)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long

    // UPDATE
    @Update
    suspend fun updateUser(user: User)

    // DELETE
    @Delete
    suspend fun deleteUser(user: User)
}