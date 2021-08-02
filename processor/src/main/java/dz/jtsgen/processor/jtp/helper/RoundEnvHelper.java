/*
 * Copyright (c) 2017 Dragan Zuvic
 *
 * This file is part of jtsgen.
 *
 * jtsgen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * jtsgen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with jtsgen.  If not, see http://www.gnu.org/licenses/
 *
 */

package dz.jtsgen.processor.jtp.helper;

import dz.jtsgen.annotations.TSIgnore;
import dz.jtsgen.annotations.TypeScript;
import dz.jtsgen.annotations.TypeScriptExecutable;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * some temporary helper
 */
public abstract class RoundEnvHelper {
    
    public static Set<Element> getFilteredTypeScriptElements(RoundEnvironment roundEnv) {
        Set<? extends Element> tsElems = roundEnv.getElementsAnnotatedWith(TypeScript.class);
        return tsElems.stream().filter(RoundEnvHelper::noneIgnored).collect(Collectors.toSet());
    }

    public static Set<Element> getFilteredTypeScriptExecutableElements(RoundEnvironment roundEnv) {
        Set<? extends Element> tsExecElems = roundEnv.getElementsAnnotatedWith(TypeScriptExecutable.class);
        return tsExecElems.stream().filter(RoundEnvHelper::noneIgnored).collect(Collectors.toSet());
    }

    private static boolean noneIgnored(Element element) {
        final String tsIgnoreSimpleName = TSIgnore.class.getSimpleName();
        return element.getAnnotationMirrors().<AnnotationMirror>stream().noneMatch(
            (y) -> tsIgnoreSimpleName.equals(y.getAnnotationType().asElement().getSimpleName().toString()));
    }
}
