import { compositeHeader, authHeader, port } from "./util";

const url = `http://localhost:${port}/api/customers`;

/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function getProfile(token) {
    const res = await fetch(url + "/getProfile",
        { method: "GET", headers: authHeader(token) })
    if (!res.ok) throw res.statusText
    const data = await res.json();
    return data;
}

/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function patchProfile(token, profile) {
    const res = await fetch(url + "/editProfile",
        {
            method: "PATCH", headers: compositeHeader(token),
            body: JSON.stringify(profile)
        })
    if (!res.ok) throw res.statusText
}

/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function createTicket(token, ticket) {
    const res = await fetch(url + "/tickets",
        {
            method: "POST", headers: compositeHeader(token),
            body: JSON.stringify(ticket)
        })
    if (!res.ok) throw res.statusText
    const data = await res.json();
    return data;
}

/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function getTicketsPage(token, noPages) {
    const res = await fetch(url + `/tickets?pageNo=${noPages}`,
        { method: "GET", headers: authHeader(token) })
    if (!res.ok) throw res.statusText
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
    if (!res.ok) throw res.statusText
    const data = await res.json();
    return data;
}

/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function reopenTicket(token, ticketId) {
    const res = await fetch(url + "/tickets/" + ticketId + "/reopen",
        { method: "PATCH", headers: authHeader(token) })
    if (!res.ok) throw res.statusText
}


/** 
* @throws {Error} if the data fails
* @throws {String} if the response is not ok
*/
async function compileSurvey(token, ticketId) {
    const res = await fetch(url + "/tickets/" + ticketId + "/compileSurvey",
        { method: "PATCH", headers: authHeader(token) })
    if (!res.ok) throw res.statusText
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

    if (files) {
        for (let i = 0; i < files.length; i++) 
            formdata.append("attachments", files.item(i));
    } 

    formdata.forEach((value, key) => console.log(key + " " + value));


    const res = await fetch(url + "/tickets/" + ticketId + "/messages",
        {
            method: "POST", headers: { "Authorization": "Bearer " + token, contentType: "multipart/form-data"  },
            body: formdata
        })
    if (!res.ok) throw res.statusText
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
    if (!res.ok) throw res.statusText
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
    if (!res.ok) throw res.statusText
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
    if (!res.ok) throw res.statusText
    const data = await res.json();
    return data;
}

async function getAttachment(token, ticketId, attachmentName) {
    console.log("GET ATTACHMENT", ticketId, attachmentName);
    const res = await fetch(url + "/tickets/" + ticketId + "/attachments/" + attachmentName,
        { method: "GET", headers: authHeader(token) })
    
    console.log("RES", res);
    if (!res.ok) throw res.statusText
    const data = await res.json();
    console.log("DATA", data);
    return data;
}

async function registerProduct(token, productId, serialNumber) {

    const productIds = { productId, serialNumber}

    const res = await fetch(url + "/products/registerProduct",
        {
            method: "PATCH", headers: compositeHeader(token),
            body: JSON.stringify(productIds)
        })
    if (!res.ok) throw res.statusText
}

const customerAPI = {
    getProfile, createTicket, getTicket, patchProfile,
    reopenTicket, compileSurvey, sendMessage, getMessagesPage,
    getProduct, getAttachment, registerProduct,
    getProductsPage, getTicketsPage
}

export default customerAPI
