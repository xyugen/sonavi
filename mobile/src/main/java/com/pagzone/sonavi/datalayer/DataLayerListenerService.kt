package com.pagzone.sonavi.datalayer

import android.util.Log
import androidx.annotation.WorkerThread
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Node
import com.google.android.gms.wearable.Wearable
import com.google.android.gms.wearable.WearableListenerService
import java.util.concurrent.ExecutionException

class DataLayerListenerService : WearableListenerService() {
    private val tag = "Mobile/DataLayerListenerService"

    override fun onCreate() {

    }

    override fun onDataChanged(dataEvents: DataEventBuffer) {
        // TODO: handle data
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    @WorkerThread
    fun getNodes(): Collection<String> {
        val results: HashSet<String> = HashSet()

        val nodeListTask: Task<List<Node>> =
            Wearable.getNodeClient(applicationContext).connectedNodes

        try {
            val nodes: List<Node> = Tasks.await(nodeListTask)

            for (node in nodes) {
                results.add(node.id)
            }
        } catch (executionException: ExecutionException) {
            Log.e(tag, "Task failed: " + executionException.message)
        } catch (exception: InterruptedException) {
            Log.e(tag, "Interrupt occurred: " + exception.message)
        }

        return results
    }
}