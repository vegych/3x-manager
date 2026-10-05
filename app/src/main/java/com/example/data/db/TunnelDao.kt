package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TunnelDao {
    @Query("SELECT * FROM tunnels ORDER BY createdAt DESC")
    fun getAllTunnels(): Flow<List<TunnelConfigEntity>>

    @Query("SELECT * FROM tunnels WHERE id = :id LIMIT 1")
    suspend fun getTunnelById(id: Long): TunnelConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTunnel(tunnel: TunnelConfigEntity): Long

    @Update
    suspend fun updateTunnel(tunnel: TunnelConfigEntity)

    @Delete
    suspend fun deleteTunnel(tunnel: TunnelConfigEntity)

    @Query("SELECT COUNT(*) FROM tunnels")
    suspend fun getTunnelCount(): Int
}
