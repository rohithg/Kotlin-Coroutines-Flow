import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.system.measureTimeMillis

/**
 * Kotlin Coroutines and Flow Demo
 * Demonstrates: Coroutines, Flow, structured concurrency, channels
 */

// Data class for API response
data class ApiResponse(
    val id: Int,
    val data: String,
    val timestamp: Long = System.currentTimeMillis()
)

class DataRepository {
    
    // Simulate API call
    suspend fun fetchData(id: Int): ApiResponse {
        delay(1000) // Simulate network delay
        return ApiResponse(id, "Data for id $id")
    }
    
    // Flow for streaming data
    fun streamData(): Flow<ApiResponse> = flow {
        repeat(5) { id ->
            delay(500)
            emit(ApiResponse(id, "Stream data $id"))
        }
    }
    
    // Cold flow that can be collected multiple times
    fun getDataFlow(): Flow<ApiResponse> = flow {
        println("Flow started")
        (1..3).forEach { id ->
            delay(300)
            emit(ApiResponse(id, "Flow data $id"))
        }
    }
}

class CoroutineExample {
    
    private val repository = DataRepository()
    
    // Parallel execution with async
    suspend fun fetchMultipleParallel(): List<ApiResponse> = coroutineScope {
        val deferred1 = async { repository.fetchData(1) }
        val deferred2 = async { repository.fetchData(2) }
        val deferred3 = async { repository.fetchData(3) }
        
        listOf(deferred1.await(), deferred2.await(), deferred3.await())
    }
    
    // Flow operators
    suspend fun processDataStream() {
        repository.streamData()
            .map { response -> 
                response.copy(data = response.data.uppercase())
            }
            .filter { it.id % 2 == 0 }
            .collect { response ->
                println("Processed: ${response.data}")
            }
    }
    
    // Error handling in coroutines
    suspend fun fetchWithErrorHandling(id: Int): Result<ApiResponse> = 
        runCatching {
            repository.fetchData(id)
        }
    
    // Structured concurrency
    suspend fun structuredConcurrency() = supervisorScope {
        launch {
            try {
                repository.fetchData(1)
                println("Task 1 completed")
            } catch (e: Exception) {
                println("Task 1 failed: ${e.message}")
            }
        }
        
        launch {
            repository.fetchData(2)
            println("Task 2 completed")
        }
    }
}

suspend fun main() = coroutineScope {
    val example = CoroutineExample()
    
    println("=== Parallel Execution ===")
    val time1 = measureTimeMillis {
        val results = example.fetchMultipleParallel()
        results.forEach { println(it) }
    }
    println("Time: ${time1}ms\n")
    
    println("=== Flow Processing ===")
    example.processDataStream()
    
    println("\n=== Structured Concurrency ===")
    example.structuredConcurrency()
    
    println("\nDemo complete!")
}
