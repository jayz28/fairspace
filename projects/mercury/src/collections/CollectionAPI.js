// @flow
import {handleHttpError} from '../common/utils/httpUtils';
import FileAPI from "../file/FileAPI";
import {MetadataAPI} from "../metadata/common/LinkedDataAPI";
import {mapCollectionNameAndDescriptionToMetadata, mapFilePropertiesToCollection} from "./collectionUtils";
import type {User} from "../users/UsersAPI";

const rootUrl = "";

export type Access = "None" | "List" | "Read" | "Write" | "Manage";

export type Permission = {
    iri: string; // iri
    access: Access;
}

export type CollectionProperties = {|
    name: string;
    description: string;
    location: string;
    ownerWorkspace: string;
|};

export type CollectionType = {|
    type?: string;
|};

export type CollectionPermissions = {|
    access?: Access;
    userPermissions: Array<Permission>;
    workspacePermissions: Array<Permission>;
    canRead: boolean;
    canWrite: boolean;
    canManage: boolean;
|};

export type Resource = {|
    iri: string;
|};

export type CollectionAuditInfo = {|
    dateCreated?: string;
    createdBy?: string; // iri
    dateModified?: string;
    modifiedBy?: string; // iri
    dateDeleted?: string;
    deletedBy?: string; // iri
|};

export type Collection = Resource & CollectionProperties & CollectionType & CollectionPermissions & CollectionAuditInfo;

class CollectionAPI {
    getCollectionProperties(name: string): Promise<Collection> {
        return FileAPI.stat(name).then(mapFilePropertiesToCollection);
    }

    getCollections(currentUser: User, showDeleted = false): Promise<Collection[]> {
        return FileAPI.list(rootUrl, showDeleted)
            .then(collections => collections.map(mapFilePropertiesToCollection))
            .catch(handleHttpError("Failure when retrieving a list of collections"));
    }

    addCollection(collection: CollectionProperties, vocabulary): Promise<void> {
        const options = {
            headers: {
                Owner: collection.ownerWorkspace
            },
            withCredentials: true
        };
        return FileAPI.createDirectory(collection.location, options)
            .then(() => this.getCollectionProperties(collection.location))
            .then((properties) => {
                collection.iri = properties.iri;
                return this.updateCollection(collection, vocabulary);
            });
    }

    deleteCollection(collection: CollectionProperties, showDeleted = false): Promise<void> {
        return FileAPI.delete(collection.location, showDeleted)
            .catch(handleHttpError("Failure while deleting collection"));
    }

    undeleteCollection(collection: CollectionProperties): Promise<void> {
        return FileAPI.undelete(collection.location)
            .catch(handleHttpError("Failure while undeleting collection"));
    }

    relocateCollection(oldLocation: string, newLocation: string): Promise<void> {
        return FileAPI.move(oldLocation, newLocation)
            .catch(handleHttpError("Failure while relocating collection"));
    }

    updateCollection(collection: Collection, vocabulary): Promise<void> {
        const metadataProperties = mapCollectionNameAndDescriptionToMetadata(collection.name, collection.description);
        return MetadataAPI.updateEntity(collection.iri, metadataProperties, vocabulary)
            .catch(e => {
                console.error(e);
                throw Error("Failure while updating a collection");
            });
    }

    setAccessMode(location: String, mode) {
        return FileAPI.post(location, {action: 'set_access_mode', mode});
    }

    setPermission(location, principal, access) {
        return FileAPI.post(location, {action: 'set_permission', principal, access});
    }
}

export default new CollectionAPI();
