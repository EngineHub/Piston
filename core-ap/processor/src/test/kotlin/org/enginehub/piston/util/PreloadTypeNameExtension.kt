/*
 * Piston, a flexible command management system.
 * Copyright (C) EngineHub <https://www.enginehub.org>
 * Copyright (C) Piston contributors
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.enginehub.piston.util

import com.google.auto.service.AutoService
import com.squareup.javapoet.ClassName
import com.squareup.javapoet.TypeName
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.Extension
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Runs before any access to TypeName & its subclasses to force-load
 * TypeName first, avoiding a deadlock.
 *
 * See https://github.com/square/javapoet/issues/637.
 */
@AutoService(Extension::class)
class PreloadTypeNameExtension : Extension, BeforeAllCallback {

    private val loadLock = ReentrantLock()

    override fun beforeAll(context: ExtensionContext?) = loadLock.withLock {
        TypeName.get(Object::class.java)
        Unit
    }
}
