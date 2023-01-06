package software.coley.recaf.services.mapping;

import jakarta.annotation.Nonnull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.coley.recaf.TestBase;
import software.coley.recaf.info.ClassInfo;
import software.coley.recaf.info.JvmClassInfo;
import software.coley.recaf.info.member.FieldMember;
import software.coley.recaf.info.member.MethodMember;
import software.coley.recaf.services.inheritance.InheritanceGraph;
import software.coley.recaf.services.mapping.aggregate.AggregateMappingManager;
import software.coley.recaf.services.mapping.aggregate.AggregatedMappings;
import software.coley.recaf.services.mapping.gen.MappingGenerator;
import software.coley.recaf.services.mapping.gen.NameGenerator;
import software.coley.recaf.services.mapping.gen.NameGeneratorFilter;
import software.coley.recaf.test.TestClassUtils;
import software.coley.recaf.test.dummy.*;
import software.coley.recaf.util.ClassDefiner;
import software.coley.recaf.util.StringUtil;
import software.coley.recaf.workspace.model.Workspace;
import software.coley.recaf.workspace.model.resource.WorkspaceResource;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link MappingApplier} with some edge case classes.
 */
class MappingApplierTest extends TestBase {
	static NameGenerator nameGenerator;
	static MappingGenerator mappingGenerator;
	Workspace workspace;
	WorkspaceResource resource;
	AggregateMappingManager aggregateMappingManager;
	InheritanceGraph inheritanceGraph;
	MappingApplier mappingApplier;

	@BeforeAll
	static void setupGenerator() {
		String alphabet = "abcdefghijklmnopqrstuvwxyz";
		nameGenerator = new NameGenerator() {
			private String name(String original) {
				Random random = new Random(original.hashCode());
				return StringUtil.generateName(alphabet, random.nextInt(1_000, 10_000));
			}

			@Nonnull
			@Override
			public String mapClass(@Nonnull ClassInfo info) {
				return name(info.getName());
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
		};
	}

	@BeforeEach
	void prepareWorkspace() throws IOException {
		// We want to reset the workspace before each test
		workspace = TestClassUtils.fromBundle(TestClassUtils.fromClasses(
				AnonymousLambda.class,
				StringSupplier.class,
				//
				DummyEnum.class,
				DummyEnumPrinter.class,
				//
				OverlapInterfaceA.class,
				OverlapInterfaceB.class,
				OverlapClassAB.class,
				OverlapCaller.class
		));
		resource = workspace.getPrimaryResource();
		workspaceManager.setCurrent(workspace);
		aggregateMappingManager = recaf.get(AggregateMappingManager.class);
		inheritanceGraph = recaf.get(InheritanceGraph.class);
		mappingGenerator = recaf.get(MappingGenerator.class);
		mappingApplier = recaf.get(MappingApplier.class);
	}

	@Test
	void applyAnonymousLambda() {
		String stringSupplierName = StringSupplier.class.getName().replace('.', '/');
		String anonymousLambdaName = AnonymousLambda.class.getName().replace('.', '/');

		// Create mappings for all classes but the runner 'AnonymousLambda'
		Mappings mappings = mappingGenerator.generate(resource, inheritanceGraph, nameGenerator, new NameGeneratorFilter(null, true) {
			@Override
			public boolean shouldMapClass(@Nonnull ClassInfo info) {
				return !info.getName().equals(anonymousLambdaName);
			}

			@Override
			public boolean shouldMapMethod(@Nonnull ClassInfo owner, @Nonnull MethodMember method) {
				return shouldMapClass(owner);
			}
		});

		// Apply the mappings to the workspace
		Set<String> modified = mappingApplier.apply(mappings, workspace);

		// The supplier class we define should be remapped.
		// The runner class (AnonymousLambda) itself should not be remapped, but should be updated to point to
		// the new StringSupplier class name.
		assertNotNull(mappings.getMappedClassName(stringSupplierName), "StringSupplier should be remapped");
		assertNull(mappings.getMappedClassName(anonymousLambdaName), "AnonymousLambda should not be remapped");
		assertTrue(modified.contains(stringSupplierName), "StringSupplier should have updated");
		assertTrue(modified.contains(anonymousLambdaName), "AnonymousLambda should have updated");

		// Assert aggregate updated too.
		AggregatedMappings aggregatedMappings = aggregateMappingManager.getAggregatedMappings();
		assertNotNull(aggregatedMappings.getMappedClassName(stringSupplierName),
				"StringSupplier should be tracked in aggregate");

		// Assert that the method is still runnable.
		String result = run(AnonymousLambda.class, "run");
		assertTrue(result.contains("One: java.util.function.Supplier"),
				"JDK class reference should not be mapped");
		assertFalse(result.contains(stringSupplierName),
				"Class reference to '" + stringSupplierName + "' should have been remapped");
	}

	@Test
	void applyDummyEnumPrinter() {
		String dummyEnumName = DummyEnum.class.getName().replace('.', '/');
		String dummyEnumPrinterName = DummyEnumPrinter.class.getName().replace('.', '/');

		// Create mappings for all classes but the runner 'DummyEnumPrinter'
		Mappings mappings = mappingGenerator.generate(resource, inheritanceGraph, nameGenerator, new NameGeneratorFilter(null, true) {
			@Override
			public boolean shouldMapClass(@Nonnull ClassInfo info) {
				return !info.getName().equals(dummyEnumPrinterName);
			}

			@Override
			public boolean shouldMapMethod(@Nonnull ClassInfo owner, @Nonnull MethodMember method) {
				return shouldMapClass(owner);
			}
		});

		// Apply the mappings to the workspace
		Set<String> modified = mappingApplier.apply(mappings, workspace);

		// The enum class we define should be remapped.
		// The runner class (DummyEnumPrinter) itself should not be remapped, but should be updated to point to
		// the new DummyEnum class name.
		assertNotNull(mappings.getMappedClassName(dummyEnumName), "DummyEnum should be remapped");
		assertNull(mappings.getMappedClassName(dummyEnumPrinterName), "DummyEnumPrinter should not be remapped");
		assertNull(mappings.getMappedMethodName(dummyEnumName, "values", "()[L" + dummyEnumName + ";"),
				"DummyEnum#values() should not be remapped");
		assertNull(mappings.getMappedMethodName(dummyEnumName, "valueOf", "(Ljava/lang/String;)L" + dummyEnumName + ";"),
				"DummyEnum#valueOf(String) should not be remapped");
		assertTrue(modified.contains(dummyEnumName), "DummyEnum should have updated");
		assertTrue(modified.contains(dummyEnumPrinterName), "DummyEnumPrinter should have updated");

		// Assert aggregate updated too.
		AggregatedMappings aggregatedMappings = aggregateMappingManager.getAggregatedMappings();
		assertNotNull(aggregatedMappings.getMappedClassName(dummyEnumName),
				"DummyEnum should be tracked in aggregate");

		// Assert that the methods are still runnable.
		run(DummyEnumPrinter.class, "run1");
		run(DummyEnumPrinter.class, "run2");
	}

	@Test
	void applyOverlapping() {
		String overlapInterfaceAName = OverlapInterfaceA.class.getName().replace('.', '/');
		String overlapInterfaceBName = OverlapInterfaceB.class.getName().replace('.', '/');
		String overlapClassABName = OverlapClassAB.class.getName().replace('.', '/');
		String overlapCallerName = OverlapCaller.class.getName().replace('.', '/');

		// Create mappings for all classes but the runner 'OverlapCaller'
		Mappings mappings = mappingGenerator.generate(resource, inheritanceGraph, nameGenerator, new NameGeneratorFilter(null, true) {
			@Override
			public boolean shouldMapClass(@Nonnull ClassInfo info) {
				return !info.getName().equals(overlapCallerName);
			}

			@Override
			public boolean shouldMapMethod(@Nonnull ClassInfo owner, @Nonnull MethodMember method) {
				return shouldMapClass(owner);
			}
		});

		// Apply the mappings to the workspace
		Set<String> modified = mappingApplier.apply(mappings, workspace);

		assertNotNull(mappings.getMappedClassName(overlapInterfaceAName), "OverlapInterfaceA should be remapped");
		assertNotNull(mappings.getMappedClassName(overlapInterfaceBName), "OverlapInterfaceB should be remapped");
		assertNotNull(mappings.getMappedClassName(overlapClassABName), "OverlapClassAB should be remapped");
		assertNull(mappings.getMappedClassName(overlapCallerName), "OverlapCaller should not be remapped");
		assertTrue(modified.contains(overlapInterfaceAName), "OverlapInterfaceA should have updated");
		assertTrue(modified.contains(overlapInterfaceBName), "OverlapInterfaceB should have updated");
		assertTrue(modified.contains(overlapClassABName), "OverlapClassAB should have updated");
		assertTrue(modified.contains(overlapCallerName), "OverlapCaller should have updated");

		// Assert aggregate updated too.
		AggregatedMappings aggregatedMappings = aggregateMappingManager.getAggregatedMappings();
		assertNotNull(aggregatedMappings.getMappedClassName(overlapInterfaceAName),
				"OverlapInterfaceA should be tracked in aggregate");
		assertNotNull(aggregatedMappings.getMappedClassName(overlapInterfaceBName),
				"OverlapInterfaceB should be tracked in aggregate");
		assertNotNull(aggregatedMappings.getMappedClassName(overlapClassABName),
				"OverlapClassAB should be tracked in aggregate");
		assertNull(aggregatedMappings.getMappedClassName(overlapCallerName),
				"OverlapCaller should not be tracked in aggregate");

		// Assert that the method is still runnable.
		run(OverlapCaller.class, "run");
	}

	private String run(Class<?> cls, String methodName) {
		String className = cls.getName();
		ClassDefiner definer = newDefiner();
		try {
			Class<?> runner = definer.findClass(className);
			Method main = runner.getDeclaredMethod(methodName);
			try {
				return (String) main.invoke(null);
			} catch (ReflectiveOperationException ex) {
				fail("Failed to execute '" + methodName + "' method", ex);
			}
		} catch (ClassNotFoundException | NoSuchMethodException ex) {
			fail("Class '" + className + "' or '" + methodName + "' method missing", ex);
		}
		throw new IllegalStateException();
	}

	private ClassDefiner newDefiner() {
		Map<String, byte[]> map = resource.getJvmClassBundle().values().stream()
				.collect(Collectors.toMap(e -> e.getName().replace('/', '.'), JvmClassInfo::getBytecode));
		return new ClassDefiner(map);
	}
}