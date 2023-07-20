export function authHeader(token) {
    return { 'Authorization': 'Bearer ' + token }
}

export const jsonHeader = { 'Content-Type': 'application/json' }

export function compositeHeader(token) {
    return { ...authHeader(token), ...jsonHeader }
}

export async function handleError(res) {
    const body = await res.json()
        if(body.error)
            throw body.error
        else
            throw res.statusText
}


export const port = 8081