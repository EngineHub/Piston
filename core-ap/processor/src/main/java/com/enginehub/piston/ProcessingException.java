package com.enginehub.piston;

import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

public class ProcessingException extends RuntimeException {

    @Nullable
    private Element causingElement;
    @Nullable
    private AnnotationMirror causingMirror;

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

    @Nullable
    public Element getCausingElement() {
        return causingElement;
    }

    @Nullable
    public AnnotationMirror getCausingMirror() {
        return causingMirror;
    }
}
