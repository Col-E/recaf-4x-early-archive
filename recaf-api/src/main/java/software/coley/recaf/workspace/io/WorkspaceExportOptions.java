package software.coley.recaf.workspace.io;

/**
 * Options for configuring / preparing a {@link WorkspaceExporter} when calling
 * {@link software.coley.recaf.workspace.WorkspaceManager#createExporter(WorkspaceExportOptions)}.
 *
 * @author Matt Coley
 */
public class WorkspaceExportOptions {
	// TODO: Flesh out
	//  - Builder pattern locally? Separate class?
	//  - How to structure this to allow clean implementations of exporter with ONE job
	//     vs a chain of if-else-if-else for all cases
	//      - enum for export target types (same directory / zip / jar / war)
	//      - want some level of automatic "same as input type"
	//          - workspace resources retain this information
}
