package androidx.livedata.contrib

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.bbou.coroutines.ExecuteAsync
import kotlinx.coroutines.launch
import java.util.concurrent.BlockingQueue
import java.util.concurrent.Executor
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

object AsyncTransformations {

    // F R O M   S Y N C   F U N C T I O N

    /**
     * LiveData<X> to LiveData<Y>
     * UNFOLDED:
     * result.addSource(source, new Observer<X>()
     * {
     * 	@Override
     * 	public void onChanged(@Nullable final X x)
     * 	{
     * 		AsyncTask.execute(new Runnable()
     * 		{
     * 			@Override
     * 			public void run()
     * 			{
     * 				result.postValue(mapFunction.apply(x));
     * 			}
     * 		});
     * 	}
     * });
     *
     * @param X input live data type
     * @param Y output livedata type
     * @param source
     * @param transform X to Y transform function
     * @return output livedata
     */
    @MainThread
    fun <X, Y> map(source: LiveData<X>, transform: (X) -> Y): LiveData<Y> {
        val result = MediatorLiveData<Y>()
        result.addSource(source) { x: X -> execute { result.postValue(transform.invoke(x)) } }
        return result
    }

    // E X E C U T O R

    private const val CORE_POOL_SIZE = 5
    private const val MAXIMUM_POOL_SIZE = 128
    private const val KEEP_ALIVE = 1

    private val THREAD_FACTORY: ThreadFactory = object : ThreadFactory {
        private val count = AtomicInteger(1)
        override fun newThread(runnable: Runnable): Thread {
            return Thread(runnable, "WorkerTask #" + count.getAndIncrement())
        }
    }

    private val POOL_WORK_QUEUE: BlockingQueue<Runnable> = LinkedBlockingQueue(10)

    private val executor: Executor = ThreadPoolExecutor(
        CORE_POOL_SIZE,
        MAXIMUM_POOL_SIZE,
        KEEP_ALIVE.toLong(),
        TimeUnit.SECONDS,
        POOL_WORK_QUEUE,
        THREAD_FACTORY
    )

    @Suppress("unused")
    @MainThread
    fun <X, Y> mapCoroutine(source: LiveData<X>, transform: (X) -> Y): LiveData<Y> {
        val result = MediatorLiveData<Y>()
        result.addSource(source) { x: X ->
            val coroutineScope = ExecuteAsync.CoroutineScope()
            coroutineScope.launch {
                ExecuteAsync.executeAndWait { result.postValue(transform.invoke(x)) }
            }
        }
        return result
    }

    /**
     * Execute a runnable on default executor
     */
    private fun execute(runnable: Runnable) {
        executor.execute(runnable)
    }

    // F R O M   A S Y N C   F U N C T I O N

    /**
     * Function that feeds its result to a consumer
     *
     * @param I input type
     * @param O output/result type
     */
    interface ProducerFunction<I, O> {

        /**
         * Transform input to output and feed result to consumer
         *
         * @param input input
         * @param consumer consumer
         */
        fun transform(input: I, consumer: Consumer<O>)
    }

    /**
     * Livedata async function
     *
     * @param I input type
     * @param O output type
     */
    abstract class LiveDataAsyncFunction<I, O> protected constructor() : Consumer<I>, ProducerFunction<I, O> {

        val result = MediatorLiveData<O>()
        private val consumer = Consumer<O> { value -> result.postValue(value) }

        /**
         * Accept input, transform it and feeds result to output consumer
         *
         * @param input
         */
        override fun accept(input: I) {
            transform(input, consumer)
        }
    }

    /**
     * LiveData<X> to LiveData<Y>
     * UNFOLDED:
     * result.addSource(source, new Observer<X>()
     * {
     *   @Override
     *	 public void onChanged(@Nullable final X x)
     *	 {
     *		function.execute(x);
     *	 }
     *	});
     *
     * @param X input live data type
     * @param Y output livedata type
     * @param source source livedata
     * @param function X to Y transform function that feeds its result to a consumer
     * @return out livedata
     */
    @MainThread
    fun <X, Y> map(source: LiveData<X>, function: LiveDataAsyncFunction<X, Y>): LiveData<Y> {
        val result = function.result
        result.addSource(source) { input -> function.accept(input) }
        return result
    }
}
