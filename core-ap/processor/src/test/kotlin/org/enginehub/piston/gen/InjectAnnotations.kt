package org.enginehub.piston.gen

import org.enginehub.piston.inject.InjectAnnotation

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
@InjectAnnotation
annotation class InjectAlpha

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
@InjectAnnotation
annotation class InjectBeta

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
@InjectAnnotation
annotation class InjectGamma(
    val value: String
)

@MustBeDocumented
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
@InjectAnnotation
annotation class InjectDelta(
    val qux: Int,
    val baz: Int,
    val thq: IntArray
)
