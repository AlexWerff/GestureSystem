function storeAuth(cookieStore, data) {
    if (!isEmpty(data.username) && !isEmpty(data.apiKey)) {
        cookieStore.put('user', data);
        return true;
    }
    return false;
}

function hasAuth(cookieStore) {
    return (cookieStore.get("user") != null);
}

function hasPermission(cookieStore,permission){
    return true;
}


function getAuth(cookieStore) {
    return cookieStore.get("user");
}

function removeAuth(cookieStore) {
    cookieStore.remove("user");
}

function isEmpty(value) {
    return value === undefined || value === null || value === "";
}