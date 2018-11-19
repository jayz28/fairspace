import AccountAPI from '../services/AccountAPI/AccountAPI'
import {createErrorHandlingPromiseAction} from "../utils/redux";
import {AUTHORIZATIONS, USER} from "./actionTypes";

export const fetchUser = createErrorHandlingPromiseAction(() => ({
    type: USER,
    payload: AccountAPI.getUser()
}));

export const fetchAuthorizations = createErrorHandlingPromiseAction(() => ({
    type: AUTHORIZATIONS,
    payload: AccountAPI.getAuthorizations()
}));
