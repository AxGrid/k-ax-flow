package com.axgrid.bend

class AxBendBuilder<T>(val prefix:String?= null) {

    val pipes: MutableMap<String, MutableList<AxBend.AxPipeAction<T>>> = mutableMapOf();

    fun pipe(name:String, builder:(AxBendPipeBuilder<T>) -> Unit) : AxBendBuilder<T> {
        pipes.compute(name) { _, v -> v ?: mutableListOf() }
        builder(AxBendPipeBuilder(name, this))
        return this
    }

    fun add(bend:AxBend<T>) : AxBendBuilder<T> {
        return this
    }

    fun add(bend:AxBendPipeBuilder<T>) : AxBendBuilder<T> {
        return this
    }



    fun build() : AxBend<T> {
        return AxBend(this.pipes);
    }
}


class AxBendPipeBuilder<T>(private val name:String, private val builder:AxBendBuilder<T>) {

    fun add(f:((T) -> Unit)) : AxBendPipeBuilder<T> {
        builder.pipes[name]!!.add(AxBend.AxPipeAction(f=f))
        return this
    }

    fun add(f:((T, AxBend<T>) -> Unit)) : AxBendPipeBuilder<T> {
        builder.pipes[name]!!.add(AxBend.AxPipeAction(fe=f))
        return this
    }

    fun add(c:(T) -> Boolean, f:(T) -> Unit) {
        builder.pipes[name]!!.add(AxBend.AxPipeAction { ctx -> if (c(ctx)) f(ctx) })
    }

}
