package com.github.encryptsl.credit;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings({"UnstableApiUsage", "unused"})
public class CreditLoader implements PluginLoader {
    @Override
    public void classloader(@NotNull PluginClasspathBuilder pluginClasspathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        resolver.addDependency(new Dependency(new DefaultArtifact("com.zaxxer:HikariCP:5.1.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.9.23"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.jetbrains.exposed:exposed-core:0.49.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.jetbrains.exposed:exposed-jdbc:0.49.0"), null));
        resolver.addDependency(new Dependency(new DefaultArtifact("org.jetbrains.exposed:exposed-kotlin-datetime:0.49.0"), null));

        resolver.addRepository(new RemoteRepository.Builder("paper", "default", "https://repo.papermc.io/repository/maven-public/").build());
    }
}
