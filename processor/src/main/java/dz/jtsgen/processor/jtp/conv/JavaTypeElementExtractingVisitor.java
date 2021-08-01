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

package dz.jtsgen.processor.jtp.conv;

import dz.jtsgen.annotations.TSIgnore;
import dz.jtsgen.annotations.TSOptional;
import dz.jtsgen.annotations.TSReadOnly;
import dz.jtsgen.processor.jtp.conv.visitors.JavaTypeConverter;
import dz.jtsgen.processor.jtp.info.TSProcessingInfo;
import dz.jtsgen.processor.model.TSMember;
import dz.jtsgen.processor.model.TSRegularMemberBuilder;
import dz.jtsgen.processor.model.TSTargetType;

import javax.lang.model.element.*;
import javax.lang.model.util.SimpleElementVisitor8;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * processes the Elements of an java interface or class, members and variables.
 * <p>
 * <p>
 * because of beans this Visitor is stateful: return type is void.
 */
class JavaTypeElementExtractingVisitor extends SimpleElementVisitor8<Void, Void> {

    protected static Logger LOG = Logger.getLogger(JavaTypeElementExtractingVisitor.class.getName());


    protected final Map<String, TSMember> members = new HashMap<>();

    // list of members to sort out setters only
    protected final Set<String> extractableMembers = new HashSet<>();

    // the current Java Type
    protected final TypeElement typeElementToConvert;

    // the environment
    protected TSProcessingInfo tsProcessingInfo;

    // the converter for unkown types
    protected JavaTypeConverter javaTypeConverter;



    JavaTypeElementExtractingVisitor(TypeElement typeElementToConvert, TSProcessingInfo visitorParam, JavaTypeConverter javaTypeConverter) {
        super();
        if (typeElementToConvert == null || visitorParam == null) throw new IllegalArgumentException();
        this.typeElementToConvert = typeElementToConvert;
        this.tsProcessingInfo = visitorParam;
        this.javaTypeConverter = javaTypeConverter;

    }


    @Override
    public Void visitType(TypeElement e, Void notcalled) {
        LOG.warning(() -> String.format("JTExV visiting type %s not used, but called", e.toString()));
        return null;
    }

    @Override
    public Void visitVariable(VariableElement e, Void notcalled) {
        final boolean isPublic = e.getModifiers().contains(Modifier.PUBLIC);
        final String name = e.getSimpleName().toString();
        final boolean isIgnored = isIgnored(e);
        final boolean  isReadOnlyAnnotation = readOnlyAnnotation(e) || readOnlyAnnotation(this.typeElementToConvert);
        final boolean  isOptionalAnnotation = optionalAnnotation(e) || optionalAnnotation(this.typeElementToConvert);
        LOG.log(Level.FINEST, () -> String.format("JTExV visiting variable %s%s", name, isIgnored?" (ignored)":""));
        if (isPublic && !members.containsKey(name)) {
            final TSTargetType tsTypeOfExecutable = convertTypeMirrorOfMemberToTsType(e, tsProcessingInfo);
            final Optional<String> comment = Optional.ofNullable(this.tsProcessingInfo.getpEnv().getElementUtils().getDocComment(e));
            members.put(name,
                    TSRegularMemberBuilder
                            .of(
                                    name,
                                    tsTypeOfExecutable,
                                    isReadOnlyAnnotation,
                                    isOptionalAnnotation)
                            .withComment(comment));
            if (! isIgnored) extractableMembers.add(name);
        }
        return null;
    }


    @Override
    public Void visitExecutable(ExecutableElement e, Void notcalled) {
        LOG.fine(() -> String.format("JTExV visiting executable %s", e.toString()));
        if (isGetterOrSetter(e)) {
            final String rawName = nameOfMethod(e).orElse("");
            final String name = mappedName(rawName);
            final boolean isPublic = e.getModifiers().contains(Modifier.PUBLIC);
            final boolean isIgnored = isIgnored(e);
            final boolean isReadOnly = readOnlyAnnotation(e) || readOnlyAnnotation(this.typeElementToConvert);
            final boolean isOptional = optionalAnnotation(e) || optionalAnnotation(this.typeElementToConvert);
            if (isGetter(e) && ( !isPublic ||  isIgnored )) return null; // return early for not converting private types
            final TSTargetType tsTypeOfExecutable = convertTypeMirrorToTsType(e, tsProcessingInfo);
            LOG.fine(() -> "is getter or setter: " + (isPublic ? "public " : " ") + e.getSimpleName() + " -> " + name +"(" + rawName+ ")" + ":" + tsTypeOfExecutable + " " +(isIgnored?"(ignored)":""));
            if (members.containsKey(name)) {
                // can't be read only anymore
                final Optional<String> comment = Optional.ofNullable(this.tsProcessingInfo.getpEnv().getElementUtils().getDocComment(e));
                members.put(name, TSRegularMemberBuilder
                        .of(
                                name,
                                isGetter(e) ? tsTypeOfExecutable : members.get(name).getType(),
                                isReadOnly,
                                isOptional)
                        .withComment(comment)
                );
            } else {
                final Optional<String> comment = Optional.ofNullable(this.tsProcessingInfo.getpEnv().getElementUtils().getDocComment(e));
                members.put(name, TSRegularMemberBuilder
                        .of(name, tsTypeOfExecutable, isReadOnly, isOptional)
                        .withComment(comment)
                );
            }
            if (isGetter(e)) extractableMembers.add(name);
        }
        return null;
    }

    protected String mappedName(String rawName) {
        return this.tsProcessingInfo.nameMapper().mapMemberName(rawName);
    }

    protected boolean isGetter(ExecutableElement e) {
        return this.tsProcessingInfo.executableHelper().isGetter(e);
    }

    protected Optional<String> nameOfMethod(ExecutableElement e) {
        return this.tsProcessingInfo.executableHelper().extractRawMemberName(e.getSimpleName().toString());
    }

    protected boolean isGetterOrSetter(ExecutableElement e) {
        return this.tsProcessingInfo.executableHelper().isGetterOrSetter(e);
    }

    protected boolean readOnlyAnnotation(Element e) {
        final TypeElement annoTationElement = this.tsProcessingInfo.elementCache().typeElementByCanonicalName(TSReadOnly.class.getCanonicalName());
        return e.getAnnotationMirrors().stream().anyMatch( (x) ->
                x.getAnnotationType().asElement().equals(annoTationElement)
        );
    }

    protected boolean optionalAnnotation(Element e) {
        final TypeElement annoTationElement = this.tsProcessingInfo.elementCache().typeElementByCanonicalName(TSOptional.class.getCanonicalName());
        return e.getAnnotationMirrors().stream().anyMatch( (x) ->
            x.getAnnotationType().asElement().equals(annoTationElement)
        );
    }

    protected boolean isIgnored(Element e) {
        final TypeElement annoTationElement = this.tsProcessingInfo.elementCache().typeElementByCanonicalName(TSIgnore.class.getCanonicalName());
        return e.getAnnotationMirrors().stream().anyMatch( (x) ->
                x.getAnnotationType().asElement().equals(annoTationElement));
    }


    protected TSTargetType convertTypeMirrorToTsType(ExecutableElement theElement, TSProcessingInfo tsProcessingInfo) {
        return new MirrorTypeToTSConverterVisitor(theElement, tsProcessingInfo, javaTypeConverter).visit(theElement.getReturnType());
    }

    protected TSTargetType convertTypeMirrorOfMemberToTsType(VariableElement theElement, TSProcessingInfo TSProcessingInfo) {
        return new MirrorTypeToTSConverterVisitor(theElement, TSProcessingInfo, javaTypeConverter).visit(theElement.asType());
    }

    List<TSMember> getMembers() {
        return members.values().stream().
                filter((x) -> extractableMembers.contains(x.getName()))
                .collect(Collectors.toList());
    }
}
