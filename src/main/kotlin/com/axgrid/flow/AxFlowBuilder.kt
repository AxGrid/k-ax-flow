package com.axgrid.flow

import java.lang.Exception
import java.util.concurrent.atomic.AtomicLong
import kotlin.reflect.KClass

class AxFlowBuilder<T: IAxFlowContext>(var start: IAxFlowState) {

    private var actionMap : Map<IAxFlowState, MutableMap<IAxFlowEvent?, MutableList<AxFlow.AxActionHolder<T>>>> =
        start.javaClass.enumConstants.associate { it to mutableMapOf() }

    private var exceptionMap : Map<IAxFlowState, MutableList<AxFlow.AxExceptionHolder<T>>> =
        start.javaClass.enumConstants.associate { it to mutableListOf() }

    var startF: ((T) -> IAxFlowState)? = null

    var indexer = AtomicLong(0)

    fun on(state: IAxFlowState, f:(AxFlowStateBuilder<T>) -> Unit) : AxFlowBuilder<T> {
        f(AxFlowStateBuilder<T>(this, state))
        return this
    }

    fun add(
        otherFlow: AxFlow<T>,
        state: IAxFlowState? = null,
        event: IAxFlowEvent? = null,
        terminate:Boolean = false,
            ) : AxFlowBuilder<T> = this.invoke(state, event, terminate) { otherFlow.execute(it, it.event) }

    fun invoke(state: IAxFlowState? = null,
               event: IAxFlowEvent? = null,
               terminate:Boolean = false,
               f:(T) -> Unit) : AxFlowBuilder<T> {
        when(state) {
            null -> actionMap.values.forEach { item -> putIntoActionMap(item, event, terminate, f) }
            else -> putIntoActionMap(actionMap[state]!!, event, terminate, f)
        }
        return this
    }

    fun state(c:(T) -> IAxFlowState) : AxFlowBuilder<T> {
        startF = c
        return this;
    }

    fun except(state: IAxFlowState? = null,
               throwable: KClass<*>? = null,
               terminate:Boolean = false,
               action:(T) -> Unit) : AxFlowBuilder<T>
    {
        when (state) {
            null -> exceptionMap.values.forEach { item -> putIntoExceptMap(item, throwable, terminate, action) }
            else -> putIntoExceptMap(exceptionMap[state]!!, throwable, terminate, action)
        }
        return this
    }

    private fun putIntoActionMap(map: MutableMap<IAxFlowEvent?, MutableList<AxFlow.AxActionHolder<T>>>,
                                 event: IAxFlowEvent?,
                                 terminate:Boolean = false,
                                 action:(T) -> Unit) =
        if (terminate) map.getOrPut(event) { mutableListOf() }.add(
            AxFlow.AxActionHolder(
                { ctx -> action(ctx); throw AxFlowTerminateException() },
                indexer.incrementAndGet()
            )
        )
        else map.getOrPut(event) { mutableListOf() }.add(AxFlow.AxActionHolder(action, indexer.incrementAndGet()))

    private fun putIntoExceptMap(list: MutableList<AxFlow.AxExceptionHolder<T>>,
                                 throwable: KClass<*>?,
                                 terminate:Boolean = false,
                                 action:(T, Throwable) -> Unit) =
        if (terminate) list.add(
            AxFlow.AxExceptionHolder(
                exceptAction = { ctx, t -> action(ctx, t); throw AxFlowTerminateException() },
                throwable = throwable,
                index = indexer.incrementAndGet()
            )
        )
        else list.add(
            AxFlow.AxExceptionHolder(
                exceptAction = action,
                throwable = throwable,
                index = indexer.incrementAndGet()
            )
        )

    private fun putIntoExceptMap(list: MutableList<AxFlow.AxExceptionHolder<T>>,
                                 throwable: KClass<*>?,
                                 terminate:Boolean = false,
                                 action:(T) -> Unit) =
        if (terminate) list.add(
            AxFlow.AxExceptionHolder(
                simpleAction = { ctx -> action(ctx); throw AxFlowTerminateException() },
                throwable = throwable,
                index = indexer.incrementAndGet()
            )
        )
        else list.add(
            AxFlow.AxExceptionHolder(
                simpleAction = action,
                throwable = throwable,
                index = indexer.incrementAndGet()
            )
        )


    fun build(): AxFlow<T> {
        return AxFlow(
            start,
            actionMap,
            exceptionMap
        );
    }


}


class AxFlowStateBuilder<T: IAxFlowContext>(private var builder: AxFlowBuilder<T>, var currentState: IAxFlowState) {
    fun invoke(event: IAxFlowEvent? = null, terminate: Boolean = false, f: (ctx:T) -> Unit) : AxFlowStateBuilder<T> {
        builder.invoke(currentState, event, terminate, f)
        return this
    }

    fun except(throwable: KClass<*>? = Exception::class, terminate: Boolean = false, f: (ctx:T) -> Unit) : AxFlowStateBuilder<T> {
        builder.except(currentState, throwable, terminate, f)
        return this
    }

//    fun cond(event: IAxFlowEvent? = null, terminate: Boolean = false, c: (ctx:T) -> Boolean) : ((T) -> Unit) -> AxFlowStateBuilder<T> {
//        //((T) -> Unit) -> AxFlowStateBuilder<T>
//        var f: ((T) -> Unit) -> AxFlowStateBuilder<T> = { action ->
//            builder.invoke(currentState, event, terminate) { ctx -> {
//                if (c(ctx)) { action(ctx) }
//            }}
//            this
//        }
//        return f;
//    }

    fun cond(event: IAxFlowEvent? = null, terminate: Boolean = false, c: (ctx:T) -> Boolean, f: (ctx:T) -> Unit) : AxFlowStateBuilder<T> {
        builder.invoke(currentState, event, terminate) {
            if (c(it)) f(it);
        }
        return this;
    }

}
