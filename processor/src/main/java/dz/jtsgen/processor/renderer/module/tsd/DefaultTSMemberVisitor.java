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

package dz.jtsgen.processor.renderer.module.tsd;

import java.io.PrintWriter;

import dz.jtsgen.processor.helper.IdentHelper;
import dz.jtsgen.processor.model.TSEnumMember;
import dz.jtsgen.processor.model.TSExecutableMember;
import dz.jtsgen.processor.model.TSMember;
import dz.jtsgen.processor.model.TSRegularMember;
import dz.jtsgen.processor.model.rendering.TSMemberVisitor;


public class DefaultTSMemberVisitor extends OutputVisitor implements TSMemberVisitor {


    DefaultTSMemberVisitor(PrintWriter out) {
        super(out);
    }

    @Override
    public void visit(TSMember x, int ident) {
        x.getComment().ifPresent( comment -> tsComment(comment,ident));
        getOut().print(IdentHelper.identPrefix(ident));
        if (x.getReadOnly()) getOut().print("readonly ");
        getOut().print(x.getName());
        if(x.getOptional()) {
            getOut().print("?: ");
        } else {
            getOut().print(": ");
        }
        getOut().print(x.getType());
        getOut().println(";");
    }

    @Override
    public void visit(TSExecutableMember x, int ident) {
        x.getComment().ifPresent( comment -> tsComment(comment,ident));
        getOut().print(IdentHelper.identPrefix(ident));
        if (x.getReadOnly()) getOut().print("readonly ");
        getOut().print(x.getName());
        getOut().print("(");
        TSRegularMember[] parameters = x.getParameters();
        if(parameters != null && parameters.length > 0) {
            boolean first = true;
            for (TSRegularMember parameter : parameters) {
                if(parameter != null) {
                    if(first) {
                        first = false;
                    } else {
                        getOut().print(",");
                    }
                    getOut().print(parameter.getName());
                    if(parameter.getOptional()) {
                        getOut().print("?: ");
                    } else {
                        getOut().print(": ");
                    }
                    getOut().print(parameter.getType());
                }
            }
        }
        getOut().print(")");
        if(x.getOptional()) {
            getOut().print("?: ");
        } else {
            getOut().print(": ");
        }
        getOut().print(x.getType());
        getOut().println(";");
    }

    @Override
    public void visit(TSEnumMember x, int ident) {
        x.getComment().ifPresent( comment -> tsComment(comment,ident));
        getOut().print(IdentHelper.identPrefix(ident));
        if ( x.getExportStrategyStringRepresentation().isPresent()) {
            getOut().print(x.getName());
            getOut().print(" = '");
            getOut().print(x.getExportStrategyStringRepresentation().get());
            getOut().print("'");
        } else {
            getOut().print(x.getName());

        }
    }
}
