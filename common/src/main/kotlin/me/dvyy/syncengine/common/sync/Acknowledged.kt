package me.dvyy.syncengine.common.sync

data class Acknowledged(val incomingQueued: Int, val mutatorsAcknowledged: Int)
