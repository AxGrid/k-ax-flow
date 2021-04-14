package com.axgrid.flow

import com.axgrid.bend.AxBend
import com.axgrid.bend.AxBendBuilder
import com.axgrid.bend.AxBendPipeBuilder
import com.axgrid.bend.AxBendTerminateException
import org.junit.jupiter.api.Test

class BendTest  {

    data class MyBendContext(
        var name:String? = null,
        var id:Int = 0,
        var reel:List<Int>? = null
    )

    fun createReels(ctx:MyBendContext, size:Int=3) {
        ctx.reel = listOf(1,2,3,4,5,6)
    }

    @Test
    fun createBends() {

        var bend = AxBendBuilder<MyBendContext>()
            .pipe("g_dap") {
                it.add { ctx -> println("start build:$ctx") }
                it.add(c={ ctx -> ctx.id < 0 }) { throw AxBendTerminateException() }
                it.add { ctx -> this.createReels(ctx, size=3) }
                it.add { ctx, bend ->
                    when {
                        ctx.id == 0 -> ctx.id = 1
                        ctx.id > 100 -> bend.execute("g_dap_fg", ctx)
                    }
                }
                it.repeat(5) { ctx, bend -> bend.execute("g_dap_fg", ctx) }
                it.add { ctx -> println("finish build:$ctx") }
            }

            .pipe("g_dap_fg") {
                it.add { ctx -> this.createReels(ctx, size=3) }
                it.add { ctx -> println("start fg build:$ctx") }
                it.add { ctx -> println("finish fg build:$ctx") }
            }
            .build();

        var ctx = bend.execute("g_dap", MyBendContext(id = 101))
        println("CTX:"+ctx)
    }


    fun <T> AxBendPipeBuilder<T>.repeat(count:Int, f:(T) -> Unit) : AxBendPipeBuilder<T>  {
        this.add { ctx -> (1..count).forEach { _ -> f(ctx) } }
        return this
    }

    fun <T> AxBendPipeBuilder<T>.repeat(count:Int, fa:(T, AxBend<T>) -> Unit) : AxBendPipeBuilder<T>  {
        this.add { ctx, bend -> (1..count).forEach { _ -> fa(ctx, bend) } }
        return this
    }

}
