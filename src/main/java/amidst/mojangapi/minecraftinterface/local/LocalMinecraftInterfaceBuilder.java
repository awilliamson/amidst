package amidst.mojangapi.minecraftinterface.local;

import java.io.IOException;
import java.net.URLClassLoader;
import java.util.Map;

import amidst.clazz.Classes;
import amidst.clazz.real.JarFileParsingException;
import amidst.clazz.symbolic.SymbolicClass;
import amidst.clazz.symbolic.SymbolicClassGraphCreationException;
import amidst.clazz.translator.ClassTranslator;
import amidst.documentation.Immutable;
import amidst.documentation.NotNull;
import amidst.logging.AmidstLogger;
import amidst.mojangapi.file.directory.DotMinecraftDirectory;
import amidst.mojangapi.file.directory.VersionDirectory;
import amidst.mojangapi.file.service.ClassLoaderService;
import amidst.mojangapi.minecraftinterface.RecognisedVersion;
import amidst.parsing.FormatException;

@Immutable
public class LocalMinecraftInterfaceBuilder {
	private final ClassTranslator translator;

	public LocalMinecraftInterfaceBuilder(ClassTranslator translator) {
		this.translator = translator;
	}

	@NotNull
	public LocalMinecraftInterface create(
			VersionDirectory versionDirectory,
			DotMinecraftDirectory dotMinecraftDirectory) throws LocalMinecraftInterfaceCreationException {
		try {
			URLClassLoader classLoader = new ClassLoaderService()
					.createClassLoader(versionDirectory, dotMinecraftDirectory);
			RecognisedVersion recognisedVersion = RecognisedVersion.from(classLoader);
			Map<String, SymbolicClass> symbolicClassMap = Classes
					.createSymbolicClassMap(versionDirectory.getJar(), classLoader, translator);
			AmidstLogger.info("Minecraft load complete.");
			return new LocalMinecraftInterface(
					symbolicClassMap.get(SymbolicNames.CLASS_INT_CACHE),
					symbolicClassMap.get(SymbolicNames.CLASS_BLOCK_INIT),
					symbolicClassMap.get(SymbolicNames.CLASS_GEN_LAYER),
					symbolicClassMap.get(SymbolicNames.CLASS_WORLD_TYPE),
					symbolicClassMap.get(SymbolicNames.CLASS_GEN_OPTIONS_FACTORY),
					recognisedVersion);
		} catch (
				ClassNotFoundException
				| JarFileParsingException
				| SymbolicClassGraphCreationException
				| FormatException
				| IOException e) {
			throw new LocalMinecraftInterfaceCreationException("unable to create local minecraft interface", e);
		}
	}
}
