package software.coley.recaf.cdi;

import jakarta.interceptor.InterceptorBinding;
import software.coley.recaf.workspace.WorkspaceCloseListener;
import software.coley.recaf.workspace.WorkspaceModificationListener;
import software.coley.recaf.workspace.WorkspaceOpenListener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Applied to bean implementations that want to automatically register workspace listeners:
 * <ul>
 *     <li>{@link WorkspaceOpenListener}</li>
 *     <li>{@link WorkspaceModificationListener}</li>
 *     <li>{@link WorkspaceCloseListener}</li>
 * </ul>
 *
 * @author Matt Coley
 */
@InterceptorBinding
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AutoRegisterWorkspaceListeners {
}
