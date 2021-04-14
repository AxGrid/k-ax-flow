package com.axgrid.flow

import org.junit.jupiter.api.Test

class ContextTest {

    data class Context(var name:String? = null, var id:Int = 0);

    fun act(slug:String, f:(Context) -> Context) = Pair(slug, f)

    var bends:Map<String, (Context) -> Context> = mapOf(
        act("g_dap_fg") { ctx ->
            ctx
                .createName("fg_test")
                .incrementId()
        },
        act("g_dap") { ctx ->
            ctx
              .createName("test")
              .incrementId()
              .incrementId(5)
              .incrementId(-2)
              .exec("g_dap_fg") { it.id < 100 }
        },
    )

    fun Context.exec(name:String, c:((Context) -> Boolean)? = null) : Context {
        if (c == null || c(this)) return bends[name]!!.invoke(this)
        return this
    }

    fun Context.createName(name:String) : Context {
        this.name = name
        return this
    }

    fun Context.incrementId(value:Int = 1) : Context {
        this.id += value
        return this
    }



    @Test
    fun testExecute() {
        var ctx =  bends["g_dap"]!!.invoke(Context())
        assert(ctx.name == "fg_test")
        assert(ctx.id == 5)
    }

}
