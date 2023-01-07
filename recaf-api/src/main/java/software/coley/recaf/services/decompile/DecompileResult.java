package software.coley.recaf.services.decompile;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.Objects;

/**
 * Result for a {@link Decompiler} output.
 *
 * @author Matt Coley
 */
public class DecompileResult {
	private final String text;
	private final Throwable exception;
	private final ResultType type;

	/**
	 * @param text
	 * 		Decompiled text.
	 * @param exception
	 * 		Failure reason.
	 * @param type
	 * 		Result type.
	 */
	public DecompileResult(@Nullable String text, @Nullable Throwable exception, @Nonnull ResultType type) {
		this.text = text;
		this.exception = exception;
		this.type = type;
	}

	/**
	 * @return Decompiled text.
	 * May be {@code null} when {@link #getType()} is not {@link ResultType#SUCCESS}.
	 */
	@Nullable
	public String getText() {
		return text;
	}

	/**
	 * @return Failure reason.
	 * May be {@code null} when {@link #getType()} is not {@link ResultType#FAILURE}.
	 */
	@Nullable
	public Throwable getException() {
		return exception;
	}

	/**
	 * @return Result type.
	 */
	@Nonnull
	public ResultType getType() {
		return type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		DecompileResult other = (DecompileResult) o;

		if (!Objects.equals(text, other.text)) return false;
		if (!Objects.equals(exception, other.exception)) return false;
		return type == other.type;
	}

	@Override
	public int hashCode() {
		int result = text != null ? text.hashCode() : 0;
		result = 31 * result + (exception != null ? exception.hashCode() : 0);
		result = 31 * result + type.hashCode();
		return result;
	}

	/**
	 * Type of result.
	 */
	public enum ResultType {
		/**
		 * Successful decompilation.
		 */
		SUCCESS,
		/**
		 * Decompilation skipped for some reason. Likely due to a thread being cancelled.
		 */
		SKIPPED,
		/**
		 * Decompilation failed to emit any output.
		 */
		FAILURE
	}
}
