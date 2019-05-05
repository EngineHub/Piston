/*
 * Piston, a flexible command management system.
 * Copyright (C) EngineHub <http://www.enginehub.com>
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

package org.enginehub.piston.gen.util;

import org.checkerframework.checker.nullness.qual.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

public class ProcessingException extends RuntimeException {
    private @Nullable Element causingElement;
    private @Nullable AnnotationMirror causingMirror;

    public ProcessingException(String message) {
        super(message);
    }

    public ProcessingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProcessingException withElement(Element element) {
        this.causingElement = element;
        return this;
    }

    public ProcessingException withAnnotation(AnnotationMirror mirror) {
        this.causingMirror = mirror;
        return this;
    }

    public @Nullable Element getCausingElement() {
        return causingElement;
    }

    public @Nullable AnnotationMirror getCausingMirror() {
        return causingMirror;
    }
}
