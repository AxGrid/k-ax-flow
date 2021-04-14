package com.axgrid.flow

import java.lang.RuntimeException

abstract class AxFlowException : RuntimeException() {
}

class AxFlowTerminateException : AxFlowException() {
}
