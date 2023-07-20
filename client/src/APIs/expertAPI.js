import { authHeader, port, handleError } from './util.js';
const url = `http://localhost:${port}/api/experts`;

/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function getTicketsPage(token, noPages) {
    const res = await fetch(url + `/tickets?pageNo=${noPages}`,
        { method: "GET", headers: authHeader(token) })
    if (!res.ok) {const body = await res.json()
        if(body.error)
            throw body.error
        else
            throw res.statusText
}
    const data = await res.json();
    return data;
}


/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function getTicket(token, ticketId) {
    const res = await fetch(url + "/tickets/" + ticketId,
        { method: "GET", headers: authHeader(token) })
    if (!res.ok) {const body = await res.json()
        if(body.error)
            throw body.error
        else
            throw res.statusText
}

    const data = await res.json();
    return data;
}

/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function resolveTicket(token, ticketId) {
    const res = await fetch(url + "/tickets/" + ticketId + "/resolve",
        { method: "PATCH", headers: authHeader(token) })
    if (!res.ok) {const body = await res.json()
        if(body.error)
            throw body.error
        else
            throw res.statusText
}
}


/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function closeTicket(token, ticketId) {
    const res = await fetch(url + "/tickets/" + ticketId + "/close",
        { method: "PATCH", headers: authHeader(token) })
    if (!res.ok) {const body = await res.json()
        if(body.error)
            throw body.error
        else
            throw res.statusText
}

    const data = await res.json();
    return data;
}

/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function sendMessage(token, message, ticketId, files) {

    const formdata = new FormData();
    formdata.append("messageText", message);
    console.log(files)

    if (files) {
        for (let i = 0; i < files.length; i++) 
            formdata.append("attachments", files[i]);
    } 

    const res = await fetch(url + "/tickets/" + ticketId + "/messages",
        {
            method: "POST", headers: { "Authorization": "Bearer " + token, contentType: "multipart/form-data"  },
            body: formdata
        })
    if (!res.ok) {const body = await res.json()
        if(body.error)
            throw body.error
        else
            throw res.statusText
}
    const data = await res.json();
    return data;
}


/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function getMessagesPage(token, ticketId, noPages) {
    const res = await fetch(url + `/tickets/${ticketId}/messages?pageNo=${noPages}`,
        { method: "GET", headers: authHeader(token) })
    if (!res.ok) {const body = await res.json()
        if(body.error)
            throw body.error
        else
            throw res.statusText
}

    const data = await res.json();
    return data;
}

/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function getProductsPage(token, noPages) {
    const res = await fetch(url + `/products?pageNo=${noPages}`,
        { method: "GET", headers: authHeader(token) })
    if (!res.ok) {const body = await res.json()
        if(body.error)
            throw body.error
        else
            throw res.statusText
}
    const data = await res.json();
    return data;
}

/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function getProduct(token, productId) {
    const res = await fetch(url + "/products/" + productId,
        { method: "GET", headers: authHeader(token) })
    if (!res.ok) {const body = await res.json()
        if(body.error)
            throw body.error
        else
            throw res.statusText
}
    const data = await res.json();
    return data;
}

/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function getAttachment(token, ticketId, attachmentName) {
    const res = await fetch(url + "/tickets/" + ticketId + "/attachments/" + attachmentName,
        { method: "GET", headers: authHeader(token) })
    if (!res.ok) {const body = await res.json()
        if(body.error)
            throw body.error
        else
            throw res.statusText
}
    const data = await res.json();
    return data;
}

const expertAPI = {
    getTicketsPage, getTicket, resolveTicket,
    closeTicket, sendMessage, getMessagesPage,
    getProductsPage, getProduct, getAttachment
}

export default expertAPI;
