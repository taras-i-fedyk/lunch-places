package com.tarasfedyk.lunchplaces.biz.model

sealed class Status<Input, out Output> {
    abstract val arg: Input

    data class Pending<Input>(
        override val arg: Input
    ) : Status<Input, Nothing>()

    sealed class Terminal<Input, Output> : Status<Input, Output>()

    data class Success<Input, Output>(
        override val arg: Input,
        val result: Output
    ) : Terminal<Input, Output>()

    data class Failure<Input>(
        override val arg: Input,
        val errorType: ErrorType
    ) : Terminal<Input, Nothing>()
}