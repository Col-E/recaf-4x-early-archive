package software.coley.recaf.ui.control.tree;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import software.coley.recaf.info.AndroidClassInfo;
import software.coley.recaf.info.FileInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.info.builder.AndroidClassInfoBuilder;
import software.coley.recaf.info.builder.TextFileInfoBuilder;
import software.coley.recaf.test.TestClassUtils;
import software.coley.recaf.test.dummy.StringConsumer;
import software.coley.recaf.test.dummy.StringConsumerUser;
import software.coley.recaf.workspace.model.BasicWorkspace;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.bundle.*;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;
import software.coley.recaf.workspace.model.resource.WorkspaceResourceBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link WorkspaceTreePath}.
 */
class WorkspaceTreePathTest {
	static Workspace workspace;
	static WorkspaceResource primaryResource;
	static WorkspaceResource secondaryResource;
	static JvmClassBundle primaryJvmBundle;
	static AndroidClassBundle primaryAndroidBundle;
	static JvmClassBundle secondaryJvmBundle;
	static FileInfo primaryFileInfo;
	static FileBundle primaryFileBundle;
	static JvmClassInfo primaryClassInfo;
	static JvmClassInfo secondaryClassInfo;
	static AndroidClassInfo primaryAndroidClassInfo;
	// Paths
	static WorkspaceTreePath p1;
	static WorkspaceTreePath p2;
	static WorkspaceTreePath p3;
	static WorkspaceTreePath p4;
	static WorkspaceTreePath p5;

	@BeforeAll
	static void setup() throws IOException {
		primaryClassInfo = TestClassUtils.fromRuntimeClass(StringConsumer.class);
		primaryJvmBundle = TestClassUtils.fromClasses(primaryClassInfo);
		primaryFileBundle = new BasicFileBundle();
		primaryFileInfo = new TextFileInfoBuilder().withName("foo").withRawContent("foo".getBytes()).build();
		primaryFileBundle.put(primaryFileInfo);
		primaryAndroidClassInfo = new AndroidClassInfoBuilder().withName("foo").build();
		primaryAndroidBundle = new BasicAndroidClassBundle();
		primaryAndroidBundle.put(primaryAndroidClassInfo);
		Map<String, AndroidClassBundle> androidClassBundles = new HashMap<>();
		androidClassBundles.put("classex.dex", primaryAndroidBundle);
		primaryResource = new WorkspaceResourceBuilder()
				.withJvmClassBundle(primaryJvmBundle)
				.withFileBundle(primaryFileBundle)
				.withAndroidClassBundles(androidClassBundles)
				.build();

		secondaryClassInfo = TestClassUtils.fromRuntimeClass(StringConsumerUser.class);
		secondaryJvmBundle = TestClassUtils.fromClasses(secondaryClassInfo);
		secondaryResource = new WorkspaceResourceBuilder()
				.withJvmClassBundle(secondaryJvmBundle)
				.build();

		workspace = new BasicWorkspace(primaryResource, List.of(secondaryResource));

		p1 = new WorkspaceTreePath(workspace, primaryResource, primaryJvmBundle, primaryClassInfo.getName(), primaryClassInfo);
		p2 = new WorkspaceTreePath(workspace, primaryResource, primaryJvmBundle, primaryClassInfo.getName(), null);
		p3 = new WorkspaceTreePath(workspace, primaryResource, primaryJvmBundle, primaryClassInfo.getName().substring(0, 10), null);
		p4 = new WorkspaceTreePath(workspace, primaryResource, primaryJvmBundle, null, null);
		p5 = new WorkspaceTreePath(workspace, primaryResource, null, null, null);
	}

	@Test
	void isPrimary() {
		assertTrue(new WorkspaceTreePath(workspace, primaryResource, null, null, null).isPrimary());
		assertFalse(new WorkspaceTreePath(workspace, secondaryResource, null, null, null).isPrimary());
	}

	@Test
	void hasBundle() {
		assertTrue(new WorkspaceTreePath(workspace, primaryResource, primaryJvmBundle, primaryClassInfo.getName(), primaryClassInfo).hasBundle());
		assertTrue(new WorkspaceTreePath(workspace, primaryResource, primaryJvmBundle, primaryClassInfo.getName(), null).hasBundle());
		assertTrue(new WorkspaceTreePath(workspace, primaryResource, primaryJvmBundle, null, null).hasBundle());
		assertTrue(new WorkspaceTreePath(workspace, primaryResource, primaryAndroidBundle, primaryAndroidClassInfo.getName(), primaryAndroidClassInfo).hasBundle());
		assertTrue(new WorkspaceTreePath(workspace, primaryResource, primaryAndroidBundle, primaryAndroidClassInfo.getName(), null).hasBundle());
		assertTrue(new WorkspaceTreePath(workspace, primaryResource, primaryAndroidBundle, null, null).hasBundle());
		assertFalse(new WorkspaceTreePath(workspace, primaryResource, null, null, null).hasBundle());
	}

	@Test
	void hasLocalPath() {
		assertTrue(new WorkspaceTreePath(workspace, primaryResource, primaryJvmBundle, primaryClassInfo.getName(), primaryClassInfo).hasLocalPath());
		assertTrue(new WorkspaceTreePath(workspace, primaryResource, primaryJvmBundle, primaryClassInfo.getName(), null).hasLocalPath());
		assertFalse(new WorkspaceTreePath(workspace, primaryResource, primaryJvmBundle, null, null).hasLocalPath());
		assertFalse(new WorkspaceTreePath(workspace, primaryResource, null, null, null).hasLocalPath());
	}

	@Test
	void hasInfo() {
		assertTrue(new WorkspaceTreePath(workspace, primaryResource, primaryAndroidBundle, primaryAndroidClassInfo.getName(), primaryAndroidClassInfo).hasInfo());
		assertTrue(new WorkspaceTreePath(workspace, primaryResource, primaryJvmBundle, primaryClassInfo.getName(), primaryClassInfo).hasInfo());
		assertFalse(new WorkspaceTreePath(workspace, primaryResource, primaryJvmBundle, primaryClassInfo.getName(), null).hasInfo());
		assertFalse(new WorkspaceTreePath(workspace, primaryResource, primaryJvmBundle, null, null).hasInfo());
		assertFalse(new WorkspaceTreePath(workspace, primaryResource, null, null, null).hasInfo());
	}

	@Test
	void isInJvmBundle() {
		assertTrue(new WorkspaceTreePath(workspace, primaryResource, primaryJvmBundle, primaryClassInfo.getName(), primaryClassInfo).isInJvmBundle());
		assertTrue(new WorkspaceTreePath(workspace, primaryResource, primaryJvmBundle, primaryClassInfo.getName(), null).isInJvmBundle());
		assertTrue(new WorkspaceTreePath(workspace, primaryResource, primaryJvmBundle, null, null).isInJvmBundle());
		assertFalse(new WorkspaceTreePath(workspace, primaryResource, null, null, null).isInJvmBundle());
		assertFalse(new WorkspaceTreePath(workspace, primaryResource, primaryFileBundle, null, null).isInJvmBundle());
		assertFalse(new WorkspaceTreePath(workspace, primaryResource, primaryAndroidBundle, null, null).isInJvmBundle());
	}

	@Test
	@Disabled
	void isInAndroidBundle() {
		assertTrue(new WorkspaceTreePath(workspace, primaryResource, primaryAndroidBundle, primaryAndroidClassInfo.getName(), primaryAndroidClassInfo).isInAndroidBundle());
		assertTrue(new WorkspaceTreePath(workspace, primaryResource, primaryAndroidBundle, primaryAndroidClassInfo.getName(), null).isInAndroidBundle());
		assertTrue(new WorkspaceTreePath(workspace, primaryResource, primaryAndroidBundle, null, null).isInAndroidBundle());
		assertFalse(new WorkspaceTreePath(workspace, primaryResource, primaryJvmBundle, primaryClassInfo.getName(), primaryClassInfo).isInAndroidBundle());
		assertFalse(new WorkspaceTreePath(workspace, primaryResource, primaryJvmBundle, primaryClassInfo.getName(), null).isInAndroidBundle());
		assertFalse(new WorkspaceTreePath(workspace, primaryResource, primaryJvmBundle, null, null).isInAndroidBundle());
		assertFalse(new WorkspaceTreePath(workspace, primaryResource, null, null, null).isInAndroidBundle());
		assertFalse(new WorkspaceTreePath(workspace, primaryResource, primaryFileBundle, null, null).isInAndroidBundle());
	}

	@Test
	void isInFileBundle() {
		assertFalse(new WorkspaceTreePath(workspace, primaryResource, primaryJvmBundle, primaryClassInfo.getName(), primaryClassInfo).isInFileBundle());
		assertFalse(new WorkspaceTreePath(workspace, primaryResource, primaryJvmBundle, primaryClassInfo.getName(), null).isInFileBundle());
		assertFalse(new WorkspaceTreePath(workspace, primaryResource, primaryJvmBundle, null, null).isInFileBundle());
		assertFalse(new WorkspaceTreePath(workspace, primaryResource, null, null, null).isInFileBundle());
		assertFalse(new WorkspaceTreePath(workspace, primaryResource, primaryAndroidBundle, null, null).isInFileBundle());
		assertTrue(new WorkspaceTreePath(workspace, primaryResource, primaryFileBundle, null, null).isInFileBundle());
	}

	@Test
	@Disabled
	void isInVersionedJvmBundle() {
		assertFalse(new WorkspaceTreePath(workspace, primaryResource, null, null, null).isInVersionedJvmBundle());
	}

	@Test
	@Disabled
	void isEmbeddedContainer() {
		assertFalse(new WorkspaceTreePath(workspace, primaryResource, null, null, null).isEmbeddedContainer());
	}

	@Nested
	class Descendant {

		@Test
		void childDescendantOfParent() {
			// Descendant of parent
			assertTrue(p1.isDescendantOf(p2));
			assertTrue(p1.isDescendantOf(p3));
			assertTrue(p1.isDescendantOf(p4));
			assertTrue(p1.isDescendantOf(p5));
			assertTrue(p2.isDescendantOf(p3));
			assertTrue(p2.isDescendantOf(p4));
			assertTrue(p2.isDescendantOf(p5));
			assertTrue(p3.isDescendantOf(p4));
			assertTrue(p3.isDescendantOf(p5));
			assertTrue(p4.isDescendantOf(p5));
		}

		@Test
		void descendantOfSelf() {
			// Descendant of self
			assertTrue(p1.isDescendantOf(p1));
			assertTrue(p2.isDescendantOf(p2));
			assertTrue(p3.isDescendantOf(p3));
			assertTrue(p4.isDescendantOf(p4));
			assertTrue(p5.isDescendantOf(p5));
		}

		@Test
		void parentNotDescendantOfChild() {
			// Parent is not descendant of child
			assertFalse(p5.isDescendantOf(p4));
			assertFalse(p5.isDescendantOf(p3));
			assertFalse(p5.isDescendantOf(p2));
			assertFalse(p5.isDescendantOf(p1));
			assertFalse(p4.isDescendantOf(p3));
			assertFalse(p4.isDescendantOf(p2));
			assertFalse(p4.isDescendantOf(p1));
			assertFalse(p3.isDescendantOf(p2));
			assertFalse(p3.isDescendantOf(p1));
			assertFalse(p2.isDescendantOf(p1));
		}
	}

	@Nested
	class Comparison {


		@Test // ignore warning about 'compareTo' on self
		@SuppressWarnings("all")
		void compareToSelfIsZero() {
			// Self comparison or equal items should always be 0
			assertEquals(0, p1.compareTo(p1));
			assertEquals(0, p2.compareTo(p2));
			assertEquals(0, p3.compareTo(p3));
			assertEquals(0, p4.compareTo(p4));
			assertEquals(0, p5.compareTo(p5));
		}

		@Test
		void compareToParentIsGreater() {
			// Children appear last (thus > 0)
			assertTrue(p1.compareTo(p2) > 0);
			assertTrue(p2.compareTo(p3) > 0);
			assertTrue(p3.compareTo(p4) > 0);
			assertTrue(p4.compareTo(p5) > 0);
		}

		@Test
		void compareToChildIsLess() {
			// Parents appear first (thus < 0)
			assertTrue(p5.compareTo(p4) < 0);
			assertTrue(p5.compareTo(p3) < 0);
			assertTrue(p5.compareTo(p2) < 0);
			assertTrue(p5.compareTo(p1) < 0);
			assertTrue(p4.compareTo(p3) < 0);
			assertTrue(p4.compareTo(p2) < 0);
			assertTrue(p4.compareTo(p1) < 0);
			assertTrue(p3.compareTo(p2) < 0);
			assertTrue(p3.compareTo(p1) < 0);
			assertTrue(p2.compareTo(p1) < 0);
		}
	}
}