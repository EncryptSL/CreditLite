package com.github.encryptsl.credit;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public class CreditLoader implements PluginLoader {

    @Override
    public void classloader(@NotNull PluginClasspathBuilder pluginClasspathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolveLibraries().stream()
                .map(DefaultArtifact::new)
                .forEach(artifact -> resolver.addDependency(new Dependency(artifact, null)));
        resolver.addRepository(new RemoteRepository.Builder(
                "maven", "default", "https://repo.maven.apache.org/maven2/"
        ).build());

        resolver.addRepository(new RemoteRepository.Builder("paper", "default", "https://repo.papermc.io/repository/maven-public/").build());

        pluginClasspathBuilder.addLibrary(resolver);
    }

    private List<String> resolveLibraries() {
        try {
            return readLibraryListFromYaml();
        } catch (IOException e) {
            e.fillInStackTrace();
        }
        return new ArrayList<>();
    }

    private List<String> readLibraryListFromYaml() throws IOException {
        List<String> libraries = new ArrayList<>();
        InputStream inputStream = CreditLoader.class.getClassLoader()
                .getResourceAsStream("paper-libraries.yml");

        if (inputStream != null) {
            try (inputStream; Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8)) {
                while (scanner.hasNextLine()) {
                    libraries.add(scanner.nextLine());
                }
            }
        } else {
            System.err.println("paper-libraries.yml not found in the classpath.");
        }
        return libraries;
    }
}
