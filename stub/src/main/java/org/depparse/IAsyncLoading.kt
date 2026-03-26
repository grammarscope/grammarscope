package org.depparse

interface IAsyncLoading {

    suspend fun loadAsync(modelPath: String)
}
