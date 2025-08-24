package me.dvyy.syncengine.client.sync

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import me.dvyy.sqlite.Database
import me.dvyy.syncengine.client.mutators.MutatorQueue
import me.dvyy.syncengine.sync.SyncRequest
import me.dvyy.syncengine.sync.SyncResult

class SyncClient(
    private val db: Database,
    private val store: SyncClientDataStore,
    val mutators: MutatorQueue<*, *>,
) {
    val httpClient = createHTTPClient()

    //TODO start sync job/mutex, wait until it finishes
    suspend fun sync() {
        val request: SyncRequest = db.read {
            store.getSyncRequest()
        }

        val updates = httpClient.post("/sync") {
            contentType(ContentType.Application.ProtoBuf)
            setBody(request)
        }.body<SyncResult>() //TODO stream back last acknowledged id & row changes

        db.write {
            store.reconcileDiff(updates)
        }
    }
}
