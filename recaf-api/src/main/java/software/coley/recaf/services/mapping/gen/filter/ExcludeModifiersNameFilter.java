package software.coley.recaf.services.mapping.gen.filter;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.member.FieldMember;
import software.coley.recaf.info.member.MethodMember;
import software.coley.recaf.services.mapping.gen.NameGeneratorFilter;

import java.util.Collection;

/**
 * Filter that excludes classes and members that match the given access modifiers.
 *
 * @author Matt Coley
 * @see IncludeModifiersNameFilter
 */
public class ExcludeModifiersNameFilter extends NameGeneratorFilter {
	private final int[] flags;
	private final boolean targetClasses;
	private final boolean targetFields;
	private final boolean targetMethods;

	/**
	 * @param next
	 * 		Next filter to link. Chaining filters allows for {@code thisFilter && nextFilter}.
	 * @param flags
	 * 		Access flags to check for.
	 * @param targetClasses
	 * 		Check against classes.
	 * @param targetFields
	 * 		Check against fields.
	 * @param targetMethods
	 * 		Check against methods.
	 */
	public ExcludeModifiersNameFilter(NameGeneratorFilter next, Collection<Integer> flags,
									  boolean targetClasses, boolean targetFields, boolean targetMethods) {
		super(next, true);
		this.flags = flags.stream().mapToInt(i -> i).toArray();
		this.targetClasses = targetClasses;
		this.targetFields = targetFields;
		this.targetMethods = targetMethods;
	}

	@Override
	public boolean shouldMapClass(@Nonnull ClassInfo info) {
		if (targetClasses && info.hasAnyModifiers(flags))
			return false;
		return super.shouldMapClass(info);
	}

	@Override
	public boolean shouldMapField(@Nonnull ClassInfo owner, @Nonnull FieldMember field) {
		if (targetFields && field.hasAnyModifiers(flags))
			return false;
		return super.shouldMapField(owner, field);
	}

	@Override
	public boolean shouldMapMethod(@Nonnull ClassInfo owner, @Nonnull MethodMember method) {
		if (targetMethods && method.hasAnyModifiers(flags))
			return false;
		return super.shouldMapMethod(owner, method);
	}
}
