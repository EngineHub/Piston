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
