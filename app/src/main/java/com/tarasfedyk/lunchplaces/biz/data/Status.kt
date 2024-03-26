package com.tarasfedyk.lunchplaces.biz.data

sealed class Status<Input, out Output> {
    data class Pending<Input>(
        val arg: Input
    ) : Status<Input, Nothing>()

    sealed class Terminal<Input, Output> : Status<Input, Output>()

    data class Success<Input, Output>(
        val arg: Input,
        val result: Output
    ) : Terminal<Input, Output>()

    data class Failure<Input>(
        val arg: Input,
        val error: Error
    ) : Terminal<Input, Nothing>()
}