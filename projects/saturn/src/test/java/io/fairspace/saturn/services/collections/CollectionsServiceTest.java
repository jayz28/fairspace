package io.fairspace.saturn.services.collections;

import io.fairspace.saturn.rdf.dao.DAO;
import io.fairspace.saturn.rdf.transactions.DatasetJobSupportInMemory;
import io.fairspace.saturn.services.AccessDeniedException;
import io.fairspace.saturn.services.permissions.Access;
import io.fairspace.saturn.services.permissions.PermissionsService;
import io.fairspace.saturn.services.users.User;
import io.fairspace.saturn.vocabulary.FS;
import org.apache.jena.graph.Node;
import org.apache.jena.vocabulary.RDF;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.function.Consumer;

import static io.fairspace.saturn.services.users.User.setCurrentUser;
import static java.util.stream.Collectors.toMap;
import static org.apache.jena.graph.NodeFactory.createURI;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CollectionsServiceTest {
    private static final Node userIri = createURI("http://ex.com/user");
    private static final Node workspaceIri = createURI("http://ex.com/ws");

    @Mock
    private Consumer<Object> eventListener;
    @Mock
    private User user;
    @Mock
    private PermissionsService permissions;
    private CollectionsService collections;

    @Before
    public void before() {
        setCurrentUser(user);
        when(user.getIri()).thenReturn(userIri);
        when(user.getName()).thenReturn("name");
        var ds = new DatasetJobSupportInMemory();
        ds.getDefaultModel().add(ds.getDefaultModel().asRDFNode(workspaceIri).asResource(), RDF.type, FS.Workspace);
        collections = new CollectionsService("http://fairspace.io/", new DAO(ds), eventListener, permissions);
    }

    @Test
    public void serviceReturnsAnEmptyListIfNoCollectionsExist() {
        assertTrue(collections.list().isEmpty());
    }

    @Test
    public void creationOfACollectionTriggersAnEvent() {
        var created = collections.create(newCollection());
        verify(eventListener, times(1)).accept(new CollectionCreatedEvent(created));
    }

    public void newlyCreatedCollectionIsProperlyInitialized() {
        var prototype = newCollection();
        var created = collections.create(prototype);
        assertTrue(created.getIri().isURI());
        assertEquals(prototype.getName(), created.getName());
        assertEquals(prototype.getDescription(), created.getDescription());
        assertEquals(prototype.getLocation(), created.getLocation());
        assertEquals(prototype.getConnectionString(), created.getConnectionString());
        assertEquals("http://example.com/user", created.getCreatedBy().getURI());
        assertNotNull(created.getDateCreated());
        assertEquals(created.getDateCreated(), created.getDateModified());
        assertEquals(Access.Manage, created.getAccess());
    }

    public void newlyCreatedCollectionIsAccessible() {
        var created = collections.create(newCollection());
        assertNotNull(collections.getByLocation("dir1"));
        assertNull(collections.getByLocation("dir2"));

        assertEquals(1, collections.list().size());
        assertTrue(collections.list().contains(created));

        assertEquals(created, collections.get(created.getIri().getURI()));
    }

    private Collection newCollection() {
        var c = new Collection();
        c.setName("c1");
        c.setLocation("dir1");
        c.setDescription("descr");
        c.setConnectionString("");
        c.setOwnerWorkspace(workspaceIri);
        return c;
    }

    @Test
    public void changingLocationEmitsAnEvent() {
        var created1 = collections.create(newCollection());

        mockPermissions(Access.Manage);

        var patch = new Collection();
        patch.setIri(created1.getIri());
        patch.setLocation("dir2");
        collections.update(patch);
        verify(eventListener, times(1)).accept(new CollectionMovedEvent(created1, "dir1"));
    }

    @Test
    public void updatesWorkAsExpected() {
        var c = collections.create(newCollection());

        mockPermissions(Access.Manage);

        var patch = new Collection();
        patch.setIri(c.getIri());
        patch.setName("new name");
        patch.setDescription("new descr");
        patch.setLocation("dir2");
        collections.update(patch);
        verify(eventListener, times(1)).accept(new CollectionMovedEvent(c, "dir1"));

        var updated = collections.get(c.getIri().getURI());
        assertEquals("new name", updated.getName());
        assertEquals("new descr", updated.getDescription());
        assertEquals("dir2", updated.getLocation());
        assertNotEquals(c.getDateModified(), updated.getDateModified());
    }

    @Test
    public void standardCharactersInLocationAreAllowed() {
        var c1 = new Collection();
        c1.setName("c1");
        c1.setLocation("Az_1-2");
        c1.setDescription("descr");
        c1.setConnectionString("managed://example.com");
        c1.setOwnerWorkspace(workspaceIri);

        assertEquals(c1.getLocation(), collections.create(c1).getLocation());
    }

    @Test(expected = IllegalArgumentException.class)
    public void nonStandardCharactersInLocationAreNotAllowed() {
        try {
            var c1 = new Collection();
            c1.setName("c1");
            c1.setLocation("dir?");
            c1.setDescription("descr");
            c1.setConnectionString("managed://example.com");
            c1.setOwnerWorkspace(workspaceIri);

            collections.create(c1);
        } finally {
            verifyNoMoreInteractions(eventListener);
        }
    }

    @Test(expected = LocationAlreadyExistsException.class)
    public void checksForLocationsUniquenessOnCreate() {
        try {
            var c1 = new Collection();
            c1.setName("c1");
            c1.setLocation("dir1");
            c1.setDescription("descr");
            c1.setConnectionString("managed://example.com");
            c1.setOwnerWorkspace(workspaceIri);

            collections.create(c1);
            c1.setIri(null);
            collections.create(c1);
        } finally {
            verify(eventListener, times(1)).accept(any(CollectionCreatedEvent.class));
            verifyNoMoreInteractions(eventListener);
        }
    }

    @Test(expected = LocationAlreadyExistsException.class)
    public void checksForLocationsUniquenessOnUpdate() {
        try {
            var c1 = collections.create(newCollection());

            var c2 = new Collection();
            c2.setName("c2");
            c2.setLocation("dir2");
            c2.setDescription("descr");
            c2.setConnectionString("managed://example.com");
            c2.setOwnerWorkspace(workspaceIri);

            collections.create(c2);

            var patch = new Collection();
            patch.setIri(c1.getIri());
            patch.setLocation(c2.getLocation());
            mockPermissions(Access.Manage);
            collections.update(patch);
        } finally {
            verify(eventListener, times(2)).accept(any(CollectionCreatedEvent.class));
            verifyNoMoreInteractions(eventListener);
        }
    }

    @Test
    public void collectionsWithNonePermissionAreInvisible() {
        var c1 = new Collection();
        c1.setName("c1");
        c1.setLocation("dir");
        c1.setDescription("descr");
        c1.setConnectionString("managed://example.com");
        c1.setOwnerWorkspace(workspaceIri);
        c1 = collections.create(c1);

        mockPermissions(Access.None);

        assertNull(collections.get(c1.getIri().getURI()));
        assertNull(collections.getByLocation(c1.getLocation()));
        assertTrue(collections.list().isEmpty());
    }

    @Test
    public void collectionsWithWritePermissionCanBeModified() {
        var c1 = new Collection();
        c1.setName("c1");
        c1.setLocation("dir");
        c1.setDescription("descr");
        c1.setConnectionString("managed://example.com");
        c1.setOwnerWorkspace(workspaceIri);
        c1 = collections.create(c1);

        mockPermissions(Access.Write);

        c1.setDescription("new description");
        collections.update(c1);
        assertEquals("new description", collections.get(c1.getIri().getURI()).getDescription());
    }

    @Test(expected = AccessDeniedException.class)
    public void collectionsWithoutWritePermissionCannotBeModified() {
        var c1 = new Collection();
        c1.setName("c1");
        c1.setLocation("dir");
        c1.setDescription("descr");
        c1.setConnectionString("managed://example.com");;
        c1.setOwnerWorkspace(workspaceIri);
        c1 = collections.create(c1);

        mockPermissions(Access.Read);

        c1.setDescription("new description");
        collections.update(c1);
    }

    private void mockPermissions(Access access) {
        when(permissions.getPermissions(any(java.util.Collection.class)))
                .thenAnswer(invocation -> invocation.<java.util.Collection<Node>>getArgument(0)
                        .stream()
                        .collect(toMap(node -> node, node -> access)));
        doCallRealMethod().when(permissions).getPermission(any());
    }
}
