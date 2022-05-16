package org.testcontainers;

import org.junit.Rule;
import org.junit.Test;
import org.rnorth.visibleassertions.VisibleAssertions;
import org.testcontainers.DockerClientFactory.DiskSpaceUsage;
import org.testcontainers.dockerclient.LogToStringContainerCallback;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MockTestcontainersConfigurationRule;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test for {@link DockerClientFactory}.
 */
public class DockerClientFactoryTest {

    @Rule
    public MockTestcontainersConfigurationRule configurationMock = new MockTestcontainersConfigurationRule();

    @Test
    public void runCommandInsideDockerShouldNotFailIfImageDoesNotExistsLocally() {
        try (DockerRegistryContainer registryContainer = new DockerRegistryContainer()) {
            registryContainer.start();
            DockerImageName imageName = registryContainer.createImage();

            DockerClientFactory dockFactory = DockerClientFactory.instance();

            dockFactory.runInsideDocker(
                imageName,
                cmd -> cmd.withCmd("sh", "-c", "echo 'SUCCESS'"),
                (client, id) ->
                    client.logContainerCmd(id)
                        .withStdOut(true)
                        .exec(new LogToStringContainerCallback())
                        .toString()
            );
        }
    }

    @Test
    public void shouldHandleBigDiskSize() throws Exception {
        String dfOutput = "/dev/disk1     2982480572 1491240286 2982480572    31%    /";
        DiskSpaceUsage usage = DockerClientFactory.instance().parseAvailableDiskSpace(dfOutput);

        VisibleAssertions.assertEquals("Available MB is correct", 2982480572L / 1024L, usage.availableMB.orElse(0L));
        VisibleAssertions.assertEquals("Available percentage is correct", 31, usage.usedPercent.orElse(0));
    }

    @Test
    public void dockerHostIpAddress() {
        DockerClientFactory instance = new DockerClientFactory();
        instance.strategy = null;
        assertThat(instance.dockerHostIpAddress()).isNotNull();
    }

    @Test
    public void failedChecksFailFast() {
        DockerClientFactory instance = DockerClientFactory.instance();
        assertThat(instance.client()).isNotNull();
        assertThat(instance.cachedClientFailure).isNull();
        try {
            RuntimeException failure = new IllegalStateException("Boom!");
            instance.cachedClientFailure = failure;
            // Fail fast
            assertThatThrownBy(instance::client).isEqualTo(failure);
        } finally {
            instance.cachedClientFailure = null;
        }
    }
}
