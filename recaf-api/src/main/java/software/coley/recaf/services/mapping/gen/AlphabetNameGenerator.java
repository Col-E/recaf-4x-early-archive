package software.coley.recaf.services.mapping.gen;

import jakarta.annotation.Nonnull;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.member.FieldMember;
import software.coley.recaf.info.member.MethodMember;
import software.coley.recaf.util.NumberUtil;
import software.coley.recaf.util.StringUtil;

import java.util.Random;

/**
 * Basic name generator using a given alphabet of characters to generate pseudo-random names with.
 * Names will always yield the same value for the same input.
 *
 * @author Matt Coley
 */
public class AlphabetNameGenerator implements NameGenerator {
	private final String alphabet;
	private final int boundsMin;
	private final int boundsMax;

	/**
	 * @param alphabet
	 * 		Alphabet to use.
	 * @param length
	 * 		Length of output names.
	 */
	public AlphabetNameGenerator(@Nonnull String alphabet, int length) {
		this.alphabet = alphabet;

		// Create bounds range to generate names of the desired length
		int alphabetSize = alphabet.length();
		int sum = 0;
		int lastCount = 0;
		for (int i = 0; i < length; i++) {
			int count = NumberUtil.intPow(alphabetSize, i + 1);
			sum += count;
			lastCount = count;
		}
		boundsMin = sum - lastCount;
		boundsMax = sum;
	}

	private String name(String original) {
		Random random = new Random(original.hashCode());
		return StringUtil.generateName(alphabet, random.nextInt(boundsMin, boundsMax));
	}

	@Nonnull
	@Override
	@SuppressWarnings("DataFlowIssue")
	public String mapClass(@Nonnull ClassInfo info) {
		if (info.isInDefaultPackage())
			return name(info.getName());

		// Ensure classes in the same package are kept together
		return name(info.getPackageName()) + "/" + name(info.getName());
	}

	@Nonnull
	@Override
	public String mapField(@Nonnull ClassInfo owner, @Nonnull FieldMember field) {
		return name(owner.getName() + field.getName());
	}

	@Nonnull
	@Override
	public String mapMethod(@Nonnull ClassInfo owner, @Nonnull MethodMember method) {
		return name(owner.getName() + method.getName());
	}
}
