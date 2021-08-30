package dz.jtsgen.processor.model;

import java.util.Optional;

import org.immutables.value.Value;

import dz.jtsgen.processor.model.rendering.TSMemberVisitor;

@Value.Immutable
public abstract class TSExecutableMember implements TSMember {

  @Value.Parameter
  public abstract String getName() ;

  @Value.Parameter
  public abstract TSTargetType getType();

  @Value.Parameter
  public abstract boolean getReadOnly();

  @Value.Parameter
  public abstract boolean getOptional();

  public abstract Optional<String> getComment();

  @Value.Parameter
  public abstract TSRegularMember[] getParameters();

  @Value.Default
  public boolean getInvalid() {
    return false;
  }

  @Override
  public void accept(TSMemberVisitor visitor, int ident) {
    visitor.visit(this, ident);
  }

  @Override
  public TSMember changedTSTarget(TSTargetType newTargetType) {
    return TSExecutableMemberBuilder.copyOf(this).withType(newTargetType);
  }

  public static TSMember changeTSParameterTargets(TSExecutableMember executableMember, TSRegularMember [] parameters) {
    return TSExecutableMemberBuilder.copyOf(executableMember).withParameters(parameters);
  }
}
