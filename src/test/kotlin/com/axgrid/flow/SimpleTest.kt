package com.axgrid.flow

import org.junit.jupiter.api.Test

class SimpleTest {

    class MyContext : AxFlowStatefulContext() {
        var name : String? = null;
    };

    enum class MyStates : IAxFlowState {
        INIT,
        READY
    }

    enum class MyEvent : IAxFlowEvent {
        TICK,
    }

    fun action(ctx: MyContext) {
        println("MyAction")
    }

    @Test
    fun axFlowBuilderTest() {

        val flow = AxFlow.start<MyContext>(MyStates.INIT);
        val secondFlow = AxFlow.start<MyContext>(MyStates.INIT)
            .invoke { println("Create Context") }
            .build();

            flow.add(secondFlow)
                .on(MyStates.INIT) {
                it.invoke(terminate = true) { t -> action(t) }
                it.invoke(MyEvent.TICK) {
                println("ERROR")
                throw Exception("TEST")
            }
        }.on(MyStates.READY) {
            it.invoke {  }
        }.invoke { println("Store Context") }

        flow.except(throwable = Exception::class) {
            println("AHTUNG!!!")
            it.name = "TEST"
        }

        var mc = flow.build().execute(MyContext(), MyEvent.TICK);
        assert(mc.name == null)
    }


    @Test
    fun mapTest() {
        var map = mutableMapOf<String, Int>()
            .withDefault { 15 }

        println("MapItem:"+map.getValue("test"));
    }



    @Test
    fun mapOfMapTest() {

        val map = mutableMapOf<String, MutableMap<String, Int>>();
        map.getOrPut("test") { mutableMapOf() }["t1"] = 15;
        println(map);
        assert(map["test"] != null)

    }


    @Test
    fun wh() {
        var x = Context(6);
        when {
            x.index % 2 == 0 -> print("ODD")
            x.index == 6 -> println("OK")
            else -> println("Error");
        }
    }

    @Test
    fun eventTest() {


    }

    enum class Events {
        MAIN,
        SECOND
    }

    enum class State : IAxFlowState {
        MAIN,
        SECOND
    }

    class IAxEvent {

    }

    data class Context(var index: Int);

    sealed class TestEvents : ITestEvents {
        class Event1 : TestEvents();
        class Event2 : TestEvents();
        class Event3 : TestEvents();
    }

    interface ITestEvents {

    }

}
