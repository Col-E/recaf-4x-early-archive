package software.coley.recaf.workspace.processors;

import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import software.coley.recaf.cdi.WorkspaceScoped;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.properties.builtin.ThrowableProperty;
import software.coley.recaf.services.inheritance.InheritanceGraph;
import software.coley.recaf.workspace.WorkspaceProcessor;
import software.coley.recaf.workspace.model.Workspace;

/**
 * Workspace processor that marks {@link ClassInfo} values that inherit from {@link Throwable}
 * as having a {@link ThrowableProperty}. This allows instant look-ups for if a class is throwable,
 * by bypassing repeated calls to {@link InheritanceGraph}.
 *
 * @author Matt Coley
 */
@WorkspaceScoped
public class ThrowablePropertyAssigning implements WorkspaceProcessor {
	private final InheritanceGraph graph;

	@Inject
	public ThrowablePropertyAssigning(InheritanceGraph graph) {
		this.graph = graph;
	}

	@Override
	public void onWorkspaceOpened(@Nonnull Workspace workspace) {
		graph.getVertex("java/lang/Throwable").allChildren().forEach(vertex -> {
			ClassInfo classInfo = vertex.getValue();
			ThrowableProperty.set(classInfo);
		});
	}

	@Override
	public String name() {
		return "Mark throwable types";
	}
}
