package com.axgrid.flow

import mu.KLogging
import kotlin.reflect.KClass

interface IAxFlowEvent { val name: String }
interface IAxFlowState { val name: String }

class AxFlow<T: IAxFlowContext>(
    private val startState: IAxFlowState,
    private val actions: Map<IAxFlowState, Map<IAxFlowEvent?, List<AxActionHolder<T>>>>,
    private val exceptions: Map<IAxFlowState, List<AxExceptionHolder<T>>>,
    private val stateF:((T) -> IAxFlowState)? = null
) {


    companion object : KLogging() {
        fun <T : IAxFlowContext> start(startState: IAxFlowState) : AxFlowBuilder<T> {
            return AxFlowBuilder<T>(startState)
        }
    }

    fun execute(ctx: T, event: IAxFlowEvent? = null) : T {
        ctx.exception = null
        if (ctx.state == null) ctx.state = startState
        ctx.prevState = ctx.state
        ctx.event = event

        val l = actions.getOrDefault(ctx.state, emptyMap()).getOrDefault(event, emptyList())
        val l2 =  actions.getOrDefault(ctx.state, emptyMap()).getOrDefault(null, emptyList())
        for(item in (l + l2).sortedBy { it.index }) {
            try {
                item.action(ctx);
            }catch (e: AxFlowTerminateException) {
                break
            }catch (e:Exception) {
                executeExcept(ctx, e)
                break
            }
        }
        return ctx;
    }

    private fun executeExcept(ctx: T, e: Throwable) {
        ctx.exception = e
        val l = exceptions.getOrDefault(ctx.state, emptyList())
        val l2 =  exceptions.getOrDefault(null, emptyList())
        (l + l2).sortedBy { it.index }.forEach {
            try {
                it.exceptAction?.invoke(ctx, e)
                it.simpleAction?.invoke(ctx)
            }catch (e:Exception) {
            }
        }
    }

    data class AxActionHolder<T: IAxFlowContext>(val action: (T) -> Unit, val index: Long);
    data class AxExceptionHolder<T: IAxFlowContext>(
        val exceptAction: ((T, Throwable) -> Unit)? = null,
        val throwable: KClass<*>?,
        val index: Long,
        val simpleAction: ((T) -> Unit)? = null)
}

