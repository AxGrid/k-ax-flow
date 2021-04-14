package com.axgrid.flow

interface IAxFlowContext {
    var state : IAxFlowState?
    var prevState : IAxFlowState?
    var event: IAxFlowEvent?
    var exception: Throwable?
}

open class AxFlowStatefulContext(
    override var state : IAxFlowState? = null,
    override var prevState : IAxFlowState? = null,
    override var event : IAxFlowEvent? = null,
    override var exception: Throwable? = null
) : IAxFlowContext {

}
