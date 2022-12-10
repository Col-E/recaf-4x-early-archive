package software.coley.recaf.workspace.io;

import jakarta.enterprise.context.ApplicationScoped;
import software.coley.recaf.info.Info;
import software.coley.recaf.util.io.ByteSource;

/**
 * Basic implementation of the info importer.
 *
 * @author Matt Coley
 */
@ApplicationScoped
public class BasicInfoImporter implements InfoImporter {
	@Override
	public Info readInfo(String name, ByteSource source) {
		// TODO: Everything
		//  - class (jvm only) [inject patcher service, allow users to add their own, use to fix illegal class files]
		//  - file
		//     - zip (use name extension if available, otherwise assume plain ZIP)
		//       - jar
		//       - jmod
		//       - war
		//       - regular zip
		//     - regular file

		// TODO: Record properties?
		//  - content-type (for quick recognition of media and other common file types)
		//      - best to do header matching once instead of each time on request
		return null;
	}
}
