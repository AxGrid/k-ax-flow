package com.axgrid.bend

class AxBend<T>(val pipes:Map<String, List<AxPipeAction<T>>>) {

    fun execute(name:String, ctx:T): T {
        var actions = pipes[name] ?: throw RuntimeException("pipe $name not found")
        for(action in actions) {
            try {
                action.execute(ctx, this)
            }catch (e:AxBendTerminateException) {
                break
            }
        }
        return ctx
    }

    data class AxPipeAction<T>(
        val fe:((T,AxBend<T>) -> Unit)? = null,
        val f:((T) -> Unit)? = null,
    ) {
        fun execute(ctx:T, env:AxBend<T>) {
            fe?.invoke(ctx, env)
            f?.invoke(ctx)
        }
    }

}


