package com.tarasfedyk.lunchplaces.biz.data

sealed class Status<Arg, out Result> {
    abstract val arg: Arg

    data class Pending<Arg>(
        override val arg: Arg
    ) : Status<Arg, Nothing>()

    sealed class Terminal<Arg, Result> : Status<Arg, Result>()

    data class Success<Arg, Result>(
        override val arg: Arg,
        val result: Result
    ) : Terminal<Arg, Result>()

    data class Failure<Arg>(
        override val arg: Arg,
        val errorType: ErrorType
    ) : Terminal<Arg, Nothing>()
}