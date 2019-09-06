import axios from "axios";
import Config from "../common/services/Config/Config";
import {extractJsonData, handleHttpError} from "../common/utils/httpUtils";
import {createMetadataIri} from "../common/utils/linkeddata/metadataUtils";
import {SEARCH_DEFAULT_SIZE} from "../constants";

export default {
    /**
     * Searches keycloak  with the qiven query string on the specified types
     * @param query
     * @param types     List of class URIs to search for. If empty, it returns all types
     * @return Promise
     */
    users: ({query, size = SEARCH_DEFAULT_SIZE}) => {
        return axios.get(Config.get().urls.userSearch + '?search=' + encodeURIComponent(query) + '&max=' + size)
            .catch(handleHttpError("Failure when retrieving users"))
            .then(extractJsonData)
            .then(users => users.map(user => ({...user, iri: createMetadataIri(user.id)})));
    }
};
