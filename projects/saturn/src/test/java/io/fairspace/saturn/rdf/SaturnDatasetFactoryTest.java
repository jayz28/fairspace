package io.fairspace.saturn.rdf;

import java.io.File;
import java.io.IOException;

import org.apache.jena.tdb2.store.DatasetGraphSwitchable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import io.fairspace.saturn.services.maintenance.MaintenanceService;
import io.fairspace.saturn.services.views.ViewStoreClientFactory;

import static io.fairspace.saturn.config.ConfigLoader.CONFIG;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class SaturnDatasetFactoryTest {
    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void testIsRestoreNeededForEmptyDirectory() throws IOException {
        File emptyDirectory = testFolder.newFolder();
        assertTrue(SaturnDatasetFactory.isRestoreNeeded(emptyDirectory));
    }

    @Test
    public void testIsRestoreNeededForNonExistingDirectory() throws IOException {
        File nonExistentDirectory = new File(testFolder.getRoot(), "non-existent-directory");
        assertTrue(SaturnDatasetFactory.isRestoreNeeded(nonExistentDirectory));
    }

    @Test
    public void testIsRestoreNotNeededForFilledDirectory() throws IOException {
        File datasetPath = testFolder.newFolder();
        new File(datasetPath, "Data-0001").mkdirs();
        assertFalse(SaturnDatasetFactory.isRestoreNeeded(datasetPath));
    }

    @Test
    public void testIsRestoreIfNoDataDirectoryIsPresent() throws IOException {
        File datasetPath = testFolder.newFolder();
        new File(datasetPath, "lost+found").mkdirs();
        assertTrue(SaturnDatasetFactory.isRestoreNeeded(datasetPath));
    }

    @Test
    public void testUnwrappingDatasetGraphIsOfRightType() {
        // give
        var viewStoreClientFactory = mock(ViewStoreClientFactory.class);
        var ds = SaturnDatasetFactory.connect(CONFIG.jena, viewStoreClientFactory);

        var dataSetGraph = MaintenanceService.unwrap(ds.asDatasetGraph());

        assertTrue(dataSetGraph instanceof DatasetGraphSwitchable);
    }
}
