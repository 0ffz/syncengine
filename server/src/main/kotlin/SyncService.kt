//import kotlinx.coroutines.flow.Flow
//import kotlinx.rpc.RemoteService
//import kotlinx.rpc.annotations.Rpc
//import kotlinx.serialization.Serializable
//
//@Serializable
//data class Change<T>(
//    val type: UpdateType,
//    val componentId: Long,
//    val timestamp: Long,
//    val data: T
//)
//
//@Serializable
//data class Changelist(
//    val startTime: Long,
//    val endTime: Long,
//    val changes: List<Change<*>>
//)
//@Rpc
//interface SyncService: RemoteService {
//    /**
//     * Begins continuous synchronization between two devices.
//     *
//     * Runs initial sync to bring each device up to date with local changes, and remains
//     * open for streaming changes live.
//     */
//    fun synchronize(
//        since: Long,
//        incoming: Flow<Changelist>
//    ): Flow<Changelist>
//}
