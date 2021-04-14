package com.axgrid.flow

class MultiplyFSM {

    enum class SpinState(val status:Int) : IAxFlowState {
        Ready(0),
        WaitTake(3),
    }

    enum class SpinType : IAxFlowState {
        Simple,
        Nudge,
        Collection
    }

    enum class SpinAction : IAxFlowEvent {
        Init,
        Get,
        Take,
        Risk,
        UserSelectHS
    }

    data class SpinContext (
        override var state: IAxFlowState? = SpinState.Ready,
        var spinType:SpinType = SpinType.Simple,
    ) : AxFlowStatefulContext()

    val flows = mapOf<SpinType, AxFlow<SpinContext>>(
        Pair(SpinType.Simple,
            AxFlow.start<SpinContext>(SpinState.Ready)
                .invoke(event = SpinAction.Init, f = this::init)
                .on(SpinState.Ready) {
                    state ->
                        state.invoke(SpinAction.Get, f = this::get)
                }
                .on(SpinState.WaitTake) {
                    state ->
                        state.invoke(SpinAction.Get) { this.take(it); this.get(it) }
                        state.invoke(SpinAction.Take) { this.take(it); }
                        state.invoke(SpinAction.Risk) { this.risk(it); }
                }
                .build()
            )
    )


    fun init(ctx:SpinContext) { }
    fun get(ctx:SpinContext) { }
    fun take(ctx:SpinContext) { }
    fun risk(ctx:SpinContext) { }

}
