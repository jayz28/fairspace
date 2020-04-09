import React, {useContext} from 'react';
import {
    Paper,
    Table,
    TableBody,
    TableCell,
    TableHead,
    TablePagination,
    TableRow,
    TableSortLabel,
    withStyles,
} from "@material-ui/core";
import {formatDateTime, MessageDisplay, usePagination, useSorting} from '../common';

import styles from './CollectionList.styles';
import {getDisplayName} from "../common/utils/userUtils";
import WorkspaceContext from "../workspaces/WorkspaceContext";
import LoadingInlay from "../common/components/LoadingInlay";

const columns = {
    name: {
        valueExtractor: 'name',
        label: 'Name'
    },
    workspace: {
        valueExtractor: 'workspaceName',
        label: 'Workspace'
    },
    created: {
        valueExtractor: 'dateCreated',
        label: 'Created'
    },
    creator: {
        valueExtractor: 'displayName',
        label: 'Creator'
    }
};

const CollectionList = ({
    collections = [],
    isSelected = () => false,
    onCollectionClick,
    onCollectionDoubleClick,
    classes
}) => {
    // Extend collections with displayName to avoid computing it when sorting
    const collectionsWithDisplayName = collections.map(collection => ({
        ...collection,
        displayName: getDisplayName(collection.creatorObj)
    }));

    const {orderedItems, orderAscending, orderBy, toggleSort} = useSorting(collectionsWithDisplayName, columns, 'name');
    const {page, setPage, rowsPerPage, setRowsPerPage, pagedItems} = usePagination(orderedItems);
    const {workspaces, workspacesLoading} = useContext(WorkspaceContext);

    if (!collections || collections.length === 0) {
        return (
            <MessageDisplay
                message="Please create a collection."
                variant="h6"
                withIcon={false}
                isError={false}
                messageColor="textSecondary"
            />
        );
    }

    if (workspacesLoading) {
        return (<LoadingInlay />);
    }

    const pagedCollections = pagedItems.map(c => ({...c, workspaceName: workspaces.find(ws => ws.iri === c.ownerWorkspace).name}));


    return (
        <Paper className={classes.root}>
            <Table>
                <TableHead>
                    <TableRow>
                        {
                            Object.entries(columns).map(([key, column]) => (
                                <TableCell key={key}>
                                    <TableSortLabel
                                        active={orderBy === key}
                                        direction={orderAscending ? 'asc' : 'desc'}
                                        onClick={() => toggleSort(key)}
                                    >
                                        {column.label}
                                    </TableSortLabel>
                                </TableCell>
                            ))
                        }
                    </TableRow>
                </TableHead>
                <TableBody>
                    {pagedCollections.map((collection) => {
                        const selected = isSelected(collection);

                        return (
                            <TableRow
                                key={collection.iri}
                                hover
                                onClick={() => onCollectionClick(collection)}
                                onDoubleClick={() => onCollectionDoubleClick(collection)}
                                selected={selected}
                            >
                                <TableCell style={{maxWidth: 160}} component="th" scope="row">
                                    {collection.name}
                                </TableCell>
                                <TableCell>
                                    {collection.workspaceName}
                                </TableCell>
                                <TableCell padding="none">
                                    {formatDateTime(collection.dateCreated)}
                                </TableCell>
                                <TableCell>
                                    {getDisplayName(collection.creatorObj)}
                                </TableCell>
                            </TableRow>
                        );
                    })}
                </TableBody>
            </Table>
            <TablePagination
                rowsPerPageOptions={[5, 10, 25, 100]}
                component="div"
                count={collections.length}
                rowsPerPage={rowsPerPage}
                page={page}
                onChangePage={(e, p) => setPage(p)}
                onChangeRowsPerPage={e => setRowsPerPage(e.target.value)}
            />
        </Paper>
    );
};

export default withStyles(styles, {withTheme: true})(CollectionList);
