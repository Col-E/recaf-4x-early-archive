package software.coley.recaf.services.mapping.gen.filter;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.member.FieldMember;
import software.coley.recaf.info.member.MethodMember;
import software.coley.recaf.services.mapping.gen.NameGeneratorFilter;
import software.coley.recaf.util.TextMatchMode;

/**
 * Filter that excludes classes <i>(and their members)</i> that match the given path.
 *
 * @author Matt Coley
 * @see IncludeClassNameFilter
 */
public class ExcludeClassNameFilter extends NameGeneratorFilter {
	private final String path;
	private final TextMatchMode matchMode;

	/**
	 * @param next
	 * 		Next filter to link. Chaining filters allows for {@code thisFilter && nextFilter}.
	 * @param path
	 * 		Class path name to exclude.
	 * @param matchMode
	 * 		Text match mode.
	 */
	public ExcludeClassNameFilter(NameGeneratorFilter next, String path, TextMatchMode matchMode) {
		super(next, true);
		this.path = path;
		this.matchMode = matchMode;
	}

	@Override
	public boolean shouldMapClass(@Nonnull ClassInfo info) {
		String name = info.getName();
		boolean matches = matchMode.match(path, name);
		if (matches)
			return false; // (exclusion) class name contains blacklisted path
		return super.shouldMapClass(info);
	}

	@Override
	public boolean shouldMapField(@Nonnull ClassInfo owner, @Nonnull FieldMember field) {
		// Consider owner type, we do not want to map fields if they are inside the exclusion filter
		return super.shouldMapField(owner, field) && shouldMapClass(owner);
	}

	@Override
	public boolean shouldMapMethod(@Nonnull ClassInfo owner, @Nonnull MethodMember method) {
		// Consider owner type, we do not want to map methods if they are inside the exclusion filter
		return super.shouldMapMethod(owner, method) && shouldMapClass(owner);
	}
}
