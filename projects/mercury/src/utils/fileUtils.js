import {PATH_SEPARATOR} from "../constants";

export function splitPathIntoArray(path) {
    return path.split(PATH_SEPARATOR).filter(s => s.length > 0);
}

export function uniqueName(fileName, usedNames) {
    if (!usedNames.includes(fileName)) {
        usedNames.push(fileName);
        return fileName;
    }
    const dotPos = fileName.lastIndexOf('.');
    const name = (dotPos >= 0) ? fileName.substring(0, dotPos) : fileName;
    const ext = (dotPos >= 0) ? fileName.substring(dotPos) : '';
    let index = 1;

    while (true) {
        const newName = `${name} (${index})${ext}`;
        if (!usedNames.includes(newName)) {
            usedNames.push(newName);
            return newName;
        }
        index += 1;
    }
}

export const joinPaths = (...paths) => paths
    .map(p => (p && p !== '/' ? p : ''))
    .join('/');

export const addCounterToFilename = (fileName) => {
    // Parse the filename
    const dotPosition = fileName.lastIndexOf('.');
    let baseName = fileName.substring(0, dotPosition);
    const extension = fileName.substring(dotPosition + 1);

    // By default the counter is set to 2
    let counter = 2;

    // Verify if the filename already contains a counter
    // If so, update the counter in the filename
    const counterMatch = / \((\d+)\)$/;
    const matches = baseName.match(counterMatch);
    if (matches) {
        baseName = baseName.substring(0, baseName.length - matches[0].length);
        counter = parseInt(matches[1], 10) + 1;
    }

    return `${baseName} (${counter}).${extension}`;
};

export function parentPath(path) {
    const pos = path.lastIndexOf('/');
    return (pos > 1) ? path.substring(0, pos) : '';
}

export function fileName(path) {
    const pos = path.lastIndexOf('/');
    return (pos > 0) ? path.substring(pos + 1) : path;
}
