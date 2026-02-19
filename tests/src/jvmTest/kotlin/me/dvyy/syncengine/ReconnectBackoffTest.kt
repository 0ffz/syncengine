package me.dvyy.syncengine

import me.dvyy.syncengine.client.sync.SyncClient

class ReconnectBackoffTest {
    val client = SyncClient.of(clientDatabase, reducers, schema, mockService)
}