package software.coley.recaf.services.mapping.gen.filter;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.member.FieldMember;
import software.coley.recaf.info.member.MethodMember;
import software.coley.recaf.services.mapping.gen.NameGeneratorFilter;
import software.coley.recaf.util.TextMatchMode;

/**
 * Filter that includes classes <i>(and their members)</i> that match the given path.
 *
 * @author Matt Coley
 * @see ExcludeClassNameFilter
 */
public class IncludeClassNameFilter extends NameGeneratorFilter {
	private final String path;
	private final TextMatchMode matchMode;

	/**
	 * @param next
	 * 		Next filter to link. Chaining filters allows for {@code thisFilter && nextFilter}.
	 * @param path
	 * 		Class path name to include.
	 * @param matchMode
	 * 		Text match mode.
	 */
	public IncludeClassNameFilter(NameGeneratorFilter next, String path, TextMatchMode matchMode) {
		super(next, false);
		this.path = path;
		this.matchMode = matchMode;
	}

	@Override
	public boolean shouldMapClass(@Nonnull ClassInfo info) {
		String name = info.getName();
		boolean matches = matchMode.match(path, name);
		if (!matches)
			return false; // (inclusion) class name does not match whitelisted path
		return super.shouldMapClass(info);
	}

	@Override
	public boolean shouldMapField(@Nonnull ClassInfo owner, @Nonnull FieldMember field) {
		// Consider owner type, we do not want to map fields if they are outside the inclusion filter
		return super.shouldMapField(owner, field) && shouldMapClass(owner);
	}

	@Override
	public boolean shouldMapMethod(@Nonnull ClassInfo owner, @Nonnull MethodMember method) {
		// Consider owner type, we do not want to map methods if they are outside the inclusion filter
		return super.shouldMapMethod(owner, method) && shouldMapClass(owner);
	}
}
