package io.github.hectorvent.floci.services.opensearch;

import io.github.hectorvent.floci.config.EmulatorConfig;
import io.github.hectorvent.floci.core.common.RegionResolver;
import io.github.hectorvent.floci.core.common.docker.ContainerBuilder;
import io.github.hectorvent.floci.core.common.docker.ContainerDetector;
import io.github.hectorvent.floci.core.common.docker.ContainerLifecycleManager;
import io.github.hectorvent.floci.core.common.docker.ContainerSpec;
import io.github.hectorvent.floci.core.common.docker.ContainerStorageHelper;
import io.github.hectorvent.floci.core.common.docker.PortAllocator;
import io.github.hectorvent.floci.services.opensearch.model.Domain;
import io.github.hectorvent.floci.services.opensearch.model.VolumeOwnership;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OpenSearchDomainManagerTest {

    @Test
    void startDomainRunsTheContainerUnderAnInitProcess() {
        EmulatorConfig config = mock(EmulatorConfig.class);
        EmulatorConfig.ServicesConfig services = mock(EmulatorConfig.ServicesConfig.class);
        EmulatorConfig.OpenSearchServiceConfig opensearch = mock(EmulatorConfig.OpenSearchServiceConfig.class);
        when(config.services()).thenReturn(services);
        when(services.opensearch()).thenReturn(opensearch);
        when(services.dockerNetwork()).thenReturn(Optional.empty());
        when(opensearch.proxyBasePort()).thenReturn(9400);
        when(opensearch.proxyMaxPort()).thenReturn(9499);
        when(opensearch.defaultImage()).thenReturn(Optional.of("opensearchproject/opensearch:2.11.0"));
        when(opensearch.dataVolume()).thenReturn(Optional.empty());
        EmulatorConfig.DockerConfig docker = mock(EmulatorConfig.DockerConfig.class);
        when(config.docker()).thenReturn(docker);
        lenient().when(docker.logMaxSize()).thenReturn("10m");
        lenient().when(docker.logMaxFile()).thenReturn("3");
        EmulatorConfig.StorageConfig storage = mock(EmulatorConfig.StorageConfig.class);
        when(config.storage()).thenReturn(storage);
        when(storage.hostPersistentPath()).thenReturn("floci-data");
        when(storage.persistentPath()).thenReturn("/data");

        PortAllocator portAllocator = mock(PortAllocator.class);
        when(portAllocator.allocate(9400, 9499)).thenReturn(9400);

        ContainerLifecycleManager lifecycleManager = mock(ContainerLifecycleManager.class);
        when(lifecycleManager.createAndStart(any())).thenReturn(
                new ContainerLifecycleManager.ContainerInfo("container-id", Map.of(
                        9200, new ContainerLifecycleManager.EndpointInfo("172.17.0.5", 9200))));

        ContainerBuilder containerBuilder = mock(ContainerBuilder.class);
        ContainerBuilder.Builder builder = mock(ContainerBuilder.Builder.class, RETURNS_SELF);
        when(containerBuilder.newContainer(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(mock(ContainerSpec.class));

        RegionResolver regionResolver = mock(RegionResolver.class);
        when(regionResolver.getAccountId()).thenReturn("000000000000");
        when(regionResolver.getDefaultRegion()).thenReturn("us-east-1");

        OpenSearchDomainManager manager = new OpenSearchDomainManager(containerBuilder, lifecycleManager,
                mock(ContainerDetector.class), portAllocator, config, regionResolver);

        Domain domain = new Domain();
        domain.setDomainName("my-domain");
        domain.setEngineVersion("OpenSearch_2.11");

        manager.startDomain(domain);

        verify(builder).withInit();
    }

    @Test
    void startDomainLabelsContainerWithResourceIdentity() {
        EmulatorConfig config = mock(EmulatorConfig.class);
        EmulatorConfig.ServicesConfig services = mock(EmulatorConfig.ServicesConfig.class);
        EmulatorConfig.OpenSearchServiceConfig opensearch = mock(EmulatorConfig.OpenSearchServiceConfig.class);
        when(config.services()).thenReturn(services);
        when(services.opensearch()).thenReturn(opensearch);
        when(services.dockerNetwork()).thenReturn(Optional.empty());
        when(opensearch.proxyBasePort()).thenReturn(9400);
        when(opensearch.proxyMaxPort()).thenReturn(9499);
        when(opensearch.defaultImage()).thenReturn(Optional.of("opensearchproject/opensearch:2.11.0"));
        when(opensearch.dataVolume()).thenReturn(Optional.empty());
        EmulatorConfig.DockerConfig docker = mock(EmulatorConfig.DockerConfig.class);
        when(config.docker()).thenReturn(docker);
        lenient().when(docker.logMaxSize()).thenReturn("10m");
        lenient().when(docker.logMaxFile()).thenReturn("3");
        EmulatorConfig.StorageConfig storage = mock(EmulatorConfig.StorageConfig.class);
        when(config.storage()).thenReturn(storage);
        when(storage.hostPersistentPath()).thenReturn("floci-data");
        when(storage.persistentPath()).thenReturn("/data");

        PortAllocator portAllocator = mock(PortAllocator.class);
        when(portAllocator.allocate(9400, 9499)).thenReturn(9400);

        ContainerLifecycleManager lifecycleManager = mock(ContainerLifecycleManager.class);
        when(lifecycleManager.createAndStart(any())).thenReturn(
                new ContainerLifecycleManager.ContainerInfo("container-id", Map.of(
                        9200, new ContainerLifecycleManager.EndpointInfo("172.17.0.5", 9200))));

        ContainerBuilder containerBuilder = mock(ContainerBuilder.class);
        ContainerBuilder.Builder builder = mock(ContainerBuilder.Builder.class, RETURNS_SELF);
        when(containerBuilder.newContainer(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(mock(ContainerSpec.class));

        RegionResolver regionResolver = mock(RegionResolver.class);
        when(regionResolver.getAccountId()).thenReturn("000000000000");
        when(regionResolver.getDefaultRegion()).thenReturn("us-east-1");

        OpenSearchDomainManager manager = new OpenSearchDomainManager(containerBuilder, lifecycleManager,
                mock(ContainerDetector.class), portAllocator, config, regionResolver);

        Domain domain = new Domain();
        domain.setDomainName("my-domain");
        domain.setEngineVersion("OpenSearch_2.11");

        manager.startDomain(domain);

        assertEquals(VolumeOwnership.FLOCI_MANAGED, domain.getVolumeOwnership());
        verify(builder).withLabels(Map.of(
                "io.floci", "aws",
                "io.floci.service", "opensearch",
                "io.floci.resource-id", "my-domain",
                "io.floci.account", "000000000000",
                "io.floci.region", "us-east-1"));
    }

    /**
     * OpenSearch has no floci-internal proxy fronting the backend, so the Docker
     * host-port binding is the only path to the domain for a client outside the
     * Docker network. It must be published even when floci itself runs inside a
     * container, while the stored endpoint stays the container-IP one floci and
     * same-network clients use.
     */
    @Test
    void startDomainPublishesAHostPortEvenWhenFlociRunsInAContainer() {
        EmulatorConfig config = mock(EmulatorConfig.class);
        EmulatorConfig.ServicesConfig services = mock(EmulatorConfig.ServicesConfig.class);
        EmulatorConfig.OpenSearchServiceConfig opensearch = mock(EmulatorConfig.OpenSearchServiceConfig.class);
        when(config.services()).thenReturn(services);
        when(services.opensearch()).thenReturn(opensearch);
        when(services.dockerNetwork()).thenReturn(Optional.empty());
        when(opensearch.proxyBasePort()).thenReturn(9400);
        when(opensearch.proxyMaxPort()).thenReturn(9499);
        when(opensearch.defaultImage()).thenReturn(Optional.of("opensearchproject/opensearch:2.11.0"));
        when(opensearch.dataVolume()).thenReturn(Optional.empty());
        EmulatorConfig.DockerConfig docker = mock(EmulatorConfig.DockerConfig.class);
        when(config.docker()).thenReturn(docker);
        lenient().when(docker.logMaxSize()).thenReturn("10m");
        lenient().when(docker.logMaxFile()).thenReturn("3");
        EmulatorConfig.StorageConfig storage = mock(EmulatorConfig.StorageConfig.class);
        when(config.storage()).thenReturn(storage);
        when(storage.hostPersistentPath()).thenReturn("floci-data");
        when(storage.persistentPath()).thenReturn("/data");

        PortAllocator portAllocator = mock(PortAllocator.class);
        when(portAllocator.allocate(9400, 9499)).thenReturn(9401);

        ContainerLifecycleManager lifecycleManager = mock(ContainerLifecycleManager.class);
        when(lifecycleManager.createAndStart(any())).thenReturn(
                new ContainerLifecycleManager.ContainerInfo("container-id", Map.of(
                        9200, new ContainerLifecycleManager.EndpointInfo("172.17.0.5", 9200))));

        ContainerBuilder containerBuilder = mock(ContainerBuilder.class);
        ContainerBuilder.Builder builder = mock(ContainerBuilder.Builder.class, RETURNS_SELF);
        when(containerBuilder.newContainer(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(mock(ContainerSpec.class));

        RegionResolver regionResolver = mock(RegionResolver.class);
        when(regionResolver.getAccountId()).thenReturn("000000000000");
        when(regionResolver.getDefaultRegion()).thenReturn("us-east-1");

        ContainerDetector containerDetector = mock(ContainerDetector.class);
        lenient().when(containerDetector.isRunningInContainer()).thenReturn(true);

        OpenSearchDomainManager manager = new OpenSearchDomainManager(containerBuilder, lifecycleManager,
                containerDetector, portAllocator, config, regionResolver);

        Domain domain = new Domain();
        domain.setDomainName("host-reach");
        domain.setEngineVersion("OpenSearch_2.11");

        manager.startDomain(domain);

        verify(portAllocator).allocate(9400, 9499);
        verify(builder).withPortBinding(9200, 9401);
        assertEquals("http://172.17.0.5:9200", domain.getEndpoint());
        assertEquals(Integer.valueOf(9401), domain.getHostPort());
    }

    /**
     * The reservation must die with the container: without the release, repeated
     * create/delete cycles exhaust the configured port range and later domains
     * can never start.
     */
    @Test
    void stopDomainReleasesTheHostPort() {
        EmulatorConfig config = mock(EmulatorConfig.class);
        EmulatorConfig.ServicesConfig services = mock(EmulatorConfig.ServicesConfig.class);
        EmulatorConfig.OpenSearchServiceConfig opensearch = mock(EmulatorConfig.OpenSearchServiceConfig.class);
        when(config.services()).thenReturn(services);
        when(services.opensearch()).thenReturn(opensearch);
        when(opensearch.keepRunningOnShutdown()).thenReturn(false);

        PortAllocator portAllocator = mock(PortAllocator.class);
        ContainerLifecycleManager lifecycleManager = mock(ContainerLifecycleManager.class);

        OpenSearchDomainManager manager = new OpenSearchDomainManager(mock(ContainerBuilder.class),
                lifecycleManager, mock(ContainerDetector.class), portAllocator, config,
                mock(RegionResolver.class));

        Domain domain = new Domain();
        domain.setDomainName("teardown");
        domain.setContainerId("container-id");
        domain.setHostPort(9401);

        manager.stopDomain(domain);

        verify(lifecycleManager).stopAndRemove("container-id", null);
        verify(portAllocator).release(9401);
        assertNull(domain.getHostPort());
    }

    /**
     * Explicit DeleteDomain tears down through {@code stopDomain}. The shutdown retention
     * option is decided by {@link OpenSearchService#shutdown()}, so it must not leave a deleted
     * domain's container running or its port reserved.
     */
    @Test
    void stopDomainRemovesTheContainerEvenWhenShutdownRetentionIsEnabled() {
        EmulatorConfig config = mock(EmulatorConfig.class);
        EmulatorConfig.ServicesConfig services = mock(EmulatorConfig.ServicesConfig.class);
        EmulatorConfig.OpenSearchServiceConfig opensearch = mock(EmulatorConfig.OpenSearchServiceConfig.class);
        when(config.services()).thenReturn(services);
        when(services.opensearch()).thenReturn(opensearch);
        when(opensearch.keepRunningOnShutdown()).thenReturn(true);

        PortAllocator portAllocator = mock(PortAllocator.class);
        ContainerLifecycleManager lifecycleManager = mock(ContainerLifecycleManager.class);

        OpenSearchDomainManager manager = new OpenSearchDomainManager(mock(ContainerBuilder.class),
                lifecycleManager, mock(ContainerDetector.class), portAllocator, config,
                mock(RegionResolver.class));

        Domain domain = new Domain();
        domain.setDomainName("deleted-domain");
        domain.setContainerId("container-id");
        domain.setHostPort(9401);

        manager.stopDomain(domain);

        verify(lifecycleManager).stopAndRemove("container-id", null);
        verify(portAllocator).release(9401);
    }

    /**
     * A configured Docker volume name gives every domain its own
     * {@code <name>-<domain-name>} volume, so no two domains share a data directory;
     * the volume is created, when absent, without Floci labels so no sweep of
     * Floci-labelled volumes collects operator storage; and the domain record is
     * stamped with that name and the operator ownership marker so delete can tell
     * operator storage from Floci-derived storage.
     */
    @Test
    void startDomainMountsConfiguredNamedDataVolume() {
        EmulatorConfig config = mock(EmulatorConfig.class);
        EmulatorConfig.ServicesConfig services = mock(EmulatorConfig.ServicesConfig.class);
        EmulatorConfig.OpenSearchServiceConfig opensearch = mock(EmulatorConfig.OpenSearchServiceConfig.class);
        when(config.services()).thenReturn(services);
        when(services.opensearch()).thenReturn(opensearch);
        when(services.dockerNetwork()).thenReturn(Optional.empty());
        when(opensearch.proxyBasePort()).thenReturn(9400);
        when(opensearch.proxyMaxPort()).thenReturn(9499);
        when(opensearch.defaultImage()).thenReturn(Optional.of("opensearchproject/opensearch:2.11.0"));
        when(opensearch.dataVolume()).thenReturn(Optional.of("opensearch-data"));
        EmulatorConfig.DockerConfig docker = mock(EmulatorConfig.DockerConfig.class);
        when(config.docker()).thenReturn(docker);
        lenient().when(docker.logMaxSize()).thenReturn("10m");
        lenient().when(docker.logMaxFile()).thenReturn("3");
        EmulatorConfig.StorageConfig storage = mock(EmulatorConfig.StorageConfig.class);
        when(config.storage()).thenReturn(storage);
        when(storage.hostPersistentPath()).thenReturn("floci-data");
        when(storage.persistentPath()).thenReturn("/data");

        PortAllocator portAllocator = mock(PortAllocator.class);
        when(portAllocator.allocate(9400, 9499)).thenReturn(9400);

        ContainerLifecycleManager lifecycleManager = mock(ContainerLifecycleManager.class);
        when(lifecycleManager.createAndStart(any())).thenReturn(
                new ContainerLifecycleManager.ContainerInfo("container-id", Map.of(
                        9200, new ContainerLifecycleManager.EndpointInfo("172.17.0.5", 9200))));

        ContainerBuilder containerBuilder = mock(ContainerBuilder.class);
        ContainerBuilder.Builder builder = mock(ContainerBuilder.Builder.class, RETURNS_SELF);
        when(containerBuilder.newContainer(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(mock(ContainerSpec.class));

        RegionResolver regionResolver = mock(RegionResolver.class);
        when(regionResolver.getAccountId()).thenReturn("000000000000");
        when(regionResolver.getDefaultRegion()).thenReturn("us-east-1");

        OpenSearchDomainManager manager = new OpenSearchDomainManager(containerBuilder, lifecycleManager,
                mock(ContainerDetector.class), portAllocator, config, regionResolver);

        Domain domain = new Domain();
        domain.setDomainName("my-domain");
        domain.setEngineVersion("OpenSearch_2.11");

        manager.startDomain(domain);

        verify(lifecycleManager).ensureUnmanagedVolume("opensearch-data-my-domain");
        verify(builder).withNamedVolume("opensearch-data-my-domain", "/usr/share/opensearch/data");
        assertEquals("opensearch-data-my-domain", domain.getDockerVolumeName());
        assertEquals(VolumeOwnership.OPERATOR_NAMED, domain.getVolumeOwnership());
    }

    /**
     * A namespaced deployment composes the configured volume name with its namespace,
     * so two namespaced deployments sharing a Docker daemon and the same configured
     * base never converge on one volume for the same domain name.
     */
    @Test
    void startDomainMountsConfiguredNamedDataVolumeUnderTheResourceNamespace() {
        EmulatorConfig config = mock(EmulatorConfig.class);
        EmulatorConfig.ServicesConfig services = mock(EmulatorConfig.ServicesConfig.class);
        EmulatorConfig.OpenSearchServiceConfig opensearch = mock(EmulatorConfig.OpenSearchServiceConfig.class);
        when(config.services()).thenReturn(services);
        when(services.opensearch()).thenReturn(opensearch);
        when(services.dockerNetwork()).thenReturn(Optional.empty());
        when(opensearch.proxyBasePort()).thenReturn(9400);
        when(opensearch.proxyMaxPort()).thenReturn(9499);
        when(opensearch.defaultImage()).thenReturn(Optional.of("opensearchproject/opensearch:2.11.0"));
        when(opensearch.dataVolume()).thenReturn(Optional.of("opensearch-data"));
        EmulatorConfig.DockerConfig docker = mock(EmulatorConfig.DockerConfig.class);
        when(config.docker()).thenReturn(docker);
        when(docker.resourceNamespace()).thenReturn(Optional.of("team-a"));
        lenient().when(docker.logMaxSize()).thenReturn("10m");
        lenient().when(docker.logMaxFile()).thenReturn("3");
        EmulatorConfig.StorageConfig storage = mock(EmulatorConfig.StorageConfig.class);
        when(config.storage()).thenReturn(storage);
        when(storage.hostPersistentPath()).thenReturn("floci-data");
        when(storage.persistentPath()).thenReturn("/data");

        PortAllocator portAllocator = mock(PortAllocator.class);
        when(portAllocator.allocate(9400, 9499)).thenReturn(9400);

        ContainerLifecycleManager lifecycleManager = mock(ContainerLifecycleManager.class);
        when(lifecycleManager.createAndStart(any())).thenReturn(
                new ContainerLifecycleManager.ContainerInfo("container-id", Map.of(
                        9200, new ContainerLifecycleManager.EndpointInfo("172.17.0.5", 9200))));

        ContainerBuilder containerBuilder = mock(ContainerBuilder.class);
        ContainerBuilder.Builder builder = mock(ContainerBuilder.Builder.class, RETURNS_SELF);
        when(containerBuilder.newContainer(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(mock(ContainerSpec.class));

        RegionResolver regionResolver = mock(RegionResolver.class);
        when(regionResolver.getAccountId()).thenReturn("000000000000");
        when(regionResolver.getDefaultRegion()).thenReturn("us-east-1");

        OpenSearchDomainManager manager = new OpenSearchDomainManager(containerBuilder, lifecycleManager,
                mock(ContainerDetector.class), portAllocator, config, regionResolver);

        Domain domain = new Domain();
        domain.setDomainName("my-domain");
        domain.setEngineVersion("OpenSearch_2.11");

        manager.startDomain(domain);

        verify(lifecycleManager).ensureUnmanagedVolume("opensearch-data-team-a-my-domain");
        verify(builder).withNamedVolume("opensearch-data-team-a-my-domain", "/usr/share/opensearch/data");
        assertEquals("opensearch-data-team-a-my-domain", domain.getDockerVolumeName());
        assertEquals(VolumeOwnership.OPERATOR_NAMED, domain.getVolumeOwnership());
    }

    /**
     * A configured absolute path bind-mounts {@code <path>/<domain-name>}: per-domain
     * subdirectory, no named volume on the domain record, and the bind ownership
     * marker in place of a volume name.
     */
    @Test
    void startDomainBindsConfiguredHostDataPathPerDomain(@TempDir Path dataRoot) {
        EmulatorConfig config = mock(EmulatorConfig.class);
        EmulatorConfig.ServicesConfig services = mock(EmulatorConfig.ServicesConfig.class);
        EmulatorConfig.OpenSearchServiceConfig opensearch = mock(EmulatorConfig.OpenSearchServiceConfig.class);
        when(config.services()).thenReturn(services);
        when(services.opensearch()).thenReturn(opensearch);
        when(services.dockerNetwork()).thenReturn(Optional.empty());
        when(opensearch.proxyBasePort()).thenReturn(9400);
        when(opensearch.proxyMaxPort()).thenReturn(9499);
        when(opensearch.defaultImage()).thenReturn(Optional.of("opensearchproject/opensearch:2.11.0"));
        when(opensearch.dataVolume()).thenReturn(Optional.of(dataRoot.toString()));
        EmulatorConfig.DockerConfig docker = mock(EmulatorConfig.DockerConfig.class);
        when(config.docker()).thenReturn(docker);
        lenient().when(docker.logMaxSize()).thenReturn("10m");
        lenient().when(docker.logMaxFile()).thenReturn("3");
        EmulatorConfig.StorageConfig storage = mock(EmulatorConfig.StorageConfig.class);
        when(config.storage()).thenReturn(storage);
        when(storage.hostPersistentPath()).thenReturn("floci-data");
        when(storage.persistentPath()).thenReturn("/data");

        PortAllocator portAllocator = mock(PortAllocator.class);
        when(portAllocator.allocate(9400, 9499)).thenReturn(9400);

        ContainerLifecycleManager lifecycleManager = mock(ContainerLifecycleManager.class);
        when(lifecycleManager.createAndStart(any())).thenReturn(
                new ContainerLifecycleManager.ContainerInfo("container-id", Map.of(
                        9200, new ContainerLifecycleManager.EndpointInfo("172.17.0.5", 9200))));

        ContainerBuilder containerBuilder = mock(ContainerBuilder.class);
        ContainerBuilder.Builder builder = mock(ContainerBuilder.Builder.class, RETURNS_SELF);
        when(containerBuilder.newContainer(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(mock(ContainerSpec.class));

        RegionResolver regionResolver = mock(RegionResolver.class);
        when(regionResolver.getAccountId()).thenReturn("000000000000");
        when(regionResolver.getDefaultRegion()).thenReturn("us-east-1");

        OpenSearchDomainManager manager = new OpenSearchDomainManager(containerBuilder, lifecycleManager,
                mock(ContainerDetector.class), portAllocator, config, regionResolver);

        Domain domain = new Domain();
        domain.setDomainName("my-domain");
        domain.setEngineVersion("OpenSearch_2.11");

        manager.startDomain(domain);

        String expectedBind = dataRoot.resolve("my-domain").toAbsolutePath().normalize().toString();
        verify(builder).withBind(expectedBind, "/usr/share/opensearch/data");
        verify(builder, never()).withNamedVolume(anyString(), anyString());
        assertNull(domain.getDockerVolumeName());
        assertEquals(VolumeOwnership.OPERATOR_BIND, domain.getVolumeOwnership());
    }

    /**
     * A namespaced deployment nests the configured bind path under a namespace
     * subdirectory, so two namespaced deployments sharing a Docker daemon host and
     * the same configured base never converge on one data directory for the same
     * domain name.
     */
    @Test
    void startDomainBindsConfiguredHostDataPathUnderTheResourceNamespace(@TempDir Path dataRoot) {
        EmulatorConfig config = mock(EmulatorConfig.class);
        EmulatorConfig.ServicesConfig services = mock(EmulatorConfig.ServicesConfig.class);
        EmulatorConfig.OpenSearchServiceConfig opensearch = mock(EmulatorConfig.OpenSearchServiceConfig.class);
        when(config.services()).thenReturn(services);
        when(services.opensearch()).thenReturn(opensearch);
        when(services.dockerNetwork()).thenReturn(Optional.empty());
        when(opensearch.proxyBasePort()).thenReturn(9400);
        when(opensearch.proxyMaxPort()).thenReturn(9499);
        when(opensearch.defaultImage()).thenReturn(Optional.of("opensearchproject/opensearch:2.11.0"));
        when(opensearch.dataVolume()).thenReturn(Optional.of(dataRoot.toString()));
        EmulatorConfig.DockerConfig docker = mock(EmulatorConfig.DockerConfig.class);
        when(config.docker()).thenReturn(docker);
        when(docker.resourceNamespace()).thenReturn(Optional.of("team-a"));
        lenient().when(docker.logMaxSize()).thenReturn("10m");
        lenient().when(docker.logMaxFile()).thenReturn("3");
        EmulatorConfig.StorageConfig storage = mock(EmulatorConfig.StorageConfig.class);
        when(config.storage()).thenReturn(storage);
        when(storage.hostPersistentPath()).thenReturn("floci-data");
        when(storage.persistentPath()).thenReturn("/data");

        PortAllocator portAllocator = mock(PortAllocator.class);
        when(portAllocator.allocate(9400, 9499)).thenReturn(9400);

        ContainerLifecycleManager lifecycleManager = mock(ContainerLifecycleManager.class);
        when(lifecycleManager.createAndStart(any())).thenReturn(
                new ContainerLifecycleManager.ContainerInfo("container-id", Map.of(
                        9200, new ContainerLifecycleManager.EndpointInfo("172.17.0.5", 9200))));

        ContainerBuilder containerBuilder = mock(ContainerBuilder.class);
        ContainerBuilder.Builder builder = mock(ContainerBuilder.Builder.class, RETURNS_SELF);
        when(containerBuilder.newContainer(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(mock(ContainerSpec.class));

        RegionResolver regionResolver = mock(RegionResolver.class);
        when(regionResolver.getAccountId()).thenReturn("000000000000");
        when(regionResolver.getDefaultRegion()).thenReturn("us-east-1");

        OpenSearchDomainManager manager = new OpenSearchDomainManager(containerBuilder, lifecycleManager,
                mock(ContainerDetector.class), portAllocator, config, regionResolver);

        Domain domain = new Domain();
        domain.setDomainName("my-domain");
        domain.setEngineVersion("OpenSearch_2.11");

        manager.startDomain(domain);

        String expectedBind = dataRoot.resolve("team-a").resolve("my-domain")
                .toAbsolutePath().normalize().toString();
        verify(builder).withBind(expectedBind, "/usr/share/opensearch/data");
        verify(builder, never()).withNamedVolume(anyString(), anyString());
        assertNull(domain.getDockerVolumeName());
        assertEquals(VolumeOwnership.OPERATOR_BIND, domain.getVolumeOwnership());
    }

    /** An operator-named volume is never deleted with the domain, whatever the storage policy. */
    @Test
    void removeDomainStorageRetainsOperatorNamedVolume() {
        EmulatorConfig config = mock(EmulatorConfig.class);
        EmulatorConfig.DockerConfig docker = mock(EmulatorConfig.DockerConfig.class);
        when(config.docker()).thenReturn(docker);
        EmulatorConfig.StorageConfig storage = mock(EmulatorConfig.StorageConfig.class);
        when(config.storage()).thenReturn(storage);
        when(storage.mode()).thenReturn("memory");
        when(storage.pruneVolumesOnDelete()).thenReturn(true);
        EmulatorConfig.ServicesConfig services = mock(EmulatorConfig.ServicesConfig.class);
        when(config.services()).thenReturn(services);
        EmulatorConfig.OpenSearchServiceConfig opensearch = mock(EmulatorConfig.OpenSearchServiceConfig.class);
        when(services.opensearch()).thenReturn(opensearch);
        when(opensearch.dataVolume()).thenReturn(Optional.empty());

        ContainerLifecycleManager lifecycleManager = mock(ContainerLifecycleManager.class);

        OpenSearchDomainManager manager = new OpenSearchDomainManager(mock(ContainerBuilder.class),
                lifecycleManager, mock(ContainerDetector.class), mock(PortAllocator.class), config,
                mock(RegionResolver.class));

        Domain domain = new Domain();
        domain.setDomainName("my-domain");
        domain.setVolumeId("abc123");
        domain.setDockerVolumeName("opensearch-data-my-domain");

        manager.removeDomainStorage(domain);

        verify(lifecycleManager, never()).removeVolume(anyString());
    }

    /** A volume Floci derived itself, current prefix, is still removed as before. */
    @Test
    void removeDomainStorageRemovesFlociDerivedVolume() {
        EmulatorConfig config = mock(EmulatorConfig.class);
        EmulatorConfig.DockerConfig docker = mock(EmulatorConfig.DockerConfig.class);
        when(config.docker()).thenReturn(docker);
        EmulatorConfig.StorageConfig storage = mock(EmulatorConfig.StorageConfig.class);
        when(config.storage()).thenReturn(storage);
        when(storage.mode()).thenReturn("memory");
        when(storage.pruneVolumesOnDelete()).thenReturn(false);
        EmulatorConfig.ServicesConfig services = mock(EmulatorConfig.ServicesConfig.class);
        when(config.services()).thenReturn(services);
        EmulatorConfig.OpenSearchServiceConfig opensearch = mock(EmulatorConfig.OpenSearchServiceConfig.class);
        when(services.opensearch()).thenReturn(opensearch);
        when(opensearch.dataVolume()).thenReturn(Optional.empty());

        ContainerLifecycleManager lifecycleManager = mock(ContainerLifecycleManager.class);

        OpenSearchDomainManager manager = new OpenSearchDomainManager(mock(ContainerBuilder.class),
                lifecycleManager, mock(ContainerDetector.class), mock(PortAllocator.class), config,
                mock(RegionResolver.class));

        Domain domain = new Domain();
        domain.setDomainName("my-domain");
        domain.setVolumeId("abc123");
        domain.setDockerVolumeName(
                ContainerStorageHelper.resourceName(config, "opensearch", "abc123", "my-domain"));

        manager.removeDomainStorage(domain);

        verify(lifecycleManager).removeVolume("floci-aws-opensearch-abc123");
    }

    /** A pre-migration record's legacy-named volume is as deletable as a current-prefix one. */
    @Test
    void removeDomainStorageRemovesLegacyDerivedVolume() {
        EmulatorConfig config = mock(EmulatorConfig.class);
        EmulatorConfig.DockerConfig docker = mock(EmulatorConfig.DockerConfig.class);
        when(config.docker()).thenReturn(docker);
        EmulatorConfig.StorageConfig storage = mock(EmulatorConfig.StorageConfig.class);
        when(config.storage()).thenReturn(storage);
        when(storage.mode()).thenReturn("memory");
        when(storage.pruneVolumesOnDelete()).thenReturn(false);
        EmulatorConfig.ServicesConfig services = mock(EmulatorConfig.ServicesConfig.class);
        when(config.services()).thenReturn(services);
        EmulatorConfig.OpenSearchServiceConfig opensearch = mock(EmulatorConfig.OpenSearchServiceConfig.class);
        when(services.opensearch()).thenReturn(opensearch);
        when(opensearch.dataVolume()).thenReturn(Optional.empty());

        ContainerLifecycleManager lifecycleManager = mock(ContainerLifecycleManager.class);

        OpenSearchDomainManager manager = new OpenSearchDomainManager(mock(ContainerBuilder.class),
                lifecycleManager, mock(ContainerDetector.class), mock(PortAllocator.class), config,
                mock(RegionResolver.class));

        Domain domain = new Domain();
        domain.setDomainName("my-domain");
        domain.setVolumeId("abc123");
        domain.setDockerVolumeName(
                ContainerStorageHelper.legacyResourceName(config, "opensearch", "abc123", "my-domain"));

        manager.removeDomainStorage(domain);

        verify(lifecycleManager).removeVolume("floci-opensearch-abc123");
    }

    /**
     * A bind-backed domain mounts no named volume, so delete removes no Docker volume at
     * all, whatever the storage policy: a stale managed name on the record must not send
     * it after a volume the domain never mounted.
     */
    @Test
    void removeDomainStorageRemovesNoDockerVolumeForBindBackedDomain() {
        EmulatorConfig config = mock(EmulatorConfig.class);
        EmulatorConfig.DockerConfig docker = mock(EmulatorConfig.DockerConfig.class);
        when(config.docker()).thenReturn(docker);
        EmulatorConfig.StorageConfig storage = mock(EmulatorConfig.StorageConfig.class);
        when(config.storage()).thenReturn(storage);
        when(storage.mode()).thenReturn("memory");
        when(storage.pruneVolumesOnDelete()).thenReturn(true);

        ContainerLifecycleManager lifecycleManager = mock(ContainerLifecycleManager.class);

        OpenSearchDomainManager manager = new OpenSearchDomainManager(mock(ContainerBuilder.class),
                lifecycleManager, mock(ContainerDetector.class), mock(PortAllocator.class), config,
                mock(RegionResolver.class));

        Domain domain = new Domain();
        domain.setDomainName("my-domain");
        domain.setVolumeOwnership(VolumeOwnership.OPERATOR_BIND);

        manager.removeDomainStorage(domain);

        verify(lifecycleManager, never()).removeVolume(anyString());
    }

    /**
     * A pre-marker record without a volume id backfills the legacy name, which a
     * configured data volume can spell exactly ({@code floci-opensearch-<domain>}).
     * The operator's reading of the colliding name wins, so delete retains it even
     * under memory mode with pruning enabled.
     */
    @Test
    void removeDomainStorageRetainsLegacyRecordVolumeMatchingTheConfiguredDataVolume() {
        EmulatorConfig config = mock(EmulatorConfig.class);
        EmulatorConfig.DockerConfig docker = mock(EmulatorConfig.DockerConfig.class);
        when(config.docker()).thenReturn(docker);
        EmulatorConfig.StorageConfig storage = mock(EmulatorConfig.StorageConfig.class);
        when(config.storage()).thenReturn(storage);
        when(storage.mode()).thenReturn("memory");
        when(storage.pruneVolumesOnDelete()).thenReturn(true);
        EmulatorConfig.ServicesConfig services = mock(EmulatorConfig.ServicesConfig.class);
        when(config.services()).thenReturn(services);
        EmulatorConfig.OpenSearchServiceConfig opensearch = mock(EmulatorConfig.OpenSearchServiceConfig.class);
        when(services.opensearch()).thenReturn(opensearch);
        when(opensearch.dataVolume()).thenReturn(Optional.of("floci-opensearch"));

        ContainerLifecycleManager lifecycleManager = mock(ContainerLifecycleManager.class);

        OpenSearchDomainManager manager = new OpenSearchDomainManager(mock(ContainerBuilder.class),
                lifecycleManager, mock(ContainerDetector.class), mock(PortAllocator.class), config,
                mock(RegionResolver.class));

        Domain domain = new Domain();
        domain.setDomainName("my-domain");

        manager.removeDomainStorage(domain);

        verify(lifecycleManager, never()).removeVolume(anyString());
    }

    /**
     * The marker, not the spelling, decides: an operator-named volume is retained even
     * when its name is spelled exactly like the legacy name Floci derives for the same
     * domain.
     */
    @Test
    void removeDomainStorageRetainsOperatorNamedVolumeEvenWhenItMatchesTheLegacyDerivedName() {
        EmulatorConfig config = mock(EmulatorConfig.class);
        EmulatorConfig.DockerConfig docker = mock(EmulatorConfig.DockerConfig.class);
        when(config.docker()).thenReturn(docker);
        EmulatorConfig.StorageConfig storage = mock(EmulatorConfig.StorageConfig.class);
        when(config.storage()).thenReturn(storage);
        when(storage.mode()).thenReturn("memory");
        when(storage.pruneVolumesOnDelete()).thenReturn(true);
        EmulatorConfig.ServicesConfig services = mock(EmulatorConfig.ServicesConfig.class);
        when(config.services()).thenReturn(services);
        EmulatorConfig.OpenSearchServiceConfig opensearch = mock(EmulatorConfig.OpenSearchServiceConfig.class);
        when(services.opensearch()).thenReturn(opensearch);
        when(opensearch.dataVolume()).thenReturn(Optional.of("floci-opensearch"));

        ContainerLifecycleManager lifecycleManager = mock(ContainerLifecycleManager.class);

        OpenSearchDomainManager manager = new OpenSearchDomainManager(mock(ContainerBuilder.class),
                lifecycleManager, mock(ContainerDetector.class), mock(PortAllocator.class), config,
                mock(RegionResolver.class));

        Domain domain = new Domain();
        domain.setDomainName("my-domain");
        domain.setDockerVolumeName("floci-opensearch-my-domain");
        domain.setVolumeOwnership(VolumeOwnership.OPERATOR_NAMED);

        manager.removeDomainStorage(domain);

        verify(lifecycleManager, never()).removeVolume(anyString());
    }

    /**
     * A managed volume is removed by its persisted name, not by names recomputed from
     * the configuration held at delete time: a resource-namespace change between
     * creation and deletion must not strand the volume under the prune policy.
     */
    @Test
    void removeDomainStorageRemovesManagedVolumeByPersistedNameWhenTheNamespaceChanged() {
        EmulatorConfig config = mock(EmulatorConfig.class);
        EmulatorConfig.DockerConfig docker = mock(EmulatorConfig.DockerConfig.class);
        when(config.docker()).thenReturn(docker);
        when(docker.resourceNamespace()).thenReturn(Optional.of("new-ns"));
        EmulatorConfig.StorageConfig storage = mock(EmulatorConfig.StorageConfig.class);
        when(config.storage()).thenReturn(storage);
        when(storage.mode()).thenReturn("memory");
        when(storage.pruneVolumesOnDelete()).thenReturn(false);

        ContainerLifecycleManager lifecycleManager = mock(ContainerLifecycleManager.class);

        OpenSearchDomainManager manager = new OpenSearchDomainManager(mock(ContainerBuilder.class),
                lifecycleManager, mock(ContainerDetector.class), mock(PortAllocator.class), config,
                mock(RegionResolver.class));

        Domain domain = new Domain();
        domain.setDomainName("my-domain");
        domain.setVolumeId("abc123");
        domain.setDockerVolumeName("floci-aws-opensearch-abc123");
        domain.setVolumeOwnership(VolumeOwnership.FLOCI_MANAGED);

        manager.removeDomainStorage(domain);

        verify(lifecycleManager).removeVolume("floci-aws-opensearch-abc123");
    }
}
